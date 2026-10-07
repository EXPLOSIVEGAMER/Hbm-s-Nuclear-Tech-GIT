package com.hbm.packet.toserver;

import com.hbm.extprop.HbmPlayerProps;
import com.hbm.items.armor.Gravitator;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class GravitatorDragPacket implements IMessage {

	double x, y, z;

	public GravitatorDragPacket() { }

	public GravitatorDragPacket(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		x = buf.readDouble();
		y = buf.readDouble();
		z = buf.readDouble();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeDouble(x);
		buf.writeDouble(y);
		buf.writeDouble(z);
	}

	public static class Handler implements IMessageHandler<GravitatorDragPacket, IMessage> {

		@Override
		public IMessage onMessage(GravitatorDragPacket m, MessageContext ctx) {

			EntityPlayer player = ctx.getServerHandler().playerEntity;
			HbmPlayerProps props = HbmPlayerProps.getData(player);
			if(props.grabbedEntityId == -1) return null;

			ItemStack stack = Gravitator.getWorn(player);
			if(stack == null || Gravitator.getFuel(stack) <= 0) return null;

			Entity grabbed = player.worldObj.getEntityByID(props.grabbedEntityId);
			if(!(grabbed instanceof EntityLivingBase) || grabbed.isDead) return null;

			double ex = m.x - player.posX;
			double ey = m.y - player.posY - player.getEyeHeight();
			double ez = m.z - player.posZ;
			if(ex * ex + ey * ey + ez * ez > 64D * 64D) return null;

			double tx = m.x;
			double ty = m.y;
			double tz = m.z;

			if(grabbed instanceof EntityPlayerMP) {
				((EntityPlayerMP) grabbed).playerNetServerHandler.setPlayerLocation(tx, ty, tz, grabbed.rotationYaw, grabbed.rotationPitch);
			} else {
				grabbed.setPosition(tx, ty, tz);
			}

			grabbed.motionX = 0D;
			grabbed.motionY = 0D;
			grabbed.motionZ = 0D;
			grabbed.fallDistance = 0F;
			grabbed.velocityChanged = true;

			return null;
		}
	}
}
