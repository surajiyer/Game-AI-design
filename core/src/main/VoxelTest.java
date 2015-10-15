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
import mechanics.Flag;
import mechanics.FlagList;
import mechanics.Score;
import java.util.concurrent.TimeUnit;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.OrthographicCamera;
import utils.GameInfo;


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
    
    @Override
    public void create () {
        // Misc.
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        modelBatch = new ModelBatch();
        instances = new Array<>();
        GlobalState.assets = new AssetManager();
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
        GlobalState.assets.load("tower/tower.g3db", Model.class);
        GlobalState.assets.load("flags/flagBlue.g3db", Model.class);
        GlobalState.assets.load("flags/flagNone.g3db", Model.class);
        GlobalState.assets.load("flags/flagRed.g3db", Model.class);
        GlobalState.assets.load("trees/tree1.g3db", Model.class);
        GlobalState.assets.load("trees/tree2.g3db", Model.class);
        GlobalState.assets.load("trees/tree3.g3db", Model.class);
        GlobalState.assets.load("trees/tree4.g3db", Model.class);
        GlobalState.assets.load("spacesphere/spacesphere.g3db", Model.class);
        GlobalState.assets.load("skyDome/skydome.g3db", Model.class);
        GlobalState.assets.load("characters/BlueWalk.g3db", Model.class);
        GlobalState.assetLoading = true;

        // create a voxel terrain
        Cube.texture = new Texture(Gdx.files.internal("tiles.png"));
        Cube.texture.bind(0);
        voxelWorld = new VoxelWorld(Cube.texture, 20, 4, 20);
        SimplexNoise.generateHeightMap(voxelWorld, 0, 64, 10, 0.5f, 0.007f, 0.002f);
//        PerlinNoiseGenerator.generateVoxels(model, 0, 64, 10);
//        float camX = model.voxelsX / 2f;
//        float camZ = model.voxelsZ / 2f;
//        float camY = model.getHeight(camX, camZ) + 1.5f;
//        camera.position.set(camX, camY, camZ);
        
        // Load the player player
        player = new Player(modelLoader.loadModel(Gdx.files.internal("characters/BlueWalk.g3dj"))
                , Vector3.Zero, camera.direction);
        playerController = new PlayerController(player, camera, 
                new Vector3(0, 7*UNITS_PER_METER, -5*UNITS_PER_METER));
        playerController.setVelocity(22*UNITS_PER_METER);
        
        // Set the initial camera position
        camera.position.set(new Vector3().set(player.getPosition())
                .add(playerController.cameraOffset));
        
        //flags
        GameInfo.flagList = new FlagList(5);
        GameInfo.flagList.setOccupant(0,"AI");
        GameInfo.flagList.setOccupant(1,"AI");
        GameInfo.flagList.setOccupant(2,"Player");
        GameInfo.flagList.setOccupant(3,"Player");
        GameInfo.flagList.setOccupant(4,"Player");
        
         //setup a minimap camera
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
        
        //time
        startTime = System.currentTimeMillis();
        elapsedTime = 0;      
        
        
        // Setup all input sources
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        GlobalState.gameController = new GameController(voxelWorld);
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
        
        // Load assets once asset manager is done loading the asset
        if(GlobalState.assetLoading && GlobalState.assets.update()) {
            GlobalState.assetsLoading(instances);
        }
        
        // Update the camera, the player and the player's animation
        GlobalState.gameController.update();
        playerController.update();
        
        // Render all 3D stuff      
        GlobalState.visibleCount = 0;
        // Render the skybox
        skyBox.render(camera);
        modelBatch.begin(camera);
        DefaultShader.defaultCullFace = GL20.GL_FRONT;
        // Render the voxel terrain
        if(voxelWorld.isVisible(camera)) {
            modelBatch.render(voxelWorld, environment);
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
        
        // Render the player character
        if(player.isVisible(camera)) {
            modelBatch.render(player);
            GlobalState.visibleCount++;
        }
        
        // Time
        elapsedTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);
        score.updateScore(elapsedTime, GameInfo.flagList);

        //flags
        Flag[] flags = GameInfo.flagList.getList();
        for(int i = 0; i < flags.length; i++) {
            ModelInstance temp = flags[i].getFlagBox();
            ModelInstance colTemp = flags[i].getCaptureBox();
           if(temp != null) {
               int[] coor = GameInfo.flagList.getCoordinates(i);
               temp.transform.setToTranslation(coor[0]*UNITS_PER_METER, 
                    coor[1]*UNITS_PER_METER, 
                    coor[2]*UNITS_PER_METER);
                              colTemp.transform.setToTranslation(coor[0]*UNITS_PER_METER, 
                    coor[1]*UNITS_PER_METER, 
                    coor[2]*UNITS_PER_METER);
               modelBatch.render(temp);
               modelBatch.render(colTemp);
           }
        }

        modelBatch.end();
        
        // Render the 2D text
        spriteBatch.begin();
        font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 
                camera.viewportHeight - 10);
        font.draw(spriteBatch, "#visible chunks: " + voxelWorld.renderedChunks 
                + "/" + voxelWorld.numChunks, 10, camera.viewportHeight - 25);
        font.draw(spriteBatch, "#visible objects: "+GlobalState.visibleCount, 10, camera.viewportHeight - 40);
        font.draw(spriteBatch, "First person mode (Num 4): "+GameController.isFirstPerson, 10, 
                camera.viewportHeight - 55);
        font.draw(spriteBatch, "Velocity (scroll): "+playerController.getVelocity()/UNITS_PER_METER, 10, 
                camera.viewportHeight - 70);
        
        //draw minimap and HUD
        spriteBatch.draw(hudMap, hudMapSprite.getX()-6, hudMapSprite.getY()-6);
        spriteBatch.draw(hudScore, camera.viewportWidth/2 -250, camera.viewportHeight - 70);
        spriteBatch.draw(heightMap, mapSprite.getX(), mapSprite.getY());    
        
        font.setColor(0,0,0,1);
        font.draw(spriteBatch, "" + score.getPS(), camera.viewportWidth/2 - 90, camera.viewportHeight - 20);
        font.draw(spriteBatch, "" + score.getCS(), camera.viewportWidth/2 + 80, camera.viewportHeight - 20);
        font.draw(spriteBatch, "" + String.valueOf(elapsedTime), camera.viewportWidth/2 -3, camera.viewportHeight - 20);
        
        for (int i = 0; i < 5; i++) {
            if ((GameInfo.flagList.getOccupant(i)).equals("AI")) {
                flagTexture = flagMarkerRed;
            }
            else if ((GameInfo.flagList.getOccupant(i)).equals("Player")) {
                flagTexture = flagMarkerBlue;
            }
            else if ((GameInfo.flagList.getOccupant(i)).equals("None")) {
                flagTexture = flagMarkerGrey;
            }
        
            spriteBatch.draw(flagTexture, GameInfo.flagList.getCoordinates(i)[2], GameInfo.flagList.getCoordinates(i)[0]);
        }
        
        spriteBatch.draw(playerMarker, player.getPosition().z/16, player.getPosition().x/16);        
    
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
