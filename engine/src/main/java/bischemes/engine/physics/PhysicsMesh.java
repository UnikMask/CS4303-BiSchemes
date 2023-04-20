package bischemes.engine.physics;

import java.util.List;

import bischemes.engine.physics.SpatialPartition.PrimitiveStore;
import processing.core.PVector;

public interface PhysicsMesh {
	/**
	 * Getter for the mesh's parent rigid body.
	 *
	 * @return The mesh's rigid body parent.
	 */
	RigidBody getParent();

	/**
	 * Check for collision with a single other primitive.
	 *
	 * @param b The primitive to check against for collisions.
	 * @return The contact manifold between this mesh and b.
	 */
	Manifold getCollision(Primitive b, PVector offset);

	/**
	 * Check for collision with an assembly of multiple primitives.
	 *
	 * @param b The primitive assembly to check against for collisions.
	 * @return The contact manifold between this primitive and b.
	 */
	Manifold getCollision(PrimitiveAssembly b, PVector offset);

	/**
	 * Check for collision with another physics mesh. On the backend, branches out
	 * respective of the other mesh's instance.
	 *
	 * @param b The physics mesh to check for collisions against.
	 *
	 * @return The manifold between this mesh and the other mesh. Returns null if b
	 *         is neither a primitive nor a primitive assembly.
	 */
	Manifold getCollision(PhysicsMesh b);

	List<PrimitiveStore> getPrimitiveStores();
}
