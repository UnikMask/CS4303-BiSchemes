package bischemes.engine.physics;

import bischemes.engine.physics.PrimitiveAssembly.PrimitiveInSet;
import processing.core.PVector;

public class PrimitiveUtils {
	public static double getRectInertia(double width, double height, double mass, PVector COM) {
		return 1.0 / 12.0 * mass * (width * width + height * height);
	}

	public static double getPrimitiveInertia(Primitive p, double mass, PVector COM) {
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
			inertia += getPrimitiveInertia(ps.primitive, mass, PVector.sub(COM, ps.offset));
		}
		inertia /= p.getAssemblySize();
		return inertia;

	}
}
