package com.dimmingechoes.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Align;
import com.dimmingechoes.TheDimmingEcho;
import com.dimmingechoes.manager.AudioManager;

public class TitleScreen implements Screen {
    private final TheDimmingEcho game;
    private final SpriteBatch batch;
    private final BitmapFont titleFont;
    private final BitmapFont subtitleFont;
    private float fadeTimer = 0f;
    private float alpha = 0f;
    private boolean fadeInComplete = false;
    private static final float FADE_DURATION = 2.0f;
    private static final float DISPLAY_DURATION = 3.0f;
    private float displayTimer = 0f;
    private boolean shouldTransition = false;

    public TitleScreen(TheDimmingEcho game) {
        this.game = game;
        this.batch = new SpriteBatch();

        // Load pixelated font for title
        FreeTypeFontGenerator titleGenerator = new FreeTypeFontGenerator(Gdx.files.internal("DawnLike/GUI/SDS_8x8.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter titleParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        titleParameter.size = 48;
        titleParameter.color = Color.WHITE;
        titleParameter.shadowColor = new Color(0, 0, 0, 0.75f);
        titleParameter.shadowOffsetX = 2;
        titleParameter.shadowOffsetY = 2;
        this.titleFont = titleGenerator.generateFont(titleParameter);
        titleGenerator.dispose();

        // Load subtitle font
        FreeTypeFontGenerator subtitleGenerator = new FreeTypeFontGenerator(Gdx.files.internal("LibertinusMono-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter subtitleParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        subtitleParameter.size = 24;
        subtitleParameter.color = Color.WHITE;
        subtitleParameter.shadowColor = new Color(0, 0, 0, 0.75f);
        subtitleParameter.shadowOffsetX = 1;
        subtitleParameter.shadowOffsetY = 1;
        this.subtitleFont = subtitleGenerator.generateFont(subtitleParameter);
        subtitleGenerator.dispose();
    }

    @Override
    public void show() {
        fadeTimer = 0f;
        alpha = 0f;
        fadeInComplete = false;
        displayTimer = 0f;
        shouldTransition = false;
        AudioManager.getInstance().playMusic("audio/title_theme.mp3", true);
    }

    @Override
    public void render(float delta) {
        // Update fade timer
        if (!fadeInComplete) {
            fadeTimer += delta;
            alpha = Math.min(1.0f, fadeTimer / FADE_DURATION);
            if (alpha >= 1.0f) {
                fadeInComplete = true;
                fadeTimer = 0f;
            }
        } else {
            displayTimer += delta;
            if (displayTimer >= DISPLAY_DURATION) {
                shouldTransition = true;
            }
        }

        // Check for input to skip or transition
        if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) || Gdx.input.justTouched()) {
            shouldTransition = true;
        }

        // Transition to main menu
        if (shouldTransition) {
            game.setScreen(new MainMenuScreen(game));
            return;
        }

        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // Set alpha for fade effect
        titleFont.setColor(1, 1, 1, alpha);
        subtitleFont.setColor(1, 1, 1, alpha);

        // Draw title
        String title = "DIMming Echoes";
        float titleWidth = titleFont.draw(batch, title, 0, 0).width;
        float titleX = (Gdx.graphics.getWidth() - titleWidth) / 2f;
        float titleY = Gdx.graphics.getHeight() / 2f + 50;
        titleFont.draw(batch, title, titleX, titleY);

        // Draw subtitle with blinking effect
        if (fadeInComplete) {
            String subtitle = "Click to continue...";
            float subtitleWidth = subtitleFont.draw(batch, subtitle, 0, 0).width;
            float subtitleX = (Gdx.graphics.getWidth() - subtitleWidth) / 2f;
            float subtitleY = titleY - 100;
            
            // Blinking effect
            float blinkAlpha = (float) Math.abs(Math.sin(displayTimer * 3)) * alpha;
            subtitleFont.setColor(1, 1, 1, blinkAlpha);
            subtitleFont.draw(batch, subtitle, subtitleX, subtitleY);
        }

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // Not needed for this screen
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
        titleFont.dispose();
        subtitleFont.dispose();
    }
} 