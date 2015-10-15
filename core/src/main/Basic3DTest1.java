/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import mechanics.BobController;
import terrain.InfiniteGrid;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import java.util.concurrent.TimeUnit;
import utils.Drawables;
import mechanics.Flag;
import mechanics.FlagList;
import mechanics.Score;

/**
 *
 * @author S.S.Iyer
 */
public class Basic3DTest1 extends ApplicationAdapter {
    
    final static float UNITS_PER_METER = 16f;
    final float HUMAN_HEIGHT = 3f*UNITS_PER_METER;
    final float[] FOG_COLOR = new float[] {0.13f, 0.13f, 0.13f, 1f};
    ModelBatch modelBatch;
    SpriteBatch spriteBatch;
    PerspectiveCamera camera;
    BobController camController;
    boolean fullscreen = false;
    boolean descendLimit = true;
    int oldWidth, oldHeight;
    ModelInstance skySphere;
    AssetManager assets;
    boolean assetLoading;
    Array<ModelInstance> instances;
    Environment environment;
    InfiniteGrid grid;
    Score score;
    BitmapFont font;
    long startTime;
    long elapsedTime;
    FlagList flagList;
    ModelInstance bobber, bobberBox;
    ShapeRenderer shapeRenderer;
    
    @Override
    public void create() {
        // Set up terrain batch to disply per frame
        shapeRenderer = new ShapeRenderer();
        modelBatch = new ModelBatch();
        spriteBatch = new SpriteBatch();
        instances = new Array<>();
        assets = new AssetManager();
        score = new Score();
        font = new BitmapFont();
        startTime = System.currentTimeMillis();
        elapsedTime = 0;
        // Set up a 3D perspective camera
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(18f*UNITS_PER_METER, 12f*UNITS_PER_METER, 0f);
        camera.lookAt(0,0,0);
        camera.near = 1.5f*UNITS_PER_METER;
        camera.far = 60f*UNITS_PER_METER;
        camera.up.set(Vector3.Y);
        camera.update();
        flagList = new FlagList(5);
        flagList.setOccupant(0,"AI");
        flagList.setOccupant(1,"AI");
        flagList.setOccupant(2,"Player");
        flagList.setOccupant(3,"Player");
        flagList.setOccupant(4,"Player");
        
        // Setup a camera controller to control the camera movements
        camController = new BobController(camera);
//        camController.setVelocity(9f*UNITS_PER_METER);
        camController = new BobController(camera);
        camController.setVelocity(9f*UNITS_PER_METER);
        Gdx.input.setInputProcessor(camController);
        
        // load a 3d Model  
        assets.load("trees/tree1.g3db", Model.class);
        assets.load("flags/flagNone.g3db", Model.class);
        assetLoading = true;
        
        // create a grid
        grid = new InfiniteGrid(160, 160, UNITS_PER_METER);
        grid.instance.transform.setToTranslation(camera.position.x, 0, camera.position.z);
        
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
        
        // Update the camera controller
        camController.update();
        if(descendLimit) {
            if(camera.position.y < grid.getHeight()+HUMAN_HEIGHT) {
                camera.position.set(camera.position.x, 
                        grid.getHeight()+HUMAN_HEIGHT, 
                        camera.position.z);
            }
        }
       
        // Time
        elapsedTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);
        score.updateScore(elapsedTime, flagList);
        
        // Render everything
        modelBatch.begin(camera);
        modelBatch.render(instances, environment);
        if(bobber != null) {
            bobber.transform.setToTranslation(camera.position.x, 0, camera.position.z);
            if(bobberBox != null)
                bobberBox.transform.setToTranslation(bobber.transform.getTranslation(new Vector3()));
            modelBatch.render(bobber);
            modelBatch.render(bobberBox);
        }
        
        Flag[] flags = flagList.getList();
        for(int i = 0; i < flags.length; i++) {
            ModelInstance temp = flags[i].getFlagBox();
            ModelInstance colTemp = flags[i].getCaptureBox();
           if(temp != null) {
               int[] coor = flagList.getCoordinates(i);
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
        //grid.updatePos(camera); // Move the grid with the camera for an infinte grid
        modelBatch.render(grid.instance);
        modelBatch.end();
        
        //spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        font.draw(spriteBatch, "Player Score: " + score.getPS(), 15, 710);
        font.draw(spriteBatch, "AI Score: " + score.getCS(), 15, 690);
        font.draw(spriteBatch, "Time elapsed: " + String.valueOf(elapsedTime) + "s", 15, 670);
        font.draw(spriteBatch, "Flags", 15, 640);
        font.draw(spriteBatch, "1: " + flagList.getOccupant(0), 15, 620);
        font.draw(spriteBatch, "2: " + flagList.getOccupant(1), 15, 600);
        font.draw(spriteBatch, "3: " + flagList.getOccupant(2), 15, 580);
        font.draw(spriteBatch, "4: " + flagList.getOccupant(3), 15, 560);
        font.draw(spriteBatch, "5: " + flagList.getOccupant(4), 15, 540);
        spriteBatch.end();
        
    }
    
    @Override
    public void dispose () {
        modelBatch.dispose();
        instances.clear();
    }
    
    public void checkInput() {
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
    }
    
    void assetLoading() {
        ModelInstance instance;
        Flag[] flags = flagList.getList();
        for(int i = 0; i < flags.length; i++) {
            instance = new ModelInstance(assets.get("flags/flagNone.g3db", Model.class));
            int[] coor = flagList.getCoordinates(i);
            flags[i].setFlagBox(instance);
            flags[i].setCaptureBox(instance);
            instance.transform.setToTranslation(
                    coor[0]*UNITS_PER_METER, 
                    coor[1]*UNITS_PER_METER, 
                    coor[2]*UNITS_PER_METER);
            instances.add(instance);
        }
        
        bobber = new ModelInstance(assets.get("trees/tree1.g3db", Model.class));
        bobber.transform.setToTranslation(camera.position.x, 0, camera.position.z);
        bobberBox = Drawables.drawBoundingBox(bobber, true);
        assetLoading = false;

    }
    
    public void addFlag(ModelInstance flag) {
        instances.add(flag);
    }
    
}
