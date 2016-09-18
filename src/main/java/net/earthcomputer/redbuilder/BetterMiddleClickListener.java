package net.earthcomputer.redbuilder;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

public class BetterMiddleClickListener {

	@SubscribeEvent
	public void onMiddleClicked(ClientTickEvent e) {
		if (e.phase != Phase.END) {
			return;
		}
		Minecraft mc = Minecraft.getMinecraft();
		World world = mc.theWorld;
		AbstractClientPlayer player = mc.thePlayer;

		if (world == null || player == null) {
			return;
		}

		PlayerControllerMP playerController = mc.playerController;

		if (playerController.isNotCreative()) {
			return;
		}

		if (!mc.gameSettings.keyBindPickBlock.isKeyDown()) {
			return;
		}

		if (!GuiScreen.isCtrlKeyDown()) {
			return;
		}

		RayTraceResult rayTraceResult = mc.objectMouseOver;
		if (rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) {
			return;
		}
		IBlockState state = world.getBlockState(rayTraceResult.getBlockPos());

		ItemStack pickBlock = player.getHeldItemMainhand();
		NBTTagCompound stackTag = pickBlock.getTagCompound();

		if (stackTag != null && stackTag.hasKey("BlockEntityTag")) {
			return;
		}

		pickBlock.setTagInfo("PickedBlock",
				new NBTTagString(Block.REGISTRY.getNameForObject(state.getBlock()).toString()));
		pickBlock.setTagInfo("StateData", new NBTTagByte((byte) state.getBlock().getMetaFromState(state)));

		NBTTagCompound display = new NBTTagCompound();
		NBTTagList lore = new NBTTagList();
		lore.appendTag(new NBTTagString("(+BLOCKSTATE)"));
		display.setTag("Lore", lore);
		if (!pickBlock.getDisplayName().endsWith(" (Custom)")) {
			display.setTag("Name", new NBTTagString(TextFormatting.RESET + pickBlock.getDisplayName() + " (Custom)"));
		}
		pickBlock.setTagInfo("display", display);

		playerController.sendSlotPacket(pickBlock, 36 + player.inventory.currentItem);
	}

	@SubscribeEvent
	public void onBlockPlaced(PlayerInteractEvent.RightClickBlock e) {
		System.out.println("Player interact event");
		if (e.getSide() != Side.CLIENT) {
			return;
		}

		ItemStack stack = e.getItemStack();
		if (stack == null) {
			return;
		}

		NBTTagCompound stackTag = stack.getTagCompound();
		if (stackTag == null || !stackTag.hasKey("PickedBlock", Constants.NBT.TAG_STRING)
				|| !stackTag.hasKey("StateData", Constants.NBT.TAG_INT)) {
			return;
		}

		String blockName = stackTag.getString("PickedBlock");
		if (!Block.REGISTRY.containsKey(new ResourceLocation(blockName))) {
			return;
		}
		int metadata = stackTag.getInteger("StateData");
		BlockPos pos = e.getPos().offset(e.getFace());
		
		Minecraft.getMinecraft().thePlayer.sendChatMessage(
				String.format("/setblock %d %d %d %s %d", pos.getX(), pos.getY(), pos.getZ(), blockName, metadata));
	}

}
