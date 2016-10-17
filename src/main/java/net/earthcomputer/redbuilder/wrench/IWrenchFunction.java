package net.earthcomputer.redbuilder.wrench;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IWrenchFunction {

	boolean applies(IBlockState state);

	IBlockState turn(World world, BlockPos pos, IBlockState state, Axis axis, AxisDirection dir, EntityPlayer player, boolean modifyWorld);

}
