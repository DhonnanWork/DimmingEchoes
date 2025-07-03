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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
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

public class DungeonScreen extends InputAdapter implements Screen {

    private final TheDimmingEcho game;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;

    private final Stage uiStage;
    private final Skin skin;
    private final Table dialogueTable;
    private final Label dialogueTextLabel;
    private final Table choicesTable;

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

    private int selectedChoiceIndex = 0;

    private String fullDialogueText = "";
    private float charTimer = 0;
    private int charIndex = 0;
    private final float CHAR_DELAY = 0.03f;

    public DungeonScreen(TheDimmingEcho game) {
        this.game = game;
        this.shapeRenderer = new ShapeRenderer();
        this.spriteBatch = new SpriteBatch();

        gameCamera = new OrthographicCamera();
        gameViewport = new FitViewport(1280, 720, gameCamera);

        skin = new Skin();
        BitmapFont font = new BitmapFont();
        skin.add("default-font", font, BitmapFont.class);
        skin.load(Gdx.files.internal("uiskin.json"));

        uiStage = new Stage(new ScreenViewport());
        dialogueTable = new Table(skin);
        dialogueTable.setFillParent(true);
        dialogueTable.bottom().padBottom(50);
        uiStage.addActor(dialogueTable);

        dialogueTextLabel = new Label("", skin);
        dialogueTextLabel.setWrap(true);
        dialogueTextLabel.setAlignment(Align.left);

        choicesTable = new Table(skin);
        choicesTable.left();

        dialogueTable.add(dialogueTextLabel).expandX().fillX().left().padLeft(20).padRight(20);
        dialogueTable.row();
        dialogueTable.add(choicesTable).left().padTop(20).padLeft(20);
        dialogueTable.setVisible(false);

        uiStage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (currentDialogueNode == null) return false;

                if (keycode == Input.Keys.ENTER) {
                    if (charIndex < fullDialogueText.length()) {
                        charIndex = fullDialogueText.length();
                        dialogueTextLabel.setText(fullDialogueText);
                        populateChoices();
                        return true;
                    }
                    if (choicesTable.getChildren().size > 0) {
                        DialogueChoice selectedChoice = currentDialogueNode.choices[selectedChoiceIndex];
                        processDialogueChoice(selectedChoice);
                        return true;
                    }
                    endDialogue();
                    return true;
                }

                if (choicesTable.getChildren().size > 0) {
                    if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
                        selectedChoiceIndex = (selectedChoiceIndex - 1 + choicesTable.getChildren().size) % choicesTable.getChildren().size;
                        updateChoiceHighlight();
                        return true;
                    }
                    if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
                        selectedChoiceIndex = (selectedChoiceIndex + 1) % choicesTable.getChildren().size;
                        updateChoiceHighlight();
                        return true;
                    }
                }
                return false;
            }
        });

        this.roomGraph = new RoomGraph();
        this.currentRoom = roomGraph.getStartingRoom();
        this.player = new Rectangle(
            gameViewport.getWorldWidth() / 2f - PLAYER_SIZE / 2f,
            gameViewport.getWorldHeight() / 2f - PLAYER_SIZE / 2f,
            PLAYER_SIZE, PLAYER_SIZE
        );

        Gdx.input.setInputProcessor(this);
    }

    private void updateChoiceHighlight() {
        for (int i = 0; i < choicesTable.getChildren().size; i++) {
            TextButton button = (TextButton) choicesTable.getChildren().get(i);
            button.setColor(i == selectedChoiceIndex ? Color.GOLD : Color.WHITE);
        }
    }

    private void populateChoices() {
        choicesTable.clear();
        if (currentDialogueNode.choices != null && currentDialogueNode.choices.length > 0) {
            for (final DialogueChoice choice : currentDialogueNode.choices) {
                TextButton choiceButton = new TextButton(choice.choiceText, skin);
                choiceButton.getLabel().setAlignment(Align.left);
                choiceButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        processDialogueChoice(choice);
                    }
                });
                choicesTable.add(choiceButton).left().row();
            }
            selectedChoiceIndex = 0;
            updateChoiceHighlight();
            // --- FIX: Force keyboard focus to the choices table ---
            uiStage.setKeyboardFocus(choicesTable);
        } else {
            // If there are no choices, there's nothing to focus on.
            uiStage.setKeyboardFocus(null);
        }
    }

    private void processDialogueChoice(DialogueChoice choice) {
        choicesTable.clear();
        // --- FIX: Release keyboard focus when choices are gone ---
        uiStage.setKeyboardFocus(null);

        currentDialogueNode = choice.next;

        if (currentDialogueNode.requiresCrystal) {
            if (dialogueNPC != null && !dialogueNPC.hasReceivedCrystal(game)) {
                if (!game.getCrystalInventory().useCrystal()) {
                    endDialogue();
                    return;
                }
                game.getUsageLog().logCrystalGiven(dialogueNPC.getName());
            }
        }

        fullDialogueText = currentDialogueNode.text;
        charIndex = 0;
        dialogueTextLabel.setText("");
    }

    private void startDialogue(NPC npc) {
        moveUp = moveDown = moveLeft = moveRight = false;

        dialogueNPC = npc;
        // --- FIX: Call the new stateful dialogue method, no more lastDialogue ---
        currentDialogueNode = npc.getDialogue(game);
        fullDialogueText = currentDialogueNode.text;
        charIndex = 0;
        dialogueTextLabel.setText("");
        choicesTable.clear();
        dialogueTable.setVisible(true);
        Gdx.input.setInputProcessor(uiStage);

        if (charIndex < fullDialogueText.length()) {
            dialogueTextLabel.setText(fullDialogueText.substring(0, charIndex));
        } else {
            populateChoices();
        }
    }

    private void endDialogue() {
        dialogueTable.setVisible(false);
        // --- FIX: Ensure focus is released when dialogue ends ---
        uiStage.setKeyboardFocus(null);
        currentDialogueNode = null;
        dialogueNPC = null;
        Gdx.input.setInputProcessor(this);
    }

    // Unchanged methods from here...
    @Override
    public void render(float delta) {
        if (currentDialogueNode == null) {
            handleMovement(delta);
        } else {
            updateTypingEffect(delta);
        }
        Color bgColor = currentRoom.getBackgroundColor();
        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        gameViewport.apply();
        gameCamera.update();
        shapeRenderer.setProjectionMatrix(gameCamera.combined);
        renderGame();
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        renderStaticUI();
        uiStage.act(delta);
        uiStage.draw();
    }
    private void updateTypingEffect(float delta) {
        charTimer += delta;
        if (charIndex < fullDialogueText.length()) {
            if (charTimer >= CHAR_DELAY) {
                charIndex++;
                dialogueTextLabel.setText(fullDialogueText.substring(0, charIndex));
                charTimer = 0;
                if (charIndex == fullDialogueText.length()) {
                    populateChoices();
                }
            }
        }
    }
    private void renderGame() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        Color wallColor = currentRoom.getBackgroundColor().cpy().mul(0.8f);
        shapeRenderer.setColor(wallColor);
        for (Rectangle obstacle : currentRoom.getObstacles()) {
            shapeRenderer.rect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
        }
        shapeRenderer.setColor(Color.GOLD);
        for (DoorZone door : currentRoom.getDoorZones()) {
            shapeRenderer.rect(door.bounds.x, door.bounds.y, door.bounds.width, door.bounds.height);
        }
        for (NPC npc : currentRoom.getNpcs()) {
            shapeRenderer.setColor(npc.hasReceivedCrystal(game) ? Color.GREEN : Color.MAGENTA);
            Rectangle npcBounds = npc.getBounds();
            shapeRenderer.rect(npcBounds.x, npcBounds.y, npcBounds.width, npcBounds.height);
        }
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(player.x, player.y, player.width, player.height);
        shapeRenderer.end();
    }
    private void renderStaticUI() {
        spriteBatch.begin();
        BitmapFont font = skin.getFont("default-font");
        font.setColor(Color.WHITE);
        font.draw(spriteBatch, "Crystals: " + game.getCrystalInventory().getCrystals(), 10, Gdx.graphics.getHeight() - 10);
        font.draw(spriteBatch, "Press [F5] to Save", 10, Gdx.graphics.getHeight() - 35);
        font.draw(spriteBatch, "Press [SPACE] to interact", 10, Gdx.graphics.getHeight() - 60);
        spriteBatch.end();
    }
    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.W: moveUp = true; break;
            case Input.Keys.S: moveDown = true; break;
            case Input.Keys.A: moveLeft = true; break;
            case Input.Keys.D: moveRight = true; break;
            case Input.Keys.F5: game.saveGame(); return true;
            case Input.Keys.SPACE:
                for (NPC npc : currentRoom.getNpcs()) {
                    if (isNear(npc)) {
                        startDialogue(npc);
                        break;
                    }
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
    private void handleMovement(float delta) {
        float moveAmount = PLAYER_SPEED * delta;
        float oldX = player.x;
        float oldY = player.y;
        if (moveUp) player.y += moveAmount;
        if (moveDown) player.y -= moveAmount;
        if (moveLeft) player.x -= moveAmount;
        if (moveRight) player.x += moveAmount;
        for (DoorZone door : currentRoom.getDoorZones()) {
            if (player.overlaps(door.bounds)) {
                changeRoom(door);
                return;
            }
        }
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
    }
    private void changeRoom(DoorZone door) {
        currentRoom = door.leadsTo;
        if (dialogueTable.isVisible()) endDialogue();
        switch (door.entryDirection) {
            case LEFT: player.x = gameViewport.getWorldWidth() - player.width - 48f; player.y = gameViewport.getWorldHeight() / 2f; break;
            case RIGHT: player.x = 48f; player.y = gameViewport.getWorldHeight() / 2f; break;
            case TOP: player.x = gameViewport.getWorldWidth() / 2f; player.y = 48f; break;
            case BOTTOM: player.x = gameViewport.getWorldWidth() / 2f; player.y = gameViewport.getWorldHeight() - player.height - 48f; break;
        }
        if (currentRoom.getRoomType() == RoomType.FINAL && !endingShown) {
            triggerEnding();
        }
    }
    private void triggerEnding() {
        int used = game.getUsageLog().totalGiven();
        String ending;
        if (used == 0) {
            ending = "Ending: A Place That No Longer Exists\nYou remembered, but never acted.";
        } else if (used >= 3) { // Changed to check if at least the 3 main story NPCs are helped
            ending = "Ending: Petals in the Void\nYou gave all you could. And something beautiful bloomed.";
        } else {
            ending = "Ending: The Keeper Becomes Stone\nSome memories returned, others stayed buried.";
        }
        game.setScreen(new EndingScreen(ending));
        endingShown = true;
    }
    private boolean isNear(NPC npc) {
        Vector2 playerCenter = player.getCenter(new Vector2());
        Vector2 npcCenter = npc.getBounds().getCenter(new Vector2());
        return playerCenter.dst(npcCenter) < INTERACTION_RADIUS;
    }
    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        uiStage.getViewport().update(width, height, true);
        dialogueTable.invalidateHierarchy();
    }
    @Override public void dispose() { shapeRenderer.dispose(); spriteBatch.dispose(); skin.dispose(); uiStage.dispose(); }
    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
