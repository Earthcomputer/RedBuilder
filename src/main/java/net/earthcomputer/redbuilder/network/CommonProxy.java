package net.earthcomputer.redbuilder.network;

import java.io.File;

import net.earthcomputer.redbuilder.EnumRedBuilderFeatures;
import net.earthcomputer.redbuilder.RedBuilder;
import net.earthcomputer.redbuilder.RedBuilderSettings;
import net.earthcomputer.redbuilder.network.handler.Handlers;
import net.earthcomputer.redbuilder.network.packet.CPacketRequestTileEntityData;
import net.earthcomputer.redbuilder.network.packet.SPacketRedBuilderServer;
import net.earthcomputer.redbuilder.network.packet.SPacketTileEntityData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy {

	public void preinit(FMLPreInitializationEvent e) {
		registerNetwork();

		MinecraftForge.EVENT_BUS.register(Handlers.instance());

		MinecraftForge.EVENT_BUS.register(RedBuilderSettings.instance());

		loadConfig(e.getSuggestedConfigurationFile());

		loadFeatures();
	}

	// NETWORK
	private int clientPacketId = 0;
	private int serverPacketId = 0;

	private void registerNetwork() {
		SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(RedBuilder.MODID);

		registerClientPacket(network, CPacketRequestTileEntityData.class);

		registerServerPacket(network, SPacketTileEntityData.class);
		registerServerPacket(network, SPacketRedBuilderServer.class);

		RedBuilder.instance().setNetwork(network);
	}

	private <P extends IMessage> void registerClientPacket(SimpleNetworkWrapper network, Class<P> packetClass) {
		registerClientPacket(network, packetClass, getHandlerClass(packetClass));
	}

	private <P extends IMessage> void registerClientPacket(SimpleNetworkWrapper network, Class<P> packetClass,
			Class<? extends IMessageHandler<P, IMessage>> handler) {
		network.registerMessage(handler, packetClass, clientPacketId++, Side.SERVER);
	}

	private <P extends IMessage> void registerServerPacket(SimpleNetworkWrapper network, Class<P> packetClass) {
		registerServerPacket(network, packetClass, getHandlerClass(packetClass));
	}

	private <P extends IMessage> void registerServerPacket(SimpleNetworkWrapper network, Class<P> packetClass,
			Class<? extends IMessageHandler<P, IMessage>> handler) {
		network.registerMessage(handler, packetClass, serverPacketId++, Side.CLIENT);
	}

	@SuppressWarnings("unchecked")
	private <P extends IMessage, H extends IMessageHandler<P, IMessage>> Class<H> getHandlerClass(
			Class<P> packetClass) {
		Class<H> handlerClass = null;
		for (Class<?> innerClass : packetClass.getClasses()) {
			if (IMessageHandler.class.isAssignableFrom(innerClass) && innerClass.getSimpleName().equals("Handler")) {
				handlerClass = (Class<H>) innerClass;
				break;
			}
		}
		if (handlerClass == null) {
			throw new IllegalArgumentException("Could not find handler class in " + packetClass.getName());
		}
		return handlerClass;
	}

	// CONFIG
	private void loadConfig(File configFile) {
		Configuration config = new Configuration(configFile);
		config.load();
		RedBuilderSettings.readFromConfig(config);
		RedBuilder.instance().setConfig(config);
	}

	// FEATURES
	private void loadFeatures() {
		for (EnumRedBuilderFeatures feature : EnumRedBuilderFeatures.values()) {
			if (feature.shouldRunInEnvironment()) {
				feature.getFeatureInstance().initialize();
			}
		}
	}

}
