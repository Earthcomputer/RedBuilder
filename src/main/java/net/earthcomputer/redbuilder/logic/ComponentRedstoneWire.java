package net.earthcomputer.redbuilder.logic;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComponentRedstoneWire implements IRedstoneComponent {

	@Override
	public PowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		RedstoneWirePowerInfo powerInfo = new RedstoneWirePowerInfo();

		EnumMap<EnumConnectionPos, Integer> inputSides = getPowerInputs(world, pos, state, powerInfo);
		getPowerOutputs(world, pos, state, powerInfo, inputSides);

		return powerInfo;
	}

	/**
	 * @return A set of facings which contain an input and should not be used as
	 *         output at a lower or equal power level
	 */
	private EnumMap<EnumConnectionPos, Integer> getPowerInputs(World world, BlockPos pos, IBlockState state,
			RedstoneWirePowerInfo powerInfo) {
		EnumMap<EnumConnectionPos, Integer> inputSides = Maps.newEnumMap(EnumConnectionPos.class);
		for (EnumConnectionPos connectionPos : EnumConnectionPos.values()) {
			inputSides.put(connectionPos, 0);
		}
		int powerHere = state.getValue(BlockRedstoneWire.POWER);

		// Input from normal sources
		for (EnumFacing side : EnumFacing.values()) {
			BlockPos offsetPos = pos.offset(side);
			IBlockState offsetState = world.getBlockState(offsetPos);
			if (offsetState.isNormalCube()) {
				int maxPower = 0;
				for (EnumFacing secondarySide : EnumFacing.values()) {
					BlockPos secondaryPos = offsetPos.offset(secondarySide);
					IBlockState secondaryState = world.getBlockState(secondaryPos);
					if (!isRedstoneWire(secondaryState)) {
						PowerInfo secondaryPowerInfo = RedstoneComponentRegistry.getPowerInfo(world,
								secondaryPos);
						int power = secondaryPowerInfo.getStrongOutputs().get(secondarySide.getOpposite());
						if (power > 0 && maxPower == 0) {
							powerInfo.canBePoweredBy(side);
						}
						if (power > maxPower) {
							maxPower = power;
						}
					}
				}
				inputSides.put(EnumConnectionPos.fromFacing(side), maxPower);
			} else if (isRedstoneWire(offsetState)) {
				int power = offsetState.getValue(BlockRedstoneWire.POWER);
				if (power > powerHere) {
					powerInfo.addCustomInput(offsetPos, power);
				}
				inputSides.put(EnumConnectionPos.fromFacing(side), power);
			} else {
				PowerInfo offsetPowerInfo = RedstoneComponentRegistry.getPowerInfo(world, offsetPos);
				int power = offsetPowerInfo.getWeakOutputs().get(side.getOpposite());
				if (power > 0) {
					powerInfo.canBePoweredBy(side);
				}
				inputSides.put(EnumConnectionPos.fromFacing(side), power);
			}
		}

		// Input from vertically diagonal redstone wire
		for (EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
			BlockPos offsetPos = pos.offset(side);
			IBlockState offsetState = world.getBlockState(offsetPos);
			if (offsetState.isNormalCube()) {
				// Test for redstone diagonally above
				if (!world.getBlockState(pos.up()).isNormalCube()) {
					BlockPos diagonalWirePos = offsetPos.up();
					IBlockState diagonalWire = world.getBlockState(diagonalWirePos);
					if (isRedstoneWire(diagonalWire)) {
						int power = diagonalWire.getValue(BlockRedstoneWire.POWER);
						if (power > powerHere) {
							inputSides.put(EnumConnectionPos.upFromFacing(side), power);
							powerInfo.addCustomInput(diagonalWirePos, power);
						}
					}
				}
			} else {
				// Test for redstone diagonally below
				BlockPos diagonalWirePos = offsetPos.down();
				IBlockState diagonalWire = world.getBlockState(diagonalWirePos);
				if (isRedstoneWire(diagonalWire)) {
					int power = diagonalWire.getValue(BlockRedstoneWire.POWER);
					if (power > powerHere) {
						inputSides.put(EnumConnectionPos.downFromFacing(side), power);
						powerInfo.addCustomInput(diagonalWirePos, power);
					}
				}
			}
		}

		return inputSides;
	}

	private void getPowerOutputs(World world, BlockPos pos, IBlockState state, RedstoneWirePowerInfo powerInfo,
			EnumMap<EnumConnectionPos, Integer> inputSides) {
		state = state.getActualState(world, pos);
		int power = state.getValue(BlockRedstoneWire.POWER);
		// This tests whether the redstone is connected in each of the
		// directions. It may be virtually unreadable, but it bypasses the need
		// for reflection
		boolean west = ((Enum<?>) state.getValue(BlockRedstoneWire.WEST)).ordinal() != 2;
		boolean east = ((Enum<?>) state.getValue(BlockRedstoneWire.EAST)).ordinal() != 2;
		boolean north = ((Enum<?>) state.getValue(BlockRedstoneWire.NORTH)).ordinal() != 2;
		boolean south = ((Enum<?>) state.getValue(BlockRedstoneWire.SOUTH)).ordinal() != 2;
		if (!west && !east && !north && !south) {
			west = east = north = south = true;
		} else if (west && !north && !south) {
			east = true;
		} else if (east && !north && !south) {
			west = true;
		} else if (north && !west && !east) {
			south = true;
		} else if (south && !west && !east) {
			north = true;
		}

		if (west) {
			testPowerOutputOnSide(world, pos, power, EnumFacing.WEST, powerInfo, inputSides);
		}
		if (east) {
			testPowerOutputOnSide(world, pos, power, EnumFacing.EAST, powerInfo, inputSides);
		}
		if (north) {
			testPowerOutputOnSide(world, pos, power, EnumFacing.NORTH, powerInfo, inputSides);
		}
		if (south) {
			testPowerOutputOnSide(world, pos, power, EnumFacing.SOUTH, powerInfo, inputSides);
		}

		if (world.getBlockState(pos.down()).isNormalCube()) {
			powerInfo.powerStrong(EnumFacing.DOWN, power);
		}
	}

	private void testPowerOutputOnSide(World world, BlockPos pos, int power, EnumFacing side,
			RedstoneWirePowerInfo powerInfo, EnumMap<EnumConnectionPos, Integer> inputSides) {
		BlockPos offsetPos = pos.offset(side);
		IBlockState offsetState = world.getBlockState(offsetPos);

		if (power > inputSides.get(EnumConnectionPos.fromFacing(side))) {
			if (isRedstoneWire(offsetState)) {
				powerInfo.addCustomOutput(offsetPos, power);
			} else {
				powerInfo.powerStrong(side, power);
			}
		}

		// Test for redstone diagonally above
		if (!world.getBlockState(pos.up()).isNormalCube()) {
			BlockPos wirePos = offsetPos.up();
			if (isRedstoneWire(world.getBlockState(wirePos))) {
				if (power > inputSides.get(EnumConnectionPos.upFromFacing(side))) {
					powerInfo.addCustomOutput(wirePos, power);
				}
			}
		}

		// Test for redstone diagonally below
		if (!offsetState.isNormalCube()) {
			if (world.getBlockState(pos.down()).isNormalCube()) {
				BlockPos wirePos = offsetPos.down();
				if (isRedstoneWire(world.getBlockState(wirePos))) {
					if (power > inputSides.get(EnumConnectionPos.downFromFacing(side))) {
						powerInfo.addCustomOutput(wirePos, power);
					}
				}
			}
		}
	}

	private static boolean isRedstoneWire(IBlockState state) {
		return state.getBlock() == Blocks.REDSTONE_WIRE;
	}

	private static class RedstoneWirePowerInfo extends PowerInfo {
		private Map<BlockPos, Integer> customInputs = Maps.newHashMap();
		private Map<BlockPos, Integer> customOutputs = Maps.newHashMap();

		public void addCustomInput(BlockPos pos, int power) {
			customInputs.put(pos, power);
		}

		public void addCustomOutput(BlockPos pos, int power) {
			customOutputs.put(pos, power);
		}

		@Override
		public Set<PowerPath> genPowerPaths(World world, BlockPos pos, IBlockState state) {
			Set<PowerPath> powerPaths = super.genPowerPaths(world, pos, state);

			for (Map.Entry<BlockPos, Integer> customInput : customInputs.entrySet()) {
				int color = PowerPathColors.interpolate(PowerPathColors.INPUT_MIN, PowerPathColors.INPUT_MAX,
						customInput.getValue());
				powerPaths.add(PowerPath.startPoint(customInput.getKey()).add(pos, color));
			}

			for (Map.Entry<BlockPos, Integer> customOutput : customOutputs.entrySet()) {
				int color = PowerPathColors.interpolate(PowerPathColors.OUTPUT_MIN, PowerPathColors.OUTPUT_MAX,
						customOutput.getValue());
				powerPaths.add(PowerPath.startPoint(customOutput.getKey()).add(pos, color));
			}

			return powerPaths;
		}

		@Override
		public boolean isValidPowerSource(World world, BlockPos thisPos, BlockPos otherPos) {
			if (isRedstoneWire(world.getBlockState(otherPos))) {
				return false;
			} else {
				return super.isValidPowerSource(world, thisPos, otherPos);
			}
		}

		@Override
		public boolean isValidPowerOutput(World world, BlockPos thisPos, BlockPos otherPos) {
			if (isRedstoneWire(world.getBlockState(otherPos))) {
				return false;
			} else {
				return super.isValidPowerOutput(world, thisPos, otherPos);
			}
		}

	}

	private static enum EnumConnectionPos {
		WEST, EAST, NORTH, SOUTH, UP, DOWN, WEST_UP, EAST_UP, NORTH_UP, SOUTH_UP, WEST_DOWN, EAST_DOWN, NORTH_DOWN, SOUTH_DOWN;

		public static EnumConnectionPos fromFacing(EnumFacing side) {
			switch (side) {
			case DOWN:
				return DOWN;
			case EAST:
				return EAST;
			case NORTH:
				return NORTH;
			case SOUTH:
				return SOUTH;
			case UP:
				return UP;
			case WEST:
				return WEST;
			default:
				return null;
			}
		}

		public static EnumConnectionPos upFromFacing(EnumFacing side) {
			switch (side) {
			case WEST:
				return WEST_UP;
			case EAST:
				return EAST_UP;
			case NORTH:
				return NORTH_UP;
			case SOUTH:
				return SOUTH_UP;
			default:
				return null;
			}
		}

		public static EnumConnectionPos downFromFacing(EnumFacing side) {
			switch (side) {
			case WEST:
				return WEST_DOWN;
			case EAST:
				return EAST_DOWN;
			case NORTH:
				return NORTH_DOWN;
			case SOUTH:
				return SOUTH_DOWN;
			default:
				return null;
			}
		}
	}

}
