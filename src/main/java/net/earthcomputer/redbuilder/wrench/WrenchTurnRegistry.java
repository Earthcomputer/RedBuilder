package net.earthcomputer.redbuilder.wrench;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockQuartz;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
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
		addFunction(new IWrenchFunction() {
			@Override
			public boolean applies(IBlockState state) {
				return state.getProperties().containsKey(BlockHorizontal.FACING);
			}

			@Override
			public IBlockState turn(IBlockState state, Axis axis, AxisDirection dir) {
				if (axis != Axis.Y) {
					return state;
				} else {
					return state.withProperty(BlockHorizontal.FACING,
							rotateFacing(state.getValue(BlockHorizontal.FACING), axis, dir));
				}
			}
		});
		addFunction(new IWrenchFunction() {
			@Override
			public boolean applies(IBlockState state) {
				return state.getProperties().containsKey(BlockLog.LOG_AXIS);
			}

			@Override
			public IBlockState turn(IBlockState state, Axis axis, AxisDirection dir) {
				EnumFacing facing;
				switch (state.getValue(BlockLog.LOG_AXIS)) {
				case X:
					facing = EnumFacing.EAST;
					break;
				case Y:
					facing = EnumFacing.UP;
					break;
				case Z:
					facing = EnumFacing.SOUTH;
					break;
				default:
					throw new AssertionError();
				}
				facing = rotateFacing(facing, axis, dir);
				return state.withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.fromFacingAxis(facing.getAxis()));
			}
		});
		addFunction(new IWrenchFunction() {
			@Override
			public boolean applies(IBlockState state) {
				return state.getProperties().containsKey(BlockRotatedPillar.AXIS);
			}

			@Override
			public IBlockState turn(IBlockState state, Axis axis, AxisDirection dir) {
				EnumFacing facing = EnumFacing.getFacingFromAxis(AxisDirection.POSITIVE,
						state.getValue(BlockRotatedPillar.AXIS));
				facing = rotateFacing(facing, axis, dir);
				return state.withProperty(BlockRotatedPillar.AXIS, facing.getAxis());
			}
		});
		addFunction(new IWrenchFunction() {
			@Override
			public boolean applies(IBlockState state) {
				return state.getProperties().containsKey(BlockQuartz.VARIANT);
			}

			@Override
			public IBlockState turn(IBlockState state, Axis axis, AxisDirection dir) {
				BlockQuartz.EnumType variant = state.getValue(BlockQuartz.VARIANT);
				switch (variant) {
				case LINES_X:
					switch (axis) {
					case Y:
						variant = BlockQuartz.EnumType.LINES_Z;
						break;
					case Z:
						variant = BlockQuartz.EnumType.LINES_Y;
						break;
					default:
						break;
					}
					break;
				case LINES_Y:
					switch (axis) {
					case X:
						variant = BlockQuartz.EnumType.LINES_Z;
						break;
					case Z:
						variant = BlockQuartz.EnumType.LINES_X;
						break;
					default:
						break;
					}
					break;
				case LINES_Z:
					switch (axis) {
					case X:
						variant = BlockQuartz.EnumType.LINES_Y;
						break;
					case Y:
						variant = BlockQuartz.EnumType.LINES_X;
						break;
					default:
						break;
					}
					break;
				default:
					break;
				}
				return state.withProperty(BlockQuartz.VARIANT, variant);
			}
		});
		addFunction(new IWrenchFunction() {
			@Override
			public boolean applies(IBlockState state) {
				return state.getProperties().containsKey(BlockSlab.HALF);
			}

			@Override
			public IBlockState turn(IBlockState state, Axis axis, AxisDirection dir) {
				if (axis == Axis.Y) {
					return state;
				} else {
					return state.withProperty(BlockSlab.HALF,
							state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.BOTTOM
									? BlockSlab.EnumBlockHalf.TOP : BlockSlab.EnumBlockHalf.BOTTOM);
				}
			}
		});
		addFunction(new IWrenchFunction() {
			@Override
			public boolean applies(IBlockState state) {
				return BlockStairs.isBlockStairs(state);
			}

			@Override
			public IBlockState turn(IBlockState state, Axis axis, AxisDirection dir) {
				EnumFacing stairsFacing = state.getValue(BlockStairs.FACING);
				EnumFacing rotatedFacing = rotateFacing(stairsFacing, axis, dir);
				if (axis == stairsFacing.getAxis()) {
					boolean flip;
					if (state.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.BOTTOM) {
						flip = rotatedFacing == EnumFacing.DOWN;
					} else {
						flip = rotatedFacing == EnumFacing.UP;
					}
					if (flip) {
						return state.withProperty(BlockStairs.HALF,
								state.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.BOTTOM
										? BlockStairs.EnumHalf.TOP : BlockStairs.EnumHalf.BOTTOM);
					} else {
						return state.withProperty(BlockStairs.FACING, stairsFacing.getOpposite());
					}
				} else if (rotatedFacing.getAxis() == Axis.Y) {
					return state;
				} else {
					return state.withProperty(BlockStairs.FACING, rotatedFacing);
				}
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
