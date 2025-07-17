package com.dimmingechoes.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Align;
import com.dimmingechoes.manager.AudioManager;
import com.badlogic.gdx.Input;
import com.dimmingechoes.TheDimmingEcho;

public class EndingScreen implements Screen {
    private final String endingMessage;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private BitmapFont promptFont;
    private float fadeAlpha = 0f;
    private float fadeTimer = 0f;
    private static final float FADE_IN_DURATION = 2.0f;
    private float timeOnScreen = 0f;
    private int clickCount = 0;
    private boolean showReturnPrompt = false;
    private final TheDimmingEcho game;

    public EndingScreen(String endingMessage) {
        this(endingMessage, null);
    }
    public EndingScreen(String endingMessage, TheDimmingEcho game) {
        this.endingMessage = endingMessage;
        this.batch = new SpriteBatch();
        this.game = game;
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("LibertinusMono-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 28;
        parameter.color = Color.WHITE;
        parameter.shadowColor = new Color(0, 0, 0, 0.75f);
        parameter.shadowOffsetX = 2;
        parameter.shadowOffsetY = 2;
        this.font = generator.generateFont(parameter);
        // Prompt font
        FreeTypeFontGenerator.FreeTypeFontParameter promptParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        promptParam.size = 22;
        promptParam.color = Color.LIGHT_GRAY;
        promptParam.shadowColor = new Color(0, 0, 0, 0.75f);
        promptParam.shadowOffsetX = 2;
        promptParam.shadowOffsetY = 2;
        this.promptFont = generator.generateFont(promptParam);
        generator.dispose();
    }

    public void dispose() {
        batch.dispose();
        font.dispose();
        if (promptFont != null) promptFont.dispose();
    }

    @Override
    public void show() {
        fadeAlpha = 0f;
        fadeTimer = 0f;
        timeOnScreen = 0f;
        clickCount = 0;
        showReturnPrompt = false;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        fadeTimer += delta;
        timeOnScreen += delta;
        if (fadeAlpha < 1f) {
            fadeAlpha = Math.min(1f, fadeTimer / FADE_IN_DURATION);
        }
        if (Gdx.input.justTouched()) {
            clickCount++;
        }
        if (timeOnScreen > 20f || clickCount >= 2) {
            showReturnPrompt = true;
        }
        batch.begin();
        font.setColor(1, 1, 1, fadeAlpha);
        font.draw(batch, endingMessage, 0, Gdx.graphics.getHeight() / 2f, Gdx.graphics.getWidth(), Align.center, true);
        if (showReturnPrompt) {
            promptFont.setColor(1, 1, 1, fadeAlpha);
            promptFont.draw(batch, "Press ENTER to return to Main Menu", 0, 60, Gdx.graphics.getWidth(), Align.center, true);
        }
        batch.end();
        if (showReturnPrompt && Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && game != null) {
            game.setScreen(new MainMenuScreen(game));
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

}
