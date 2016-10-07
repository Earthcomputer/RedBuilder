package net.earthcomputer.redbuilder.wrench;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;

public enum EnumTurnDirection {
	Y_AXIS("y_axis"), FACING_AXIS("facing_axis"), PERPENDICULAR_FACING_AXIS("perpendicular_facing_axis");

	private String name;

	private EnumTurnDirection(String name) {
		this.name = name;
	}

	public Axis getAxis(EnumFacing horizontalPlayerFacing) {
		switch (this) {
		case Y_AXIS:
			return Axis.Y;
		case FACING_AXIS:
			return horizontalPlayerFacing.rotateY().getAxis();
		case PERPENDICULAR_FACING_AXIS:
			return horizontalPlayerFacing.getAxis();
		default:
			// wtf?
			throw new AssertionError();
		}
	}

	public AxisDirection getAxisDirection(EnumFacing horizontalPlayerFacing, boolean reverse) {
		AxisDirection direction;

		switch (this) {
		case Y_AXIS:
			direction = AxisDirection.POSITIVE;
			break;
		case FACING_AXIS:
			direction = horizontalPlayerFacing.rotateY().getAxisDirection();
			break;
		case PERPENDICULAR_FACING_AXIS:
			direction = reverseAxisDirection(horizontalPlayerFacing.getAxisDirection());
			break;
		default:
			// wtf?
			throw new AssertionError();
		}

		if (reverse) {
			direction = reverseAxisDirection(direction);
		}
		return direction;
	}

	public EnumTurnDirection nextTurnDir() {
		return values()[(ordinal() + 1) % values().length];
	}

	public String getName() {
		return name;
	}

	public static EnumTurnDirection getByName(String name) {
		return BY_NAME.get(name);
	}

	public static String[] getNames() {
		return NAMES;
	}

	private static final String[] NAMES = new String[values().length];
	private static final Map<String, EnumTurnDirection> BY_NAME = Maps.newHashMap();

	static {
		EnumTurnDirection[] values = values();
		for (int i = 0; i < values.length; i++) {
			EnumTurnDirection turnDir = values[i];
			NAMES[i] = turnDir.getName();
			BY_NAME.put(turnDir.getName(), turnDir);
		}
	}

	private static AxisDirection reverseAxisDirection(AxisDirection dir) {
		return dir == AxisDirection.NEGATIVE ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
	}
}
