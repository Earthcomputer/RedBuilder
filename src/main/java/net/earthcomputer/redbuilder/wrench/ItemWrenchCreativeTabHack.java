package net.earthcomputer.redbuilder.wrench;

import java.util.List;

import net.earthcomputer.redbuilder.RedBuilder;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemWrenchCreativeTabHack extends Item {

	public static final ItemWrenchCreativeTabHack INSTANCE = new ItemWrenchCreativeTabHack();

	private ItemWrenchCreativeTabHack() {
		setRegistryName(RedBuilder.MODID, "wrench_creative_tab_hack");
		setCreativeTab(CreativeTabs.REDSTONE);
	}

	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		ItemStack wrench = new ItemStack(Items.STICK);
		WrenchEventListener.setTurnDir(wrench, EnumTurnDirection.Y_AXIS);
		subItems.add(wrench);
	}

}
