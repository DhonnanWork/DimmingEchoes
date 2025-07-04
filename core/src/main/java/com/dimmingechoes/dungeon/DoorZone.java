package com.dimmingechoes.dungeon;

import com.badlogic.gdx.math.Rectangle;

/**
 * Represents an area in a room that, when entered, transports the player to another room.
 */
public class DoorZone {
    public final Rectangle bounds;
    public final Room leadsTo;
    public final Direction entryDirection; // The side of the room this door is on

    public enum Direction {
        LEFT, RIGHT, TOP, BOTTOM
    }

    public DoorZone(Rectangle bounds, Room leadsTo, Direction entryDirection) {
        this.bounds = bounds;
        this.leadsTo = leadsTo;
        this.entryDirection = entryDirection;
    }
}
