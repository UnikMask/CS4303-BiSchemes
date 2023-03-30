package bischemes.engine;

import java.util.List;

public interface EventCaller {
	public List<String> getEvents();

	public List<EventCallback> geCallbacksFromEvent(String event);
}
