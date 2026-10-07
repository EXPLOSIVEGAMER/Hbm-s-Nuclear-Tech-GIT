package com.hbm.handler.overlay;

import com.hbm.extprop.HbmPlayerProps;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.armor.Gravitator;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class GravitatorOverlay extends HudOverlay {

	private final FluidTank tank = new FluidTank(Fluids.FERROFLUID, Gravitator.MAX_FUEL);

	@Override
	public boolean isVisible(Minecraft mc, EntityPlayer player) {
		return player != null && HbmPlayerProps.getData(player).enableHUD && Gravitator.getWorn(player) != null;
	}

	@Override
	public boolean sortOnSide() {
		return true;
	}

	@Override
	public int[] getSize() {
		return new int[] { 35, 21 };
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;
		ItemStack stack = Gravitator.getWorn(player);

		if(stack == null) return;

		mc.renderEngine.bindTexture(new ResourceLocation("hbm:textures/gui/tool/gui_gravitator_overlay.png"));
		func_146110_a(guiLeft, guiTop, 0, 0, 35, 21, 42F, 21F);

		if(HbmPlayerProps.getData(player).grabbedEntityId != -1) {
			func_146110_a(guiLeft + 25, guiTop + 3, 35, 0, 7, 3, 42F, 21F);
		}

		tank.setTankType(Fluids.FERROFLUID);
		tank.setFill(Gravitator.getFuel(stack));
		tank.renderTank(guiLeft + 25, guiTop + 16, this.zLevel, 7, 7);
	}
}
