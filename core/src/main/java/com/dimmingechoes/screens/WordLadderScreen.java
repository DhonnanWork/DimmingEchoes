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

    private final String[] ladder = {"COLD", "CORD", "WORD", "WARM"};
    private int currentStep = 0;
    private final StringBuilder currentGuess = new StringBuilder();

    private boolean puzzleOver = false;
    private float transitionTimer = 0f;
    private static final float TRANSITION_DELAY = 2.0f;

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

        wordLabels = new Label[ladder.length];
        setupUI();
    }

    private void setupUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        infoLabel = new Label("Change one letter at a time.", skin);
        root.add(infoLabel).padBottom(30).row();

        for (int i = 0; i < ladder.length; i++) {
            wordLabels[i] = new Label(i == 0 ? ladder[i] : "_ _ _ _", skin);
            wordLabels[i].setAlignment(Align.center);
            root.add(wordLabels[i]).padBottom(15).row();
        }

        setupKeyboard();

        stage.addActor(root);
        stage.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean keyTyped(com.badlogic.gdx.scenes.scene2d.InputEvent event, char character) {
                if (puzzleOver) return false;
                if (Character.isLetter(character) && currentGuess.length() < ladder[0].length()) {
                    currentGuess.append(Character.toUpperCase(character));
                    updateDisplay();
                }
                return true;
            }

            @Override
            public boolean keyDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, int keycode) {
                if (puzzleOver) return false;
                if (keycode == Input.Keys.ENTER && currentGuess.length() == ladder[0].length()) {
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
                    if (!puzzleOver && currentGuess.length() < ladder[0].length()) {
                        currentGuess.append(btn.getText().toString());
                        updateDisplay();
                    }
                }
            });
            keyboard.add(btn).width(50).height(50).pad(2);
            if (c == 'P' || c == 'L') keyboard.row();
        }
    }

    private void submitGuess() {
        if (currentGuess.toString().equals(ladder[currentStep + 1])) {
            currentStep++;
            wordLabels[currentStep].setText(ladder[currentStep]);
            wordLabels[currentStep].setColor(Color.GREEN);
            currentGuess.setLength(0);
            if (currentStep == ladder.length - 1) {
                infoLabel.setText("You solved it!");
                puzzleOver = true;
                transitionTimer = TRANSITION_DELAY;
            }
        } else {
            infoLabel.setText("Not the right word. Try again.");
        }
    }

    private void updateDisplay() {
        wordLabels[currentStep + 1].setText(currentGuess.toString() + "_ ".repeat(Math.max(0, ladder[0].length() - currentGuess.length())));
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

        if (puzzleOver) {
            transitionTimer -= delta;
            if (transitionTimer <= 0) {
                game.setPuzzleWordLadderSolved(true);
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