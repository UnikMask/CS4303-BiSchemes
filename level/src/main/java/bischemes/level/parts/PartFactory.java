package bischemes.level.parts;

import bischemes.engine.GObject;
import bischemes.engine.VisualUtils;
import bischemes.engine.physics.*;
import bischemes.level.parts.behaviour.*;
import bischemes.level.util.LColour;
import bischemes.level.util.SpriteLoader;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartFactory {

	private final int ELLIPSE_VERTICES = 20;
	private final int DEFAULT_COLOUR = 0x000000ff;

	private double restitution;
	private double staticFriction;
	private double dynamicFriction;
	private final Map<String, Object> rbProperties = new HashMap<>(10);

	private boolean hasMesh;
	private boolean hasInertia;

	public PartFactory() {
		initSurface();
		initRBGeometry();
	}

	public void initSurface() {
		restitution = 0;
		staticFriction = 0;
		dynamicFriction = 0;
	}

	public void setSurfaceProperties(double restitution, double staticFriction, double dynamicFriction) {
		this.restitution = restitution;
		this.staticFriction = staticFriction;
		this.dynamicFriction = dynamicFriction;
	}

	public void setSurfaceRestitution(double restitution) {
		this.restitution = restitution;
	}

	public void setSurfaceStaticFriction(double staticFriction) {
		this.staticFriction = staticFriction;
	}

	public void setSurfaceDynamicFriction(double dynamicFriction) {
		this.dynamicFriction = dynamicFriction;
	}

	private void resetRB() {
		rbProperties.clear();
		rbProperties.put("move", false);
		rbProperties.put("rotate", false);
		hasInertia = false;
	}

	public void initRBGeometry() {
		resetRB();
		hasMesh = true;
	}

	public void initRBNoCollision() {
		resetRB();
		hasMesh = false;
	}

	public void initRBMoveable(float mass) {
		initRBGeometry();
		rbProperties.put("move", true);
		rbProperties.put("mass", mass);
	}

	public void initRBRotateable(float mass) {
		initRBGeometry();
		rbProperties.put("rotate", true);
		rbProperties.put("mass", mass);
		hasInertia = true;
	}

	public void initRBBlock(float mass) {
		initRBRotateable(mass);
		rbProperties.put("move", true);
	}

	private void finishRigidBody(GObject obj, Primitive p) {
		if (hasMesh) {
			rbProperties.put("mesh", p);
			if (hasInertia) {
				float mass = (float) rbProperties.get("mass");
				rbProperties.put("inertia", PrimitiveUtils.getPrimitiveInertia(p, mass, null));
			}
			else rbProperties.remove("inertia");
		}
		else {
			rbProperties.remove("mesh");
			rbProperties.remove("inertia");
		}
		obj.setRigidBody(new RigidBody(new RigidBodyProperties(rbProperties)));
	}

	private void finishPolygon(GObject obj, List<PVector> vertices) {
		Surface surface = new Surface(restitution, staticFriction, dynamicFriction);
		finishRigidBody(obj, new Primitive(surface, vertices));
	}

	private void finishCircle(GObject obj, float radius) {
		Surface surface = new Surface(restitution, staticFriction, dynamicFriction);
		finishRigidBody(obj, new Primitive(surface, radius));
	}

	public RObject createRect(GObject parent, PVector anchor, PVector dimensions, float orientation, LColour colour,
			int id) {
		return (RObject) createRect(new RObject(parent, anchor, orientation, id, colour), dimensions, orientation);
	}

	public GObject createRect(GObject parent, PVector anchor, PVector dimensions, float orientation) {
		return createRect(new GObject(parent, anchor, orientation), dimensions, orientation);
	}

	private GObject createRect(GObject obj, PVector dimensions, float orientation) {
		obj.addVisualAttributes(VisualUtils.makeRect(dimensions, DEFAULT_COLOUR));

		List<PVector> vertices = new ArrayList<>(4);
		vertices.add(new PVector(dimensions.x / 2f, dimensions.y / 2f));
		vertices.add(new PVector(-dimensions.x / 2f, dimensions.y / 2f));
		vertices.add(new PVector(-dimensions.x / 2f, -dimensions.y / 2f));
		vertices.add(new PVector(dimensions.x / 2f, -dimensions.y / 2f));

		finishPolygon(obj, vertices);
		return obj;
	}

	public RObject createTriangle(GObject parent, PVector anchor, PVector vertex1, PVector vertex2, PVector vertex3,
								  LColour colour, int id) {
		return (RObject) createTriangle(new RObject(parent, anchor, 0, id, colour), vertex1, vertex2, vertex3);
	}

	public GObject createTriangle(GObject parent, PVector anchor, PVector vertex1, PVector vertex2, PVector vertex3) {
		return createTriangle(new GObject(parent, anchor, 0), vertex1, vertex2, vertex3);
	}

	private GObject createTriangle(GObject obj, PVector vertex1, PVector vertex2, PVector vertex3) {
		obj.addVisualAttributes(
				VisualUtils.makeTriangle(vertex1.copy(), vertex2.copy(), vertex3.copy(), DEFAULT_COLOUR));

		List<PVector> vertices = new ArrayList<>(3);
		vertices.add(vertex1.copy());
		vertices.add(vertex2.copy());
		vertices.add(vertex3.copy());

		finishPolygon(obj, vertices);
		return obj;
	}

	public RObject createPolygon(GObject parent, PVector anchor, PVector dimensions, int sides, float orientation,
			LColour colour, int id) {
		return (RObject) createPolygon(new RObject(parent, anchor, 0, id, colour), dimensions, sides, orientation);
	}

	public GObject createPolygon(GObject parent, PVector anchor, PVector dimensions, int sides) {
		return createPolygon(parent, anchor, dimensions, sides, 0f);
	}

	public GObject createPolygon(GObject parent, PVector anchor, PVector dimensions, int sides, float orientation) {
		return createPolygon(new GObject(parent, anchor, 0), dimensions, sides, orientation);
	}

	private GObject createPolygon(GObject obj, PVector dimensions, int sides, float orientation) {
		obj.addVisualAttributes(
				VisualUtils.makeUntexturedPolygon(dimensions, sides, orientation, new PVector(), DEFAULT_COLOUR));

		List<PVector> vertices = new ArrayList<>(sides);
		float increment = (float) (2 * Math.PI / sides);
		float angle = orientation;
		for (int i = 0; i < sides; i++) {
			vertices.add(new PVector((float) (dimensions.x / 2 * Math.cos(angle)),
					(float) (dimensions.y / 2 * Math.sin(angle))));
			angle += increment;
		}

		finishPolygon(obj, vertices);
		return obj;
	}

	public RObject createEllipse(GObject parent, PVector anchor, PVector dimensions, float orientation, LColour colour,
			int id) {
		return createPolygon(parent, anchor, dimensions, ELLIPSE_VERTICES, orientation, colour, id);
	}

	public GObject createEllipse(GObject parent, PVector anchor, PVector dimensions) {
		return createPolygon(parent, anchor, dimensions, ELLIPSE_VERTICES);
	}

	public GObject createEllipse(GObject parent, PVector anchor, PVector dimensions, float orientation) {
		return createPolygon(parent, anchor, dimensions, ELLIPSE_VERTICES, orientation);
	}

	public RObject createCircle(GObject parent, PVector anchor, float radius, LColour colour, int id) {
		return (RObject) createCircle(new RObject(parent, anchor, 0, id, colour), radius);
	}

	public GObject createCircle(GObject parent, PVector anchor, float radius) {
		return createCircle(new GObject(parent, anchor, 0f), radius);
	}

	private GObject createCircle(GObject obj, float radius) {
		obj.addVisualAttributes(VisualUtils.makeUntexturedPolygon(new PVector(radius, radius), ELLIPSE_VERTICES,
				0f, new PVector(), DEFAULT_COLOUR));

		finishCircle(obj, radius);
		return obj;
	}

	// TODO
	public RObject makeBlock(GObject parent, PVector anchor, PVector dimensions, boolean initState, float mass,
							 LColour colour, int id) {
		initRBBlock(mass);
		RObject block = createRect(parent, anchor, dimensions, 0f, colour, id);
		BStateBlock.assign(block, initState, dimensions);
		return block;
	}

	// TODO
	public RObject makeDoor(GObject parent, PVector anchor, PVector dimensions, boolean initState, LColour colour,
			int id) {
		initRBGeometry();
		RObject rect = createRect(parent, anchor, dimensions, 0f, colour, id);
		BStateHide.assign(rect, initState).addLockIcon(dimensions);
		return rect;
	}

	// TODO
	public RObject makeSpike(GObject parent, PVector anchor, float orientation, int length, LColour colour, int id) {
		return null;
	}

	// TODO
	public RObject makeLever(GObject parent, PVector anchor, float orientation, int[] linkedIDs, LColour colour,
			int id) {
		initRBNoCollision();
		RObject lever = new RObject(parent, anchor, orientation, id, colour);
		lever.addVisualAttributes(VisualUtils.makeRect(new PVector(1, 1), SpriteLoader.getLever()));
		BStateFlip.assign(lever, false);
		BStateSwitchStates.assign(lever, linkedIDs);
		BInteractStateSwitch.assign(lever, 1, 1).addIndicator(new PVector(1, 1));
		return null;
	}

	// TODO
	public RObject makePortal(GObject parent, PVector anchor, int width, float orientation, int id) {
		return null;
	}

	// TODO determine design
	public RObject makeExit(GObject parent) {
		return null;
	}

	// TODO determine design
	public RObject makeAdjacency(GObject parent) {
		return null;
	}

}
