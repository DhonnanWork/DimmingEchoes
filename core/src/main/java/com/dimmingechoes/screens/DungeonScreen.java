package com.dimmingechoes.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
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

public class DungeonScreen extends InputAdapter implements Screen {

    private float mapScale;
    // Variabel-variabel (tidak ada perubahan di sini)
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
    private float stateTime;
    private static final float PLAYER_SPEED = 250f;
    private static final float INTERACTION_RADIUS = 64f;
    private boolean moveUp, moveDown, moveLeft, moveRight;
    private boolean isFacingRight =true;
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
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        gameCamera = new OrthographicCamera();
        gameViewport = new FitViewport(1100, 720, gameCamera);
        skin = new Skin();
        skin.addRegions(new TextureAtlas(Gdx.files.internal("uiskin.atlas")));
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("LibertinusMono-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 20;
        parameter.color = Color.WHITE;
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        skin.add("default-font", font);
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        skin.add("default", labelStyle);
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = font;
        skin.add("default", textButtonStyle);
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
                        processDialogueChoice(currentDialogueNode.choices[selectedChoiceIndex]);
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
        this.player = new Rectangle(gameViewport.getWorldWidth() / 2f - PLAYER_SIZE / 2f, gameViewport.getWorldHeight() / 2f - PLAYER_SIZE / 2f, PLAYER_SIZE, PLAYER_SIZE);
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void show() {
        map = new TmxMapLoader().load(currentRoom.getTmxPath());
        int mapWidthInPixels = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class);
        this.mapScale = gameViewport.getWorldWidth() / mapWidthInPixels;
        renderer = new OrthogonalTiledMapRenderer(map, this.mapScale);
        parseCollisionLayer();

        playerSpriteSheet = new Texture(Gdx.files.internal("Player.png"));
        playerSpriteSheet = new Texture(Gdx.files.internal("player_walk_left.png"));


        int FRAME_COLS = 8;
        int FRAME_ROWS = 1;
        int frameWidth = playerSpriteSheet.getWidth() / FRAME_COLS;
        int frameHeight = playerSpriteSheet.getHeight() / FRAME_ROWS;

        TextureRegion[][] tmp = TextureRegion.split(playerSpriteSheet, frameWidth, frameHeight);
        TextureRegion[] walkFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
        int index = 0;
        for (int i = 0; i < FRAME_ROWS; i++) {
            for (int j = 0; j < FRAME_COLS; j++) {
                walkFrames[index++] = tmp[i][j];
            }
        }

        playerAnimation = new Animation<TextureRegion>(0.1f, walkFrames);
        stateTime = 0f;
    }

    private void clampCamera() {
        // Dapatkan ukuran peta dalam satuan dunia (sudah diskalakan)
        float mapWidth = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class) * this.mapScale;
        float mapHeight = map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class) * this.mapScale;

        // Dapatkan setengah dari lebar dan tinggi viewport kamera
        float cameraHalfWidth = gameCamera.viewportWidth * 0.5f;
        float cameraHalfHeight = gameCamera.viewportHeight * 0.5f;

        // Batasi posisi x kamera
        gameCamera.position.x = MathUtils.clamp(gameCamera.position.x, cameraHalfWidth, mapWidth - cameraHalfWidth);

        // Batasi posisi y kamera
        gameCamera.position.y = MathUtils.clamp(gameCamera.position.y, cameraHalfHeight, mapHeight - cameraHalfHeight);
    }

    @Override
    public void render(float delta) {
        // TAMBAHKAN BARIS INI di awal render
        stateTime += delta;

        if (currentDialogueNode == null) {
            handleMovement(delta);
        } else {
            updateTypingEffect(delta);
        }
        // ... sisa kode sampai renderer.render() tidak berubah ...
        Color bgColor = currentRoom.getBackgroundColor();
        ScreenUtils.clear(bgColor.r, bgColor.g, bgColor.b, bgColor.a);
        gameViewport.apply();
        gameCamera.position.x = player.x + player.width / 2;
        gameCamera.position.y = player.y + player.height / 2;
        clampCamera();
        gameCamera.update();
        gameViewport.apply();

        renderer.setView(gameCamera);
        renderer.render();

        // Panggil renderGame() yang sudah dimodifikasi
        renderGame();

        // Kode UI tidak berubah
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        renderStaticUI();
        uiStage.act(delta);
        uiStage.draw();
    }

    private void handleMovement(float delta) {
        float moveAmount = PLAYER_SPEED * delta;
        float oldX = player.x;
        float oldY = player.y;

        if (moveLeft) {
            player.x -= moveAmount;
            isFacingRight = true; // Player menghadap kiri
        }
        if (moveRight) {
            player.x += moveAmount;
            isFacingRight = false; // Player menghadap kanan
        }

        if (moveUp) player.y += moveAmount;
        if (moveDown) player.y -= moveAmount;

        for (DoorZone door : currentRoom.getDoorZones()) {
            if (player.overlaps(door.bounds)) {
                if (door.leadsTo.getRoomType() == RoomType.FINAL) {
                    triggerEnding();
                } else {
                    changeRoom(door);
                }
                return;
            }
        }

        // Cek tabrakan dengan tembok
        if (player.x != oldX) {
            for (Rectangle obstacle : currentRoom.getObstacles()) {
                if (player.overlaps(obstacle)) {
                    player.x = oldX; // Kembalikan ke posisi X semula jika nabrak
                    break;
                }
            }
        }
        if (player.y != oldY) {
            for (Rectangle obstacle : currentRoom.getObstacles()) {
                if (player.overlaps(obstacle)) {
                    player.y = oldY; // Kembalikan ke posisi Y semula jika nabrak
                    break;
                }
            }
        }
    }


    private void changeRoom(DoorZone door) {
        currentRoom = door.leadsTo;
        if (map != null) {
            map.dispose();
        }

        map = new TmxMapLoader().load(currentRoom.getTmxPath());
        renderer.setMap(map);

// PERBAIKAN 2: Panggil parseCollisionLayer setiap kali pindah ruangan
        parseCollisionLayer();

        if (dialogueTable.isVisible()) endDialogue();
        switch (door.entryDirection) {
            case LEFT: player.x = gameViewport.getWorldWidth() - player.width - 48f; player.y = gameViewport.getWorldHeight() / 2f; break;
            case RIGHT: player.x = 48f; player.y = gameViewport.getWorldHeight() / 2f; break;
            case TOP: player.x = gameViewport.getWorldWidth() / 2f; player.y = 48f; break;
            case BOTTOM: player.x = gameViewport.getWorldWidth() / 2f; player.y = gameViewport.getWorldHeight() - player.height - 48f; break;
        }
    }

    private void triggerEnding() {
        if (endingShown) return;
        // PERBAIKAN 4: Variabel 'ending' diganti menjadi 'endingMessage' agar cocok
        int used = game.getUsageLog().totalGiven();
        String endingMessage;
        String endingImagePath;

        if (used == 0) {
            endingMessage = "Ending: A Place That No Longer Exists\nYou remembered, but never acted.";
            endingImagePath = "../Tiled/Shinking.png";
        } else if (used >= 3) {
            endingMessage = "Ending: Petals in the Void\nYou gave all you could. And something beautiful bloomed.";
            endingImagePath = "../Tiled/Roblox.png";
        } else {
            endingMessage = "Ending: The Keeper Becomes Stone\nSome memories returned, others stayed buried.";
            endingImagePath = "../Tiled/FinalPuni.png";
        }

        game.setScreen(new EndingScreen(endingMessage, endingImagePath));
        endingShown = true;
    }

    private void parseCollisionLayer() {
        currentRoom.getObstacles().clear();
        if (map.getLayers().get("Collision") == null) return;

        for (MapObject object : map.getLayers().get("Collision").getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();

                // Terapkan skala pada posisi dan ukuran objek kolisi
                rect.x *= this.mapScale;
                rect.y *= this.mapScale;
                rect.width *= this.mapScale;
                rect.height *= this.mapScale;

                currentRoom.addObstacle(rect);
            }
        }
    }

    // ... (Sisa kode Anda sama dan tidak perlu diubah) ...
    private void renderGame() {
        // ... (kode shapeRenderer tetap sama) ...
        shapeRenderer.end();

        // Dapatkan frame animasi seperti biasa
        TextureRegion currentFrame = playerAnimation.getKeyFrame(stateTime, true);

        // --- LOGIKA MEMBALIK GAMBAR ---
        // Jika player seharusnya hadap kiri TAPI gambarnya BELUM terbalik, maka balik gambarnya.
        if (!isFacingRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }
        // Jika player seharusnya hadap kanan TAPI gambarnya TERLANJUR terbalik, maka balikkan lagi.
        if (isFacingRight && currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }
        // ----------------------------

        spriteBatch.setProjectionMatrix(gameCamera.combined);
        spriteBatch.begin();
        spriteBatch.draw(currentFrame, player.x, player.y, PLAYER_SIZE, PLAYER_SIZE);
        spriteBatch.end();
    }

    private void renderStaticUI() { spriteBatch.begin(); BitmapFont font = skin.getFont("default-font"); font.setColor(Color.WHITE); font.draw(spriteBatch, "Crystals: " + game.getCrystalInventory().getCrystals(), 10, Gdx.graphics.getHeight() - 10); font.draw(spriteBatch, "Press [F5] to Save", 10, Gdx.graphics.getHeight() - 35); font.draw(spriteBatch, "Press [SPACE] to interact", 10, Gdx.graphics.getHeight() - 60); spriteBatch.end(); }
    @Override public boolean keyDown(int keycode) { switch (keycode) { case Input.Keys.W: moveUp = true; break; case Input.Keys.S: moveDown = true; break; case Input.Keys.A: moveLeft = true; break; case Input.Keys.D: moveRight = true; break; case Input.Keys.F5: game.saveGame(); return true; case Input.Keys.SPACE: for (NPC npc : currentRoom.getNpcs()) { if (isNear(npc)) { startDialogue(npc); break; } } return true; } return false; }
    @Override public boolean keyUp(int keycode) { switch (keycode) { case Input.Keys.W: moveUp = false; break; case Input.Keys.S: moveDown = false; break; case Input.Keys.A: moveLeft = false; break; case Input.Keys.D: moveRight = false; break; } return false; }
    private void updateTypingEffect(float delta) { charTimer += delta; if (charIndex < fullDialogueText.length() && charTimer >= CHAR_DELAY) { charIndex++; dialogueTextLabel.setText(fullDialogueText.substring(0, charIndex)); charTimer = 0; if (charIndex == fullDialogueText.length()) populateChoices(); } }
    private void startDialogue(NPC npc) { moveUp = moveDown = moveLeft = moveRight = false; dialogueNPC = npc; currentDialogueNode = npc.getDialogue(game); fullDialogueText = currentDialogueNode.text; charIndex = 0; dialogueTextLabel.setText(""); choicesTable.clear(); dialogueTable.setVisible(true); Gdx.input.setInputProcessor(uiStage); if (charIndex < fullDialogueText.length()) dialogueTextLabel.setText(fullDialogueText.substring(0, charIndex)); else populateChoices(); }
    private void endDialogue() { dialogueTable.setVisible(false); uiStage.setKeyboardFocus(null); currentDialogueNode = null; dialogueNPC = null; Gdx.input.setInputProcessor(this); }
    private boolean isNear(NPC npc) { return player.getCenter(new Vector2()).dst(npc.getBounds().getCenter(new Vector2())) < INTERACTION_RADIUS; }
    private void updateChoiceHighlight() { for (int i = 0; i < choicesTable.getChildren().size; i++) { TextButton button = (TextButton) choicesTable.getChildren().get(i); button.setColor(i == selectedChoiceIndex ? Color.GOLD : Color.WHITE); } }
    private void populateChoices() { choicesTable.clear(); if (currentDialogueNode.choices != null && currentDialogueNode.choices.length > 0) { for (final DialogueChoice choice : currentDialogueNode.choices) { TextButton choiceButton = new TextButton(choice.choiceText, skin); choiceButton.getLabel().setAlignment(Align.left); choiceButton.addListener(new ClickListener() { @Override public void clicked(InputEvent event, float x, float y) { processDialogueChoice(choice); } }); choicesTable.add(choiceButton).left().row(); } selectedChoiceIndex = 0; updateChoiceHighlight(); uiStage.setKeyboardFocus(choicesTable); } else { uiStage.setKeyboardFocus(null); } }
    private void processDialogueChoice(DialogueChoice choice) { choicesTable.clear(); uiStage.setKeyboardFocus(null); currentDialogueNode = choice.next; if (currentDialogueNode.requiresCrystal) { if (dialogueNPC != null && !dialogueNPC.hasReceivedCrystal(game)) { if (!game.getCrystalInventory().useCrystal()) { endDialogue(); return; } game.getUsageLog().logCrystalGiven(dialogueNPC.getName()); } } fullDialogueText = currentDialogueNode.text; charIndex = 0; dialogueTextLabel.setText(""); }
    @Override public void resize(int width, int height) { gameViewport.update(width, height, true); uiStage.getViewport().update(width, height, true); dialogueTable.invalidateHierarchy(); }
    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        skin.dispose();
        uiStage.dispose();
        if (map != null) map.dispose();
        if (renderer != null) renderer.dispose();

        // --- TAMBAHKAN BARIS INI ---
        playerSpriteSheet.dispose();
    } @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
