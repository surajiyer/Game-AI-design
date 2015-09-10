/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

/**
 *
 * @author S.S.Iyer
 */
public abstract class Terrain {
    
    ModelInstance instance;
    
    public abstract void updatePos(Vector3 pos);
}
