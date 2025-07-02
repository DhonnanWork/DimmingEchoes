package com.dimmingEchoes.dungeon;

import com.badlogic.gdx.math.Rectangle;
import com.dimmingEchoes.entities.NPC;

public class RoomGraph {

    private Room startingRoom;

    // --- Define the world dimensions ---
    // Using a fixed design resolution is good practice.
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
        // Build all the room objects first
        Room start = new Room(RoomType.START);
        Room memory = new Room(RoomType.MEMORY);
        Room battle = new Room(RoomType.BATTLE);
        Room finalRoom = new Room(RoomType.FINAL);

        // --- Define START Room Layout ---
        setupRoomBoundaries(start);
        start.addDoorZone(new DoorZone(new Rectangle(0, WORLD_HEIGHT / 2 - DOOR_WIDTH / 2, DOOR_THICKNESS, DOOR_WIDTH), memory, DoorZone.Direction.LEFT));
        start.addDoorZone(new DoorZone(new Rectangle(WORLD_WIDTH - DOOR_THICKNESS, WORLD_HEIGHT / 2 - DOOR_WIDTH / 2, DOOR_THICKNESS, DOOR_WIDTH), battle, DoorZone.Direction.RIGHT));
        start.addDoorZone(new DoorZone(new Rectangle(WORLD_WIDTH / 2 - DOOR_WIDTH / 2, WORLD_HEIGHT - DOOR_THICKNESS, DOOR_WIDTH, DOOR_THICKNESS), finalRoom, DoorZone.Direction.TOP));
        start.addNPC(new NPC("The Keeper", RoomType.START, 300, 500));
        start.addNPC(new NPC("The Whisper", RoomType.START, 800, 400));

        // --- Define MEMORY Room Layout ---
        setupRoomBoundaries(memory);
        // This door leads back to the start room
        memory.addDoorZone(new DoorZone(new Rectangle(WORLD_WIDTH - DOOR_THICKNESS, WORLD_HEIGHT / 2 - DOOR_WIDTH / 2, DOOR_THICKNESS, DOOR_WIDTH), start, DoorZone.Direction.RIGHT));
        memory.addNPC(new NPC("The Laughing Girl", RoomType.MEMORY, WORLD_WIDTH / 2, WORLD_HEIGHT / 2));
        // Add some decorative obstacles
        memory.addObstacle(new Rectangle(200, 200, 50, 50));
        memory.addObstacle(new Rectangle(900, 450, 80, 80));

        // --- Define BATTLE Room Layout ---
        setupRoomBoundaries(battle);
        battle.addDoorZone(new DoorZone(new Rectangle(0, WORLD_HEIGHT / 2 - DOOR_WIDTH / 2, DOOR_THICKNESS, DOOR_WIDTH), start, DoorZone.Direction.LEFT));
        battle.addNPC(new NPC("The Stranger", RoomType.BATTLE, WORLD_WIDTH / 2, 400));


        // --- Define FINAL Room Layout ---
        setupRoomBoundaries(finalRoom);
        finalRoom.addDoorZone(new DoorZone(new Rectangle(WORLD_WIDTH / 2 - DOOR_WIDTH / 2, 0, DOOR_WIDTH, DOOR_THICKNESS), start, DoorZone.Direction.BOTTOM));
        // This room is intentionally empty of NPCs.

        this.startingRoom = start;
    }

    /**
     * A helper method to add the four outer walls to any room.
     * @param room The room to add boundaries to.
     */
    private void setupRoomBoundaries(Room room) {
        // Bottom wall
        room.addObstacle(new Rectangle(0, 0, WORLD_WIDTH, WALL_THICKNESS));
        // Top wall
        room.addObstacle(new Rectangle(0, WORLD_HEIGHT - WALL_THICKNESS, WORLD_WIDTH, WALL_THICKNESS));
        // Left wall
        room.addObstacle(new Rectangle(0, 0, WALL_THICKNESS, WORLD_HEIGHT));
        // Right wall
        room.addObstacle(new Rectangle(WORLD_WIDTH - WALL_THICKNESS, 0, WALL_THICKNESS, WORLD_HEIGHT));
    }
}
