/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terrain;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

/**
 *
 * @author S.S.Iyer
 */
public abstract class Terrain {
    
    public ModelInstance instance;
    
    /**
     * Update the position of the terrain w.r.t to the given vector point.
     * @param pos Vector3 point
     */
    public abstract void updatePos(Vector3 pos);
    
    /**
     * Get the height of the center point of the terrain.
     * @return 
     */
    public abstract float getHeight();
}
