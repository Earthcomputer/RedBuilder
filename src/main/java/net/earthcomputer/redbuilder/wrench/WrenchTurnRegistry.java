package net.earthcomputer.redbuilder.wrench;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;

public class WrenchTurnRegistry {

	private static final List<IWrenchFunction> functions = Lists.newArrayList();

	public static IBlockState turn(IBlockState state, Axis axis, AxisDirection axisDirection) {
		for (IWrenchFunction function : functions) {
			if (function.applies(state)) {
				return function.turn(state, axis, axisDirection);
			}
		}
		return state;
	}

	public static void addFunction(IWrenchFunction function) {
		functions.add(0, function);
	}

	static {
		addFunction(new IWrenchFunction() {
			@Override
			public boolean applies(IBlockState state) {
				return state.getProperties().containsKey(BlockDirectional.FACING);
			}

			@Override
			public IBlockState turn(IBlockState state, Axis axis, AxisDirection dir) {
				return state.withProperty(BlockDirectional.FACING,
						rotateFacing(state.getValue(BlockDirectional.FACING), axis, dir));
			}
		});
	}

	private static EnumFacing rotateFacing(EnumFacing facing, Axis axis, AxisDirection dir) {
		switch (dir) {
		case POSITIVE:
			return facing.rotateAround(axis);
		case NEGATIVE:
			switch (axis) {
			case X:
				switch (facing) {
				case DOWN:
					return EnumFacing.NORTH;
				case UP:
					return EnumFacing.SOUTH;
				case NORTH:
					return EnumFacing.UP;
				case SOUTH:
					return EnumFacing.DOWN;
				case WEST:
				case EAST:
					return facing;
				default:
					throw new NullPointerException("facing");
				}
			case Y:
				try {
					return facing.rotateYCCW();
				} catch (IllegalStateException e) {
					return facing;
				}
			case Z:
				switch (facing) {
				case WEST:
					return EnumFacing.DOWN;
				case EAST:
					return EnumFacing.UP;
				case DOWN:
					return EnumFacing.EAST;
				case UP:
					return EnumFacing.WEST;
				case NORTH:
				case SOUTH:
					return facing;
				default:
					throw new NullPointerException("facing");
				}
			default:
				throw new NullPointerException("axis");
			}
		default:
			throw new NullPointerException("dir");
		}
	}

}
