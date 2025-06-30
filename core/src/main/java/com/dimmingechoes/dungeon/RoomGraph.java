package com.dimmingEchoes.dungeon;

import com.dimmingEchoes.entities.NPC;

import java.util.HashMap;
import java.util.Map;

public class RoomGraph {

    private Room startingRoom;
    private Map<Room, Room[]> connections;

    public RoomGraph() {
        connections = new HashMap<>();
        generateRooms();
    }

    public Room getStartingRoom() {
        return startingRoom;
    }

    public Room[] getConnected(Room room) {
        return connections.getOrDefault(room, new Room[0]);
    }

    private void generateRooms() {
        // Build all rooms
        Room start = new Room(RoomType.START, GridBuilder.build(15, 9, true, true));
        Room memory = new Room(RoomType.MEMORY, GridBuilder.build(15, 9, true, true));
        Room battle = new Room(RoomType.BATTLE, GridBuilder.build(15, 9, true, true));
        Room finalRoom = new Room(RoomType.FINAL, GridBuilder.build(15, 9, true, true));

        // START ROOM: 2 NPCs
        start.addNPC(new NPC("The Keeper", RoomType.START, 3, 5));
        start.addNPC(new NPC("The Whisper", RoomType.START, 8, 4));
        memory.addNPC(new NPC("The Laughing Girl", RoomType.MEMORY, 6, 5));
        battle.addNPC(new NPC("The Stranger", RoomType.BATTLE, 5, 4));

        // FINAL ROOM: no NPC, ending trigger

        // Connections from START
        start.setConnections(memory, battle, finalRoom);

        // Each room connects back to START only
        memory.setConnections(start, null, null);
        battle.setConnections(null, start, null);
        finalRoom.setConnections(null, null, start);

        // Set graph
        this.startingRoom = start;

        connections.put(start, new Room[]{memory, battle, finalRoom});
        connections.put(memory, new Room[]{start});
        connections.put(battle, new Room[]{start});
        connections.put(finalRoom, new Room[]{start});
    }
}
