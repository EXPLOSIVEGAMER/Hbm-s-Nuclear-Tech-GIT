package com.hbm.handler.overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.lwjgl.opengl.Display;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IEnergyHandlerMK2;
import api.hbm.fluidmk2.IFluidUserMK2;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

@SideOnly(Side.CLIENT)
public class MachineHover {

	public static String machineTitle;
	public static final List<String> machineLines = new ArrayList<>();

	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {

		if(event.phase != Phase.END) return;

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;

		if(player == null || mc.currentScreen != null || !Display.isActive() || !HudOverlay.interactionsEnabled()) {
			clear();
			return;
		}

		MovingObjectPosition mop = HoverRay.raycast(mc, 128D);

		if(mop == null || mop.typeOfHit != MovingObjectType.BLOCK) {
			clear();
			return;
		}

		TileEntity te = mc.theWorld.getTileEntity(mop.blockX, mop.blockY, mop.blockZ);

		if(te == null || te instanceof TileEntityPipeBaseNT || !isMachine(te)) {
			clear();
			return;
		}

		inspect(mc, te, mop.blockX, mop.blockY, mop.blockZ);
	}

	private static boolean isMachine(TileEntity te) {
		return te instanceof TileEntityMachineBase || te instanceof IEnergyHandlerMK2 || te instanceof IFluidUserMK2;
	}

	private static void inspect(Minecraft mc, TileEntity te, int x, int y, int z) {

		machineTitle = I18nUtil.resolveKey(mc.theWorld.getBlock(x, y, z).getUnlocalizedName() + ".name");
		machineLines.clear();

		Integer progress = intField(te, "progress");
		Integer processTime = intField(te, "processTime");
		if(processTime == null) processTime = intField(te, "processTimeBase");

		if(progress != null && processTime != null && processTime > 0) {
			machineLines.add("Progress: " + (progress * 100 / processTime) + "% (" + progress + " / " + processTime + " t)");
		}

		Integer heat = intField(te, "heat");
		if(heat != null) machineLines.add("Heat: " + amount(heat) + "°C");

		if(te instanceof IEnergyHandlerMK2) {
			IEnergyHandlerMK2 energy = (IEnergyHandlerMK2) te;
			machineLines.add("Energy: " + amount(energy.getPower()) + " / " + amount(energy.getMaxPower()) + " HE");
		}

		if(te instanceof IFluidUserMK2) {
			for(FluidTank tank : ((IFluidUserMK2) te).getAllTanks()) {
				if(tank == null) continue;

				FluidType type = tank.getTankType();
				if(type == null || type == Fluids.NONE) continue;

				machineLines.add("&[" + type.getColor() + "&]" + type.getLocalizedName() + ": " + amount(tank.getFill()) + " / " + amount(tank.getMaxFill()) + " mB");
			}
		}
	}

	private static Integer intField(Object obj, String name) {
		try {
			return obj.getClass().getField(name).getInt(obj);
		} catch(Exception ex) {
			return null;
		}
	}

	private static String amount(long value) {
		return String.format(Locale.US, "%,d", value);
	}

	private static void clear() {
		machineTitle = null;
		machineLines.clear();
	}
}
