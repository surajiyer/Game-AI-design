/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import terrain.Terrain;
import terrain.InfiniteGrid;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import terrain.SimplexNoise;
import terrain.TerrainGenerator;

/**
 *
 * @author S.S.Iyer
 */
public class Basic3DTest1 extends ApplicationAdapter {
    
    final static float UNITS_PER_METER = 96f;
    final float HUMAN_HEIGHT = 0.5f*UNITS_PER_METER;
    SpriteBatch batch;
    PerspectiveCamera camera;
    FPCameraController camController;
    boolean fullscreen = false;
    int oldWidth, oldHeight;
    ModelBatch modelBatch;
    ModelBuilder modelBuilder;
    ModelInstance skySphere;
    AssetManager assets;
    Array<ModelInstance> instances;
    Terrain terrain;
    Environment environment;
    boolean assetLoading;
    
    @Override
    public void create() 
    {       
        // Set up terrain batch to disply per frame
        modelBatch = new ModelBatch();
        instances = new Array<ModelInstance>();
        assets = new AssetManager();
        
        // Set up a 3D perspective camera
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(3*UNITS_PER_METER, 2f*UNITS_PER_METER, 0f);
        camera.lookAt(0,0,0);
        camera.near = 0.25f*UNITS_PER_METER;
        camera.far = 10f*UNITS_PER_METER;
        camera.up.set(Vector3.Y);
        camera.update();
        
        // Setup a camera controller to control the camera movements
        camController = new FPCameraController(camera);
        camController.setVelocity(1.5f*UNITS_PER_METER);
        Gdx.input.setInputProcessor(camController);
        
        // load a 3d Model
        assets.load("tower/tower.g3db", Model.class);
        assets.load("flags/flagBlue.g3db", Model.class);
        assets.load("flags/flagNone.g3db", Model.class);
        assets.load("flags/flagRed.g3db", Model.class);
        assets.load("trees/tree1.g3db", Model.class);
        assets.load("trees/tree2.g3db", Model.class);
        assets.load("trees/tree3.g3db", Model.class);
        assets.load("trees/tree4.g3db", Model.class);
        assets.load("spacesphere/spacesphere.g3db", Model.class);
        assetLoading = true;
        
        // create a 3d box terrain
        terrain = new InfiniteGrid(160, 160, 0.125f*UNITS_PER_METER);
        terrain.instance.transform.setToTranslation(camera.position.x, 0, camera.position.z);
        
        // create the surrounding environment
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.set(new ColorAttribute(ColorAttribute.Fog, 0.13f, 0.13f, 0.13f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
        
        //float[][] f = SimplexNoise.generateSimplexNoise(750, 450);
        float[][] f = SimplexNoise.generateOctavedSimplexNoise(750, 450, 6, 0.03f, 0.003f);
        SimplexNoise.createImage(f);
    }
    
    @Override
    public void render () {
        // Load assets once asset manager is done loading the asset
        if(assetLoading && assets.update()) {
            assetLoading();
        }
        
        // Update the camera controller
        camController.update();
        if(camera.position.y < terrain.getHeight()+HUMAN_HEIGHT) {
            camera.position.set(camera.position.x, 
                    terrain.getHeight()+HUMAN_HEIGHT, 
                    camera.position.z);
        }
        
        // Clear the color buffer and the depth buffer
        Gdx.gl.glClearColor(0.13f, 0.13f, 0.13f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        
        // Set new viewport size and full screen
        if(Gdx.input.isKeyPressed(Input.Keys.F)) {
            // set resolution to default and set fullscreen to true
            if(!fullscreen) {
                oldWidth = Gdx.graphics.getWidth();
                oldHeight = Gdx.graphics.getHeight();
                Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode().width, 
                        Gdx.graphics.getDesktopDisplayMode().height, true);
            } else {
                Gdx.graphics.setDisplayMode(oldWidth, oldHeight, false);
            }
            fullscreen = !fullscreen;
        }
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        // Move the grid with the camera for an infinte grid
        terrain.updatePos(camera.position);
        
        // Load the models
        modelBatch.begin(camera);
        modelBatch.render(instances, environment);
        modelBatch.render(terrain.instance);
//        if (skySphere != null) {
//            skySphere.transform.setToTranslation(camera.position);
//            modelBatch.render(skySphere);
//        }
        modelBatch.end();
    }
    
    @Override
    public void dispose () {
        modelBatch.dispose();
        instances.clear();
    }
    
    void assetLoading() {
        ModelInstance instance = new ModelInstance(assets.get("tower/tower.g3db", Model.class));
        instances.add(instance);
        instance = new ModelInstance(assets.get("flags/flagNone.g3db", Model.class));
        instance.transform.setToTranslation(Randomizer.randFloat(-5,5)*UNITS_PER_METER, 0, Randomizer.randFloat(-5,5)*UNITS_PER_METER);
        instances.add(instance);
        instance = new ModelInstance(assets.get("flags/flagNone.g3db", Model.class));
        instance.transform.setToTranslation(Randomizer.randFloat(-5,5)*UNITS_PER_METER, 0, Randomizer.randFloat(-5,5)*UNITS_PER_METER);
        instances.add(instance);
        instance = new ModelInstance(assets.get("flags/flagNone.g3db", Model.class));
        instance.transform.setToTranslation(Randomizer.randFloat(-5,5)*UNITS_PER_METER, 0, Randomizer.randFloat(-5,5)*UNITS_PER_METER);
        instances.add(instance);
        instance = new ModelInstance(assets.get("flags/flagNone.g3db", Model.class));
        instance.transform.setToTranslation(Randomizer.randFloat(-5,5)*UNITS_PER_METER, 0, Randomizer.randFloat(-5,5)*UNITS_PER_METER);
        instances.add(instance);
        instance = new ModelInstance(assets.get("flags/flagNone.g3db", Model.class));
        instance.transform.setToTranslation(Randomizer.randFloat(-5,5)*UNITS_PER_METER, 0, Randomizer.randFloat(-5,5)*UNITS_PER_METER);
        instances.add(instance);
        skySphere = new ModelInstance(assets.get("spacesphere/spacesphere.g3db", Model.class));
        skySphere.transform.setToScaling(10*UNITS_PER_METER, 10*UNITS_PER_METER, 10*UNITS_PER_METER);
        assetLoading = false;
    }
}
