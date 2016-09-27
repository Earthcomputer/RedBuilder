package net.earthcomputer.redbuilder.logic;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComponentRedstoneWire implements IRedstoneComponent {

	@Override
	public RedstonePowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		final Set<BlockPos> validPowerSources = getValidPowerSources(world, pos, state);

		RedstonePowerInfo powerInfo = new RedstonePowerInfo() {
			@Override
			public boolean isValidPowerSource(World world, BlockPos thisPos, BlockPos otherPos) {
				return world.getBlockState(otherPos).getBlock() == Blocks.REDSTONE_WIRE
						? validPowerSources.contains(otherPos) : super.isValidPowerSource(world, thisPos, otherPos);
			}
		};

		powerInfo.canBePowered();
		BlockPos posBelow = pos.down();
		if (!world.getBlockState(posBelow).isNormalCube()) {
			for (EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
				BlockPos offsetPos = posBelow.offset(side);
				if (validPowerSources.contains(offsetPos)) {
					powerInfo.addPowerRequirement(
							new RedstonePowerInfo.PowerRequirement(offsetPos.subtract(pos), side.getOpposite(), true));
				}
			}
		}

		int power = state.getValue(BlockRedstoneWire.POWER);
		if (power > 0) {
			state = state.getActualState(world, pos);
			boolean west = ((Enum<?>) state.getValue(BlockRedstoneWire.WEST)).ordinal() != 2;
			boolean east = ((Enum<?>) state.getValue(BlockRedstoneWire.EAST)).ordinal() != 2;
			boolean north = ((Enum<?>) state.getValue(BlockRedstoneWire.NORTH)).ordinal() != 2;
			boolean south = ((Enum<?>) state.getValue(BlockRedstoneWire.SOUTH)).ordinal() != 2;
			if (!west && !east && !north && !south) {
				west = east = north = south = true;
			}

			if (west) {
				powerInfo.powerStrong(EnumFacing.WEST, power);
			}
			if (east) {
				powerInfo.powerStrong(EnumFacing.EAST, power);
			}
			if (north) {
				powerInfo.powerStrong(EnumFacing.NORTH, power);
			}
			if (south) {
				powerInfo.powerStrong(EnumFacing.SOUTH, power);
			}
			powerInfo.powerStrong(EnumFacing.DOWN, power);
		}

		return powerInfo;
	}

	private Set<BlockPos> getValidPowerSources(World world, BlockPos pos, IBlockState state) {
		Set<BlockPos> validPowerSources = Sets.newHashSet();

		int powerOfWire = state.getValue(BlockRedstoneWire.POWER);
		if (powerOfWire < 15) {
			for (EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
				BlockPos offsetPos = pos.offset(side);
				IBlockState offsetState = world.getBlockState(offsetPos);
				if (offsetState.getBlock() == Blocks.REDSTONE_WIRE) {
					if (offsetState.getValue(BlockRedstoneWire.POWER) - 1 >= powerOfWire) {
						validPowerSources.add(offsetPos);
					}
				} else if (offsetState.isNormalCube()) {
					IBlockState stateAbove = world.getBlockState(offsetPos.up());
					if (stateAbove.getBlock() == Blocks.REDSTONE_WIRE
							&& stateAbove.getValue(BlockRedstoneWire.POWER) - 1 >= powerOfWire
							&& !world.getBlockState(pos.up()).isNormalCube()) {
						validPowerSources.add(offsetPos);
					}
				} else {
					IBlockState stateBelow = world.getBlockState(offsetPos.down());
					if (stateBelow.getBlock() == Blocks.REDSTONE_WIRE
							&& stateBelow.getValue(BlockRedstoneWire.POWER) - 1 >= powerOfWire) {
						validPowerSources.add(offsetPos);
					}
				}
			}
		}
		return validPowerSources;
	}

}
