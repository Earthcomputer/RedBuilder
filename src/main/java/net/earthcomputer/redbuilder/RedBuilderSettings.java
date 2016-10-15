package net.earthcomputer.redbuilder;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RedBuilderSettings {

	public static final RedBuilderSettings INSTANCE = new RedBuilderSettings();

	public static boolean useCommandsForNonRedBuilderServers;

	public static boolean enableAdvancedMiddleClick;
	public static boolean enableRedstonePowerInfo;
	public static EnumAntiWaterSetting antiWaterSetting;

	private RedBuilderSettings() {
	}

	public static void readFromConfig(Configuration config) {
		useCommandsForNonRedBuilderServers = getProp(config, "general", "useCommandsForNonRedBuilderServers", "false",
				Property.Type.BOOLEAN).getBoolean();

		enableAdvancedMiddleClick = getProp(config, "features", "advancedMiddleClick", "true", Property.Type.BOOLEAN)
				.getBoolean();
		enableRedstonePowerInfo = getProp(config, "features", "redstonePowerInfo", "false", Property.Type.BOOLEAN)
				.getBoolean();
		Property prop = getProp(config, "features", "antiWater", EnumAntiWaterSetting.LAVA_ONLY.getName(),
				Property.Type.STRING, EnumAntiWaterSetting.getNames());
		prop.setConfigEntryClass(TranslatedCycleValueEntry.class);
		antiWaterSetting = EnumAntiWaterSetting.getByName(prop.getString());

		if (config.hasChanged()) {
			config.save();
		}
	}

	private static Property getProp(Configuration config, String ctgy, String key, String dflt, Property.Type type) {
		return getProp(config, ctgy, key, dflt, type, (String[]) null);
	}

	private static Property getProp(Configuration config, String ctgy, String key, String dflt, Property.Type type,
			String... validValues) {
		Property prop = config.get(ctgy, key, dflt,
				I18n.format("redbuilder.configgui." + ctgy + "." + key + ".tooltip"), type);
		prop.setLanguageKey("redbuilder.configgui." + ctgy + "." + key);
		if (validValues != null) {
			prop.setValidValues(validValues);
		}
		return prop;
	}

	@SubscribeEvent
	public void configChanged(ConfigChangedEvent.PostConfigChangedEvent e) {
		if (e.getModID().equals(RedBuilder.MODID)) {
			readFromConfig(RedBuilder.instance().getConfig());
		}
	}

	public static enum EnumAntiWaterSetting {
		NONE("none"), WATER_ONLY("water_only", Blocks.WATER, Blocks.FLOWING_WATER), LAVA_ONLY("lava_only", Blocks.LAVA,
				Blocks.FLOWING_LAVA), BOTH("both", Blocks.WATER, Blocks.FLOWING_WATER, Blocks.LAVA,
						Blocks.FLOWING_LAVA);

		private String name;
		private Set<Block> liquidBlocks;

		private EnumAntiWaterSetting(String name, Block... liquidBlocks) {
			this.name = name;
			this.liquidBlocks = ImmutableSet.copyOf(liquidBlocks);
		}

		public String getName() {
			return name;
		}

		public boolean isMatchingLiquid(Block liquid) {
			return liquidBlocks.contains(liquid);
		}

		public static String[] getNames() {
			return NAMES;
		}

		public static EnumAntiWaterSetting getByName(String name) {
			return BY_NAME.get(name);
		}

		private static final String[] NAMES = new String[values().length];
		private static final Map<String, EnumAntiWaterSetting> BY_NAME = Maps.newHashMap();

		static {
			EnumAntiWaterSetting[] values = values();
			for (int i = 0; i < values.length; i++) {
				EnumAntiWaterSetting setting = values[i];
				NAMES[i] = setting.getName();
				BY_NAME.put(setting.getName(), setting);
			}
		}
	}

}
