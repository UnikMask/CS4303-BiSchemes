package bischemes.engine.physics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import bischemes.engine.physics.PrimitiveAssembly.PrimitiveInSet;
import processing.core.PVector;

/**
 * Uniform grid for coarse-grained collision detection
 */
public class GridSector {
	private PVector dimensions;
	private PVector sizePerCell;
	private PVector position;

	private int nrows;
	private int ncols;

	/**
	 * Primitive as stored in the grid.
	 */
	class PrimitiveStore {
		Primitive p;
		PVector position;
		PVector AABBbounds;

		@Override
		public int hashCode() {
			return p.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof PrimitiveStore)) {
				return false;
			}
			PrimitiveStore p = (PrimitiveStore) o;
			return p.p == this.p;
		}

		public PrimitiveStore(Primitive p) {
			this.p = p;
			this.position = p.getParent().getPosition();
			this.AABBbounds = p.getAABBBounds();
		}

		public PrimitiveStore(PrimitiveInSet p) {
			this.p = p.primitive;
			this.position = PVector.add(p.primitive.getParent().getPosition(), p.offset);
			this.AABBbounds = p.primitive.getAABBBounds();
		}
	}

	/**
	 * Contents of a grid cell.
	 */
	class GridCell {
		HashSet<PrimitiveStore> list = new HashSet<>();

		public boolean hasOverlap() {
			return list.size() > 1;
		}
	}

	// Lists making up grid storage
	private ArrayList<GridCell> sector;
	private HashSet<GridCell> overlaps = new HashSet<>();
	private HashMap<PrimitiveStore, ArrayList<GridCell>> primitives = new HashMap<>();

	public void add(Primitive p) {
		add(new PrimitiveStore(p));
	}

	public void add(PrimitiveInSet p) {
		add(new PrimitiveStore(p));
	}

	private void add(PrimitiveStore p) {
		if (primitives.containsKey(p)) {
			return;
		}
		primitives.put(p, new ArrayList<>());
		for (int i = (int) (p.position.x - position.x - p.AABBbounds.x / 2); i <= (int) (p.position.x - position.x
				+ p.AABBbounds.x / 2); i++) {
			for (int j = (int) (p.position.y - position.y - p.AABBbounds.y / 2); i <= (int) (p.position.y - position.y
					+ p.AABBbounds.y / 2); i++) {
				if (i < ncols && j < nrows) {
					int index = j * ncols + i;
					GridCell c = sector.get(index);
					c.list.add(p);
					primitives.get(p).add(c);
					if (c.hasOverlap() && !overlaps.contains(c)) {
						overlaps.add(c);
					}
				}
			}

		}
	}

	public void move(Primitive p) {
		remove(p);
		add(p);
	}

	public void move(PrimitiveInSet p) {
		remove(p);
		add(p);
	}

	public void remove(Primitive p) {
		remove(new PrimitiveStore(p));
	}

	public void remove(PrimitiveInSet p) {
		remove(new PrimitiveStore(p));

	}

	private void remove(PrimitiveStore p) {
		for (GridCell c : primitives.get(p)) {
			c.list.remove(p);
			if (overlaps.contains(c) && !c.hasOverlap()) {
				overlaps.remove(c);
			}
		}
		primitives.remove(p);
	}

	public ArrayList<Manifold> getCollisions() {
		ArrayList<Manifold> manifolds = new ArrayList<>(overlaps.size());
		return manifolds;
	}

	/**
	 * Constructor for a grid sector.
	 *
	 * @param dimensions The dimensions of the sector (in metric units)
	 * @param nrows      The number of rows in the sector.
	 * @param ncols      The number of columns in the sector.
	 */
	public GridSector(PVector dimensions, int nrows, int ncols) {
		this.dimensions = dimensions;
		this.nrows = nrows;
		this.ncols = ncols;
		sector = new ArrayList<>(nrows * ncols);
		for (int i = 0; i < nrows * ncols; i++) {
			sector.add(new GridCell());
		}
		sizePerCell = new PVector(dimensions.x / (float) ncols, dimensions.y / (float) nrows);
	}
}
