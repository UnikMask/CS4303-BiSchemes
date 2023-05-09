#!/bin/env sh


./copyLevels.sh
./gradlew jar
cp game/build/libs/game.jar .

