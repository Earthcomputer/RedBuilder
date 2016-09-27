package net.earthcomputer.redbuilder;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RedBuilderSettings {

	public static final RedBuilderSettings INSTANCE = new RedBuilderSettings();

	public static boolean enableAdvancedMiddleClick;
	public static boolean enableRedstonePowerInfo;

	private RedBuilderSettings() {
	}

	public static void readFromConfig(Configuration config) {
		enableAdvancedMiddleClick = getProp(config, "features", "advancedMiddleClick", "true",
				"Whether any block can be Ctrl + pick-blocked (not just tile entities)", Property.Type.BOOLEAN)
						.getBoolean();
		enableRedstonePowerInfo = getProp(config, "features", "redstonePowerInfo", "false",
				"Whether to display the inputs and outputs of a redstone component when they are hovered over",
				Property.Type.BOOLEAN).getBoolean();

		if (config.hasChanged()) {
			config.save();
		}
	}

	private static Property getProp(Configuration config, String ctgy, String key, String dflt, String desc,
			Property.Type type) {
		Property prop = config.get(ctgy, key, dflt, desc, type);
		prop.setLanguageKey("redbuilder.configgui." + ctgy + "." + key);
		return prop;
	}

	@SubscribeEvent
	public void configChanged(ConfigChangedEvent.PostConfigChangedEvent e) {
		if (e.getModID().equals(RedBuilder.MODID)) {
			readFromConfig(RedBuilder.getInstance().getConfig());
		}
	}

}
