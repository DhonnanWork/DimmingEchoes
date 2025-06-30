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
import com.dimmingEchoes.dialogue.DialogueChoice;
import com.dimmingEchoes.dialogue.DialogueNode;
import com.dimmingEchoes.dungeon.Room;
import com.dimmingEchoes.dungeon.RoomGraph;
import com.dimmingEchoes.dungeon.RoomType;
import com.dimmingEchoes.entities.NPC;
import com.dimmingEchoes.save.SaveManager;

public class DungeonScreen implements Screen, InputProcessor {

    private final TheDimmingEcho game;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;
    private final BitmapFont font;

    private Room currentRoom;
    private final RoomGraph roomGraph;

    private int playerX = 2, playerY = 2;
    private static final int CELL_SIZE = 64;

    private boolean moveUp, moveDown, moveLeft, moveRight;
    private float moveTimer = 0f;
    private final float moveDelay = 0.15f;

    private DialogueNode currentDialogueNode = null;
    private NPC dialogueNPC = null;
    private int selectedChoiceIndex = 0;
    private boolean endingShown = false;

    // Typing effect
    private String displayedText = "";
    private float charTimer = 0;
    private int charIndex = 0;
    private final float CHAR_DELAY = 0.03f;

    public DungeonScreen(TheDimmingEcho game) {
        this.game = game;
        this.shapeRenderer = new ShapeRenderer();
        this.spriteBatch = new SpriteBatch();
        this.font = new BitmapFont();
        this.roomGraph = new RoomGraph();
        this.currentRoom = roomGraph.getStartingRoom();
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        moveTimer += delta;

        if (currentDialogueNode == null && moveTimer >= moveDelay) {
            handleMovement();
            moveTimer = 0f;
        }

        renderGame();

        if (currentDialogueNode != null) {
            renderDialogue();
        }
    }

    private void renderGame() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        int[][] grid = currentRoom.getGrid();

        for (int y = 0; y < grid[0].length; y++) {
            for (int x = 0; x < grid.length; x++) {
                if (grid[x][y] == 1) {
                    shapeRenderer.setColor(Color.DARK_GRAY);
                } else if (grid[x][y] == 2 && currentRoom.getConnectedFromDoorAt(x, y) != null) {
                    shapeRenderer.setColor(Color.GOLD);
                } else {
                    shapeRenderer.setColor(Color.BLACK);
                }
                shapeRenderer.rect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }

        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(playerX * CELL_SIZE, playerY * CELL_SIZE, CELL_SIZE, CELL_SIZE);

        for (NPC npc : currentRoom.getNpcs()) {
            shapeRenderer.setColor(npc.hasReceivedCrystal() ? Color.GREEN : Color.MAGENTA);
            shapeRenderer.rect(npc.getX() * CELL_SIZE, npc.getY() * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }

        shapeRenderer.end();

        spriteBatch.begin();
        font.setColor(Color.WHITE);
        font.draw(spriteBatch, "Crystals: " + game.getCrystalInventory().getCrystals(), 10, Gdx.graphics.getHeight() - 10);
        spriteBatch.end();
    }

    private void renderDialogue() {
        spriteBatch.begin();
        float x = 20;
        float y = 180;

        // Typing effect
        charTimer += Gdx.graphics.getDeltaTime();
        if (charIndex < currentDialogueNode.text.length()) {
            if (charTimer >= CHAR_DELAY) {
                displayedText += currentDialogueNode.text.charAt(charIndex++);
                charTimer = 0;
            }
        }

        font.setColor(Color.WHITE);
        font.draw(spriteBatch, displayedText, x, y);

        if (charIndex >= currentDialogueNode.text.length() && currentDialogueNode.choices != null) {
            for (int i = 0; i < currentDialogueNode.choices.length; i++) {
                String prefix = (i == selectedChoiceIndex ? "> " : "  ");
                font.draw(spriteBatch, prefix + (i + 1) + ") " + currentDialogueNode.choices[i].choiceText, x, y - (30 * (i + 1)));
            }
        }

        spriteBatch.end();
    }

    private void handleMovement() {
        int nextX = playerX;
        int nextY = playerY;

        if (moveUp) nextY++;
        if (moveDown) nextY--;
        if (moveLeft) nextX--;
        if (moveRight) nextX++;

        if (isValidMove(nextX, nextY)) {
            playerX = nextX;
            playerY = nextY;
        }

        if (currentRoom.getGrid()[playerX][playerY] == 2) {
            Room next = currentRoom.getConnectedFromDoorAt(playerX, playerY);
            if (next != null) {
                currentRoom = next;
                playerX = 2;
                playerY = 2;
                currentDialogueNode = null;
                dialogueNPC = null;
                displayedText = "";
                charIndex = 0;

                if (currentRoom.getRoomType() == RoomType.FINAL && !endingShown) {
                    triggerEnding();
                }
            }
        }
    }

    private void triggerEnding() {
        int used = game.getUsageLog().totalGiven();
        String ending;

        if (used == 0) {
            ending = "Ending: A Place That No Longer Exists\nYou remembered, but never acted.";
        } else if (used == 5) {
            ending = "Ending: Petals in the Void\nYou gave all. And something beautiful bloomed.";
        } else {
            ending = "Ending: The Keeper Becomes Stone\nSome memories returned, others stayed buried.";
        }

        game.setScreen(new EndingScreen(ending));
        endingShown = true;
    }

    private boolean isValidMove(int x, int y) {
        int[][] grid = currentRoom.getGrid();
        if (x < 0 || x >= grid.length || y < 0 || y >= grid[0].length) return false;
        if (grid[x][y] == 1) return false;

        for (NPC npc : currentRoom.getNpcs()) {
            if (npc.getX() == x && npc.getY() == y) return false;
        }
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.F5) {
            game.saveGame();
            return true;
        }

        if (currentDialogueNode != null) {
            if (keycode == Input.Keys.ENTER) {
                if (charIndex < currentDialogueNode.text.length()) {
                    displayedText = currentDialogueNode.text;
                    charIndex = currentDialogueNode.text.length();
                    return true;
                } else if (currentDialogueNode.endDialogue) {
                    currentDialogueNode = null;
                    dialogueNPC = null;
                    displayedText = "";
                    charIndex = 0;
                    return true;
                }
            }

            if (keycode >= Input.Keys.NUM_1 && keycode <= Input.Keys.NUM_9) {
                int index = keycode - Input.Keys.NUM_1;
                if (currentDialogueNode.choices != null && index < currentDialogueNode.choices.length) {
                    DialogueNode next = currentDialogueNode.choices[index].next;

                    if (next.requiresCrystal && dialogueNPC != null && !dialogueNPC.hasReceivedCrystal()) {
                        if (!game.getCrystalInventory().useCrystal()) return true;
                        game.getUsageLog().logCrystalGiven(dialogueNPC.getName());
                    }

                    currentDialogueNode = next;
                    if (dialogueNPC != null) dialogueNPC.setLastDialogue(currentDialogueNode);
                    displayedText = "";
                    charIndex = 0;
                }
                return true;
            }
        }

        if (keycode == Input.Keys.W) moveUp = true;
        if (keycode == Input.Keys.S) moveDown = true;
        if (keycode == Input.Keys.A) moveLeft = true;
        if (keycode == Input.Keys.D) moveRight = true;

        if (keycode == Input.Keys.SPACE) {
            for (NPC npc : currentRoom.getNpcs()) {
                if (isAdjacent(npc)) {
                    dialogueNPC = npc;
                    currentDialogueNode = npc.getLastDialogue() != null ? npc.getLastDialogue() : npc.getRootDialogue();
                    selectedChoiceIndex = 0;
                    displayedText = "";
                    charIndex = 0;
                    break;
                }
            }
        }

        return true;
    }

    private boolean isAdjacent(NPC npc) {
        int dx = Math.abs(npc.getX() - playerX);
        int dy = Math.abs(npc.getY() - playerY);
        return (dx + dy == 1);
    }

    @Override public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.W) moveUp = false;
        if (keycode == Input.Keys.S) moveDown = false;
        if (keycode == Input.Keys.A) moveLeft = false;
        if (keycode == Input.Keys.D) moveRight = false;
        return false;
    }

    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        font.dispose();
    }
}
