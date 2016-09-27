package net.earthcomputer.redbuilder.logic;

import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComponentPressurePlateNormal implements IRedstoneComponent {

	@Override
	public RedstonePowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		RedstonePowerInfo powerInfo = new RedstonePowerInfo();
		if (state.getValue(BlockPressurePlate.POWERED)) {
			powerInfo.powerNeighborsWeakly();
			powerInfo.powerStrong(EnumFacing.DOWN, 15);
		}
		return powerInfo;
	}

}
