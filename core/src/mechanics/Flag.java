package mechanics;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import utils.GameObject;

/**
 *
 * @author Kevin van Eenige and Daniël van der Laan
 */
public class Flag extends GameObject {
    
    public enum Occupant {
        NONE, AI, PLAYER;
    };
    
    public static ModelInstance blueFlag, redFlag, noneFlag;
    private static int idCount = 0;
    
    private ModelInstance model;
    private final int id;
    private final int scoreWeight;
    private Occupant occupant;
    
    public Flag(Vector3 pos, int scoreWeight) {
        this.id = idCount++;
        this.scoreWeight = scoreWeight;
        this.occupant = Occupant.NONE;
        this.model = noneFlag.copy();
        setPosition(pos);
    }

    public int getScoreWeight() {
        return scoreWeight;
    }

    public int getID() {
        return id;
    }

    public Occupant getOccupant() {
        return occupant;
    }

    public void setOccupant(Occupant occupant) {
        this.occupant = occupant;
        switch(occupant) {
            case AI:
                this.model = redFlag.copy();
                break;
            case PLAYER:
                this.model = blueFlag.copy();
                break;
            case NONE:
                this.model = noneFlag.copy();
                break;
        }
        model.transform.set(worldTrans);
        calculateBounds();
    }

    @Override
    public void setScale(float scl) {
        scale = scl / scale;
        model.transform.scl(scale);
        worldTrans.set(model.transform);
        scale = scl;
        calculateBounds();
    }

    @Override
    public void setPosition(Vector3 pos) {
        position.set(pos);
        model.transform.setTranslation(position);
        worldTrans.set(model.transform);
        calculateBounds();
    }

    @Override
    protected void calculateBounds() {
        model.calculateBoundingBox(bounds);
        super.calculateBounds();
    }

    @Override
    public boolean isVisible(Camera cam) {
        model.transform.getTranslation(tmp);
        //tmp.add(center);
        return cam.frustum.sphereInFrustum(tmp, radius);
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        model.getRenderables(renderables, pool);
    }

}
