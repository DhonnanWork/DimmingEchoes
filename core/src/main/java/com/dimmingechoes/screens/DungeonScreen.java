package com.dimmingEchoes.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.dimmingEchoes.TheDimmingEcho;
import com.dimmingEchoes.dialogue.DialogueChoice;
import com.dimmingEchoes.dialogue.DialogueNode;
import com.dimmingEchoes.dungeon.DoorZone;
import com.dimmingEchoes.dungeon.Room;
import com.dimmingEchoes.dungeon.RoomGraph;
import com.dimmingEchoes.dungeon.RoomType;
import com.dimmingEchoes.entities.NPC;

// We change this to InputAdapter for simpler input handling
public class DungeonScreen extends InputAdapter implements Screen {

    private final TheDimmingEcho game;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;

    // --- NEW: Scene2D UI System ---
    private final Stage uiStage;
    private final Skin skin;
    private final Table dialogueTable;
    private final Label dialogueTextLabel;
    private final Table choicesTable;
    private BitmapFont uiFont; // We still need a font for the top-left UI

    private OrthographicCamera gameCamera;
    private Viewport gameViewport;

    private Room currentRoom;
    private final RoomGraph roomGraph;

    private Rectangle player;
    private static final float PLAYER_SIZE = 40f;
    private static final float PLAYER_SPEED = 250f;
    private static final float INTERACTION_RADIUS = 64f;

    private boolean moveUp, moveDown, moveLeft, moveRight;

    private DialogueNode currentDialogueNode = null;
    private NPC dialogueNPC = null;
    private boolean endingShown = false;

    // Typing effect state
    private String fullDialogueText = "";
    private float charTimer = 0;
    private int charIndex = 0;
    private final float CHAR_DELAY = 0.03f;

    public DungeonScreen(TheDimmingEcho game) {
        this.game = game;
        this.shapeRenderer = new ShapeRenderer();
        this.spriteBatch = new SpriteBatch();

        // --- Camera and Viewport Setup ---
        gameCamera = new OrthographicCamera();
        gameViewport = new FitViewport(1280, 720, gameCamera);

        // --- Asset and Skin Loading (REVISED) ---
        // 1. Create an EMPTY skin.
        skin = new Skin();

// 2. Create a basic, default font that requires no external files.
        BitmapFont font = new BitmapFont();

// 3. Add the default font to the skin.
        skin.add("default-font", font, BitmapFont.class);

// 4. Load the JSON file. The skin will now use the basic font.
        skin.load(Gdx.files.internal("uiskin.json"));


        // --- Scene2D Setup (NEW) --
        uiStage = new Stage(new ScreenViewport());
        dialogueTable = new Table(skin);
        dialogueTable.setFillParent(true); // Make the table fill the screen
        dialogueTable.bottom().padBottom(50); // Align it to the bottom
        uiStage.addActor(dialogueTable);

        // Create the UI components once
        dialogueTextLabel = new Label("", skin);
        dialogueTextLabel.setWrap(true);
        choicesTable = new Table();

        // Add them to the main dialogue table
        dialogueTable.add(dialogueTextLabel).width(Gdx.graphics.getWidth() - 100).left();
        dialogueTable.row();
        dialogueTable.add(choicesTable).padTop(20).left();
        dialogueTable.setVisible(false); // Hide it initially

        // --- Game World Setup ---
        this.roomGraph = new RoomGraph();
        this.currentRoom = roomGraph.getStartingRoom();
        this.player = new Rectangle(
            gameViewport.getWorldWidth() / 2f - PLAYER_SIZE / 2f,
            gameViewport.getWorldHeight() / 2f - PLAYER_SIZE / 2f,
            PLAYER_SIZE, PLAYER_SIZE
        );

        Gdx.input.setInputProcessor(this); // Set input to this screen (for movement)
    }

    @Override
    public void render(float delta) {
        if (currentDialogueNode == null) {
            handleMovement(delta);
        } else {
            updateTypingEffect(delta);
        }

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // --- Game World Rendering ---
        gameViewport.apply();
        gameCamera.update();
        shapeRenderer.setProjectionMatrix(gameCamera.combined);
        renderGame();

        // --- UI Rendering ---
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        renderStaticUI();

        // --- Scene2D UI Rendering (NEW) ---
        uiStage.act(delta); // Update the stage
        uiStage.draw();     // Draw the stage (our dialogue box)
    }

    private void updateTypingEffect(float delta) {
        charTimer += delta;
        if (charIndex < fullDialogueText.length()) {
            if (charTimer >= CHAR_DELAY) {
                charIndex++;
                dialogueTextLabel.setText(fullDialogueText.substring(0, charIndex));
                charTimer = 0;

                // If finished typing, show choices
                if (charIndex == fullDialogueText.length()) {
                    populateChoices();
                }
            }
        }
    }

    private void renderGame() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.DARK_GRAY);
        for (Rectangle obstacle : currentRoom.getObstacles()) {
            shapeRenderer.rect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
        }
        shapeRenderer.setColor(Color.GOLD);
        for (DoorZone door : currentRoom.getDoorZones()) {
            shapeRenderer.rect(door.bounds.x, door.bounds.y, door.bounds.width, door.bounds.height);
        }
        for (NPC npc : currentRoom.getNpcs()) {
            shapeRenderer.setColor(npc.hasReceivedCrystal() ? Color.GREEN : Color.MAGENTA);
            Rectangle npcBounds = npc.getBounds();
            shapeRenderer.rect(npcBounds.x, npcBounds.y, npcBounds.width, npcBounds.height);
        }
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(player.x, player.y, player.width, player.height);
        shapeRenderer.end();
    }

    private void renderStaticUI() {
        // Get the font directly from the skin
        BitmapFont font = skin.getFont("default-font");

        spriteBatch.begin();
        font.setColor(Color.WHITE);
        font.draw(spriteBatch, "Crystals: " + game.getCrystalInventory().getCrystals(), 10, Gdx.graphics.getHeight() - 10);
        font.draw(spriteBatch, "Press [F5] to Save", 10, Gdx.graphics.getHeight() - 35);
        font.draw(spriteBatch, "Press [SPACE] to interact", 10, Gdx.graphics.getHeight() - 60);
        spriteBatch.end();
    }

    private void startDialogue(NPC npc) {
        dialogueNPC = npc;
        currentDialogueNode = npc.getLastDialogue() != null ? npc.getLastDialogue() : npc.getRootDialogue();

        fullDialogueText = currentDialogueNode.text;
        charIndex = 0;
        dialogueTextLabel.setText("");
        choicesTable.clear(); // Clear old choices

        dialogueTable.setVisible(true);
        Gdx.input.setInputProcessor(uiStage); // Switch input to the UI
    }

    private void processDialogueChoice(DialogueChoice choice) {
        DialogueNode next = choice.next;

        if (next.requiresCrystal && dialogueNPC != null && !dialogueNPC.hasReceivedCrystal()) {
            if (!game.getCrystalInventory().useCrystal()) {
                // In a real game, you might show a "Not enough crystals" message.
                // For now, we just won't proceed.
                return;
            }
            game.getUsageLog().logCrystalGiven(dialogueNPC.getName());
        }

        currentDialogueNode = next;
        if(dialogueNPC != null) dialogueNPC.setLastDialogue(currentDialogueNode);

        // If this is an end node, close the dialogue. Otherwise, start the next line.
        if (currentDialogueNode.endDialogue) {
            endDialogue();
        } else {
            fullDialogueText = currentDialogueNode.text;
            charIndex = 0;
            dialogueTextLabel.setText("");
            choicesTable.clear();
        }
    }

    private void populateChoices() {
        choicesTable.clear();
        if (currentDialogueNode.choices != null && currentDialogueNode.choices.length > 0) {
            for (final DialogueChoice choice : currentDialogueNode.choices) {
                TextButton choiceButton = new TextButton(choice.choiceText, skin);
                choiceButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        processDialogueChoice(choice);
                    }
                });
                choicesTable.add(choiceButton).left().row();
            }
        }
    }


    private void endDialogue() {
        dialogueTable.setVisible(false);
        currentDialogueNode = null;
        dialogueNPC = null;
        Gdx.input.setInputProcessor(this); // Give input control back to the player
    }

    @Override
    public boolean keyDown(int keycode) {
        // This input only runs when dialogue is NOT active
        switch (keycode) {
            case Input.Keys.W: moveUp = true; break;
            case Input.Keys.S: moveDown = true; break;
            case Input.Keys.A: moveLeft = true; break;
            case Input.Keys.D: moveRight = true; break;
            case Input.Keys.F5:
                game.saveGame();
                return true;
            case Input.Keys.SPACE:
                for (NPC npc : currentRoom.getNpcs()) {
                    if (isNear(npc)) {
                        startDialogue(npc);
                        break;
                    }
                }
                return true;
            case Input.Keys.ENTER:
                // If dialogue is active (which it shouldn't be here, but for safety)
                // we can add a "finish typing" shortcut.
                if (currentDialogueNode != null && charIndex < fullDialogueText.length()) {
                    charIndex = fullDialogueText.length();
                    dialogueTextLabel.setText(fullDialogueText);
                    populateChoices();
                }
                return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.W: moveUp = false; break;
            case Input.Keys.S: moveDown = false; break;
            case Input.Keys.A: moveLeft = false; break;
            case Input.Keys.D: moveRight = false; break;
        }
        return false;
    }

    // --- HELPER AND UNCHANGED METHODS ---

    private void handleMovement(float delta) {
        // (This method is unchanged from your original)
        float moveAmount = PLAYER_SPEED * delta;
        float oldX = player.x;
        float oldY = player.y;

        if (moveUp) player.y += moveAmount;
        if (moveDown) player.y -= moveAmount;
        if (moveLeft) player.x -= moveAmount;
        if (moveRight) player.x += moveAmount;

        if (player.x != oldX) {
            for (Rectangle obstacle : currentRoom.getObstacles()) {
                if (player.overlaps(obstacle)) {
                    player.x = oldX;
                    break;
                }
            }
        }
        if (player.y != oldY) {
            for (Rectangle obstacle : currentRoom.getObstacles()) {
                if (player.overlaps(obstacle)) {
                    player.y = oldY;
                    break;
                }
            }
        }

        for (DoorZone door : currentRoom.getDoorZones()) {
            if (player.overlaps(door.bounds)) {
                changeRoom(door);
                return;
            }
        }
    }

    private void changeRoom(DoorZone door) {
        // (This method is unchanged from your original)
        currentRoom = door.leadsTo;
        if (dialogueTable.isVisible()) endDialogue();

        switch (door.entryDirection) {
            case LEFT:
                player.x = gameViewport.getWorldWidth() - player.width - 48f;
                player.y = gameViewport.getWorldHeight() / 2f;
                break;
            case RIGHT:
                player.x = 48f;
                player.y = gameViewport.getWorldHeight() / 2f;
                break;
            case TOP:
                player.x = gameViewport.getWorldWidth() / 2f;
                player.y = 48f;
                break;
            case BOTTOM:
                player.x = gameViewport.getWorldWidth() / 2f;
                player.y = gameViewport.getWorldHeight() - player.height - 48f;
                break;
        }

        if (currentRoom.getRoomType() == RoomType.FINAL && !endingShown) {
            triggerEnding();
        }
    }

    private void triggerEnding() {
        // (This method is unchanged from your original)
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

    private boolean isNear(NPC npc) {
        // (This method is unchanged from your original)
        Vector2 playerCenter = player.getCenter(new Vector2());
        Vector2 npcCenter = npc.getBounds().getCenter(new Vector2());
        return playerCenter.dst(npcCenter) < INTERACTION_RADIUS;
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        uiStage.getViewport().update(width, height, true);
        // We must also update the layout of our dialogue table
        dialogueTable.invalidateHierarchy();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        skin.dispose();
        uiStage.dispose();
       // uiFont.dispose();
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
