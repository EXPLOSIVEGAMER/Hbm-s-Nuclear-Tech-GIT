package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.machine.TileEntityDriveRack;
import net.minecraft.block.material.Material;
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
}
