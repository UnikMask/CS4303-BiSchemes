package bischemes.engine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;

import bischemes.engine.physics.Manifold;
import bischemes.engine.physics.Primitive;
import bischemes.engine.physics.RigidBody;
import bischemes.engine.physics.RigidBodyProperties;
import bischemes.engine.physics.Surface;
import processing.core.PVector;

class CollisionTests {
	Primitive circle = new Primitive(new Surface(0, 0, 0), 1);
	Primitive cube = new Primitive(new Surface(0, 0, 0), Arrays.asList(new PVector(-0.5f, -0.5f),
			new PVector(-0.5f, 0.5f), new PVector(0.5f, 0.5f), new PVector(0.5f, -0.5f)));

	@Test
	public void testCircleToCircleCollision() {
		GObject bA = new GObject(null, new PVector(-2, 0), 0);
		bA.setRigidBody(new RigidBody(new RigidBodyProperties(Map.of("mesh", circle.copy()))));
		GObject bB = new GObject(null, new PVector(2, 0), 0);
		bB.setRigidBody(new RigidBody(new RigidBodyProperties(Map.of("mesh", circle.copy()))));

		RigidBody rbA = bA.getRigidBody();
		RigidBody rbB = bB.getRigidBody();

		// Case 0 - Check correct RigidBody creation
		assertNotNull(rbA.getProperties());
		assertNotNull(rbB.getProperties());
		assertNotNull(rbA.getParent());
		assertNotNull(rbB.getParent());
		assertNotNull(rbA.getProperties().mesh);
		assertNotNull(rbB.getProperties().mesh);

		// Case 1 - no collisions
		System.out.println(bA.getPosition());
		System.out.println(bB.getPosition());
		Manifold m = rbA.getProperties().mesh.getCollision(rbB.getProperties().mesh);
		assertNotNull(m);
		assertFalse(m.isCollision());
	}
}
