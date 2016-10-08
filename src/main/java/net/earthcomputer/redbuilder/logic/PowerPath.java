package net.earthcomputer.redbuilder.logic;

import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;

public class PowerPath {

	protected List<BlockPos> points = Lists.newArrayList();
	protected List<Integer> colors = Lists.newArrayList();

	protected PowerPath(BlockPos startingPoint) {
		points.add(startingPoint);
	}

	public static PowerPath startPoint(BlockPos startingPoint) {
		return new PowerPath(startingPoint);
	}

	public PowerPath add(BlockPos point, int color) {
		points.add(point);
		colors.add(color);
		return this;
	}

	public void draw() {
		if (colors.isEmpty()) {
			return;
		}
		if (points.size() != colors.size() + 1) {
			throw new IllegalStateException("points.size() != colors.size() + 1");
		}

		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer vertexBuffer = tessellator.getBuffer();

		vertexBuffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

		Iterator<BlockPos> pointIterator = points.iterator();
		Iterator<Integer> colorIterator = colors.iterator();
		BlockPos point = pointIterator.next();
		int color;

		while (pointIterator.hasNext()) {
			color = colorIterator.next();
			drawPoint(vertexBuffer, point, color);
			point = pointIterator.next();
			drawPoint(vertexBuffer, point, color);
		}

		tessellator.draw();
	}

	private void drawPoint(VertexBuffer buffer, BlockPos point, int color) {
		// @formatter:off
		buffer.pos(point.getX() + 0.5, point.getY() + 0.5, point.getZ() + 0.5)
			.color((color & 0x00ff0000) >> 16, (color & 0x0000ff00) >> 8, color & 0x000000ff,
				(color & 0xff000000) >>> 24)
			.endVertex();
		// @formatter:on
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((colors == null) ? 0 : colors.hashCode());
		result = prime * result + ((points == null) ? 0 : points.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PowerPath))
			return false;
		PowerPath other = (PowerPath) obj;
		if (colors == null) {
			if (other.colors != null)
				return false;
		} else if (!colors.equals(other.colors))
			return false;
		if (points == null) {
			if (other.points != null)
				return false;
		} else if (!points.equals(other.points))
			return false;
		return true;
	}

}
