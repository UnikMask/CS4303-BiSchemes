package bischemes.level;

import processing.core.PVector;

public interface GameInterface {

    void loadNextRoom(Room room, PVector newPlayerPosition);

    void switchPlayerColour();

    void completeLevel();

}
