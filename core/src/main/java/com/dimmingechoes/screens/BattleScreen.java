package com.dimmingechoes.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.dimmingechoes.TheDimmingEcho;
import com.dimmingechoes.manager.BattleManager;
import com.dimmingechoes.entities.Player;
import com.dimmingechoes.entities.Enemy;
import java.util.List;
import java.util.Random;

public class BattleScreen implements Screen {
    private final TheDimmingEcho game;
    private final BattleManager battleManager;
    private final Stage stage;
    private final Skin skin;
    private final BitmapFont font;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private Table uiTable;
    private Label infoLabel;
    private Enemy selectedEnemy;
    private int healAmount = 10;
    private String itemMessage = "";
    
    // Victory/Game Over effects
    private float victoryTimer = 0f;
    private float victoryAlpha = 0f;
    private boolean showingVictory = false;
    private boolean showingGameOver = false;
    private static final float VICTORY_FADE_DURATION = 2.0f;
    
    // Attack delay
    private float attackDelay = 0f;
    private static final float ATTACK_DELAY_TIME = 1.0f;
    private boolean canAttack = true;
    
    // Strong attack mechanics
    private Random random = new Random();
    private boolean strongAttackMode = false;
    private static final float STRONG_ATTACK_CHANCE = 0.3f; // 30% chance

    public BattleScreen(TheDimmingEcho game, Player player, List<Enemy> enemies) {
        this.game = game;
        this.battleManager = new BattleManager(player, enemies);
        this.stage = new Stage(new ScreenViewport());
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();

        // Load pixel font
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("DawnLike/GUI/SDS_8x8.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 24;
        parameter.color = Color.WHITE;
        this.font = generator.generateFont(parameter);
        generator.dispose();

        this.skin = new Skin();
        skin.add("default-font", font);
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        skin.add("default", labelStyle);
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.YELLOW; // Add yellow hover effect
        skin.add("default", buttonStyle);

        createUI();
    }

    private void createUI() {
        uiTable = new Table();
        uiTable.setFillParent(true);
        uiTable.bottom().left().padBottom(60).padLeft(60); // Align to bottom-left with padding

        infoLabel = new Label("", skin);
        infoLabel.setAlignment(Align.center);
        uiTable.add(infoLabel).colspan(3).padBottom(40).row();

        // Attack Button
        TextButton attackButton = new TextButton("Attack", skin);
        attackButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (battleManager.isPlayerTurn() && !battleManager.isBattleOver() && canAttack) {
                    Enemy target = getFirstAliveEnemy();
                    if (target != null) {
                        canAttack = false;
                        attackDelay = ATTACK_DELAY_TIME;
                        
                        // Check for strong attack
                        if (random.nextFloat() < STRONG_ATTACK_CHANCE) {
                            strongAttackMode = true;
                            battleManager.getPlayer().setHealth(1);
                            target.setHealth(1);
                            infoLabel.setText("Strong attack! Both at 1 HP!");
                        } else {
                            battleManager.playerAttack(target);
                        }
                        updateInfoLabel();
                    }
                }
            }
        });
        uiTable.add(attackButton).left().width(180).height(60).padRight(30).row(); // Left-align

        // Echo Crystal Button
        TextButton itemButton = new TextButton("Use Echo Crystal", skin);
        itemButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (battleManager.isPlayerTurn() && !battleManager.isBattleOver() && canAttack) {
                    if (game.getCrystalInventory().getCrystals() > 0) {
                        game.getCrystalInventory().useCrystal();
                        battleManager.getPlayer().setHealth(battleManager.getPlayer().getHealth() + healAmount);
                        itemMessage = "You used an Echo Crystal and healed!";
                    } else {
                        itemMessage = "No Echo Crystals left!";
                    }
                    battleManager.playerUseItem();
                    updateInfoLabel();
                }
            }
        });
        uiTable.add(itemButton).left().width(180).height(60).padRight(30).row(); // Left-align

        // Flee Button
        TextButton fleeButton = new TextButton("Flee", skin);
        fleeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (battleManager.isPlayerTurn() && !battleManager.isBattleOver() && canAttack) {
                    battleManager.playerFlee();
                    updateInfoLabel();
                }
            }
        });
        uiTable.add(fleeButton).left().width(180).height(60); // Left-align

        stage.addActor(uiTable);
    }

    private Enemy getFirstAliveEnemy() {
        for (Enemy e : battleManager.getEnemies()) {
            if (e.getHealth() > 0) return e;
        }
        return null;
    }

    private void updateInfoLabel() {
        if (!itemMessage.isEmpty()) {
            infoLabel.setText(itemMessage);
            itemMessage = "";
            return;
        }
        if (battleManager.isBattleOver()) {
            if (battleManager.getPlayer().getHealth() <= 0) {
                game.logFailure();
                game.getCrystalInventory().useCrystal();
                if (game.getCrystalInventory().getCrystals() <= 0) {
                    game.setScreen(new EndingScreen("Your echoes have faded completely. The silence is now absolute."));
                    return;
                }
                showingGameOver = true;
                victoryTimer = 0f;
                victoryAlpha = 0f;
            } else {
                showingVictory = true;
                victoryTimer = 0f;
                victoryAlpha = 0f;
            }
            uiTable.setVisible(false); // Hide UI controls on battle end
        } else if (battleManager.isPlayerTurn()) {
            infoLabel.setText("Your turn!");
        } else {
            infoLabel.setText("Enemy turn...");
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        updateInfoLabel();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update attack delay
        if (!canAttack) {
            attackDelay -= delta;
            if (attackDelay <= 0) {
                canAttack = true;
            }
        }

        // Update victory/game over effects
        if (showingVictory || showingGameOver) {
            victoryTimer += delta;
            victoryAlpha = Math.min(1.0f, victoryTimer / VICTORY_FADE_DURATION);
            
            // Auto-return to dungeon after 3 seconds
            if (victoryTimer > 3.0f) {
                game.setScreen(new DungeonScreen(game));
                return;
            }
        }

        batch.begin();
        // Draw player HP at top-left (always visible)
        font.draw(batch, battleManager.getPlayer().getName() + " HP: " + battleManager.getPlayer().getHealth() + "/" + battleManager.getPlayer().getMaxHealth(), 100, Gdx.graphics.getHeight() - 100);
        // Draw player icon and health (bottom left)
        Player player = battleManager.getPlayer();
        float playerX = 100; // Gap from left edge
        float playerY = 100; // Gap from bottom edge
        float playerSize = 96;
        
        // Draw player sprite from player.png
        try {
            com.badlogic.gdx.graphics.Texture playerTexture = new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("Player.png"));
            batch.draw(playerTexture, playerX, playerY, playerSize, playerSize);
            playerTexture.dispose();
        } catch (Exception e) {
            // Fallback to white rectangle if texture not found
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(playerX, playerY, playerSize, playerSize);
            shapeRenderer.end();
        }
        
        // Draw enemies with white blobs (top right)
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float enemySize = 96;
        float enemyMargin = enemySize * 1.5f; // 1.5x size from edge
        float enemyX = screenWidth - enemyMargin - enemySize;
        float enemyY = screenHeight - enemyMargin - enemySize;
        
        for (Enemy enemy : battleManager.getEnemies()) {
            if (enemy.getHealth() > 0) {
                // Draw white blob for enemy
                shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(Color.WHITE);
                shapeRenderer.circle(enemyX + enemySize/2, enemyY + enemySize/2, enemySize/2);
                shapeRenderer.end();
                
                // Draw enemy health above blob with proper spacing
                String enemyHealthText = enemy.getName() + " HP: " + enemy.getHealth() + "/" + enemy.getMaxHealth();
                float textWidth = font.draw(batch, enemyHealthText, 0, 0).width;
                float textX = enemyX + (enemySize - textWidth) / 2f;
                float textY = enemyY + enemySize + 30; // 30px gap above blob
                font.draw(batch, enemyHealthText, textX, textY);
                
                // Move to next enemy position (stacked vertically)
                enemyY -= (enemySize + 60); // 60px gap between enemies
            }
        }
        batch.end();

        // Enemy AI turn
        if (!battleManager.isBattleOver() && !battleManager.isPlayerTurn() && canAttack) {
            Enemy enemy = getFirstAliveEnemy();
            if (enemy != null) {
                battleManager.enemyTurn(enemy);
                updateInfoLabel();
            }
        }

        // Draw victory/game over screen
        if (showingVictory || showingGameOver) {
            batch.begin();
            font.setColor(1, 1, 1, victoryAlpha);
            String text = showingVictory ? "VICTORY!" : "GAME OVER";
            float textWidth = font.draw(batch, text, 0, 0).width;
            float textX = (Gdx.graphics.getWidth() - textWidth) / 2f;
            float textY = Gdx.graphics.getHeight() / 2f + 50;
            // Draw sparkles for victory
            if (showingVictory) {
                drawSparkles(textX, textY, victoryAlpha);
            }
            font.draw(batch, text, textX, textY);
            font.setColor(Color.WHITE);
            batch.end();
        }

        stage.act(delta);
        stage.draw();
    }

    private void drawSparkles(float textX, float textY, float alpha) {
        // Draw sparkles on left and right sides
        String sparkle = "*";
        float sparkleAlpha = (float) Math.abs(Math.sin(victoryTimer * 4)) * alpha;
        font.setColor(1, 1, 0, sparkleAlpha); // Yellow sparkles
        
        // Left sparkles
        font.draw(batch, sparkle, textX - 60, textY + 20);
        font.draw(batch, sparkle, textX - 80, textY - 10);
        font.draw(batch, sparkle, textX - 40, textY - 30);
        
        // Right sparkles
        font.draw(batch, sparkle, textX + 60, textY + 20);
        font.draw(batch, sparkle, textX + 80, textY - 10);
        font.draw(batch, sparkle, textX + 40, textY - 30);
        
        font.setColor(1, 1, 1, alpha);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        font.dispose();
        batch.dispose();
        shapeRenderer.dispose();
    }
}
