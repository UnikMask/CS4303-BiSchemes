package bischemes.level.parts;

import bischemes.engine.GObject;
import bischemes.engine.VisualUtils;
import bischemes.engine.physics.*;
import bischemes.level.Room;
import bischemes.level.parts.behaviour.*;
import bischemes.level.util.LColour;
import bischemes.level.util.SpriteLoader;
import processing.core.PVector;

import java.util.*;

public class PartFactory {

	private final int ELLIPSE_VERTICES = 20;
	private final int DEFAULT_COLOUR = 0x000000ff;
	private final float PORTAL_PROTRUDE = 0.2f;
	private final float PORTAL_SUB_WIDTH = 0.8f;

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
		setSurfaceRestitution(restitution);
		setSurfaceStaticFriction(staticFriction);
		setSurfaceDynamicFriction(dynamicFriction);
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

	public void initRBMoveable(double mass) {
		initRBGeometry();
		rbProperties.put("move", true);
		rbProperties.put("mass", mass);
	}

	public void initRBRotateable(double mass) {
		initRBGeometry();
		rbProperties.put("rotate", true);
		rbProperties.put("mass", mass);
		hasInertia = true;
	}

	public void initRBBlock(double mass) {
		initRBRotateable(mass);
		rbProperties.put("move", true);
	}

	private void finishRigidBody(GObject obj, Primitive p) {
		if (hasMesh) {
			rbProperties.put("mesh", p);
			if (hasInertia) {
				double mass = (double) rbProperties.get("mass");
				rbProperties.put("inertia", PrimitiveUtils.getPrimitiveInertia(p, mass, new PVector()));
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
		return (RObject) createRect(new RObject(parent, anchor, orientation, id, colour), dimensions);
	}
	public RObject createCornerRect(GObject parent, PVector corner, PVector dimensions, float orientation,
									LColour colour, int id) {
		PVector anchor = dimensions.copy().div(2f).add(corner);
		return (RObject) createRect(new RObject(parent, anchor, orientation, id, colour), dimensions);
	}

	public GObject createRect(GObject parent, PVector anchor, PVector dimensions, float orientation) {
		return createRect(new GObject(parent, anchor, orientation), dimensions);
	}

	public GObject createCornerRect(GObject parent, PVector corner, PVector dimensions, float orientation) {
		PVector anchor = dimensions.copy().div(2f).add(corner);
		return createRect(new GObject(parent, anchor, orientation), dimensions);
	}

	private GObject createRect(GObject obj, PVector dimensions) {
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
	public RObject createCornerTriangle(GObject parent, PVector corner, PVector vertex2, PVector vertex3,
										LColour colour, int id) {
		PVector anchor = new PVector(
				(Math.min(0, Math.min(vertex2.x, vertex3.x)) + Math.max(0, Math.max(vertex2.x, vertex3.x))) / 2f,
				(Math.min(0, Math.min(vertex2.y, vertex3.y)) + Math.max(0, Math.max(vertex2.y, vertex3.y))) / 2f
		);
		PVector vertex1 = new PVector().sub(anchor);
		anchor = anchor.add(corner);
		return (RObject) createTriangle(new RObject(parent, anchor, 0, id, colour), vertex1, vertex2, vertex3);
	}
	public GObject createTriangle(GObject parent, PVector anchor, PVector vertex1, PVector vertex2, PVector vertex3) {
		return createTriangle(new GObject(parent, anchor, 0), vertex1, vertex2, vertex3);
	}
	public GObject createCornerTriangle(GObject parent, PVector corner, PVector vertex2, PVector vertex3) {
		PVector anchor = new PVector(
				(Math.min(0, Math.min(vertex2.x, vertex3.x)) + Math.max(0, Math.max(vertex2.x, vertex3.x))) / 2f,
				(Math.min(0, Math.min(vertex2.y, vertex3.y)) + Math.max(0, Math.max(vertex2.y, vertex3.y))) / 2f
		);
		PVector vertex1 = new PVector().sub(anchor);
		anchor = anchor.add(corner);
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

	public RObject createCircle(GObject parent, PVector anchor, float radius, LColour colour, int id) {
		return (RObject) createCircle(new RObject(parent, anchor, 0, id, colour), radius);
	}

	private GObject createCircle(GObject obj, float radius) {
		obj.addVisualAttributes(VisualUtils.makeUntexturedPolygon(new PVector(radius * 2, radius * 2), ELLIPSE_VERTICES,
				0f, new PVector(), DEFAULT_COLOUR));

		finishCircle(obj, radius);
		return obj;
	}

	public RObject createTrapezium(GObject parent, PVector anchor, float orientation, float height, PVector widths,
								   LColour colour, int id) {
		return (RObject) createTrapezium(new RObject(parent, anchor, orientation, id, colour), height, widths);
	}
	public RObject createCornerTrapezium(GObject parent, PVector corner, float orientation, float height, PVector widths,
										 LColour colour, int id) {
		PVector anchor = corner.copy().add(widths.x / 2f, height / 2f);
		return (RObject) createTrapezium(new RObject(parent, anchor, orientation, id, colour), height, widths);
	}

	public GObject createTrapezium(GObject parent, PVector anchor, float orientation, float height, PVector widths) {
		return createTrapezium(new GObject(parent, anchor, orientation), height, widths);
	}

	public GObject createCornerTrapezium(GObject parent, PVector corner, float orientation, float height, PVector widths) {
		PVector anchor = corner.copy().add(widths.x / 2f, height / 2f);
		return createTrapezium(new GObject(parent, anchor, orientation), height, widths);
	}

	private GObject createTrapezium(GObject obj, float height, PVector widths) {
		float halfHeight = height / 2f;
		float rectWidth = Math.min(widths.x, widths.y);

		List<PVector> vertices = new ArrayList<>(4);
		vertices.add(new PVector(-widths.y / 2f, halfHeight));
		vertices.add(new PVector(-widths.x / 2f, -halfHeight));
		vertices.add(new PVector(widths.x / 2f, -halfHeight));
		vertices.add(new PVector(widths.y / 2f, halfHeight));


		PVector t1V3 = new PVector(-rectWidth, (widths.x > widths.y) ? -halfHeight : halfHeight );
		PVector t2V3 = new PVector(rectWidth, t1V3.y );

		obj.addVisualAttributes(VisualUtils.makeRect(new PVector(rectWidth, height), DEFAULT_COLOUR));

		obj.addVisualAttributes(
				VisualUtils.makeTriangle(vertices.get(0).copy(), vertices.get(1).copy(), t1V3, DEFAULT_COLOUR));
		obj.addVisualAttributes(
				VisualUtils.makeTriangle(vertices.get(2).copy(), vertices.get(3).copy(), t2V3, DEFAULT_COLOUR));

		finishPolygon(obj, vertices);

		return obj;
	}

	public RObject makeBlock(GObject parent, PVector anchor, PVector dimensions, boolean initState, double mass,
							 LColour colour, int id) {
		initRBBlock(mass);
		RObject block = createRect(parent, anchor, dimensions, 0f, colour, id);
		BStateBlock.assign(block, initState, dimensions);
		return block;
	}

	public RObject makeCornerBlock(GObject parent, PVector corner, PVector dimensions, boolean initState, double mass,
								   LColour colour, int id) {
		initRBBlock(mass);
		RObject block = createCornerRect(parent, corner, dimensions, 0f, colour, id);
		BStateBlock.assign(block, initState, dimensions);
		return block;
	}

	public RObject makeDoor(GObject parent, PVector anchor, PVector dimensions, boolean initState, LColour colour,
			int id) {
		initRBGeometry();
		RObject rect = createRect(parent, anchor, dimensions, 0f, colour, id);
		BStateHide.assign(rect, initState).addLockIcon(dimensions);
		return rect;
	}
	public RObject makeCornerDoor(GObject parent, PVector corner, PVector dimensions, boolean initState, LColour colour,
								  int id) {
		initRBGeometry();
		RObject rect = createCornerRect(parent, corner, dimensions, 0f, colour, id);
		BStateHide.assign(rect, initState).addLockIcon(dimensions);
		return rect;
	}
	public RObject makeSpike(GObject parent, PVector anchor, float orientation, int length, LColour colour, int id) {
		initRBGeometry();

		RObject spikes = new RObject(parent, anchor, orientation, id, colour);
		PrimitiveAssembly assembly = new PrimitiveAssembly();
		Surface surface = new Surface(restitution, staticFriction, dynamicFriction);

		PVector v1 = new PVector(-0.5f, 0.5f);
		PVector v2 = new PVector(0.5f, 0.5f);
		PVector v3 = new PVector(0f, -0.5f);
		for (int i = 0; i < length; i++) {
			spikes.addVisualAttributes(VisualUtils.makeTriangle(
					v1.copy().add(i, 0), v2.copy().add(i, 0), v3.copy().add(i, 0), DEFAULT_COLOUR));
			assembly.addPrimitive(new Primitive(surface, Arrays.asList(
					v1.copy(), v2.copy(), v3.copy())), new PVector(i, 0));
		}

		rbProperties.put("mesh", assembly);
		rbProperties.remove("inertia");

		spikes.setRigidBody(new RigidBody(new RigidBodyProperties(rbProperties)));
		BHitKill.assign(spikes);

		return spikes;
	}

	public RObject makeLever(GObject parent, PVector anchor, float orientation, int[] linkedIDs, LColour colour,
			int id) {
		initRBNoCollision();
		RObject lever = new RObject(parent, anchor, orientation, id, colour);

		lever.addVisualAttributes(VisualUtils.makeRect(new PVector(1, 1), SpriteLoader.getLever()));

		BStateFlip.assign(lever, false);
		BStateSwitchStates.assign(lever, linkedIDs);
		BInteractStateSwitch.assign(lever, 1, 1).addIndicator(new PVector(1, 1));
		return lever;
	}


	public RObject makeOnewayPortal(GObject parent, PVector anchor, int width, boolean isVertical, boolean flipFace,
									boolean initialState, LColour colour, int id) {
		float orientation = 0f;
		PVector newAnchor = anchor.copy();
		PVector link = anchor.copy();
		PVector offset = new PVector();

		if (isVertical) {
			offset.y = PORTAL_PROTRUDE;
			if (flipFace) orientation = (float) (Math.PI / 2f);
			else offset.y *= -1;
		}
		else {
			orientation = (float) (Math.PI / 4f);
			offset.x = PORTAL_PROTRUDE;
			if (!flipFace) {
				orientation *= 3f;
				offset.x *= -1;
			}
		}

		newAnchor.add(offset);
		link.sub(offset);

		initRBNoCollision();

		RObject portal = createTrapezium(parent, newAnchor, orientation, PORTAL_PROTRUDE,
				new PVector(((float) width) * PORTAL_SUB_WIDTH, (float) width), colour, id);
		portal.setState(initialState);

		BInteractTeleport b = BInteractTeleport.assign(portal, width, PORTAL_PROTRUDE, link, true);
		/*
		TODO these following lines my make for better indicator positioning
		TODO if it does work better, use such offset in makePortal()
		offset.mult(1f / PORTAL_PROTRUDE);
		b.addColourSwitchIndicator(offset);
		*/
		b.addColourSwitchIndicator(new PVector(0, 1f));
		b.configureOffset(true, true, isVertical, !isVertical);
		b.setActiveOnState(true);
		b.configureGravityFlip(isVertical);

		return portal;
	}

	public RObject makePortal(GObject parent, PVector anchor, int width, boolean isVertical, boolean initialState,
							  LColour colour1, int id) {
		LColour colour2 = (colour1 == LColour.PRIMARY) ? LColour.SECONDARY : LColour.PRIMARY;

		float orientation1 = 0f;
		float orientation2 = (float) (Math.PI / 2f);

		PVector anchor1 = anchor.copy();
		PVector anchor2 = anchor.copy();

		PVector offset = new PVector();

		if (isVertical)
			offset.y = PORTAL_PROTRUDE;
		else {
			orientation1 += (float) (Math.PI / 4f);
			orientation2+= (float) (Math.PI / 4f);
			offset.x = PORTAL_PROTRUDE;
		}

		anchor1.add(offset);
		anchor2.sub(offset);

		initRBNoCollision();

		RObject portal = new RObject(parent, anchor, 0, id);

		RObject childPortal1 = createTrapezium(portal, anchor1, orientation1, PORTAL_PROTRUDE,
				new PVector(((float) width) * PORTAL_SUB_WIDTH, (float) width), colour1, id);

		RObject childPortal2 = createTrapezium(portal, anchor2, orientation2, PORTAL_PROTRUDE,
				new PVector(((float) width) * PORTAL_SUB_WIDTH, (float) width), colour2, id);

		portal.setState(initialState);

		BInteractTeleport b1 = BInteractTeleport.assign(childPortal1, width, PORTAL_PROTRUDE, anchor2,true);
		BInteractTeleport b2 = BInteractTeleport.assign(childPortal2, width, PORTAL_PROTRUDE, anchor1,true);

		b1.addColourSwitchIndicator(new PVector(0, 1f));
		b1.setActiveOnState(true);
		b1.configureGravityFlip(isVertical);
		b1.configureOffset(true, true, isVertical, !isVertical);

		b2.addColourSwitchIndicator(new PVector(0, 1f));
		b2.setActiveOnState(true);
		b2.configureGravityFlip(isVertical);
		b2.configureOffset(true, true, isVertical, !isVertical);

		return portal;
	}

	// TODO determine design
	public RObject makeExit(GObject parent, PVector range, boolean isVertical, boolean zeroAxis, int id) {

		PVector anchor = new PVector(-1, -1);
		PVector dimensions;
		float length = range.y - range.x;
		if (isVertical) {
			if (!zeroAxis)
				anchor.y = Room.getRoom(parent).getDimensions().y + 1;
			anchor.x = length / 2f;
			dimensions = new PVector(length, 1);
		}
		else {
			if (!zeroAxis)
				anchor.x = Room.getRoom(parent).getDimensions().x + 1;
			anchor.y = length / 2f;
			dimensions = new PVector(1, length);
		}


		initRBGeometry();
		//TODO might not be ok for colour to be null
		RObject exit = createRect(parent, anchor, dimensions, 0f, null, id);
		BHitExit.assign(exit);

		return exit;
	}

}
