package net.earthcomputer.redbuilder.logic;

import net.minecraft.block.BlockRedstoneRepeater;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComponentRepeater implements IRedstoneComponent {

	@Override
	public PowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		PowerInfo powerInfo = new PowerInfo();
		EnumFacing repeaterFacing = state.getValue(BlockRedstoneRepeater.FACING).getOpposite();

		powerInfo.canBePoweredBy(repeaterFacing.getOpposite());
		powerInfo.canBePoweredByStrongly(repeaterFacing.rotateY());
		powerInfo.canBePoweredByStrongly(repeaterFacing.rotateYCCW());

		if (state.getBlock() == Blocks.POWERED_REPEATER) {
			powerInfo.powerStrong(repeaterFacing, PowerInfo.MAX_POWER);
		}

		return powerInfo;
	}

}
