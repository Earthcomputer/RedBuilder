package net.earthcomputer.redbuilder.logic;

import net.minecraft.block.BlockLever;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComponentLever implements IRedstoneComponent {

	@Override
	public RedstonePowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		RedstonePowerInfo powerInfo = new RedstonePowerInfo();
		if (state.getValue(BlockLever.POWERED)) {
			powerInfo.powerNeighborsWeakly();
			powerInfo.powerStrong(state.getValue(BlockLever.FACING).getFacing().getOpposite(), 15);
		}
		return powerInfo;
	}

}
