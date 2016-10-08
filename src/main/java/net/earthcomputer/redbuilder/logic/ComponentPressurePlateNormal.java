package net.earthcomputer.redbuilder.logic;

import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComponentPressurePlateNormal implements IRedstoneComponent {

	@Override
	public PowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		PowerInfo powerInfo = new PowerInfo();
		if (state.getValue(BlockPressurePlate.POWERED)) {
			powerInfo.powerNeighborsWeakly();
			powerInfo.powerStrong(EnumFacing.DOWN, PowerInfo.MAX_POWER);
		}
		return powerInfo;
	}

}
