package bischemes.engine.physics;

import java.util.ArrayList;
import java.util.List;

import bischemes.engine.GObject;
import processing.core.PMatrix;
import processing.core.PMatrix2D;
import processing.core.PVector;

public class RigidBody {
	private PVector MOVE_THRESHOLD = new PVector(0.0001f, 0.0001f);

	// Parent values - position, orientation
	private GObject parent;

	// Real Values
	RigidBodyProperties properties;

	// Derived Values
	private PMatrix transformMatrix;
	private double inverseMass;
	private double inverseInertia;

	// Force Bookkeeping
	private List<PVector> forces = new ArrayList<>();
	private PVector forceAccumulation = new PVector();
	public boolean hasMoved = true;
	// private double torqueAccumulation;

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public GObject getParent() {
		return parent;
	}

	public double getMass() {
		return properties.mass;
	}

	public RigidBodyProperties getProperties() {
		return properties;
	}

	/**
	 * Get the inverse mass of the rigid body, derived from its mass.
	 *
	 * @return The rigid body's inverse mass.
	 */
	public double getInverseMass() {
		return properties.isMovable ? inverseMass : 0;
	}

	/**
	 * Get the inverse inertia of the rigid body, derived from the rigid body's
	 * inertia.
	 *
	 * @return the rigid body's inverse inertia.
	 */
	public double getInverseInertia() {
		return properties.isRotatable ? inverseInertia : 0;
	}

	/**
	 * Get the global position of the rigid body, relative to world coordinates
	 *
	 * @return The global position of the rigid body.
	 */
	public PVector getPosition() {
		return parent.getPosition();
	}

	/**
	 * Set the local position of the rigid body, in regards to its closest parent.
	 *
	 * @param newPosition The new local position of the rigid body.
	 */
	public void setPosition(PVector newPosition) {
		parent.setLocalPosition(newPosition);
	}

	/**
	 * Set the local orientation of the rigid body, in regards to its closeset
	 * parent.
	 *
	 * @param newOrientation The new local orientation of the rigid body.
	 */
	public void setOrientation(double newOrientation) {
		parent.setLocalOrientation(newOrientation);
	}

	/**
	 * Get the global orientation of the rigid body, in regards to world
	 * coordinates.
	 *
	 * @return the global orientation of the rigid body.
	 */
	public double getOrientation() {
		return parent.getOrientation();
	}

	/**
	 * Get the transform matrix derived from the rigid body and the game object.
	 *
	 * @return The transform matrix of the rigid body.
	 */
	public PMatrix getTransformMatrix() {
		return transformMatrix;
	}

	////////////////////
	// public Methods //
	////////////////////

	public void enable(GObject parent) {
		if (this.parent == null) {
			this.parent = parent;
			derive();
		}
	}

	/**
	 * Initialise a game update for a rigid body by resetting accumulators and force
	 * lists, and deriving derived values.
	 */
	public void initUpdate() {
		forces = new ArrayList<>();
		forceAccumulation = new PVector();
		// torqueAccumulation = 0;
		derive();
	}

	/**
	 * Integrate all forces into the velocity and position of the rigid body,
	 * following d'Alembert's principle.
	 *
	 * @param duration The duration of the frame.
	 */
	public void integrate(double duration) {
		if (!properties.isMovable) {
			return;
		}
		hasMoved = false;

		// Apply forces to accumulator
		for (PVector f : forces) {
			forceAccumulation.add(f);
		}

		// Apply accumulator to velocity, velocity to position, and rotation to orientation
		properties.velocity = PVector.add(properties.velocity, PVector.mult(forceAccumulation, (float) duration));
		PVector pos = parent.getPosition().copy();
		parent.setLocalPosition(
				PVector.add(parent.getLocalPosition(), PVector.mult(properties.velocity, (float) duration)));
		parent.setLocalOrientation(parent.getLocalOrientation() + properties.rotation * duration);
		pos.sub(parent.getPosition());
		if (Math.abs(properties.velocity.x) > MOVE_THRESHOLD.x || Math.abs(properties.velocity.y) > MOVE_THRESHOLD.y) {
			hasMoved = true;
		}
	}

	/**
	 * Add a force to the forces list of the object for the frame.
	 *
	 * @param force The force to add to the list.
	 */
	public void addForce(PVector force) {
		forces.add(force);
	}

	public void applyImpulse(PVector impulse, PVector applicationPoint) {
		if (properties.isMovable) {
			properties.velocity = PVector.add(properties.velocity, PVector.mult(impulse, (float) getInverseMass()));
		}
		if (properties.isRotatable) {
			properties.rotation += inverseInertia * applicationPoint.cross(impulse).z;
		}
	}

	/////////////////////
	// Private methods //
	/////////////////////

	// Derivate derived values (mass, transform matrix) from
	// real values.
	private void derive() {
		inverseMass = (properties.isMovable && properties.mass != 0) ? (1 / properties.mass) : 0;
		inverseInertia = (properties.isRotatable && properties.inertia != 0) ? (1 / properties.inertia) : 0;

		// Derive transform matrix from position and orientation
		PVector pos = parent.getPosition();
		transformMatrix = new PMatrix2D(1, 0, 0, 0, 1, 0);
		transformMatrix.translate(pos.x, pos.y);
		transformMatrix.rotate((float) parent.getOrientation());

		// Derive mesh if it exists
		if (properties.mesh != null) {
			properties.mesh.derive();
		}
	}

	//////////////////
	// Constructors //
	//////////////////

	/**
	 * Constructor for a rigid body.
	 *
	 * @param properties The rigid body's property structure.
	 */
	public RigidBody(RigidBodyProperties properties) {
		this.properties = properties;

		if (properties.mesh != null) {
			properties.mesh.enable(this);
		}
	}
}
