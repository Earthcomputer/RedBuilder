package net.earthcomputer.redbuilder;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.earthcomputer.redbuilder.network.CommonProxy;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = RedBuilder.MODID, name = RedBuilder.NAME, version = RedBuilder.VERSION, guiFactory = "net.earthcomputer.redbuilder.RedBuilderGuiFactory")
public class RedBuilder {
	public static final String MODID = "redbuilder";
	public static final String NAME = "Red Builder";
	public static final String VERSION = "${version}";

	public static final Logger LOGGER = LogManager.getLogger(NAME);

	@Instance(RedBuilder.MODID)
	private static RedBuilder INSTANCE;

	private SimpleNetworkWrapper network;
	private Configuration config;
	private RedBuilderCreativeTab creativeTab;

	@SidedProxy(clientSide = "net.earthcomputer.redbuilder.network.ClientProxy", serverSide = "net.earthcomputer.redbuilder.network.CommonProxy")
	private static CommonProxy proxy;

	public static RedBuilder instance() {
		return INSTANCE;
	}

	public SimpleNetworkWrapper getNetwork() {
		return network;
	}

	public void setNetwork(SimpleNetworkWrapper network) {
		this.network = network;
	}

	public Configuration getConfig() {
		return config;
	}

	public void setConfig(Configuration config) {
		this.config = config;
	}

	public RedBuilderCreativeTab getCreativeTab() {
		return creativeTab;
	}

	public void setCreativeTab(RedBuilderCreativeTab creativeTab) {
		this.creativeTab = creativeTab;
	}

	@NetworkCheckHandler
	public boolean acceptsRemote(Map<String, String> remoteMods, Side remoteSide) {
		return true;
	}

	@EventHandler
	public void preinit(FMLPreInitializationEvent e) {
		proxy.preinit(e);
	}
}
