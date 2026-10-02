package com.hbm.tileentity.machine;

import com.hbm.tileentity.TileEntityMachineBase;

public class TileEntityDriveRack extends TileEntityMachineBase {
	public TileEntityDriveRack() { super(24); }

	@Override
	public String getName() { return "tile.drive_rack"; }

	@Override
	public void updateEntity() {
		// TODO: make this class actually do something
		System.out.println("unfinished TE");
	}
}
