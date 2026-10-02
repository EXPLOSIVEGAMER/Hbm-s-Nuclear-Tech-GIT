package com.hbm.tileentity.machine;

import com.hbm.items.ModItems;
import com.hbm.tileentity.TileEntityMachineBase;
import net.minecraft.item.ItemStack;

public class TileEntityDriveRack extends TileEntityMachineBase {
	public TileEntityDriveRack() { super(24); }

	// pripyat
	private static final int[] slots = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23 };

	@Override
	public String getName() { return "tile.drive_rack"; }

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		if (i >= 0 && i <= 23 && (stack.getItem() == ModItems.full_drive || stack.getItem() == ModItems.hard_drive)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return slots;
	}

	@Override
	public void updateEntity() {

	}
}
