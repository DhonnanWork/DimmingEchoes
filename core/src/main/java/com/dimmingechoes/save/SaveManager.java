package com.dimmingechoes.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

public class SaveManager {
    private static final String SAVE_FILE = "savegame.json";

    public static void save(SaveData data) {
        FileHandle file = Gdx.files.local(SAVE_FILE);
        Json json = new Json();
        file.writeString(json.prettyPrint(data), false);
    }

    public static SaveData load() {
        FileHandle file = Gdx.files.local(SAVE_FILE);
        if (!file.exists()) return null;
        Json json = new Json();
        return json.fromJson(SaveData.class, file.readString());
    }

    public static void delete() {
        FileHandle file = Gdx.files.local(SAVE_FILE);
        if (file.exists()) file.delete();
    }
}
