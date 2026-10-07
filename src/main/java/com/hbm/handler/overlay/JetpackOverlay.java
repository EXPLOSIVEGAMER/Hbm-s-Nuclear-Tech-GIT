package com.hbm.handler.overlay;

import org.lwjgl.opengl.GL11;

import com.hbm.extprop.HbmPlayerProps;
import com.hbm.items.armor.Jetpack;
import com.hbm.items.armor.JetpackFueledBase;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.JetpackControlPacket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class JetpackOverlay extends HudOverlay {

	@Override
	public boolean isVisible(Minecraft mc, EntityPlayer player) {
		return player != null && HbmPlayerProps.getData(player).enableHUD && Jetpack.getWorn(player) != null;
	}

	@Override
	public boolean sortOnSide() {
		return true;
	}

	@Override
	public int[] getSize() {
		return new int[] { 90, 47 };
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;
		ItemStack jetpack = Jetpack.getWorn(player);
		if(jetpack == null) return;

		HbmPlayerProps props = HbmPlayerProps.getData(player);
		int mode = ((props.jetpackMode % Jetpack.MODE_COUNT) + Jetpack.MODE_COUNT) % Jetpack.MODE_COUNT;

		mc.renderEngine.bindTexture(new ResourceLocation("hbm:textures/gui/tool/gui_jetpack_overlay.png"));

		func_146110_a(guiLeft, guiTop, 0, 0, 90, 47, 119F, 53F);
		drawTank(guiLeft, guiTop, jetpack);

		if(mode == Jetpack.MODE_BUILDER) func_146110_a(guiLeft + 31, guiTop + 23, 90, 17, 18, 18, 119F, 53F);
		else if(mode == Jetpack.MODE_VECTOR) func_146110_a(guiLeft + 31, guiTop + 23, 90, 35, 18, 18, 119F, 53F);

		if(props.enableBackpack) func_146110_a(guiLeft + 55, guiTop + 20, 90, 0, 29, 17, 119F, 53F);

		if(interactionsEnabled()) {
			if(checkClick(mouseX, mouseY, 55, 20, 29, 17)) drawGradientRect(guiLeft + 55, guiTop + 20, guiLeft + 84, guiTop + 37, 0x40FFFFFF, 0x40FFFFFF);
			if(checkClick(mouseX, mouseY, 31, 23, 18, 18)) drawGradientRect(guiLeft + 31, guiTop + 23, guiLeft + 49, guiTop + 41, 0x40FFFFFF, 0x40FFFFFF);
		}
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {

		if(button != 0) return false;

		if(checkClick(mouseX, mouseY, 55, 20, 29, 17)) {
			click(JetpackControlPacket.TOGGLE);
			return true;
		}

		if(checkClick(mouseX, mouseY, 31, 23, 18, 18)) {
			click(JetpackControlPacket.CYCLE_MODE);
			return true;
		}

		return false;
	}

	private static void click(int action) {
		Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
		PacketDispatcher.wrapper.sendToServer(new JetpackControlPacket(action));
	}

	private static void drawTank(int x, int y, ItemStack jetpack) {

		if(!(jetpack.getItem() instanceof JetpackFueledBase)) return;

		JetpackFueledBase item = (JetpackFueledBase) jetpack.getItem();
		float fill = MathHelper.clamp_float((float) JetpackFueledBase.getFuel(jetpack) / (float) item.maxFuel, 0F, 1F);
		if(fill <= 0F) return;

		float top = 42 - (42 - 25) * fill;
		float right = 5 + (top - 25) * (21 - 5) / (float) (42 - 25);
		int color = item.fuel.getColor();
		float r = (color >> 16 & 0xFF) / 255F;
		float g = (color >> 8 & 0xFF) / 255F;
		float b = (color & 0xFF) / 255F;

		GL11.glDisable(GL11.GL_TEXTURE_2D);

		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		tess.setColorOpaque_F(r, g, b);
		tess.addVertex(x + 5, y + 42, 0);
		tess.addVertex(x + 21, y + 42, 0);
		tess.addVertex(x + right, y + top, 0);
		tess.addVertex(x + 5, y + top, 0);
		tess.draw();

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}
}
