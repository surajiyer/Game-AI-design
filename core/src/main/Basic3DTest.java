/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

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
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import terrain.HeightMap;
import terrain.SimplexTerrain;

/**
 *
 * @author S.S.Iyer
 */
public class Basic3DTest extends ApplicationAdapter {
    
    final static float UNITS_PER_METER = 16f;
    final float HUMAN_HEIGHT = 3f*UNITS_PER_METER;
    final float[] FOG_COLOR = new float[] {0.13f, 0.13f, 0.13f, 1f};
    SpriteBatch batch;
    PerspectiveCamera camera;
    FPCameraController camController;
    boolean fullscreen = false;
    boolean descendLimit = true;
    boolean enableWireframe = false;
    int oldWidth, oldHeight;
    ModelBatch modelBatch;
    ModelInstance skySphere;
    AssetManager assets;
    boolean assetLoading;
    Array<ModelInstance> instances;
    Environment environment;
    InfiniteGrid grid;
    SimplexTerrain terra;
    RenderContext renderContext;
    
    @Override
    public void create() {
        // Enable blending
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        // Enable depth testing
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthFunc(GL20.GL_LESS);
        
        // Set up terrain batch to disply per frame
        modelBatch = new ModelBatch();
        instances = new Array<>();
        assets = new AssetManager();
        
        // Set up a 3D perspective camera
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(18f*UNITS_PER_METER, 12f*UNITS_PER_METER, 0f);
        camera.lookAt(0,0,0);
        camera.near = 1.5f*UNITS_PER_METER;
        camera.far = 300f*UNITS_PER_METER;
        camera.up.set(Vector3.Y);
        camera.update();
        
        // Setup a camera controller to control the camera movements
        camController = new FPCameraController(camera);
        camController.setVelocity(9f*UNITS_PER_METER);
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
        grid = new InfiniteGrid(160, 160, UNITS_PER_METER);
        grid.instance.transform.setToTranslation(camera.position.x, 0, camera.position.z);
        
        // create a terrain shader program
        HeightMap hm = new HeightMap(Gdx.files.internal("heightmaps/Heightmap192x192.png"));
        terra = new SimplexTerrain(hm, 1f, 100f, Gdx.files.internal("heightmaps/heightmap_lookup.png"));
        terra.shader.init();
        renderContext = new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.WEIGHTED, 1));
        
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
        
        // Render terrain
        if(enableWireframe)
            terra.primitiveType = GL20.GL_LINE_STRIP;
        else
            terra.primitiveType = GL20.GL_TRIANGLE_STRIP;
        
        // Move the grid with the camera for an infinte grid
        grid.updatePos(camera.position);
        
        // Load the models
        modelBatch.begin(camera);
        modelBatch.render(instances, environment);
        modelBatch.render(grid.instance);
//        if (skySphere != null) {
//            skySphere.transform.setToTranslation(camera.position);
//            modelBatch.render(skySphere);
//        }
        modelBatch.end();
        
        terra.lookup.bind();
        terra.shader.begin(camera, renderContext);
        terra.shader.render(terra);
        terra.shader.end();
    }
    
    @Override
    public void dispose () {
        modelBatch.dispose();
        instances.clear();
        terra.dispose();
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
