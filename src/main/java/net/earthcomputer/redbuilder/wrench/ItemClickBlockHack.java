package net.earthcomputer.redbuilder.wrench;

import net.earthcomputer.redbuilder.RedBuilder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * This is used to bypass sending the click to the server
 */
public class ItemClickBlockHack extends Item {

	public static final ItemClickBlockHack INSTANCE = new ItemClickBlockHack();

	private Item wrenchItem;

	private ItemClickBlockHack() {
		setRegistryName(RedBuilder.MODID, "wrench_click_block_hack");
	}

	@SuppressWarnings("deprecation")
	public static void setupItemStackForHack(ItemStack stack) {
		INSTANCE.wrenchItem = stack.getItem();
		stack.setItem(INSTANCE);
	}

	@SuppressWarnings("deprecation")
	@Override
	public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos,
			EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		stack.setItem(wrenchItem);
		wrenchItem = null;
		return EnumActionResult.SUCCESS;
	}

}
