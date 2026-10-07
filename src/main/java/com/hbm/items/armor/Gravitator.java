package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;

import api.hbm.fluidmk2.IFillableItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

public class Gravitator extends ItemArmorMod implements IFillableItem {

	public static final int MAX_FUEL = 1000;

	public Gravitator() {
		super(ArmorModHandler.helmet_only, true, false, false, false);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		list.add(EnumChatFormatting.LIGHT_PURPLE + Fluids.FERROFLUID.getLocalizedName() + ": " + getFuel(stack) + "mB / " + MAX_FUEL + "mB");
		list.add("");
		super.addInformation(stack, player, list, ext);
		list.add(EnumChatFormatting.GOLD + "Can be worn on its own!");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addDesc(List list, ItemStack stack, ItemStack armor) {
		list.add(EnumChatFormatting.RED + "  " + stack.getDisplayName() + " (" + Fluids.FERROFLUID.getLocalizedName() + ": " + getFuel(stack) + "mB / " + MAX_FUEL + "mB)");
	}

	@Override
	public boolean isValidArmor(ItemStack stack, int armorType, Entity entity) {
		return armorType == 0;
	}

	public static ItemStack getWorn(EntityPlayer player) {
		ItemStack helmet = player.getCurrentArmor(3);
		if(helmet == null) return null;
		if(helmet.getItem() instanceof Gravitator) return helmet;

		if(ArmorModHandler.hasMods(helmet)) {
			ItemStack mod = ArmorModHandler.pryMod(helmet, ArmorModHandler.helmet_only);
			if(mod != null && mod.getItem() instanceof Gravitator) return mod;
		}

		return null;
	}

	public static int getFuel(ItemStack stack) {
		if(stack.stackTagCompound == null) {
			stack.stackTagCompound = new NBTTagCompound();
			return 0;
		}

		return stack.stackTagCompound.getInteger("fuel");
	}

	public static void setFuel(ItemStack stack, int fuel) {
		if(stack.stackTagCompound == null) stack.stackTagCompound = new NBTTagCompound();
		stack.stackTagCompound.setInteger("fuel", fuel);
	}

	@Override
	public boolean acceptsFluid(FluidType type, ItemStack stack) {
		return type == Fluids.FERROFLUID;
	}

	@Override
	public int tryFill(FluidType type, int amount, ItemStack stack) {
		if(!acceptsFluid(type, stack)) return amount;

		int toFill = Math.min(amount, MAX_FUEL - getFuel(stack));
		setFuel(stack, getFuel(stack) + toFill);

		return amount - toFill;
	}

	@Override
	public boolean providesFluid(FluidType type, ItemStack stack) {
		return false;
	}

	@Override
	public int tryEmpty(FluidType type, int amount, ItemStack stack) {
		return 0;
	}

	@Override
	public FluidType getFirstFluidType(ItemStack stack) {
		return null;
	}

	@Override
	public int getFill(ItemStack stack) {
		return 0;
	}
}
