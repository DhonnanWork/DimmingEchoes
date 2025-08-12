package com.dimmingechoes.dungeon;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.dimmingechoes.entities.NPC;

import java.util.ArrayList;
import java.util.List;

public class Room {

    private final String tmxPath;
    private final RoomType type;
    private final Color backgroundColor; // <-- ADDED
    private final List<NPC> npcs = new ArrayList<>();
    private final List<Rectangle> obstacles = new ArrayList<>();
    private final List<DoorZone> doorZones = new ArrayList<>();

    // Constructor is now updated
    public Room(String tmxPath, RoomType type, Color backgroundColor) {
        this.tmxPath = tmxPath;
        this.type = type;
        this.backgroundColor = backgroundColor;
    }
    public RoomType getRoomType() {
        return type;
    }
    public String getTmxPath() {
        return tmxPath;
    }
    public Color getBackgroundColor() {
        return backgroundColor;
    }
    public List<NPC> getNpcs() {
        return npcs;
    }
    public void addNPC(NPC npc) {
        npcs.add(npc);
    }
    public List<Rectangle> getObstacles() {
        return obstacles;
    }
    public void addObstacle(Rectangle obstacle) {
        obstacles.add(obstacle);
    }
    public List<DoorZone> getDoorZones() {
        return doorZones;
    }
    public void addDoorZone(DoorZone doorZone) {
        doorZones.add(doorZone);
    }
}
