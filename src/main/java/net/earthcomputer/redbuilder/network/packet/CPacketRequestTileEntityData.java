package net.earthcomputer.redbuilder.network.packet;

import io.netty.buffer.ByteBuf;
import net.earthcomputer.redbuilder.RedBuilder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CPacketRequestTileEntityData implements IMessage {

	private BlockPos pos;

	public CPacketRequestTileEntityData() {
	}

	public CPacketRequestTileEntityData(BlockPos pos) {
		this.pos = pos;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		pos = PacketUtils.readBlockPos(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketUtils.writeBlockPos(buf, pos);
	}

	public static class Handler implements IMessageHandler<CPacketRequestTileEntityData, IMessage> {

		@Override
		public IMessage onMessage(final CPacketRequestTileEntityData message, final MessageContext ctx) {
			FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					TileEntity tileEntity = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.pos);
					NBTTagCompound tag = null;
					if (tileEntity != null) {
						tag = tileEntity.writeToNBT(new NBTTagCompound());
					}
					RedBuilder.instance().getNetwork().sendTo(new SPacketTileEntityData(message.pos, tag),
							ctx.getServerHandler().playerEntity);
				}
			});
			return null;
		}

	}

}
