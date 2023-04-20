package bischemes.engine.physics;

import java.util.ArrayList;
import java.util.List;

import bischemes.engine.physics.SpatialPartition.PrimitiveStore;
import processing.core.PVector;

/**
 * Assembly of primitives that make up a more complex mesh.
 */
public class PrimitiveAssembly implements PhysicsMesh {
	private RigidBody parent;
	private List<PrimitiveInSet> assembly = new ArrayList<>();

	class PrimitiveInSet {
		Primitive primitive;
		PVector offset;

		PrimitiveInSet(Primitive primitive, PVector offset) {
			this.primitive = primitive;
			this.offset = offset;
		}
	}

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public Iterable<PrimitiveInSet> getAssembly() {
		return assembly;
	}

	public RigidBody getParent() {
		return parent;
	}

	////////////////////
	// Public Methods //
	////////////////////

	public void addPrimitive(Primitive primitive, PVector offset) {
		assembly.add(new PrimitiveInSet(primitive, offset));
	}

	public Manifold getCollision(PhysicsMesh b) {
		if (b instanceof PrimitiveAssembly) {
			return getCollision((PrimitiveAssembly) b, new PVector());
		} else if (b instanceof Primitive) {
			return getCollision((Primitive) b, new PVector());
		} else {
			return null;
		}
	}

	public Manifold getCollision(PrimitiveAssembly b, PVector offset) {
		Manifold m = new Manifold(this.parent, b.getParent());
		for (PrimitiveInSet p : assembly) {
			m.combine(p.primitive.getCollision(b, p.offset));
		}
		return m;
	}

	public Manifold getCollision(Primitive b, PVector offset) {
		Manifold m = new Manifold(this.parent, b.getParent());
		for (PrimitiveInSet p : assembly) {
			m.combine(p.primitive.getCollision(b, PVector.mult(p.offset, -1)));
		}
		return m;
	}

	public List<PrimitiveStore> getPrimitiveStores() {
		return assembly.stream().map((pInSet) -> {
			PVector transformedOffset = pInSet.offset.copy();
			transformedOffset.rotate((float) parent.getOrientation());
			return new PrimitiveStore(pInSet.primitive, pInSet.offset);
		}).toList();
	}

	//////////////////
	// Constructors //
	//////////////////

	public PrimitiveAssembly(RigidBody parent) {
		this.parent = parent;
	}
}
