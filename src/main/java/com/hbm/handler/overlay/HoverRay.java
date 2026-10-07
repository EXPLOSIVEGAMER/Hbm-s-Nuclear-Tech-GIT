package com.hbm.handler.overlay;

import org.lwjgl.input.Mouse;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

@SideOnly(Side.CLIENT)
public class HoverRay {

	public static Vec3[] ray(Minecraft mc, double range) {

		EntityPlayer player = mc.thePlayer;
		double nx = (double) Mouse.getX() / (double) mc.displayWidth * 2D - 1D;
		double ny = (double) Mouse.getY() / (double) mc.displayHeight * 2D - 1D;
		double aspect = (double) mc.displayWidth / (double) mc.displayHeight;
		double tan = Math.tan(Math.toRadians(mc.entityRenderer.getFOVModifier(1F, true)) / 2D);
		double yaw = Math.toRadians(player.rotationYaw);
		double pitch = Math.toRadians(player.rotationPitch);
		double lookX = -Math.sin(yaw) * Math.cos(pitch);
		double lookY = -Math.sin(pitch);
		double lookZ = Math.cos(yaw) * Math.cos(pitch);
		double rightX = -Math.cos(yaw);
		double rightZ = -Math.sin(yaw);
		double upX = -rightZ * lookY;
		double upY = rightZ * lookX - rightX * lookZ;
		double upZ = rightX * lookY;
		double dirX = lookX + rightX * nx * aspect * tan + upX * ny * tan;
		double dirY = lookY + upY * ny * tan;
		double dirZ = lookZ + rightZ * nx * aspect * tan + upZ * ny * tan;
		double len = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
		dirX /= len;
		dirY /= len;
		dirZ /= len;
		double ex = player.posX;
		double ey = player.posY + player.getEyeHeight();
		double ez = player.posZ;

		return new Vec3[] { Vec3.createVectorHelper(ex, ey, ez), Vec3.createVectorHelper(ex + dirX * range, ey + dirY * range, ez + dirZ * range) };
	}

	public static MovingObjectPosition raycast(Minecraft mc, double range) {
		Vec3[] ray = ray(mc, range);
		return mc.theWorld.func_147447_a(ray[0], ray[1], false, false, true);
	}
}
