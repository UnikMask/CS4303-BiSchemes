package bischemes.game;

import bischemes.engine.EngineRuntime;
import bischemes.level.Level;

public class Game {
	EngineRuntime engine;
	GameState state;

	// States of a level/game - feel free to modify
	enum GameState {
		PAUSE, PLAY, INTRO, FINISH, END
	}

	public void update() {
		engine.update();

		// Update behaviour per state
		switch (state) {
		case PAUSE:
			break;
		case PLAY:
			break;
		case INTRO:
			break;
		case FINISH:
			break;
		case END:
		}
	}

	public void setup() {

	}

	public Game(Level level) {
	}
}
