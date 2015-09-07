/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

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
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Array;

/**
 *
 * @author S.S.Iyer
 */
public class Basic3DTest extends ApplicationAdapter {
    
    SpriteBatch batch;
    PerspectiveCamera camera;
    CameraInputController camController;
    boolean fullscreen = false;
    int oldWidth, oldHeight;
    ModelBatch modelBatch;
    ModelBuilder modelBuilder;
    AssetManager assets;
    Array<ModelInstance> instances;
    Model model;
    Environment environment;
    boolean assetLoading;
    
    @Override
    public void create() {
        // Set up model batch to disply per frame
        modelBatch = new ModelBatch();
        instances = new Array<ModelInstance>();
        assets = new AssetManager();
        
        // Set up a 3d perspective camera
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(50f, 50f, 50f);
        camera.lookAt(0,0,0);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();
        
        // Setup a camera controller to control the camera movements
        camController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(camController);
        
        // create a 3d box model
        modelBuilder = new ModelBuilder();
//        model = modelBuilder.createBox(5f, 5f, 5f, 
//            new Material(ColorAttribute.createDiffuse(Color.GREEN)),
//            Usage.Position | Usage.Normal);
//        instance = new ModelInstance(model);
        
        // load a 3d Model
        assets.load("tower.g3db", Model.class);
        assetLoading = true;
        
        // create the surrounding environment
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
    }
    
    @Override
    public void render () {
        // Load assets once asset manager is done loading the asset
        if(assetLoading && assets.update()) {
            instances.add(new ModelInstance(assets.get("tower.g3db", Model.class)));
            assetLoading = false;
        }
        
        // Update the camera controller
        camController.update();
        
        // Clear the color buffer and the depth buffer
        Gdx.gl.glClearColor(1, 1, 1, 1);
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
        
        // Load the models
        modelBatch.begin(camera);
        modelBatch.render(instances, environment);
        modelBatch.end();
    }
    
    @Override
    public void dispose () {
        modelBatch.dispose();
        instances.clear();
        //model.dispose();
    }
}
