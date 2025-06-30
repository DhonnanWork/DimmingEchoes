package com.dimmingechoes;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {

    // --- Core Rendering Tools ---
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera uiCamera;
    private OrthographicCamera gameCamera;
    private GlyphLayout layout = new GlyphLayout();

    // --- Game State Management ---
    enum GameState {
        PLAYING,
        CONFIRM_TRANSITION
    }
    private GameState currentState;
    private Door pendingDoor;
    private boolean isYesSelected = true; // --- NEW: Tracks which option is highlighted

    // --- Player State ---
    private Rectangle player;
    private Texture playerTexture;
    private final float PLAYER_SIZE = 50f;
    private final float PLAYER_SPEED = 300f;
    private int playerHp;
    private int playerMaxHp;

    // --- Item System State ---
    private Array<ItemType> inventory;
    private int currentItemIndex;

    // --- World State ---
    private Room[][] rooms;
    private int currentRoomX;
    private int currentRoomY;
    private final int WORLD_WIDTH = 3;
    private final int WORLD_HEIGHT = 3;
    private final char[][] roomNames = {
        {'A', 'B', 'C'},
        {'D', 'E', 'F'},
        {'G', 'H', 'I'}
    };

    private static final String TAG = "GameLog";

    enum ItemType {
        HEALING_POTION, SPEED_BOOST, TELEPORT_BOMB, SHIELD, DECOY
    }

    enum Direction {
        NORTH, SOUTH, EAST, WEST
    }

    static class Door {
        Rectangle rect;
        Direction dir;

        Door(float x, float y, float width, float height, Direction dir) {
            this.rect = new Rectangle(x, y, width, height);
            this.dir = dir;
        }
    }

    static class Room {
        Array<Rectangle> walls = new Array<>();
        Array<Door> doors = new Array<>();

        public Room(float x, float y, float width, float height, float thickness, boolean north, boolean south, boolean east, boolean west) {
            float doorSize = 100f;
            walls.add(new Rectangle(x, y, thickness, height));
            walls.add(new Rectangle(x + width - thickness, y, thickness, height));
            walls.add(new Rectangle(x, y, width, thickness));
            walls.add(new Rectangle(x, y + height - thickness, width, thickness));
            if (north) doors.add(new Door(x + width/2 - doorSize/2, y + height - thickness, doorSize, thickness, Direction.NORTH));
            if (south) doors.add(new Door(x + width/2 - doorSize/2, y, doorSize, thickness, Direction.SOUTH));
            if (west)  doors.add(new Door(x, y + height/2 - doorSize/2, thickness, doorSize, Direction.WEST));
            if (east)  doors.add(new Door(x + width - thickness, y + height/2 - doorSize/2, thickness, doorSize, Direction.EAST));
        }
    }


    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);

        // This is a placeholder. You should replace "libgdx.png" with your own player asset.
        playerTexture = new Texture(Gdx.files.internal("libgdx.png"));

        uiCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCamera.setToOrtho(false);
        gameCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        gameCamera.setToOrtho(false);

        playerMaxHp = 10;
        playerHp = playerMaxHp;
        inventory = new Array<>();
        inventory.add(ItemType.HEALING_POTION);
        inventory.add(ItemType.SPEED_BOOST);
        currentItemIndex = 0;

        generateRooms();
        currentRoomX = 1;
        currentRoomY = 1;

        float roomWidth = Gdx.graphics.getWidth();
        float roomHeight = Gdx.graphics.getHeight();
        player = new Rectangle(
            (currentRoomX * roomWidth) + (roomWidth / 2f) - (PLAYER_SIZE / 2f),
            (currentRoomY * roomHeight) + (roomHeight / 2f) - (PLAYER_SIZE / 2f),
            PLAYER_SIZE,
            PLAYER_SIZE
        );

        currentState = GameState.PLAYING;
        Gdx.input.setCursorCatched(true);

        Gdx.app.log(TAG, "Game created. Player and 3x3 world initialized.");
    }

    private void generateRooms() {
        rooms = new Room[WORLD_WIDTH][WORLD_HEIGHT];
        float roomWidth = Gdx.graphics.getWidth();
        float roomHeight = Gdx.graphics.getHeight();
        float wallThickness = 20f;

        for (int x = 0; x < WORLD_WIDTH; x++) {
            for (int y = 0; y < WORLD_HEIGHT; y++) {
                boolean north = (y < WORLD_HEIGHT - 1);
                boolean south = (y > 0);
                boolean west = (x > 0);
                boolean east = (x < WORLD_WIDTH - 1);
                rooms[x][y] = new Room(x * roomWidth, y * roomHeight, roomWidth, roomHeight, wallThickness, north, south, east, west);
            }
        }
    }

    @Override
    public void render() {
        switch (currentState) {
            case PLAYING:
                handleInput();
                updatePlayerPosition();
                checkCollisions();
                break;
            case CONFIRM_TRANSITION:
                handleConfirmationInput();
                break;
        }

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1);
        drawWorld();
        drawUI();

        if (currentState == GameState.CONFIRM_TRANSITION) {
            drawConfirmationDialog();
        }
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Keys.C)) useCurrentItem();
        if (Gdx.input.isKeyJustPressed(Keys.E)) cycleItem();
    }

    // --- MODIFIED: Handles highlighting and Enter key confirmation ---
    private void handleConfirmationInput() {
        // A/D or Left/Right toggles the selection between Yes and No
        if (Gdx.input.isKeyJustPressed(Keys.A) || Gdx.input.isKeyJustPressed(Keys.LEFT)) {
            isYesSelected = true;
        }
        if (Gdx.input.isKeyJustPressed(Keys.D) || Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
            isYesSelected = false;
        }

        // Enter key confirms the currently highlighted selection
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            confirmTransition(isYesSelected);
            return;
        }

        // Mouse click logic
        if (Gdx.input.justTouched()) {
            float dialogWidth = 400;
            float screenCenterX = Gdx.graphics.getWidth() / 2f;
            float screenCenterY = Gdx.graphics.getHeight() / 2f;
            float buttonWidth = 100;
            float buttonHeight = 50;
            float dialogY = screenCenterY - 150 / 2f;
            float buttonY = dialogY + 20;

            Rectangle yesBounds = new Rectangle(screenCenterX - buttonWidth - 20, buttonY, buttonWidth, buttonHeight);
            Rectangle noBounds = new Rectangle(screenCenterX + 20, buttonY, buttonWidth, buttonHeight);

            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            uiCamera.unproject(touchPos);

            if (yesBounds.contains(touchPos.x, touchPos.y)) {
                confirmTransition(true);
            } else if (noBounds.contains(touchPos.x, touchPos.y)) {
                confirmTransition(false);
            }
        }
    }

    private void confirmTransition(boolean accepted) {
        if (accepted) {
            changeRoom(pendingDoor);
        } else {
            // Push player back slightly if they say no, to prevent re-triggering
            switch (pendingDoor.dir) {
                case NORTH: player.y -= 10; break;
                case SOUTH: player.y += 10; break;
                case EAST:  player.x -= 10; break;
                case WEST:  player.x += 10; break;
            }
        }

        currentState = GameState.PLAYING;
        pendingDoor = null;
        isYesSelected = true; // Reset highlight to default ("Yes")
        Gdx.input.setCursorCatched(true);
    }

    private void useCurrentItem() {
        if (inventory.isEmpty()) return;
        ItemType item = inventory.get(currentItemIndex);
        Gdx.app.log(TAG, "Used item: " + item.name());
        if (item == ItemType.HEALING_POTION) {
            if (playerHp < playerMaxHp) playerHp++;
        }
        inventory.removeIndex(currentItemIndex);
        if (currentItemIndex >= inventory.size && !inventory.isEmpty()) {
            currentItemIndex = 0;
        }
    }

    private void cycleItem() {
        if (inventory.isEmpty()) return;
        currentItemIndex = (currentItemIndex + 1) % inventory.size;
        Gdx.app.log(TAG, "Cycled to item: " + inventory.get(currentItemIndex).name());
    }

    private void updatePlayerPosition() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT))  player.x -= PLAYER_SPEED * deltaTime;
        if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) player.x += PLAYER_SPEED * deltaTime;
        if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP))    player.y += PLAYER_SPEED * deltaTime;
        if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN))  player.y -= PLAYER_SPEED * deltaTime;
    }

    private void checkCollisions() {
        Room currentRoom = rooms[currentRoomX][currentRoomY];

        float oldX = player.x;
        // Simplified movement application to avoid duplicating code in updatePlayerPosition
        for(Rectangle wall : currentRoom.walls) {
            if (player.overlaps(wall)) {
                player.x = oldX;
                break;
            }
        }

        float oldY = player.y;
        for(Rectangle wall : currentRoom.walls) {
            if (player.overlaps(wall)) {
                player.y = oldY;
                break;
            }
        }

        for (Door door : currentRoom.doors) {
            if (player.overlaps(door.rect)) {
                Gdx.app.log(TAG, "Player entered a " + door.dir.name() + " door.");
                currentState = GameState.CONFIRM_TRANSITION;
                pendingDoor = door;
                Gdx.input.setCursorCatched(false);
                return;
            }
        }
    }

    // --- MODIFIED: Implements smart teleportation logic ---
    private void changeRoom(Door door) {
        float roomWidth = Gdx.graphics.getWidth();
        float roomHeight = Gdx.graphics.getHeight();
        float offset = PLAYER_SIZE * 0.5f; // A small buffer distance

        switch (door.dir) {
            case NORTH:
                currentRoomY++;
                // Player appears just above the bottom door of the new room
                player.y = (currentRoomY * roomHeight) + door.rect.height + offset;
                break;
            case SOUTH:
                currentRoomY--;
                // Player appears just below the top door of the new room
                player.y = ((currentRoomY + 1) * roomHeight) - door.rect.height - PLAYER_SIZE - offset;
                break;
            case EAST:
                currentRoomX++;
                // Player appears just to the right of the left door of the new room
                player.x = (currentRoomX * roomWidth) + door.rect.width + offset;
                break;
            case WEST:
                currentRoomX--;
                // Player appears just to the left of the right door of the new room
                player.x = ((currentRoomX + 1) * roomWidth) - door.rect.width - PLAYER_SIZE - offset;
                break;
        }
    }

    private void drawWorld() {
        Room currentRoom = rooms[currentRoomX][currentRoomY];
        gameCamera.position.set(player.x + player.width / 2, player.y + player.height / 2, 0);
        gameCamera.update();

        shapeRenderer.setProjectionMatrix(gameCamera.combined);
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(Color.GRAY);
        for (Rectangle wall : currentRoom.walls) {
            shapeRenderer.rect(wall.x, wall.y, wall.width, wall.height);
        }
        shapeRenderer.setColor(Color.GREEN);
        for (Door door : currentRoom.doors) {
            shapeRenderer.rect(door.rect.x, door.rect.y, door.rect.width, door.rect.height);
        }
        shapeRenderer.end();

        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();
        batch.draw(playerTexture, player.x, player.y, player.width, player.height);
        batch.end();
    }

    private void drawUI() {
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        font.draw(batch, "HP: " + playerHp + " / " + playerMaxHp, 20, Gdx.graphics.getHeight() - 20);
        String currentItemText = "Item: " + (inventory.isEmpty() ? "NONE" : inventory.get(currentItemIndex).name());
        font.draw(batch, currentItemText, 20, Gdx.graphics.getHeight() - 40);
        font.draw(batch, "Room: " + roomNames[currentRoomX][currentRoomY], 20, Gdx.graphics.getHeight() - 60);
        batch.end();
    }

    // --- MODIFIED: Draws a highlight based on selection ---
    private void drawConfirmationDialog() {
        float dialogWidth = 450; // Made wider for help text
        float dialogHeight = 150;
        float screenCenterX = Gdx.graphics.getWidth() / 2f;
        float screenCenterY = Gdx.graphics.getHeight() / 2f;
        float dialogX = screenCenterX - dialogWidth / 2;
        float dialogY = screenCenterY - dialogHeight / 2;

        float buttonWidth = 100;
        float buttonHeight = 50;
        float buttonY = dialogY + 45;

        Rectangle yesButtonRect = new Rectangle(screenCenterX - buttonWidth - 20, buttonY, buttonWidth, buttonHeight);
        Rectangle noButtonRect = new Rectangle(screenCenterX + 20, buttonY, buttonWidth, buttonHeight);

        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeType.Filled);

        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(dialogX, dialogY, dialogWidth, dialogHeight);

        // --- HIGHLIGHT LOGIC ---
        shapeRenderer.setColor(Color.GOLD);
        if (isYesSelected) {
            shapeRenderer.rect(yesButtonRect.x - 2, yesButtonRect.y - 2, yesButtonRect.width + 4, yesButtonRect.height + 4);
        } else {
            shapeRenderer.rect(noButtonRect.x - 2, noButtonRect.y - 2, noButtonRect.width + 4, noButtonRect.height + 4);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();

        int destX = currentRoomX;
        int destY = currentRoomY;
        switch (pendingDoor.dir) {
            case NORTH: destY++; break;
            case SOUTH: destY--; break;
            case EAST:  destX++; break;
            case WEST:  destX--; break;
        }
        char destRoomName = roomNames[destX][destY];

        String question = "Move to Room " + destRoomName + "?";
        layout.setText(font, question);
        font.draw(batch, layout, screenCenterX - layout.width / 2, dialogY + dialogHeight - 20);

        font.setColor(Color.BLACK);
        layout.setText(font, "Yes");
        font.draw(batch, layout, yesButtonRect.x + (yesButtonRect.width / 2 - layout.width / 2), yesButtonRect.y + (yesButtonRect.height / 2 + layout.height / 2));

        layout.setText(font, "No");
        font.draw(batch, layout, noButtonRect.x + (noButtonRect.width / 2 - layout.width / 2), noButtonRect.y + (noButtonRect.height / 2 + layout.height / 2));
        font.setColor(Color.WHITE);

        String helpText = "[A/D] or [Left/Right] to select. [Enter] to confirm.";
        layout.setText(font, helpText);
        font.draw(batch, layout, screenCenterX - layout.width / 2, dialogY + 30);

        batch.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        playerTexture.dispose();
    }
}
