package bischemes.engine.physics;

import processing.core.PVector;

public interface PhysicsMesh {
	RigidBody getParent();

	Manifold getCollision(Primitive b, PVector offset);

	Manifold getCollision(PrimitiveAssembly b, PVector offset);

	Manifold getCollision(PhysicsMesh b);
}
