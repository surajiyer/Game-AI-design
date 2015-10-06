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
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;

/**
 * Used to control the camera.
 * @author S.S.Iyer
 */
public class PlayerController extends InputAdapter {
    private final Player player;
    public final Vector3 cameraOffset;
    private final IntIntMap keys = new IntIntMap();
    private int FORWARD = Keys.W;
    private int STRAFE_LEFT = Keys.A;
    private int BACKWARD = Keys.S;
    private int STRAFE_RIGHT = Keys.D;
    private float velocity = 5;
    private float degreesPerPixel = 0.5f;

    public PlayerController(Player player, Vector3 cameraOffset) {
        this.player = player;
        this.cameraOffset = cameraOffset;
    }

    @Override
    public boolean keyDown (int keycode) {
        keys.put(keycode, keycode);
        return true;
    }

    @Override
    public boolean keyUp (int keycode) {
        keys.remove(keycode, 0);
        return true;
    }

    /** 
     * Sets the velocity in units per second for moving forward, backward and 
     * strafing left/right.
     * @param velocity the velocity in units per second 
     */
    public void setVelocity (float velocity) {
        this.velocity = velocity;
    }

    public void update (Camera camera) {
        update(camera, Gdx.graphics.getDeltaTime());
    }
    
    private final Vector3 tmp = new Vector3();
    private final Quaternion tmp1 = new Quaternion();
    private final Vector3 position = new Vector3();
    
    public void update(Camera camera, float deltaTime) {
        // Rotate player head to face the direction of the camera
        player.setHeadDirection(camera.direction);
        if() {
            
        }
        player.setBodyDirection(player.model.transform.getRotation(tmp1, true)
                .transform(new Vector3()));
        
        // Move the player and the camera following him forward/backward
        if (keys.containsKey(FORWARD)) {
            tmp.set(player.direction).nor().scl(deltaTime * velocity);
            player.position.add(tmp);
            position.set(player.position);
            camera.position.set(position.add(cameraOffset));
        }
        if (keys.containsKey(BACKWARD)) {
            tmp.set(player.direction).nor().scl(-deltaTime * velocity);
            player.position.add(tmp);
            position.set(player.position);
            camera.position.set(position.add(cameraOffset));
        }
        
        // Strafe left/right
        if (keys.containsKey(STRAFE_LEFT)) {
            tmp.set(player.direction).crs(camera.up).nor().scl(-deltaTime * velocity);
            player.position.add(tmp);
            position.set(player.position);
            camera.position.set(position.add(cameraOffset));
        }
        if (keys.containsKey(STRAFE_RIGHT)) {
            tmp.set(player.direction).crs(camera.up).nor().scl(deltaTime * velocity);
            player.position.add(tmp);
            position.set(player.position);
            camera.position.set(position.add(cameraOffset));
        }
        camera.update(true);
    }
}
