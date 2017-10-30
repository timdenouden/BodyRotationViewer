package denouden.tim.bodyrotationviewer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Created by Tim on 2/12/17.
 */

public class FileSelectScreen implements Screen {
    OrthographicCamera camera;
    BodyRotationViewer app;
    FileHandle[] handles;
    int selected = 0;

    public FileSelectScreen(BodyRotationViewer app) {
        this.app = app;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0f);
        camera.update();
        handles = Gdx.files.internal(Gdx.files.getLocalStoragePath()).list();
        Gdx.input.setInputProcessor(new InputAdapter() {

            @Override
            public boolean keyUp (int keyCode) {
                if(keyCode == Input.Keys.DPAD_DOWN) {
                    selected++;
                    if(selected > handles.length - 1) {
                        selected = 0;
                    }
                }
                else if(keyCode == Input.Keys.DPAD_UP) {
                    selected--;
                    if(selected < 0) {
                        selected = handles.length;
                    }
                }

                if(keyCode == Input.Keys.ENTER) {
                    app.setScreen(new ViewerScreen(app, handles[selected].name()));
                }
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        app.batch.setProjectionMatrix(camera.combined);
        app.batch.begin();
        app.bitmapFont.setColor(Color.WHITE);
        app.bitmapFont.draw(app.batch, "press up or down to select, press enter to play file", 0f, Gdx.graphics.getHeight() - 5);
        for (int i = 0; i < handles.length; i++) {
            if(i == selected) {
                app.bitmapFont.setColor(Color.GREEN);
            }
            else {
                app.bitmapFont.setColor(Color.WHITE);
            }
            app.bitmapFont.draw(app.batch, handles[i].name(), 0f, Gdx.graphics.getHeight() - (20 * i) -40);
        }
        app.batch.end();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
