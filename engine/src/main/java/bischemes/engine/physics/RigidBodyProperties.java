package bischemes.engine.physics;

import java.util.Map;

import processing.core.PVector;

/**
 * Struct containing the real properties of a rigid body.
 */
public class RigidBodyProperties {
	public double mass;
	public double inertia;
	public double damping;
	public PVector velocity;
	public double rotation;

	public boolean isMovable = false;
	public boolean isRotatable = false;

	public PhysicsMesh mesh;

	/**
	 * parse a given property and set its value on the properties.
	 *
	 * @param prop  The property's name.
	 * @param value The property's new value.
	 */
	public void parseProperty(String prop, Object value) {
		switch (prop) {
		case "mass" -> mass = (double) value;
		case "inertia" -> inertia = (double) value;
		case "damping" -> damping = (double) value;
		case "velocity" -> velocity = (PVector) value;
		case "rotation" -> rotation = (double) value;
		case "move" -> isMovable = (boolean) value;
		case "rotate" -> isRotatable = (boolean) value;
		case "mesh" -> mesh = (PhysicsMesh) value;
		}
	}

	/**
	 * Constructor for a new rigid body property struct.
	 *
	 * @param properties The properties map of the rigid body.
	 */
	public RigidBodyProperties(Map<String, Object> properties) {
		for (var entry : properties.entrySet()) {
			parseProperty(entry.getKey(), entry.getValue());
		}
	}
}
