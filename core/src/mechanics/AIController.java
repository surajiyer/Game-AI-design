/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mechanics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntIntMap;
import static mechanics.GlobalState.UNITS_PER_METER;
import terrain.VoxelWorld;
import utils.MultipleAnimationsController;

/**
 * Used to control the camera.
 *
 * @author S.S.Iyer
 */
public class AIController {

    public final VoxelWorld playerWorld;
    public final Player player;
    MultipleAnimationsController playerAnimsController;
    private float velocity = 5;
    private final Vector3 tmp = new Vector3();
    IntArray path;

    public AIController(Player player, VoxelWorld world) {
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
        this.playerWorld = world;
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
    public boolean init = true;
    int index = 0;

    public boolean update(IntArray path) {
        if (init) {
            index = path.size - 1;
            init = false;
        }
        this.path = path;
        return update(Gdx.graphics.getDeltaTime());

    }
    
    int times = 0;

    public boolean update(float deltaTime) {
        Vector3 movement = new Vector3(path.get(index),
                playerWorld.getHeight(path.get(index), path.get(index + 1)), path.get(index + 1));
        tmp.set(movement).nor().scl(Gdx.graphics.getDeltaTime() * velocity);
        //player.setPosition(tmp1.set(player.getPosition()).scl(1, 0, 1).add(tmp));
        player.setPosition(movement.scl(16f));
        times += 1;
        if(times == 2) {
            index -=2;
            times = 0;
        }
        if (index <= 0) {
            init = true;
            return false;
        }
        return true;
    }

    public boolean move(IntArray path) {
        for (int i = 0; i < path.size; i += 2) {
            Vector3 movement = new Vector3(path.get(i), playerWorld.getHeight(path.get(i), path.get(i + 1)), path.get(i + 1));
            while (!player.getPosition().equals(movement)) {
                tmp.set(movement).nor().scl(Gdx.graphics.getDeltaTime() * velocity);
                player.setPosition(player.getPosition().add(tmp));
            }
        }
        return true;
    }
}
