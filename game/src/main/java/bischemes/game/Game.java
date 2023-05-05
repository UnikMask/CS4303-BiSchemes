package bischemes.game;

import java.util.Arrays;
import java.util.Map;

import bischemes.engine.EngineRuntime;
import bischemes.engine.GObject;
import bischemes.engine.SceneGridPair;
import bischemes.engine.VisualUtils;
import bischemes.engine.physics.GridSector;
import bischemes.engine.physics.Primitive;
import bischemes.engine.physics.RigidBody;
import bischemes.engine.physics.RigidBodyProperties;
import bischemes.engine.physics.Surface;
import bischemes.game.Game.GameState;
import bischemes.game.InputHandler.InputCommand;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class Game {
	EngineRuntime engine;
	GameState state = GameState.PLAY;

	SceneGridPair mainScene;

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
			engine.update();
			PVector camMovement = new PVector();
			for (InputCommand c : InputHandler.getInstance().getHeldCommands()) {
				camMovement.add(switch (c) {
				case UP -> new PVector(0, 0.1f);
				case DOWN -> new PVector(0, -0.1f);
				case LEFT -> new PVector(-0.1f, 0);
				case RIGHT -> new PVector(0.1f, 0);
				default -> new PVector();
				});
			}
			if (Math.abs(camMovement.x) > 0.01f || Math.abs(camMovement.y) > 0.01f) {
				engine.setCameraPosition(PVector.add(engine.getCameraPosition(), camMovement));
			}
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
		engine.setCameraPosition(new PVector(0, 9));
		mainScene = new SceneGridPair(new GObject(null, new PVector(), 0),
				new GridSector(new PVector(16, 9), new PVector(), 16, 9));

		Primitive rect = new Primitive(new Surface(0, 0, 0), Arrays.asList(new PVector(-8, -4.5f),
				new PVector(-8, 4.5f), new PVector(8, 4.5f), new PVector(8, -4.5f)));

		mainScene.scene.setRigidBody(new RigidBody(new RigidBodyProperties(Map.of("mesh", rect))));
		mainScene.scene.addVisualAttributes(VisualUtils.makeRect(new PVector(16, 9), 0xff2e2a2b));
		engine.attachScene(mainScene);
	}

	public Game(PApplet applet, PGraphics g) {
		engine = new EngineRuntime(applet, applet.getGraphics());
		setup();
	}
}
