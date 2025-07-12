package com.dimmingechoes.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.dimmingechoes.TheDimmingEcho;
import com.dimmingechoes.save.SaveManager;

public class PauseMenuScreen implements Screen {
    private final TheDimmingEcho game;
    private final DungeonScreen dungeonScreen;
    private final Stage stage;
    private final Skin skin;
    private final BitmapFont menuFont;
    private final BitmapFont titleFont;
    private boolean showingSaveSlots = false;
    private Table mainTable;
    private Table saveSlotsTable;

    public PauseMenuScreen(TheDimmingEcho game, DungeonScreen dungeonScreen) {
        this.game = game;
        this.dungeonScreen = dungeonScreen;
        this.stage = new Stage(new ScreenViewport());

        // Load pixelated font for menu
        FreeTypeFontGenerator menuGenerator = new FreeTypeFontGenerator(Gdx.files.internal("DawnLike/GUI/SDS_8x8.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter menuParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        menuParameter.size = 28;
        menuParameter.color = Color.WHITE;
        menuParameter.shadowColor = new Color(0, 0, 0, 0.75f);
        menuParameter.shadowOffsetX = 2;
        menuParameter.shadowOffsetY = 2;
        this.menuFont = menuGenerator.generateFont(menuParameter);
        menuGenerator.dispose();

        // Load title font
        FreeTypeFontGenerator titleGenerator = new FreeTypeFontGenerator(Gdx.files.internal("DawnLike/GUI/SDS_8x8.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter titleParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        titleParameter.size = 36;
        titleParameter.color = Color.WHITE;
        titleParameter.shadowColor = new Color(0, 0, 0, 0.75f);
        titleParameter.shadowOffsetX = 3;
        titleParameter.shadowOffsetY = 3;
        this.titleFont = titleGenerator.generateFont(titleParameter);
        titleGenerator.dispose();

        // Create skin for UI
        this.skin = new Skin();
        skin.add("default-font", menuFont);
        skin.add("title-font", titleFont);
        
        Label.LabelStyle labelStyle = new Label.LabelStyle(menuFont, Color.WHITE);
        skin.add("default", labelStyle);
        
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, Color.WHITE);
        skin.add("title", titleStyle);
        
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = menuFont;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.downFontColor = Color.GOLD;
        buttonStyle.overFontColor = Color.YELLOW;
        skin.add("default", buttonStyle);

        createMainMenu();
        createSaveSlotsMenu();
    }

    private void createMainMenu() {
        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();

        // Title
        Label titleLabel = new Label("PAUSED", skin, "title");
        mainTable.add(titleLabel).padBottom(50).row();

        // Resume Button
        TextButton resumeButton = new TextButton("Resume", skin);
        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(dungeonScreen);
            }
        });
        mainTable.add(resumeButton).width(250).height(50).padBottom(20).row();

        // Save Game Button
        TextButton saveButton = new TextButton("Save Game", skin);
        saveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showSaveSlots();
            }
        });
        mainTable.add(saveButton).width(250).height(50).padBottom(20).row();

        // Exit to Main Menu Button
        TextButton exitButton = new TextButton("Exit to Main Menu", skin);
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        mainTable.add(exitButton).width(250).height(50).padBottom(20).row();

        // Exit Game Button
        TextButton exitGameButton = new TextButton("Exit Game", skin);
        exitGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
        mainTable.add(exitGameButton).width(250).height(50).padBottom(20).row();

        stage.addActor(mainTable);
    }

    private void createSaveSlotsMenu() {
        saveSlotsTable = new Table();
        saveSlotsTable.setFillParent(true);
        saveSlotsTable.center();

        // Title
        Label titleLabel = new Label("Select Save Slot", skin, "title");
        saveSlotsTable.add(titleLabel).padBottom(50).row();

        // Create save slot buttons
        for (int i = 1; i <= SaveManager.getMaxSaveSlots(); i++) {
            final int slot = i;
            String slotText = getSlotText(slot);
            
            TextButton slotButton = new TextButton(slotText, skin);
            slotButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.saveGame(slot);
                    showMainMenu();
                }
            });
            saveSlotsTable.add(slotButton).width(300).height(50).padBottom(15).row();
        }

        // Back Button
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showMainMenu();
            }
        });
        saveSlotsTable.add(backButton).width(200).height(50).padTop(20).row();

        stage.addActor(saveSlotsTable);
        saveSlotsTable.setVisible(false);
    }

    private String getSlotText(int slot) {
        if (SaveManager.hasSave(slot)) {
            com.dimmingechoes.save.SaveData data = SaveManager.load(slot);
            if (data != null) {
                return "Slot " + slot + ": " + data.getSaveTimeString();
            }
        }
        return "Slot " + slot + ": Empty";
    }

    private void showSaveSlots() {
        mainTable.setVisible(false);
        saveSlotsTable.setVisible(true);
        showingSaveSlots = true;
        
        // Update slot texts
        for (int i = 0; i < saveSlotsTable.getChildren().size - 1; i++) { // -1 for back button
            if (saveSlotsTable.getChildren().get(i) instanceof TextButton) {
                TextButton button = (TextButton) saveSlotsTable.getChildren().get(i);
                button.setText(getSlotText(i + 1));
            }
        }
    }

    private void showMainMenu() {
        mainTable.setVisible(true);
        saveSlotsTable.setVisible(false);
        showingSaveSlots = false;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // Render the dungeon screen first (as background)
        dungeonScreen.render(delta);
        
        // Draw semi-transparent overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClearColor(0, 0, 0, 0.5f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Draw UI
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
        // Not needed for this screen
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        menuFont.dispose();
        titleFont.dispose();
    }
} 