package bischemes.engine.physics;

import java.util.ArrayList;
import java.util.List;

import processing.core.PVector;

/**
 * Assembly of primitives that make up a more complex mesh.
 */
public class PrimitiveAssembly implements PhysicsMesh {
	private RigidBody parent;
	private List<PrimitiveInSet> assembly = new ArrayList<>();
	private List<PrimitiveInSet> baseAssembly = new ArrayList<>();

	class PrimitiveInSet {
		Primitive primitive;
		PVector offset;

		PrimitiveInSet copyDerived() {
			primitive.derive();
			PVector noffset = offset.copy();
			noffset.rotate((float) getParent().getOrientation());
			return new PrimitiveInSet(primitive, noffset);
		}

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

	public void derive() {
		assembly = new ArrayList<>(baseAssembly.size());
		for (PrimitiveInSet p : baseAssembly) {
			assembly.add(p.copyDerived());
		}
	}

	public void addPrimitive(Primitive primitive, PVector offset) {
		baseAssembly.add(new PrimitiveInSet(primitive, offset));
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

	//////////////////
	// Constructors //
	//////////////////

	public PrimitiveAssembly(RigidBody parent) {
		this.parent = parent;
	}
}
