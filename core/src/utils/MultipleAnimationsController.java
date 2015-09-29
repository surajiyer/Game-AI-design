/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import java.util.HashMap;

/**
 *
 * @author S.S.Iyer
 */
public class MultipleAnimationsController {
    HashMap<String, AnimationController> controllerPool;
    public int loopCount;
    public float animationSpeed;
    
    public MultipleAnimationsController() {
        controllerPool = new HashMap<>();
        loopCount = -1;
        animationSpeed = 1f;
    }
    
    public void addAnimations(String[] animations, ModelInstance target) {
        for(String animation : animations) {
            this.addAnimation(animation, target);
        }
    }
    
    public void addAnimation(String animation, ModelInstance target) {
        AnimationController controller = new AnimationController(target);
        controller.setAnimation(animation, loopCount, animationSpeed, null);
        controllerPool.put(animation, controller);
    }
    
    public void removeAnimation(String animation) {
        controllerPool.remove(animation);
    }
    
    public void update() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        for(String animation : controllerPool.keySet()) {
            controllerPool.get(animation).update(deltaTime);
        }
    }
}
