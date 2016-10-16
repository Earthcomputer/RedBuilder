package net.earthcomputer.redbuilder.network.packet;

import io.netty.buffer.ByteBuf;
import net.earthcomputer.redbuilder.RedBuilder;
import net.earthcomputer.redbuilder.network.handler.ClientSideRedBuilderHandler;
import net.earthcomputer.redbuilder.network.handler.Handlers;
import net.earthcomputer.redbuilder.network.handler.IUniformInstructionHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SPacketTileEntityData implements IMessage {

	private BlockPos pos;
	private NBTTagCompound data;

	public SPacketTileEntityData() {
	}

	public SPacketTileEntityData(BlockPos pos, NBTTagCompound data) {
		this.pos = pos;
		this.data = data;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.pos = PacketUtils.readBlockPos(buf);
		this.data = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketUtils.writeBlockPos(buf, pos);
		ByteBufUtils.writeTag(buf, data);
	}

	public static class Handler implements IMessageHandler<SPacketTileEntityData, IMessage> {

		@Override
		public IMessage onMessage(SPacketTileEntityData message, MessageContext ctx) {
			IUniformInstructionHandler handler = Handlers.getClientSideInstructionHandler();
			if (handler instanceof ClientSideRedBuilderHandler) {
				((ClientSideRedBuilderHandler) handler).onTileEntityDataReceived(message.pos, message.data);
			} else {
				RedBuilder.LOGGER.error(
						"SPacketTileEntityData packet received but the client-side handler was not a ClientSideRedBuilderHandler");
			}
			return null;
		}

	}

}
