package com.dimmingEchoes.dungeon;

import com.dimmingEchoes.TheDimmingEcho;
import com.dimmingEchoes.entities.NPC;

import java.util.ArrayList;
import java.util.List;

public class Room {

    private final RoomType type;
    private final int[][] grid;
    private final List<NPC> npcs = new ArrayList<>();
    private final Room[] connections = new Room[3]; // left, right, center

    public Room(RoomType type, int[][] grid) {
        this.type = type;
        this.grid = grid;
    }

    public RoomType getType() { return type; }
    public int[][] getGrid() { return grid; }

    public List<NPC> getNpcs() { return npcs; }
    public void addNPC(NPC npc) { npcs.add(npc); }

    // Returns this room's type (e.g., FINAL, BATTLE, etc.)
    public RoomType getRoomType() {
        return type;
    }

    public void setConnections(Room left, Room right, Room center) {
        connections[0] = left;
        connections[1] = right;
        connections[2] = center;
    }

    public Room getConnectedFromDoorAt(int x, int y) {
        if (grid[x][y] == 2) {
            if (x == 1) return connections[0];
            if (x == grid.length - 2) return connections[1];
            if (y == 1 || y == grid[0].length - 2) return connections[2];
        }
        return null;
    }
}
