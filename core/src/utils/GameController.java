/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;
import mechanics.GlobalState;
import mechanics.PlayerController;

/**
 * Used to control the camera.
 * @author S.S.Iyer
 */
public class GameController extends InputAdapter {
    
    private final IntIntMap keys = new IntIntMap();
    private final int TOGGLE_WIREFRAME = Keys.NUM_1;
    private final int TOGGLE_DESCEND_LIMIT = Keys.NUM_2;
    private final int TOGGLE_FIRST_PERSON = Keys.NUM_3;
    private final int TOGGLE_FULLSCREEN = Keys.F;
    private final int RELEASE_CURSOR = Keys.ESCAPE;
    private final Vector3 tmp = new Vector3();

    public GameController() {
        Gdx.input.setCursorCatched(GlobalState.cursorCaught);
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
        if(!GlobalState.cursorCaught && !GlobalState.fullScreen) {
            GlobalState.cursorCaught = true;
            Gdx.input.setCursorCatched(GlobalState.cursorCaught);
        }
        return false;
    }
    
    public void update(PlayerController playerController, Camera camera) {
        // Toggle catching the cursor
        if(keys.containsKey(RELEASE_CURSOR) && !GlobalState.fullScreen) {
            GlobalState.cursorCaught = false;
            Gdx.input.setCursorCatched(GlobalState.cursorCaught);
        }
        
        // Toggle catching the cursor
        if(Gdx.input.isKeyPressed(TOGGLE_FIRST_PERSON)) {
            GlobalState.isFirstPerson = !GlobalState.isFirstPerson;
            if(!GlobalState.isFirstPerson) {
                playerController.player.setPosition(tmp.set(camera.position)
                        .sub(playerController.cameraOffset));
            }
        }
        
        // Toggle wireframe output for VoxelWorld
        if (keys.containsKey(TOGGLE_WIREFRAME)) {
            GlobalState.enableWireframe = !GlobalState.enableWireframe;
        }
        
        // Toggle the depth limit for the camera
        if (keys.containsKey(TOGGLE_DESCEND_LIMIT)) {
            GlobalState.descendLimit = !GlobalState.descendLimit;
        }
        
        // Toggle full screen
        //if (keys.containsKey(TOGGLE_FULLSCREEN)) {
        if(Gdx.input.isKeyPressed(TOGGLE_FULLSCREEN)) {
            if(!GlobalState.fullScreen) { // set resolution to default and set fullScreen to true
                GlobalState.oldWidth = Gdx.graphics.getWidth();
                GlobalState.oldHeight = Gdx.graphics.getHeight();
                Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode().width, 
                        Gdx.graphics.getDesktopDisplayMode().height, true);
            } else {
                Gdx.graphics.setDisplayMode(GlobalState.oldWidth, GlobalState.oldHeight, false);
            }
            GlobalState.fullScreen = !GlobalState.fullScreen;
        }
    }
}
