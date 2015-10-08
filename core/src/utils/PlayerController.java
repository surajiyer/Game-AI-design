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

/**
 * Used to control the camera.
 * @author S.S.Iyer
 */
public class PlayerController extends InputAdapter {
    
    private final Player player;
    MultipleAnimationsController playerAnimsController;
    private final Camera camera;
    public final Vector3 cameraOffset;
    public boolean isFirstPerson;
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
    private float degreesPerPixel = 0.5f;
    private float turnSpeed = 120f;
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
            "Left Leg|Left LegAction"}, player.model);
        
        // Set the camera
        this.camera = camera;
        this.cameraOffset = cameraOffset;
        this.isFirstPerson = false;
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
            if(isFirstPerson) {
                camera.direction.rotate(camera.up, deltaX);
                tmp.set(camera.direction).crs(camera.up).nor();
                camera.direction.rotate(tmp, deltaY);
            } else {
                camera.rotateAround(player.position, player.up, deltaX);
                camera.rotateAround(player.position, 
                        tmp.set(camera.direction).crs(player.up).nor(), deltaY);
                cameraOffset.set(tmp.set(camera.position).sub(player.position));
                camera.lookAt(player.position);
                camera.up.set(Vector3.Y);
            }
            return true;
        }
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

    public void update () {
        update(Gdx.graphics.getDeltaTime());
    }
    
    public void update(float deltaTime) {
        // Rotate player head to face the direction of the camera
        player.setDirection(tmp.set(camera.direction));
        
        // Move forward
        if (keys.containsKey(FORWARD)) {
            tmp.set(player.headDirection).nor().scl(deltaTime * velocity);
            player.position.add(tmp);
            camera.position.set(tmp.set(player.position).add(cameraOffset));
            playerAnimsController.animationSpeed = 4;
            playerAnimsController.update();
        }
        
        // Move backward
        if (keys.containsKey(BACKWARD)) {
            tmp.set(player.headDirection).nor().scl(-deltaTime * velocity);
            player.position.add(tmp);
            camera.position.set(tmp.set(player.position).add(cameraOffset));
            playerAnimsController.animationSpeed = -4;
            playerAnimsController.update();
        }
        
        // Strafe left
        if (keys.containsKey(STRAFE_LEFT)) {
            tmp.set(player.headDirection).crs(camera.up).nor().scl(-deltaTime * velocity);
            player.position.add(tmp);
            player.setBodyDirection(tmp.set(player.headDirection).rotate(player.up, 60));
            tmp.set(player.position);
            camera.position.set(tmp.add(cameraOffset));
        }
        
        // Strafe right
        if (keys.containsKey(STRAFE_RIGHT)) {
            tmp.set(player.headDirection).crs(camera.up).nor().scl(deltaTime * velocity);
            player.position.add(tmp);
            player.setBodyDirection(tmp.set(player.headDirection).rotate(player.up, -60));
            tmp.set(player.position);
            camera.position.set(tmp.add(cameraOffset));
        }
        
        // Turn left
        if (keys.containsKey(TURN_LEFT)) {
            camera.direction.rotate(camera.up, deltaTime * turnSpeed);
        }
        
        // Turn right
        if (keys.containsKey(TURN_RIGHT)) {
            camera.direction.rotate(camera.up, -deltaTime * turnSpeed);
        }
        
        // Look up
        if (keys.containsKey(LOOK_UP)) {
            camera.direction.rotate(tmp.set(camera.direction).crs(camera.up).nor(), 
                    deltaTime * turnSpeed);
        }
        
        // Look down
        if (keys.containsKey(LOOK_DOWN)) {
            camera.direction.rotate(tmp.set(camera.direction).crs(camera.up).nor(), 
                    -deltaTime * turnSpeed);
        }
        
        camera.update(true);
    }
}
