package com.dimmingechoes.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.dimmingechoes.TheDimmingEcho;
import com.dimmingechoes.manager.AudioManager;

public class EndingScreen implements Screen {
    private final TheDimmingEcho game;
    private final String endingID;
    private String[] narrationChunks;
    private String finalLineText;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final BitmapFont promptFont;

    private Texture endingTexture;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    private float timer = 0f;
    private float fadeAlpha = 0f;
    private static final float FADE_DURATION = 2.0f;
    private boolean isFadingIn = true;

    private int currentChunkIndex = 0;
    private float typewriterProgress = 0f;
    private static final float TYPEWRITER_SPEED = 40f; // chars/sec
    private boolean chunkFinishedTyping = false;
    private boolean allChunksDone = false;
    private boolean showReturnPrompt = false;

    public EndingScreen(TheDimmingEcho game, String endingID) {
        this.game = game;
        this.endingID = endingID;
        this.batch = new SpriteBatch();

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);

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
        this.promptFont = generator.generateFont(promptParam);
        generator.dispose();

        setupEnding(this.endingID);
    }

    private void setupEnding(String id) {
        String imagePath = "";
        String musicPath = "";
        switch (id) {
            case "FADE": // The Memory That Fades
                narrationChunks = new String[]{
                    "You give the final crystal, your last piece of identity, to the loving voice you once locked away. There is no grand explosion, no crumbling of walls. There is only a quiet, gentle release.",
                    "Your form begins to dissolve, not into dust, but into motes of soft light that drift on an unseen breeze. The sharp edges of the ruins soften, the oppressive silence is filled with a low, peaceful hum. You feel the pain of your own fading, but it is overshadowed by the warmth of the memory you have chosen to set free.",
                    "The Keeper is gone. But in the heart of the dungeon, the echo of love remains, no longer a whisper, but a steady, gentle light. You did not reclaim your soul; you gave it a better legacy."
                };
                finalLineText = "The Keeper is gone, but the love that broke their heart is finally free to heal.";
                imagePath = "ending1.jpeg";
                musicPath = "ending1.mp3";
                break;
            case "STONE": // The Keeper Becomes Stone
                narrationChunks = new String[]{
                    "You clutch the final crystal, a desperate anchor to the self you know. The light within it sputters and dies, trapped within your grasp. A cold stillness settles over you, a feeling heavier than grief. It is the feeling of permanence.",
                    "From your feet upwards, your form hardens. The flowing cloak becomes rigid, the sorrowful posture is frozen in time. The ruins around you cease their slow, dreamlike drift, becoming a fixed and silent tomb. You have won the battle against pain by building a fortress of flesh and bone so absolute that nothing can ever touch you again.",
                    "You are safe. You are whole. You are utterly, completely alone."
                };
                finalLineText = "In the heart of the ruin, you stand eternal—a monument to the fear of feeling.";
                imagePath = "ending2.jpeg";
                musicPath = "ending2.mp3";
                break;
            case "NOMATTER": // A Place That No Longer Exists
                narrationChunks = new String[]{
                    "You walk the path of understanding, but not of healing. You observed every fractured memory, cataloged every source of pain, and learned the precise architecture of your own sorrow. Yet, you offered nothing, risked nothing, gave nothing.",
                    "You have the answer, the blueprint to a shattered soul. But knowledge without warmth is a hollow thing. Your connection to this place, to the very feelings that define it, severs. You become a ghost, an observer with no substance, your own existence thinning until it is nothing but a footnote in a story you never truly lived.",
                    "You are not gone. You have simply ceased to matter."
                };
                finalLineText = "You have become the answer to a question no one can feel.";
                imagePath = "ending3.jpeg";
                musicPath = "ending3.mp3";
                break;
            case "VOID": // Petals in the Void
                narrationChunks = new String[]{
                    "With a final, selfless act, you give everything away. The vessel of grief known as The Keeper is no more, its purpose fulfilled. Your form scatters into a thousand blooming echoes, each a perfect, vibrant memory now free from the soul that held it.",
                    "These petals of light drift through the void. Where they land, the cold stone of the ruins transforms. Color bleeds back into the world, ethereal moss grows on the broken archways, and the dim sky shimmers with a soft, hopeful aurora. The fragments of your soul—joy, sorrow, wisdom—are no longer prisoners, but residents of a garden born from your sacrifice.",
                    "The Keeper has vanished completely, but the world they left behind is, for the first time, beautiful."
                };
                finalLineText = "The dungeon is no longer a place of sorrow, but a garden of memory, tended by the ghost of the one who gave everything to let it grow.";
                imagePath = "ending4.jpeg";
                musicPath = "ending4.mp3";
                break;
            case "PARTIAL": // The Echoes That Linger
                narrationChunks = new String[]{
                    "You restored some, but not all, of the echoes of yourself. You are no longer whole, but you are not entirely empty. The pain of some memories has returned, but with it, flickers of warmth.",
                    "The dungeon of your soul does not crumble, nor does it bloom. It remains, a quiet, broken monument to what was lost and what was reclaimed."
                };
                finalLineText = "You find a fragile peace in the twilight of your memories, forever caught between the sorrow of forgetting and the pain of remembering.";
                imagePath = "ending5.jpeg";
                musicPath = "ending5.mp3";
                break;
            case "FAILURE": // The Echoes Take Their Toll
                narrationChunks = new String[]{
                    "You faced the pain, but you were not strong enough. The echo you fought, the memory you failed to grasp, proved overwhelming. It did not just defeat you; it took a piece of you as its prize.",
                    "The air in the dungeon grows colder, the shadows deeper. The crystal that shattered to pay for your failure leaves a permanent scar on the landscape and on what remains of your soul. You are diminished, and the path forward is now steeper, haunted by the tangible cost of your weakness.",
                    "You may still continue, but you are no longer just the mender of this broken place. You are now one of its wounds."
                };
                finalLineText = "You came to heal a broken soul, and instead became just another one of its scars.";
                imagePath = "endingFail.jpeg";
                musicPath = "audio/ds vu.mp3";
                break;
            default:
                narrationChunks = new String[]{"The story ends, but the echoes remain."};
                finalLineText = "Ending: Unknown";
                imagePath = "endingFail.jpeg";
                musicPath = "audio/ds vu.mp3";
                break;
        }
        if (!imagePath.isEmpty()) {
            endingTexture = new Texture(Gdx.files.internal(imagePath));
        }
        if (!musicPath.isEmpty()) {
            AudioManager.getInstance().playMusic(musicPath, false);
        }
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        timer += delta;

        // Handle fade-in
        if (isFadingIn) {
            fadeAlpha = Math.min(1f, timer / FADE_DURATION);
            if (fadeAlpha >= 1f) {
                isFadingIn = false;
        }
        }

        // Handle input
        if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) || Gdx.input.justTouched()) {
            if (allChunksDone && !showReturnPrompt) {
                 showReturnPrompt = true;
            } else if (showReturnPrompt) {
                game.setScreen(new MainMenuScreen(game));
                return;
            } else if (chunkFinishedTyping) {
                currentChunkIndex++;
                if (currentChunkIndex >= narrationChunks.length) {
                    allChunksDone = true;
                } else {
                    typewriterProgress = 0;
                    chunkFinishedTyping = false;
                }
            } else {
                typewriterProgress = narrationChunks[currentChunkIndex].length();
        }
        }

        // Update typewriter
        if (!allChunksDone && !chunkFinishedTyping) {
            typewriterProgress += TYPEWRITER_SPEED * delta;
            if (typewriterProgress >= narrationChunks[currentChunkIndex].length()) {
                typewriterProgress = narrationChunks[currentChunkIndex].length();
                chunkFinishedTyping = true;
            }
        }

        // --- Rendering Logic ---
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw background image
        batch.setColor(1, 1, 1, fadeAlpha);
        if (endingTexture != null) {
            batch.draw(endingTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        }

        // Draw text
        font.setColor(1, 1, 1, fadeAlpha);
        float yPos = Gdx.graphics.getHeight() * 0.5f + 80;
        float width = viewport.getWorldWidth() - 100;
        // Draw narration centered and bold
        if (!allChunksDone) {
            String currentChunkText = narrationChunks[currentChunkIndex];
            int charsToShow = Math.min((int) typewriterProgress, currentChunkText.length());
            drawBoldText(font, batch, currentChunkText.substring(0, charsToShow), 50, yPos, width, Align.center, true, fadeAlpha);
        } else {
            drawBoldText(font, batch, finalLineText, 50, yPos, width, Align.center, true, fadeAlpha);
            }

        // Draw prompt
        if (showReturnPrompt) {
            promptFont.draw(batch, "Press any key to return to Main Menu", 0, 80, viewport.getWorldWidth(), Align.center, true);
        } else if (chunkFinishedTyping && !allChunksDone) {
             promptFont.draw(batch, "Press any key to continue...", 0, 80, viewport.getWorldWidth(), Align.center, true);
        }

        batch.end();
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

    // Helper to draw bold text (simulate thickness by drawing multiple times with offset)
    private void drawBoldText(BitmapFont font, SpriteBatch batch, String text, float x, float y, float targetWidth, int align, boolean wrap, float alpha) {
        font.setColor(0, 0, 0, alpha * 0.5f);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx == 0 && dy == 0) continue;
                font.draw(batch, text, x + dx, y + dy, targetWidth, align, wrap);
            }
        }
        font.setColor(1, 1, 1, alpha);
        font.draw(batch, text, x, y, targetWidth, align, wrap);
    }
}
