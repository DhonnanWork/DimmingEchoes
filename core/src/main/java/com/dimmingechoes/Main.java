package com.dimmingechoes;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {

    private ShapeRenderer shapeRenderer;
    private Rectangle player;
    private Array<Rectangle> walls;

    private final float PLAYER_SIZE = 50f;
    private final float PLAYER_SPEED = 300f;

    // --- NEW: Add a "TAG" for our log messages for easy filtering ---
    private static final String TAG = "GameLog";


    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();

        float roomWidth = 600f;
        float roomHeight = 400f;
        float wallThickness = 20f;

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float roomCenterX = screenWidth / 2;
        float roomCenterY = screenHeight / 2;

        float roomX = roomCenterX - roomWidth / 2;
        float roomY = roomCenterY - roomHeight / 2;

        walls = new Array<>();

        walls.add(new Rectangle(roomX, roomY, wallThickness, roomHeight)); // Left
        walls.add(new Rectangle(roomX + roomWidth - wallThickness, roomY, wallThickness, roomHeight)); // Right
        walls.add(new Rectangle(roomX, roomY, roomWidth, wallThickness)); // Bottom

        float gapSize = 100f;
        float gapX = roomCenterX - gapSize / 2;
        walls.add(new Rectangle(roomX, roomY + roomHeight - wallThickness, gapX - roomX, wallThickness)); // Top-Left
        walls.add(new Rectangle(gapX + gapSize, roomY + roomHeight - wallThickness, roomWidth - (gapX + gapSize - roomX), wallThickness)); // Top-Right

        player = new Rectangle(roomCenterX - PLAYER_SIZE/2, roomCenterY - PLAYER_SIZE/2, PLAYER_SIZE, PLAYER_SIZE);

        Gdx.app.log(TAG, "Game created. Player and walls initialized.");
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        float oldX = player.x;
        float oldY = player.y;

        // --- NEW: LOGGING FOR KEY PRESSES ---
        // We use isKeyJustPressed so it only logs once when you press the key down.
        if (Gdx.input.isKeyJustPressed(Keys.W) || Gdx.input.isKeyJustPressed(Keys.UP))    Gdx.app.log(TAG, "Input: UP");
        if (Gdx.input.isKeyJustPressed(Keys.S) || Gdx.input.isKeyJustPressed(Keys.DOWN))  Gdx.app.log(TAG, "Input: DOWN");
        if (Gdx.input.isKeyJustPressed(Keys.A) || Gdx.input.isKeyJustPressed(Keys.LEFT))  Gdx.app.log(TAG, "Input: LEFT");
        if (Gdx.input.isKeyJustPressed(Keys.D) || Gdx.input.isKeyJustPressed(Keys.RIGHT)) Gdx.app.log(TAG, "Input: RIGHT");


        // --- Player movement logic (X-axis) ---
        // isKeyPressed is used for movement so the player moves as long as the key is held.
        if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)) {
            player.x -= PLAYER_SPEED * deltaTime;
        }
        if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) {
            player.x += PLAYER_SPEED * deltaTime;
        }

        // Collision check for X-axis
        for (Rectangle wall : walls) {
            if (player.overlaps(wall)) {
                // --- NEW: LOGGING FOR COLLISION ---
                Gdx.app.log(TAG, "Collision on X-axis. Movement stopped.");
                player.x = oldX;
                break;
            }
        }

        // --- Player movement logic (Y-axis) ---
        if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP)) {
            player.y += PLAYER_SPEED * deltaTime;
        }
        if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN)) {
            player.y -= PLAYER_SPEED * deltaTime;
        }

        // Collision check for Y-axis
        for (Rectangle wall : walls) {
            if (player.overlaps(wall)) {
                // --- NEW: LOGGING FOR COLLISION ---
                Gdx.app.log(TAG, "Collision on Y-axis. Movement stopped.");
                player.y = oldY;
                break;
            }
        }

        // --- Drawing ---
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1);
        shapeRenderer.begin(ShapeType.Filled);

        shapeRenderer.setColor(Color.RED);
        for (Rectangle wall : walls) {
            shapeRenderer.rect(wall.x, wall.y, wall.width, wall.height);
        }

        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(player.x, player.y, player.width, player.height);

        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
