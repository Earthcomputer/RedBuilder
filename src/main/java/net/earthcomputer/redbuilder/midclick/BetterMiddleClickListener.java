package net.earthcomputer.redbuilder.midclick;

import java.util.Map;

import com.google.common.collect.Maps;

import net.earthcomputer.redbuilder.IRedBuilderFeature;
import net.earthcomputer.redbuilder.RedBuilderSettings;
import net.earthcomputer.redbuilder.network.handler.Handlers;
import net.earthcomputer.redbuilder.util.ClientChatUtils;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BetterMiddleClickListener implements IRedBuilderFeature {

	@Override
	public void initialize() {
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
			MinecraftForge.EVENT_BUS.register(new ClientEvtListener());
		}
		MinecraftForge.EVENT_BUS.register(new ServerEvtListener());
	}

	private static class ClientEvtListener {
		private static final int TIME_TO_WAIT = 10;

		private Map<BlockPos, IBlockState> pendingBlockPlacements = Maps.newHashMap();
		private Map<BlockPos, Integer> timeLeftAtPos = Maps.newHashMap();

		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public void onMiddleClicked(ClientTickEvent e) {
			if (e.phase != Phase.END) {
				return;
			}

			if (!RedBuilderSettings.enableAdvancedMiddleClick) {
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

			pickBlock.setTagInfo("PickedBlock", new NBTTagString(state.getBlock().delegate.name().toString()));
			pickBlock.setTagInfo("StateData", new NBTTagByte((byte) state.getBlock().getMetaFromState(state)));

			NBTTagCompound display = new NBTTagCompound();
			NBTTagList lore = new NBTTagList();
			lore.appendTag(new NBTTagString("(+BLOCKSTATE)"));
			display.setTag("Lore", lore);
			if (!pickBlock.getDisplayName().endsWith(" (Custom)")) {
				display.setTag("Name",
						new NBTTagString(TextFormatting.RESET + pickBlock.getDisplayName() + " (Custom)"));
			}
			pickBlock.setTagInfo("display", display);

			playerController.sendSlotPacket(pickBlock, 36 + player.inventory.currentItem);
		}

		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public void onBlockRightClicked(PlayerInteractEvent.RightClickBlock e) {
			if (!Handlers.isBestExecutionSide(Side.CLIENT)) {
				return;
			}

			if (e.getSide() != Side.CLIENT) {
				return;
			}

			ItemStack stack = e.getItemStack();
			IBlockState state = getPickedBlock(stack);
			if (state == null) {
				return;
			}
			BlockPos pos = e.getPos();
			if (!state.getBlock().canReplace(e.getWorld(), pos, e.getFace(), stack)) {
				pos = pos.offset(e.getFace());
			}

			pendingBlockPlacements.put(pos, state);
			timeLeftAtPos.put(pos, TIME_TO_WAIT);
		}

		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public void tick(TickEvent.PlayerTickEvent e) {
			if (!Handlers.isBestExecutionSide(Side.CLIENT)) {
				return;
			}

			if (e.side != Side.CLIENT) {
				return;
			}

			Map<BlockPos, Integer> newMap = Maps.newHashMap();
			World world = e.player.worldObj;
			for (Map.Entry<BlockPos, Integer> entry : timeLeftAtPos.entrySet()) {
				BlockPos pos = entry.getKey();
				int newValue = entry.getValue() - 1;
				if (newValue == 0) {
					IBlockState newState = pendingBlockPlacements.remove(pos);
					if (world.getBlockState(pos).getBlock() == newState.getBlock()) {
						ClientChatUtils.setBlock(pos, newState);
					}
				} else {
					newMap.put(pos, newValue);
				}
			}
			timeLeftAtPos.clear();
			timeLeftAtPos.putAll(newMap);
		}

		@SubscribeEvent
		public void onPlayerChangedDimension(PlayerChangedDimensionEvent e) {
			if (e.player != Minecraft.getMinecraft().thePlayer) {
				return;
			}
			clearPendingBlocks();
		}

		@SubscribeEvent
		public void onPlayerLoggedOut(PlayerLoggedOutEvent e) {
			if (e.player != Minecraft.getMinecraft().thePlayer) {
				return;
			}
			clearPendingBlocks();
		}

		@SubscribeEvent
		public void onPlayerRespawned(PlayerRespawnEvent e) {
			if (e.player != Minecraft.getMinecraft().thePlayer) {
				return;
			}
			clearPendingBlocks();
		}

		private void clearPendingBlocks() {
			pendingBlockPlacements.clear();
			timeLeftAtPos.clear();
		}
	}

	private static class ServerEvtListener {
		@SubscribeEvent
		public void onBlockPlaced(BlockEvent.PlaceEvent e) {
			if (!Handlers.isBestExecutionSide(Side.SERVER)) {
				return;
			}

			if (e.getWorld().isRemote) {
				return;
			}

			IBlockState state = getPickedBlock(e.getItemInHand());
			if (state == null) {
				return;
			}

			e.getWorld().setBlockState(e.getPos(), state);
		}
	}

	@SuppressWarnings("deprecation")
	private static IBlockState getPickedBlock(ItemStack stack) {
		if (stack == null) {
			return null;
		}

		NBTTagCompound stackTag = stack.getTagCompound();
		if (stackTag == null || !stackTag.hasKey("PickedBlock", Constants.NBT.TAG_STRING)
				|| !stackTag.hasKey("StateData", Constants.NBT.TAG_BYTE)) {
			return null;
		}

		Block block = Block.getBlockFromName(stackTag.getString("PickedBlock"));
		if (block == null) {
			return null;
		}
		int metadata = stackTag.getByte("StateData");
		// No-one will ever know why this is deprecated :/
		return block.getStateFromMeta(metadata);
	}

}
