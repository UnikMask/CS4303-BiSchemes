package bischemes.engine;

import java.util.ArrayList;
import java.util.List;

import processing.core.PVector;

public class VisualUtils {
	public static VisualAttribute makeTexturedPolygon(PVector size, int sides, float baseAngle, PVector anchor,
			String texture) {
		List<PVector> vertices = new ArrayList<>();
		List<PVector> uvMap = new ArrayList<>();

		float increment = (float) (2 * Math.PI / sides);
		for (int i = 0; i < sides; i++) {
			float angle = baseAngle + i * increment;
			vertices.add(new PVector((float) (anchor.x + size.x / 2 * Math.cos(angle)),
					(float) (anchor.y + size.y / 2 * Math.sin(angle))));
			uvMap.add(new PVector(1 / 2 + 1 / 2 * (float) Math.cos(angle), 1 / 2 + 1 / 2 * (float) Math.sin(angle)));
		}
		return new VisualAttribute(vertices, uvMap, texture);
	}

	public static VisualAttribute makeUntexturedPolygon(PVector size, int sides, float baseAngle, PVector anchor,
			int color) {
		List<PVector> vertices = new ArrayList<>();

		float increment = (float) (2 * Math.PI / sides);
		for (int i = 0; i < sides; i++) {
			float angle = baseAngle + i * increment;
			vertices.add(new PVector((float) (anchor.x + size.x / 2 * Math.cos(angle)),
					(float) (anchor.y + size.y / 2 * Math.sin(angle))));
		}
		return new VisualAttribute(vertices, color);
	}

	public static VisualAttribute makeRect(PVector size, int color) {
		return makeUntexturedPolygon(size, 4, 0, new PVector(0, 0), color);
	}

	public VisualAttribute makeRect(PVector size, String texture) {
		return makeTexturedPolygon(size, 4, 0, new PVector(), texture);
	}
}
