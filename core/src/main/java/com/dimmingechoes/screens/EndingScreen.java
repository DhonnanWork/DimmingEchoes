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
    private final TheDimmingEcho game;
    private final String endingID;
    private String narrationText;
    private String finalLineText;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private BitmapFont promptFont;
    private float fadeAlpha = 0f;
    private float fadeTimer = 0f;
    private static final float FADE_IN_DURATION = 2.0f;
    private float timeOnScreen = 0f;
    private boolean showReturnPrompt = false;
    private float typewriterProgress = 0f;
    private static final float TYPEWRITER_SPEED = 40f; // chars/sec
    private boolean narrationDone = false;
    private static final float PROMPT_DELAY = 1.5f;

    public EndingScreen(TheDimmingEcho game, String endingID) {
        this.game = game;
        this.endingID = endingID;
        this.batch = new SpriteBatch();
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
        selectEndingText(endingID);
    }

    private void selectEndingText(String id) {
        switch (id) {
            case "FADE":
                narrationText = "You gave everything you could. The echoes fade, but peace settles in the silence. The world is lighter, if only for a moment.";
                finalLineText = "Ending: The Fading Light";
                break;
            case "STONE":
                narrationText = "You kept the last piece for yourself. The echoes remain, unresolved. The world outside is unchanged, but you carry the weight within.";
                finalLineText = "Ending: The Stone Heart";
                break;
            case "VOID":
                narrationText = "You gave until there was nothing left. The echoes vanish, and so do you. In the void, there is neither pain nor memory.";
                finalLineText = "Ending: The Vanishing";
                break;
            case "NOMATTER":
                narrationText = "You walked these halls untouched, never giving nor losing. The world remains as it was, and so do you. Some stories end before they begin.";
                finalLineText = "Ending: A Place That No Longer Exists";
                break;
            case "FAILURE":
                narrationText = "You faltered, and the echoes slipped away. Some things cannot be reclaimed. The silence is absolute.";
                finalLineText = "Ending: Lost to Silence";
                break;
            default:
                narrationText = "The story ends, but the echoes remain.";
                finalLineText = "Ending: Unknown";
        }
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
        typewriterProgress = 0f;
        narrationDone = false;
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
        // Typewriter effect
        if (!narrationDone) {
            typewriterProgress += TYPEWRITER_SPEED * delta;
            if (typewriterProgress >= narrationText.length()) {
                typewriterProgress = narrationText.length();
                narrationDone = true;
            }
        }
        if (narrationDone && timeOnScreen > PROMPT_DELAY + (narrationText.length() / TYPEWRITER_SPEED)) {
            showReturnPrompt = true;
        }
        batch.begin();
        font.setColor(1, 1, 1, fadeAlpha);
        String toShow = narrationText.substring(0, Math.min((int)typewriterProgress, narrationText.length()));
        font.draw(batch, toShow, 0, Gdx.graphics.getHeight() / 2f + 40, Gdx.graphics.getWidth(), Align.center, true);
        if (narrationDone) {
            font.draw(batch, finalLineText, 0, Gdx.graphics.getHeight() / 2f - 40, Gdx.graphics.getWidth(), Align.center, true);
        }
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
