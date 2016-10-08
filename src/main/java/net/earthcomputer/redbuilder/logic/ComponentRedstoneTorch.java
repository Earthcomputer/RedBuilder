package net.earthcomputer.redbuilder.logic;

import net.minecraft.block.BlockRedstoneTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComponentRedstoneTorch implements IRedstoneComponent {

	@Override
	public PowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		PowerInfo powerInfo = new PowerInfo();
		EnumFacing backwardsFacing = state.getValue(BlockRedstoneTorch.FACING).getOpposite();

		powerInfo.canBePoweredBy(backwardsFacing);

		if (isLit(state)) {
			for (EnumFacing side : EnumFacing.values()) {
				if (side != backwardsFacing) {
					powerInfo.powerWeak(side, PowerInfo.MAX_POWER);
				}
			}
			powerInfo.powerStrong(EnumFacing.UP, PowerInfo.MAX_POWER);
		}

		return powerInfo;
	}

	private boolean isLit(IBlockState state) {
		return state.getBlock() == Blocks.REDSTONE_TORCH;
	}

}
