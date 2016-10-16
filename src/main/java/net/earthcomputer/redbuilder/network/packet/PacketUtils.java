package net.earthcomputer.redbuilder.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketUtils {

	private PacketUtils() {
	}

	public static BlockPos readBlockPos(ByteBuf buf) {
		return new BlockPos(ByteBufUtils.readVarInt(buf, 4), ByteBufUtils.readVarInt(buf, 4),
				ByteBufUtils.readVarInt(buf, 4));
	}

	public static void writeBlockPos(ByteBuf buf, BlockPos pos) {
		ByteBufUtils.writeVarInt(buf, pos.getX(), 4);
		ByteBufUtils.writeVarInt(buf, pos.getY(), 4);
		ByteBufUtils.writeVarInt(buf, pos.getZ(), 4);
	}

}
