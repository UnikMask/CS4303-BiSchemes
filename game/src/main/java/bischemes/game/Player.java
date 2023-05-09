package bischemes.game;

import java.util.Map;
import java.util.Arrays;
import java.util.List;

import bischemes.engine.GObject;
import bischemes.engine.VisualUtils;
import bischemes.engine.*;
import bischemes.engine.physics.*;
import bischemes.engine.physics.ForceGenerators.DirectionalGravity;
import bischemes.game.InputHandler.InputCommand;
import bischemes.level.PlayerAbstract;
import processing.core.PVector;

public class Player extends PlayerAbstract {
	// Player Constants
	private static final double JUMP_INTENSITY = 8;
	public static final PVector PLAYER_SIZE = new PVector(0.75f, 1.8f);
	private static final double RUN_THRESHOLD = 0.01;
	private static final double WALL_DOT_THRESHOLD = 0.3;
	private static final double MIRROR_THRESHOLD = 0.01;
	private static final double WALL_MAX_TIMER = 0.2;
	private static final float WALL_JUMP_INTENSITY = 2;
	private static final String fpIdle = "char_idle.png";
	private static final List<String> fpRun = Arrays.asList("char_run2.png", "char_run3.png", "char_run1.png");
	private static final String fpJump = "char_jump.png";
	private static final String fpWall = "char_wall.png";

	// Sprite Constants for player animations
	private List<Integer> spritesRun;
	private int spriteIdle;
	private int spriteJump;
	private int spriteWall;
	private int color;

	enum PlayerState {
		IDLE, RUN, JUMP, WALL, FALL
	}

	private Integer playerVisuals;
	private PlayerState state = PlayerState.IDLE;
	private double tAnimation = 0;
	private PlayerMovement pMvt = new PlayerMovement(this, new PVector(1, 0));
	private DirectionalGravity gravity;
	private double wallTimer;
	private PVector wallNormal;

	/////////////////////
	// GObject Methods //
	/////////////////////

	@Override
	public void onHit(GObject hit, Manifold m) {
		// If the player is in the air
		if (state == PlayerState.JUMP || state == PlayerState.WALL) {
			PVector normal = m.objectB == getRigidBody() ? m.getNormal() : PVector.mult(m.getNormal(), -1);
			float projectedCollision = PVector.dot(normal, PVector.mult(gravity.getDirection(), -1));
			if (projectedCollision < -WALL_DOT_THRESHOLD) {
				// Hit a ceiling
				return;
			} else if (Math.abs(projectedCollision) < WALL_DOT_THRESHOLD) {
				// Hit a wall
				state = PlayerState.WALL;
				wallNormal = normal;
				wallTimer = 0;
			} else {
				// Hit floor
				state = Math.abs(
						PVector.dot(getRigidBody().getProperties().velocity, gravity.getTangent())) > RUN_THRESHOLD
								? PlayerState.RUN
								: PlayerState.IDLE;
			}
		}
	}

	@Override
	public void update() {
		// Get movement vector
		PVector movement = new PVector();
		double projectedGravity = PVector.dot(gravity.getDirection(), new PVector(0, -1));
		for (InputCommand c : InputHandler.getInstance().getHeldCommands()) {
			movement.add(switch (c) {
				case RIGHT -> projectedGravity > 0 ? new PVector(1, 0) : new PVector(-1, 0);
				case LEFT -> projectedGravity > 0 ? new PVector(-1, 0) : new PVector(1, 0);
				default -> new PVector();
			});

			// Deal with jump
			if (c == InputCommand.UP && state != PlayerState.JUMP) {
				PVector jumpForce = PVector.mult(PVector.add(PVector.mult(gravity.getDirection(), -1),
						state == PlayerState.WALL ? PVector.mult(wallNormal, WALL_JUMP_INTENSITY) : new PVector())
						.normalize(), (float) JUMP_INTENSITY);
				System.out.println(jumpForce);
				rigidBody.applyImpulse(PVector.mult(jumpForce, (float) rigidBody.getMass()),
						PVector.sub(position, new PVector(0, 0.45f)));
				state = PlayerState.JUMP;
			}
		}

		// Set running state
		PVector tangent = gravity.getTangent();
		double projectedVelocity = PVector.dot(getRigidBody().getProperties().velocity, tangent);
		if (Math.abs(projectedVelocity) > RUN_THRESHOLD && state == PlayerState.IDLE) {
			state = PlayerState.RUN;
		} else if (Math.abs(projectedVelocity) <= RUN_THRESHOLD && state == PlayerState.RUN) {
			state = PlayerState.IDLE;
		}
		if (Math.abs(PVector.dot(movement, tangent)) > RUN_THRESHOLD) {
			pMvt.setDirection(PVector.dot(movement, tangent) >= 0);
			pMvt.updateForce(getRigidBody());
		}

		// Wall-state check
		if (state == PlayerState.WALL) {
			wallTimer += 1.0 / 60.0;
			if (wallTimer > WALL_MAX_TIMER) {
				state = PlayerState.JUMP;
			}
		}

		// Mirror character accordingly
		VisualAttribute current = getVisualAttribute(playerVisuals);
		current.mirrorX = current.mirrorY ? projectedVelocity + MIRROR_THRESHOLD * (current.mirrorX ? 1 : -1) > 0
				: projectedVelocity + MIRROR_THRESHOLD * (current.mirrorX ? -1 : 1) < 0;
		current.mirrorY = PVector.dot(gravity.getDirection(), new PVector(0, 1)) > 0;

		// Set new animation frame
		applyFrame(getVisibleFrame());
		gravity.updateForce(getRigidBody());
	}

	@Override
	public PVector getGravityDirection() {
		return gravity.getDirection();
	}

	@Override
	public void setGravityDirection(PVector direction) {
		gravity.setDirection(direction);
	}

	public void setDirectionalGravity(DirectionalGravity g) {
		this.gravity = g;
	}

	public void setColour(int colour) {
		for (VisualAttribute v : visualAttributes) {
			v.setColour(colour);
		}
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
						.abs(PVector.dot(getRigidBody().getProperties().velocity, gravity.getTangent()));
				tAnimation += projectedVelocity / spritesRun.size() / 60;
				tAnimation %= spritesRun.size();
				int frame = (int) ((tAnimation * spritesRun.size()) % spritesRun.size());
				yield (spritesRun.get(frame));
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
		VisualAttribute a = VisualUtils.makeRect(new PVector(2, 2), color, EngineRuntime.applet.loadImage(fp));
		a.visible = false;
		a.setHighPriority(true);
		return addVisualAttributes(a).get(0);
	}

	//////////////////
	// Constructors //
	//////////////////

	// Constructor for a player.
	public Player(PVector position, float orientation, DirectionalGravity gravity, int color) {
		super(null, position, orientation);
		setRigidBody(new RigidBody(
				new RigidBodyProperties(Map.of("mass", 35.0, "inertia", 20.0, "move", true, "rotate", false, "mesh",
						new Primitive(new Surface(0.2, 1.0, 1.0), PrimitiveUtils.makeRect(PLAYER_SIZE))))));
		this.gravity = gravity;
		this.color = color;

		// Generate sprites
		spriteIdle = generateSprite(fpIdle);
		spriteJump = generateSprite(fpJump);
		spritesRun = fpRun.stream().map(fp -> generateSprite(fp)).toList();
		spriteWall = generateSprite(fpWall);
		applyFrame(getVisibleFrame());
	}

}
