package com.dimmingechoes.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
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
import com.dimmingechoes.screens.LoadGameScreen;
import com.dimmingechoes.manager.AudioManager;

public class MainMenuScreen implements Screen {
    private final TheDimmingEcho game;
    private final SpriteBatch batch;
    private final Stage stage;
    private final Skin skin;
    private final BitmapFont menuFont;
    private final BitmapFont titleFont;
    private Texture backgroundTexture;
    private boolean backgroundLoaded = false;

    public MainMenuScreen(TheDimmingEcho game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.stage = new Stage(new ScreenViewport());

        // Load pixelated font for menu
        FreeTypeFontGenerator menuGenerator = new FreeTypeFontGenerator(Gdx.files.internal("DawnLike/GUI/SDS_8x8.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter menuParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        menuParameter.size = 32;
        menuParameter.color = Color.WHITE;
        menuParameter.shadowColor = new Color(0, 0, 0, 0.75f);
        menuParameter.shadowOffsetX = 2;
        menuParameter.shadowOffsetY = 2;
        this.menuFont = menuGenerator.generateFont(menuParameter);
        menuGenerator.dispose();

        // Load title font
        FreeTypeFontGenerator titleGenerator = new FreeTypeFontGenerator(Gdx.files.internal("DawnLike/GUI/SDS_8x8.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter titleParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        titleParameter.size = 48;
        titleParameter.color = Color.WHITE;
        titleParameter.shadowColor = new Color(0, 0, 0, 0.75f);
        titleParameter.shadowOffsetX = 3;
        titleParameter.shadowOffsetY = 3;
        this.titleFont = titleGenerator.generateFont(titleParameter);
        titleGenerator.dispose();

        // Create skin for UI
        this.skin = new Skin();
        skin.add("default-font", menuFont);

        Label.LabelStyle labelStyle = new Label.LabelStyle(menuFont, Color.WHITE);
        skin.add("default", labelStyle);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = menuFont;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.downFontColor = Color.GOLD;
        buttonStyle.overFontColor = Color.YELLOW;
        skin.add("default", buttonStyle);

        createUI();
    }

    private void createUI() {
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();

        // Title
        Label titleLabel = new Label("Dimming Echoes", skin);
        titleLabel.setFontScale(1.5f);
        mainTable.add(titleLabel).padBottom(50).row();

        // New Game Button
        TextButton newGameButton = new TextButton("New Game", skin);
        newGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.resetGame();
                game.setScreen(new PrologueScreen(game));
            }
        });
        mainTable.add(newGameButton).width(300).height(60).padBottom(20).row();

        // Load Game Button
        TextButton loadGameButton = new TextButton("Load Game", skin);
        loadGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new LoadGameScreen(game, MainMenuScreen.this));
            }
        });
        mainTable.add(loadGameButton).width(300).height(60).padBottom(20).row();

        // GitHub Button
        TextButton githubButton = new TextButton("See GitHub Page", skin);
        githubButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.net.openURI("https://github.com/DhonnanWork/DimmingEchoes");
            }
        });
        mainTable.add(githubButton).width(300).height(60).padBottom(20).row();

        // Exit Game Button
        TextButton exitButton = new TextButton("Exit Game", skin);
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
        mainTable.add(exitButton).width(300).height(60).padBottom(20).row();

        stage.addActor(mainTable);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        // Try to load background image
        try {
            backgroundTexture = new Texture(Gdx.files.internal("menu+bg.png"));
            backgroundLoaded = true;
        } catch (Exception e) {
            Gdx.app.log("MainMenuScreen", "Background image not found: menu_bg.png");
            backgroundLoaded = false;
        }
        AudioManager.getInstance().playMusic("audio/menu_theme.mp3", true);
    }

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // Draw background if available
        if (backgroundLoaded && backgroundTexture != null) {
            // Calculate scaling to fit the screen while maintaining aspect ratio
            float screenWidth = Gdx.graphics.getWidth();
            float screenHeight = Gdx.graphics.getHeight();
            float bgWidth = backgroundTexture.getWidth();
            float bgHeight = backgroundTexture.getHeight();

            float scaleX = screenWidth / bgWidth;
            float scaleY = screenHeight / bgHeight;
            float scale = Math.max(scaleX, scaleY);

            float scaledWidth = bgWidth * scale;
            float scaledHeight = bgHeight * scale;
            float x = (screenWidth - scaledWidth) / 2f;
            float y = (screenHeight - scaledHeight) / 2f;

            batch.draw(backgroundTexture, x, y, scaledWidth, scaledHeight);
        }

        batch.end();

        // Draw UI
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        // Not needed for this screen
    }

    @Override
    public void resume() {
        // Not needed for this screen
    }

    @Override
    public void hide() {
        AudioManager.getInstance().stopMusic();
        // Not needed for this screen
    }

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
        skin.dispose();
        menuFont.dispose();
        titleFont.dispose();
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
    }
}
