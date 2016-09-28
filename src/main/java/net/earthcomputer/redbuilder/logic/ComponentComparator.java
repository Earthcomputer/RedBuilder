package net.earthcomputer.redbuilder.logic;

import java.util.List;

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

	@Override
	public RedstonePowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		RedstonePowerInfo powerInfo = new RedstonePowerInfo();
		EnumFacing comparatorFacing = state.getValue(BlockRedstoneComparator.FACING).getOpposite();

		int inputOverride = getInputOverride(world, pos, comparatorFacing);
		if (inputOverride == -1) {
			powerInfo.canBePoweredBy(comparatorFacing.getOpposite());
		}

		// Redstone blocks are an exception to the normal rule
		EnumFacing sideFacing = comparatorFacing.rotateY();
		if (world.getBlockState(pos.offset(sideFacing)).getBlock() == Blocks.REDSTONE_BLOCK) {
			powerInfo.canBePoweredBy(sideFacing);
		} else {
			powerInfo.canBePoweredByStrongly(sideFacing);
		}
		sideFacing = comparatorFacing.rotateYCCW();
		if (world.getBlockState(pos.offset(sideFacing)).getBlock() == Blocks.REDSTONE_BLOCK) {
			powerInfo.canBePoweredBy(sideFacing);
		} else {
			powerInfo.canBePoweredByStrongly(sideFacing);
		}

		int power = calcComparatorPower(world, pos, state, comparatorFacing, inputOverride);
		if (power > 0) {
			powerInfo.powerStrong(comparatorFacing, power);
		}
		return powerInfo;
	}

	private int getInputOverride(World world, BlockPos pos, final EnumFacing comparatorFacing) {
		EnumFacing backwardsFacing = comparatorFacing.getOpposite();
		BlockPos testingPos = pos.offset(backwardsFacing);
		IBlockState testingBlock = world.getBlockState(testingPos);
		if (testingBlock.hasComparatorInputOverride()) {
			return testingBlock.getComparatorInputOverride(world, testingPos);
		}

		if (testingBlock.isNormalCube()) {
			if (world.getRedstonePower(pos, comparatorFacing) >= 15) {
				return -1;
			}

			testingPos = testingPos.offset(backwardsFacing);
			testingBlock = world.getBlockState(testingPos);
			if (testingBlock.hasComparatorInputOverride()) {
				return testingBlock.getComparatorInputOverride(world, testingPos);
			}

			List<EntityItemFrame> itemFrameList = world.getEntitiesWithinAABB(EntityItemFrame.class,
					new AxisAlignedBB(testingPos), new Predicate<EntityItemFrame>() {
						@Override
						public boolean apply(EntityItemFrame itemFrame) {
							return itemFrame != null && itemFrame.getHorizontalFacing() == comparatorFacing;
						}
					});
			if (itemFrameList.size() == 1) {
				return itemFrameList.get(0).getAnalogOutput();
			}
		}
		return -1;
	}

	private int calcComparatorPower(World world, BlockPos pos, IBlockState state, EnumFacing comparatorFacing,
			int inputOverride) {
		EnumFacing backwardsFacing = comparatorFacing.getOpposite();

		int input = inputOverride == -1 ? world.getRedstonePower(pos.offset(backwardsFacing), backwardsFacing)
				: inputOverride;
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
		// Redstone blocks are an exception to the normal rule
		if (world.getBlockState(offsetPos).getBlock() == Blocks.REDSTONE_BLOCK) {
			return 15;
		} else {
			return world.getStrongPower(offsetPos, sideFacing);
		}
	}

}
