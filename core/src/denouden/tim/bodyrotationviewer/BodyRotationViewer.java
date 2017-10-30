package denouden.tim.bodyrotationviewer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;

public class BodyRotationViewer extends Game {

	public ModelBatch modelBatch;
	SpriteBatch batch;
	BitmapFont bitmapFont;

	@Override
	public void create () {
		batch = new SpriteBatch();
		bitmapFont = new BitmapFont();
		modelBatch = new ModelBatch();
		this.setScreen(new FileSelectScreen(this));
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		bitmapFont.dispose();
		modelBatch.dispose();
	}
}
