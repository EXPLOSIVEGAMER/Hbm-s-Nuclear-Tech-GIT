package com.hbm.blocks.network.hypertube;

import api.hbm.block.IToolable;
import api.hbm.hypertube.HyperTubeShape;
import com.hbm.items.ModItems;
import com.hbm.tileentity.network.hypertube.TileEntityHyperTube;
import com.hbm.tileentity.network.hypertube.TileEntityHyperTubeBaseNT;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

public class BlockHyperTube extends BlockContainer implements IToolable {
	public static int renderID = RenderingRegistry.getNextAvailableRenderId();

	@Override
	public int getRenderType() {
		return renderID;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	public BlockHyperTube() { super(Material.iron); }

	@Override
	public TileEntity createNewTileEntity(World world, int i) {
		return new TileEntityHyperTube();
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {
		if (tool != ToolType.SCREWDRIVER || world.isRemote) return false;

		ForgeDirection[] ends = HyperTubeShape.getEnds(world.getBlockMetadata(x, y, z));
		ForgeDirection a = ends[0], b = ends[1];

		if (!player.isSneaking()) {
			ForgeDirection axis = ForgeDirection.getOrientation(side);
			a = a.getRotation(axis);
			b = b.getRotation(axis);
		} else {
			do { b = ForgeDirection.getOrientation((b.ordinal() + 1) % 6); } while (b == a);
		}

		world.setBlockMetadataWithNotify(x, y, z, HyperTubeShape.getMeta(a, b), 3);

		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof TileEntityHyperTubeBaseNT) ((TileEntityHyperTubeBaseNT) te).rebuildNode();
		return true;
	}

	@Override
	public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		return ModItems.hypertube_wand;
	}

	@Override
	public Item getItem(World p_149694_1_, int p_149694_2_, int p_149694_3_, int p_149694_4_) {
		return ModItems.hypertube_wand;
	}
}
