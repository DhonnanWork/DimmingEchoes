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

public class EndingScreen implements Screen {
    private final String endingMessage;
    private final SpriteBatch batch;
    private final BitmapFont font;

    public EndingScreen(String endingMessage) {
        this.endingMessage = endingMessage;
        this.batch = new SpriteBatch();

        // Menggunakan font yang lebih baik, sama seperti di DungeonScreen
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("LibertinusMono-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 28; // Ukuran font bisa disesuaikan
        parameter.color = Color.WHITE;
        parameter.shadowColor = new Color(0, 0, 0, 0.75f);
        parameter.shadowOffsetX = 2;
        parameter.shadowOffsetY = 2;
        this.font = generator.generateFont(parameter);
        generator.dispose();
    }

    public void dispose() {
        batch.dispose();
        font.dispose();
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        font.draw(batch, endingMessage, 0, Gdx.graphics.getHeight() / 2f, Gdx.graphics.getWidth(), Align.center, true);
        batch.end();

        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

}
