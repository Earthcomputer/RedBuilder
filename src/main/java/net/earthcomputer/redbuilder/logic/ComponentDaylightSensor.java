package net.earthcomputer.redbuilder.logic;

import net.minecraft.block.BlockDaylightDetector;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComponentDaylightSensor implements IRedstoneComponent {

	@Override
	public PowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		PowerInfo powerInfo = new PowerInfo();
		int power = state.getValue(BlockDaylightDetector.POWER);
		if (power > 0) {
			for (EnumFacing side : EnumFacing.values()) {
				powerInfo.powerWeak(side, power);
			}
		}
		return powerInfo;
	}

}
