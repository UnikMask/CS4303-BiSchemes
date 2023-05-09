package bischemes.engine.physics;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import bischemes.engine.physics.Primitive.PrimitiveType;
import bischemes.engine.physics.PrimitiveAssembly.PrimitiveInSet;
import processing.core.PVector;

public class PrimitiveUtils {
	private static Primitive circleWithCOM = new Primitive(new Surface(0, 0, 0), generateRegularPolygon(26, 1));

	public static double getRectInertia(double width, double height, double mass, PVector COM) {
		return 1.0 / 12.0 * mass * (width * width + height * height);
	}

	public static double getPrimitiveInertia(Primitive p, double mass, PVector COM) {
		if (p.getType() == PrimitiveType.CIRCLE && COM == null) {
			return 2.0 / 5.0 * mass * p.getRadius() * p.getRadius();
		} else if (p.getType() == PrimitiveType.CIRCLE) {
			p = circleWithCOM;
		}
		double inertia = 0;
		for (PVector v : p.getVertices()) {
			inertia += Math.pow(PVector.sub(v, COM).mag(), 2);
		}
		inertia *= (mass / p.getVertices().size());
		return inertia;
	}

	public static double getPrimitiveAssemblyInertia(PrimitiveAssembly p, double mass, PVector COM) {
		double inertia = 0;
		for (PrimitiveInSet ps : p.getAssembly()) {
			inertia += getPrimitiveInertia(ps.primitive, mass / p.getAssemblySize(), PVector.sub(COM, ps.offset));
		}
		return inertia;

	}

	public static List<PVector> generateRegularPolygon(int sides, double size) {
		List<PVector> vertices = new ArrayList<>(sides);
		double increment = 2 * Math.PI / sides;
		for (int i = 0; i < sides; i++) {
			double angle = i * increment;
			vertices.add(new PVector(((float) (size / 2 * Math.cos(angle))), ((float) (size / 2 * Math.sin(angle)))));
		}
		return vertices;
	}

	public static List<PVector> makeRect(PVector size) {
		return Arrays.asList(new PVector(-size.x / 2, -size.y / 2), new PVector(-size.x / 2, size.y / 2),
				new PVector(size.x / 2, size.y / 2), new PVector(size.x / 2, -size.y / 2));
	}
}
