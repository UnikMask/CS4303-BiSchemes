package bischemes.engine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;

import bischemes.engine.physics.*;
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
		Manifold m = rbA.getProperties().mesh.getCollision(rbB.getProperties().mesh);
		assertNotNull(m);
		assertFalse(m.isCollision());

		// Case 2 - collision
		bA.setLocalPosition(new PVector(0.1f, 0));
		m = rbA.getProperties().mesh.getCollision(rbB.getProperties().mesh);
		assertTrue(m.isCollision());
	}

	@Test
	public void testCircleToPolygonCollision() {
		// Set objects
		GObject bA = new GObject(null, new PVector(-2, 0), 0);
		GObject bB = new GObject(null, new PVector(2, 0), 0);
		bA.setRigidBody(new RigidBody(new RigidBodyProperties(Map.of("mesh", cube.copy()))));
		bB.setRigidBody(new RigidBody(new RigidBodyProperties(Map.of("mesh", circle.copy()))));

		// Case 1 - no collisions
		PhysicsMesh mA = bA.getRigidBody().getProperties().mesh;
		PhysicsMesh mB = bB.getRigidBody().getProperties().mesh;
		Manifold m = mA.getCollision(mB);
		assertFalse(m.isCollision());

		// Case 2 - Direct Collision
		bA.setLocalPosition(new PVector(0.51f, 0));
		m = mA.getCollision(mB);
		assertTrue(m.isCollision());

		// Case 3 - Corner Collision
		bA.setLocalPosition(new PVector(1, -0.5f));
		m = mA.getCollision(mB);
		assertTrue(m.isCollision());
	}

	@Test
	public void testPolygonToPolygonCollision() {
		// Set objects
		GObject bA = new GObject(null, new PVector(-2, 0), 0);
		GObject bB = new GObject(null, new PVector(2, 0), 0);
		bA.setRigidBody(new RigidBody(new RigidBodyProperties(Map.of("mesh", cube.copy()))));
		bB.setRigidBody(new RigidBody(new RigidBodyProperties(Map.of("mesh", cube.copy()))));

		// Case 1 - no collisions
		PhysicsMesh mA = bA.getRigidBody().getProperties().mesh;
		PhysicsMesh mB = bB.getRigidBody().getProperties().mesh;
		Manifold m = mA.getCollision(mB);
		assertFalse(m.isCollision());

		// Case 2 - no collision, very close
		bA.setLocalPosition(new PVector(0.95f, 0));
		m = mA.getCollision(mB);
		assertFalse(m.isCollision());

		// Case 2 - collision, with orientation, edge on A
		bA.setLocalOrientation(Math.PI / 4);
		mA.derive();
		m = mA.getCollision(mB);
		assertTrue(m.isCollision());

		// Case 3 - collision, with orientation, edge on B
		bA.setLocalOrientation(0);
		bB.setLocalOrientation(Math.PI / 4);
		mA.derive();
		mB.derive();
		m = mA.getCollision(mB);
		assertTrue(m.isCollision());
	}

	@Test
	public void testGridSector() {
		GridSector g = new GridSector(new PVector(16, 9), new PVector(-8, -4.5f), 16, 9);
		GObject bA = new GObject(null, new PVector(-2, 0), 0);
		GObject bB = new GObject(null, new PVector(2, 0), 0);
		bA.setRigidBody(new RigidBody(new RigidBodyProperties(Map.of("mesh", cube.copy()))));
		bB.setRigidBody(new RigidBody(new RigidBodyProperties(Map.of("mesh", cube.copy()))));

		// Case 1 - no overlap
		g.move(bA.getRigidBody());
		g.move(bB.getRigidBody());
		assertTrue(g.getCollisions().isEmpty());

		// Case 2 - overlap
		bA.setLocalPosition(new PVector(1.01f, 0));
		g.move(bA.getRigidBody());
		assertFalse(g.getCollisions().isEmpty());
	}
}
