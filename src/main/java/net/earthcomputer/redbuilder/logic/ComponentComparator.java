package net.earthcomputer.redbuilder.logic;

import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.base.Predicate;

import net.minecraft.block.BlockRedstoneComparator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComponentComparator implements IRedstoneComponent {

	private static final Random random = new Random(0);

	@Override
	public PowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		ComparatorPowerInfo powerInfo = new ComparatorPowerInfo();
		EnumFacing comparatorFacing = state.getValue(BlockRedstoneComparator.FACING).getOpposite();

		boolean hasInputOverride = getInputOverride(world, pos, comparatorFacing, powerInfo);
		if (!hasInputOverride) {
			powerInfo.canBePoweredBy(comparatorFacing.getOpposite());
		}

		registerSidePower(world, pos, comparatorFacing.rotateY(), powerInfo);
		registerSidePower(world, pos, comparatorFacing.rotateYCCW(), powerInfo);

		int power = calcComparatorPower(world, pos, state, comparatorFacing, hasInputOverride);
		if (power > 0) {
			powerInfo.powerStrong(comparatorFacing, power);
		}
		return powerInfo;
	}

	private boolean getInputOverride(World world, BlockPos pos, EnumFacing comparatorFacing,
			ComparatorPowerInfo powerInfo) {
		final EnumFacing backwardsFacing = comparatorFacing.getOpposite();
		BlockPos testingPos = pos.offset(backwardsFacing);
		IBlockState testingBlock = world.getBlockState(testingPos);
		if (testingBlock.hasComparatorInputOverride()) {
			powerInfo.setInputOverride(testingPos);
			return true;
		}

		if (testingBlock.isNormalCube()) {
			if (world.getRedstonePower(pos, comparatorFacing) >= PowerInfo.MAX_POWER) {
				return false;
			}

			testingPos = testingPos.offset(backwardsFacing);
			testingBlock = world.getBlockState(testingPos);
			if (testingBlock.hasComparatorInputOverride()) {
				powerInfo.setInputOverride(testingPos);
				return true;
			}

			List<EntityItemFrame> itemFrameList = world.getEntitiesWithinAABB(EntityItemFrame.class,
					new AxisAlignedBB(testingPos), new Predicate<EntityItemFrame>() {
						@Override
						public boolean apply(EntityItemFrame itemFrame) {
							return itemFrame != null && itemFrame.getHorizontalFacing() == backwardsFacing;
						}
					});
			if (itemFrameList.size() == 1) {
				powerInfo.setInputOverride(testingPos);
				return true;
			}
		}
		return false;
	}

	private void registerSidePower(World world, BlockPos pos, EnumFacing sideFacing, ComparatorPowerInfo powerInfo) {
		if (isWeakInputAllowed(world, pos, sideFacing)) {
			powerInfo.canBePoweredBy(sideFacing);
		} else {
			powerInfo.canBePoweredByStrongly(sideFacing);
		}
	}

	private int calcComparatorPower(World world, BlockPos pos, IBlockState state, EnumFacing comparatorFacing,
			boolean hasInputOverride) {
		EnumFacing backwardsFacing = comparatorFacing.getOpposite();

		int input;
		if (hasInputOverride) {
			random.setSeed(world.getTotalWorldTime() * 69);
			input = random.nextInt(14) + 1;
		} else {
			input = world.getRedstonePower(pos.offset(backwardsFacing), backwardsFacing);
		}
		int sidePower = Math.max(getSidePower(world, pos, comparatorFacing.rotateY()),
				getSidePower(world, pos, comparatorFacing.rotateYCCW()));

		if (state.getValue(BlockRedstoneComparator.MODE) == BlockRedstoneComparator.Mode.COMPARE) {
			return input >= sidePower ? input : 0;
		} else {
			return Math.max(input - sidePower, 0);
		}

	}

	private int getSidePower(World world, BlockPos pos, EnumFacing sideFacing) {
		BlockPos offsetPos = pos.offset(sideFacing);
		if (isWeakInputAllowed(world, pos, sideFacing)) {
			return world.getRedstonePower(offsetPos, sideFacing);
		} else {
			return world.getStrongPower(offsetPos, sideFacing);
		}
	}

	private boolean isWeakInputAllowed(World world, BlockPos pos, EnumFacing side) {
		return world.getBlockState(pos.offset(side)).getBlock() == Blocks.REDSTONE_BLOCK;
	}

	private static class ComparatorPowerInfo extends PowerInfo {
		private BlockPos inputOverrideOrigin;

		public void setInputOverride(BlockPos source) {
			this.inputOverrideOrigin = source;
		}

		@Override
		public Set<PowerPath> genPowerPaths(World world, BlockPos pos, IBlockState state) {
			Set<PowerPath> paths = super.genPowerPaths(world, pos, state);

			if (inputOverrideOrigin != null) {
				paths.add(PowerPath.startPoint(inputOverrideOrigin).add(pos, PowerPathColors.COMPARATOR_READING));
			}

			return paths;
		}
	}

}
