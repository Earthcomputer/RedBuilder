package net.earthcomputer.redbuilder.logic;

import net.minecraft.block.BlockRedstoneTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComponentRedstoneTorch implements IRedstoneComponent {

	@Override
	public RedstonePowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		RedstonePowerInfo powerInfo = new RedstonePowerInfo();
		EnumFacing backwardsFacing = state.getValue(BlockRedstoneTorch.FACING).getOpposite();

		powerInfo.canBePoweredBy(backwardsFacing);

		for (EnumFacing side : EnumFacing.values()) {
			if (side != backwardsFacing) {
				powerInfo.powerWeak(side, 15);
			}
		}
		powerInfo.powerStrong(EnumFacing.UP, 15);

		return powerInfo;
	}

}
