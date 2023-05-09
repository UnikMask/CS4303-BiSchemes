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
	DirectionalGravity gravity;
	boolean playerInPrimary = true;
	Level level;
	SceneGridPair primaryScene;
	SceneGridPair secondaryScene;
	Pair<Integer> colours;
	List<Room> rooms;
	Room currentRoom;
	GObject primaryNode;
	boolean isPrimaryScene;

	// Transition components
	Room nextRoom;
	GObject nextRoomNode;
	PVector previousPlayerPosition;
	PVector targetPlayerPosition;
	PVector previousCameraPosition;
	PVector targetCameraPosition;
	double timer = 0;

	// States of a level/game - feel free to modify
	enum GameState {
		PAUSE, PLAY, TRANSITION, INTRO, FINISH, END
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
			case TRANSITION:
				timer += (1.0 / 60.0) / TRANSITION_DURATION;

				if (timer < 1) {
					engine.setCameraPosition(PVector.lerp(previousCameraPosition, targetCameraPosition, (float) timer));
					player.setLocalPosition(PVector.lerp(previousPlayerPosition, targetPlayerPosition, (float) timer));
				} else {
					unloadLastRoom();
					timer = 0;
				}
				engine.update();
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
		gravity = new DirectionalGravity();
		player = new Player(initRoom.getSpawnPosition(), 0, gravity, level.getColourSecondary());
		loadRoom(initRoom);
		secondaryScene.attachToGObject(secondaryScene.scene, player);

		level.setGameInterface(this);
		level.setPlayer(player);

		engine.setPause(false);
	}

	public void loadRoom(Room room) {
		// Initialise scenes
		currentRoom = room;
		primaryScene.grid = new GridSector(room.getDimensions(), new PVector(),
				(int) room.getDimensions().x, (int) room.getDimensions().y);
		primaryNode = new GObject(null, PVector.div(room.getDimensions(), 2), 0);
		VisualAttribute primaryBg = VisualUtils.makeRect(room.getDimensions(),
				colours.a);
		// primaryNode.addVisualAttributes(primaryBg); //TODO re-add bg
		primaryScene.attachToGObject(primaryScene.scene, primaryNode);
		secondaryScene.grid = new GridSector(room.getDimensions(), new PVector(),
				(int) room.getDimensions().x, (int) room.getDimensions().y);

		// Add geometries to both scenes
		primaryScene.attachToGObject(primaryScene.scene, room.getPrimaryGeometry());
		secondaryScene.attachToGObject(secondaryScene.scene, room.getSecondaryGeometry());

		// Load objects into the game
		ArrayDeque<RObject> q = new ArrayDeque<>(room.getObjects());
		while (!q.isEmpty()) {
			RObject o = q.pollFirst();
			if (o.getLColour() == null) {
				q.addAll(o.getChildren().stream().map((go) -> (RObject) go).toList());
			} else {
				switch (o.getLColour()) {
					case PRIMARY -> {
						primaryScene.attachToGObject(primaryScene.scene, o);
					}
					case SECONDARY -> {
						secondaryScene.attachToGObject(secondaryScene.scene, o);
					}
				}
			}
		}
	}

	// Alex TODO when called this method should switch the current room
	public void loadNextRoom(Room room, PVector newPlayerPosition) {

	}

	// Alex TODO when called this should switch the player's colour
	public void switchPlayerColour() {

	}

	private void loadNextRoom(Room room, Adjacency adj, PVector direction, boolean isPrimaryScene) {
		engine.setPause(true);
		nextRoom = room;

		// Compute transition vectors
		PVector nextRoomPosition = PVector.add(
				PVector.add(PVector.div(room.getDimensions(), 2), primaryNode.getPosition()),
				PVector.mult(direction, PVector.dot(PVector.div(currentRoom.getDimensions(), 2), direction)));

		previousPlayerPosition = player.getLocalPosition().copy();
		targetPlayerPosition = PVector.add(adj.getLocalPosition(),
				PVector.mult(direction, PVector.dot(Player.PLAYER_SIZE, direction)));

		previousCameraPosition = engine.getCameraPosition();
		targetCameraPosition = PVector.add(engine.getCameraPosition(), nextRoomPosition);

		// Set the next room's node
		nextRoomNode = new GObject(null, nextRoomPosition, 0);
		nextRoomNode.addVisualAttributes(VisualUtils.makeRect(room.getDimensions(), colours.a));
		primaryScene.attachToGObject(primaryScene.scene, nextRoomNode);

		// Attach next room components to next room
		secondaryScene.attachToGObject(secondaryScene.scene, room.getPrimaryGeometry());
		state = GameState.TRANSITION;
	}

	private void unloadLastRoom() {
		deconstructCurrentRoom();
		loadRoom(nextRoom);
		player.setLocalPosition(targetPlayerPosition);
		if (isPrimaryScene) {
			primaryScene.attachToGObject(primaryScene.scene, player);
		} else {
			secondaryScene.attachToGObject(secondaryScene.scene, player);
		}
		deconstructNextRoom();
		state = GameState.PLAY;
		engine.setPause(false);
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

	private void deconstructNextRoom() {
		nextRoom = null;
		nextRoomNode = null;

		previousPlayerPosition = null;
		targetPlayerPosition = null;

		previousCameraPosition = null;
		targetCameraPosition = null;
	}

	//////////////////
	// Constructors //
	//////////////////

	public Game(PApplet applet, PGraphics g) {
		engine = new EngineRuntime(applet, g);
		setup();
	}

}
