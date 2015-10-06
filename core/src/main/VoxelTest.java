/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import utils.FPCameraController;
import utils.GameObject;
import terrain.SimplexNoise;
import terrain.VoxelWorld;
import utils.MultipleAnimationsController;
import utils.Player;
import utils.PlayerController;

/**
 *
 * @author S.S.Iyer
 */
public class VoxelTest extends ApplicationAdapter {
    
    final static float UNITS_PER_METER = 16;
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
    Array<GameObject> instances;
    int visibleCount;
    Player player;
    PlayerController playerController;
    MultipleAnimationsController playerAnimsController;

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
        camera.far = 360f;
        camera.up.set(Vector3.Y);
        camera.update();
        
        // Setup a camera controller to control the camera movements
        camController = new FPCameraController(camera);
        
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
        assets.load("characters/BlueWalk.g3db", Model.class);
        assetLoading = true;

        // create a voxel terrain
        voxelWorld = new VoxelWorld(Gdx.files.internal("tiles.png"), 20, 4, 20);
        SimplexNoise.generateHeightMap(voxelWorld, 0, 64, 10, 0.5f, 0.007f, 0.002f);
//        PerlinNoiseGenerator.generateVoxels(voxelWorld, 0, 64, 10);
//        float camX = voxelWorld.voxelsX / 2f;
//        float camZ = voxelWorld.voxelsZ / 2f;
//        float camY = voxelWorld.getHighest(camX, camZ) + 1.5f;
//        camera.position.set(camX, camY, camZ);
        
        // Load the player player
        GameObject instance = new GameObject(modelLoader.loadModel(Gdx.files.internal("characters/BlueWalk.g3dj")));
        instance.transform.scl(1/UNITS_PER_METER);
        player = new Player(instance, Vector3.Zero);
        playerController = new PlayerController(player, new Vector3(-4, 5, 0));
        playerController.setVelocity(16);
        
        // Set up an animation controller for the walking action of the player player
        playerAnimsController = new MultipleAnimationsController();
        playerAnimsController.animationSpeed = 2;
        playerAnimsController.addAnimations(new String[] {
            "Head|HeadAction",
            "Torso|TorsoAction",
            "Right Hand|Right HandAction",
            "Left Hand|Left HandAction",
            "Right Leg|Right LegAction",
            "Left Leg|Left LegAction"}, player.model);
        
        // Set the initial camera position
        camera.position.set(new Vector3().set(player.position).add(playerController.cameraOffset));
        
        // Setup all input sources
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(playerController);
        inputMultiplexer.addProcessor(camController);
        Gdx.input.setInputProcessor(inputMultiplexer);
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
            assetsLoading();
        }
        
        // Render all 3D stuff
        visibleCount = 0;
        modelBatch.begin(camera);
        DefaultShader.defaultCullFace = GL20.GL_FRONT;
        modelBatch.render(voxelWorld, environment);
        DefaultShader.defaultCullFace = GL20.GL_BACK;
        for(GameObject instance : instances) {
            if(isVisible(camera, instance)) {
                modelBatch.render(instance);
                visibleCount++;
            }
        }
        if(isVisible(camera, player.model)) {
            modelBatch.render(player.model);
            visibleCount++;
        }
        modelBatch.end();
        
        // Update the camera, the player and the player's animation
        camController.update();
        playerController.update(camera);
        playerAnimsController.update();
        player.update();

        // Render the 2D text
        spriteBatch.begin();
        font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond() 
                + ", #visible chunks: " + voxelWorld.renderedChunks
                + "/" + voxelWorld.numChunks + ", #visible objects: "+visibleCount, 10, 20);
        spriteBatch.end();
    }
    
    private Vector3 objectPosition = new Vector3();
    public boolean isVisible(final Camera cam, final GameObject instance) {
        instance.transform.getTranslation(objectPosition);
        objectPosition.add(instance.center);
        return cam.frustum.sphereInFrustum(objectPosition, instance.radius);
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
    
    public void checkInput() {
        if(Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
           voxelWorld.drawWireFrame = !voxelWorld.drawWireFrame;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
           descendLimit = !descendLimit;
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
        if(Gdx.input.isKeyPressed(Input.Keys.NUM_3)) {
           voxelWorld.useShader = !voxelWorld.useShader;
        }
    }
    
    void assetsLoading() {
        // Load the tower
        GameObject instance = new GameObject(assets.get("tower/tower.g3db", Model.class));
        instance.transform.scl(1/UNITS_PER_METER);
        instances.add(instance);
        
        // Load tree 1
        instance = new GameObject(assets.get("trees/tree1.g3db", Model.class));
        instance.transform.scl(1/UNITS_PER_METER);
        instance.transform.trn(0, 0, -12);
        instances.add(instance);
        
        // Load tree 2
        instance = new GameObject(assets.get("trees/tree2.g3db", Model.class));
        instance.transform.scl(1/UNITS_PER_METER);
        instance.transform.trn(-12, 0, -6);
        instances.add(instance);
        
        // Load tree 3
        instance = new GameObject(assets.get("trees/tree3.g3db", Model.class));
        instance.transform.scl(1/UNITS_PER_METER);
        instance.transform.trn(-12, 0, 6);
        instances.add(instance);
        
        // Load tree 4
        instance = new GameObject(assets.get("trees/tree4.g3db", Model.class));
        instance.transform.scl(1/UNITS_PER_METER);
        instance.transform.trn(0, 0, 12);
        instances.add(instance);
        
        // Load the red flag
        instance = new GameObject(assets.get("flags/flagRed.g3db", Model.class));
        instance.transform.scl(1/UNITS_PER_METER);
        instance.transform.trn(6, 0, -3f);
        instances.add(instance);
        
        // Load the uncaptured flag
        instance = new GameObject(assets.get("flags/flagNone.g3db", Model.class));
        instance.transform.scl(1/UNITS_PER_METER);
        instance.transform.trn(6, 0, 0);
        instances.add(instance);
        
        // Load the blur flag
        instance = new GameObject(assets.get("flags/flagBlue.g3db", Model.class));
        instance.transform.scl(1/UNITS_PER_METER);
        instance.transform.trn(6, 0, 3);
        instances.add(instance);
        
        // Load the sky box
        skySphere = new ModelInstance(assets.get("spacesphere/spacesphere.g3db", Model.class));
        skySphere.transform.scl(60, 60, 60);
        
        // Done loading assets
        assetLoading = false;
    }
}
