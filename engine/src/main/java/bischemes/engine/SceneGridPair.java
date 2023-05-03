package bischemes.engine;

import java.util.ArrayDeque;
import java.util.HashSet;

import bischemes.engine.physics.GridSector;
import bischemes.engine.physics.RigidBody;

public class SceneGridPair {
	GObject scene;
	GridSector grid;
	HashSet<RigidBody> bodies;

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
