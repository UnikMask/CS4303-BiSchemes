package bischemes.game;

import java.util.Map;
import java.util.Arrays;
import java.util.List;

import bischemes.engine.GObject;
import bischemes.engine.VisualUtils;
import bischemes.engine.*;
import bischemes.engine.physics.*;
import bischemes.game.InputHandler.InputCommand;
import processing.core.PVector;

public class Player extends GObject {
	// Player Constants
	private static final PVector BASE_MOVEMENT_INTENSITY = new PVector(30, 0);
	private static final PVector JUMP_FORCE = new PVector(0, 5);
	private static final double RUN_THRESHOLD = 0.01;
	private static final double WALL_DOT_THRESHOLD = 0.3;
	private static final double MIRROR_THRESHOLD = 0.1;
	private static final String fpIdle = "char_idle.png";
	private static final List<String> fpRun = Arrays.asList("char_run2.png", "char_run3.png", "char_run1.png",
			"char_run1.png");
	private static final String fpJump = "char_jump.png";
	private static final String fpWall = "char_wall.png";

	// Sprite Constants for player animations
	private List<Integer> spritesRun;
	private int spriteIdle;
	private int spriteJump;
	private int spriteWall;

	enum PlayerState {
		IDLE, RUN, JUMP, WALL, FALL
	}

	private Integer playerVisuals;
	private PlayerState state = PlayerState.IDLE;
	private double tAnimation = 0;

	/////////////////////
	// GObject Methods //
	/////////////////////

	@Override
	public void onHit(GObject hit, Manifold m) {
		if (state == PlayerState.JUMP) {
			if (Math.abs(PVector.dot(m.getNormal(), new PVector(0, 1))) < WALL_DOT_THRESHOLD) {
				state = PlayerState.WALL;
			} else {
				state = Math.abs(getRigidBody().getProperties().velocity.x) > RUN_THRESHOLD ? PlayerState.RUN
						: PlayerState.IDLE;
			}
		}
	}

	@Override
	public void update() {
		// Get movement vector
		PVector movement = new PVector();
		for (InputCommand c : InputHandler.getInstance().getHeldCommands()) {
			movement.add(switch (c) {
			case RIGHT -> BASE_MOVEMENT_INTENSITY;
			case LEFT -> PVector.mult(BASE_MOVEMENT_INTENSITY, -1);
			default -> new PVector();
			});

			// Deal with jump
			if (c == InputCommand.UP && state != PlayerState.JUMP && state != PlayerState.WALL) {
				state = PlayerState.JUMP;
				rigidBody.applyImpulse(PVector.mult(JUMP_FORCE, (float) rigidBody.getMass()),
						PVector.sub(position, new PVector(0, 0.45f)));
			}
		}

		// Set running state
		double projectedVelocity = PVector.dot(getRigidBody().getProperties().velocity, new PVector(1, 0));
		if (Math.abs(projectedVelocity) > RUN_THRESHOLD && state == PlayerState.IDLE) {
			state = PlayerState.RUN;
		} else if (Math.abs(projectedVelocity) <= RUN_THRESHOLD && state == PlayerState.RUN) {
			state = PlayerState.IDLE;
		}
		rigidBody.addForce(PVector.mult(movement, (float) rigidBody.getMass()));

		// Mirror character accordingly
		VisualAttribute current = getVisualAttribute(playerVisuals);
		current.mirrorX = movement.x + MIRROR_THRESHOLD * (current.mirrorX ? -1 : 1) < 0;

		// Set new animation frame
		applyFrame(getVisibleFrame());
	}

	/////////////////////
	// Private Methods //
	/////////////////////

	// Get the player's visible frame index.
	private int getVisibleFrame() {
		return switch (state) {
		case IDLE -> spriteIdle;
		case RUN -> {
			double projectedVelocity = Math
					.abs(PVector.dot(getRigidBody().getProperties().velocity, new PVector(1, 0)));
			tAnimation += projectedVelocity / spritesRun.size() / 60;
			tAnimation %= spritesRun.size();
			System.out.println("tAnimation = " + tAnimation);
			int frame = (int) ((tAnimation * spritesRun.size()) % spritesRun.size());
			yield(spritesRun.get(frame));
		}
		case JUMP -> spriteJump;
		case WALL -> spriteWall;
		case FALL -> spriteJump;
		};
	}

	// Apply a new frame to the player.
	private void applyFrame(int frame) {
		if (playerVisuals == null) {
			getVisualAttribute(frame).visible = true;
			playerVisuals = frame;
		} else if (playerVisuals != frame) {
			getVisualAttribute(playerVisuals).visible = false;
			getVisualAttribute(frame).visible = true;
			getVisualAttribute(frame).mirrorX = getVisualAttribute(playerVisuals).mirrorX;
			getVisualAttribute(frame).mirrorY = getVisualAttribute(playerVisuals).mirrorY;
			playerVisuals = frame;
		}
	}

	// Generate a sprite for the player and add it to its list of visual attributes.
	private int generateSprite(String fp) {
		VisualAttribute a = VisualUtils.makeRect(new PVector(1, 1), 0xff54494b, EngineRuntime.applet.loadImage(fp));
		a.visible = false;
		return addVisualAttributes(a).get(0);
	}

	//////////////////
	// Constructors //
	//////////////////

	// Constructor for a player.
	public Player(PVector position, float orientation) {
		super(null, position, orientation);
		setRigidBody(new RigidBody(
				new RigidBodyProperties(Map.of("mass", 35.0, "inertia", 20.0, "move", true, "rotate", false, "mesh",
						new Primitive(new Surface(0, 2.0, 1.0), PrimitiveUtils.makeRect(new PVector(0.4f, 1)))))));

		// Generate sprites
		spriteIdle = generateSprite(fpIdle);
		spriteJump = generateSprite(fpJump);
		spritesRun = fpRun.stream().map(fp -> generateSprite(fp)).toList();
		spriteWall = generateSprite(fpWall);
		applyFrame(getVisibleFrame());
	}

}
