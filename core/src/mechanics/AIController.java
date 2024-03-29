/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mechanics;

import AI.ReinforcementLearning;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;
import static mechanics.GlobalState.UNITS_PER_METER;
import terrain.VoxelWorld;
import utils.MultipleAnimationsController;

/**
 * Used to control the camera.
 *
 * @author S.S.Iyer
 */
public class AIController {
    
    private enum AIState {
        DEFAULT, // Do nothing
        STEP, // Calculate new path
        MOVE, // Moving to the destination
        EVAL; // Evaluating reward at destination
    }

    public final VoxelWorld playerWorld;
    public final Player player;
    MultipleAnimationsController playerAnimsController;
    ReinforcementLearning RL;
    private float velocity = 5;
    private final Vector3 tmp = new Vector3();
    private AIState state;
    
    private IntArray path;
    private int index = 0;
    private final Vector3 destination = new Vector3();
    boolean pointReached = true;
    
    public AIController(Player player, VoxelWorld world) {
        this.player = player;
        this.playerWorld = world;
        
        // Set up an animation controller for the walking action of the player
        playerAnimsController = new MultipleAnimationsController();
        playerAnimsController.addAnimations(new String[]{
            "Torso|TorsoAction",
            "LegRight|LegRightAction",
            "LegLeft|LegLeftAction",
            "ArmRight|ArmRightAction",
            "ArmLeft|ArmLeftAction",
            "Head|HeadAction.001"}, player.instance);
        
        // Setup the brain of the AI
        this.RL = new ReinforcementLearning(player);
        
        // Set the starting state of the AI
        this.state = AIState.STEP;
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
        switch(state) {
            case STEP:
                //System.out.println("Acquiring next step");
                path = RL.step();
                state = AIState.MOVE;
                index = path.size;
                //System.out.println("Path found");
                break;
            case MOVE:
                update(Gdx.graphics.getDeltaTime());
                break;
            case EVAL:
                //System.out.println("Evaluating");
                RL.evaluate();
                state = AIState.STEP;
                //state = AIState.DEFAULT;
                break;
        }
        if (GlobalState.respawnAI) {
            player.respawn();
            GlobalState.respawnAI = false;
        }
    }

    public void update(float deltaTime) {
        // If the next point is reached, set direction to next point.
        if(pointReached) {
            index -= 2;
            if(index <= 0) {
                state = AIState.EVAL;
                return;
            }
            float scale = playerWorld.getScale();
            float x = path.get(index) * scale;
            float z = path.get(index + 1) * scale;
            destination.set(x, playerWorld.getHeight(x, z), z);
        }
        
        // Move the AI to the next point
        player.rotate(tmp.set(destination).sub(player.getPosition()));
        tmp.nor().scl(deltaTime * velocity);
        tmp.set(player.getPosition().add(tmp));
        tmp.y = playerWorld.getHeight(tmp.x, tmp.z);
        player.setPosition(tmp);
        pointReached = tmp.dst(destination) <= UNITS_PER_METER;
    }
}
