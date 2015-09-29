/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terrain;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import main.FPCameraController;

/**
 *
 * @author S.S.Iyer
 */
public class VoxelTest extends ApplicationAdapter {
    
    final static int UNITS_PER_METER = 16;
    final float HUMAN_HEIGHT = 3f*UNITS_PER_METER;
    final float[] FOG_COLOR = new float[] {0.13f, 0.13f, 0.13f, 1f};
    SpriteBatch spriteBatch;
    BitmapFont font;
    ModelBatch modelBatch;
    PerspectiveCamera camera;
    Environment environment;
    FPCameraController camController;
    VoxelWorld voxelWorld;
    boolean fullscreen = false;
    boolean descendLimit = true;
    boolean enableWireframe = false;
    int oldWidth, oldHeight;
    ModelInstance skySphere;
    AssetManager assets;
    boolean assetLoading;
    Array<ModelInstance> instances;

    @Override
    public void create () {
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        modelBatch = new ModelBatch();
        instances = new Array<>();
        assets = new AssetManager();
        DefaultShader.defaultCullFace = GL20.GL_FRONT;
        
        // Set up a 3D perspective camera
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(18f*UNITS_PER_METER, 12f*UNITS_PER_METER, 0f);
        camera.lookAt(0,0,0);
        camera.near = 1.5f;
        camera.far = 300f*UNITS_PER_METER;
        camera.up.set(Vector3.Y);
        camera.update();
        
        // Setup a camera controller to control the camera movements
        camController = new FPCameraController(camera);
        camController.setVelocity(0.5f*UNITS_PER_METER);
        Gdx.input.setInputProcessor(camController);
        
        // Load a 3d Model
        assets.load("tower/tower.g3db", Model.class);
        assets.load("flags/flagBlue.g3db", Model.class);
        assets.load("flags/flagNone.g3db", Model.class);
        assets.load("flags/flagRed.g3db", Model.class);
        assets.load("trees/tree1.g3db", Model.class);
        assets.load("trees/tree2.g3db", Model.class);
        assets.load("trees/tree3.g3db", Model.class);
        assets.load("trees/tree4.g3db", Model.class);
        assets.load("spacesphere/spacesphere.g3db", Model.class);
        assetLoading = false;

        // Load the voxel texture
        Texture texture = new Texture(Gdx.files.internal("tiles.png"));
        TextureRegion[][] tiles = TextureRegion.split(texture, 32, 32);

        MathUtils.random.setSeed(0);
        voxelWorld = new VoxelWorld(tiles[0], 10, 3, 10);
        PerlinNoiseGenerator.generateVoxels(voxelWorld, 0, 63, 10);
        float camX = voxelWorld.voxelsX / 2f;
        float camZ = voxelWorld.voxelsZ / 2f;
        float camY = voxelWorld.getHighest(camX, camZ) + 1.5f;
        camera.position.set(camX, camY, camZ);
        
        // create the surrounding environment
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.set(new ColorAttribute(ColorAttribute.Fog, FOG_COLOR[0], 
                FOG_COLOR[1], FOG_COLOR[2], FOG_COLOR[3]));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
    }

    @Override
    public void render () {
        // Clear the color buffer and the depth buffer
        Gdx.gl.glClearColor(FOG_COLOR[0], FOG_COLOR[1], FOG_COLOR[2], FOG_COLOR[3]);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        
        // Check for user input
        checkInput();
        
        // Set viewport
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        // Load assets once asset manager is done loading the asset
        if(assetLoading && assets.update()) {
            assetLoading();
        }
        
        modelBatch.begin(camera);
        modelBatch.render(voxelWorld, environment);
        modelBatch.render(instances, environment);
        modelBatch.end();
        camController.update();

        spriteBatch.begin();
        font.draw(spriteBatch, "fps: " + Gdx.graphics.getFramesPerSecond() 
                + ", #visible chunks: " + voxelWorld.renderedChunks
                + "/" + voxelWorld.numChunks, 0, 20);
        spriteBatch.end();
    }

    @Override
    public void resize (int width, int height) {
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }
    
    public void checkInput() {
        if(Gdx.input.isKeyPressed(Input.Keys.H)) {
           descendLimit = !descendLimit;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.L)) {
           enableWireframe = !enableWireframe;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.F)) {
            if(!fullscreen) { // set resolution to default and set fullscreen to true
                oldWidth = Gdx.graphics.getWidth();
                oldHeight = Gdx.graphics.getHeight();
                Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode().width, 
                        Gdx.graphics.getDesktopDisplayMode().height, true);
            } else {
                Gdx.graphics.setDisplayMode(oldWidth, oldHeight, false);
            }
            fullscreen = !fullscreen;
        }
    }
    
    void assetLoading() {
        ModelInstance instance = new ModelInstance(assets.get("tower/tower.g3db", Model.class));
        instances.add(instance);
        instance = new ModelInstance(assets.get("trees/tree1.g3db", Model.class));
        instance.transform.setToTranslation(0, 0, -12*UNITS_PER_METER);
        instances.add(instance);
        instance = new ModelInstance(assets.get("trees/tree2.g3db", Model.class));
        instance.transform.setToTranslation(-12*UNITS_PER_METER, 0, -6*UNITS_PER_METER);
        instances.add(instance);
        instance = new ModelInstance(assets.get("trees/tree3.g3db", Model.class));
        instance.transform.setToTranslation(-12*UNITS_PER_METER, 0, 6*UNITS_PER_METER);
        instances.add(instance);
        instance = new ModelInstance(assets.get("trees/tree4.g3db", Model.class));
        instance.transform.setToTranslation(0, 0, 12*UNITS_PER_METER);
        instances.add(instance);
        instance = new ModelInstance(assets.get("flags/flagRed.g3db", Model.class));
        instance.transform.setToTranslation(6*UNITS_PER_METER, 0, -3f*UNITS_PER_METER);
        instances.add(instance);
        instance = new ModelInstance(assets.get("flags/flagNone.g3db", Model.class));
        instance.transform.setToTranslation(6*UNITS_PER_METER, 0, 0);
        instances.add(instance);
        instance = new ModelInstance(assets.get("flags/flagBlue.g3db", Model.class));
        instance.transform.setToTranslation(6*UNITS_PER_METER, 0, 3f*UNITS_PER_METER);
        instances.add(instance);
        skySphere = new ModelInstance(assets.get("spacesphere/spacesphere.g3db", Model.class));
        skySphere.transform.setToScaling(60*UNITS_PER_METER, 60*UNITS_PER_METER, 60*UNITS_PER_METER);
        assetLoading = false;
    }
}
