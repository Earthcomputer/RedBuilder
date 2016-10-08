package net.earthcomputer.redbuilder.logic;

import net.minecraft.block.BlockTripWireHook;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComponentTripwireHook implements IRedstoneComponent {

	@Override
	public PowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		PowerInfo powerInfo = new PowerInfo();

		if (state.getValue(BlockTripWireHook.POWERED)) {
			powerInfo.powerNeighborsWeakly();
			powerInfo.powerStrong(state.getValue(BlockTripWireHook.FACING).getOpposite(), PowerInfo.MAX_POWER);
		}

		return powerInfo;
	}

}
