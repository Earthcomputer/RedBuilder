package net.earthcomputer.redbuilder;

import java.lang.reflect.Method;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RedBuilderCreativeTab extends CreativeTabs {

	private static final ItemStack ICON_ITEM = new ItemStack(Items.REDSTONE);

	static {
		ICON_ITEM.setTagInfo("ench", new NBTTagList());
	}

	private final List<ItemStack> items = Lists.newArrayList();

	public RedBuilderCreativeTab() {
		super(RedBuilder.MODID);
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void addItem(ItemStack item) {
		items.add(item);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Item getTabIconItem() {
		return Items.REDSTONE;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public ItemStack getIconItemStack() {
		return ICON_ITEM;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void displayAllRelevantItems(List<ItemStack> items) {
		items.addAll(this.items);
	}

	@SideOnly(Side.CLIENT)
	private boolean shouldUpdateCreativeSearch;

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPreGuiKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre e) {
		GuiScreen gui = e.getGui();
		if (gui instanceof GuiContainerCreative) {
			GuiContainerCreative creativeGui = (GuiContainerCreative) gui;

			shouldUpdateCreativeSearch = false;

			char typedChar = Keyboard.getEventCharacter();
			int keyCode = Keyboard.getEventKey();
			if ((keyCode == Keyboard.KEY_NONE && typedChar >= ' ') || Keyboard.getEventKeyState()) {
				if (getSelectedTab(creativeGui).hasSearchBar()) {
					boolean isSwappingHotbar = false;
					Minecraft mc = Minecraft.getMinecraft();
					if (mc.thePlayer.inventory.getItemStack() == null && creativeGui.getSlotUnderMouse() != null) {
						for (int i = 0; i < 9; i++) {
							if (mc.gameSettings.keyBindsHotbar[i].isActiveAndMatches(keyCode)) {
								isSwappingHotbar = true;
								break;
							}
						}
					}
					if (!isSwappingHotbar) {
						if (wouldTextFieldAccept(getSearchBar(creativeGui), typedChar, keyCode)) {
							shouldUpdateCreativeSearch = true;
						}
					}
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPostGuiKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Post e) {
		if (shouldUpdateCreativeSearch) {
			GuiContainerCreative gui = (GuiContainerCreative) e.getGui();
			if (getSelectedTab(gui) == CreativeTabs.SEARCH) {
				getItemList(gui).addAll(items);
				try {
					Method method = GuiContainerCreative.class.getDeclaredMethod("updateFilteredItems",
							Class.forName("net.minecraft.client.gui.inventory.GuiContainerCreative$ContainerCreative"));
					method.setAccessible(true);
					method.invoke(gui, gui.inventorySlots);
				} catch (Exception e1) {
					throw Throwables.propagate(e1);
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	private static CreativeTabs getSelectedTab(GuiContainerCreative gui) {
		return CreativeTabs.CREATIVE_TAB_ARRAY[gui.getSelectedTabIndex()];
	}

	@SideOnly(Side.CLIENT)
	private static GuiTextField getSearchBar(GuiContainerCreative gui) {
		return ReflectionHelper.getPrivateValue(GuiContainerCreative.class, gui, "searchField", "field_147062_A");
	}

	@SideOnly(Side.CLIENT)
	private static boolean wouldTextFieldAccept(GuiTextField textField, char typedChar, int keyCode) {
		if (!textField.isFocused()) {
			return false;
		} else if (GuiScreen.isKeyComboCtrlA(keyCode)) {
			return true;
		} else if (GuiScreen.isKeyComboCtrlC(keyCode)) {
			return true;
		} else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
			return true;
		} else if (GuiScreen.isKeyComboCtrlX(keyCode)) {
			return true;
		} else {
			switch (keyCode) {
			case Keyboard.KEY_BACK:
			case Keyboard.KEY_HOME:
			case Keyboard.KEY_LEFT:
			case Keyboard.KEY_RIGHT:
			case Keyboard.KEY_END:
			case Keyboard.KEY_DELETE:
				return true;
			default:
				return ChatAllowedCharacters.isAllowedCharacter(typedChar);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	private static List<ItemStack> getItemList(GuiContainerCreative gui) {
		return ReflectionHelper.getPrivateValue(
				ReflectionHelper.getClass(GuiContainerCreative.class.getClassLoader(),
						"net.minecraft.client.gui.inventory.GuiContainerCreative$ContainerCreative"),
				gui.inventorySlots, "itemList", "field_148330_a");
	}

}
