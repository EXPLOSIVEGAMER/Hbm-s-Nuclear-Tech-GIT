package com.hbm.items.tool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.network.hypertube.BlockHyperTube;
import com.hbm.main.MainRegistry;
import com.hbm.render.util.RenderOverhead;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.i18n.I18nUtil;
import com.hbm.wiaj.WorldInAJar;

import api.hbm.hypertube.HyperTubeShape;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.BlockEvent;

public class ItemHyperTubeWand extends Item implements ILookOverlay {

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {

		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			for(String s : I18nUtil.resolveKeyArray(this.getUnlocalizedName(stack) + ".desc")) {
				list.add(EnumChatFormatting.YELLOW + s);
			}
		} else {
			list.add(EnumChatFormatting.DARK_GRAY + "" + EnumChatFormatting.ITALIC + "Hold <" + EnumChatFormatting.YELLOW + EnumChatFormatting.ITALIC + "LSHIFT" + EnumChatFormatting.DARK_GRAY
					+ EnumChatFormatting.ITALIC + "> to display more info");
		}
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fx, float fy, float fz) {
		if(player.isSneaking() && !stack.hasTagCompound()) {
			ForgeDirection dir = ForgeDirection.getOrientation(side);

			x += dir.offsetX;
			y += dir.offsetY;
			z += dir.offsetZ;

			if(world.getBlock(x, y, z).isReplaceable(world, x, y, z)) {
				world.setBlock(x, y, z, ModBlocks.hypertube, HyperTubeShape.getMeta(dir, dir.getOpposite()), 3);
				if(!player.capabilities.isCreativeMode) stack.stackSize--;
			}

			return true;
		}

		side = snapToFreeEnd(world, x, y, z, side);

		if(!stack.hasTagCompound()) {
			NBTTagCompound nbt = stack.stackTagCompound = new NBTTagCompound();

			nbt.setInteger("x", x);
			nbt.setInteger("y", y);
			nbt.setInteger("z", z);
			nbt.setInteger("side", side);

			int count = 0;
			if(player.capabilities.isCreativeMode) {
				count = 256;
			} else {
				for(ItemStack inventoryStack : player.inventory.mainInventory) {
					if(inventoryStack != null && inventoryStack.getItem() == this) {
						count += inventoryStack.stackSize;
					}
				}
			}

			nbt.setInteger("count", count);
		} else {
			NBTTagCompound nbt = stack.stackTagCompound;

			int sx = nbt.getInteger("x");
			int sy = nbt.getInteger("y");
			int sz = nbt.getInteger("z");
			int sSide = nbt.getInteger("side");
			int count = nbt.getInteger("count");

			if(!world.isRemote) {

				int constructCount = construct(world, null, sx, sy, sz, sSide, x, y, z, side, 0, 0, 0, count);
				if(constructCount > 0) {
					int toRemove = construct(world, world, sx, sy, sz, sSide, x, y, z, side, 0, 0, 0, count);

					if(!player.capabilities.isCreativeMode) {
						for(ItemStack inventoryStack : player.inventory.mainInventory) {
							if(inventoryStack != null && inventoryStack.getItem() == this) {
								int removing = Math.min(toRemove, inventoryStack.stackSize);
								inventoryStack.stackSize -= removing;
								toRemove -= removing;
							}

							if(toRemove <= 0) break;
						}

						player.inventoryContainer.detectAndSendChanges();
					}

					player.addChatMessage(new ChatComponentText("Hypertube built!"));
				} else if(constructCount == 0) {
					player.addChatMessage(new ChatComponentText("Not enough hypertubes, build cancelled"));
				} else {
					player.addChatMessage(new ChatComponentText("Hypertube obstructed, build cancelled"));
				}
			} else {
				RenderOverhead.clearActionPreview();
				lastMop = null;
			}

			stack.stackTagCompound = null;
		}

		return true;
	}

	private static MovingObjectPosition lastMop;
	private static int lastSide;

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean inHand) {
		if(!(entity instanceof EntityPlayer)) return;
		EntityPlayer player = (EntityPlayer) entity;

		if(!inHand && stack.hasTagCompound()) {
			ItemStack held = player.getHeldItem();
			if(held == null || held.getItem() != this) {
				stack.stackTagCompound = null;
				if(world.isRemote) {
					RenderOverhead.clearActionPreview();
					lastMop = null;
				}
			}
		}

		if(world.isRemote && inHand) {
			if(!stack.hasTagCompound()) {
				RenderOverhead.clearActionPreview();
				lastMop = null;
				return;
			}

			MovingObjectPosition mop = Minecraft.getMinecraft().objectMouseOver;
			if(mop == null || mop.typeOfHit != MovingObjectType.BLOCK) {
				RenderOverhead.clearActionPreview();
				lastMop = null;
				return;
			}

			int x = mop.blockX;
			int y = mop.blockY;
			int z = mop.blockZ;
			int side = snapToFreeEnd(world, x, y, z, mop.sideHit);

			if(lastMop != null && mop.blockX == lastMop.blockX && mop.blockY == lastMop.blockY && mop.blockZ == lastMop.blockZ && side == lastSide) return;
			lastMop = mop;
			lastSide = side;

			NBTTagCompound nbt = stack.stackTagCompound;

			int sx = nbt.getInteger("x");
			int sy = nbt.getInteger("y");
			int sz = nbt.getInteger("z");
			int sSide = nbt.getInteger("side");
			int count = nbt.getInteger("count");

			int sizeX = Math.abs(sx - x) + 1 + PREVIEW_BUFFER * 2;
			int sizeY = Math.abs(sy - y) + 1 + PREVIEW_BUFFER * 2;
			int sizeZ = Math.abs(sz - z) + 1 + PREVIEW_BUFFER * 2;

			int minX = Math.min(sx, x) - PREVIEW_BUFFER;
			int minY = Math.min(sy, y) - PREVIEW_BUFFER;
			int minZ = Math.min(sz, z) - PREVIEW_BUFFER;

			WorldInAJar wiaj = new WorldInAJar(sizeX, sizeY, sizeZ);
			boolean pathSuccess = construct(world, wiaj, sx, sy, sz, sSide, x, y, z, side, minX, minY, minZ, count) > 0;

			RenderOverhead.setActionPreview(wiaj, minX, minY, minZ, pathSuccess);
		}
	}

	private static final int PREVIEW_BUFFER = 4;

	private static int snapToFreeEnd(World world, int x, int y, int z, int side) {
		Block onBlock = world.getBlock(x, y, z);
		if(!(onBlock instanceof BlockHyperTube)) return side;

		ForgeDirection clicked = ForgeDirection.getOrientation(side);
		int meta = world.getBlockMetadata(x, y, z);

		if(HyperTubeShape.isOpen(meta, clicked) && isFree(world, x + clicked.offsetX, y + clicked.offsetY, z + clicked.offsetZ)) return side;

		for(ForgeDirection end : HyperTubeShape.getEnds(meta)) {
			if(isFree(world, x + end.offsetX, y + end.offsetY, z + end.offsetZ)) return end.ordinal();
		}

		return side;
	}

	private static boolean isFree(World world, int x, int y, int z) {
		return world.getBlock(x, y, z).isReplaceable(world, x, y, z);
	}

	@Override
	public boolean onBlockStartBreak(ItemStack stack, int x, int y, int z, EntityPlayer playerEntity) {
		if(!playerEntity.isSneaking()) return false;

		World world = playerEntity.worldObj;
		Block block = world.getBlock(x, y, z);

		if(!playerEntity.capabilities.isCreativeMode) return false;
		if(!(playerEntity instanceof EntityPlayerMP)) return false;

		EntityPlayerMP player = (EntityPlayerMP) playerEntity;

		if(!world.isRemote && block instanceof BlockHyperTube) {
			for(ForgeDirection end : HyperTubeShape.getEnds(world.getBlockMetadata(x, y, z))) {
				breakExtra(world, player, x + end.offsetX, y + end.offsetY, z + end.offsetZ, 32);
			}
		}

		return false;
	}

	private void breakExtra(World world, EntityPlayerMP player, int x, int y, int z, int depth) {
		depth--;
		if(depth <= 0) return;

		Block block = world.getBlock(x, y, z);
		int meta = world.getBlockMetadata(x, y, z);
		if(!(block instanceof BlockHyperTube)) return;

		ForgeDirection[] ends = HyperTubeShape.getEnds(meta);

		BlockEvent.BreakEvent event = ForgeHooks.onBlockBreakEvent(world, player.theItemInWorldManager.getGameType(), player, x, y, z);
		if(event.isCanceled())
			return;

		block.onBlockHarvested(world, x, y, z, meta, player);
		if(block.removedByPlayer(world, player, x, y, z, false)) {
			block.onBlockDestroyedByPlayer(world, x, y, z, meta);
		}

		player.playerNetServerHandler.sendPacket(new S23PacketBlockChange(x, y, z, world));

		for(ForgeDirection end : ends) {
			breakExtra(world, player, x + end.offsetX, y + end.offsetY, z + end.offsetZ, depth);
		}
	}

	private static int construct(World routeWorld, IBlockAccess buildWorld, int x1, int y1, int z1, int side1, int x2, int y2, int z2, int side2, int box, int boy, int boz, int max) {
		ForgeDirection startDir = ForgeDirection.getOrientation(side1);
		ForgeDirection targetDir = ForgeDirection.getOrientation(side2);

		int tx = x2 + targetDir.offsetX;
		int ty = y2 + targetDir.offsetY;
		int tz = z2 + targetDir.offsetZ;

		int x = x1 + startDir.offsetX;
		int y = y1 + startDir.offsetY;
		int z = z1 + startDir.offsetZ;

		ForgeDirection from = startDir.getOpposite();
		ForgeDirection heading = startDir;
		Set<BlockPos> visited = new HashSet<>();

		for(int loopDepth = 1; loopDepth <= max; loopDepth++) {
			BlockPos pos = new BlockPos(x, y, z);
			if(!isFree(routeWorld, x, y, z) || visited.contains(pos)) return -1;
			visited.add(pos);

			boolean atTarget = x == tx && y == ty && z == tz;
			ForgeDirection out = atTarget ? targetDir.getOpposite() : getNextDirection(routeWorld, x, y, z, tx, ty, tz, heading, from, visited);
			if(out == ForgeDirection.UNKNOWN) return -1;

			ForgeDirection a = from;
			ForgeDirection b = out;
			if(a == b) b = a.getOpposite();
			int meta = HyperTubeShape.getMeta(a, b);

			if(buildWorld instanceof World) {
				((World) buildWorld).setBlock(x - box, y - boy, z - boz, ModBlocks.hypertube, meta, 3);
			} else if(buildWorld instanceof WorldInAJar) {
				((WorldInAJar) buildWorld).setBlock(x - box, y - boy, z - boz, ModBlocks.hypertube, meta);
			}

			if(atTarget) return loopDepth;

			x += out.offsetX;
			y += out.offsetY;
			z += out.offsetZ;
			from = out.getOpposite();
			heading = out;
		}

		return 0;
	}

	private static ForgeDirection getNextDirection(World world, int x, int y, int z, int tx, int ty, int tz, ForgeDirection heading, ForgeDirection back, Set<BlockPos> visited) {
		List<ForgeDirection> closer = new ArrayList<>();
		List<ForgeDirection> other = new ArrayList<>();

		int distance = taxiDistance(x, y, z, tx, ty, tz);

		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if(dir == back) continue;
			if(taxiDistance(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, tx, ty, tz) < distance) closer.add(dir);
			else other.add(dir);
		}

		closer.sort((a, b) -> {
			if(a == heading) return -1;
			if(b == heading) return 1;
			return axisDistance(b, x, y, z, tx, ty, tz) - axisDistance(a, x, y, z, tx, ty, tz);
		});

		other.sort((a, b) -> {
			if(a == heading) return -1;
			if(b == heading) return 1;
			return Math.abs(a.offsetY) - Math.abs(b.offsetY);
		});

		for(ForgeDirection dir : closer) if(canStep(world, x, y, z, dir, visited)) return dir;
		for(ForgeDirection dir : other) if(canStep(world, x, y, z, dir, visited)) return dir;

		return ForgeDirection.UNKNOWN;
	}

	private static boolean canStep(World world, int x, int y, int z, ForgeDirection dir, Set<BlockPos> visited) {
		int nx = x + dir.offsetX;
		int ny = y + dir.offsetY;
		int nz = z + dir.offsetZ;
		return isFree(world, nx, ny, nz) && !visited.contains(new BlockPos(nx, ny, nz));
	}

	private static int axisDistance(ForgeDirection dir, int x, int y, int z, int tx, int ty, int tz) {
		if(dir.offsetX != 0) return Math.abs(tx - x);
		if(dir.offsetY != 0) return Math.abs(ty - y);
		return Math.abs(tz - z);
	}

	private static int taxiDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
		return Math.abs(x1 - x2) + Math.abs(y1 - y2) + Math.abs(z1 - z2);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(Pre event, World world, int x, int y, int z) {
		EntityPlayer player = MainRegistry.proxy.me();
		if(player == null || !player.isSneaking() || !player.capabilities.isCreativeMode) return;

		Block block = world.getBlock(x, y, z);
		if(block instanceof BlockHyperTube) {
			List<String> text = new ArrayList<>();
			text.add("Break whole hypertube line");
			ILookOverlay.printGeneric(event, I18nUtil.resolveKey(block.getUnlocalizedName() + ".name"), 0xffff00, 0x404000, text);
		}
	}
}
