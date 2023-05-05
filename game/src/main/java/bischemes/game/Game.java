package bischemes.game;

import java.util.Arrays;
import java.util.Map;

import bischemes.engine.EngineRuntime;
import bischemes.engine.GObject;
import bischemes.engine.SceneGridPair;
import bischemes.engine.VisualUtils;
import bischemes.engine.physics.GridSector;
import bischemes.engine.physics.Primitive;
import bischemes.engine.physics.PrimitiveUtils;
import bischemes.engine.physics.RigidBody;
import bischemes.engine.physics.RigidBodyProperties;
import bischemes.engine.physics.Surface;
import bischemes.engine.physics.ForceGenerators.DirectionalGravity;
import bischemes.engine.physics.ForceGenerators.ForceGenerator;
import bischemes.game.Game.GameState;
import bischemes.game.InputHandler.InputCommand;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class Game {
	EngineRuntime engine;
	GameState state = GameState.PLAY;

	SceneGridPair mainScene;

	GObject demoGravItem;
	ForceGenerator gravity = new DirectionalGravity(1.0, new PVector(0, -1, 0));
	// ForceGenerator gravity = new DirectionalGravity();

	// States of a level/game - feel free to modify
	enum GameState {
		PAUSE, PLAY, INTRO, FINISH, END
	}

	public void update(PGraphics g) {
		// Update behaviour per state
		switch (state) {
		case PAUSE:
			break;
		case PLAY:
			// Update forces on grav item
			gravity.updateForce(demoGravItem.getRigidBody());
			engine.update();
			break;
		case INTRO:
			break;
		case FINISH:
			break;
		case END:
		}
	}

	public void setup() {
		engine.setCameraBounds(new PVector(16, 9));
		mainScene = new SceneGridPair(new GObject(null, new PVector(), 0),
				new GridSector(new PVector(16, 9), new PVector(-8, -4.5f), 16, 9));
		mainScene.scene.addVisualAttributes(VisualUtils.makeRect(new PVector(16, 9), 0xffffc857));

		// Create an obstacle
		GObject obstacles = new GObject(null, new PVector(0, -3.5f), 0);
		obstacles.setRigidBody(new RigidBody(new RigidBodyProperties(
				Map.of("mesh", new Primitive(new Surface(0, 0.2, 0.2), PrimitiveUtils.makeRect(new PVector(16, 2)))))));
		obstacles.addVisualAttributes(VisualUtils.makeRect(new PVector(16, 2), 0xff54494b));
		mainScene.attachToGObject(mainScene.scene, obstacles);

		// Create an item that will fall down on the floor
		demoGravItem = new GObject(null, new PVector(0, 0), 0);
		Primitive demoGravPrim = new Primitive(new Surface(0.5, 0.05, 0.05),
				PrimitiveUtils.makeRect(new PVector(1, 1)));
		demoGravItem.setRigidBody(new RigidBody(new RigidBodyProperties(
				Map.of("mass", 15.0, "inertia", PrimitiveUtils.getPrimitiveInertia(demoGravPrim, 15.0, new PVector()),
						"rotate", true, "move", true, "mesh", demoGravPrim))));
		demoGravItem.addVisualAttributes(VisualUtils.makeRect(new PVector(1, 1), 0xff54494b));
		mainScene.attachToGObject(mainScene.scene, demoGravItem);

		// Attach the scene to the engine & start the simulation
		engine.attachScene(mainScene);
		engine.setPause(false);
	}

	public Game(PApplet applet, PGraphics g) {
		engine = new EngineRuntime(applet, applet.getGraphics());
		setup();
	}
}
