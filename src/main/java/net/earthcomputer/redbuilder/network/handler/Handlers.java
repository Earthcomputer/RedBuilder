package net.earthcomputer.redbuilder.network.handler;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.earthcomputer.redbuilder.RedBuilder;
import net.earthcomputer.redbuilder.RedBuilderSettings;
import net.earthcomputer.redbuilder.network.packet.SPacketRedBuilderServer;
import net.earthcomputer.redbuilder.util.ReflectionNames;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerDisconnectionFromClientEvent;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Handlers {

	private static final Handlers INSTANCE = new Handlers();

	private Handlers() {
	}

	public static Handlers instance() {
		return INSTANCE;
	}

	// Common fields
	private static Side bestExecutionSide = Side.SERVER;
	// Client fields
	private static IUniformInstructionHandler clientSideHandler;
	@SideOnly(Side.CLIENT)
	private static boolean forgeServer;
	// Server fields
	private static final Map<EntityPlayerMP, IUniformInstructionHandler> serverSideHandlers = Maps.newHashMap();
	private static final List<EntityPlayerMP> clientsNeedingUpdate = Collections
			.synchronizedList(Lists.<EntityPlayerMP> newArrayList());

	// PUBLIC METHODS
	public static boolean isBestExecutionSide(Side side) {
		return side == bestExecutionSide;
	}

	public static boolean isBestExecutionSide(World world) {
		return isBestExecutionSide(world.isRemote ? Side.CLIENT : Side.SERVER);
	}

	public static IUniformInstructionHandler getClientSideInstructionHandler() {
		return clientSideHandler;
	}

	public static IUniformInstructionHandler getInstructionHandler(Side side, EntityPlayer player) {
		if (side == Side.SERVER) {
			return serverSideHandlers.get(player);
		} else {
			return clientSideHandler;
		}
	}

	@SideOnly(Side.CLIENT)
	public static boolean isForgeServer() {
		return forgeServer;
	}

	// EVENT HANDLERS
	// Client
	@SideOnly(Side.CLIENT)
	public static void onReceiveServerRedBuilderMessage(String serverRedBuilderVersion) {
		bestExecutionSide = Side.SERVER;
		clientSideHandler = new ClientSideRedBuilderHandler();
		RedBuilder.LOGGER.info("Received RedBuilder hello message, using client-side RedBuilder handler");
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void clientSideConnection(ClientConnectedToServerEvent e) {
		forgeServer = "MODDED".equals(e.getConnectionType());

		if (RedBuilderSettings.useCommandsForNonRedBuilderServers) {
			bestExecutionSide = Side.CLIENT;
			clientSideHandler = new ClientSideCommandHandler();
			RedBuilder.LOGGER.info("Connected to server, using client-side command handler");
		} else {
			bestExecutionSide = Side.SERVER;
			clientSideHandler = new ClientSideNonRedBuilderHandler();
			RedBuilder.LOGGER.info("Connected to server, using client-side non-RedBuilder handler");
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void clientSideDisconnection(ClientDisconnectionFromServerEvent e) {
		bestExecutionSide = Side.SERVER;
		clientSideHandler = null;
	}

	// Server
	@SubscribeEvent
	public void serverSideConnection(ServerConnectionFromClientEvent e) {
		NetworkDispatcher dispatcher = NetworkDispatcher.get(e.getManager());
		EntityPlayerMP player = ((NetHandlerPlayServer) e.getHandler()).playerEntity;

		Map<String, String> modList;
		if ("MODDED".equals(((Enum<?>) ReflectionHelper.getPrivateValue(NetworkDispatcher.class, dispatcher,
				ReflectionNames.NetworkDispatcher_connectionType)).name())) {
			modList = dispatcher.getModList();
		} else {
			modList = ImmutableMap.of();
		}

		if (modList.containsKey(RedBuilder.MODID)) {
			serverSideHandlers.put(player, new ServerSideRedBuilderHandler(player));
			RedBuilder.LOGGER.info("Connection from RedBuilder client, using server-side RedBuilder handler");
			clientsNeedingUpdate.add(player);
		} else {
			serverSideHandlers.put(player, new ServerSideNonRedBuilderHandler(player));
			RedBuilder.LOGGER.info("Connection from non-RedBuilder client, using server-side non-RedBuilder handler");
		}
	}

	@SubscribeEvent
	public void serverSideDisconnection(ServerDisconnectionFromClientEvent e) {
		EntityPlayerMP player = ((NetHandlerPlayServer) e.getHandler()).playerEntity;
		serverSideHandlers.remove(player);
	}

	@SubscribeEvent
	public void serverTick(ServerTickEvent e) {
		if (e.phase != Phase.START) {
			return;
		}

		synchronized (clientsNeedingUpdate) {
			for (EntityPlayerMP player : clientsNeedingUpdate) {
				RedBuilder.instance().getNetwork().sendTo(new SPacketRedBuilderServer(RedBuilder.VERSION), player);
			}
			clientsNeedingUpdate.clear();
		}
	}

}
