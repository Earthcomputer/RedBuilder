package net.earthcomputer.redbuilder.logic;

import net.minecraft.block.BlockButton;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComponentButton implements IRedstoneComponent {

	@Override
	public RedstonePowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		RedstonePowerInfo powerInfo = new RedstonePowerInfo();
		if (state.getValue(BlockButton.POWERED)) {
			powerInfo.powerNeighborsWeakly();
			powerInfo.powerStrong(state.getValue(BlockButton.FACING).getOpposite(), 15);
		}
		return powerInfo;
	}

}
