package net.earthcomputer.redbuilder.logic;

import net.minecraft.block.BlockRailDetector;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComponentRailDetector implements IRedstoneComponent {

	@Override
	public PowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		PowerInfo powerInfo = new PowerInfo();
		if (state.getValue(BlockRailDetector.POWERED)) {
			powerInfo.powerNeighborsWeakly();
			powerInfo.powerStrong(EnumFacing.DOWN, PowerInfo.MAX_POWER);
		}
		return powerInfo;
	}

}
