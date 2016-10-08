package net.earthcomputer.redbuilder.logic;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComponentNormalPowered implements IRedstoneComponent {

	@Override
	public PowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		PowerInfo powerInfo = new PowerInfo();
		powerInfo.canBePowered();
		return powerInfo;
	}

}
