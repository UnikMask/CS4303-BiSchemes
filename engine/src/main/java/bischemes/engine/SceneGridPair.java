package bischemes.engine;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.HashSet;

import bischemes.engine.physics.GridSector;
import bischemes.engine.physics.RigidBody;

public class SceneGridPair {
	public GObject scene;
	public GridSector grid;
	public HashSet<RigidBody> bodies;

	public void attachToGObject(GObject object, GObject child) {
		object.children.add(child);
		child.parent = object;

		ArrayDeque<GObject> q = new ArrayDeque<>(Arrays.asList(child));
		while (!q.isEmpty()) {
			GObject current = q.pollFirst();
			if (current.getRigidBody() != null) {
				bodies.add(current.getRigidBody());
			} else {
				q.addAll(current.children);
			}
		}
	}

	public SceneGridPair(GObject scene, GridSector grid) {
		this.scene = scene;
		this.grid = grid;

		bodies = new HashSet<>();
		ArrayDeque<GObject> q = new ArrayDeque<>();
		while (!q.isEmpty()) {
			GObject current = q.pollFirst();
			if (current.getRigidBody() != null) {
				bodies.add(current.getRigidBody());
			} else {
				q.addAll(current.children);
			}
		}
	}
}
