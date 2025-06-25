package com.dimmingechoes;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {

    // --- Core Rendering Tools ---
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera uiCamera; // A separate camera for the UI that doesn't move
    private OrthographicCamera gameCamera; // <-- NEW: A camera for the game world that will move

    // --- Player State ---
    private Rectangle player;
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

    // --- Logging Tag ---
    private static final String TAG = "GameLog";

    // --- Helper Enums for Readability ---
    enum ItemType {
        HEALING_POTION, SPEED_BOOST, TELEPORT_BOMB, SHIELD, DECOY
    }

    enum Direction {
        NORTH, SOUTH, EAST, WEST
    }

    // --- Helper Class for Doors ---
    static class Door {
        Rectangle rect;
        Direction dir;

        Door(float x, float y, float width, float height, Direction dir) {
            this.rect = new Rectangle(x, y, width, height);
            this.dir = dir;
        }
    }

    // --- Helper Class for Rooms ---
    static class Room {
        Array<Rectangle> walls = new Array<>();
        Array<Door> doors = new Array<>();

        public Room(float x, float y, float width, float height, float thickness, boolean north, boolean south, boolean east, boolean west) {
            float doorSize = 100f;
            walls.add(new Rectangle(x, y, thickness, height)); // Left
            walls.add(new Rectangle(x + width - thickness, y, thickness, height)); // Right
            walls.add(new Rectangle(x, y, width, thickness)); // Bottom
            walls.add(new Rectangle(x, y + height - thickness, width, thickness)); // Top
            if (north) doors.add(new Door(x + width/2 - doorSize/2, y + height - thickness, doorSize, thickness, Direction.NORTH));
            if (south) doors.add(new Door(x + width/2 - doorSize/2, y, doorSize, thickness, Direction.SOUTH));
            if (west)  doors.add(new Door(x, y + height/2 - doorSize/2, thickness, doorSize, Direction.WEST));
            if (east)  doors.add(new Door(x + width - thickness, y + height/2 - doorSize/2, thickness, doorSize, Direction.EAST));
        }
    }


    @Override
    public void create() {
        // --- Initialize Rendering Tools ---
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        // --- NEW: Initialize both cameras ---
        uiCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCamera.setToOrtho(false);
        gameCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        gameCamera.setToOrtho(false);

        // --- Initialize Player Stats & Items ---
        playerMaxHp = 10;
        playerHp = playerMaxHp;
        inventory = new Array<>();
        inventory.add(ItemType.HEALING_POTION);
        inventory.add(ItemType.SPEED_BOOST);
        inventory.add(ItemType.TELEPORT_BOMB);
        inventory.add(ItemType.SHIELD);
        inventory.add(ItemType.DECOY);
        currentItemIndex = 0;

        // --- Generate the World ---
        generateRooms();
        currentRoomX = 1;
        currentRoomY = 1;

        // --- Initialize Player ---
        float roomWidth = Gdx.graphics.getWidth();
        float roomHeight = Gdx.graphics.getHeight();
        player = new Rectangle(
            (currentRoomX * roomWidth) + (roomWidth / 2f) - (PLAYER_SIZE / 2f),
            (currentRoomY * roomHeight) + (roomHeight / 2f) - (PLAYER_SIZE / 2f),
            PLAYER_SIZE,
            PLAYER_SIZE
        );

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
        handleInput();
        updatePlayerPosition();
        checkCollisions();
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1);
        drawWorld();
        drawUI();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Keys.C)) useCurrentItem();
        if (Gdx.input.isKeyJustPressed(Keys.E)) cycleItem();
        if (Gdx.input.isKeyJustPressed(Keys.W) || Gdx.input.isKeyJustPressed(Keys.UP))    Gdx.app.log(TAG, "Input: UP");
        if (Gdx.input.isKeyJustPressed(Keys.S) || Gdx.input.isKeyJustPressed(Keys.DOWN))  Gdx.app.log(TAG, "Input: DOWN");
        if (Gdx.input.isKeyJustPressed(Keys.A) || Gdx.input.isKeyJustPressed(Keys.LEFT))  Gdx.app.log(TAG, "Input: LEFT");
        if (Gdx.input.isKeyJustPressed(Keys.D) || Gdx.input.isKeyJustPressed(Keys.RIGHT)) Gdx.app.log(TAG, "Input: RIGHT");
    }

    private void useCurrentItem() {
        if (inventory.isEmpty()) {
            Gdx.app.log(TAG, "Inventory is empty. Cannot use item.");
            return;
        }
        ItemType item = inventory.get(currentItemIndex);
        Gdx.app.log(TAG, "Used item: " + item.name());
        switch (item) {
            case HEALING_POTION:
                if (playerHp < playerMaxHp) {
                    playerHp++;
                    Gdx.app.log(TAG, "Healed 1 HP. Current HP: " + playerHp);
                } else {
                    Gdx.app.log(TAG, "HP is already full.");
                }
                break;
            case SPEED_BOOST:
            case TELEPORT_BOMB:
            case SHIELD:
            case DECOY:
                Gdx.app.log(TAG, item.name() + " is not implemented yet.");
                break;
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
        float oldY = player.y;
        Rectangle potentialPosition = new Rectangle(player);
        potentialPosition.y = oldY;
        for (Rectangle wall : currentRoom.walls) {
            if (potentialPosition.overlaps(wall)) {
                Gdx.app.log(TAG, "Collision on X-axis with wall.");
                player.x = oldX;
                break;
            }
        }
        potentialPosition.x = player.x;
        potentialPosition.y = player.y;
        for (Rectangle wall : currentRoom.walls) {
            if (potentialPosition.overlaps(wall)) {
                Gdx.app.log(TAG, "Collision on Y-axis with wall.");
                player.y = oldY;
                break;
            }
        }
        for (Door door : currentRoom.doors) {
            if (player.overlaps(door.rect)) {
                Gdx.app.log(TAG, "Player entered a " + door.dir.name() + " door.");
                changeRoom(door.dir);
                return;
            }
        }
    }

    private void changeRoom(Direction direction) {
        switch (direction) {
            case NORTH: currentRoomY++; player.y += 50; break;
            case SOUTH: currentRoomY--; player.y -= 50; break;
            case EAST:  currentRoomX++; player.x += 50; break;
            case WEST:  currentRoomX--; player.x -= 50; break;
        }
    }

    private void drawWorld() {
        Room currentRoom = rooms[currentRoomX][currentRoomY];

        // --- THIS IS THE CORRECTED CAMERA LOGIC ---
        // 1. Update our game camera's position to center on the player
        gameCamera.position.set(player.x + player.width / 2, player.y + player.height / 2, 0);
        gameCamera.update();

        // 2. Tell the ShapeRenderer to use our updated game camera
        shapeRenderer.setProjectionMatrix(gameCamera.combined);
        // --- END CORRECTION ---

        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        for (Rectangle wall : currentRoom.walls) {
            shapeRenderer.rect(wall.x, wall.y, wall.width, wall.height);
        }
        shapeRenderer.setColor(Color.GREEN);
        for (Door door : currentRoom.doors) {
            shapeRenderer.rect(door.rect.x, door.rect.y, door.rect.width, door.rect.height);
        }
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(player.x, player.y, player.width, player.height);
        shapeRenderer.end();
    }

    private void drawUI() {
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        font.draw(batch, "HP: " + playerHp + " / " + playerMaxHp, 20, Gdx.graphics.getHeight() - 20);
        String currentItemText = "Item: " + (inventory.isEmpty() ? "NONE" : inventory.get(currentItemIndex).name());
        font.draw(batch, currentItemText, 20, Gdx.graphics.getHeight() - 40);
        batch.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
