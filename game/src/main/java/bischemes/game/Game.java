package bischemes.game;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayDeque;

import bischemes.engine.*;
import bischemes.game.InputHandler.InputCommand;
import bischemes.engine.physics.*;
import bischemes.engine.physics.ForceGenerators.DirectionalGravity;
import bischemes.game.Game.GameState;
import bischemes.level.parts.*;
import bischemes.level.*;
import bischemes.level.util.LColour;
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
				// Check for interact input
				if (InputHandler.getInstance().getPressedCommands().contains(InputCommand.INTERACT)) {
					currentRoom.interact();
				}

				// Update forces on grav item
				setEngineCameraPosition();
				if (InputHandler.getInstance().hasInteraction()) currentRoom.interact();
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

		loadRoom(initRoom, initRoom.getSpawnPosition());
		secondaryScene.attachToGObject(secondaryScene.scene, player);

		level.setGame(this);

		engine.setPause(false);
	}

	public void removeRigidBody(RigidBody r, LColour l) {
		if (l == LColour.PRIMARY) primaryScene.removeRigidBody(r);
		else secondaryScene.removeRigidBody(r);
	}

	public void addRigidBody(RigidBody r, LColour l) {
		if (l == LColour.PRIMARY) primaryScene.addRigidBody(r);
		else secondaryScene.addRigidBody(r);
	}

	public void loadRoom(Room room, PVector playerPosition) {

		// Initialise scenes
		currentRoom = room;
		PVector extraDimensions = PVector.add(room.getDimensions(), new PVector(2, 2));

		// Load primary scene
		primaryScene.scene = new GObject(null, new PVector(), 0);
		primaryScene.grid = new GridSector(extraDimensions, new PVector(-1, -1),
				(int) extraDimensions.x, (int) extraDimensions.y);
		VisualAttribute primaryBg = VisualUtils.makeRect(room.getDimensions(),
				colours.a);
		primaryBg.setOffset(PVector.div(room.getDimensions(), 2));
		primaryScene.scene.addVisualAttributes(primaryBg);

		// Load secondary scene
		secondaryScene.scene = new GObject(null, new PVector(), 0);
		secondaryScene.grid = new GridSector(extraDimensions, new PVector(-1, -1),
				(int) extraDimensions.x, (int) extraDimensions.y);

		// Load player
		if (player == null) {
			player = new Player(playerPosition, 0, isPrimaryScene? gravities.a: gravities.b, level.getColourSecondary());
		} else {
			player.setLocalPosition(playerPosition);
		}
		if (isPrimaryScene) {
			primaryScene.attachToGObject(primaryScene.scene, player);
		} else {
			secondaryScene.attachToGObject(secondaryScene.scene, player);
		}

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
		engine.setPause(true);
		loadRoom(room, PVector.add(newPlayerPosition, new PVector(1, 0)));
		engine.setPause(false);
	}

	// Alex TODO when called this should switch the player's colour
	public void switchPlayerColour() {
		//isPrimaryScene = !isPrimaryScene;
	}

	//
	// Alex TODO when called this should return the user to the MapUI (set RunnerState to MENU)
	public void completeLevel() {
		level.setCompleted(true);
		state = GameState.END;
	}

	//////////////////
	// Constructors //
	//////////////////

	public Game(PApplet applet, PGraphics g) {
		engine = new EngineRuntime(applet, g);
		setup();
	}

}
