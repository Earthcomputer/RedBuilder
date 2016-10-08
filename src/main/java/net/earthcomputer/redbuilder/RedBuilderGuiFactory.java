package net.earthcomputer.redbuilder;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.DummyConfigElement.DummyCategoryElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.CategoryEntry;
import net.minecraftforge.fml.client.config.IConfigElement;

public class RedBuilderGuiFactory implements IModGuiFactory {

	@Override
	public void initialize(Minecraft minecraftInstance) {
		// nop
	}

	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return MainGui.class;
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}

	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
		return null;
	}

	public static class MainGui extends GuiConfig {
		public MainGui(GuiScreen prevScreen) {
			super(prevScreen, getConfigElements(), RedBuilder.MODID, false, false,
					I18n.format("redbuilder.configgui.title"));
		}

		private static List<IConfigElement> getConfigElements() {
			List<IConfigElement> elements = Lists.newArrayList();
			elements.add(new DummyCategoryElement("features", "redbuilder.configgui.ctgy.features",
					FeaturesCategoryEntry.class));
			return elements;
		}

		public static class FeaturesCategoryEntry extends SimpleCategoryEntry {

			public FeaturesCategoryEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList,
					IConfigElement configElement) {
				super(owningScreen, owningEntryList, configElement);
			}

			@Override
			protected GuiScreen buildChildScreen() {
				return buildChildScreen("features");
			}

		}

		public static abstract class SimpleCategoryEntry extends CategoryEntry {

			public SimpleCategoryEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList,
					IConfigElement configElement) {
				super(owningScreen, owningEntryList, configElement);
			}

			protected GuiScreen buildChildScreen(String ctgyName) {
				return new GuiConfig(owningScreen,
						new ConfigElement(RedBuilder.instance().getConfig().getCategory(ctgyName)).getChildElements(),
						owningScreen.modID, false, false,
						GuiConfig.getAbridgedConfigPath(RedBuilder.instance().getConfig().toString()));
			}
		}
	}

}
