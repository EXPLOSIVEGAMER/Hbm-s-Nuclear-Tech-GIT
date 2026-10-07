package com.hbm.handler;

import org.lwjgl.opengl.GL11;

import com.hbm.extprop.HbmPlayerProps;
import com.hbm.handler.overlay.HoverRay;
import com.hbm.handler.overlay.HudOverlay;
import com.hbm.items.armor.Gravitator;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.GravitatorDragPacket;
import com.hbm.packet.toserver.GravitatorPacket;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;

@SideOnly(Side.CLIENT)
public class GravitatorHandler {

	private static double grabDistance = 4D;

	public static void onCursorClick(Minecraft mc, EntityPlayer player, int button) {

		ItemStack stack = Gravitator.getWorn(player);
		if(stack == null || Gravitator.getFuel(stack) <= 0) return;

		HbmPlayerProps props = HbmPlayerProps.getData(player);

		if(button == 1) {
			if(props.grabbedEntityId == -1) return;

			Vec3[] ray = HoverRay.ray(mc, 1D);
			double dx = ray[1].xCoord - ray[0].xCoord;
			double dy = ray[1].yCoord - ray[0].yCoord;
			double dz = ray[1].zCoord - ray[0].zCoord;
			double len = Math.sqrt(dx * dx + dy * dy + dz * dz);

			PacketDispatcher.wrapper.sendToServer(new GravitatorPacket(GravitatorPacket.THROW, (float) (dx / len), (float) (dy / len), (float) (dz / len)));
			return;
		}

		if(props.grabbedEntityId != -1) {
			PacketDispatcher.wrapper.sendToServer(new GravitatorPacket(GravitatorPacket.RELEASE, -1));
			return;
		}

		EntityLivingBase target = raycast(mc);
		if(target != null) PacketDispatcher.wrapper.sendToServer(new GravitatorPacket(GravitatorPacket.GRAB, target.getEntityId()));
	}

	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {

		if(event.phase != Phase.END) return;

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;
		if(player == null) return;

		HbmPlayerProps props = HbmPlayerProps.getData(player);
		if(props.grabbedEntityId == -1) return;

		if(mc.currentScreen != null || !HudOverlay.interactionsEnabled()) {
			PacketDispatcher.wrapper.sendToServer(new GravitatorPacket(GravitatorPacket.RELEASE, -1));
			return;
		}

		Vec3[] ray = HoverRay.ray(mc, grabDistance);
		PacketDispatcher.wrapper.sendToServer(new GravitatorDragPacket(ray[1].xCoord, ray[1].yCoord, ray[1].zCoord));
	}

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event) {

		if(event.phase != Phase.END || event.side != Side.SERVER) return;

		EntityPlayer player = event.player;
		HbmPlayerProps props = HbmPlayerProps.getData(player);
		if(props.grabbedEntityId == -1) return;

		ItemStack stack = Gravitator.getWorn(player);
		Entity grabbed = player.worldObj.getEntityByID(props.grabbedEntityId);

		if(stack == null || !(grabbed instanceof EntityLivingBase) || grabbed.isDead || grabbed == player || player.getDistanceToEntity(grabbed) > 64D || Gravitator.getFuel(stack) <= 0) {
			props.grabbedEntityId = -1;
			return;
		}

		if(player.ticksExisted % 20 == 0) {
			Gravitator.setFuel(stack, Math.max(Gravitator.getFuel(stack) - 1, 0));

			ItemStack helmet = player.getCurrentArmor(3);
			if(helmet != null && helmet != stack) ArmorModHandler.setMod(helmet, ArmorModHandler.helmet_only, stack);
		}
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event) {

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;
		if(player == null) return;

		HbmPlayerProps props = HbmPlayerProps.getData(player);
		if(props.grabbedEntityId == -1) return;

		Entity entity = player.worldObj.getEntityByID(props.grabbedEntityId);
		if(!(entity instanceof EntityLivingBase)) return;

		double ex = entity.prevPosX + (entity.posX - entity.prevPosX) * event.partialTicks;
		double ey = entity.prevPosY + (entity.posY - entity.prevPosY) * event.partialTicks;
		double ez = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * event.partialTicks;
		double cx = player.prevPosX + (player.posX - player.prevPosX) * event.partialTicks;
		double cy = player.prevPosY + (player.posY - player.prevPosY) * event.partialTicks;
		double cz = player.prevPosZ + (player.posZ - player.prevPosZ) * event.partialTicks;
		double hw = entity.width * 0.5D;
		double h = entity.height;
		double minX = ex - hw, minY = ey, minZ = ez - hw;
		double maxX = ex + hw, maxY = ey + h, maxZ = ez + hw;

		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_COLOR_MATERIAL);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		Tessellator tess = Tessellator.instance;
		tess.startDrawing(GL11.GL_LINES);
		tess.setColorOpaque_F(0.25F, 0.55F, 1F);

		tess.addVertex(minX - cx, maxY - cy, minZ - cz);
		tess.addVertex(minX - cx, minY - cy, minZ - cz);
		tess.addVertex(minX - cx, maxY - cy, minZ - cz);
		tess.addVertex(maxX - cx, maxY - cy, minZ - cz);
		tess.addVertex(maxX - cx, maxY - cy, minZ - cz);
		tess.addVertex(maxX - cx, minY - cy, minZ - cz);
		tess.addVertex(minX - cx, minY - cy, minZ - cz);
		tess.addVertex(maxX - cx, minY - cy, minZ - cz);
		tess.addVertex(maxX - cx, minY - cy, minZ - cz);
		tess.addVertex(maxX - cx, minY - cy, maxZ - cz);
		tess.addVertex(maxX - cx, maxY - cy, maxZ - cz);
		tess.addVertex(maxX - cx, maxY - cy, minZ - cz);
		tess.addVertex(maxX - cx, maxY - cy, maxZ - cz);
		tess.addVertex(maxX - cx, minY - cy, maxZ - cz);
		tess.addVertex(minX - cx, maxY - cy, minZ - cz);
		tess.addVertex(minX - cx, maxY - cy, maxZ - cz);
		tess.addVertex(minX - cx, maxY - cy, maxZ - cz);
		tess.addVertex(minX - cx, minY - cy, maxZ - cz);
		tess.addVertex(minX - cx, maxY - cy, maxZ - cz);
		tess.addVertex(maxX - cx, maxY - cy, maxZ - cz);
		tess.addVertex(minX - cx, minY - cy, maxZ - cz);
		tess.addVertex(maxX - cx, minY - cy, maxZ - cz);
		tess.addVertex(minX - cx, minY - cy, minZ - cz);
		tess.addVertex(minX - cx, minY - cy, maxZ - cz);

		tess.draw();

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
	}

	private static EntityLivingBase raycast(Minecraft mc) {

		EntityPlayer player = mc.thePlayer;
		Vec3[] ray = HoverRay.ray(mc, 64D);
		Vec3 from = ray[0];
		Vec3 to = ray[1];
		AxisAlignedBB search = player.boundingBox.addCoord(to.xCoord - from.xCoord, to.yCoord - from.yCoord, to.zCoord - from.zCoord).expand(1D, 1D, 1D);

		EntityLivingBase best = null;
		double bestDist = 64D * 64D;

		for(Object o : player.worldObj.getEntitiesWithinAABBExcludingEntity(player, search)) {
			if(!(o instanceof EntityLivingBase)) continue;

			EntityLivingBase entity = (EntityLivingBase) o;
			if(entity.isDead) continue;

			MovingObjectPosition mop = entity.boundingBox.expand(0.3D, 0.3D, 0.3D).calculateIntercept(from, to);
			if(mop == null) continue;

			double dist = from.squareDistanceTo(mop.hitVec);

			if(dist < bestDist) {
				bestDist = dist;
				best = entity;
			}
		}

		if(best != null) grabDistance = Math.max(2D, Math.min(48D, Math.sqrt(bestDist)));
		return best;
	}
}
