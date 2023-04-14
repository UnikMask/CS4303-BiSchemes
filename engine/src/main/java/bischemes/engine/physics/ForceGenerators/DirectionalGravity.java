package bischemes.engine.physics.ForceGenerators;

import bischemes.engine.physics.RigidBody;
import processing.core.PVector;

/**
 * Class representing a directional gravity force.
 */
public class DirectionalGravity implements ForceGenerator {
	private static final double GRAVITY_DEFAULT_COEFF = 9.81;
	private static final PVector GRAVITY_DEFAULT_DIRECTION = new PVector(0, -1f);
	double coeff;
	PVector direction;

	public void updateForce(RigidBody b) {
		b.addForce(PVector.mult(direction, (float) (b.getMass() * coeff)));
	}

	public DirectionalGravity() {
		this(GRAVITY_DEFAULT_COEFF, GRAVITY_DEFAULT_DIRECTION);
	}

	public DirectionalGravity(double coeff, PVector direction) {
		this.coeff = coeff;
		this.direction = direction;
	}

}
