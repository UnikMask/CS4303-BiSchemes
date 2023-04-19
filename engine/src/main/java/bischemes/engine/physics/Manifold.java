package bischemes.engine.physics;

import processing.core.PVector;
import java.util.List;
import java.util.ArrayList;

/**
 * Details on a collision between 2 rigid bodies.
 */
public class Manifold {
	private List<Interpenetration> contactPoints = new ArrayList<>();
	private RigidBody objectA;
	private RigidBody objectB;

	/**
	 * Representation of an interpenetration of a point from one rigid body into the
	 * other.
	 */
	class Interpenetration {
		PVector contactPoint;
		PVector surfaceNormal;
		double penetration;

		Interpenetration(PVector contactPoint, PVector surfaceNormal, double penetration) {
			this.contactPoint = contactPoint;
			this.surfaceNormal = surfaceNormal;
			this.penetration = penetration;
		}
	}

	/**
	 * Add a new contact point to the manifold.
	 *
	 * @param point       The contact point of the manifold.
	 * @param normal      The normal of the collision surface.
	 * @param penetration The penetration depth of the collision.
	 */
	public void addContactPoint(PVector point, PVector normal, double penetration) {
		contactPoints.add(new Interpenetration(point, normal, penetration));
	}

	/**
	 * Resolve the collsion described by the manifold by applying impulses to the
	 * rigid bodies. THE MANIFOLD MUST NOT BE USED AGAIN AFTER THIS.
	 */
	public void resolveCollision() {

	}

	/**
	 * TODO Finish the impulse resolution algorithm Compute the impulse coefficient
	 * of the manifold on the 2 rigid bodies.
	 *
	 * @return The impulse factor.
	 */
	public PVector computeImpulse() {
		for (Interpenetration contact : contactPoints) {
			PVector radA = PVector.sub(contact.contactPoint, objectA.getPosition());
			PVector radB = PVector.sub(contact.contactPoint, objectB.getPosition());

			PVector relVelocity = PVector.sub(objectB.properties.velocity, objectA.properties.velocity)
					.sub(new PVector(-radA.y, radA.x).mult((float) objectA.properties.rotation))
					.add(new PVector(-radB.y, radB.x).mult((float) objectB.properties.rotation));

			float velocityProjectionOnNormal = PVector.dot(relVelocity, contact.surfaceNormal);
			if (velocityProjectionOnNormal > 0) {
				return new PVector();
			}
		}
		return null;
	}

	/**
	 * Combine 2 manifolds into a same manifold. Deals with reversed parents cases.
	 * This method silently fails if the given manifold does not share the same
	 * parents as the current one.
	 *
	 * @param m The manifold to combine to the current on.
	 */
	public void combine(Manifold m) {
		if ((m.objectA != this.objectA || m.objectB != this.objectB)
				&& (m.objectA != this.objectB || m.objectB != this.objectA)) {
			return;
		}
		contactPoints.addAll(m.contactPoints);
	}

	/**
	 * Getter to whether the manifold has detected a collision between both rigid
	 * bodies or not.
	 *
	 * @return Whether the 2 rigid bodies of the manifold are collided.
	 */
	public boolean isCollision() {
		return !contactPoints.isEmpty();
	}

	/**
	 * Constructor for a manifold.
	 */
	public Manifold(RigidBody objectA, RigidBody objectB) {
		this.objectA = objectA;
		this.objectB = objectB;
	}
}
