// File: core/src/main/java/com/dimmingechoes/screens/DungeonScreen.java

package com.dimmingechoes.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
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
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.dimmingechoes.TheDimmingEcho;
import com.dimmingechoes.dialogue.DialogueChoice;
import com.dimmingechoes.dialogue.DialogueNode;
import com.dimmingechoes.dungeon.DoorZone;
import com.dimmingechoes.dungeon.Room;
import com.dimmingechoes.dungeon.RoomGraph;
import com.dimmingechoes.dungeon.RoomType;
import com.dimmingechoes.entities.NPC;
import com.badlogic.gdx.math.MathUtils;
import com.dimmingechoes.manager.AudioManager;

public class DungeonScreen extends InputAdapter implements Screen {

    // --- All fields ---
    private final TheDimmingEcho game;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;
    private final Stage uiStage;
    private final Skin skin;
    private final Table dialogueTable;
    private final Label dialogueTextLabel;
    private final Table choicesTable;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera gameCamera;
    private Viewport gameViewport;
    private Room currentRoom;
    private final RoomGraph roomGraph;
    private Rectangle player;
    private static final float PLAYER_SIZE = 50f;
    private Texture playerSpriteSheet;
    private Animation<TextureRegion> playerAnimation;
    private final Label speakerNameLabel;
    private float stateTime;
    private static final float PLAYER_SPEED = 250f;
    private static final float INTERACTION_RADIUS = 64f;
    private boolean moveUp, moveDown, moveLeft, moveRight;
    private boolean isFacingRight = true;
    private DialogueNode currentDialogueNode = null;
    private NPC dialogueNPC = null;
    private boolean endingShown = false;
    private int selectedChoiceIndex = 0;
    private String fullDialogueText = "";
    private float charTimer = 0;
    private int charIndex = 0;
    private final float CHAR_DELAY = 0.03f;
    private float mapScale;
    private boolean isMemoryPuzzleSolved = false;
    private Rectangle puzzleTriggerBounds = null;
    private boolean awaitingFinalChoice = false;
    private String finalChoiceResult = null;

    // --- NEW: ShapeRenderer for Debugging ---
    private final ShapeRenderer debugRenderer;

    public DungeonScreen(TheDimmingEcho game) {
        this.game = game;
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        // --- NEW: Initialize Debug Renderer ---
        debugRenderer = new ShapeRenderer();

        gameCamera = new OrthographicCamera();
        gameViewport = new FitViewport(1280, 720, gameCamera);

        skin = new Skin();
        skin.addRegions(new TextureAtlas(Gdx.files.internal("uiskin.atlas")));
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("LibertinusMono-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 20;
        parameter.color = Color.WHITE;
        BitmapFont defaultFont = generator.generateFont(parameter);
        skin.add("default-font", defaultFont);
        parameter.size = 22;
        parameter.color = Color.GOLD;
        BitmapFont nameFont = generator.generateFont(parameter);
        skin.add("name-font", nameFont);
        generator.dispose();

        Label.LabelStyle defaultLabelStyle = new Label.LabelStyle(defaultFont, Color.WHITE);
        skin.add("default", defaultLabelStyle);
        Label.LabelStyle nameLabelStyle = new Label.LabelStyle(nameFont, Color.GOLD);
        skin.add("name-style", nameLabelStyle);
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = defaultFont;
        skin.add("default", textButtonStyle);

        uiStage = new Stage(new ScreenViewport());
        dialogueTable = new Table(skin);
        speakerNameLabel = new Label("", skin, "name-style");
        dialogueTextLabel = new Label("", skin);
        dialogueTextLabel.setWrap(true);
        dialogueTextLabel.setAlignment(Align.left);
        choicesTable = new Table(skin);
        choicesTable.left();
        dialogueTable.add(speakerNameLabel).expandX().left().padTop(10).padLeft(15).row();
        dialogueTable.add(dialogueTextLabel).expand().fill().left().pad(5, 15, 10, 15).row();
        dialogueTable.add(choicesTable).expandX().left().padBottom(10).padLeft(15);
        dialogueTable.setVisible(false);
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        uiStage.addActor(rootTable);
        rootTable.add(dialogueTable).width(Gdx.graphics.getWidth() * 0.8f).height(Gdx.graphics.getHeight() * 0.3f).bottom().padBottom(20);

        uiStage.addListener(new InputListener() {
            @Override public boolean keyDown(InputEvent event, int keycode) {
                if (currentDialogueNode == null) return false;
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    if (charIndex < fullDialogueText.length()) {
                        charIndex = fullDialogueText.length();
                        dialogueTextLabel.setText(fullDialogueText);
                        populateChoices();
                        return true;
                    }
                    if (choicesTable.getChildren().size > 0) {
                        processDialogueChoice(currentDialogueNode.choices[selectedChoiceIndex]);
                        return true;
                    }
                    if (currentDialogueNode.endDialogue || currentDialogueNode.choices.length == 0) {
                        endDialogue();
                    }
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
        this.player = new Rectangle(0, 0, PLAYER_SIZE, PLAYER_SIZE);
    }

    // ... (rest of the constructor and other methods are unchanged) ...

    @Override
    public void render(float delta) {
        stateTime += delta;
        if (currentDialogueNode == null) {
            handleMovement(delta);
        } else {
            updateTypingEffect(delta);
        }

        Color bgColor = currentRoom.getBackgroundColor();
        ScreenUtils.clear(bgColor.r, bgColor.g, bgColor.b, bgColor.a);

        gameCamera.position.set(player.x + player.width / 2, player.y + player.height / 2, 0);
        clampCamera();
        gameCamera.update();
        gameViewport.apply();

        renderer.setView(gameCamera);
        renderer.render();

        renderGameObjects();

        // --- NEW: Call the debug rendering method ---
        renderDebugShapes();

        // --- UI Rendering ---
        if (dialogueTable.isVisible()) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.setProjectionMatrix(uiStage.getCamera().combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, 0.7f);
            shapeRenderer.rect(dialogueTable.getX(), dialogueTable.getY(), dialogueTable.getWidth(), dialogueTable.getHeight());
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        uiStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        renderStaticUI();
        uiStage.act(delta);
        uiStage.draw();
    }

    // --- NEW: Debug Rendering Method ---
    private void renderDebugShapes() {
        // Enable blending for transparency
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        debugRenderer.setProjectionMatrix(gameCamera.combined);
        debugRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw DoorZones in a semi-transparent green
        debugRenderer.setColor(0, 1, 0, 0.35f); // Green with 35% opacity
        for (DoorZone door : currentRoom.getDoorZones()) {
            debugRenderer.rect(door.bounds.x, door.bounds.y, door.bounds.width, door.bounds.height);
        }

        debugRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        skin.dispose();
        uiStage.dispose();
        if (map != null) map.dispose();
        if (renderer != null) renderer.dispose();
        if (playerSpriteSheet != null) playerSpriteSheet.dispose();
        // --- NEW: Dispose the debug renderer ---
        debugRenderer.dispose();
    }

    // --- All other methods remain unchanged ---

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        loadMap(currentRoom.getTmxPath());
        playerSpriteSheet = new Texture(Gdx.files.internal("player_walk_left.png"));
        int FRAME_COLS = 8, FRAME_ROWS = 1;
        int frameWidth = playerSpriteSheet.getWidth() / FRAME_COLS;
        int frameHeight = playerSpriteSheet.getHeight() / FRAME_ROWS;
        TextureRegion[][] tmp = TextureRegion.split(playerSpriteSheet, frameWidth, frameHeight);
        TextureRegion[] walkFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
        int index = 0;
        for (int i = 0; i < FRAME_ROWS; i++) { for (int j = 0; j < FRAME_COLS; j++) { walkFrames[index++] = tmp[i][j]; } }
        playerAnimation = new Animation<>(0.1f, walkFrames);
        stateTime = 0f;
        AudioManager.getInstance().playMusic("audio/proximity to the inevitable.mp3", true);
    }

    private void loadMap(String tmxPath) {
        if (map != null) map.dispose();
        map = new TmxMapLoader().load(tmxPath);
        int mapWidthInPixels = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class);
        this.mapScale = gameViewport.getWorldWidth() / mapWidthInPixels;
        renderer = new OrthogonalTiledMapRenderer(map, this.mapScale);
        parseTiledMapObjects();
    }

    private void parseTiledMapObjects() {
        currentRoom.getObstacles().clear();
        currentRoom.getNpcs().clear();
        puzzleTriggerBounds = null;

        if (map.getLayers().get("Collision") != null) {
            for (MapObject object : map.getLayers().get("Collision").getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    currentRoom.addObstacle(new Rectangle(rect.x * mapScale, rect.y * mapScale, rect.width * mapScale, rect.height * mapScale));
                }
            }
        }

        for (MapLayer layer : map.getLayers()) {
            if (layer.getObjects() == null) continue;
            if (layer.getName().equalsIgnoreCase("Spawns")) continue;

            for (MapObject object : layer.getObjects()) {
                String type = object.getProperties().get("type", String.class);
                if (type != null && type.equals("NPC")) {
                    String name = object.getName();
                    Integer gid = object.getProperties().get("gid", Integer.class);
                    if (name == null || name.trim().isEmpty()) {
                        Gdx.app.log("MAP_PARSER_ERROR", "Found an NPC-type object on layer '" + layer.getName() + "' that has no Name. Skipping.");
                        continue;
                    }
                    if (gid == null) {
                        Gdx.app.log("MAP_PARSER_ERROR", "NPC '" + name + "' is a RECTANGLE/SHAPE object, not a TILE object. It has no image (GID). Please delete it in Tiled and replace it by stamping a tile from your tileset. Skipping.");
                        continue;
                    }
                    float x = object.getProperties().get("x", Float.class) * mapScale;
                    float y = object.getProperties().get("y", Float.class) * mapScale;
                    currentRoom.addNPC(new NPC(name, currentRoom.getRoomType(), x, y, gid));
                }
            }
        }

        boolean playerSpawned = false;
        if (map.getLayers().get("Spawns") != null) {
            for (MapObject object : map.getLayers().get("Spawns").getObjects()) {
                String type = object.getProperties().get("type", String.class);
                if (type != null && type.equals("Player")) {
                    float x = object.getProperties().get("x", Float.class) * mapScale;
                    float y = object.getProperties().get("y", Float.class) * mapScale;
                    player.setPosition(x, y);
                    playerSpawned = true;
                    break;
                }
            }
        }

        if (map.getLayers().get("Interactables") != null) {
            for (MapObject object : map.getLayers().get("Interactables").getObjects()) {
                if (object.getName() != null && object.getName().equals("PuzzleTrigger")) {
                    if (object instanceof RectangleMapObject) {
                        Rectangle rect = ((RectangleMapObject) object).getRectangle();
                        puzzleTriggerBounds = new Rectangle(rect.x * mapScale, rect.y * mapScale, rect.width * mapScale, rect.height * mapScale);
                    }
                }
            }
        }

        if (!playerSpawned) {
            player.setPosition(gameViewport.getWorldWidth() / 2f - PLAYER_SIZE / 2, gameViewport.getWorldHeight() / 2f - PLAYER_SIZE / 2);
            Gdx.app.log("DungeonScreen", "Warning: No 'Player' spawn object found in 'Spawns' layer. Spawning at center.");
        }
    }

    private void renderGameObjects() {
        spriteBatch.setProjectionMatrix(gameCamera.combined);
        spriteBatch.begin();

        for (NPC npc : currentRoom.getNpcs()) {
            TiledMapTile tile = map.getTileSets().getTile(npc.gid);
            if (tile != null) {
                TextureRegion region = tile.getTextureRegion();
                float tileWidth = region.getRegionWidth() * mapScale;
                float tileHeight = region.getRegionHeight() * mapScale;
                spriteBatch.draw(region, npc.getX(), npc.getY(), tileWidth, tileHeight);
            }
        }

        TextureRegion currentFrame = playerAnimation.getKeyFrame(stateTime, true);
        if (isFacingRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        } else if (!isFacingRight && currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }
        spriteBatch.draw(currentFrame, player.x, player.y, PLAYER_SIZE, PLAYER_SIZE);

        spriteBatch.end();
    }

    private void renderStaticUI() {
        spriteBatch.setProjectionMatrix(uiStage.getCamera().combined);
        spriteBatch.begin();
        BitmapFont font = skin.getFont("default-font");
        font.setColor(Color.WHITE);
        font.draw(spriteBatch, "Crystals: " + game.getCrystalInventory().getCrystals(), 10, Gdx.graphics.getHeight() - 10);
        font.draw(spriteBatch, "Press [SPACE] to interact", 10, Gdx.graphics.getHeight() - 35);
        font.draw(spriteBatch, "Press [ESC] to pause", 10, Gdx.graphics.getHeight() - 60);
        spriteBatch.end();
    }

    private void handleMovement(float delta) {
        float moveAmount = PLAYER_SPEED * delta;
        float oldX = player.x;
        float oldY = player.y;

        if (moveLeft) { player.x -= moveAmount; isFacingRight = false; }
        if (moveRight) { player.x += moveAmount; isFacingRight = true; }
        if (moveUp) player.y += moveAmount;
        if (moveDown) player.y -= moveAmount;

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

        if (currentRoom.getTmxPath() == null || currentRoom.getTmxPath().isEmpty()) {
            if (currentRoom.getRoomType() == RoomType.FINAL && !awaitingFinalChoice) {
                // Check for 'A Place That No Longer Exists' ending
                if (game.getUsageLog().totalGiven() == 0 && game.getCrystalsLostToFailure() == 0) {
                    game.setScreen(new EndingScreen(game, "NOMATTER"));
                    endingShown = true;
                    return;
                }
                presentFinalChoice();
            }
            return;
        }

        loadMap(currentRoom.getTmxPath());
    }

    @Override
    public boolean keyDown(int keycode) {
        if (currentDialogueNode != null) return false;
        switch (keycode) {
            case Input.Keys.W: case Input.Keys.UP: moveUp = true; break;
            case Input.Keys.S: case Input.Keys.DOWN: moveDown = true; break;
            case Input.Keys.A: case Input.Keys.LEFT: moveLeft = true; break;
            case Input.Keys.D: case Input.Keys.RIGHT: moveRight = true; break;
            case Input.Keys.ESCAPE: game.setScreen(new PauseMenuScreen(game, this)); return true;
            case Input.Keys.SPACE:
                if (!isMemoryPuzzleSolved && puzzleTriggerBounds != null && player.overlaps(puzzleTriggerBounds)) {
                    isMemoryPuzzleSolved = true;
                    return true;
                }
                for (NPC npc : currentRoom.getNpcs()) {
                    if (isNear(npc)) {
                        startDialogue(npc);
                        return true;
                    }
                }
                break;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.W: case Input.Keys.UP: moveUp = false; break;
            case Input.Keys.S: case Input.Keys.DOWN: moveDown = false; break;
            case Input.Keys.A: case Input.Keys.LEFT: moveLeft = false; break;
            case Input.Keys.D: case Input.Keys.RIGHT: moveRight = false; break;
        }
        return false;
    }

    private void startDialogue(NPC npc) {
        moveUp = moveDown = moveLeft = moveRight = false;
        dialogueNPC = npc;
        currentDialogueNode = npc.getDialogue(game);
        speakerNameLabel.setText(npc.getName());
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
        speakerNameLabel.setText("");
        uiStage.setKeyboardFocus(null);
        currentDialogueNode = null;
        dialogueNPC = null;
        Gdx.input.setInputProcessor(this);
    }

    private void processDialogueChoice(DialogueChoice choice) {
        choicesTable.clear();
        uiStage.setKeyboardFocus(null);

        // Launch WordleScreen if the Laughing Girl's riddle is attempted
        if (dialogueNPC != null && dialogueNPC.getName().equals("The Laughing Girl") && choice.choiceText.equals("[Attempt the riddle]")) {
            endDialogue();
            game.setScreen(new WordleScreen(game, this));
            return;
        }

        if (dialogueNPC != null && dialogueNPC.getName().equals("The Stranger") && choice.choiceText.equals("Begin Battle")) {
            com.dimmingechoes.entities.Player playerEntity = new com.dimmingechoes.entities.Player("You", 30, 8, 3, null);
            java.util.List<com.dimmingechoes.entities.Enemy> enemies = new java.util.ArrayList<>();
            enemies.add(new com.dimmingechoes.entities.Enemy("The Stranger", 20, 6, 2, null));
            game.setScreen(new BattleScreen(game, playerEntity, enemies));
            return;
        }

        if (awaitingFinalChoice) {
            finalChoiceResult = choice.choiceText;
            awaitingFinalChoice = false;
            endDialogue();
            triggerEndingWithChoice(finalChoiceResult);
            return;
        }

        if (choice.next != null) {
            currentDialogueNode = choice.next;
            if (currentDialogueNode.requiresCrystal) {
                if (dialogueNPC != null && !dialogueNPC.hasReceivedCrystal(game)) {
                    if (game.getCrystalInventory().useCrystal()) {
                        game.getUsageLog().logCrystalGiven(dialogueNPC.getName());
                    } else {
                        endDialogue();
                        return;
                    }
                }
            }
            fullDialogueText = currentDialogueNode.text;
            charIndex = 0;
            dialogueTextLabel.setText("");
            if (currentDialogueNode.endDialogue) {
                populateChoices();
            }
        } else {
            endDialogue();
        }
    }

    private void populateChoices() {
        choicesTable.clear();
        if (currentDialogueNode != null && currentDialogueNode.choices != null && currentDialogueNode.choices.length > 0) {
            for (final DialogueChoice choice : currentDialogueNode.choices) {
                TextButton choiceButton = new TextButton(choice.choiceText, skin);
                choiceButton.getLabel().setAlignment(Align.left);
                choiceButton.addListener(new ClickListener() { @Override public void clicked(InputEvent event, float x, float y) { processDialogueChoice(choice); } });
                choicesTable.add(choiceButton).left().row();
            }
            selectedChoiceIndex = 0;
            updateChoiceHighlight();
            uiStage.setKeyboardFocus(choicesTable);
        } else {
            uiStage.setKeyboardFocus(null);
        }
    }

    private void presentFinalChoice() {
        awaitingFinalChoice = true;
        DialogueNode end = new DialogueNode("...", null, false, false, true);
        DialogueNode giveChoice = new DialogueNode("You offer the final crystal. The echoes grow silent...", new DialogueChoice[]{ new DialogueChoice("...", end) }, false, false, true);
        DialogueNode keepChoice = new DialogueNode("You keep the final crystal. The silence lingers.", new DialogueChoice[]{ new DialogueChoice("...", end) }, false, false, true);
        DialogueChoice[] choices = { new DialogueChoice("Give the final crystal", giveChoice), new DialogueChoice("Keep it", keepChoice) };
        startDialogue(new NPC("FinalChoice", RoomType.FINAL, 0, 0, 0) {
            @Override public DialogueNode getDialogue(TheDimmingEcho game) { return new DialogueNode("At the threshold, a choice: Will you give the final crystal, or keep it?", choices, false, false, false); }
            @Override public String getName() { return "???"; }
        });
    }

    private void triggerEndingWithChoice(String choice) {
        if (endingShown) return;
        int crystalsUsed = game.getUsageLog().totalGiven();
        int crystalsLostToFailure = game.getCrystalsLostToFailure();
        if (crystalsLostToFailure > 0) {
            game.setScreen(new EndingScreen(game, "FAILURE"));
            endingShown = true;
            return;
        }
        if (choice.equals("Give the final crystal")) {
            crystalsUsed++;
            if (crystalsUsed == 5) {
                game.setScreen(new EndingScreen(game, "VOID"));
                endingShown = true;
                return;
            } else {
                game.setScreen(new EndingScreen(game, "FADE"));
                endingShown = true;
                return;
            }
        } else if (choice.equals("Keep it")) {
            game.setScreen(new EndingScreen(game, "STONE"));
            endingShown = true;
            return;
        }
        // Fallback
        game.setScreen(new EndingScreen(game, "FAILURE"));
        endingShown = true;
    }

    private void updateChoiceHighlight() {
        for (int i = 0; i < choicesTable.getChildren().size; i++) {
            TextButton button = (TextButton) choicesTable.getChildren().get(i);
            button.getLabel().setColor(i == selectedChoiceIndex ? Color.GOLD : Color.WHITE);
        }
    }

    private void clampCamera() {
        float mapWidth = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class) * this.mapScale;
        float mapHeight = map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class) * this.mapScale;
        float cameraHalfWidth = gameCamera.viewportWidth * 0.5f;
        float cameraHalfHeight = gameCamera.viewportHeight * 0.5f;
        gameCamera.position.x = MathUtils.clamp(gameCamera.position.x, cameraHalfWidth, mapWidth - cameraHalfWidth);
        gameCamera.position.y = MathUtils.clamp(gameCamera.position.y, cameraHalfHeight, mapHeight - cameraHalfHeight);
    }

    private void updateTypingEffect(float delta) {
        charTimer += delta;
        if (charIndex < fullDialogueText.length() && charTimer >= CHAR_DELAY) {
            charIndex++;
            dialogueTextLabel.setText(fullDialogueText.substring(0, charIndex));
            charTimer = 0;
            if (charIndex == fullDialogueText.length()) populateChoices();
        }
    }

    private boolean isNear(NPC npc) {
        return player.getCenter(new Vector2()).dst(npc.getBounds().getCenter(new Vector2())) < INTERACTION_RADIUS;
    }

    public boolean isMemoryPuzzleSolved() { return isMemoryPuzzleSolved; }

    // Called by WordleScreen when the puzzle is completed
    public void puzzleCompleted(boolean success) {
        isMemoryPuzzleSolved = success;
    }

    @Override public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        uiStage.getViewport().update(width, height, true);
        dialogueTable.invalidateHierarchy();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { AudioManager.getInstance().stopMusic(); }
}
