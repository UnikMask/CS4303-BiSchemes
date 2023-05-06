package bischemes.game;

import java.util.Map;

import bischemes.engine.GObject;
import bischemes.engine.VisualUtils;
import bischemes.engine.physics.Primitive;
import bischemes.engine.physics.PrimitiveUtils;
import bischemes.engine.physics.RigidBody;
import bischemes.engine.physics.RigidBodyProperties;
import bischemes.engine.physics.Surface;
import bischemes.game.InputHandler.InputCommand;
import processing.core.PVector;

public class Player extends GObject {
	private static final PVector mvtForce = new PVector(30, 0);
	private static final PVector JUMP_FORCE = new PVector(0, 5);
	private static final double RUN_THRESHOLD = 0.01;

	enum PlayerState {
		IDLE, RUN, JUMP, WALL, FALL
	}

	private PlayerState state = PlayerState.IDLE;

	@Override
	public void update() {
		// TODO Player Controls
		PVector movement = new PVector();
		for (InputCommand c : InputHandler.getInstance().getHeldCommands()) {
			movement.add(switch (c) {
			case RIGHT -> mvtForce;
			case LEFT -> PVector.mult(mvtForce, -1);
			default -> new PVector();
			});

			// Deal with jump
			if (c == InputCommand.UP && state != PlayerState.JUMP && state != PlayerState.WALL) {
				state = PlayerState.JUMP;
				rigidBody.applyImpulse(PVector.mult(JUMP_FORCE, (float) rigidBody.getMass()), position);
			}
		}

		// Set running state
		if (Math.abs(movement.x) > RUN_THRESHOLD && state == PlayerState.IDLE) {
			state = PlayerState.RUN;
		} else if (Math.abs(movement.x) <= RUN_THRESHOLD && state == PlayerState.RUN) {
			state = PlayerState.IDLE;
		}
		rigidBody.addForce(PVector.mult(movement, (float) rigidBody.getMass()));
	}

	public Player(PVector position, float rotation) {
		super(null, position, rotation);
		setRigidBody(new RigidBody(new RigidBodyProperties(Map.of("mass", 35.0, "inertia", 20.0, "move", true, "rotate",
				true, "mesh", new Primitive(new Surface(0, 2.0, 1.0), PrimitiveUtils.makeRect(new PVector(1, 1)))))));
		addVisualAttributes(VisualUtils.makeRect(new PVector(1, 1), 0xff54494b));
	}

}
