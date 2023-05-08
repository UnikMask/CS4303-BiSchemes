package bischemes.game;

import bischemes.engine.physics.RigidBody;
import bischemes.engine.physics.ForceGenerators.ForceGenerator;
import processing.core.PVector;

public class PlayerMovement implements ForceGenerator {
	private static final double BASE_MOVEMENT_INTENSITY = 3;
	private static final double MAX_PROJECTED_VELOCITY = 12.0;

	Player p;
	PVector t;
	boolean isRight;

	public void updateForce(RigidBody b) {
		// Set final vector
		double projectedVelocity = PVector.dot(p.getRigidBody().getProperties().velocity, t);
		PVector fmb = PVector.mult(t, (float) (BASE_MOVEMENT_INTENSITY * p.getRigidBody().getMass()
				* (1 - MAX_PROJECTED_VELOCITY + Math.abs(projectedVelocity))));
		b.addForce(PVector.mult(fmb, isRight ? -1 : 1));
	}

	public void setDirection(boolean isRight) {
		this.isRight = isRight;
	}

	public PlayerMovement(Player p, PVector gravityTangent) {
		this.p = p;
		t = gravityTangent;
	}
}
