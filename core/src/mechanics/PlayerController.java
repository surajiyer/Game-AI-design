/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mechanics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;
import utils.GameController;
import utils.MultipleAnimationsController;
import static main.VoxelTest.UNITS_PER_METER;

/**
 * Used to control the camera.
 * @author S.S.Iyer
 */
public class PlayerController extends InputAdapter {
    
    private final Player player;
    MultipleAnimationsController playerAnimsController;
    private final Camera camera;
    public final Vector3 cameraOffset;
    private final IntIntMap keys = new IntIntMap();
    private final int FORWARD = Keys.W;
    private final int STRAFE_LEFT = Keys.A;
    private final int BACKWARD = Keys.S;
    private final int STRAFE_RIGHT = Keys.D;
    private final int UP = Keys.Q;
    private final int DOWN = Keys.Z;
    private final int LOOK_UP = Keys.UP;
    private final int LOOK_DOWN = Keys.DOWN;
    private final int TURN_LEFT = Keys.LEFT;
    private final int TURN_RIGHT = Keys.RIGHT;
    private float velocity = 5;
    private final float degreesPerPixel = 0.5f;
    private final float turnSpeed = 120f;
    private boolean strafingLeft = false;
    private boolean strafingRight = false;
    private final Vector3 tmp = new Vector3();

    public PlayerController(Player player, Camera camera, Vector3 cameraOffset) {
        this.player = player;
        
        // Set up an animation controller for the walking action of the player
        playerAnimsController = new MultipleAnimationsController();
        playerAnimsController.addAnimations(new String[] {
            "Head|HeadAction",
            "Torso|TorsoAction",
            "Right Hand|Right HandAction",
            "Left Hand|Left HandAction",
            "Right Leg|Right LegAction",
            "Left Leg|Left LegAction"}, player.instance);
        
        // Set the camera
        this.camera = camera;
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
    
    @Override
    public boolean mouseMoved (int screenX, int screenY) {
        if(GameController.cursorCaught) {
            float deltaX = -Gdx.input.getDeltaX() * degreesPerPixel;
            float deltaY = -Gdx.input.getDeltaY() * degreesPerPixel;
            if(GameController.isFirstPerson) {
                camera.direction.rotate(camera.up, deltaX);
                tmp.set(camera.direction).crs(camera.up).nor();
                camera.direction.rotate(tmp, deltaY);
            } else {
                camera.rotateAround(player.getPosition(), player.up, deltaX);
                camera.rotateAround(player.getPosition(), 
                        tmp.set(camera.direction).crs(player.up).nor(), deltaY);
                cameraOffset.set(tmp.set(camera.position).sub(player.getPosition()));
                camera.lookAt(player.getPosition());
                camera.up.set(Vector3.Y);
            }
            player.rotate(camera.direction, deltaX, deltaY);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean scrolled (int amount) {
        velocity += -amount * UNITS_PER_METER;
        return false;
    }

    /** 
     * Sets the velocity in units per second for moving forward, backward and 
     * strafing left/right.
     * @param velocity the velocity in units per second
     */
    public void setVelocity (float velocity) {
        this.velocity = velocity;
    }
    
    /** @return the velocity of the player */
    public float getVelocity () {
        return velocity;
    }

    public void update () {
        update(Gdx.graphics.getDeltaTime());
    }    
    
    public void update(float deltaTime) {
        // If any of the following movements, update the animations controller
        PlayerController: if (keys.containsKey(FORWARD) || keys.containsKey(BACKWARD) 
                || keys.containsKey(STRAFE_LEFT) || keys.containsKey(STRAFE_RIGHT)) {
            playerAnimsController.update(deltaTime, 4);
        }
        
        // Move forward
        if (keys.containsKey(FORWARD)) {
            tmp.set(player.direction).nor().scl(deltaTime * velocity);
            player.setPosition(player.getPosition().add(tmp));
            camera.position.set(tmp.set(player.getPosition()).add(cameraOffset));
        }
        
        // Move backward
        if (keys.containsKey(BACKWARD)) {
            tmp.set(player.direction).nor().scl(-deltaTime * velocity);
            player.setPosition(player.getPosition().add(tmp));
            camera.position.set(tmp.set(player.getPosition()).add(cameraOffset));
        }
        
        // Strafe left
        if (keys.containsKey(STRAFE_LEFT)) {
            tmp.set(player.direction).crs(camera.up).nor().scl(-deltaTime * velocity);
            if(!strafingLeft) {
                player.rotateBody(40);
                strafingLeft = true;
            }
            player.setPosition(player.getPosition().add(tmp));
            tmp.set(player.getPosition());
            camera.position.set(tmp.add(cameraOffset));
        } else if(strafingLeft) {
            player.rotateBody(-40);
            strafingLeft = false;
        }
        
        // Strafe right
        if (keys.containsKey(STRAFE_RIGHT)) {
            tmp.set(player.direction).crs(camera.up).nor().scl(deltaTime * velocity);
            if(!strafingRight) {
                player.rotateBody(-40);
                strafingRight = true;
            }
            player.setPosition(player.getPosition().add(tmp));
            tmp.set(player.getPosition());
            camera.position.set(tmp.add(cameraOffset));
        } else if(strafingRight) {
            player.rotateBody(40);
            strafingRight = false;
        }
        
        // Turn left
        if (keys.containsKey(TURN_LEFT)) {
            if(GameController.isFirstPerson)
                camera.direction.rotate(camera.up, deltaTime * turnSpeed);
            else
                camera.rotateAround(player.getPosition(), player.up, -deltaTime * turnSpeed);
        }
        
        // Turn right
        if (keys.containsKey(TURN_RIGHT)) {
            if(GameController.isFirstPerson)
                camera.direction.rotate(camera.up, -deltaTime * turnSpeed);
            else
                camera.rotateAround(player.getPosition(), player.up, deltaTime * turnSpeed);
        }
        
        // Look up
        if (keys.containsKey(LOOK_UP)) {
            if(GameController.isFirstPerson)
                camera.direction.rotate(tmp.set(camera.direction).crs(camera.up).nor(), 
                    deltaTime * turnSpeed);
            else
                camera.rotateAround(player.getPosition(), 
                        tmp.set(camera.direction).crs(player.up).nor(), deltaTime * turnSpeed);
        }
        
        // Look down
        if (keys.containsKey(LOOK_DOWN)) {
            if(GameController.isFirstPerson)
                camera.direction.rotate(tmp.set(camera.direction).crs(camera.up).nor(), 
                        -deltaTime * turnSpeed);
            else
                camera.rotateAround(player.getPosition(), 
                        tmp.set(camera.direction).crs(player.up).nor(), -deltaTime * turnSpeed);
        }
        
        if(!GameController.isFirstPerson) {
            camera.lookAt(player.getPosition());
            camera.up.set(Vector3.Y);
        }
        camera.update(true);
    }
}
