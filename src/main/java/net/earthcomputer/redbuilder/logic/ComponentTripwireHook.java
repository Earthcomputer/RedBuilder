package net.earthcomputer.redbuilder.logic;

import net.minecraft.block.BlockTripWireHook;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComponentTripwireHook implements IRedstoneComponent {

	@Override
	public RedstonePowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		RedstonePowerInfo powerInfo = new RedstonePowerInfo();

		if (state.getValue(BlockTripWireHook.POWERED)) {
			powerInfo.powerNeighborsWeakly();
			powerInfo.powerStrong(state.getValue(BlockTripWireHook.FACING).getOpposite(), 15);
		}

		return powerInfo;
	}

}
