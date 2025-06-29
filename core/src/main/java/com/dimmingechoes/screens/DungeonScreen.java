package com.dimmingEchoes.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.dimmingEchoes.TheDimmingEcho;
import com.dimmingEchoes.dungeon.Room;
import com.dimmingEchoes.dungeon.RoomGraph;
import com.dimmingEchoes.entities.NPC;

public class DungeonScreen implements Screen, InputProcessor {

    private final TheDimmingEcho game;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final BitmapFont font;

    private Room currentRoom;
    private RoomGraph roomGraph;

    private int playerX = 2, playerY = 2;
    private static final int CELL_SIZE = 64;

    private final int MESSAGE_BOX_HEIGHT = 120;
    private final Color MESSAGE_BOX_COLOR = new Color(0, 0, 0, 0.75f);

    private String fullDialogueText = "";
    private String currentText = "";
    private float charTimer = 0f;
    private int currentCharIndex = 0;
    private final float CHAR_DELAY = 0.03f;
    private boolean showingDialogue = false;

    public DungeonScreen(TheDimmingEcho game) {
        this.game = game;
        this.shapeRenderer = new ShapeRenderer();
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.font.setColor(Color.WHITE);
        this.roomGraph = new RoomGraph();
        this.currentRoom = roomGraph.getStartingRoom();
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int[][] grid = currentRoom.getGrid();
        int offsetX = (Gdx.graphics.getWidth() - grid.length * CELL_SIZE) / 2;
        int offsetY = (Gdx.graphics.getHeight() - grid[0].length * CELL_SIZE) / 2;

        // === Draw Grid ===
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int y = 0; y < grid[0].length; y++) {
            for (int x = 0; x < grid.length; x++) {
                if (grid[x][y] == 1) shapeRenderer.setColor(Color.DARK_GRAY); // Wall
                else if (grid[x][y] == 2) shapeRenderer.setColor(Color.GOLD); // Door
                else shapeRenderer.setColor(Color.BLACK); // Floor

                shapeRenderer.rect(offsetX + x * CELL_SIZE, offsetY + y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }

        // === Draw NPCs ===
        shapeRenderer.setColor(Color.GREEN);
        for (NPC npc : currentRoom.getNpcs()) {
            shapeRenderer.rect(offsetX + npc.getX() * CELL_SIZE, offsetY + npc.getY() * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }

        // === Draw Player ===
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(offsetX + playerX * CELL_SIZE, offsetY + playerY * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        shapeRenderer.end();

        // === Update Typing Effect ===
        if (showingDialogue && currentCharIndex < fullDialogueText.length()) {
            charTimer += delta;
            while (charTimer > CHAR_DELAY && currentCharIndex < fullDialogueText.length()) {
                currentText += fullDialogueText.charAt(currentCharIndex++);
                charTimer -= CHAR_DELAY;
            }
        }

        // === Draw UI ===
        batch.begin();

        // Crystal Count (Top Left)
        font.draw(batch, "Crystals: " + game.getCrystalInventory().getCrystals(), 10, Gdx.graphics.getHeight() - 10);

        // Dialogue Box
        if (showingDialogue) {
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(MESSAGE_BOX_COLOR);
            shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), MESSAGE_BOX_HEIGHT);
            shapeRenderer.end();
            batch.begin();
            font.draw(batch, currentText, 20, MESSAGE_BOX_HEIGHT - 20);
        }

        batch.end();
    }

    private boolean isValidMove(int x, int y) {
        int[][] grid = currentRoom.getGrid();
        if (x < 0 || x >= grid.length || y < 0 || y >= grid[0].length) return false;
        if (grid[x][y] == 1) return false; // wall
        for (NPC npc : currentRoom.getNpcs()) {
            if (npc.getX() == x && npc.getY() == y) return false;
        }
        return true;
    }

    private boolean isAdjacent(int x1, int y1, int x2, int y2) {
        return (Math.abs(x1 - x2) + Math.abs(y1 - y2)) == 1;
    }

    private void showDialogue(String text) {
        fullDialogueText = text;
        currentText = "";
        currentCharIndex = 0;
        charTimer = 0;
        showingDialogue = true;
    }

    private void skipOrFinishDialogue() {
        if (!showingDialogue) return;
        if (currentCharIndex < fullDialogueText.length()) {
            // Finish text immediately
            currentText = fullDialogueText;
            currentCharIndex = fullDialogueText.length();
        } else {
            // End dialogue
            showingDialogue = false;
            fullDialogueText = "";
            currentText = "";
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ENTER) {
            skipOrFinishDialogue();
            return true;
        }

        if (showingDialogue) return false;

        int nextX = playerX;
        int nextY = playerY;

        if (keycode == Input.Keys.W || keycode == Input.Keys.UP) nextY += 1;
        if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) nextY -= 1;
        if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) nextX -= 1;
        if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) nextX += 1;

        if (isValidMove(nextX, nextY)) {
            playerX = nextX;
            playerY = nextY;
        }

        if (keycode == Input.Keys.SPACE) {
            for (NPC npc : currentRoom.getNpcs()) {
                if (isAdjacent(playerX, playerY, npc.getX(), npc.getY())) {
                    showDialogue(npc.getDialogue());
                    return true;
                }
            }

            if (currentRoom.getGrid()[playerX][playerY] == 2) {
                Room nextRoom = currentRoom.getConnectedFromDoorAt(playerX, playerY);
                if (nextRoom != null) {
                    currentRoom = nextRoom;
                    playerX = 2;
                    playerY = 2;
                    showDialogue("You stepped into a new memory...");
                }
            }
        }

        return true;
    }

    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
