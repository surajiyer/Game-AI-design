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
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntIntMap;
import utils.MultipleAnimationsController;
import static main.VoxelTest.UNITS_PER_METER;
import utils.GameInfo;

/**
 * Used to control the camera.
 *
 * @author S.S.Iyer
 */
public class AIController extends InputAdapter {

    public final Player player;
    MultipleAnimationsController playerAnimsController;
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

    public AIController(Player player) {
        this.player = player;

        // Set up an animation controller for the walking action of the player
        playerAnimsController = new MultipleAnimationsController();
        playerAnimsController.addAnimations(new String[]{
            "Head|HeadAction",
            "Torso|TorsoAction",
            "Right Hand|Right HandAction",
            "Left Hand|Left HandAction",
            "Right Leg|Right LegAction",
            "Left Leg|Left LegAction"}, player.instance);

    }

    @Override
    public boolean keyDown(int keycode) {
        keys.put(keycode, keycode);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        keys.remove(keycode, 0);
        return true;
    }

    @Override
    public boolean scrolled(int amount) {
        velocity += -amount * UNITS_PER_METER;
        return false;
    }

    /**
     * Sets the velocity in units per second for moving forward, backward and
     * strafing left/right.
     *
     * @param velocity the velocity in units per second
     */
    public void setVelocity(float velocity) {
        this.velocity = velocity;
    }

    /**
     * @return the velocity of the player
     */
    public float getVelocity() {
        return velocity;
    }

    public void update() {
        update(Gdx.graphics.getDeltaTime());
    }

    public void update(float deltaTime) {

    }

    public boolean move(IntArray path) {
        for (int i = 0; i < path.size; i+=2) {
            Vector3 movement = new Vector3(path.get(i), GameInfo.world.getHeight(path.get(i), path.get(i + 1)), path.get(i + 1));
            while (!player.getPosition().equals(movement)) {
                tmp.set(movement).nor().scl(Gdx.graphics.getDeltaTime() * velocity);
                player.setPosition(player.getPosition().add(tmp));
            }
        }
        return true;
    }
}
