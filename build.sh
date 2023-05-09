#!/bin/env sh


./copyLevels.sh
./gradlew level:build
./gradlew engine:build
./gradlew jar
cp game/build/libs/game.jar .

