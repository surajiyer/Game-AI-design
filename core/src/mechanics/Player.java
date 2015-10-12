/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mechanics;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import utils.GameObject;

/**
 *
 * @author S.S.Iyer
 */
public class Player extends GameObject {
    
    public enum PlayerParts {
        RIGHT_HAND_PART(0),
        LEFT_HAND_PART(2),
        RIGHT_LEG_PART(4),
        LEFT_LEG_PART(6),
        TORSO_PART(8),
        HEAD_PART(10);
        
        int id;
        
        private PlayerParts(int id) {
            this.id = id;
        }
    }    
    
    public final ModelInstance instance;
    public final Vector3 direction;
    public final Vector3 up;
    public int flags;
    
    private Node headNode;
    private final Quaternion tmpQuat = new Quaternion();
    
    public Player(Model model, Vector3 pos, Vector3 dir) {
        instance = new ModelInstance(model);
        headNode = instance.nodes.get(PlayerParts.HEAD_PART.id);
        setPosition(pos);
        direction = new Vector3(dir).nor().scl(1, 0, 1);
        up = new Vector3(Vector3.Y);
        calculateDimensions();
    }
    
    @Override
    public void setScale(float scl) {
        scale = scl / scale;
        instance.transform.scl(scl);
        scale = scl;
    }
    
    @Override
    public void setPosition(Vector3 pos) {
        position.set(pos);
        instance.transform.setTranslation(pos);
    }
    
    public void rotate(Vector3 dir, float deltaX, float deltaY) {
        headNode.rotation.mul(tmpQuat.setEulerAngles(0, -deltaY, 0));
        instance.transform.rotate(up, deltaX);
        direction.set(dir).nor().scl(1, 0, 1);
    }
    
    public void rotateBody(float deltaX) {
        headNode.rotation.mul(tmpQuat.setEulerAngles(0, 0, deltaX));
        instance.transform.rotate(up, deltaX);
    }
    
    @Override
    protected void calculateDimensions() {
        instance.calculateBoundingBox(bounds);
        super.calculateDimensions();
    }

    @Override
    public boolean isVisible(Camera cam) {
        instance.transform.getTranslation(tmp);
        tmp.add(center);
        return cam.frustum.sphereInFrustum(tmp, radius);
    }
    
    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        instance.getRenderables(renderables, pool);
    }

}