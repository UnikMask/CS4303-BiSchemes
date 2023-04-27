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

		double restitution;
		double staticFriction;
		double dynamicFriction;

		Interpenetration(PVector contactPoint, PVector surfaceNormal, double penetration, Primitive a, Primitive b) {
			this.contactPoint = contactPoint;
			this.surfaceNormal = surfaceNormal;
			this.penetration = penetration;

			// Calculate restitution, static and dynamic friction
			this.restitution = (b.surface.restitution + a.surface.restitution) / 2;
			this.staticFriction = Math
					.sqrt(Math.pow(a.surface.staticFriction, 2) + Math.pow(b.surface.staticFriction, 2));
			this.dynamicFriction = Math
					.sqrt(Math.pow(a.surface.dynamicFriction, 2) + Math.pow(b.surface.dynamicFriction, 2));
		}
	}

	/**
	 * Add a new contact point to the manifold.
	 *
	 * @param point       The contact point of the manifold.
	 * @param normal      The normal of the collision surface.
	 * @param penetration The penetration depth of the collision.
	 */
	public void addContactPoint(PVector point, PVector normal, double penetration, Primitive a, Primitive b) {
		contactPoints.add(new Interpenetration(point, normal, penetration, a, b));
	}

	/**
	 * Apply the impulse of the manifold to the two rigid bodies.
	 */
	public void applyImpulse() {
		for (Interpenetration contact : contactPoints) {
			PVector radA = PVector.sub(contact.contactPoint, objectA.getPosition());
			PVector radB = PVector.sub(contact.contactPoint, objectB.getPosition());

			PVector relVelocity = PVector.sub(objectB.properties.velocity, objectA.properties.velocity)
					.sub(new PVector(-radA.y, radA.x).mult((float) objectA.properties.rotation))
					.add(new PVector(-radB.y, radB.x).mult((float) objectB.properties.rotation));

			double velocityProjectionOnNormal = PVector.dot(relVelocity, contact.surfaceNormal);
			if (velocityProjectionOnNormal > 0) {
				continue;
			}

			// Calculate impulse resolution
			double radACrossNormal = Math.pow(radA.cross(contact.surfaceNormal).z, 2),
					radBCrossNormal = Math.pow(radB.cross(contact.surfaceNormal).z, 2);
			double factorDiv = 1 / (contactPoints.size() * (objectA.getInverseMass() + objectB.getInverseMass()
					+ radACrossNormal * objectA.getInverseInertia() + radBCrossNormal * objectB.getInverseInertia()));
			double j = -(1 + contact.restitution) * velocityProjectionOnNormal * factorDiv;
			PVector impulse = PVector.mult(contact.surfaceNormal, (float) j);

			objectA.applyImpulse(PVector.mult(impulse, -1), contact.contactPoint);
			objectB.applyImpulse(impulse, contact.contactPoint);

			// Calculate friction resolution
			PVector tan = PVector
					.sub(relVelocity,
							PVector.mult(contact.surfaceNormal, PVector.dot(relVelocity, contact.surfaceNormal)))
					.normalize();
			double velocityProjectionOnTan = PVector.dot(relVelocity, tan);
			double frictionFactor = -velocityProjectionOnTan * factorDiv;
			PVector frictionImpulse = PVector.mult(tan,
					(float) (Math.abs(frictionFactor) > contact.staticFriction * j ? -j * contact.dynamicFriction
							: frictionFactor));

			objectA.applyImpulse(PVector.mult(frictionImpulse, -1), contact.contactPoint);
			objectB.applyImpulse(frictionImpulse, contact.contactPoint);
		}
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
