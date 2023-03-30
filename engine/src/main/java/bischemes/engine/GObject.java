package bischemes.engine;

import java.util.ArrayList;
import java.util.List;

import processing.core.PGraphics;
import processing.core.PVector;

public class GObject {
	private GObject parent;
	private List<GObject> children = new ArrayList<>();
	private PVector position;
	private float rotation;
	private List<VisualAttribute> visualAttributes = new ArrayList<>();

	/////////////////////////
	// Getters And Setters //
	/////////////////////////

	public PVector getPosition() {
		PVector globalPosition = new PVector();
		GObject currentObject = this;
		while (currentObject.parent != null) {
			globalPosition.add(currentObject.position);
			currentObject = currentObject.parent;
		}
		return globalPosition;
	}

	public float getRotation() {
		GObject currentObject = this;
		float globalRotation = 0;
		while (currentObject.parent != null) {
			globalRotation += currentObject.rotation;
			currentObject = currentObject.parent;
		}
		return globalRotation;
	}

	public PVector getLocalPosition() {
		return position;
	}

	public float getLocalRotation() {
		return rotation;
	}

	////////////////////
	// Public Methods //
	////////////////////

	public void draw(PGraphics g) {
		for (VisualAttribute a : visualAttributes) {
			a.draw(g);
		}
		for (GObject child : children) {
			child.draw(g);
		}
	}

	public GObject(GObject parent, PVector position, float rotation) {
		this.position = position;
		this.rotation = rotation;
		parent.addChild(this);
	}

	/////////////////////
	// Private Methods //
	/////////////////////

	private void addChild(GObject child) {
		child.parent = this;
		children.add(child);
	}

	public void addVisualAttributes(VisualAttribute... attributes) {
		for (VisualAttribute a : attributes) {
			a.activate(this);
			visualAttributes.add(a);
		}
	}

}
