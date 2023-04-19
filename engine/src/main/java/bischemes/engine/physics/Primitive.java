package bischemes.engine.physics;

import java.util.List;

import bischemes.engine.physics.PrimitiveAssembly.PrimitiveInSet;
import processing.core.PVector;

public class Primitive implements PhysicsMesh {
	private PrimitiveType primitiveType;
	private RigidBody parent;

	private List<PVector> vertices;
	private double radius;
	private PVector AABBBounds;

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

	/////////////////////
	// Physics Methods //
	/////////////////////

	/**
	 * Check for collision with a single other primitive.
	 *
	 * @param b The primitive to check against for collisions.
	 * @return The contact manifold between this primitive and b.
	 */
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

	/**
	 * Check for collision with an assembly of multiple primitives.
	 *
	 * @param b The primitive assembly to check against for collisions.
	 * @return The contact manifold between this primitive and b.
	 */
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

	// TODO - Polygon-to-Polygon collision code.
	private Manifold polygonToPolygonCollision(Primitive b, PVector offset) {
		return null;
	}

	// Generate a circle-to-polygon collision, where b is the polygon primitive.
	private Manifold circleToPolygonCollision(Primitive b, PVector offset) {
		Manifold m = new Manifold(this.parent, b.parent);
		PVector vertexParentPosition = PVector.add(b.parent.getPosition(), offset);

		// Step 1 - find the nearest penetration edge.
		double minPenetration = -Double.MAX_VALUE;
		PVector v0min = new PVector(), v1min = new PVector();
		PVector vNormal = null;
		float reverseFactor = 1;

		boolean calibrated = false;
		PVector v0 = null;
		for (PVector v : b.vertices) {
			v = PVector.add(vertexParentPosition, v);
			if (v0 == null) {
				v0 = v;
			}
			PVector normal = new PVector(PVector.sub(v, v0).y, -PVector.sub(v, v0).x).mult(reverseFactor).normalize();
			PVector support = PVector.sub(this.parent.getPosition(), PVector.mult(normal, (float) this.radius));
			double dist = PVector.dot(PVector.sub(support, v), normal);

			if (!calibrated && dist > 0.0f) {
				reverseFactor = -1.0f;
				dist = -dist;
			}
			if (dist > minPenetration) {
				if (dist > 0) {
					return m;
				}
				v0min = v0;
				v1min = v;
				vNormal = normal;
				minPenetration = dist;
			}

			v0 = v;
			calibrated = true;
		}

		// Step 2 - Find the collision point with the edge

		PVector tan1 = PVector.sub(v0min, v1min);
		PVector tan2 = PVector.sub(v1min, v0min);

		// If the center of the mass is in the voronoi region of the edge
		PVector contactPoint = PVector.sub(this.parent.getPosition(), PVector.mult(vNormal, (float) this.radius));
		if (PVector.dot(PVector.sub(this.parent.getPosition(), v0min), tan1) > 0
				&& PVector.dot(PVector.sub(this.parent.getPosition(), v1min), tan2) > 0) {
		} else {
			double distV0 = PVector.sub(this.parent.getPosition(), v0min).mag();
			double distV1 = PVector.sub(this.parent.getPosition(), v1min).mag();
			vNormal = PVector.sub(this.parent.getPosition(), distV0 < distV1 ? v0min : v1min).normalize();
			contactPoint = PVector.sub(this.parent.getPosition(), PVector.mult(vNormal, (float) this.radius));
			minPenetration = PVector.dot(PVector.sub(distV0 < distV1 ? v0min : v1min, contactPoint), vNormal);
		}
		m.addContactPoint(contactPoint, vNormal, minPenetration);

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
			m.addContactPoint(contactPoint, normal, penetration);
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
	public Primitive(RigidBody parent, List<PVector> vertices) {
		this.parent = parent;
		primitiveType = PrimitiveType.POLYGON;
		this.vertices = vertices;

		// Assemble AABB bounds
		PVector min = new PVector(), max = new PVector();
		for (PVector v : vertices) {
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
	public Primitive(RigidBody parent, double radius) {
		this.parent = parent;
		primitiveType = PrimitiveType.CIRCLE;
		this.radius = radius;
		this.AABBBounds = new PVector(2 * (float) radius, 2 * (float) radius);
	}
}
