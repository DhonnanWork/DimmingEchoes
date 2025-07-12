package com.dimmingechoes.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

public class AudioManager {
    private static AudioManager instance;
    private Music currentMusic;

    private AudioManager() {}

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    public void playMusic(String path, boolean looping) {
        stopMusic();
        FileHandle file = Gdx.files.internal(path);
        if (!file.exists()) return;
        currentMusic = Gdx.audio.newMusic(file);
        currentMusic.setLooping(looping);
        currentMusic.play();
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
        }
    }

    public void playSound(String path) {
        FileHandle file = Gdx.files.internal(path);
        if (!file.exists()) return;
        Sound sound = Gdx.audio.newSound(file);
        sound.play();
        // Optionally dispose after playing if not reused
        sound.dispose();
    }
} 