/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import terrain.Chunk;
import utils.Cube;
import terrain.SimplexNoise;
import terrain.VoxelWorld;
import terrain.Chunk.CubeType;
import utils.ConcreteGameObject;
import utils.EnvironmentCubeMap;
import utils.GameController;
import mechanics.Player;
import mechanics.PlayerController;

/**
 *
 * @author S.S.Iyer
 */
public class VoxelTest extends ApplicationAdapter {
    
    public final static int UNITS_PER_METER = 16;
    final float HUMAN_HEIGHT = 3f*UNITS_PER_METER;
    final float[] FOG_COLOR = new float[] {0.13f, 0.13f, 0.13f, 1f};
    SpriteBatch spriteBatch;
    BitmapFont font;
    ModelBatch modelBatch;
    PerspectiveCamera camera;
    Environment environment;
    VoxelWorld voxelWorld;
    EnvironmentCubeMap skyBox;
    GameController gameController;
    AssetManager assets;
    boolean assetLoading;
    Array<ConcreteGameObject> instances;
    int visibleCount;
    Player player;
    PlayerController playerController;
    Cube cube;
    Texture texture;
    Chunk chunk;
     
    @Override
    public void create () {
        // Misc.
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        modelBatch = new ModelBatch();
        instances = new Array<>();
        assets = new AssetManager();
        ModelLoader modelLoader = new G3dModelLoader(new JsonReader());
        
        // create the surrounding environment
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.set(new ColorAttribute(ColorAttribute.Fog, FOG_COLOR[0], 
                FOG_COLOR[1], FOG_COLOR[2], FOG_COLOR[3]));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
        
        // Set up a 3D perspective camera
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 1.5f;
        camera.far = 360f*UNITS_PER_METER;
        camera.up.set(Vector3.Y);
        camera.update();
        
        // Create a skybox
        skyBox = new EnvironmentCubeMap(new Pixmap(Gdx.files.internal("skybox.png")));
        
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
        assets.load("skyDome/skydome.g3db", Model.class);
        assets.load("characters/BlueWalk.g3db", Model.class);
        assetLoading = true;

        // create a voxel terrain
        texture = new Texture(Gdx.files.internal("tiles.png"));
        voxelWorld = new VoxelWorld(Gdx.files.internal("tiles.png"), 20, 4, 20);
        SimplexNoise.generateHeightMap(voxelWorld, 0, 64, 10, 0.5f, 0.007f, 0.002f);
//        PerlinNoiseGenerator.generateVoxels(voxelWorld, 0, 64, 10);
//        float camX = voxelWorld.voxelsX / 2f;
//        float camZ = voxelWorld.voxelsZ / 2f;
//        float camY = voxelWorld.getHighest(camX, camZ) + 1.5f;
//        camera.position.set(camX, camY, camZ);
        
        // Load the player player
        player = new Player(modelLoader.loadModel(Gdx.files.internal("characters/BlueWalk.g3dj"))
                , Vector3.Zero, camera.direction);
        playerController = new PlayerController(player, camera, 
                new Vector3(0, 3*UNITS_PER_METER, -5*UNITS_PER_METER));
        playerController.setVelocity(24*UNITS_PER_METER);
        
        // Set the initial camera position
        camera.position.set(new Vector3().set(player.getPosition())
                .add(playerController.cameraOffset));
        
        // Setup all input sources
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        gameController = new GameController(voxelWorld);
        inputMultiplexer.addProcessor(gameController);
        inputMultiplexer.addProcessor(playerController);
        Gdx.input.setInputProcessor(inputMultiplexer);
        
        // Create a cube
        cube = new Cube(CubeType.GRASS);
        cube.setScale(UNITS_PER_METER);
        chunk = new Chunk();
    }
    
    @Override
    public void render () {
        // Clear the color buffer and the depth buffer
        Gdx.gl.glClearColor(FOG_COLOR[0], FOG_COLOR[1], FOG_COLOR[2], FOG_COLOR[3]);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        
        // Set viewport
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        // Load assets once asset manager is done loading the asset
        if(assetLoading && assets.update()) {
            assetsLoading();
        }
        
        // Update the camera, the player and the player's animation
        gameController.update();
        playerController.update();
        
        // Render all 3D stuff        
        visibleCount = 0;
        // Render the skybox
        skyBox.render(camera);
        modelBatch.begin(camera);
        DefaultShader.defaultCullFace = GL20.GL_FRONT;
        // Render the voxel terrain
        modelBatch.render(voxelWorld, environment);
        modelBatch.flush();
        DefaultShader.defaultCullFace = GL20.GL_BACK;
        // Render cube
        if(cube.isVisible(camera)) {
            texture.bind();
            modelBatch.render(cube);
            visibleCount++;
        }
//        if(chunk.isVisible(camera)) {
//            modelBatch.render(chunk);
//            visibleCount++;
//        }
        // Render all loaded models
        for(ConcreteGameObject gameObject : instances) {
            if(gameObject.isVisible(camera)) {
                modelBatch.render(gameObject);
                visibleCount++;
            }
        }
        // Render the player character
        if(player.isVisible(camera)) {
            modelBatch.render(player);
            visibleCount++;
        }
        modelBatch.end();
        
        // Render the 2D text
        spriteBatch.begin();
        font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 
                camera.viewportHeight - 10);
        font.draw(spriteBatch, "#visible chunks: " + voxelWorld.renderedChunks 
                + "/" + voxelWorld.numChunks, 10, camera.viewportHeight - 25);
        font.draw(spriteBatch, "#visible objects: "+visibleCount, 10, camera.viewportHeight - 40);
        font.draw(spriteBatch, "First person mode (Num 4): "+GameController.isFirstPerson, 10, 
                camera.viewportHeight - 55);
        font.draw(spriteBatch, "Velocity (scroll): "+playerController.getVelocity()/UNITS_PER_METER, 10, 
                camera.viewportHeight - 70);
        spriteBatch.end();
    }

    @Override
    public void resize (int width, int height) {
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }
    
    @Override
    public void dispose () {
        modelBatch.dispose();
        instances.clear();
        assets.dispose();
    }
    
    Vector3 tmp = new Vector3();
    
    void assetsLoading() {
        // Load the tower
        ConcreteGameObject gameObject = new ConcreteGameObject(assets.get("tower/tower.g3db", Model.class));
        instances.add(gameObject);
        
        // Load tree 1
        gameObject = new ConcreteGameObject(assets.get("trees/tree1.g3db", Model.class));
        gameObject.setPosition(tmp.set(0, 0, -12*UNITS_PER_METER));
        instances.add(gameObject);
        
        // Load tree 2
        gameObject = new ConcreteGameObject(assets.get("trees/tree2.g3db", Model.class));
        gameObject.setPosition(tmp.set(-12*UNITS_PER_METER, 0, -6*UNITS_PER_METER));
        instances.add(gameObject);
        
        // Load tree 3
        gameObject = new ConcreteGameObject(assets.get("trees/tree3.g3db", Model.class));
        gameObject.setPosition(tmp.set(-12*UNITS_PER_METER, 0, 6*UNITS_PER_METER));
        instances.add(gameObject);
        
        // Load tree 4
        gameObject = new ConcreteGameObject(assets.get("trees/tree4.g3db", Model.class));
        gameObject.setPosition(tmp.set(0, 0, 12*UNITS_PER_METER));
        instances.add(gameObject);
        
        // Load the red flag
        gameObject = new ConcreteGameObject(assets.get("flags/flagRed.g3db", Model.class));
        gameObject.setPosition(tmp.set(6*UNITS_PER_METER, 0, -3*UNITS_PER_METER));
        instances.add(gameObject);
        
        // Load the uncaptured flag
        gameObject = new ConcreteGameObject(assets.get("flags/flagNone.g3db", Model.class));
        gameObject.setPosition(tmp.set(6*UNITS_PER_METER, 0, 0));
        instances.add(gameObject);
        
        // Load the blur flag
        gameObject = new ConcreteGameObject(assets.get("flags/flagBlue.g3db", Model.class));
        gameObject.setPosition(tmp.set(6*UNITS_PER_METER, 0, 3*UNITS_PER_METER));
        instances.add(gameObject);
        
        // Done loading assets
        assetLoading = false;
    }
}
