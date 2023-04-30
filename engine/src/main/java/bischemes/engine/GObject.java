package bischemes.engine;

import java.util.ArrayList;
import java.util.List;

import bischemes.engine.physics.RigidBody;
import processing.core.PGraphics;
import processing.core.PVector;

public class GObject {
	protected GObject parent;
	protected List<GObject> children = new ArrayList<>();
	protected PVector position;
	protected double orientation;
	protected List<VisualAttribute> visualAttributes = new ArrayList<>();

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

	public void setLocalPosition(PVector newLocalPosition) {
		this.position = newLocalPosition;
	}

	public PVector getLocalPosition() {
		return position;
	}

	public double getOrientation() {
		GObject currentObject = this;
		float globalRotation = 0;
		while (currentObject.parent != null) {
			globalRotation += currentObject.orientation;
			currentObject = currentObject.parent;
		}
		return globalRotation;
	}

	public void setLocalOrientation(double orientation) {
		this.orientation = orientation;
	}

	public double getLocalOrientation() {
		return orientation;
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

	// recursively set the colour of this GObject's and its children's visualAttributes.
	// TEXTURED visualAttributes become TINTED_TEXTURED
	public void setColour(int colour) {
		for (VisualAttribute visAttr : visualAttributes)
			visAttr.setColour(colour);
		for (GObject child : children)
			child.setColour(colour);
	}


	public GObject(GObject parent, PVector position, float rotation) {
		this.position = position;
		this.orientation = rotation;
		if (parent != null)
			parent.addChild(this);
	}

	public void addVisualAttributes(VisualAttribute... attributes) {
		for (VisualAttribute a : attributes) {
			a.activate(this);
			visualAttributes.add(a);
		}
	}

	public VisualAttribute getVisualAttribute(int index) {
		return visualAttributes.get(index);
	}

	public void removeVisualAttributes(int... indices) {
		for (int i : indices) {
			visualAttributes.remove(i);
		}
	}

	/////////////////////
	// Private Methods //
	/////////////////////

	private void addChild(GObject child) {
		child.parent = this;
		children.add(child);
	}

}
