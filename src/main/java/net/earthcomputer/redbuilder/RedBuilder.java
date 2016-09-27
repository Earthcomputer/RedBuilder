package net.earthcomputer.redbuilder;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Throwables;

import net.earthcomputer.redbuilder.logic.RedstoneLogicDisplayListener;
import net.earthcomputer.redbuilder.midclick.BetterMiddleClickListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModContainer;
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

	public static RedBuilder getInstance() {
		return instance;
	}

	public Configuration getConfig() {
		return config;
	}

	@NetworkCheckHandler
	public boolean acceptsRemote(Map<String, String> remoteMods, Side remoteSide) {
		Field descriptorField = ReflectionHelper.findField(FMLModContainer.class, "descriptor");
		// These mods are allowed to be on the server side even though they are
		// not client-side-only
		List<String> allowedRemoteMods = Arrays.asList("mcp", "fml", "forge");

		for (String remoteMod : remoteMods.keySet()) {
			if (Loader.isModLoaded(remoteMod) && !allowedRemoteMods.contains(remoteMod)) {
				// We have this mod on both the client and server, check if it's
				// client-side-only
				ModContainer mod = Loader.instance().getIndexedModList().get(remoteMod);
				if (mod instanceof FMLModContainer) {
					Map<?, ?> descriptor;
					try {
						descriptor = (Map<?, ?>) descriptorField.get(mod);
					} catch (Exception e) {
						throw Throwables.propagate(e);
					}
					Boolean isClientOnly = (Boolean) descriptor.get("clientSideOnly");
					if (isClientOnly != Boolean.TRUE) {
						// Not client-side-only we have a mod that modifies both
						// the client and the server and hence could mess up Red
						// Builder
						return false;
					}
				} else {
					// Not made with a @Mod annotation, assume it's okay
				}
			}
		}
		return true;
	}

	@EventHandler
	public void preinit(FMLPreInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(new BetterMiddleClickListener());
		MinecraftForge.EVENT_BUS.register(ChatBlocker.INSTANCE);
		MinecraftForge.EVENT_BUS.register(new RedstoneLogicDisplayListener());
		MinecraftForge.EVENT_BUS.register(RedBuilderSettings.INSTANCE);

		config = new Configuration(e.getSuggestedConfigurationFile());
		config.load();
		RedBuilderSettings.readFromConfig(config);
	}
}
