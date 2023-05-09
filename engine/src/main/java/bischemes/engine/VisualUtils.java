package bischemes.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import processing.core.PImage;
import processing.core.PVector;

public final class VisualUtils {

	/**
	 * Make a textured visual polygon.
	 */
	public static VisualAttribute makeTexturedPolygon(PVector size, int sides, float baseAngle, PVector anchor,
			String texture) {
		List<PVector> vertices = new ArrayList<>();
		List<PVector> uvMap = new ArrayList<>();

		generateRegularPolygon(vertices, uvMap, sides, baseAngle, size, anchor);
		return new VisualAttribute(vertices, uvMap, texture);
	}

	/**
	 * Make a textured visual polygon.
	 */
	public static VisualAttribute makeTexturedPolygon(PVector size, int sides, float baseAngle, PVector anchor,
			PImage texture) {
		List<PVector> vertices = new ArrayList<>();
		List<PVector> uvMap = new ArrayList<>();

		generateRegularPolygon(vertices, uvMap, sides, baseAngle, size, anchor);
		return new VisualAttribute(vertices, uvMap, texture);
	}

	/**
	 * Make a textured visual polygon.
	 */
	public static VisualAttribute makeTexturedPolygon(PVector size, int sides, float baseAngle, PVector anchor,
													  PImage texture, int colour) {
		List<PVector> vertices = new ArrayList<>();
		List<PVector> uvMap = new ArrayList<>();

		generateRegularPolygon(vertices, uvMap, sides, baseAngle, size, anchor);
		return new VisualAttribute(vertices, uvMap, texture, colour);
	}

	public static VisualAttribute makeUntexturedPolygon(PVector size, int sides, float baseAngle, PVector anchor,
			int color) {
		List<PVector> vertices = new ArrayList<>();

		double increment = 2 * Math.PI / sides;
		for (int i = 0; i < sides; i++) {
			double angle = baseAngle + i * increment;
			vertices.add(new PVector((float) (anchor.x + (size.x / 2) * Math.cos(angle)),
					(float) (anchor.y + (size.y / 2) * Math.sin(angle))));
		}
		return new VisualAttribute(vertices, color);
	}

	public static void generateRegularPolygon(List<PVector> vertices, List<PVector> uvMap, int sides, float baseAngle,
			PVector size, PVector anchor) {
		float increment = (float) (2 * Math.PI / sides);
		for (int i = 0; i < sides; i++) {
			float angle = baseAngle + i * increment;
			vertices.add(new PVector(anchor.x + (size.x / 2) * (float) Math.cos(angle),
					anchor.y + (size.y / 2) * (float) Math.sin(angle)));
			uvMap.add(new PVector(1 / 2 + 1 / 2 * (float) Math.cos(angle), 1 / 2 + 1 / 2 * (float) Math.sin(angle)));
		}
	}

	public static VisualAttribute makeRect(PVector size, int color) {
		return new VisualAttribute(
				Arrays.asList(new PVector(-size.x / 2, -size.y / 2), new PVector(-size.x / 2, size.y / 2),
						new PVector(size.x / 2, size.y / 2), new PVector(size.x / 2, -size.y / 2)),
				color);
	}

	public static VisualAttribute makeRect(PVector size, int color, PImage texture) {
		return new VisualAttribute(
				Arrays.asList(new PVector(-size.x / 2, -size.y / 2), new PVector(-size.x / 2, size.y / 2),
						new PVector(size.x / 2, size.y / 2), new PVector(size.x / 2, -size.y / 2)),
				Arrays.asList(new PVector(0, 1), new PVector(0, 0), new PVector(1, 0), new PVector(1, 1)), texture,
				color);
	}

	public static VisualAttribute makeRect(PVector size, String texture) {
		return new VisualAttribute(
				Arrays.asList(new PVector(-size.x / 2, -size.y / 2), new PVector(-size.x / 2, size.y / 2),
						new PVector(size.x / 2, size.y / 2), new PVector(size.x / 2, -size.y / 2)),
				Arrays.asList(new PVector(0, 0), new PVector(0, 1), new PVector(1, 1), new PVector(1, 0)), texture);
	}

	public static VisualAttribute makeRect(PVector size, PImage texture) {
		return new VisualAttribute(
				Arrays.asList(new PVector(-size.x / 2, -size.y / 2), new PVector(-size.x / 2, size.y / 2),
						new PVector(size.x / 2, size.y / 2), new PVector(size.x / 2, -size.y / 2)),
				Arrays.asList(new PVector(0, 0), new PVector(0, 1), new PVector(1, 1), new PVector(1, 0)), texture);
	}

	public static VisualAttribute makeTriangle(PVector vertex1, PVector vertex2, PVector vertex3, int colour) {
		List<PVector> vertices = new ArrayList<>(3);
		vertices.add(vertex1);
		vertices.add(vertex2);
		vertices.add(vertex3);
		return new VisualAttribute(vertices, colour);
	}

	public static VisualAttribute makeEdge(PVector anchor, PVector vertex, int colour) {
		List<PVector> vertices = new ArrayList<>(2);
		vertices.add(anchor);
		vertices.add(vertex);
		return new VisualAttribute(vertices, colour);
	}
}
