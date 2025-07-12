package com.dimmingechoes.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

public class SaveManager {
    private static final String SAVE_FILE_PREFIX = "savegame";
    private static final String SAVE_FILE_SUFFIX = ".json";
    private static final int MAX_SAVE_SLOTS = 3;

    public static void save(SaveData data, int slot) {
        if (slot < 1 || slot > MAX_SAVE_SLOTS) return;
        
        String filename = SAVE_FILE_PREFIX + slot + SAVE_FILE_SUFFIX;
        FileHandle file = Gdx.files.local(filename);
        Json json = new Json();
        file.writeString(json.prettyPrint(data), false);
    }

    public static SaveData load(int slot) {
        if (slot < 1 || slot > MAX_SAVE_SLOTS) return null;
        
        String filename = SAVE_FILE_PREFIX + slot + SAVE_FILE_SUFFIX;
        FileHandle file = Gdx.files.local(filename);
        if (!file.exists()) return null;
        
        Json json = new Json();
        return json.fromJson(SaveData.class, file.readString());
    }

    public static boolean hasSave(int slot) {
        if (slot < 1 || slot > MAX_SAVE_SLOTS) return false;
        
        String filename = SAVE_FILE_PREFIX + slot + SAVE_FILE_SUFFIX;
        FileHandle file = Gdx.files.local(filename);
        return file.exists();
    }

    public static void delete(int slot) {
        if (slot < 1 || slot > MAX_SAVE_SLOTS) return;
        
        String filename = SAVE_FILE_PREFIX + slot + SAVE_FILE_SUFFIX;
        FileHandle file = Gdx.files.local(filename);
        if (file.exists()) file.delete();
    }

    public static int getMaxSaveSlots() {
        return MAX_SAVE_SLOTS;
    }

    // Legacy methods for backward compatibility
    public static void save(SaveData data) {
        save(data, 1); // Default to slot 1
    }

    public static SaveData load() {
        return load(1); // Default to slot 1
    }

    public static void delete() {
        delete(1); // Default to slot 1
    }
}
