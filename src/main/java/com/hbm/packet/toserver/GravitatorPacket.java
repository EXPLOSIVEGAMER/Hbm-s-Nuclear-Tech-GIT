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
import net.minecraft.item.ItemStack;

public class GravitatorPacket implements IMessage {

	public static final byte GRAB = 0;
	public static final byte RELEASE = 1;
	public static final byte THROW = 2;

	byte action;
	int entityId;
	float dirX, dirY, dirZ;

	public GravitatorPacket() { }

	public GravitatorPacket(byte action, int entityId) {
		this.action = action;
		this.entityId = entityId;
	}

	public GravitatorPacket(byte action, float dirX, float dirY, float dirZ) {
		this.action = action;
		this.dirX = dirX;
		this.dirY = dirY;
		this.dirZ = dirZ;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		action = buf.readByte();
		entityId = buf.readInt();
		dirX = buf.readFloat();
		dirY = buf.readFloat();
		dirZ = buf.readFloat();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(action);
		buf.writeInt(entityId);
		buf.writeFloat(dirX);
		buf.writeFloat(dirY);
		buf.writeFloat(dirZ);
	}

	public static class Handler implements IMessageHandler<GravitatorPacket, IMessage> {

		@Override
		public IMessage onMessage(GravitatorPacket m, MessageContext ctx) {

			EntityPlayer player = ctx.getServerHandler().playerEntity;
			HbmPlayerProps props = HbmPlayerProps.getData(player);
			ItemStack stack = Gravitator.getWorn(player);

			if(stack == null || Gravitator.getFuel(stack) <= 0) return null;

			if(m.action == GRAB) {
				if(props.grabbedEntityId != -1) return null;

				Entity target = player.worldObj.getEntityByID(m.entityId);

				if(target instanceof EntityLivingBase && target != player && !target.isDead && player.getDistanceToEntity(target) <= 64D) {
					props.grabbedEntityId = target.getEntityId();
				}

			} else if(m.action == RELEASE) {
				props.grabbedEntityId = -1;

			} else if(m.action == THROW) {
				Entity grabbed = props.grabbedEntityId == -1 ? null : player.worldObj.getEntityByID(props.grabbedEntityId);

				if(grabbed != null) {
					grabbed.motionX = m.dirX * 2.0D;
					grabbed.motionY = m.dirY * 2.0D + 0.25D;
					grabbed.motionZ = m.dirZ * 2.0D;
					grabbed.velocityChanged = true;
				}

				props.grabbedEntityId = -1;
			}

			return null;
		}
	}
}
