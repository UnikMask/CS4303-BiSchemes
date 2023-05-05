package bischemes.engine;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
	private PVector cameraPosition = new PVector(0, 0);
	private PVector cameraBounds = new PVector();
	private float cameraRotation = 0;

	// Time Variables
	private boolean paused = true;
	private double deltaT = 0;
	private long lastTimeStamp = new Date().getTime();

	public void setPause(boolean pause) {
		if (this.paused != pause) {
			this.paused = pause;
			if (pause = false) {
				System.out.println("Unpaused!");
				lastTimeStamp = new Date().getTime();
			}
		}
	}

	public void draw() {
		PVector scale = new PVector(applet.width / cameraBounds.x, applet.height / cameraBounds.y);
		PVector posAnchored = new PVector(cameraPosition.x - cameraBounds.x / 2, cameraPosition.y + cameraBounds.y / 2);
		g.pushMatrix();
		g.rotate(-cameraRotation);
		g.scale(scale.x, -scale.y);
		g.translate(-posAnchored.x, -posAnchored.y);
		for (SceneGridPair scene : scenes) {
			scene.scene.draw(g);
		}
		g.popMatrix();
	}

	public void update() {
		long currentTime = new Date().getTime();
		deltaT = (double) (currentTime - lastTimeStamp) / 1000.0;
		lastTimeStamp = currentTime;

		if (paused) {
			draw();
			return;
		}

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

	public PVector getCameraPosition() {
		return this.cameraPosition;
	}

	public void setCameraBounds(PVector bounds) {
		this.cameraBounds = bounds;
	}

	public void setCameraRotation(float rotation) {
		this.cameraRotation = rotation;
	}

	public EngineRuntime(PApplet applet, PGraphics g) {
		EngineRuntime.applet = applet;
		this.g = g;
	}
}
