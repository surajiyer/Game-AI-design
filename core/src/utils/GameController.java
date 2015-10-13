/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.IntIntMap;
import terrain.VoxelWorld;

/**
 * Used to control the camera.
 * @author S.S.Iyer
 */
public class GameController extends InputAdapter {
    
    VoxelWorld voxelWorld;
    public static boolean fullScreen = false;
    public static boolean descendLimit = true;
    public static boolean enableWireframe = false;
    public static boolean cursorCaught = true;
    public static boolean isFirstPerson = false;
    int oldWidth, oldHeight;
    private final IntIntMap keys = new IntIntMap();
    private final int TOGGLE_WIREFRAME = Keys.NUM_1;
    private final int TOGGLE_DESCEND_LIMIT = Keys.NUM_2;
    private final int TOGGLE_FIRST_PERSON = Keys.NUM_3;
    private final int TOGGLE_FULLSCREEN = Keys.F;
    private final int RELEASE_CURSOR = Keys.ESCAPE;

    public GameController(VoxelWorld voxelWorld) {
        this.voxelWorld = voxelWorld;
        Gdx.input.setCursorCatched(cursorCaught);
    }

    @Override
    public boolean keyDown (int keycode) {
        if(!keys.containsKey(keycode))
            keys.put(keycode, keycode);
        return false;
    }

    @Override
    public boolean keyUp (int keycode) {
        keys.remove(keycode, 0);
        return false;
    }
    
    @Override
    public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        if(!cursorCaught && !fullScreen) {
            cursorCaught = true;
            Gdx.input.setCursorCatched(cursorCaught);
        }
        return false;
    }
    
    public void update() {
        // Toggle catching the cursor
        if(keys.containsKey(RELEASE_CURSOR) && !fullScreen) {
            cursorCaught = false;
            Gdx.input.setCursorCatched(cursorCaught);
        }
        
        // Toggle catching the cursor
        if(keys.containsKey(TOGGLE_FIRST_PERSON)) {
            isFirstPerson = !isFirstPerson;
        }
        
        // Toggle wireframe output for VoxelWorld
        if (keys.containsKey(TOGGLE_WIREFRAME)) {
            enableWireframe = !enableWireframe;
        }
        
        // Toggle the depth limit for the camera
        if (keys.containsKey(TOGGLE_DESCEND_LIMIT)) {
            descendLimit = !descendLimit;
        }
        
        // Toggle full screen
        //if (keys.containsKey(TOGGLE_FULLSCREEN)) {
        if(Gdx.input.isKeyPressed(TOGGLE_FULLSCREEN)) {
            if(!fullScreen) { // set resolution to default and set fullScreen to true
                oldWidth = Gdx.graphics.getWidth();
                oldHeight = Gdx.graphics.getHeight();
                Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode().width, 
                        Gdx.graphics.getDesktopDisplayMode().height, true);
            } else {
                Gdx.graphics.setDisplayMode(oldWidth, oldHeight, false);
            }
            fullScreen = !fullScreen;
        }
    }
}
