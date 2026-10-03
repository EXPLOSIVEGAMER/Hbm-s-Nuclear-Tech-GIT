package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.dim.CelestialBody;
import com.hbm.items.ItemVOTVdrive;
import com.hbm.tileentity.machine.TileEntityDriveRack;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockDriveRack extends BlockDummyable {
	public BlockDriveRack(Material mat) { super(mat); }

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {


		// I have no fucking idea who put this here, but when I deleted it the game wouldn’t start.
		// Words cannot describe my fucking confusion.
		if (meta >= 6) {
			return new TileEntityDriveRack();
		}
		return null;
	}

	@Override
	public int[] getDimensions() { return new int[] {1, 0, 0, 0, 0, 0}; }

	@Override
	public int getOffset() { return 0; }

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if (!CelestialBody.inOrbit(world)) return false;

		if (world.isRemote) {
			return true;
		} else {
			int[] pos = this.findCore(world, x, y, z);

			if (pos == null)
				return false;

			TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);

			if (!(te instanceof TileEntityDriveRack))
				return false;

			TileEntityDriveRack rack = (TileEntityDriveRack) te;

			ItemStack heldStack = player.getHeldItem();

			if (heldStack != null && heldStack.getItem() instanceof ItemVOTVdrive) {
				for (int i = 0; i < rack.slots.length; i++) {
					if (rack.slots[i] == null) {
						rack.slots[i] = heldStack.copy();
						heldStack.stackSize = 0;
						rack.markChanged();
						world.playSoundEffect(
							x, y, z,
							"hbm:item.upgradePlug",
							1.0F,
							1.0F
						);
						break;
					}
				}
			} else if (heldStack == null) {
				for (int i = 0; i < rack.slots.length; i++) {
					if (rack.slots[i] != null) {
						if (!player.inventory.addItemStackToInventory(rack.slots[i].copy())) {
							player.dropPlayerItemWithRandomChoice(
								rack.slots[i].copy(),
								false
							);
						}

						rack.slots[i] = null;
						rack.markChanged();
						break;
					}
				}
			}
		}
		return true;
	}
}
