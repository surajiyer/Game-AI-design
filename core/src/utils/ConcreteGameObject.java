/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/**
 *
 * @author S.S.Iyer
 */
public class ConcreteGameObject extends GameObject {
    public final ModelInstance instance;
    
    public ConcreteGameObject(Model model) {
        instance = new ModelInstance(model);
        worldTrans.set(instance.transform);
        this.calculateBounds();
    }
    
    @Override
    protected void calculateBounds() {
        instance.calculateBoundingBox(bounds);
        super.calculateBounds();
    }
    
    @Override
    public boolean isVisible(final Camera cam) {
        instance.transform.getTranslation(tmp);
        tmp.add(center);
        return cam.frustum.sphereInFrustum(tmp, radius);
    }
    
    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        instance.getRenderables(renderables, pool);
    }

    @Override
    public void setScale(float scl) {
        scale = scl / scale;
        instance.transform.scl(scale);
        worldTrans.scl(scale);
        scale = scl;
    }

    @Override
    public void setPosition(Vector3 pos) {
        position.set(pos);
        instance.transform.setTranslation(pos);
        worldTrans.setTranslation(pos);
    }
}
