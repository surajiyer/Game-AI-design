/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;
import utils.Player;

/**
 * Used to control the camera.
 * @author S.S.Iyer
 */
public class BobController extends InputAdapter {
    private final Camera camera;
    private final Player bob;
    private final IntIntMap keys = new IntIntMap();
    private int TURN_LEFT = Keys.LEFT;
    private int TURN_RIGHT = Keys.RIGHT;
    private int STRAFE_LEFT = Keys.A;
    private int STRAFE_RIGHT = Keys.D;
    private int FORWARD = Keys.W;
    private int BACKWARD = Keys.S;
    private int UP = Keys.Q;
    private int DOWN = Keys.Z;
    private int LOOK_UP = Keys.UP;
    private int LOOK_DOWN = Keys.DOWN;
    private float turnSpeed = 120f;
    private float velocity = 5;
    private float degreesPerPixel = 0.5f;
    private final Vector3 tmp = new Vector3();

    public BobController(Camera camera) {
        this.camera = camera;
        bob = new Player(camera.position);
    }
    
    public Vector3 getPos() {
        return camera.position;
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
    
    /** 
     * Sets the turning speed of the camera for turning left/right.
     * @param turnSpeed 
     */
    public void setRotationSpeed (float turnSpeed) {
        this.turnSpeed = turnSpeed;
    }

    /** 
     * Sets how many degrees to rotate per pixel the mouse moved.
     * @param degreesPerPixel 
     */
    public void setDegreesPerPixel (float degreesPerPixel) {
        this.degreesPerPixel = degreesPerPixel;
    }

    @Override
    public boolean touchDragged (int screenX, int screenY, int pointer) {
        float deltaX = -Gdx.input.getDeltaX() * degreesPerPixel;
        float deltaY = -Gdx.input.getDeltaY() * degreesPerPixel;
        camera.direction.rotate(camera.up, deltaX);
        tmp.set(camera.direction).crs(camera.up).nor();
        camera.direction.rotate(tmp, deltaY);
        // camera.up.rotate(tmp, deltaY);
        return true;
    }

    public void update () {
        update(Gdx.graphics.getDeltaTime());
    }

    public void update(float deltaTime) {
        if (keys.containsKey(FORWARD)) {
            tmp.set(camera.direction).nor().scl(deltaTime * velocity);
            camera.position.add(tmp);
        }
        if (keys.containsKey(BACKWARD)) {
            tmp.set(camera.direction).nor().scl(-deltaTime * velocity);
            camera.position.add(tmp);
        }
        if (keys.containsKey(TURN_LEFT)) {
            camera.direction.rotate(camera.up, deltaTime * turnSpeed);
        }
        if (keys.containsKey(TURN_RIGHT)) {
            camera.direction.rotate(camera.up, -deltaTime * turnSpeed);
        }
        if (keys.containsKey(LOOK_UP)) {
            camera.direction.rotate(tmp.set(camera.direction).crs(camera.up).nor(), 
                    deltaTime * turnSpeed);
        }
        if (keys.containsKey(LOOK_DOWN)) {
            camera.direction.rotate(tmp.set(camera.direction).crs(camera.up).nor(), 
                    -deltaTime * turnSpeed);
        }
        if (keys.containsKey(STRAFE_LEFT)) {
            tmp.set(camera.direction).crs(camera.up).nor().scl(-deltaTime * velocity);
            camera.position.add(tmp);
        }
        if (keys.containsKey(STRAFE_RIGHT)) {
            tmp.set(camera.direction).crs(camera.up).nor().scl(deltaTime * velocity);
            camera.position.add(tmp);
        }
        if (keys.containsKey(UP)) {
            tmp.set(camera.up).nor().scl(deltaTime * velocity);
            camera.position.add(tmp);
        }
        if (keys.containsKey(DOWN)) {
            tmp.set(camera.up).nor().scl(-deltaTime * velocity);
            camera.position.add(tmp);
        }
        camera.update(true);
    }
}
