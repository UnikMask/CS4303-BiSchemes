package bischemes.engine;

import java.util.List;

import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;

public class VisualAttribute {
	private List<PVector> vertices;
	private PVector offset = new PVector();
	private PShape shape;
	private VisualKind visualKind;
	private GObject obj;
	private int color;
	private PImage texture;
	private List<PVector> texCoords;

	enum VisualKind {
		TEXTURED, UNTEXTURED, TINTED_TEXTURED
	}

	public VisualKind getVisualKind() {
		return visualKind;
	}

	public void setOffset(PVector offset) {
		this.offset = offset;
	}

	private void loadShape() {
		if (shape != null) {
			return;
		}
		shape = EngineRuntime.applet.createShape();
		shape.beginShape();

		if (visualKind == VisualKind.UNTEXTURED) {
			for (PVector v : vertices) {
				shape.vertex(v.x, v.y);
			}
			shape.fill(color);
		} else {
			for (int i = 0; i < vertices.size(); i++) {
				shape.vertex(vertices.get(i).x, vertices.get(i).y, texCoords.get(i).x, texCoords.get(i).y);
			}
			if (visualKind == VisualKind.TINTED_TEXTURED)
				shape.tint(color);
			shape.texture(texture);
		}

		shape.endShape();
	}

	public void draw(PGraphics g) {
		loadShape();
		g.pushMatrix();
		PVector pos = obj.getPosition();
		g.translate(pos.x, pos.y);
		g.translate(offset.x, offset.y);
		g.shape(shape);
		g.popMatrix();
	}

	public void activate(GObject obj) {
		this.obj = obj;
	}

	public void makeUntextured(int colour) {
		this.visualKind = VisualKind.UNTEXTURED;
		this.color = colour;
		this.texture = null;
		this.texCoords = null;
		this.shape = null;
	}

	public void makeTextured(List<PVector> texCoords, String texture) {
		makeTextured(texCoords, EngineRuntime.applet.loadImage(texture));
	}

	public void makeTextured(List<PVector> texCoords, PImage texture) {
		this.visualKind = VisualKind.TEXTURED;
		this.texture = texture;
		this.texCoords = texCoords;
		this.shape = null;
	}

	public void makeTintedTexture(int colour, List<PVector> texCoords, PImage texture) {
		this.visualKind = VisualKind.TINTED_TEXTURED;
		this.color = colour;
		this.texture = texture;
		this.texCoords = texCoords;
		this.shape = null;
	}

	public void makeTintedTexture(int colour) {
		if (this.visualKind == VisualKind.UNTEXTURED)
			throw new IllegalStateException("Cannot set a TINTED_TEXTURE tint unless VisualAttribute is in TEXTURED or TINTED_TEXTURE state");
		this.color = colour;
		this.shape = null;
	}

	public void setColour(int colour) {
		if (this.visualKind == VisualKind.UNTEXTURED) makeUntextured(colour);
		else makeTintedTexture(colour);
	}

	public VisualAttribute(List<PVector> vertices, List<PVector> texCoords, String texturePath) {
		this.vertices = vertices;
		this.makeTextured(texCoords, texturePath);
	}

	// ch315 - Added a constructor which takes a PImage so that we that a loaded PImage texture can be
	//		   re-used and to try to reduce unnecessary calls of loadImage()
	public VisualAttribute(List<PVector> vertices, List<PVector> texCoords, PImage texture) {
		this.vertices = vertices;
		this.makeTextured(texCoords, texture);
	}


	public VisualAttribute(List<PVector> vertices) {
		this(vertices, 0x000000ff);
	}
	public VisualAttribute(List<PVector> vertices, int color) {
		this.vertices = vertices;
		this.makeUntextured(color);
	}
}
