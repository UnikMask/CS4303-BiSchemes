package bischemes.engine;

import java.util.HashSet;
import java.util.Set;

import bischemes.engine.physics.RigidBody;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class EngineRuntime {
	public static PApplet applet;
	private PGraphics g;
	private Set<GObject> scenes = new HashSet<>();
	private HashSet<RigidBody> bodies = new HashSet<>();

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
		for (GObject scene : scenes) {
			scene.draw(g);
		}
		g.popMatrix();
	}

	public void update() {
		// TODO update loop
	}

	public void attachScene(GObject scene) {
		scenes.add(scene);
	}

	public void removeScene(GObject scene) {
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
