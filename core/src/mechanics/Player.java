/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mechanics;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import static mechanics.GlobalState.UNITS_PER_METER;
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
    
    public enum PlayerType {
        HUMAN, AI;
    }
    
    public final ModelInstance instance;
    private final Vector3 direction;
    public final Vector3 up;
    public int flags;
    public final PlayerType type;
    
    private final Node headNode;
    private final Quaternion tmpQuat = new Quaternion();
    
    public Player(Model model, PlayerType type, Vector3 pos, Vector3 dir) {
        instance = new ModelInstance(model);
        this.type = type;
        headNode = instance.nodes.get(PlayerParts.HEAD_PART.id);
        respawn();
        direction = new Vector3(dir).nor().scl(1, 0, 1);
        up = new Vector3(Vector3.Y);
    }
    
    @Override
    public void setScale(float scl) {
        scale = scl / scale;
        instance.transform.scl(scl);
        scale = scl;
        worldTrans.set(instance.transform);
        calculateBounds();
    }
    
    @Override
    public void setPosition(Vector3 pos) {
        position.set(pos);
        instance.transform.setTranslation(pos);
        worldTrans.set(instance.transform);
        calculateBounds();
    }
    
    public void setDirection(Vector3 dir, float deltaX) {
        // Calculate head rotation
        float deltaY = (float) (Math.asin(tmp.set(dir.nor()).crs(Vector3.Y).len()) 
                * MathUtils.radDeg);
        if(dir.y < 0) deltaY *= -1; else deltaY -= 180;
        headNode.rotation.setEulerAngles(0, deltaY, 0);
        instance.calculateTransforms();
        
        // Calculate body rotation
        tmp.set(direction).scl(1, 0, 1);
        direction.set(dir).scl(1, 0, 1);
        instance.transform.rotate(up, deltaX);
        worldTrans.set(instance.transform);
        calculateBounds();
    }
    
    public void setDirection(Vector3 dir) {
        // Calculate head rotation
        float deltaY = (float) (Math.asin(tmp.set(dir.nor()).crs(Vector3.Y).len()) 
                * MathUtils.radDeg);
        if(dir.y < 0) deltaY *= -1; else deltaY -= 180;
        headNode.rotation.setEulerAngles(0, deltaY, 0);
        instance.calculateTransforms();
        
        // Calculate body rotation
        tmp.set(direction).scl(1, 0, 1);
        direction.set(dir).scl(1, 0, 1);
        float deltaX = (float) -(Math.asin(tmp.crs(direction).len()) * MathUtils.radDeg);
        if(Math.abs(deltaX) > 0.01) {
            if(tmp.y > 0) deltaX = -deltaX;
            roundOff(deltaX);
            instance.transform.rotate(up, deltaX);
        }
        worldTrans.set(instance.transform);
        calculateBounds();
    }
    
    /** 
     * Round off given value to nearest multiple of 5
     * @param n number to round off
     * @return rounded off-number
     */
    public float roundOff(float n) {
        n *= 2f;
        if(n > 0) {
            n = (float) Math.ceil(n);
        } else {
            n = (float) Math.floor(n);
        }
        n /= 2f;
        return n;
    }
    
    public Vector3 getDirection() {
        return new Vector3(direction);
    }
    
    public void rotateBody(float deltaX) {
        instance.transform.rotate(up, deltaX);
        instance.calculateTransforms();
    }
    
    @Override
    protected void calculateBounds() {
        instance.calculateBoundingBox(bounds);
        super.calculateBounds();
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
    
    public void respawn() {
        int x = (int) (MathUtils.random(GlobalState.widthField)*GlobalState.worldScale);
        int z = (int) (MathUtils.random(GlobalState.depthField)*GlobalState.worldScale);
        float y = GlobalState.voxelworld.getHeight(x, z);
        tmp.set(x, y, z);
        setPosition(tmp);
    }
}
