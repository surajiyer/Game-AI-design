/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.utils.IntIntMap;

/**
 *
 * @author S.S.Iyer
 */
public class FPCameraController extends FirstPersonCameraController {
    private final Camera camera;
    private final IntIntMap keys = new IntIntMap();
    private int TURN_LEFT = Input.Keys.LEFT;
    private int TURN_RIGHT = Input.Keys.RIGHT;
    private int DOWN = Input.Keys.Z;
    private float rotateAngle = 360f;

    public FPCameraController(Camera camera) {
        super(camera);
        this.camera = camera;
    }
    
    @Override
    public void update () {
        update(Gdx.graphics.getDeltaTime());
    }
    
    @Override
    public void update(float deltaTime) {
        System.out.println("Hi");
        super.update(deltaTime);
        if (keys.containsKey(TURN_LEFT)) {
            System.out.println("Turn left");
            camera.direction.rotate(camera.up, -deltaTime * rotateAngle);
        }
        if (keys.containsKey(TURN_RIGHT)) {
            System.out.println("Turn right");
            camera.direction.rotate(camera.up, deltaTime * rotateAngle);
        }
        camera.update(true);
    }
}
