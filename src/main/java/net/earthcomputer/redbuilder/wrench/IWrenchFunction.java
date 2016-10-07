package net.earthcomputer.redbuilder.wrench;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;

public interface IWrenchFunction {

	boolean applies(IBlockState state);

	IBlockState turn(IBlockState state, Axis axis, AxisDirection dir);

}
