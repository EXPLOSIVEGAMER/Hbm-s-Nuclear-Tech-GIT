package com.hbm.tileentity.machine;

import com.hbm.items.ModItems;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;

public class TileEntityDriveRack extends TileEntityMachineBase {
	public TileEntityDriveRack() { super(25); }

	// pripyat
	public static final int[] all_slots = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24 };

	@Override
	public String getName() { return "tile.drive_rack"; }

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		if (i >= 0 && i <= 24 && (stack.getItem() == ModItems.full_drive || stack.getItem() == ModItems.hard_drive)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return all_slots;
	}

	@Override
	public void updateEntity() {
		if (!worldObj.isRemote) {
			networkPackNT(50);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);

		for (ItemStack slot : slots) {
			BufferUtil.writeItemStack(buf, slot);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);

		for (int i = 0; i < slots.length; i++) {
			slots[i] = BufferUtil.readItemStack(buf);
		}
	}
}
