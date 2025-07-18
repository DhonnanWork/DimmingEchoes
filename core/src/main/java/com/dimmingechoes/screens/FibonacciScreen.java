package com.dimmingechoes.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.dimmingechoes.TheDimmingEcho;
import com.dimmingechoes.manager.GameLogger;

public class FibonacciScreen implements Screen {
    private final TheDimmingEcho game;
    private final DungeonScreen dungeonScreen;
    private final Stage stage;
    private final Skin skin;
    private Label sequenceLabel;
    private TextField answerField;
    private Label infoLabel;

    private final int correctAnswer = 8;
    private boolean puzzleOver = false;
    private float transitionTimer = 0f;
    private static final float TRANSITION_DELAY = 2.0f;
    private int incorrectAttempts = 0;
    private static final int MAX_ATTEMPTS = 3;

    public FibonacciScreen(TheDimmingEcho game, DungeonScreen dungeonScreen) {
        this.game = game;
        this.dungeonScreen = dungeonScreen;
        this.stage = new Stage(new ScreenViewport());

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("DawnLike/GUI/SDS_8x8.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 32;
        parameter.color = Color.WHITE;
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();

        this.skin = new Skin();
        skin.add("default-font", font);
        skin.add("default", new Label.LabelStyle(font, Color.WHITE));
        TextField.TextFieldStyle tfs = new TextField.TextFieldStyle();
        tfs.font = font;
        tfs.fontColor = Color.WHITE;
        // Do not set cursor, selection, or background drawables to avoid 'white' error
        skin.add("default", tfs);

        setupUI();
    }

    private void setupUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        infoLabel = new Label("What number comes next?", skin);
        root.add(infoLabel).padBottom(30).row();

        sequenceLabel = new Label("1, 1, 2, 3, 5, ?", skin);
        root.add(sequenceLabel).padBottom(20).row();

        answerField = new TextField("", skin);
        answerField.setAlignment(Align.center);
        root.add(answerField).width(100).padBottom(20).row();

        stage.addActor(root);
        stage.setKeyboardFocus(answerField);

        answerField.setTextFieldListener((textField, c) -> {
            if (c == '\n' || c == '\r') {
                submitGuess();
            }
        });
    }

    private void submitGuess() {
        if (puzzleOver) return;
        try {
            int guess = Integer.parseInt(answerField.getText());
            if (guess == correctAnswer) {
                infoLabel.setText("Correct!");
                puzzleOver = true;
                transitionTimer = TRANSITION_DELAY;
                GameLogger.getInstance().log("Player solved the Fibonacci puzzle.");
            } else {
                incorrectAttempts++;
                if (incorrectAttempts >= MAX_ATTEMPTS) {
                    infoLabel.setText("Too many wrong guesses.");
                    puzzleOver = true;
                    transitionTimer = TRANSITION_DELAY;
                    game.logFailure();
                    GameLogger.getInstance().log("Player failed Fibonacci puzzle (too many attempts).");
                    return;
                }
                infoLabel.setText("That's not it. Try again.");
                answerField.setText("");
            }
        } catch (NumberFormatException e) {
            infoLabel.setText("Please enter a number.");
            answerField.setText("");
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();

        if (puzzleOver) {
            transitionTimer -= delta;
            if (transitionTimer <= 0) {
                game.setPuzzleFibonacciSolved(true);
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
        stage.dispose();
        skin.dispose();
    }
} 