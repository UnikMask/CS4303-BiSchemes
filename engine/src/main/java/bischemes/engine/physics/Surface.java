package bischemes.engine.physics;

/**
 * A primitive's surface's physical properties
 */
public class Surface {
	double restitution;
	double staticFriction;
	double dynamicFriction;

	public double getRestitution() {
		return restitution;
	}

	public double getStaticFriction() {
		return staticFriction;
	}

	public double getDynamicFriction() {
		return dynamicFriction;
	}

	public Surface(double restitution, double staticFriction, double dynamicFriction) {
		this.restitution = restitution;
		this.staticFriction = staticFriction;
		this.dynamicFriction = dynamicFriction;
	}
}
