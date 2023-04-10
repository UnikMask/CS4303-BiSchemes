package bischemes.engine.physics;

import bischemes.engine.GObject;
import processing.core.PMatrix;
import processing.core.PVector;

public class RigidBody {
	// Parent values - position, orientation
	private GObject parent;

	// Real Values
	float mass;
	float damping;
	PVector velocity;
	float rotation;

	// Derived Values
	private PMatrix transformMatrix;
	private float inverseMass;
	private boolean hasMass;

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	/**
	 * Get the inverse mass, derived from the rigid body.
	 */
	public float getInverseMass() {
		return inverseMass;
	}

	/**
	 * Get the transform matrix derived from the rigid body and the game object.
	 */
	public PMatrix getTransformMatrix() {
		return transformMatrix;
	}

	// Derivate derived values (mass, transform matrix) from
	// real values.
	private void derivate() {
		if (hasMass) {
			inverseMass = 1 / mass;
		} else {
			inverseMass = 0;
		}
	}

}
