package com.hbm.packet.toserver;

import com.hbm.extprop.HbmPlayerProps;
import com.hbm.items.armor.Jetpack;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class JetpackControlPacket implements IMessage {

	public static final int TOGGLE = 0;
	public static final int CYCLE_MODE = 1;

	int action;

	public JetpackControlPacket() { }

	public JetpackControlPacket(int action) {
		this.action = action;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		action = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(action);
	}

	public static class Handler implements IMessageHandler<JetpackControlPacket, IMessage> {

		@Override
		public IMessage onMessage(JetpackControlPacket m, MessageContext ctx) {

			EntityPlayer player = ctx.getServerHandler().playerEntity;
			HbmPlayerProps props = HbmPlayerProps.getData(player);

			if(m.action == TOGGLE) {
				props.toggleJetpack();
			} else if(m.action == CYCLE_MODE) {
				props.jetpackMode = (props.jetpackMode + 1) % Jetpack.MODE_COUNT;
			}

			return null;
		}
	}
}
