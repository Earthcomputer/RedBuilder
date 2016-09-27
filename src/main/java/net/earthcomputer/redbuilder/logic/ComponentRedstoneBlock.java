package net.earthcomputer.redbuilder.logic;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComponentRedstoneBlock implements IRedstoneComponent {

	@Override
	public RedstonePowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		RedstonePowerInfo powerInfo = new RedstonePowerInfo();
		powerInfo.powerNeighborsWeakly();
		return powerInfo;
	}

}
