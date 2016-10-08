package net.earthcomputer.redbuilder.logic;

import net.minecraft.block.BlockLever;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComponentLever implements IRedstoneComponent {

	@Override
	public PowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		PowerInfo powerInfo = new PowerInfo();
		if (state.getValue(BlockLever.POWERED)) {
			powerInfo.powerNeighborsWeakly();
			powerInfo.powerStrong(state.getValue(BlockLever.FACING).getFacing().getOpposite(),
					PowerInfo.MAX_POWER);
		}
		return powerInfo;
	}

}
