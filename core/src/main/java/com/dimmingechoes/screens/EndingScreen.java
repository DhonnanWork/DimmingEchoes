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

public class EndingScreen implements Screen {
    private final String endingMessage;
    private final SpriteBatch batch;
    private final BitmapFont font;

    private Texture endingImage;
    private final String imagePath;

    public EndingScreen(String endingMessage, String imagePath) {
        this.endingMessage = endingMessage;
        this.imagePath = imagePath;
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
        if (endingImage != null) {
            endingImage.dispose();
        }
    }

    @Override
    public void show() {
        // Muat gambar hanya jika path-nya valid
        if (imagePath != null && !imagePath.isEmpty()) {
            endingImage = new Texture(Gdx.files.internal(imagePath));
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        font.draw(batch, endingMessage, 50, Gdx.graphics.getHeight() - 50);
        font.draw(batch, "Press ESC to exit.", 50, 100);
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
