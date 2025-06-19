package com.dimmingEchoes;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle; // For simple collision detection

public class Main extends ApplicationAdapter implements InputProcessor {

    SpriteBatch batch;
    Texture playerTexture;
    Texture wallTexture;

    // Grid dimensions and cell size
    private static final int GRID_WIDTH = 5;
    private static final int GRID_HEIGHT = 5;
    private static final int CELL_SIZE = 64; // Size of each square in pixels

    // Player position (in grid coordinates)
    private int playerGridX;
    private int playerGridY;

    // Grid representation: 0 for empty, 1 for wall
    private int[][] grid;

    // Player movement speed (in cells per press, usually 1)
    private static final int MOVE_DISTANCE = 1;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Load textures (you'll need to create these files in the assets folder)
        playerTexture = new Texture("player.png"); // A white square for player
        wallTexture = new Texture("wall.png");     // A white square for walls

        // Initialize grid and player position
        initializeGrid();

        // Set this class as the input processor
        Gdx.input.setInputProcessor(this);
    }

    private void initializeGrid() {
        grid = new int[GRID_WIDTH][GRID_HEIGHT];

        // Set up some walls for testing
        // Example: a simple border wall
        for (int x = 0; x < GRID_WIDTH; x++) {
            grid[x][0] = 1; // Bottom wall
            grid[x][GRID_HEIGHT - 1] = 1; // Top wall
        }
        for (int y = 0; y < GRID_HEIGHT; y++) {
            grid[0][y] = 1; // Left wall
            grid[GRID_WIDTH - 1][y] = 1; // Right wall
        }

        // Place player in the center (or any safe starting point)
        playerGridX = GRID_WIDTH / 2;
        playerGridY = GRID_HEIGHT / 2;

        // Ensure player doesn't start on a wall
        if (grid[playerGridX][playerGridY] == 1) {
            playerGridX = 1; // Example: move to a safe spot if center is a wall
            playerGridY = 1;
        }
    }

    @Override
    public void render() {
        // Clear the screen
        Gdx.gl.glClearColor(0, 0, 0, 1); // Black background
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // Draw the grid (walls and empty spaces)
        drawGrid();

        // Draw the player
        batch.draw(playerTexture, playerGridX * CELL_SIZE, playerGridY * CELL_SIZE, CELL_SIZE, CELL_SIZE);

        batch.end();
    }

    private void drawGrid() {
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                if (grid[x][y] == 1) { // If it's a wall
                    batch.draw(wallTexture, x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
                // If it's 0 (empty), we don't draw anything specific, background is clear.
            }
        }
    }

    // --- InputProcessor methods ---

    @Override
    public boolean keyDown(int keycode) {
        int nextX = playerGridX;
        int nextY = playerGridY;

        // Determine potential next position based on input
        if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
            nextX -= MOVE_DISTANCE;
        } else if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
            nextX += MOVE_DISTANCE;
        } else if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
            nextY += MOVE_DISTANCE;
        } else if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
            nextY -= MOVE_DISTANCE;
        } else {
            return false; // Not a movement key
        }

        // Check if the move is valid before updating player position
        if (isValidMove(nextX, nextY)) {
            playerGridX = nextX;
            playerGridY = nextY;
        }

        return true; // Event consumed
    }

    private boolean isValidMove(int gridX, int gridY) {
        // 1. Check if within grid bounds
        if (gridX < 0 || gridX >= GRID_WIDTH || gridY < 0 || gridY >= GRID_HEIGHT) {
            return false;
        }
        // 2. Check if the target cell is a wall
        if (grid[gridX][gridY] == 1) {
            return false;
        }
        // If both checks pass, the move is valid
        return true;
    }

    // --- Other InputProcessor methods (can be left as default/empty) ---
    @Override
    public boolean keyUp(int keycode) { return false; }
    @Override
    public boolean keyTyped(char character) { return false; }
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override
    public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override
    public boolean scrolled(float amountX, float amountY) { return false; }

    @Override
    public void dispose() {
        batch.dispose();
        playerTexture.dispose();
        wallTexture.dispose();
    }
}