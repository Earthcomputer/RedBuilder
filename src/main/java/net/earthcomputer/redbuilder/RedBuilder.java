package net.earthcomputer.redbuilder;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.earthcomputer.redbuilder.antiwater.AntiWaterEventListener;
import net.earthcomputer.redbuilder.logic.RedstoneLogicDisplayListener;
import net.earthcomputer.redbuilder.midclick.BetterMiddleClickListener;
import net.earthcomputer.redbuilder.wrench.WrenchEventListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = RedBuilder.MODID, name = RedBuilder.NAME, version = RedBuilder.VERSION, clientSideOnly = true, guiFactory = "net.earthcomputer.redbuilder.RedBuilderGuiFactory")
public class RedBuilder {
	public static final String MODID = "redbuilder";
	public static final String NAME = "Red Builder";
	public static final String VERSION = "${version}";

	public static final Logger LOGGER = LogManager.getLogger(NAME);

	@Instance(RedBuilder.MODID)
	private static RedBuilder instance;

	private Configuration config;
	public boolean forgeServer;

	public static RedBuilder getInstance() {
		return instance;
	}

	public Configuration getConfig() {
		return config;
	}

	@NetworkCheckHandler
	public boolean acceptsRemote(Map<String, String> remoteMods, Side remoteSide) {
		forgeServer = false;
		for (String remoteMod : remoteMods.keySet()) {
			if (remoteMod.equals("forge")) {
				forgeServer = true;
			}
		}
		return true;
	}

	@EventHandler
	public void preinit(FMLPreInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(new BetterMiddleClickListener());
		MinecraftForge.EVENT_BUS.register(new RedstoneLogicDisplayListener());
		MinecraftForge.EVENT_BUS.register(new AntiWaterEventListener());
		
		MinecraftForge.EVENT_BUS.register(ChatBlocker.INSTANCE);
		MinecraftForge.EVENT_BUS.register(RedBuilderSettings.INSTANCE);

		config = new Configuration(e.getSuggestedConfigurationFile());
		config.load();
		RedBuilderSettings.readFromConfig(config);
	}
}
