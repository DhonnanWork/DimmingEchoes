package com.dimmingechoes.dungeon;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.dimmingechoes.entities.NPC;

public class RoomGraph {

    private Room startingRoom;

    // --- Define the world dimensions ---
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
        // Build all the room objects first, now with colors
        Room start = new Room("Tiled/Tengah.tmx", RoomType.START, new Color(0.2f, 0.2f, 0.2f, 1));
        Room memory = new Room("Tiled/Kiri.tmx", RoomType.MEMORY, new Color(0.1f, 0.2f, 0.3f, 1));
        Room battle = new Room("Tiled/Kanan.tmx", RoomType.BATTLE, new Color(0.3f, 0.1f, 0.1f, 1));

        Room finalRoomTrigger = new Room("", RoomType.FINAL, new Color(0.1f, 0.1f, 0.1f, 1));

        // --- Define START Room Layout ---
        start.addDoorZone(new DoorZone(new Rectangle(0, WORLD_HEIGHT / 2 - DOOR_WIDTH / 2, DOOR_THICKNESS, DOOR_WIDTH), memory, DoorZone.Direction.LEFT));
        start.addDoorZone(new DoorZone(new Rectangle(WORLD_WIDTH - DOOR_THICKNESS, WORLD_HEIGHT / 2 - DOOR_WIDTH / 2, DOOR_THICKNESS, DOOR_WIDTH), battle, DoorZone.Direction.RIGHT));
        start.addDoorZone(new DoorZone(new Rectangle(WORLD_WIDTH / 2 - DOOR_WIDTH / 2, WORLD_HEIGHT - DOOR_THICKNESS, DOOR_WIDTH, DOOR_THICKNESS), finalRoomTrigger, DoorZone.Direction.TOP));
        start.addNPC(new NPC("The Keeper", RoomType.START, 300, 500));
        start.addNPC(new NPC("The Whisper", RoomType.START, 800, 400));

        // --- Define MEMORY Room Layout --
        memory.addDoorZone(new DoorZone(new Rectangle(WORLD_WIDTH - DOOR_THICKNESS, WORLD_HEIGHT / 2 - DOOR_WIDTH / 2, DOOR_THICKNESS, DOOR_WIDTH), start, DoorZone.Direction.RIGHT));
        memory.addNPC(new NPC("The Laughing Girl", RoomType.MEMORY, WORLD_WIDTH / 2, WORLD_HEIGHT / 2));
       // --- Obstacle Memory Room ---
        memory.addObstacle(new Rectangle(200, 200, 50, 50));
        memory.addObstacle(new Rectangle(900, 450, 80, 80));

        // --- Define BATTLE Room Layout ---
        //setupRoomBoundaries(battle);
        battle.addDoorZone(new DoorZone(new Rectangle(0, WORLD_HEIGHT / 2 - DOOR_WIDTH / 2, DOOR_THICKNESS, DOOR_WIDTH), start, DoorZone.Direction.LEFT));
        battle.addNPC(new NPC("The Stranger", RoomType.BATTLE, WORLD_WIDTH / 2, 400));

        // --- FINAL Room dihapus karena bukan ruangan fisik ---

        this.startingRoom = start;
    }

//    private void setupRoomBoundaries(Room room) {
//        room.addObstacle(new Rectangle(0, 0, WORLD_WIDTH, WALL_THICKNESS));
//        room.addObstacle(new Rectangle(0, WORLD_HEIGHT - WALL_THICKNESS, WORLD_WIDTH, WALL_THICKNESS));
//        room.addObstacle(new Rectangle(0, 0, WALL_THICKNESS, WORLD_HEIGHT));
//        room.addObstacle(new Rectangle(WORLD_WIDTH - WALL_THICKNESS, 0, WALL_THICKNESS, WORLD_HEIGHT));
//    }
}
