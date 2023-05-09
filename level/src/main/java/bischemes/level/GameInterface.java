package bischemes.level;

import bischemes.engine.physics.RigidBody;
import bischemes.level.util.LColour;
import processing.core.PVector;

public interface GameInterface {

    void loadNextRoom(Room room, PVector newPlayerPosition);

    void switchPlayerColour();

    void completeLevel();

    void removeRigidBody(RigidBody r, LColour l);

    void addRigidBody(RigidBody r, LColour l);

}
