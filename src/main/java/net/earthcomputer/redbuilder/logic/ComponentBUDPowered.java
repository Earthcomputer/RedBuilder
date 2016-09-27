package net.earthcomputer.redbuilder.logic;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComponentBUDPowered implements IRedstoneComponent {

	@Override
	public RedstonePowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		RedstonePowerInfo powerInfo = new RedstonePowerInfo();
		powerInfo.canBePowered();
		powerInfo.canBePoweredBy(new BlockPos(0, 1, 0));
		return powerInfo;
	}

}
