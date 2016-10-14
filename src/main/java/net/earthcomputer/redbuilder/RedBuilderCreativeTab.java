package net.earthcomputer.redbuilder;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
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

}
