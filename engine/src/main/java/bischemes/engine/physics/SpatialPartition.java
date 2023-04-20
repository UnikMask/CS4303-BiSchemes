package bischemes.engine.physics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bischemes.engine.GObject;
import processing.core.PVector;

/**
 * A quad-tree based spatial partition of a scene
 */
public class SpatialPartition {
	GObject scene;
	HashMap<RigidBody, List<QuadTreeNode>> objects = new HashMap<>();
	QuadTreeNode quad;

	static class PrimitiveStore {
		Primitive p;
		PVector offset;

		public PrimitiveStore(Primitive p, PVector offset) {
			this.p = p;
			this.offset = offset;
		}
	}

	class QuadTreeNode {
		PVector position;
		PVector size;

		QuadTreeNode children[];

		public boolean isLeaf() {
			return children == null;
		}

		public void createChildren() {
			PVector newSize = PVector.div(size, 2);
			PVector incr = PVector.div(size, 4);
			children = new QuadTreeNode[] { new QuadTreeNode(new PVector(-incr.x, -incr.y), newSize),
					new QuadTreeNode(new PVector(incr.x, -incr.y), newSize),
					new QuadTreeNode(new PVector(-incr.x, incr.y), newSize),
					new QuadTreeNode(new PVector(incr.x, incr.y), newSize) };
		}

		public int getChildIndex(PrimitiveStore ps) {
			return 0;
		}

		public List<QuadTreeNode> add(PrimitiveStore ps) {
			return null;
		}

		public QuadTreeNode(PVector position, PVector size) {
			this.position = position;
			this.size = size;
		}
	}

	public void add(RigidBody obj) {
		objects.put(obj, new ArrayList<>());
		for (PrimitiveStore ps : obj.properties.mesh.getPrimitiveStores()) {
			addToQuad(ps);
		}
	}

	public void addToQuad(PrimitiveStore ps) {

	}
}
