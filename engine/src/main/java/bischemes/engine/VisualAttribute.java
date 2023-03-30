package bischemes.engine;

import java.util.List;

import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PMatrix;
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
		TEXTURED, UNTEXTURED
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
		if (visualKind == VisualKind.TEXTURED) {
			for (int i = 0; i < vertices.size(); i++) {
				shape.vertex(vertices.get(i).x, vertices.get(i).y, texCoords.get(i).x, texCoords.get(i).y);
			}
			shape.texture(texture);
		} else {
			for (PVector v : vertices) {
				shape.vertex(v.x, v.y);
			}
			shape.fill(color);
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

	public VisualAttribute(List<PVector> vertices, List<PVector> texCoords, String texturePath) {
		visualKind = VisualKind.TEXTURED;
		this.vertices = vertices;
		texture = EngineRuntime.applet.loadImage(texturePath);
	}

	public VisualAttribute(List<PVector> vertices, int color) {
		visualKind = VisualKind.UNTEXTURED;
		this.vertices = vertices;
		this.color = color;
	}
}
