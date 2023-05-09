package bischemes.engine;

import java.util.List;

import processing.core.PConstants;
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
	private int color = 0xffffffff;
	private PImage texture;
	private PVector size;
	private List<PVector> texCoords;
	public boolean visibilityPriority = false;
	public boolean mirrorX = false;
	public boolean mirrorY = false;
	public boolean visible = true;

	private PVector scaling = new PVector(1, 1);

	enum VisualKind {
		TEXTURED, UNTEXTURED, TINTED_TEXTURED
	}

	////////////////////
	// Public Methods //
	////////////////////

	public VisualKind getVisualKind() {
		return visualKind;
	}

	public void setOffset(PVector offset) {
		this.offset = offset;
	}

	public void draw(PGraphics g) {
		if (!visible) {
			return;
		} else if (shape == null) {
			loadShape();
		}
		g.pushMatrix();
		PVector pos = obj.getPosition();
		g.translate(pos.x, pos.y);
		g.translate(offset.x, offset.y);
		g.rotate((float) obj.getOrientation());
		if (scaling != null)
			g.scale(scaling.x * (mirrorX ? -1 : 1), scaling.y * (mirrorY ? -1 : 1));
		g.shape(shape);
		g.popMatrix();
	}

	public void activate(GObject obj) {
		this.obj = obj;
	}

	public void setHighPriority(boolean priority) {
		this.visibilityPriority = priority;
	}

	public void setColour(int colour) {
		if (this.visualKind == VisualKind.UNTEXTURED)
			makeUntextured(colour);
		else
			makeTintedTexture(colour);
	}

	public PVector getSize() {
		if (size == null) {
			PVector max = new PVector(-Float.MAX_VALUE, -Float.MAX_VALUE);
			PVector min = new PVector(Float.MAX_VALUE, Float.MAX_VALUE);
			for (PVector v : vertices) {
				max.x = v.x > max.x ? v.x : max.x;
				max.y = v.y > max.y ? v.y : max.y;
				min.x = v.x < min.x ? v.x : min.x;
				min.y = v.y < min.y ? v.y : min.y;
			}
			size = PVector.sub(max, min);
		}
		return size;
	}

	public void mirrorVerticesV() {
		for (PVector v : vertices)
			if (v.x > 0)
				v.x *= -1;
	}

	public void mirrorVerticesH() {
		for (PVector v : vertices)
			if (v.y > 0)
				v.y *= -1;
	}

	public void setScaling(PVector scaling) {
		this.scaling = scaling;
	}

	public void setScaling(float scaling) {
		if (this.scaling == null)
			this.scaling = new PVector(scaling, scaling);
		else {
			this.scaling.x = scaling;
			this.scaling.y = scaling;
		}
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
			throw new IllegalStateException(
					"Cannot set a TINTED_TEXTURE tint unless VisualAttribute is in TEXTURED or TINTED_TEXTURE state");
		this.color = colour;
		this.shape = null;
	}

	/////////////////////
	// Private Methods //
	/////////////////////

	// Load a shape to cache if required.
	private void loadShape() {
		shape = EngineRuntime.applet.createShape();
		shape.beginShape();
		shape.noStroke();

		if (visualKind == VisualKind.UNTEXTURED) {
			shape.fill(color);
			for (PVector v : vertices) {
				shape.vertex(v.x, v.y);
			}
		} else {
			// shape.textureMode(PConstants.NORMAL);
			shape.texture(texture);

			if (visualKind == VisualKind.TINTED_TEXTURED) {
				shape.tint(color);
			}

			for (int i = 0; i < vertices.size(); i++) {
				shape.vertex(vertices.get(i).x, vertices.get(i).y, texCoords.get(i).x * texture.width,
						texCoords.get(i).y * texture.height);
			}
		}
		shape.endShape();
	}

	//////////////////
	// Constructors //
	//////////////////

	public VisualAttribute(List<PVector> vertices, List<PVector> texCoords, String texturePath) {
		this.vertices = vertices;
		this.makeTextured(texCoords, texturePath);
	}

	// ch315 - Added a constructor which takes a PImage so that we that a loaded
	// PImage texture can be
	// re-used and to try to reduce unnecessary calls of loadImage()
	public VisualAttribute(List<PVector> vertices, List<PVector> texCoords, PImage texture) {
		this.vertices = vertices;
		this.makeTextured(texCoords, texture);
	}

	public VisualAttribute(List<PVector> vertices, List<PVector> texCoords, PImage texture, int color) {
		this.vertices = vertices;
		this.makeTintedTexture(color, texCoords, texture);
	}

	public VisualAttribute(List<PVector> vertices) {
		this(vertices, 0x000000ff);
	}

	public VisualAttribute(List<PVector> vertices, int color) {
		this.vertices = vertices;
		this.makeUntextured(color);
	}
}
