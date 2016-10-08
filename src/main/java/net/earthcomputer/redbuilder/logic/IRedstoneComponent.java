package net.earthcomputer.redbuilder.logic;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IRedstoneComponent {

	PowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state);
	
}
