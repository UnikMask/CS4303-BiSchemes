/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package bischemes.game;

import bischemes.game.Runner.RunnerState;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

public class Runner extends PApplet {
	public RunnerState state = RunnerState.PLAY;
	public Game game;
	public PGraphics g;

	enum RunnerState {
		MENU, HELP, PLAY, PAUSE
	}

	public void settings() {
		size(1920, 1080, PConstants.P2D);
		// fullScreen();
	}

	public void setup() {
		frameRate(60);
		g = this.createGraphics(this.width, this.height, PConstants.P2D);
	}

	public void draw() {
		background(0);
		switch (state) {
		case PLAY:
			if (game == null) {
				game = new Game(this, g);
			} else {
				game.update(g);
			}
			break;
		default:
			break;
		}
		InputHandler.getInstance().initFrame();
	}

	public void keyPressed() {
		InputHandler.getInstance().keyPressed(this.key);
	}

	public void keyReleased() {
		InputHandler.getInstance().keyReleased(this.key);
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "bischemes.game.Runner" });
	}
}
