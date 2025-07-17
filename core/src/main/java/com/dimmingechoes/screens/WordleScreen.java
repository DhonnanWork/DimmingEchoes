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

public class WordleScreen implements Screen {
    private static final String TARGET_WORD = "BOOK";
    private static final int WORD_LENGTH = 4;
    private static final int MAX_GUESSES = 5;

    private final TheDimmingEcho game;
    private final DungeonScreen dungeonScreen;
    private final SpriteBatch batch;
    private final Stage stage;
    private final Skin skin;
    private final BitmapFont font;
    private final StringBuilder currentGuess = new StringBuilder();
    private final String[] guesses = new String[MAX_GUESSES];
    private final int[][] feedback = new int[MAX_GUESSES][WORD_LENGTH]; // 0: gray, 1: yellow, 2: green
    private int guessCount = 0;
    private boolean puzzleOver = false;
    private boolean puzzleWon = false;
    private Label infoLabel;
    private float transitionTimer = 0f;
    private static final float TRANSITION_DELAY = 2.0f;

    public WordleScreen(TheDimmingEcho game, DungeonScreen dungeonScreen) {
        this.game = game;
        this.dungeonScreen = dungeonScreen;
        this.batch = new SpriteBatch();
        this.stage = new Stage(new ScreenViewport());
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("DawnLike/GUI/SDS_8x8.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 32;
        parameter.color = Color.WHITE;
        this.font = generator.generateFont(parameter);
        generator.dispose();
        this.skin = new Skin();
        skin.add("default-font", font);
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        skin.add("default", labelStyle);
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        skin.add("default", buttonStyle);
        setupUI();
    }

    private void setupUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.center();
        infoLabel = new Label("Guess the 4-letter word!", skin);
        root.add(infoLabel).padBottom(30).row();
        // Add guess grid
        for (int i = 0; i < MAX_GUESSES; i++) {
            Table row = new Table();
            for (int j = 0; j < WORD_LENGTH; j++) {
                Label cell = new Label("", skin);
                cell.setName("cell-" + i + "-" + j);
                cell.setAlignment(Align.center);
                cell.setColor(Color.LIGHT_GRAY);
                row.add(cell).width(60).height(60).pad(5);
            }
            root.add(row).row();
        }
        // Add on-screen keyboard (A-Z)
        Table keyboard = new Table();
        String keys = "QWERTYUIOPASDFGHJKLZXCVBNM";
        for (char c : keys.toCharArray()) {
            TextButton btn = new TextButton(String.valueOf(c), skin);
            btn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                    if (puzzleOver) return;
                    if (currentGuess.length() < WORD_LENGTH) {
                        currentGuess.append(btn.getText().toString());
                        updateGrid();
                    }
                }
            });
            keyboard.add(btn).width(50).height(50).pad(2);
            if (btn.getText().toString().equals("P") || btn.getText().toString().equals("L") || btn.getText().toString().equals("M"))
                keyboard.row();
        }
        // Add backspace and enter
        TextButton backBtn = new TextButton("<", skin);
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                if (puzzleOver) return;
                if (currentGuess.length() > 0) {
                    currentGuess.deleteCharAt(currentGuess.length() - 1);
                    updateGrid();
                }
            }
        });
        keyboard.add(backBtn).width(50).height(50).pad(2);
        TextButton enterBtn = new TextButton("ENTER", skin);
        enterBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                if (puzzleOver) return;
                if (currentGuess.length() == WORD_LENGTH) {
                    submitGuess();
                }
            }
        });
        keyboard.add(enterBtn).width(100).height(50).pad(2);
        root.add(keyboard).colspan(1).padTop(20).row();
        stage.addActor(root);
        // Add physical keyboard controls
        stage.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean keyTyped(com.badlogic.gdx.scenes.scene2d.InputEvent event, char character) {
                if (puzzleOver) return false;
                if (Character.isLetter(character) && currentGuess.length() < WORD_LENGTH) {
                    currentGuess.append(Character.toUpperCase(character));
                    updateGrid();
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, int keycode) {
                if (puzzleOver) return false;
                if (keycode == Input.Keys.ENTER && currentGuess.length() == WORD_LENGTH) {
                    submitGuess();
                    return true;
                }
                if (keycode == Input.Keys.BACKSPACE && currentGuess.length() > 0) {
                    currentGuess.deleteCharAt(currentGuess.length() - 1);
                    updateGrid();
                    return true;
                }
                return false;
            }
        });
    }

    private void updateGrid() {
        for (int i = 0; i < MAX_GUESSES; i++) {
            for (int j = 0; j < WORD_LENGTH; j++) {
                Label cell = stage.getRoot().findActor("cell-" + i + "-" + j);
                if (cell != null) {
                    if (i < guessCount) {
                        char c = guesses[i].charAt(j);
                        cell.setText(String.valueOf(c));
                        if (feedback[i][j] == 2) cell.setColor(Color.GREEN);
                        else if (feedback[i][j] == 1) cell.setColor(Color.GOLD);
                        else cell.setColor(Color.DARK_GRAY);
                    } else if (i == guessCount && j < currentGuess.length()) {
                        cell.setText(String.valueOf(currentGuess.charAt(j)));
                        cell.setColor(Color.LIGHT_GRAY);
                    } else {
                        cell.setText("");
                        cell.setColor(Color.LIGHT_GRAY);
                    }
                }
            }
        }
    }

    private void submitGuess() {
        String guess = currentGuess.toString();
        guesses[guessCount] = guess;
        // Feedback logic
        boolean[] used = new boolean[WORD_LENGTH];
        for (int j = 0; j < WORD_LENGTH; j++) {
            if (guess.charAt(j) == TARGET_WORD.charAt(j)) {
                feedback[guessCount][j] = 2; // green
                used[j] = true;
            }
        }
        for (int j = 0; j < WORD_LENGTH; j++) {
            if (feedback[guessCount][j] == 0) {
                for (int k = 0; k < WORD_LENGTH; k++) {
                    if (!used[k] && guess.charAt(j) == TARGET_WORD.charAt(k)) {
                        feedback[guessCount][j] = 1; // yellow
                        used[k] = true;
                        break;
                    }
                }
            }
        }
        updateGrid();
        if (guess.equals(TARGET_WORD)) {
            puzzleOver = true;
            puzzleWon = true;
            infoLabel.setText("Correct! It was BOOK.");
            transitionTimer = TRANSITION_DELAY;
            return;
        }
        guessCount++;
        currentGuess.setLength(0);
        if (guessCount >= MAX_GUESSES) {
            puzzleOver = true;
            puzzleWon = false;
            infoLabel.setText("Out of guesses! The answer was BOOK.");
            transitionTimer = TRANSITION_DELAY;
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        updateGrid();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.end();
        stage.act(delta);
        stage.draw();
        if (puzzleOver) {
            transitionTimer -= delta;
            if (transitionTimer <= 0) {
                if (!puzzleWon) {
                    game.logFailure();
                    game.setScreen(new EndingScreen(game, "FAILURE"));
                    return;
                } else {
                    dungeonScreen.puzzleCompleted(true);
                    game.setScreen(dungeonScreen);
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
    @Override
    public void dispose() { batch.dispose(); font.dispose(); stage.dispose(); }
} 