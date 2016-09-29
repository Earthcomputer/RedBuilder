package net.earthcomputer.redbuilder.logic;

public class PowerPathColors {

	private PowerPathColors() {
	}

	public static final int INPUT_MIN = 0xff400000;
	public static final int INPUT_MAX = 0xffff0000;
	public static final int OUTPUT_MIN = 0xff000040;
	public static final int OUTPUT_MAX = 0xff0000ff;
	public static final int COMPARATOR_READING = 0xff8000cc;

	public static int interpolate(int minColor, int maxColor, int value) {
		return interpolate(minColor, maxColor, value, 0, 15);
	}

	public static int interpolate(int minColor, int maxColor, int value, int minValue, int maxValue) {
		int rangeValues = maxValue - minValue;
		int position = value - minValue;
		
		// http://www.gamedev.net/topic/537295-how-do-i-interpolate-colors/
		int val1;
		int val2;
		
		val1 = (minColor & 0x00ff0000) >> 16;
		val2 = (maxColor & 0x00ff0000) >> 16;
		int red = interpolateInt(val1, val2, position, rangeValues);
		
		val1 = (minColor & 0x0000ff00) >> 8;
		val2 = (maxColor & 0x0000ff00) >> 8;
		int green = interpolateInt(val1, val2, position, rangeValues);
		
		val1 = (minColor & 0x000000ff);
		val2 = (maxColor & 0x000000ff);
		int blue = interpolateInt(val1, val2, position, rangeValues);
		
		val1 = (minColor & 0xff000000) >>> 24;
		val2 = (maxColor & 0xff000000) >>> 24;
		int alpha = interpolateInt(val1, val2, position, rangeValues);
		
		return (red << 16) | (green << 8) | (blue) | (alpha << 24);
	}

	private static int interpolateInt(int min, int max, int position, int rangeValues) {
		return min + (max - min) * position / rangeValues;
	}

}
