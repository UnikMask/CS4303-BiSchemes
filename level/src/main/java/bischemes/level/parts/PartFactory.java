package bischemes.level.parts;

import bischemes.engine.GObject;
import bischemes.engine.VisualUtils;
import bischemes.engine.physics.*;
import bischemes.level.parts.behaviour.OnStateChangeBlock;
import bischemes.level.parts.behaviour.OnStateChangeDoor;
import bischemes.level.util.LColour;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartFactory {

	public enum PhysicsPreset {
		RESET, GEOMETRY, BLOCK, CONTACT_ONLY
	}

	private final int ELLIPSE_VERTICES = 20;
	private final int DEFAULT_COLOUR = 0x000000ff;

	private double restitution;
	private double staticFriction;
	private double dynamicFriction;
	private final Map<String, Object> RB_PROPERTIES = new HashMap<>(8);

	private PhysicsPreset preset = null;

	public PartFactory() {
		resetSurfaceProperties();
		resetRigidBodyProperties();
	}

	public PartFactory(PhysicsPreset preset) {
		initPhysicsProperties(preset);
	}

	public void resetSurfaceProperties() {
		preset = null;
		restitution = 0;
		staticFriction = 0;
		dynamicFriction = 0;
	}

	public void setSurfaceProperties(double restitution, double staticFriction, double dynamicFriction) {
		preset = null;
		this.restitution = restitution;
		this.staticFriction = staticFriction;
		this.dynamicFriction = dynamicFriction;
	}

	public void setSurfaceRestitution(double restitution) {
		preset = null;
		this.restitution = restitution;
	}

	public void setSurfaceStaticFriction(double staticFriction) {
		preset = null;
		this.staticFriction = staticFriction;
	}

	public void setSurfaceDynamicFriction(double dynamicFriction) {
		preset = null;
		this.dynamicFriction = dynamicFriction;
	}

	public void resetRigidBodyProperties() {
		preset = null;
		RB_PROPERTIES.clear();
		RB_PROPERTIES.put("mass", 0.0);
		RB_PROPERTIES.put("inertia", 0.0);
		RB_PROPERTIES.put("damping", 0.0);
		RB_PROPERTIES.put("velocity", new PVector());
		RB_PROPERTIES.put("rotation", 0.0);
		RB_PROPERTIES.put("move", false);
		RB_PROPERTIES.put("rotate", false);
		RB_PROPERTIES.put("mesh", null);
	}

	public void setRigidBodyProperty(String key, Object value) {
		if (!RB_PROPERTIES.containsKey(key))
			throw new IllegalArgumentException("RigidBodyProperty " + key + " does not exist");
		else if (key.equals("mesh") && !(value instanceof PhysicsMesh))
			throw new IllegalArgumentException(
					"Invalid RigidBodyProperty value for mesh, " + value + " is not of type PhysicsMesh");
		else if (!RB_PROPERTIES.get(key).getClass().equals(value.getClass()))
			throw new IllegalArgumentException("Invalid RigidBodyProperty value for" + key + ", " + value
					+ " is not of type " + RB_PROPERTIES.get(key).getClass());
		preset = null;
		RB_PROPERTIES.replace(key, value);
	}

	public void initPhysicsProperties(PhysicsPreset preset) {
		if (this.preset == preset)
			return;
		else
			this.preset = preset;
		resetSurfaceProperties();
		resetRigidBodyProperties();
		// TODO assign correct default properties here
		switch (preset) {
		case GEOMETRY:
			break;
		case BLOCK:
			RB_PROPERTIES.replace("move", true);
			break;
		case CONTACT_ONLY:
			break;
		case RESET:
			break;
		}
	}

	private void finishPolygon(GObject obj, List<PVector> vertices) {
		RigidBody rigidBody = new RigidBody(new RigidBodyProperties(RB_PROPERTIES));
		Surface surface = new Surface(restitution, staticFriction, dynamicFriction);
		Primitive primitive = new Primitive(surface, vertices);
		obj.setRigidBody(rigidBody);
		// TODO how to assign RigidBody and Primitive to GObject?
	}

	private void finishCircle(GObject obj, float radius) {
		RigidBody rigidBody = new RigidBody(new RigidBodyProperties(RB_PROPERTIES));
		Surface surface = new Surface(restitution, staticFriction, dynamicFriction);
		Primitive primitive = new Primitive(surface, radius);
		obj.setRigidBody(rigidBody);
		// TODO how to assign RigidBody and Primitive to GObject?
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

	public RObject createTriangle(GObject parent, PVector anchor, PVector vertex1, PVector vertex2, LColour colour,
			int id) {
		return (RObject) createTriangle(new RObject(parent, anchor, 0, id, colour), vertex1, vertex2);
	}

	public GObject createTriangle(GObject parent, PVector anchor, PVector vertex1, PVector vertex2) {
		return createTriangle(new GObject(parent, anchor, 0), vertex1, vertex2);
	}

	private GObject createTriangle(GObject obj, PVector vertex1, PVector vertex2) {
		obj.addVisualAttributes(
				VisualUtils.makeTriangle(new PVector(), vertex1.copy(), vertex2.copy(), DEFAULT_COLOUR));

		List<PVector> vertices = new ArrayList<>(3);
		vertices.add(new PVector());
		vertices.add(vertex1.copy());
		vertices.add(vertex2.copy());

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
		obj.addVisualAttributes(VisualUtils.makeUntexturedPolygon(new PVector(radius, radius), ELLIPSE_VERTICES, 0f,
				new PVector(), DEFAULT_COLOUR));

		finishCircle(obj, radius);
		return obj;
	}

	// TODO
	public RObject makeBlock(GObject parent, PVector anchor, PVector dimensions, boolean initState, LColour colour,
			int id) {
		RObject block = createRect(parent, anchor, dimensions, 0f, colour, id);
		OnStateChangeBlock.newOnStateChange(block, initState, dimensions);
		return block;
	}

	// TODO
	public RObject makeDoor(GObject parent, PVector anchor, PVector dimensions, boolean initState, LColour colour,
			int id) {
		RObject rect = createRect(parent, anchor, dimensions, 0f, colour, id);
		OnStateChangeDoor.newOnStateChange(rect, initState, dimensions);
		return rect;
	}

	// TODO
	public RObject makeSpike(GObject parent, PVector anchor, float orientation, LColour colour, int id) {
		return null;
	}

	// TODO
	public RObject makeLever(GObject parent, PVector anchor, float orientation, int[] linkedIDs, LColour colour,
			int id) {
		return null;
	}

	// TODO
	public RObject makePortal(GObject parent, PVector anchor, float width, float orientation, int id) {
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
