package com.hbm.blocks.network.hypertube;

import api.hbm.hypertube.HyperTubeNode;
import api.hbm.hypertube.HyperTubeShape;
import api.hbm.hypertube.IHyperTubeReceiver;
import com.hbm.tileentity.network.hypertube.TileEntityHyperTubeBaseNT;
import com.hbm.tileentity.network.hypertube.TileEntityHyperTubeStation;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.HyperTubeNetworkProvider;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockHyperTubeStation extends BlockContainer {
	public BlockHyperTubeStation() { super(Material.iron); }

	@Override
	public TileEntity createNewTileEntity(World world, int i) {
		return new TileEntityHyperTubeStation();
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
		return ForgeDirection.getOrientation(side).getOpposite().ordinal();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) return true;

		TileEntity te = world.getTileEntity(x, y, z);
		if (!(te instanceof  TileEntityHyperTubeStation)) return false;
		TileEntityHyperTubeStation station = (TileEntityHyperTubeStation) te;

		if (player.isSneaking()) {
			// Debug Purpose
			DirPos pos = station.getTubeConnection();
			HyperTubeNode node = (HyperTubeNode) UniNodespace.getNode(world, pos.getX(), pos.getY(), pos.getZ(), HyperTubeNetworkProvider.PROVIDER);
			if (node == null || !node.hasValidNet()) {
				player.addChatComponentMessage(new ChatComponentText(station.getStationName() + ": no network"));
			} else {
				player.addChatComponentMessage(new ChatComponentText(station.getStationName()
					+ ": " + node.net.receiverEntries.size() + " destinations, "
					+ node.net.links.size() + " tubes"));
			}
		} else {
			if (!station.destinations.isEmpty()) {
			TileEntityHyperTubeStation.Destination destination = station.destinations.get(0);
			station.startTravel(player, (IHyperTubeReceiver) world.getTileEntity(destination.x, destination.y, destination.z));
			}
		}
		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
			if (world.isRemote) return;

		ForgeDirection facing = ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z));
		int tx = x + facing.offsetX, ty = y + facing.offsetY, tz = z + facing.offsetZ;
		if(!(world.getBlock(tx, ty, tz) instanceof BlockHyperTube)) return;

		ForgeDirection toStation = facing.getOpposite();
		int meta = world.getBlockMetadata(tx, ty, tz);
		if(HyperTubeShape.isOpen(meta, toStation)) return;

		ForgeDirection[] ends = HyperTubeShape.getEnds(meta);
		for(ForgeDirection end : ends) {
			ForgeDirection other = ends[0] == end ? ends[1] : ends[0];
			Block neighbour = world.getBlock(tx + other.offsetX, ty + other.offsetY, tz + other.offsetZ);
			if (neighbour.isReplaceable(world, tx + other.offsetX, ty + other.offsetY, tz + other.offsetZ)) {
				world.setBlockMetadataWithNotify(tx, ty, tz, HyperTubeShape.getMeta(end, toStation), 3);
				TileEntity te = world.getTileEntity(tx, ty, tz);
				if (te instanceof TileEntityHyperTubeBaseNT) ((TileEntityHyperTubeBaseNT) te).rebuildNode();
				return;
			}
		}
	}
}
