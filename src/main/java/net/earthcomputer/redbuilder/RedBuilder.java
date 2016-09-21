package net.earthcomputer.redbuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = RedBuilder.MODID, name = RedBuilder.NAME, version = RedBuilder.VERSION, clientSideOnly = true)
public class RedBuilder {
	public static final String MODID = "redbuilder";
	public static final String NAME = "Red Builder";
	public static final String VERSION = "${version}";

	public static final Logger LOGGER = LogManager.getLogger(NAME);

	@NetworkCheckHandler
	public boolean acceptsRemote(Map<String, String> remoteMods, Side remoteSide) {
		for (String remoteMod : remoteMods.keySet()) {
			if (Loader.isModLoaded(remoteMod) && !remoteMod.equals(MODID)) {
				return false;
			}
		}
		return true;
	}

	@EventHandler
	public void preinit(FMLPreInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(new BetterMiddleClickListener());
		MinecraftForge.EVENT_BUS.register(ChatBlocker.INSTANCE);
	}
}
