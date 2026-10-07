package com.hbm.handler.overlay;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import com.hbm.handler.GravitatorHandler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

@SideOnly(Side.CLIENT)
public class HudOverlayManager {

	private final List<HudOverlay> overlays = new ArrayList<>();
	private boolean lastLeft = false;
	private boolean lastRight = false;

	public HudOverlayManager register(HudOverlay overlay) {
		this.overlays.add(overlay);
		return this;
	}

	@SubscribeEvent
	public void onRenderHUD(RenderGameOverlayEvent.Post event) {

		if(event.type != ElementType.HOTBAR) return;

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;

		if(player == null || mc.currentScreen != null) return;

		int width = event.resolution.getScaledWidth();
		int height = event.resolution.getScaledHeight();
		int[] mouse = getMouse(mc);

		List<HudOverlay> visible = layout(mc, player, width, height);

		for(HudOverlay overlay : visible) {
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glColor4f(1F, 1F, 1F, 1F);
			overlay.drawScreen(mouse[0], mouse[1], event.partialTicks);
		}

		GL11.glColor4f(1F, 1F, 1F, 1F);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);
	}

	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {

		if(event.phase != Phase.END) return;

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;
		boolean interactive = player != null && mc.currentScreen == null && Display.isActive() && HudOverlay.interactionsEnabled();
		boolean left = interactive && Mouse.isButtonDown(0);
		boolean right = interactive && Mouse.isButtonDown(1);

		if((left && !lastLeft) || (right && !lastRight)) {

			if(mc.inGameHasFocus) {
				mc.inGameHasFocus = false;
				Mouse.setGrabbed(false);
			}

			int button = left && !lastLeft ? 0 : 1;
			ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
			int[] mouse = getMouse(mc);
			boolean consumed = false;

			for(HudOverlay overlay : layout(mc, player, res.getScaledWidth(), res.getScaledHeight())) {
				if(overlay.mouseClicked(mouse[0], mouse[1], button)) {
					consumed = true;
					break;
				}
			}

			if(!consumed) GravitatorHandler.onCursorClick(mc, player, button);
		}

		lastLeft = left;
		lastRight = right;
	}

	private List<HudOverlay> layout(Minecraft mc, EntityPlayer player, int width, int height) {

		List<HudOverlay> visible = new ArrayList<>();
		int total = 0;

		for(HudOverlay overlay : this.overlays) {
			if(!overlay.isVisible(mc, player)) continue;
			visible.add(overlay);
			if(overlay.sortOnSide()) total += overlay.getSize()[1] + 8;
		}

		int y = (height - total) / 2 + 4;

		for(HudOverlay overlay : visible) {
			overlay.screenWidth = width;
			overlay.screenHeight = height;

			if(overlay.sortOnSide()) {
				int[] size = overlay.getSize();
				overlay.guiLeft = width - size[0];
				overlay.guiTop = y;
				y += size[1] + 8;
			} else {
				int[] anchor = overlay.getAnchor(width, height);
				overlay.guiLeft = anchor[0];
				overlay.guiTop = anchor[1];
			}
		}

		return visible;
	}

	private static int[] getMouse(Minecraft mc) {
		ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
		return new int[] { Mouse.getX() * res.getScaledWidth() / mc.displayWidth, res.getScaledHeight() - Mouse.getY() * res.getScaledHeight() / mc.displayHeight - 1 };
	}
}
