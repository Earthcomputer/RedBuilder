package net.earthcomputer.redbuilder.wrench;

import net.earthcomputer.redbuilder.ChatBlocker;
import net.earthcomputer.redbuilder.RedBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class WrenchEventListener {

	public static void registerHackItems() {
		GameRegistry.register(ItemClickBlockHack.INSTANCE);
		GameRegistry.register(ItemWrenchCreativeTabHack.INSTANCE);

		ModelResourceLocation hackLocation = new ModelResourceLocation(RedBuilder.MODID + ":hack_item", "inventory");
		ModelLoader.setCustomModelResourceLocation(ItemClickBlockHack.INSTANCE, 0, hackLocation);
		ModelLoader.setCustomModelResourceLocation(ItemWrenchCreativeTabHack.INSTANCE, 0, hackLocation);
	}

	@SubscribeEvent
	public void onPlayerRightClickedEntity(EntityInteract e) {
		if (getWrenchTurnDir(e) != null) {
			e.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onEntityRightClicked(EntityInteractSpecific e) {
		if (getWrenchTurnDir(e) != null) {
			e.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onRightClickAir(RightClickItem e) {
		EnumTurnDirection turnDir = getWrenchTurnDir(e);
		if (turnDir == null) {
			return;
		}

		ItemStack stack = e.getItemStack();
		setTurnDir(stack, turnDir.nextTurnDir());
	}

	@SubscribeEvent
	public void onLeftClicked(LeftClickBlock e) {
		if (onWrenchUsed(e, true)) {
			e.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onRightClicked(RightClickBlock e) {
		if (onWrenchUsed(e, false)) {
			ItemClickBlockHack.setupItemStackForHack(e.getItemStack());
		}
	}

	private boolean onWrenchUsed(PlayerInteractEvent e, boolean reverse) {
		EnumTurnDirection turnDir = getWrenchTurnDir(e);
		if (turnDir == null) {
			return false;
		}

		EntityPlayer player = e.getEntityPlayer();

		EnumFacing horizontalPlayerFacing = player.getHorizontalFacing();
		World world = e.getWorld();
		if (!world.isRemote) {
			return false;
		}
		BlockPos pos = e.getPos();
		IBlockState state = world.getBlockState(pos);
		IBlockState turnedState = WrenchTurnRegistry.turn(state, turnDir.getAxis(horizontalPlayerFacing),
				turnDir.getAxisDirection(horizontalPlayerFacing, reverse));
		// With reverse the server thinks the player has destroyed the block, we
		// need to tell the server the block hasn't changed
		if (turnedState.equals(state) && !reverse) {
			return true;
		}

		if (world.getTileEntity(pos) != null) {
			TextComponentTranslation message = new TextComponentTranslation("redbuilder.wrench.tileentity");
			message.getStyle().setColor(TextFormatting.RED);
			Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(message);
			return false;
		}

		ChatBlocker.setBlock(pos, turnedState);

		return true;
	}

	private EnumTurnDirection getWrenchTurnDir(PlayerInteractEvent e) {
		if (e.getSide() == Side.SERVER) {
			return null;
		}

		EntityPlayer player = e.getEntityPlayer();
		if (!player.isCreative()) {
			return null;
		}

		ItemStack stack = e.getItemStack();
		if (stack == null) {
			return null;
		}

		NBTTagCompound stackTag = stack.getTagCompound();
		if (stackTag == null || !stackTag.hasKey("WrenchTurnDirection")) {
			return null;
		}

		EnumTurnDirection turnDir = EnumTurnDirection.getByName(stackTag.getString("WrenchTurnDirection"));
		return turnDir;
	}

	static void setTurnDir(ItemStack stack, EnumTurnDirection turnDir) {
		stack.setTagInfo("WrenchTurnDirection", new NBTTagString(turnDir.getName()));
		NBTTagCompound display = new NBTTagCompound();
		display.setString("Name", TextFormatting.RESET.toString() + TextFormatting.AQUA
				+ I18n.format("redbuilder.wrench." + turnDir.getName()));
		stack.setTagInfo("display", display);
		stack.setTagInfo("ench", new NBTTagList());
	}

}
