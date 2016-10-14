package net.earthcomputer.redbuilder.network;

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
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy {

	public void preinit(FMLPreInitializationEvent e) {
		SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(RedBuilder.MODID);
		int clientPacketId = 0;
		int serverPacketId = 0;
		network.registerMessage(CPacketRequestTileEntityData.Handler.class, CPacketRequestTileEntityData.class,
				clientPacketId++, Side.SERVER);
		network.registerMessage(SPacketTileEntityData.Handler.class, SPacketTileEntityData.class, serverPacketId++,
				Side.CLIENT);
		network.registerMessage(SPacketRedBuilderServer.Handler.class, SPacketRedBuilderServer.class, serverPacketId++,
				Side.CLIENT);
		RedBuilder.instance().setNetwork(network);

		MinecraftForge.EVENT_BUS.register(Handlers.INSTANCE);

		MinecraftForge.EVENT_BUS.register(RedBuilderSettings.INSTANCE);

		Configuration config = new Configuration(e.getSuggestedConfigurationFile());
		config.load();
		RedBuilderSettings.readFromConfig(config);
		RedBuilder.instance().setConfig(config);

		for (EnumRedBuilderFeatures feature : EnumRedBuilderFeatures.values()) {
			if (feature.shouldRunInEnvironment()) {
				feature.getFeatureInstance().initialize();
			}
		}
	}
	
	public void wrenchClientInitialize() {
	}

}
