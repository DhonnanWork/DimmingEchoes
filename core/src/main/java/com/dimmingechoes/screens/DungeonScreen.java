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
import com.dimmingechoes.manager.GameLogger;

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

    // --- NEW: ShapeRenderer for Debugging ---
    private final ShapeRenderer debugRenderer;

    // --- Add fields for the custom ending choice UI ---
    private boolean showEndingChoiceOverlay = false;
    private String[] endingChoices = {"Keep the final crystal", "Give the final crystal", "Ignore the voices and keep walking ahead"};
    private int endingChoiceIndex = 0;
    private DoorZone lastTriggeredTopDoor = null;

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

    public Rectangle getPlayer() {
        return player;
    }
    public Room getCurrentRoom() {
        return currentRoom;
    }

    // ... (rest of the constructor and other methods are unchanged) ...

    @Override
    public void render(float delta) {
        stateTime += delta;
        if (currentDialogueNode == null && !showEndingChoiceOverlay) {
            handleMovement(delta);
        } else if (currentDialogueNode != null) {
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

        // Draw ending choice overlay if needed
        if (showEndingChoiceOverlay) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapeRenderer.setProjectionMatrix(gameCamera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, 0.95f);
            shapeRenderer.rect(0, 0, gameViewport.getWorldWidth(), gameViewport.getWorldHeight());
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
            spriteBatch.setProjectionMatrix(gameCamera.combined);
            spriteBatch.begin();
            BitmapFont font = skin.getFont("default-font");
            float yStart = gameViewport.getWorldHeight() / 2f + 40;
            for (int i = 0; i < endingChoices.length; i++) {
                font.setColor(i == endingChoiceIndex ? Color.GOLD : Color.LIGHT_GRAY);
                font.draw(spriteBatch, endingChoices[i], 0, yStart - i * 40, gameViewport.getWorldWidth(), Align.center, false);
            }
            font.setColor(Color.WHITE);
            font.draw(spriteBatch, "Use UP/DOWN and ENTER to choose", 0, 80, gameViewport.getWorldWidth(), Align.center, false);
            spriteBatch.end();
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
        if (game.getNextPlayerPosition() != null) {
            Room loadedRoom = roomGraph.findRoomByTmxPath(game.getNextRoomTmxPath());
            if (loadedRoom != null) {
                this.currentRoom = loadedRoom;
            }
            player.setPosition(game.getNextPlayerPosition().x, game.getNextPlayerPosition().y);
            game.clearNextSpawnPoint();
        }
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
        if (showEndingChoiceOverlay) return; // Prevent movement while overlay is up
        float moveAmount = PLAYER_SPEED * delta;
        float oldX = player.x;
        float oldY = player.y;

        if (moveLeft) { player.x -= moveAmount; isFacingRight = false; }
        if (moveRight) { player.x += moveAmount; isFacingRight = true; }
        if (moveUp) player.y += moveAmount;
        if (moveDown) player.y -= moveAmount;

        // Intercept top door in the middle room
        for (DoorZone door : currentRoom.getDoorZones()) {
            if (player.overlaps(door.bounds)) {
                if (currentRoom.getRoomType() == RoomType.START && door.entryDirection == DoorZone.Direction.TOP && !showEndingChoiceOverlay) {
                    showEndingChoiceOverlay = true;
                    endingChoiceIndex = 0;
                    lastTriggeredTopDoor = door;
                    return;
                }
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
            if (currentRoom.getRoomType() == RoomType.FINAL && !endingShown) {
                // Check for 'A Place That No Longer Exists' ending
                if (game.getUsageLog().totalGiven() == 0 && game.getCrystalsLostToFailure() == 0) {
                    game.setScreen(new EndingScreen(game, "NOMATTER"));
                    endingShown = true;
                    return;
                }
                // If dialogue is not visible, force ending (limbo fix)
                if (!dialogueTable.isVisible()) {
                    triggerEndingWithChoice(getFinalChoiceMade());
                    return;
                }
                presentFinalChoice();
            }
            return;
        }
        loadMap(currentRoom.getTmxPath());
        GameLogger.getInstance().log("Player entered room: " + currentRoom.getTmxPath());
    }

    @Override
    public boolean keyDown(int keycode) {
        if (showEndingChoiceOverlay) {
            if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
                endingChoiceIndex = (endingChoiceIndex - 1 + endingChoices.length) % endingChoices.length;
                return true;
            }
            if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
                endingChoiceIndex = (endingChoiceIndex + 1) % endingChoices.length;
                return true;
            }
            if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                showEndingChoiceOverlay = false;
                if (endingChoices[endingChoiceIndex].equals("Keep the final crystal")) {
                    triggerEndingWithChoice("Keep it");
                } else if (endingChoices[endingChoiceIndex].equals("Give the final crystal")) {
                    triggerEndingWithChoice("Give the final crystal");
                } else {
                    triggerEndingWithChoice("Ignore the voices and keep walking ahead");
                }
                return true;
            }
            return true;
        }
        if (currentDialogueNode != null) return false;
        switch (keycode) {
            case Input.Keys.W: case Input.Keys.UP: moveUp = true; break;
            case Input.Keys.S: case Input.Keys.DOWN: moveDown = true; break;
            case Input.Keys.A: case Input.Keys.LEFT: moveLeft = true; break;
            case Input.Keys.D: case Input.Keys.RIGHT: moveRight = true; break;
            case Input.Keys.ESCAPE:
                GameLogger.getInstance().log("Game paused.");
                game.setScreen(new PauseMenuScreen(game, this));
                return true;
            case Input.Keys.SPACE:
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
        GameLogger.getInstance().log("Player started dialogue with '" + npc.getName() + "'.");
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
        GameLogger.getInstance().log("Player chose dialogue option: '" + choice.choiceText + "'");

        if (dialogueNPC != null && dialogueNPC.getName().equals("FinalChoice")) {
            if (choice.choiceText.equals("Give the final crystal") || choice.choiceText.equals("Keep it")) {
                finalChoiceMade = choice.choiceText;
            }
            // If player chooses skip at any point, immediately trigger ending
            if (choice.choiceText.equals("[Skip to Ending]")) {
                triggerEndingWithChoice(getFinalChoiceMade());
                return;
            }
            // If this is the final '...' node, trigger ending
            if (choice.choiceText.equals("...")) {
                triggerEndingWithChoice(getFinalChoiceMade());
                return;
            }
        }

        if (dialogueNPC != null && dialogueNPC.getName().equals("The Laughing Girl")) {
            if (choice.choiceText.equals("[Attempt the word puzzle]")) {
                endDialogue();
                GameLogger.getInstance().log("Player is attempting the Wordle puzzle.");
                game.setScreen(new WordleScreen(game, this));
                return;
            }
            if (choice.choiceText.equals("[Attempt the ladder puzzle]")) {
                endDialogue();
                GameLogger.getInstance().log("Player is attempting the Word Ladder puzzle.");
                game.setScreen(new WordLadderScreen(game, this));
                return;
            }
            if (choice.choiceText.equals("[Attempt the number puzzle]")) {
                endDialogue();
                GameLogger.getInstance().log("Player is attempting the Fibonacci puzzle.");
                game.setScreen(new FibonacciScreen(game, this));
                return;
            }
        }

        if (choice.next != null) {
            currentDialogueNode = choice.next;
            if (currentDialogueNode.requiresCrystal) {
                if (dialogueNPC != null && !dialogueNPC.hasReceivedCrystal(game)) {
                    if (game.getCrystalInventory().useCrystal()) {
                        game.getUsageLog().logCrystalGiven(dialogueNPC.getName());
                        GameLogger.getInstance().log("Player gave a crystal to '" + dialogueNPC.getName() + "'.");
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
        DialogueNode end = new DialogueNode("...", null, false, false, true);
        DialogueNode giveChoice = new DialogueNode("You offer the final crystal. The echoes grow silent...", new DialogueChoice[]{
            new DialogueChoice("...", end),
            new DialogueChoice("[Skip to Ending]", null)
        }, false, false, true);
        DialogueNode keepChoice = new DialogueNode("You keep the final crystal. The silence lingers.", new DialogueChoice[]{
            new DialogueChoice("...", end),
            new DialogueChoice("[Skip to Ending]", null)
        }, false, false, true);
        DialogueChoice[] choices = {
            new DialogueChoice("Give the final crystal", giveChoice),
            new DialogueChoice("Keep it", keepChoice),
            new DialogueChoice("[Skip to Ending]", null)
        };
        startDialogue(new NPC("FinalChoice", RoomType.FINAL, 0, 0, 0) {
            @Override public DialogueNode getDialogue(TheDimmingEcho game) { return new DialogueNode("At the threshold, a choice: Will you give the final crystal, or keep it?", choices, false, false, false); }
            @Override public String getName() { return "???"; }
        });
    }

    private void triggerEndingWithChoice(String choice) {
        if (endingShown) return;
        int crystalsUsed = game.getUsageLog().totalGiven();
        int crystalsLost = game.getCrystalsLostToFailure();
        // Failure ending
        if (crystalsLost > 0) {
            game.setScreen(new EndingScreen(game, "FAILURE"));
            endingShown = true;
            return;
        }
        // Skip/Ignore logic
        if (choice.equals("Ignore the voices and keep walking ahead")) {
            if (crystalsUsed == 0) {
                game.setScreen(new EndingScreen(game, "NOMATTER"));
            } else {
                game.setScreen(new EndingScreen(game, "PARTIAL"));
            }
            endingShown = true;
            return;
        }
        // Keep logic
        if (choice.equals("Keep it")) {
            if (crystalsUsed == 0) {
                game.setScreen(new EndingScreen(game, "STONE"));
            } else {
                game.setScreen(new EndingScreen(game, "PARTIAL"));
            }
            endingShown = true;
            return;
        }
        // Give logic
        if (choice.equals("Give the final crystal")) {
            if (crystalsUsed == 4) {
                game.setScreen(new EndingScreen(game, "VOID"));
            } else if (crystalsUsed > 0 && crystalsUsed < 4) {
                game.setScreen(new EndingScreen(game, "PARTIAL"));
            } else if (crystalsUsed == 0) {
                game.setScreen(new EndingScreen(game, "FADE"));
            }
            endingShown = true;
            return;
        }
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

    @Override public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        uiStage.getViewport().update(width, height, true);
        dialogueTable.invalidateHierarchy();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { AudioManager.getInstance().stopMusic(); }

    // Helper to remember which final choice was made (Give or Keep)
    private String finalChoiceMade = null;
    private String getFinalChoiceMade() {
        return finalChoiceMade != null ? finalChoiceMade : "Give the final crystal";
    }
}
