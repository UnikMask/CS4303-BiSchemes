package bischemes.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.TreeSet;

public class InputHandler {
	private static InputHandler instance;
	private HashMap<Character, InputCommand> onHeldCommands = new HashMap<>(
			Map.of('w', InputCommand.UP, 's', InputCommand.DOWN, 'a', InputCommand.LEFT, 'd', InputCommand.RIGHT));
	private HashMap<Character, InputCommand> onPressCommands = new HashMap<>(
			Map.of('p', InputCommand.PAUSE, 'e', InputCommand.INTERACT));
	private TreeSet<Character> heldKeys = new TreeSet<>();
	private TreeSet<Character> pressedKeys = new TreeSet<>();

	public enum InputCommand {
		UP, DOWN, LEFT, RIGHT, INTERACT, PAUSE, NONE
	}

	public void initFrame() {
		pressedKeys = new TreeSet<>();
	}

	public InputCommand keyPressed(Character c) {
		if (!heldKeys.contains(c)) {
			heldKeys.add(c);
			return onPressCommands.containsKey(c) ? onPressCommands.get(c) : InputCommand.NONE;
		}
		return InputCommand.NONE;
	}

	public InputCommand keyReleased(Character c) {
		if (heldKeys.contains(c)) {
			heldKeys.remove(c);
		}
		return InputCommand.NONE;
	}

	public Set<InputCommand> getHeldCommands() {
		TreeSet<InputCommand> ret = new TreeSet<>();
		for (Character c : heldKeys) {
			if (onHeldCommands.containsKey(c)) {
				ret.add(onHeldCommands.get(c));
			}
		}
		return ret;
	}

	public Set<InputCommand> getPressedCommands() {
		TreeSet<InputCommand> ret = new TreeSet<>();
		for (Character c : pressedKeys) {
			if (onPressCommands.containsKey(c)) {
				ret.add(onPressCommands.get(c));
			}
		}
		return ret;

	}

	public static InputHandler getInstance() {
		if (instance == null) {
			instance = new InputHandler();
		}
		return instance;
	}
}
