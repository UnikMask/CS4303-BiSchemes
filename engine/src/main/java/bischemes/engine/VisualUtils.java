package bischemes.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import processing.core.PImage;
import processing.core.PVector;

public final class VisualUtils {
	private VisualUtils() {}

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



	// ch315 - additional VisualUtil methods added

	// identical to makeTexturedPolygon() except takes a PImage instead of a String texture path
	public static VisualAttribute makeTexturedPolygon(PVector size, int sides, float baseAngle, PVector anchor,
													  PImage texture) {
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
	// identical to makeRect() except takes a PImage instead of a String texture path
	public VisualAttribute makeRect(PVector size, PImage texture) {
		return makeTexturedPolygon(size, 4, 0, new PVector(), texture);
	}

	public static VisualAttribute makeTriangle(PVector anchor, PVector vertex1, PVector vertex2, int colour) {
		List<PVector> vertices = new ArrayList<>(3);
		vertices.add(anchor);
		vertices.add(vertex1);
		vertices.add(vertex2);
		return new VisualAttribute(vertices, colour);
	}
	public static VisualAttribute makeEdge(PVector anchor, PVector vertex, int colour) {
		List<PVector> vertices = new ArrayList<>(2);
		vertices.add(anchor);
		vertices.add(vertex);
		return new VisualAttribute(vertices, colour);
	}
}
