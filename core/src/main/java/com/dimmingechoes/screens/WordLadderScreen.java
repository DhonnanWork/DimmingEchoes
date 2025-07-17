package com.dimmingechoes.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.dimmingechoes.TheDimmingEcho;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WordLadderScreen implements Screen {
    private final TheDimmingEcho game;
    private final DungeonScreen dungeonScreen;
    private final SpriteBatch batch;
    private final Stage stage;
    private final Skin skin;
    private Label infoLabel;
    private final Label[] wordLabels;

    // Add a set of valid 4-letter words for validation
    private static final java.util.Set<String> VALID_WORDS = new java.util.HashSet<>(java.util.Arrays.asList(
        "COLD", "CORD", "CARD", "WORD", "WARD", "WARM"
    ));
    // Change ladder to only require start and end
    private final String startWord = "COLD";
    private final String endWord = "WARM";
    private String currentWord = startWord;
    private int steps = 0;
    private static final int MAX_STEPS = 5;
    private final StringBuilder currentGuess = new StringBuilder();

    private boolean puzzleOver = false;
    private float transitionTimer = 0f;
    private static final float TRANSITION_DELAY = 2.0f;
    private int incorrectAttempts = 0;
    private static final int MAX_ATTEMPTS = 3;
    private boolean gameOver = false;

    public WordLadderScreen(TheDimmingEcho game, DungeonScreen dungeonScreen) {
        this.game = game;
        this.dungeonScreen = dungeonScreen;
        this.batch = new SpriteBatch();
        this.stage = new Stage(new ScreenViewport());

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("DawnLike/GUI/SDS_8x8.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 32;
        parameter.color = Color.WHITE;
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();

        this.skin = new Skin();
        skin.add("default-font", font);
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        skin.add("default", labelStyle);
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        skin.add("default", buttonStyle);

        wordLabels = new Label[MAX_STEPS + 1];
        setupUI();
    }

    private void setupUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        infoLabel = new Label("Change one letter at a time to turn COLD into WARM.", skin);
        root.add(infoLabel).padBottom(30).row();
        wordLabels[0] = new Label(startWord, skin);
        wordLabels[0].setAlignment(Align.center);
        root.add(wordLabels[0]).padBottom(15).row();
        for (int i = 1; i <= MAX_STEPS; i++) {
            wordLabels[i] = new Label("_ _ _ _", skin);
            wordLabels[i].setAlignment(Align.center);
            root.add(wordLabels[i]).padBottom(15).row();
        }

        setupKeyboard();

        stage.addActor(root);
        stage.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean keyTyped(com.badlogic.gdx.scenes.scene2d.InputEvent event, char character) {
                if (puzzleOver) return false;
                if (Character.isLetter(character) && currentGuess.length() < 4) {
                    currentGuess.append(Character.toUpperCase(character));
                    updateDisplay();
                }
                return true;
            }

            @Override
            public boolean keyDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, int keycode) {
                if (puzzleOver) return false;
                if (keycode == Input.Keys.ENTER && currentGuess.length() == 4) {
                    submitGuess();
                } else if (keycode == Input.Keys.BACKSPACE && currentGuess.length() > 0) {
                    currentGuess.deleteCharAt(currentGuess.length() - 1);
                    updateDisplay();
                }
                return true;
            }
        });
    }

    private void setupKeyboard() {
        Table keyboard = stage.getRoot().findActor("keyboard");
        if (keyboard == null) {
            keyboard = new Table();
            keyboard.setName("keyboard");
            stage.addActor(keyboard);
        }
        keyboard.clear();
        String keys = "QWERTYUIOPASDFGHJKLZXCVBNM";
        for (char c : keys.toCharArray()) {
            TextButton btn = new TextButton(String.valueOf(c), skin);
            btn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                    if (!puzzleOver && !gameOver && currentGuess.length() < 4) {
                        currentGuess.append(btn.getText().toString());
                        updateDisplay();
                    }
                }
            });
            keyboard.add(btn).width(50).height(50).pad(2);
            if (c == 'P' || c == 'L') keyboard.row();
        }
        // Position the keyboard at 20% of the screen height and center it
        keyboard.setPosition(
            (stage.getWidth() - keyboard.getPrefWidth()) / 2f,
            stage.getHeight() * 0.2f
        );
    }

    private void submitGuess() {
        if (gameOver) return;
        String guess = currentGuess.toString();
        if (guess.length() != 4 || !VALID_WORDS.contains(guess)) {
            infoLabel.setText("Not a valid word.");
            currentGuess.setLength(0);
            updateDisplay();
            return;
        }
        // Check one letter difference
        int diff = 0;
        for (int i = 0; i < 4; i++) {
            if (guess.charAt(i) != currentWord.charAt(i)) diff++;
        }
        if (diff != 1) {
            infoLabel.setText("Change exactly one letter.");
            currentGuess.setLength(0);
            updateDisplay();
            return;
        }
        steps++;
        currentWord = guess;
        wordLabels[steps].setText(currentWord);
        wordLabels[steps].setColor(Color.GREEN);
        currentGuess.setLength(0);
        incorrectAttempts = 0;
        if (currentWord.equals(endWord)) {
            infoLabel.setText("You solved it!");
            puzzleOver = true;
            transitionTimer = TRANSITION_DELAY;
        } else if (steps == MAX_STEPS) {
            infoLabel.setText("Game Over! Too many steps.");
            gameOver = true;
            transitionTimer = TRANSITION_DELAY;
        }
    }

    private void updateDisplay() {
        wordLabels[steps + 1].setText(currentGuess.toString() + "_ ".repeat(Math.max(0, 4 - currentGuess.length())));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();

        if ((puzzleOver || gameOver) && transitionTimer > 0) {
            transitionTimer -= delta;
            if (transitionTimer <= 0) {
                if (puzzleOver) {
                    game.setPuzzleWordLadderSolved(true);
                }
                game.setScreen(dungeonScreen);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        batch.dispose();
        stage.dispose();
        skin.dispose();
    }
} 