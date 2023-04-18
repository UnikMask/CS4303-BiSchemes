package bischemes.engine.physics;

import processing.core.PVector;
import java.util.List;
import java.util.ArrayList;

public class Manifold {
	List<Interpenetration> contactPoints = new ArrayList<>();
	RigidBody objectA;
	RigidBody objectB;

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

	public void addContactPoint(PVector point, PVector normal, double penetration) {
		contactPoints.add(new Interpenetration(point, normal, penetration));
	}

	public void resolveCollision() {

	}

	public void combine(Manifold m) {
		if ((m.objectA != this.objectA || m.objectB != this.objectB)
				&& (m.objectA != this.objectB || m.objectB != this.objectA)) {
			return;
		}
		contactPoints.addAll(m.contactPoints);
	}

	public Manifold(RigidBody objectA, RigidBody objectB) {
		this.objectA = objectA;
		this.objectB = objectB;
	}
}
