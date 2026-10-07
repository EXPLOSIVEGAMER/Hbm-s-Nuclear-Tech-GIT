package com.hbm.handler;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;

@SideOnly(Side.CLIENT)
public class FreeCursorHandler {

	private boolean cursorFree = false;

	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {

		Minecraft mc = Minecraft.getMinecraft();
		boolean ctrl = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);

		if(ctrl && mc.currentScreen == null && mc.thePlayer != null && mc.inGameHasFocus && Display.isActive()) {
			cursorFree = true;
			mc.inGameHasFocus = false;
			Mouse.setGrabbed(false);
		} else if(cursorFree && !ctrl) {
			cursorFree = false;

			if(!mc.inGameHasFocus && mc.currentScreen == null && mc.thePlayer != null && Display.isActive()) {
				mc.setIngameFocus();
			}
		}
	}
}
