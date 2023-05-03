package bischemes.engine.physics;

import java.util.ArrayList;
import java.util.List;

import bischemes.engine.physics.PrimitiveAssembly.PrimitiveInSet;
import processing.core.PVector;

public class Primitive implements PhysicsMesh {
	private PrimitiveType primitiveType;
	private RigidBody parent;
	Surface surface;

	// Reals
	private List<PVector> baseVerts;
	private double radius;
	private PVector AABBBounds;

	// Derived
	private List<PVector> vertices;

	static enum PrimitiveType {
		POLYGON, CIRCLE
	}

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	public PrimitiveType getType() {
		return primitiveType;
	}

	public RigidBody getParent() {
		return parent;
	}

	public PVector getAABBBounds() {
		return AABBBounds;
	}

	/////////////////////
	// Physics Methods //
	/////////////////////

	public void derive() {
		if (primitiveType == PrimitiveType.POLYGON) {
			vertices = new ArrayList<>(baseVerts.size());
			for (PVector v : baseVerts) {
				PVector vn = v.copy();
				vn.rotate((float) getParent().getOrientation());
				vertices.add(vn);
			}

		}
	}

	public Manifold getCollision(Primitive b, PVector offset) {
		return switch (this.primitiveType) {
		case CIRCLE -> switch (b.primitiveType) {
			case CIRCLE -> this.circleToCircleCollision(b, offset);
			case POLYGON -> this.circleToPolygonCollision(b, offset);
			};
		case POLYGON -> switch (b.primitiveType) {
			case CIRCLE -> b.circleToPolygonCollision(this, PVector.mult(offset, -1));
			case POLYGON -> this.polygonToPolygonCollision(b, offset);
			};
		};
	}

	public Manifold getCollision(PrimitiveAssembly b, PVector offset) {
		Manifold m = new Manifold(this.parent, b.getParent());
		for (PrimitiveInSet p : b.getAssembly()) {
			m.combine(getCollision(p.primitive, PVector.sub(p.offset, offset)));
		}
		return m;
	}

	public Manifold getCollision(PhysicsMesh b) {
		if (b instanceof Primitive) {
			return getCollision((Primitive) b);
		} else if (b instanceof PrimitiveAssembly) {
			return getCollision((PrimitiveAssembly) b);
		} else {
			return null;
		}
	}

	/////////////////////
	// Private Methods //
	/////////////////////

	// Get full collision manifold for contact between 2 polygon primitives.
	private Manifold polygonToPolygonCollision(Primitive b, PVector offset) {
		Manifold m = new Manifold(this.parent, b.getParent());
		m.combine(queryFaceDistance(b, offset));
		m.combine(b.queryFaceDistance(this, PVector.mult(offset, -1)));
		return m;
	}

	private class BestDist {
		double minPenetration;
		PVector bestNormal;
		PVector bestSupport;
		PVector v0, v1;

		public BestDist(double minPenetration, PVector bestNormal, PVector bestSupport, PVector v0, PVector v1) {
			this.minPenetration = minPenetration;
			this.bestNormal = bestNormal;
			this.bestSupport = bestSupport;
			this.v0 = v0;
			this.v1 = v1;
		}
	}

	// Get the best penetration distance between 2 polygons - fails early and
	// returns null if proven not colliding.
	private BestDist getBestDist(Primitive b, PVector offset) {
		double minPenetration = -Double.MAX_VALUE;
		PVector bestNormal = new PVector();
		PVector bestSupport = new PVector();

		PVector v1min = new PVector(), v2min = new PVector();

		float reverseFactor = 1.0f;
		PVector v2 = PVector.add(this.vertices.get(this.vertices.size() - 1), this.parent.getPosition());
		boolean calibrating = true;
		for (PVector p1 : this.vertices) {
			PVector v1 = PVector.add(p1, this.parent.getPosition());

			PVector v1v2 = PVector.sub(v2, v1);
			PVector normal = new PVector(v1v2.y, -v1v2.x).mult(reverseFactor).normalize();
			PVector support = PVector.add(b.getSupportPoint(PVector.mult(v1v2, -1)), offset);
			double dist = PVector.dot(PVector.sub(support, v1), normal);

			if (calibrating && dist > 0) {
				dist = -dist;
				reverseFactor = -1.0f;
			}
			if (dist > minPenetration) {
				if (minPenetration > 0) {
					return null;
				}
				v1min = v1;
				v2min = v2;

				minPenetration = dist;
				bestNormal = normal;
				bestSupport = support;
			}
			v2 = v1;
		}
		return new BestDist(minPenetration, bestNormal, bestSupport, v1min, v2min);
	}

	// Query face distance between this polygon, from which the faces are checked,
	// against another polygon's vertices. Returns a collision manifold with a
	// potential collision point if there is one.
	private Manifold queryFaceDistance(Primitive b, PVector offset) {
		Manifold m = new Manifold(this.parent, b.getParent());
		BestDist bd = getBestDist(b, offset);
		if (bd != null) {
			m.addContactPoint(bd.bestSupport, bd.bestNormal, bd.minPenetration, this, b);
		}

		return m;
	}

	// Get the support point of a primitive in a given direction.
	private PVector getSupportPoint(PVector direction) {
		return switch (primitiveType) {
		case CIRCLE -> circleSupportPoint(direction);
		case POLYGON -> polygonSupportPoint(direction);
		};
	}

	// Get the support point of a circle primitive in a given direction.
	private PVector circleSupportPoint(PVector direction) {
		return PVector.add(this.parent.getPosition(), PVector.mult(direction, (float) this.radius));
	}

	// Get the support point of a polygon primitive in a given direction.
	private PVector polygonSupportPoint(PVector direction) {
		PVector max = vertices.get(0);
		double maxDot = PVector.dot(vertices.get(0), direction);
		for (PVector v : vertices) {
			double dot = v.dot(direction);
			if (dot > maxDot) {
				max = v;
				maxDot = dot;
			}
		}
		return max;
	}

	// Generate a circle-to-polygon collision, where b is the polygon primitive.
	private Manifold circleToPolygonCollision(Primitive b, PVector offset) {
		Manifold m = new Manifold(this.parent, b.parent);
		BestDist bd = getBestDist(b, offset);
		if (bd == null) {
			return m;
		}

		// Step 2 - Find the collision point with the edge
		PVector tan1 = PVector.sub(bd.v0, bd.v1);
		PVector tan2 = PVector.sub(bd.v1, bd.v0);

		// If the center of the mass is in the voronoi region of the edge
		PVector contactPoint = PVector.sub(this.parent.getPosition(), PVector.mult(bd.bestNormal, (float) this.radius));
		if (PVector.dot(PVector.sub(this.parent.getPosition(), bd.v0), tan1) > 0
				&& PVector.dot(PVector.sub(this.parent.getPosition(), bd.v1), tan2) > 0) {
		} else {
			double distV0 = PVector.sub(this.parent.getPosition(), bd.v0).mag();
			double distV1 = PVector.sub(this.parent.getPosition(), bd.v1).mag();
			bd.bestNormal = PVector.sub(this.parent.getPosition(), distV0 < distV1 ? bd.v0 : bd.v1).normalize();
			contactPoint = PVector.sub(this.parent.getPosition(), PVector.mult(bd.bestNormal, (float) this.radius));
			bd.minPenetration = PVector.dot(PVector.sub(distV0 < distV1 ? bd.v1 : bd.v0, contactPoint), bd.bestNormal);
		}
		m.addContactPoint(contactPoint, bd.bestNormal, bd.minPenetration, this, b);

		return m;
	}

	// Generate a circle-to-circle collision between 2 primitives, and return a
	// manifold.
	private Manifold circleToCircleCollision(Primitive b, PVector offset) {
		PVector dir = PVector.sub(PVector.add(b.parent.getPosition(), offset), this.parent.getPosition());
		Manifold m = new Manifold(this.parent, b.parent);
		if (dir.mag() < this.radius + b.radius) {
			double penetration = this.radius + b.radius - dir.mag();
			PVector normal = dir.normalize();
			PVector contactPoint = PVector.add(this.parent.getPosition(), PVector.mult(normal, (float) this.radius));
			m.addContactPoint(contactPoint, normal, penetration, this, b);
		}
		return m;
	}

	//////////////////
	// Constructors //
	//////////////////

	/**
	 * Constructor for a polygon-type primitive.
	 *
	 * @param parent   The rigid body parent of the primitive.
	 * @param vertices The list of vertices that define a primitive - the polygon
	 *                 MUST be flat and convex.
	 */
	public Primitive(RigidBody parent, Surface surface, List<PVector> vertices) {
		this.parent = parent;
		primitiveType = PrimitiveType.POLYGON;
		this.surface = surface;
		this.baseVerts = vertices;

		// Assemble AABB bounds
		PVector min = new PVector(), max = new PVector();
		for (PVector v : baseVerts) {
			min.x = v.x < min.x ? v.x : min.x;
			min.y = v.y < min.y ? v.y : min.y;
			max.x = v.x > max.x ? v.x : max.x;
			max.y = v.y > min.y ? v.y : max.y;
		}
		AABBBounds = PVector.sub(max, min);

	}

	/**
	 * Constructor for a circle-type primitive.
	 *
	 * @param parent The rigid body parent of the primitive.
	 * @param radius The radius of the circle.
	 */
	public Primitive(Surface surface, double radius) {
		primitiveType = PrimitiveType.CIRCLE;
		this.radius = radius;
		this.surface = surface;
		this.AABBBounds = new PVector(2 * (float) radius, 2 * (float) radius);
	}
}
