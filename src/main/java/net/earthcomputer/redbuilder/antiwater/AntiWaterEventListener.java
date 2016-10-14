package net.earthcomputer.redbuilder.antiwater;

import static net.minecraft.init.Blocks.*;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.earthcomputer.redbuilder.IRedBuilderFeature;
import net.earthcomputer.redbuilder.RedBuilder;
import net.earthcomputer.redbuilder.RedBuilderSettings;
import net.earthcomputer.redbuilder.network.ClientInstructionWrapper;
import net.earthcomputer.redbuilder.network.handler.Handlers;
import net.earthcomputer.redbuilder.network.handler.UnsupportedInstructionException;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

public class AntiWaterEventListener implements IRedBuilderFeature {

	private static final Set<Block> PROTECTED_BLOCKS = ImmutableSet.of(RAIL, GOLDEN_RAIL, ACTIVATOR_RAIL, DETECTOR_RAIL,
			LEVER, WOODEN_BUTTON, STONE_BUTTON, TRIPWIRE, TRIPWIRE_HOOK, REDSTONE_WIRE, UNLIT_REDSTONE_TORCH,
			REDSTONE_TORCH, UNPOWERED_REPEATER, POWERED_REPEATER, UNPOWERED_COMPARATOR, POWERED_COMPARATOR);

	@Override
	public void initialize() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onLoadWorld(WorldEvent.Load e) {
		e.getWorld().addEventListener(new WorldEvtListener());
	}

	private static void onBlockChanged(World world, BlockPos pos, IBlockState oldBlock, IBlockState newBlock) {
		if (!Handlers.isBestExecutionSide(world)) {
			return;
		}

		if (Handlers.isBestExecutionSide(Side.CLIENT) && !ClientInstructionWrapper.getPlayer().isCreative()) {
			return;
		}

		if (!RedBuilderSettings.antiWaterSetting.isMatchingLiquid(newBlock.getBlock())) {
			return;
		}

		if (!PROTECTED_BLOCKS.contains(oldBlock.getBlock())) {
			return;
		}

		if (Handlers.isBestExecutionSide(Side.CLIENT)) {
			try {
				Handlers.getClientSideInstructionHandler().setBlock(pos, oldBlock);
			} catch (UnsupportedInstructionException e) {
				RedBuilder.LOGGER.error(e.getMessage());
			}
		} else {
			world.setBlockState(pos, oldBlock);
		}
	}

	private static class WorldEvtListener implements IWorldEventListener {

		@Override
		public void notifyBlockUpdate(World world, BlockPos pos, IBlockState oldState, IBlockState newState,
				int flags) {
			onBlockChanged(world, pos, oldState, newState);
		}

		@Override
		public void notifyLightSet(BlockPos pos) {
		}

		@Override
		public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
		}

		@Override
		public void playSoundToAllNearExcept(EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x,
				double y, double z, float volume, float pitch) {
		}

		@Override
		public void playRecord(SoundEvent soundIn, BlockPos pos) {
		}

		@Override
		public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord,
				double xSpeed, double ySpeed, double zSpeed, int... parameters) {
		}

		@Override
		public void onEntityAdded(Entity entityIn) {
		}

		@Override
		public void onEntityRemoved(Entity entityIn) {
		}

		@Override
		public void broadcastSound(int soundID, BlockPos pos, int data) {
		}

		@Override
		public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {
		}

		@Override
		public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
		}

	}

}
