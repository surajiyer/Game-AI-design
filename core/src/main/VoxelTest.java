/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import AI.ReinforcementLearning;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
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
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import mechanics.GlobalState;
import terrain.SimplexNoise;
import terrain.VoxelWorld;
import utils.ConcreteGameObject;
import utils.EnvironmentCubeMap;
import mechanics.Player;
import mechanics.PlayerController;
import com.badlogic.gdx.utils.IntArray;
import mechanics.AIController;
import mechanics.Flag;
import mechanics.Flag.Occupant;
import static mechanics.GlobalState.UNITS_PER_METER;
import static mechanics.GlobalState.assetsManager;
import static mechanics.GlobalState.flagsManager;
import static mechanics.GlobalState.miniMap;
import static mechanics.GlobalState.scoreBoard;


/**
 *
 * @author S.S.Iyer
 */
public class VoxelTest extends ApplicationAdapter {
    final float[] FOG_COLOR = new float[] {0.13f, 0.13f, 0.13f, 1f};
    Environment environment;
    PerspectiveCamera camera;
    ModelBatch modelBatch;
    SpriteBatch spriteBatch;
    BitmapFont font;
    boolean assetLoading;
    EnvironmentCubeMap skyBox;
    VoxelWorld voxelWorld;
    Array<ConcreteGameObject> instances;
    Array<Player> players = new Array<>();
    Player player;
    PlayerController playerController;
    Player AI;
    ReinforcementLearning RL;
    Boolean step = true;
    Boolean evaluate = false;
    Boolean moveAI = false;
    IntArray path;
    AIController aiController;
    
    @Override
    public void create () {
        // Misc.
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        modelBatch = new ModelBatch();
        instances = new Array<>();
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
        GlobalState.assetsManager.load("tower/tower.g3db", Model.class);
        GlobalState.assetsManager.load("flags/flagBlue.g3db", Model.class);
        GlobalState.assetsManager.load("flags/flagNone.g3db", Model.class);
        GlobalState.assetsManager.load("flags/flagRed.g3db", Model.class);
        GlobalState.assetsManager.load("trees/tree1.g3db", Model.class);
        GlobalState.assetsManager.load("trees/tree2.g3db", Model.class);
        GlobalState.assetsManager.load("trees/tree3.g3db", Model.class);
        GlobalState.assetsManager.load("trees/tree4.g3db", Model.class);
        GlobalState.assetsManager.load("characters/BlueWalk.g3db", Model.class);
        assetLoading = true;
        
        // Create a voxel terrain
        voxelWorld = new VoxelWorld(new Texture(Gdx.files.internal("tiles.png")), 
                20, 4, 20,400, 4);
        SimplexNoise.generateHeightMap(voxelWorld, 0, 64, 10, 0.5f, 0.007f, 0.002f);
        voxelWorld.voxelTextures.bind(0);
//        PerlinNoiseGenerator.generateVoxels(model, 0, 64, 10);
//        float camX = model.voxelsX / 2f;
//        float camZ = model.voxelsZ / 2f;
//        float camY = model.getHeight(camX, camZ) + 1.5f;
//        camera.position.set(camX, camY, camZ);
        voxelWorld.setScale(UNITS_PER_METER);
        
        // Load the player
        player = new Player(modelLoader.loadModel(Gdx.files.internal("characters/BlueWalk.g3dj"))
                , Vector3.Zero, camera.direction);
        playerController = new PlayerController(player, camera, 
                new Vector3(0, 7*UNITS_PER_METER, -5*UNITS_PER_METER));
        playerController.setVelocity(22*UNITS_PER_METER);
        players.add(player);
        
        // Load the AI
        AI = new Player(modelLoader.loadModel(Gdx.files.internal("characters/BlueWalk.g3dj"))
                , Vector3.Zero, camera.direction);
        RL = new ReinforcementLearning(AI);
        aiController = new AIController(AI, voxelWorld);
        aiController.setVelocity(22*UNITS_PER_METER);
        players.add(AI);

        // Set the initial camera position
        camera.position.set(new Vector3().set(player.getPosition())
                .add(playerController.cameraOffset));
        
        // Setup all input sources
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(GlobalState.gameController);
        inputMultiplexer.addProcessor(playerController);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }
    
    @Override
    public void render () {
        // Clear the color buffer and the depth buffer
        Gdx.gl.glClearColor(FOG_COLOR[0], FOG_COLOR[1], FOG_COLOR[2], FOG_COLOR[3]);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        
        // Set viewport
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        // Load assetsManager once asset manager is done loading the asset
        if(assetLoading && GlobalState.assetsManager.update()) {
            assetsLoading(instances);
            assetLoading = false;
        }
        
        // Update the camera, the player and the player's animation
        GlobalState.gameController.update(playerController, camera);
        playerController.update();
        
        // Render all 3D stuff      
        GlobalState.visibleCount = 0;
        // Render the skybox
        skyBox.render(camera);
        modelBatch.begin(camera);
        //DefaultShader.defaultCullFace = GL20.GL_FRONT;
        DefaultShader.defaultCullFace = GL20.GL_NONE;
        // Render the voxel terrain
        if(voxelWorld.isVisible(camera)) {
            modelBatch.render(voxelWorld, environment);
        }
        modelBatch.flush();
        DefaultShader.defaultCullFace = GL20.GL_BACK;
        
        // Render all loaded models
//        for(ConcreteGameObject gameObject : instances) {
//            if(gameObject.isVisible(camera)) {
//                modelBatch.render(gameObject);
//                GlobalState.visibleCount++;
//            }
//        }
        if(step) {
            System.out.println("HI");
            path = RL.step();
            step = false;
            moveAI = true;
        }
        if(moveAI) {
            if(!aiController.update(path)) {
                evaluate = true;
                moveAI = false;
            }
        }
        if(evaluate) {
            System.out.println("evaluate");
            RL.evaluate();
            //step = true;
            evaluate = false;
        }
        
        // Render the player character
        if(player.isVisible(camera) && !GlobalState.isFirstPerson) {
            modelBatch.render(player);
            GlobalState.visibleCount++;
        }
        
        // Render the AI character
        if(AI.isVisible(camera)) {
            modelBatch.render(AI);
            GlobalState.visibleCount++;
        }
        
        // Update the scoreBoard board
        scoreBoard.updateScore();

        modelBatch.end();
        
        // Render the 2D text
        spriteBatch.begin();
        font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 
                camera.viewportHeight - 10);
        font.draw(spriteBatch, "#visible chunks: " + voxelWorld.renderedChunks 
                + "/" + voxelWorld.numChunks, 10, camera.viewportHeight - 25);
        font.draw(spriteBatch, "#visible objects: "+GlobalState.visibleCount, 10, camera.viewportHeight - 40);
        font.draw(spriteBatch, "First person mode (Num 3): "+GlobalState.isFirstPerson, 10, 
                camera.viewportHeight - 55);
        font.draw(spriteBatch, "Velocity (scroll): "+playerController.getVelocity()/UNITS_PER_METER, 10, 
                camera.viewportHeight - 70);
        font.draw(spriteBatch, "Camera position: "+camera.position, 10, 
                camera.viewportHeight - 85);
        font.draw(spriteBatch, "Camera direction: "+camera.direction, 10, 
                camera.viewportHeight - 100);
        
        // Draw minimap and HUD
        scoreBoard.draw(spriteBatch, camera, font);
        miniMap.draw(spriteBatch, camera, players);
        
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
        GlobalState.dispose();
    }
    
    public void assetsLoading(Array<ConcreteGameObject> instances) {
        // Load the tower
        ConcreteGameObject gameObject = new ConcreteGameObject(
                assetsManager.get("tower/tower.g3db", Model.class));
        instances.add(gameObject);
        
        // Load trees in the voxel world
        voxelWorld.loadTrees();
        
        // Load the flag models
        Flag.redFlag = new ModelInstance(assetsManager.get("flags/flagRed.g3db", Model.class));
        Flag.blueFlag = new ModelInstance(assetsManager.get("flags/flagBlue.g3db", Model.class));
        Flag.noneFlag = new ModelInstance(assetsManager.get("flags/flagNone.g3db", Model.class));
        
        // Load new flags into the game
        flagsManager.generateFlags();
    }
}
