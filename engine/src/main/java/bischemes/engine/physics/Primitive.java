package bischemes.engine.physics;

import java.util.List;

import processing.core.PVector;

public class Primitive implements PhysicsMesh {
	private PrimitiveType primitiveType;

	private List<PVector> vertices;
	private double radius;
	private PVector AABBBounds;

	static enum PrimitiveType {
		POLYGON, CIRCLE
	}

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public PrimitiveType getType() {
		return primitiveType;
	}

	//////////////////
	// Constructors //
	//////////////////

	public Primitive(List<PVector> vertices) {
		primitiveType = PrimitiveType.POLYGON;
		this.vertices = vertices;
	}

	public Primitive(double radius) {
		primitiveType = PrimitiveType.CIRCLE;
		this.radius = radius;
		this.AABBBounds = new PVector(2 * (float) radius, 2 * (float) radius);
	}
}
