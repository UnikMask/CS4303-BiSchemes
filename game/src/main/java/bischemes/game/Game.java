package bischemes.game;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayDeque;

import bischemes.engine.*;
import bischemes.engine.physics.*;
import bischemes.engine.physics.ForceGenerators.DirectionalGravity;
import bischemes.game.Game.GameState;
import bischemes.level.parts.*;
import bischemes.level.*;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class Game implements GameInterface {
	// Constants
	private static final double TRANSITION_DURATION = 0.5;

	EngineRuntime engine;
	GameState state = GameState.PLAY;

	// General game components
	Player player;
	boolean playerInPrimary = true;
	Level level;
	SceneGridPair primaryScene;
	SceneGridPair secondaryScene;
	Pair<Integer> colours;
	List<Room> rooms;
	Room currentRoom;
	GObject primaryNode;
	boolean isPrimaryScene;
	Pair<DirectionalGravity> gravities;

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
		level.initialiseRooms();
		rooms = Arrays.asList(level.getRooms());
		colours = new Pair<>(level.getColourPrimary(), level.getColourSecondary());

		Room initRoom = level.getInitRoom();
		gravities = new Pair<>(new DirectionalGravity(new PVector(0, 1)), new DirectionalGravity(new PVector(0, -1)));
		player = new Player(initRoom.getSpawnPosition(), 0, gravities.b, level.getColourSecondary());

		loadRoom(initRoom);
		secondaryScene.attachToGObject(secondaryScene.scene, player);

		level.setGame(this);

		engine.setPause(false);
	}

	public void loadRoom(Room room) {
		// Initialise scenes
		currentRoom = room;
		PVector extraDimensions = PVector.add(room.getDimensions(), new PVector(2, 2));
		primaryScene.grid = new GridSector(extraDimensions, new PVector(-1, -1),
				(int) extraDimensions.x, (int) extraDimensions.y);
		primaryNode = new GObject(null, PVector.div(room.getDimensions(), 2), 0);
		VisualAttribute primaryBg = VisualUtils.makeRect(room.getDimensions(),
				colours.a);
		// primaryNode.addVisualAttributes(primaryBg); //TODO re-add bg
		primaryScene.attachToGObject(primaryScene.scene, primaryNode);
		secondaryScene.grid = new GridSector(extraDimensions, new PVector(-1, -1),
				(int) extraDimensions.x, (int) extraDimensions.y);

		// Add geometries to both scenes
		primaryScene.attachToGObject(primaryScene.scene, room.getPrimaryGeometry());
		secondaryScene.attachToGObject(secondaryScene.scene, room.getSecondaryGeometry());

		// Initialise and load objects into the game
		ArrayDeque<RObject> q = new ArrayDeque<>(room.getObjects());
		while (!q.isEmpty()) {
			RObject o = q.pollFirst();
			if (o.getLColour() == null) {
				q.addAll(o.getChildren().stream().filter((go) -> go instanceof RObject).map((go) -> (RObject) go)
						.toList());
			} else {
				switch (o.getLColour()) {
					case PRIMARY -> {
						o.init(player, gravities.a);
						primaryScene.attachToGObject(primaryScene.scene, o);
					}
					case SECONDARY -> {
						o.init(player, gravities.b);
						secondaryScene.attachToGObject(secondaryScene.scene, o);
					}
				}
			}
		}
	}


	public void loadNextRoom(Room room, PVector newPlayerPosition) {

	}

	// Alex TODO when called this should switch the player's colour
	public void switchPlayerColour() {

	}

	//
	// Alex TODO when called this should return the user to the MapUI (set RunnerState to MENU)
	public void completeLevel() {
	//
		level.setCompleted(true);
		//TODO rest of the method :)
	}

	/////////////////
	// Destructors //
	/////////////////

	private void deconstructCurrentRoom() {
		primaryScene = null;
		secondaryScene = null;
		primaryNode = null;
		currentRoom = null;
	}

	//////////////////
	// Constructors //
	//////////////////

	public Game(PApplet applet, PGraphics g) {
		engine = new EngineRuntime(applet, g);
		setup();
	}

}
