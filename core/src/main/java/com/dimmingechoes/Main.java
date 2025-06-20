// CHANGE THIS LINE
package com.dimmingechoes;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {

    private ShapeRenderer shapeRenderer;
    private Rectangle player;
    private Rectangle wall;
    private final float PLAYER_SIZE = 50f;
    private final float PLAYER_SPEED = 300f;
    private final int WALL_MULTIPLIER = 5;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        player = new Rectangle(100, 100, PLAYER_SIZE, PLAYER_SIZE);
        float wallSize = PLAYER_SIZE * WALL_MULTIPLIER;
        wall = new Rectangle(400, 200, wallSize, wallSize);
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        float oldX = player.x;
        float oldY = player.y;

        if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)) {
            player.x -= PLAYER_SPEED * deltaTime;
        }
        if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) {
            player.x += PLAYER_SPEED * deltaTime;
        }
        if (player.overlaps(wall)) {
            player.x = oldX;
        }
        if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP)) {
            player.y += PLAYER_SPEED * deltaTime;
        }
        if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN)) {
            player.y -= PLAYER_SPEED * deltaTime;
        }
        if (player.overlaps(wall)) {
            player.y = oldY;
        }
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1);
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(wall.x, wall.y, wall.width, wall.height);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(player.x, player.y, player.width, player.height);
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
