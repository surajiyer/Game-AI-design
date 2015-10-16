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
import mechanics.GlobalState;
import terrain.Chunk;
import utils.Cube;
import terrain.SimplexNoise;
import terrain.VoxelWorld;
import utils.ConcreteGameObject;
import utils.EnvironmentCubeMap;
import utils.GameController;
import mechanics.Player;
import mechanics.PlayerController;
import mechanics.Score;
import java.util.concurrent.TimeUnit;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.IntArray;
import mechanics.AIController;
import terrain.TreeList;


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
    EnvironmentCubeMap skyBox;
    Array<ConcreteGameObject> instances;
    Player player;
    PlayerController playerController;
    Cube cube;
    Chunk chunk;
     
    public static final int miniWidth = 800;
    public static final int miniHeight = 480;
    public static final int miniScale = 4;
    public static final int markerSize = 20;
    public static final int miniMapLeft = 0;
    public static final int miniMapRight = 200;
    public static final int miniMapTop = 480;
    public static final int miniMapBottom = 280;
    private Texture playerMarker;
    private Texture heightMap;
    private Sprite mapSprite;
    private Texture flagMarkerBlue;
    private Texture flagMarkerRed;
    private Texture flagMarkerGrey;
    private Texture hudMap;
    private Texture hudScore;
    private Texture flagTexture;
    private Sprite hudMapSprite;
    Score score;
    long startTime;
    long elapsedTime; 
    
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
        GlobalState.assetsManager.load("spacesphere/spacesphere.g3db", Model.class);
        GlobalState.assetsManager.load("skyDome/skydome.g3db", Model.class);
        GlobalState.assetsManager.load("characters/BlueWalk.g3db", Model.class);
        GlobalState.assetLoading = true;
        
        // Create a voxel terrain
        GlobalState.voxelTextures = new Texture(Gdx.files.internal("tiles.png"));
        GlobalState.voxelWorld = new VoxelWorld(20, 4, 20);
        SimplexNoise.generateHeightMap(GlobalState.voxelWorld, 0, 64, 10, 0.5f, 0.007f, 0.002f);
        GlobalState.voxelTextures.bind(0);
//        PerlinNoiseGenerator.generateVoxels(model, 0, 64, 10);
//        float camX = model.voxelsX / 2f;
//        float camZ = model.voxelsZ / 2f;
//        float camY = model.getHeight(camX, camZ) + 1.5f;
//        camera.position.set(camX, camY, camZ);
        GlobalState.voxelWorld.setScale(UNITS_PER_METER);
        
        // Pass the voxelworld to the trees 
        GlobalState.treeList = new TreeList(400);
        
        // Load the player
        player = new Player(modelLoader.loadModel(Gdx.files.internal("characters/BlueWalk.g3dj"))
                , Vector3.Zero, camera.direction);
        
        
        playerController = new PlayerController(player, camera, 
                new Vector3(0, 7*UNITS_PER_METER, -5*UNITS_PER_METER));
        playerController.setVelocity(22*UNITS_PER_METER);
        
        // Load the AI
        GlobalState.AI = new Player(modelLoader.loadModel(Gdx.files.internal("characters/BlueWalk.g3dj"))
                , Vector3.Zero, camera.direction);
        RL = new ReinforcementLearning();
        aiController = new AIController(GlobalState.AI);
        aiController.setVelocity(22*UNITS_PER_METER);
        
        // Set the initial camera position
        camera.position.set(new Vector3().set(player.getPosition())
                .add(playerController.cameraOffset));
        
        // Setup a minimap camera
        //miniMapCam = new OrthographicCamera(miniWidth, miniHeight);
        //miniMapCam.zoom = miniScale;
        playerMarker = new Texture(Gdx.files.internal("markers/playerdot.png"));
        heightMap = new Texture(Gdx.files.internal("simplexmap.png"));
        mapSprite = new Sprite(heightMap);
        flagMarkerBlue = new Texture(Gdx.files.internal("markers/blue.png"));
        flagMarkerRed = new Texture(Gdx.files.internal("markers/red.png"));
        flagMarkerGrey = new Texture(Gdx.files.internal("markers/grey.png"));
        hudMap = new Texture(Gdx.files.internal("markers/hudMap.png"));
        hudMapSprite = new Sprite(hudMap);
        hudScore = new Texture(Gdx.files.internal("markers/hudScore.png"));
        score = new Score();    
        
        // Time
        startTime = System.currentTimeMillis();
        elapsedTime = 0;      
        
        // Setup all input sources
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        GlobalState.gameController = new GameController();
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
        if(GlobalState.assetLoading && GlobalState.assetsManager.update()) {
            GlobalState.assetsLoading(instances);
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
        if(GlobalState.voxelWorld.isVisible(camera)) {
            modelBatch.render(GlobalState.voxelWorld, environment);
        }
        modelBatch.flush();
        DefaultShader.defaultCullFace = GL20.GL_BACK;
        
        // Render all loaded models
        for(ConcreteGameObject gameObject : instances) {
            if(gameObject.isVisible(camera)) {
                modelBatch.render(gameObject);
                GlobalState.visibleCount++;
            }
        }
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
        if(GlobalState.AI.isVisible(camera)) {
            modelBatch.render(GlobalState.AI);
            GlobalState.visibleCount++;
        }
        
        // Time
        elapsedTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);
        score.updateScore(elapsedTime, GlobalState.flagsManager);

        modelBatch.end();
        
        // Render the 2D text
        spriteBatch.begin();
        font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 
                camera.viewportHeight - 10);
        font.draw(spriteBatch, "#visible chunks: " + GlobalState.voxelWorld.renderedChunks 
                + "/" + GlobalState.voxelWorld.numChunks, 10, camera.viewportHeight - 25);
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
        spriteBatch.draw(hudMap, hudMapSprite.getX()-6, hudMapSprite.getY()-6);
        spriteBatch.draw(hudScore, camera.viewportWidth/2 -250, camera.viewportHeight - 70);
        spriteBatch.draw(heightMap, mapSprite.getX(), mapSprite.getY());    
        
        font.setColor(0,0,0,1);
        font.draw(spriteBatch, "" + score.getPS(), camera.viewportWidth/2 - 90, camera.viewportHeight - 20);
        font.draw(spriteBatch, "" + score.getCS(), camera.viewportWidth/2 + 80, camera.viewportHeight - 20);
        font.draw(spriteBatch, "" + String.valueOf(elapsedTime), camera.viewportWidth/2 -3, camera.viewportHeight - 20);
        
        Vector3 tmp = new Vector3();
        for (int i = 0; i < 5; i++) {
            switch (GlobalState.flagsManager.getOccupant(i)) {
                case AI:
                    flagTexture = flagMarkerRed;
                    break;
                case PLAYER:
                    flagTexture = flagMarkerBlue;
                    break;
                case NONE:  
                    flagTexture = flagMarkerGrey;
                    break;
            }
            tmp.set(GlobalState.flagsManager.getFlagPosition(i));
            spriteBatch.draw(flagTexture, tmp.z, tmp.x);
        }
        spriteBatch.draw(playerMarker, player.getPosition().z/UNITS_PER_METER, 
                player.getPosition().x/UNITS_PER_METER);
        spriteBatch.draw(playerMarker, GlobalState.AI.getPosition().z/UNITS_PER_METER, GlobalState.AI.getPosition().x/UNITS_PER_METER);
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
}
