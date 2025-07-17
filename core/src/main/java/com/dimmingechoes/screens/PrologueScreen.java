package com.dimmingechoes.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.dimmingechoes.TheDimmingEcho;
import com.badlogic.gdx.utils.Align;

public class PrologueScreen implements Screen {
    private final TheDimmingEcho game;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final String[] prologueTexts = new String[] {
        "To escape the pain of grief, a soul can build its own prison.",
        "It is done. The silence you purchased.\nThe peace you built from the dust of your own heart.",
        "You sealed this place. But now you walk it again.",
        "The dungeon is not a place.\nIt is the echo of a soul trying to remember why it broke.",
        "Take the first step. See if it is worth the cost."
    };
    private int currentChunk = 0;
    private float chunkTimer = 0f;
    private float fadeAlpha = 0f;
    private float typewriterProgress = 0f;
    private boolean fadingIn = true;
    private boolean fadingOut = false;
    private boolean finished = false;
    private static final float FADE_DURATION = 1.0f;
    private static final float TYPEWRITER_SPEED = 40f; // chars/sec
    private static final float CHUNK_DISPLAY_TIME = 2.0f; // seconds after typewriter
    private static final float FADE_OUT_DELAY = 0.5f;
    private float postTypewriterTimer = 0f;

    public PrologueScreen(TheDimmingEcho game) {
        this.game = game;
        this.batch = new SpriteBatch();
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("DawnLike/GUI/SDS_8x8.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 32;
        parameter.color = Color.WHITE;
        parameter.shadowColor = new Color(0, 0, 0, 0.75f);
        parameter.shadowOffsetX = 2;
        parameter.shadowOffsetY = 2;
        this.font = generator.generateFont(parameter);
        generator.dispose();
    }

    @Override
    public void show() {
        currentChunk = 0;
        chunkTimer = 0f;
        fadeAlpha = 0f;
        typewriterProgress = 0f;
        fadingIn = true;
        fadingOut = false;
        finished = false;
        postTypewriterTimer = 0f;
    }

    @Override
    public void render(float delta) {
        if (finished) {
            game.setScreen(new DungeonScreen(game));
            return;
        }

        // Handle input to skip
        if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) || Gdx.input.justTouched()) {
            finished = true;
            return;
        }

        String chunk = prologueTexts[currentChunk];
        int totalChars = chunk.length();

        // Fade in
        if (fadingIn) {
            fadeAlpha += delta / FADE_DURATION;
            if (fadeAlpha >= 1f) {
                fadeAlpha = 1f;
                fadingIn = false;
            }
        } else if (!fadingOut) {
            // Typewriter effect
            if (typewriterProgress < totalChars) {
                typewriterProgress += TYPEWRITER_SPEED * delta;
                if (typewriterProgress > totalChars) typewriterProgress = totalChars;
            } else {
                postTypewriterTimer += delta;
                if (postTypewriterTimer >= CHUNK_DISPLAY_TIME) {
                    fadingOut = true;
                    postTypewriterTimer = 0f;
                }
            }
        } else {
            // Fade out
            fadeAlpha -= delta / FADE_DURATION;
            if (fadeAlpha <= 0f) {
                fadeAlpha = 0f;
                fadingOut = false;
                currentChunk++;
                if (currentChunk >= prologueTexts.length) {
                    finished = true;
                } else {
                    // Reset for next chunk
                    typewriterProgress = 0f;
                    fadingIn = true;
                }
            }
        }

        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        font.setColor(1, 1, 1, fadeAlpha);
        String toDraw = chunk.substring(0, Math.min((int)typewriterProgress, chunk.length()));
        float width = Gdx.graphics.getWidth() * 0.8f;
        float x = (Gdx.graphics.getWidth() - width) / 2f;
        float y = Gdx.graphics.getHeight() / 2f + 40;
        font.draw(batch, toDraw, x, y, width, Align.center, true);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {}
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
} 