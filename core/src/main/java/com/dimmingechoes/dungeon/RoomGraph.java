// File: core/src/main/java/com/dimmingechoes/dungeon/RoomGraph.java

package com.dimmingechoes.dungeon;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;

public class RoomGraph {

    private Room startingRoom;

    // --- World dimensions for defining door zones ---
    private static final float WORLD_WIDTH = 1280;
    private static final float WORLD_HEIGHT = 720;
    private static final float WALL_THICKNESS = 32;
    private static final float DOOR_WIDTH = 120;
    private static final float DOOR_THICKNESS = 32;

    public RoomGraph() {
        generateRooms();
    }

    public Room getStartingRoom() {
        return startingRoom;
    }

    private void generateRooms() {
        // Step 1: Define the rooms with their TMX paths and background colors.
        Room start = new Room("Tiled/Tengah.tmx", RoomType.START, new Color(0.2f, 0.2f, 0.2f, 1));
        Room memory = new Room("Tiled/Kiri.tmx", RoomType.MEMORY, new Color(0.1f, 0.2f, 0.3f, 1));
        Room battle = new Room("Tiled/Kanan.tmx", RoomType.EMPTY, new Color(0.3f, 0.1f, 0.1f, 1));
        Room finalRoomTrigger = new Room("", RoomType.FINAL, new Color(0.1f, 0.1f, 0.1f, 1));

        // Step 2: Define the connections (DoorZones) between rooms.
        start.addDoorZone(new DoorZone(new Rectangle(0, (WORLD_HEIGHT / 2 - DOOR_WIDTH / 2) + 60, DOOR_THICKNESS, DOOR_WIDTH), memory, DoorZone.Direction.LEFT));
        start.addDoorZone(new DoorZone(new Rectangle(WORLD_WIDTH - DOOR_THICKNESS, (WORLD_HEIGHT / 2 - DOOR_WIDTH / 2) + 60, DOOR_THICKNESS, DOOR_WIDTH), battle, DoorZone.Direction.RIGHT));
        start.addDoorZone(new DoorZone(new Rectangle(WORLD_WIDTH / 2 - DOOR_WIDTH / 2, (WORLD_HEIGHT - DOOR_THICKNESS), DOOR_WIDTH, DOOR_THICKNESS), finalRoomTrigger, DoorZone.Direction.TOP));
        memory.addDoorZone(new DoorZone(new Rectangle(WORLD_WIDTH - DOOR_THICKNESS, (WORLD_HEIGHT / 2 - DOOR_WIDTH / 2) + 60, DOOR_THICKNESS, DOOR_WIDTH), start, DoorZone.Direction.RIGHT));
        battle.addDoorZone(new DoorZone(new Rectangle(0, (WORLD_HEIGHT / 2 - DOOR_WIDTH / 2) + 60, DOOR_THICKNESS, DOOR_WIDTH), start, DoorZone.Direction.LEFT));

        // NOTE: All addNPC() calls have been REMOVED.
        // The TMX file is now the single source of truth for NPC placement.

        this.startingRoom = start;
    }
}
