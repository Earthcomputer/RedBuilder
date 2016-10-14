package net.earthcomputer.redbuilder.network.packet;

import io.netty.buffer.ByteBuf;
import net.earthcomputer.redbuilder.network.ClientInstructionWrapper;
import net.earthcomputer.redbuilder.network.handler.Handlers;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SPacketRedBuilderServer implements IMessage {

	private String version;

	public SPacketRedBuilderServer() {
	}

	public SPacketRedBuilderServer(String version) {
		this.version = version;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		version = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, version);
	}

	public static class Handler implements IMessageHandler<SPacketRedBuilderServer, IMessage> {

		@Override
		public IMessage onMessage(final SPacketRedBuilderServer message, MessageContext ctx) {
			ClientInstructionWrapper.runLater(new Runnable() {
				@Override
				public void run() {
					Handlers.onReceiveServerRedBuilderMessage(message.version);
				}
			});
			return null;
		}

	}

}
