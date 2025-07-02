package com.dimmingEchoes.dungeon;

import com.badlogic.gdx.math.Rectangle;
import com.dimmingEchoes.entities.NPC;

import java.util.ArrayList;
import java.util.List;

public class Room {

    private final RoomType type;
    private final List<NPC> npcs = new ArrayList<>();
    private final List<Rectangle> obstacles = new ArrayList<>();
    private final List<DoorZone> doorZones = new ArrayList<>();

    public Room(RoomType type) {
        this.type = type;
    }

    public RoomType getRoomType() { return type; }

    public List<NPC> getNpcs() { return npcs; }
    public void addNPC(NPC npc) { npcs.add(npc); }

    public List<Rectangle> getObstacles() { return obstacles; }
    public void addObstacle(Rectangle obstacle) { obstacles.add(obstacle); }

    public List<DoorZone> getDoorZones() { return doorZones; }
    public void addDoorZone(DoorZone doorZone) { doorZones.add(doorZone); }
}
