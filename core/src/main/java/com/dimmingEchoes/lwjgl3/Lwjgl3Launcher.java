package com.dimmingEchoes.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.dimmingEchoes.Main; // Import your Main game class

public class Lwjgl3Launcher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

        // Configure window size to accommodate the grid
        config.setTitle("Dimming Echoes");
        config.setWindowedMode(GRID_WIDTH * CELL_SIZE, GRID_HEIGHT * CELL_SIZE); // Calculated size
        config.setResizable(false); // Usually better for grid-based games to prevent distortion

        new Lwjgl3Application(new Main(), config);
    }
}