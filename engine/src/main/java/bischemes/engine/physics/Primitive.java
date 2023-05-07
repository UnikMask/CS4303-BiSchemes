package bischemes.engine.physics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bischemes.engine.GObject;
import bischemes.engine.Pair;
import bischemes.engine.physics.PrimitiveAssembly.PrimitiveInSet;
import processing.core.PVector;

public class Primitive implements PhysicsMesh {
	private static final double PARRALLEL_THRESHOLD = 0.0001;

	private PrimitiveType primitiveType;
	private RigidBody parent;
	Surface surface;

	// Reals
	private List<PVector> baseVerts;
	private double radius;
	private PVector baseAABB = new PVector();

	// Derived
	private List<PVector> vertices;
	private PVector AABBBounds = new PVector();

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
		if (primitiveType == PrimitiveType.CIRCLE) {
			return;
		}

		vertices = new ArrayList<>(baseVerts.size());
		for (PVector v : baseVerts) {
			PVector vn = v.copy();
			vn.rotate((float) parent.getOrientation());
			vertices.add(vn);
		}

		// Recompute AABB bounds
		List<PVector> aabb = Arrays.asList(new PVector(-baseAABB.x / 2, -baseAABB.y / 2),
				new PVector(-baseAABB.x / 2, baseAABB.y / 2), new PVector(baseAABB.x / 2, baseAABB.y / 2),
				new PVector(baseAABB.x / 2, -baseAABB.y / 2));
		PVector min = new PVector(Float.MAX_VALUE, Float.MAX_VALUE);
		PVector max = new PVector(-Float.MAX_VALUE, -Float.MAX_VALUE);
		for (PVector v : aabb) {
			v.rotate((float) parent.getOrientation());
			min.x = v.x < min.x ? v.x : min.x;
			min.y = v.y < min.y ? v.y : min.y;
			max.x = v.x > max.x ? v.x : max.x;
			max.y = v.y > max.y ? v.y : max.y;
		}
		AABBBounds = PVector.sub(max, min);
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
			return getCollision((Primitive) b, new PVector());
		} else if (b instanceof PrimitiveAssembly) {
			return getCollision((PrimitiveAssembly) b, new PVector());
		} else {
			return null;
		}
	}

	public List<PVector> getVertices() {
		if (primitiveType == PrimitiveType.CIRCLE) {
			return null;
		} else if (vertices != null) {
			return vertices;
		} else {
			return baseVerts;
		}
	}

	public double getRadius() {
		if (primitiveType == PrimitiveType.POLYGON) {
			return 0;
		}
		return radius;
	}

	public void enable(RigidBody parent) {
		if (this.parent == null) {
			this.parent = parent;
		}
	}

	public Primitive copy() {
		if (primitiveType == PrimitiveType.POLYGON) {
			List<PVector> newVertices = new ArrayList<>(baseVerts.size());
			for (PVector v : baseVerts) {
				newVertices.add(v.copy());
			}
			return new Primitive(surface, newVertices);
		} else {
			return new Primitive(surface, radius);
		}
	}

	/////////////////////
	// Private Methods //
	/////////////////////

	// Get full collision manifold for contact between 2 polygon primitives.
	private Manifold polygonToPolygonCollision(Primitive b, PVector offset) {
		System.out.println("First pass!");
		Manifold m = new Manifold(this.parent, b.getParent());
		m.combine(queryFaceDistance(b, offset));

		if (m.isCollision()) {
			Manifold m2 = new Manifold(b.getParent(), this.parent);
			m2.combine(b.queryFaceDistance(this, PVector.mult(offset, -1)));
			System.out.println("Second pass!");

			if (m2.isCollision()) {
				return m.getMaxPenetration() > m2.getMaxPenetration() ? m : m2;
			} else {
				return m2;
			}
		}
		return m;
	}

	// Query face distance between this polygon, from which the faces are checked,
	// against another polygon's vertices. Returns a collision manifold with a
	// potential collision point if there is one.
	private Manifold queryFaceDistance(Primitive b, PVector offset) {
		Manifold m = new Manifold(this.parent, b.getParent());

		System.out.println(
				"Query face distance: Object A: " + this.parent.getParent() + ", object B: " + b.parent.getParent());

		double minPenetration = -Double.MAX_VALUE;
		PVector bestNormal = new PVector();
		PVector bestSupport = new PVector();
		int bestIndex = 0;

		// Get best support point and add first point
		PVector v1min = new PVector(), v2min = new PVector();
		float reverseFactor = 1.0f;
		PVector v2 = PVector.add(this.vertices.get(this.vertices.size() - 1), this.parent.getPosition());
		boolean calibrating = true;
		System.out.println("Checking edges...");
		for (int i = 0; i < this.vertices.size(); i++) {
			PVector v1 = PVector.add(this.vertices.get(i), this.parent.getPosition());

			PVector v1v2 = PVector.sub(v1, v2);
			PVector normal = new PVector(-v1v2.y, v1v2.x).mult(reverseFactor).normalize();
			int supportIndex = b.polygonSupportPoint(PVector.mult(normal, -1));
			PVector support = PVector.add(b.vertices.get(supportIndex), b.parent.getPosition()).add(offset);
			double dist = PVector.dot(PVector.sub(support, v1), normal);
			System.out.println("\t v1: " + v1 + ", v2: " + v2 + ", normal: " + normal + ", support: " + support
					+ ", dist: " + dist);

			if (calibrating && dist > 0) {
				i--;
				reverseFactor = -1.0f;
				calibrating = false;
				continue;
			}
			if (dist > minPenetration) {
				if (dist > 0) {
					return m;
				}
				v1min = v1;
				v2min = v2;

				minPenetration = dist;
				bestNormal = normal;
				bestSupport = support;
				bestIndex = supportIndex;
			}
			v2 = v1;
			calibrating = false;
		}

		// Get incident face
		PVector adjacent0 = PVector
				.add(b.vertices.get((b.vertices.size() + bestIndex - 1) % b.vertices.size()), b.parent.getPosition())
				.add(offset);
		PVector adjacent1 = PVector.add(b.vertices.get((bestIndex + 1) % b.vertices.size()), b.parent.getPosition())
				.add(offset);
		double dot0 = Math.abs(PVector.dot(PVector.sub(adjacent0, bestSupport).normalize(), bestNormal)),
				dot1 = Math.abs(PVector.dot(PVector.sub(adjacent1, bestSupport).normalize(), bestNormal));
		PVector adjacent = dot1 > dot0 ? adjacent0 : adjacent1;
		PVector rayDir = PVector.sub(bestSupport, adjacent);

		m.addContactPoint(bestSupport, bestNormal, minPenetration, this, b);

		// Perform Sutherland Clipping
		PVector i1 = PVector.add(this.vertices.get(b.vertices.size() - 1), this.parent.getPosition());
		boolean isEdge = true;
		System.out.println("Before clip - v0: " + bestSupport + ", v1: " + adjacent);
		System.out.println("Primary clip -->");
		PVector clip = clip(rayDir, bestSupport, new Pair<>(v1min, v2min));
		if (clip != null) {
			adjacent = clip;
			isEdge = false;
			rayDir = PVector.sub(bestSupport, adjacent);
		}
		System.out.println("Secondary clip -->");
		for (PVector v : this.vertices) {
			PVector i0 = PVector.add(v, this.parent.getPosition());
			clip = clip(rayDir, bestSupport, new Pair<>(i0, i1));
			if (clip != null) {
				adjacent = clip;
				isEdge = true;
				rayDir = PVector.sub(bestSupport, adjacent);
			}
		}
		System.out.println("After clip - v0: " + bestSupport + ", v1: " + adjacent);
		System.out.println("Is edge? " + isEdge);
		if (isEdge) {
			m.addContactPoint(adjacent, bestNormal, PVector.dot(PVector.sub(adjacent, v1min), bestNormal), this, b);
		}

		return m;
	}

	// Clips the incidence's 2nd point
	private PVector clip(PVector dir, PVector p0, Pair<PVector> incident) {
		PVector i0i1 = PVector.sub(incident.b, incident.a);
		PVector normal = new PVector(-i0i1.y, i0i1.x).normalize();
		if (Math.abs(PVector.dot(normal, dir)) < PARRALLEL_THRESHOLD) {
			return null;
		}

		double t = (PVector.dot(normal, p0) - PVector.dot(normal, incident.a)) / PVector.dot(normal, dir);
		System.out.println("\t Clip: incident - <" + incident.a + ", " + incident.b + ">, t = " + t);
		if (t < 1 && t >= 0) {
			return PVector.add(p0, PVector.mult(dir, (float) t));
		} else {
			return null;
		}
	}

	// Get the support point of a circle primitive in a given direction.
	private PVector circleSupportPoint(PVector direction) {
		return PVector.add(this.parent.getPosition(), PVector.mult(direction, (float) this.radius));
	}

	// Get the support point of a polygon primitive in a given direction.
	private int polygonSupportPoint(PVector direction) {
		int max = 0;
		double maxDot = PVector.dot(vertices.get(0), direction);
		for (int i = 0; i < vertices.size(); i++) {
			double dot = vertices.get(i).dot(direction);
			if (dot > maxDot) {
				max = i;
				maxDot = dot;
			}
		}
		return max;
	}

	// Generate a circle-to-polygon collision, where b is the polygon primitive.
	private Manifold circleToPolygonCollision(Primitive b, PVector offset) {
		Manifold m = new Manifold(this.parent, b.parent);

		double minPenetration = -Double.MAX_VALUE;
		PVector bestNormal = new PVector();
		PVector bestSupport = new PVector();

		PVector v1min = new PVector(), v2min = new PVector();

		float reverseFactor = 1.0f;
		PVector v2 = PVector.add(b.vertices.get(b.vertices.size() - 1), b.parent.getPosition());
		boolean calibrating = true;
		for (int i = 0; i < b.vertices.size(); i++) {
			PVector v1 = PVector.add(b.vertices.get(i), b.parent.getPosition());

			PVector v1v2 = PVector.sub(v1, v2);
			PVector normal = new PVector(-v1v2.y, v1v2.x).mult(reverseFactor).normalize();
			PVector support = PVector.add(this.circleSupportPoint(PVector.mult(normal, -1)), offset);
			double dist = PVector.dot(PVector.sub(support, v1), normal);

			if (calibrating && dist > 0) {
				reverseFactor = -1.0f;
				i--;
				calibrating = false;
				continue;
			}
			if (dist > minPenetration) {
				if (dist > 0) {
					return m;
				}
				v1min = v1;
				v2min = v2;

				minPenetration = dist;
				bestNormal = normal;
				bestSupport = support;
			}
			v2 = v1;
			calibrating = false;
		}

		// Step 2 - Find the collision point with the edge
		PVector tan1 = PVector.sub(v1min, v2min);
		PVector tan2 = PVector.sub(v2min, v1min);

		// If the center of the mass is in the voronoi region of the edge
		PVector contactPoint = PVector.sub(this.parent.getPosition(), PVector.mult(bestNormal, (float) this.radius));
		if (PVector.dot(PVector.sub(this.parent.getPosition(), v1min), tan1) > 0
				&& PVector.dot(PVector.sub(this.parent.getPosition(), v2min), tan2) > 0) {
		} else {
			double distV0 = PVector.sub(this.parent.getPosition(), v1min).mag();
			double distV1 = PVector.sub(this.parent.getPosition(), v2min).mag();
			bestNormal = PVector.sub(this.parent.getPosition(), distV0 < distV1 ? v1min : v2min).normalize();
			contactPoint = PVector.sub(this.parent.getPosition(), PVector.mult(bestNormal, (float) this.radius));
			minPenetration = PVector.dot(PVector.sub(distV0 < distV1 ? v2min : v1min, contactPoint), bestNormal);
		}
		m.addContactPoint(contactPoint, bestNormal, minPenetration, this, b);

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
	public Primitive(Surface surface, List<PVector> vertices) {
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
		baseAABB = PVector.sub(max, min);
		AABBBounds = baseAABB.copy();

	}

	public Primitive(Surface surface, List<PVector> vertices, PVector AABBbounds) {
		this(surface, vertices);
		this.baseAABB = AABBbounds;
		this.AABBBounds = AABBbounds.copy();
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
		this.baseAABB = new PVector(2 * (float) radius, 2 * (float) radius);
		this.AABBBounds = baseAABB;
	}
}
