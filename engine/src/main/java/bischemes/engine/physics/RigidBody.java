package bischemes.engine.physics;

import java.util.ArrayList;
import java.util.List;

import bischemes.engine.GObject;
import processing.core.PMatrix;
import processing.core.PMatrix2D;
import processing.core.PVector;

public class RigidBody {
	// Parent values - position, orientation
	private GObject parent;

	// Real Values
	double mass;
	double inertia;
	double damping;
	PVector velocity;
	double rotation;

	// Derived Values
	private PMatrix transformMatrix;
	private double inverseMass;
	private double inverseInertia;

	// Options
	private boolean hasMass;
	private boolean hasInertia;

	// Force Bookkeeping
	private List<PVector> forces = new ArrayList<>();
	private PVector forceAccumulation;
	// private double torqueAccumulation;

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public double getMass() {
		return mass;
	}

	/**
	 * Get the inverse mass of the rigid body, derived from its mass.
	 *
	 * @return The rigid body's inverse mass.
	 */
	public double getInverseMass() {
		return inverseMass;
	}

	/**
	 * Get the inverse inertia of the rigid body, derived from the rigid body's
	 * inertia.
	 *
	 * @return the rigid body's inverse inertia.
	 */
	public double getInverseInertia() {
		return inverseInertia;
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
	 */
	public void setPosition(PVector newPosition) {
		parent.setLocalPosition(newPosition);
	}

	/**
	 * Set the local orientation of the rigid body, in regards to its closeset
	 * parent.
	 */
	public void setOrientation(double newOrientation) {
		parent.setLocalOrientation(newOrientation);
	}

	/**
	 * Get the global orientation of the rigid body, in regards to world
	 * coordinates.
	 */
	public double getOrientation() {
		return parent.getOrientation();
	}

	/**
	 * Get the transform matrix derived from the rigid body and the game object.
	 */
	public PMatrix getTransformMatrix() {
		return transformMatrix;
	}

	////////////////////
	// public Methods //
	////////////////////

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
		// Apply forces to accumulator
		for (PVector f : forces) {
			forceAccumulation.add(f);
		}

		// Apply accumulator to velocity, and velocity to position
		velocity = PVector.add(velocity, PVector.mult(forceAccumulation, (float) duration));
		parent.setLocalPosition(PVector.add(parent.getLocalPosition(), PVector.mult(velocity, (float) duration)));
	}

	/**
	 * Add a force to the forces list of the object for the frame.
	 *
	 * @param force The force to add to the list.
	 */
	public void addForce(PVector force) {
		forces.add(force);
	}

	/////////////////////
	// Private methods //
	/////////////////////

	// Derivate derived values (mass, transform matrix) from
	// real values.
	private void derive() {
		inverseMass = (hasMass && mass != 0) ? 1 / mass : 0;
		inverseInertia = (hasInertia && inertia != 0) ? 1 / inertia : 0;

		// Derive transform matrix from position and orientation
		PVector pos = parent.getPosition();
		transformMatrix = new PMatrix2D(1, 0, 0, 0, 1, 0);
		transformMatrix.translate(pos.x, pos.y);
		transformMatrix.rotate((float) parent.getOrientation());
	}

}