package denouden.tim.bodyrotationviewer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.opencsv.CSVReader;

import java.util.ArrayList;

/**
 * Created by Tim on 2/12/17.
 */

public class ViewerScreen implements Screen {
    private static final String PATH_TO_DRIVE = "/Users/Tim/Google Drive/NASA Technology Commercialization /App/Posture Corrector/Body Rotation Sensor/";
    private static final float GRID_MIN = -10f;
    private static final float GRID_MAX = 10f;
    private static final float GRID_STEP = 1f;

    private BodyRotationViewer app;
    private String filename = "bodyRotationSensorOutput.csv";
    private Environment environment;
    private PerspectiveCamera cam;
    private OrthographicCamera orthoCam;
    private Model model, debugModel;
    private ModelInstance instance, debugModelInstance;

    ArrayList<float[]> data;
    int currentStepIndex = 0;

    public ViewerScreen(BodyRotationViewer app, String filename) {
        this.app = app;
        if(filename.length() < 0) {
            app.setScreen(new FileSelectScreen(app));
        }
        else {
            this.filename = filename;
        }
    }

    @Override
    public void show() {
        FileHandle fileHandle = Gdx.files.internal(filename);
        if(fileHandle.exists()) {
            data = loadCSVFile(fileHandle);
        }
        else {
            data = new ArrayList<float[]>();
            Gdx.app.log("BodyRotationViewer", "file not found at " + filename);
        }
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(10f, 10f, 10f);
        cam.lookAt(0,0,0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        orthoCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        orthoCam.position.set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0f);
        orthoCam.update();

        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createBox(2f, 4f, 1f,
                new Material(ColorAttribute.createDiffuse(Color.GREEN)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        instance = new ModelInstance(model);

        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("grid", GL20.GL_LINES, VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorUnpacked, new Material());
        builder.setColor(Color.LIGHT_GRAY);
        for (float t = GRID_MIN; t <= GRID_MAX; t += GRID_STEP) {
            builder.line(t, 0, GRID_MIN, t, 0, GRID_MAX);
            builder.line(GRID_MIN, 0, t, GRID_MAX, 0, t);
        }
        builder = modelBuilder.part("axes", GL20.GL_LINES, VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorUnpacked, new Material());
        builder.setColor(Color.RED);        //X
        builder.line(0, 0, 0, 100, 0, 0);
        builder.setColor(Color.GREEN);      //Y
        builder.line(0, 0, 0, 0, 100, 0);
        builder.setColor(Color.BLUE);       //Z
        builder.line(0, 0, 0, 0, 0, 100);
        debugModel = modelBuilder.end();
        debugModelInstance = new ModelInstance(debugModel);

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(new CameraInputController(cam));
        multiplexer.addProcessor(new InputAdapter() {

            @Override
            public boolean keyUp (int keyCode) {
                if(keyCode == Input.Keys.SPACE) {
                    currentStepIndex = 0;
                }
                if(keyCode == Input.Keys.ESCAPE) {
                    app.setScreen(new FileSelectScreen(app));
                }
                return true;
            }
        });

        Gdx.input.setInputProcessor(multiplexer);
    }

    private ArrayList<float[]> loadCSVFile(FileHandle fileHandle) {
        CSVReader reader = new CSVReader(fileHandle.reader());
        ArrayList<float[]> data = new ArrayList<float[]>();
        try {
            String [] nextLine;
            reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                float[] step = new float[4];
                for(int i = 0; i < step.length; i++) {
                    step[i] = Float.valueOf(nextLine[i]);
                }
                data.add(step);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        cam.update();
        orthoCam.update();
        updateDataStep();

        app.modelBatch.begin(cam);
        app.modelBatch.render(debugModelInstance);
        app.modelBatch.render(instance, environment);
        app.modelBatch.end();

        app.batch.setProjectionMatrix(orthoCam.combined);
        app.batch.begin();
        app.bitmapFont.draw(app.batch, "Press Space to restart, ESC to quit", 0f, Gdx.graphics.getHeight() - 5);
        app.batch.end();
    }

    private void updateDataStep() {
        instance.transform.idt();
        if(currentStepIndex < data.size()) {
            float[] currentStep = data.get(currentStepIndex);
            instance.transform.setFromEulerAngles(currentStep[0], currentStep[1], currentStep[2]);
            currentStepIndex++;
        }
        instance.transform.translate(0, 2f, 0);
        instance.calculateTransforms();
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
        model.dispose();
    }
}
