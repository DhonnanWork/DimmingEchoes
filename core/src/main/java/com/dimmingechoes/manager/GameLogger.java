package com.dimmingechoes.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameLogger {
    private static GameLogger instance;
    private FileHandle logFile;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss");

    private GameLogger() {}

    public static GameLogger getInstance() {
        if (instance == null) {
            instance = new GameLogger();
        }
        return instance;
    }

    public void init() {
        // Ensure the gamelog directory exists
        java.io.File dir = new java.io.File("gamelog");
        if (!dir.exists()) dir.mkdirs();
        String filename = "gamelog/gamelog-" + FILE_DATE_FORMAT.format(new Date()) + ".txt";
        logFile = com.badlogic.gdx.Gdx.files.absolute(filename);
        logFile.writeString("", false); // Start new log file
    }

    public void log(String message) {
        if (logFile == null) return;
        String timestamp = DATE_FORMAT.format(new Date());
        logFile.writeString("[" + timestamp + "] " + message + "\n", true);
    }

    public void dispose() {
        // No explicit cleanup needed for FileHandle appends
    }
} 