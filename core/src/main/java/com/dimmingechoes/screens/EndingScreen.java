package com.dimmingechoes.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.dimmingechoes.TheDimmingEcho;
import com.dimmingechoes.manager.AudioManager;

public class EndingScreen implements Screen {
    private final TheDimmingEcho game;
    private final String endingID;
    private String narrationText;
    private String finalLineText;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private BitmapFont promptFont;

    private Texture endingTexture;
    private OrthographicCamera camera;
    private Viewport viewport;

    private float imageFadeAlpha = 0f;
    private float textFadeAlpha = 0f;
    private float timer = 0f;

    private static final float IMAGE_FADE_IN_DURATION = 2.0f;
    private static final float TEXT_FADE_IN_DELAY = IMAGE_FADE_IN_DURATION + 1.0f;
    private static final float TEXT_FADE_IN_DURATION = 1.5f;
    private static final float PROMPT_DELAY = TEXT_FADE_IN_DELAY + TEXT_FADE_IN_DURATION + 1.0f;

    private boolean imageFadeInDone = false;
    private boolean textFadeInDone = false;
    private boolean showReturnPrompt = false;

    private float typewriterProgress = 0f;
    private static final float TYPEWRITER_SPEED = 40f; // chars/sec

    public EndingScreen(TheDimmingEcho game, String endingID) {
        this.game = game;
        this.endingID = endingID;
        this.batch = new SpriteBatch();

        camera = new OrthographicCamera();
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("LibertinusMono-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 28;
        parameter.color = Color.WHITE;
        parameter.shadowColor = new Color(0, 0, 0, 0.75f);
        parameter.shadowOffsetX = 2;
        parameter.shadowOffsetY = 2;
        this.font = generator.generateFont(parameter);

        FreeTypeFontGenerator.FreeTypeFontParameter promptParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        promptParam.size = 22;
        promptParam.color = Color.LIGHT_GRAY;
        promptParam.shadowColor = new Color(0, 0, 0, 0.75f);
        promptParam.shadowOffsetX = 2;
        promptParam.shadowOffsetY = 2;
        this.promptFont = generator.generateFont(promptParam);
        generator.dispose();
    }

    private void setupEnding(String id) {
        String imagePath = "";
        String musicPath = "";
        switch (id) {
            case "FADE":
                narrationText = "You gave everything you could. The echoes fade, but peace settles in the silence. The world is lighter, if only for a moment.";
                finalLineText = "Ending: The Fading Light";
                imagePath = "ending1.jpeg";
                musicPath = "ending1.mp3";
                break;
            case "STONE":
                narrationText = "You kept the last piece for yourself. The echoes remain, unresolved. The world outside is unchanged, but you carry the weight within.";
                finalLineText = "Ending: The Stone Heart";
                imagePath = "ending2.jpeg";
                musicPath = "ending2.mp3";
                break;
            case "VOID":
                narrationText = "You gave until there was nothing left. The echoes vanish, and so do you. In the void, there is neither pain nor memory.";
                finalLineText = "Ending: The Vanishing";
                imagePath = "ending3.jpeg";
                musicPath = "ending3.mp3";
                break;
            case "NOMATTER":
                narrationText = "You walked these halls untouched, never giving nor losing. The world remains as it was, and so do you. Some stories end before they begin.";
                finalLineText = "Ending: A Place That No Longer Exists";
                imagePath = "ending4.jpeg";
                musicPath = "ending4.mp3";
                break;
            case "PARTIAL":
                narrationText = "You restored some, but not all, of the echoes of yourself. You are no longer whole, but you are not entirely empty. The pain of some memories has returned, but with it, flickers of warmth. The dungeon of your soul does not crumble, nor does it bloom. It remains, a quiet, broken monument to what was lost and what was reclaimed. You find a fragile peace in the twilight of your memories, forever caught between the sorrow of forgetting and the pain of remembering.";
                finalLineText = "Ending: The Echoes That Linger";
                imagePath = "ending5.png";
                musicPath = "ending5.mp3";
                break;
            case "FAILURE":
                narrationText = "You faltered, and the echoes slipped away. Some things cannot be reclaimed. The silence is absolute.";
                finalLineText = "Ending: Lost to Silence";
                imagePath = "endingFail.jpeg";
                musicPath = "endingFail.mp3";
                break;
            default:
                narrationText = "The story ends, but the echoes remain.";
                finalLineText = "Ending: Unknown";
                imagePath = "endingFail.jpeg";
                musicPath = "endingFail.mp3";
        }
        if (!imagePath.isEmpty()) {
            endingTexture = new Texture(Gdx.files.internal(imagePath));
        }
        if (!musicPath.isEmpty()) {
            AudioManager.getInstance().playMusic("audio/" + musicPath, false);
        }
    }

    @Override
    public void show() {
        setupEnding(this.endingID);
    }

    @Override
    public void render(float delta) {
        timer += delta;

        // Update animations
        if (!imageFadeInDone) {
            imageFadeAlpha = Math.min(1f, timer / IMAGE_FADE_IN_DURATION);
            if (imageFadeAlpha >= 1f) imageFadeInDone = true;
        }
        if (imageFadeInDone && !textFadeInDone) {
            float textProgress = (timer - TEXT_FADE_IN_DELAY) / TEXT_FADE_IN_DURATION;
            textFadeAlpha = Math.min(1f, textProgress);
            if (textFadeAlpha >= 1f) textFadeInDone = true;
        }
        if (textFadeInDone && timer >= PROMPT_DELAY) {
            showReturnPrompt = true;
        }

        // --- Rendering Logic ---
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);

        // Remove all blurShader, fboA, fboB, and blurRadius logic. Only use a fade-in for the image.
        batch.begin();
        batch.setColor(1, 1, 1, imageFadeAlpha);
        if (endingTexture != null) {
            batch.draw(endingTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        }
        batch.setColor(Color.WHITE);
        batch.end();

        // --- Text Rendering ---
        batch.begin();
        if (textFadeInDone) {
            int charsToShow = Math.min((int)typewriterProgress, narrationText.length());
            font.setColor(1, 1, 1, textFadeAlpha);
            font.draw(batch, narrationText.substring(0, charsToShow), 0, Gdx.graphics.getHeight() / 2f + 60, Gdx.graphics.getWidth(), Align.center, true);
            if (charsToShow == narrationText.length()) {
                font.draw(batch, finalLineText, 0, Gdx.graphics.getHeight() / 2f - 60, Gdx.graphics.getWidth(), Align.center, true);
            }
        }

        if (showReturnPrompt) {
            promptFont.setColor(1, 1, 1, 1);
            promptFont.draw(batch, "Press ENTER to return to Main Menu", 0, 80, Gdx.graphics.getWidth(), Align.center, true);
        }
        batch.end();

        if (showReturnPrompt && Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && game != null) {
            game.setScreen(new MainMenuScreen(game));
        }

        if (textFadeInDone && typewriterProgress < narrationText.length()) {
            typewriterProgress += TYPEWRITER_SPEED * delta;
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        if (promptFont != null) promptFont.dispose();
        if (endingTexture != null) endingTexture.dispose();
        AudioManager.getInstance().stopMusic();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}
}
