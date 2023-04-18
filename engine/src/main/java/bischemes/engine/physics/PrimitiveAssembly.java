package bischemes.engine.physics;

import processing.core.PVector;
import java.util.List;

public class PrimitiveAssembly {
	private List<PrimitiveInSet> assembly;

	class PrimitiveInSet {
		Primitive primitive;
		PVector offset;
	}

}
