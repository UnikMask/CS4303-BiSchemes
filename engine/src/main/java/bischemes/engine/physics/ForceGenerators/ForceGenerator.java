package bischemes.engine.physics.ForceGenerators;

import bischemes.engine.physics.RigidBody;

public interface ForceGenerator {
	public void updateForce(RigidBody b);
}
