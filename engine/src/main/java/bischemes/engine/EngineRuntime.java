package bischemes.engine;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import bischemes.engine.physics.Manifold;
import bischemes.engine.physics.RigidBody;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class EngineRuntime {
	public static PApplet applet;
	private PGraphics g;
	private Set<SceneGridPair> scenes = new HashSet<>();

	// Camera Variables
	private PVector cameraPosition = new PVector(16, 9);
	private PVector cameraBounds = new PVector();
	private float cameraRotation = 0;

	// Time Variables
	private boolean paused = true;
	private long deltaT = 0;

	public void setPause(boolean pause) {
		if (this.paused != pause) {
			deltaT = 0;
			this.paused = pause;
		}
	}

	public void draw() {
		g.pushMatrix();
		PVector scale = new PVector(((float) applet.width) / cameraBounds.x, ((float) applet.height) / cameraBounds.y);
		PVector posAnchored = PVector.sub(cameraPosition, PVector.div(cameraBounds, 2));
		g.rotate(-cameraRotation);
		g.scale(scale.x, scale.y);
		g.translate(-posAnchored.x, -posAnchored.y);
		for (SceneGridPair scene : scenes) {
			scene.scene.draw(g);
		}
		g.popMatrix();
	}

	public void update() {
		// 1. GObject per-frame Update
		for (SceneGridPair s : scenes) {
			ArrayDeque<GObject> q = new ArrayDeque<>(Arrays.asList(s.scene));
			while (!q.isEmpty()) {
				GObject current = q.pollFirst();
				current.update();
				q.addAll(current.children);
			}

			// 2. Movement Integration
			for (RigidBody rb : s.bodies) {
				rb.integrate(deltaT);
				if (rb.hasMoved) {
					s.grid.move(rb);
				}
				rb.initUpdate();
			}

			// 4. Collision Resolution
			HashMap<Pair<RigidBody>, Manifold> collisions = s.grid.getCollisions();
			for (var entry : collisions.entrySet()) {
				entry.getValue().applyImpulse();
			}

			// 5. Collision Events
			for (Pair<RigidBody> rp : collisions.keySet()) {
				rp.a.getParent().onHit(rp.b.getParent());
				rp.b.getParent().onHit(rp.a.getParent());
			}
		}

		// 6. Draw
		draw();
	}

	public void attachScene(SceneGridPair scene) {
		scenes.add(scene);
	}

	public void removeScene(SceneGridPair scene) {
		scenes.remove(scene);
	}

	public void setCameraPosition(PVector position) {
		this.cameraPosition = position;
	}

	public void setCameraBounds(PVector bounds) {
		this.cameraBounds = bounds;
	}

	public void setCameraRotation(float rotation) {
		this.cameraRotation = rotation;
	}

	public EngineRuntime(PApplet applet, PGraphics g) {
		if (EngineRuntime.applet != null) {
			EngineRuntime.applet = applet;
		}
		this.g = g;
	}
}
