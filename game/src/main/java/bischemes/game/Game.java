package bischemes.game;

import java.util.Arrays;
import java.util.List;

import bischemes.engine.*;
import bischemes.engine.physics.*;
import bischemes.engine.physics.ForceGenerators.DirectionalGravity;
import bischemes.game.Game.GameState;
import bischemes.level.Level;
import bischemes.level.Room;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class Game {
	EngineRuntime engine;
	GameState state = GameState.PLAY;

	Player player;
	DirectionalGravity gravity;
	boolean playerInPrimary = true;
	Level level;
	SceneGridPair primaryScene;
	SceneGridPair secondaryScene;
	Pair<Integer> colours;
	List<Room> rooms;
	Room currentRoom;

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
				setEngineCameraPosition();
				engine.update();
				gravity.updateForce(player.getRigidBody());
				break;
			case INTRO:
				break;
			case FINISH:
				break;
			case END:
		}
	}

	public void setEngineCameraPosition() {
		PVector minCameraPosition = PVector.add(new PVector(), PVector.div(engine.getCameraBounds(), 2));
		PVector maxCameraPosition = PVector.sub(currentRoom.getDimensions(), PVector.div(engine.getCameraBounds(), 2));
		PVector newPosition = new PVector(
				Math.min(Math.max(minCameraPosition.x, player.getPosition().x), maxCameraPosition.x),
				Math.min(Math.max(minCameraPosition.y, player.getPosition().y), maxCameraPosition.y));
		engine.setCameraPosition(PVector.lerp(engine.getCameraPosition(), newPosition, 0.1f));
	}

	public void setup() {
		engine.setCameraBounds(new PVector(16, 9));
		primaryScene = new SceneGridPair(new GObject(null, new PVector(), 0), null);
		secondaryScene = new SceneGridPair(new GObject(null, new PVector(), 0), null);
		engine.attachScene(primaryScene);
		engine.attachScene(secondaryScene);
	}

	public void setLevel(Level level) {
		this.level = level;
		rooms = Arrays.asList(level.getRooms());
		colours = new Pair<>(level.getColourPrimary(), level.getColourSecondary());

		Room initRoom = level.getInitRoom();
		loadRoom(initRoom);
		gravity = new DirectionalGravity();
		player = new Player(initRoom.getSpawnPosition(), 0, gravity, level.getColourSecondary());
		primaryScene.attachToGObject(primaryScene.scene, player);
	}

	public void loadRoom(Room room) {
		// Immediate room loading
		if (currentRoom == null) {
			// Initialise scenes
			currentRoom = room;
			primaryScene.grid = new GridSector(room.getDimensions(), new PVector(),
					(int) room.getDimensions().x, (int) room.getDimensions().y);
			VisualAttribute primaryBg = VisualUtils.makeRect(room.getDimensions(), colours.a);
			primaryBg.setOffset(PVector.div(room.getDimensions(), 2));
			//primaryScene.scene.addVisualAttributes(primaryBg);
			secondaryScene.grid = new GridSector(room.getDimensions(), new PVector(),
					(int) room.getDimensions().x, (int) room.getDimensions().y);

			// Add geometries to both scenes
			primaryScene.attachToGObject(primaryScene.scene, room.getPrimaryGeometry());
			secondaryScene.attachToGObject(secondaryScene.scene, room.getSecondaryGeometry());
		}
		engine.setPause(false);
	}

	public Game(PApplet applet, PGraphics g) {
		engine = new EngineRuntime(applet, g);
		setup();
	}

}
