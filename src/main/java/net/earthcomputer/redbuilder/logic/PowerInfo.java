package net.earthcomputer.redbuilder.logic;

import java.util.EnumMap;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PowerInfo {

	public static final int MAX_POWER = 15;
	
	private EnumMap<EnumFacing, Integer> strongOutputs = Maps.newEnumMap(EnumFacing.class);
	private EnumMap<EnumFacing, Integer> weakOutputs = Maps.newEnumMap(EnumFacing.class);
	private Set<PowerRequirement> powerRequirements = Sets.newHashSet();

	public PowerInfo() {
		for (EnumFacing facing : EnumFacing.values()) {
			strongOutputs.put(facing, 0);
			weakOutputs.put(facing, 0);
		}
	}

	public void powerNeighborsStrongly() {
		for (EnumFacing side : EnumFacing.values()) {
			powerStrong(side, MAX_POWER);
		}
	}

	public void powerNeighborsWeakly() {
		for (EnumFacing side : EnumFacing.values()) {
			powerWeak(side, MAX_POWER);
		}
	}

	public void powerStrong(EnumFacing side, int power) {
		if (power > strongOutputs.get(side)) {
			strongOutputs.put(side, power);
		}
		powerWeak(side, power);
	}

	public void powerWeak(EnumFacing side, int power) {
		if (power > weakOutputs.get(side)) {
			weakOutputs.put(side, power);
		}
	}

	public void addPowerRequirement(PowerRequirement powerRequirement) {
		powerRequirements.add(powerRequirement);
	}

	public void canBePoweredBy(BlockPos pos, EnumFacing side) {
		addPowerRequirement(new PowerRequirement(pos.offset(side), side.getOpposite(), false));
	}

	public void canBePoweredBy(BlockPos pos) {
		for (EnumFacing side : EnumFacing.values()) {
			canBePoweredBy(pos, side);
		}
	}

	public void canBePoweredBy(EnumFacing side) {
		canBePoweredBy(BlockPos.ORIGIN, side);
	}

	public void canBePowered() {
		canBePoweredBy(BlockPos.ORIGIN);
	}

	public void canBePoweredByStrongly(BlockPos pos, EnumFacing side) {
		addPowerRequirement(new PowerRequirement(pos.offset(side), side.getOpposite(), true));
	}

	public void canBePoweredByStrongly(BlockPos pos) {
		for (EnumFacing side : EnumFacing.values()) {
			canBePoweredByStrongly(pos, side);
		}
	}

	public void canBePoweredByStrongly(EnumFacing side) {
		canBePoweredByStrongly(BlockPos.ORIGIN, side);
	}

	public void canBePoweredStrongly() {
		canBePoweredByStrongly(BlockPos.ORIGIN);
	}

	protected EnumMap<EnumFacing, Integer> getStrongOutputs() {
		return strongOutputs;
	}

	protected EnumMap<EnumFacing, Integer> getWeakOutputs() {
		return weakOutputs;
	}

	protected Set<PowerRequirement> getPowerRequirements() {
		return powerRequirements;
	}

	public boolean isValidPowerSource(World world, BlockPos thisPos, BlockPos otherPos) {
		return true;
	}

	public boolean isValidPowerOutput(World world, BlockPos thisPos, BlockPos otherPos) {
		return true;
	}

	public Set<PowerPath> genPowerPaths(World world, BlockPos pos, IBlockState state) {
		Set<PowerPath> paths = Sets.newHashSet();
		// Power inputs
		for (PowerRequirement requirement : powerRequirements) {
			BlockPos origin = pos.add(requirement.getOrigin());
			if (world.getBlockState(origin).isNormalCube()) {
				if (requirement.requiresStrong()) {
					continue;
				}
				for (EnumFacing side : EnumFacing.values()) {
					BlockPos offsetPos = origin.offset(side);
					if (!isValidPowerSource(world, pos, offsetPos)) {
						continue;
					}
					PowerInfo otherPowerInfo = RedstoneComponentRegistry.getPowerInfo(world, offsetPos);
					int powerProviding = otherPowerInfo.getStrongOutputs().get(side.getOpposite());
					if (powerProviding == 0) {
						continue;
					}
					int color = PowerPathColors.interpolate(PowerPathColors.INPUT_MIN, PowerPathColors.INPUT_MAX,
							powerProviding);
					paths.add(PowerPath.startPoint(offsetPos).add(origin, color)
							.add(origin.offset(requirement.getDirection()), color).add(pos, color));
				}
			} else {
				if (!isValidPowerSource(world, pos, origin)) {
					continue;
				}
				PowerInfo otherPowerInfo = RedstoneComponentRegistry.getPowerInfo(world, origin);
				int powerProviding;
				if (requirement.requiresStrong()) {
					powerProviding = otherPowerInfo.getStrongOutputs().get(requirement.getDirection());
				} else {
					powerProviding = otherPowerInfo.getWeakOutputs().get(requirement.getDirection());
				}
				if (powerProviding == 0) {
					continue;
				}
				int color = PowerPathColors.interpolate(PowerPathColors.INPUT_MIN, PowerPathColors.INPUT_MAX,
						powerProviding);
				paths.add(PowerPath.startPoint(origin).add(origin.offset(requirement.getDirection()), color).add(pos,
						color));
			}
		}
		// Power outputs
		for (EnumFacing side : EnumFacing.values()) {
			BlockPos offsetPos = pos.offset(side);
			int weakPower = weakOutputs.get(side);
			if (weakPower == 0) {
				continue;
			}
			int strongPower = strongOutputs.get(side);
			int color = PowerPathColors.interpolate(PowerPathColors.OUTPUT_MIN, PowerPathColors.OUTPUT_MAX, weakPower);
			if (isValidPowerOutput(world, pos, offsetPos)) {
				paths.add(PowerPath.startPoint(pos).add(offsetPos, color));
			}
			if (strongPower == 0) {
				continue;
			}
			if (!world.getBlockState(offsetPos).isNormalCube()) {
				continue;
			}
			color = PowerPathColors.interpolate(PowerPathColors.OUTPUT_MIN, PowerPathColors.OUTPUT_MAX, strongPower);
			for (EnumFacing secondarySide : EnumFacing.values()) {
				BlockPos secondaryOffsetPos = offsetPos.offset(secondarySide);
				if (secondarySide == side.getOpposite()) {
					continue;
				}
				if (world.getBlockState(secondaryOffsetPos).isNormalCube()) {
					continue;
				}
				if (!isValidPowerOutput(world, pos, secondaryOffsetPos)) {
					continue;
				}
				paths.add(PowerPath.startPoint(offsetPos).add(secondaryOffsetPos, color));
			}
		}
		return paths;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("strongOutputs", strongOutputs).add("weakOutputs", weakOutputs)
				.add("powerRequirements", powerRequirements).toString();
	}

	public static class PowerRequirement {
		private BlockPos origin;
		private EnumFacing direction;
		private boolean requireStrong;

		public PowerRequirement(BlockPos origin, EnumFacing direction, boolean requireStrong) {
			this.origin = origin;
			this.direction = direction;
			this.requireStrong = requireStrong;
		}

		public BlockPos getOrigin() {
			return origin;
		}

		public EnumFacing getDirection() {
			return direction;
		}

		public boolean requiresStrong() {
			return requireStrong;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((direction == null) ? 0 : direction.hashCode());
			result = prime * result + ((origin == null) ? 0 : origin.hashCode());
			result = prime * result + (requireStrong ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof PowerRequirement))
				return false;
			PowerRequirement other = (PowerRequirement) obj;
			if (direction != other.direction)
				return false;
			if (origin == null) {
				if (other.origin != null)
					return false;
			} else if (!origin.equals(other.origin))
				return false;
			if (requireStrong != other.requireStrong)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return Objects.toStringHelper(this).add("origin", origin).add("direction", direction)
					.add("requireStrong", requireStrong).toString();
		}
	}

}
