package net.earthcomputer.redbuilder.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.util.math.RayTraceResult;

public class ClientInstructionWrapper {

	private static final Minecraft MC = Minecraft.getMinecraft();

	private ClientInstructionWrapper() {
	}

	public static EntityPlayer getPlayer() {
		return MC.thePlayer;
	}

	public static void runLater(Runnable task) {
		MC.addScheduledTask(task);
	}

	public static RayTraceResult getObjMouseOver() {
		return MC.objectMouseOver;
	}

	public static void sendVanillaPacketToServer(Packet<?> packet) {
		MC.thePlayer.connection.sendPacket(packet);
	}

}
