/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.utils.Pool;

/**
 *
 * @author S.S.Iyer
 */
public abstract class GameObject implements RenderableProvider {
    protected final Vector3 position = new Vector3();
    protected float scale = 1f;
    protected final BoundingBox bounds = new BoundingBox();
    private ModelInstance boundsModel = null;
    public final Vector3 center = new Vector3();
    public final Vector3 dimensions = new Vector3();
    public float radius;
    protected Vector3 tmp = new Vector3();
    
    protected void calculateDimensions() {
        bounds.getCenter(center);
        bounds.getDimensions(dimensions);
        radius = dimensions.len() / 2f;
    }
    
    public abstract void setScale(float scl);
    
    public abstract void setPosition(Vector3 pos);
    
    /** @return the scaling of the cube */
    public float getScale() {
        return scale;
    }
    
    /** @return the world position of the cube */
    public Vector3 getPosition() {
        return position;
    }
    
    public ModelInstance boundingBoxModel() {
        if(boundsModel != null) {
            boundsModel.transform.setToScaling(scale, scale, scale);
            boundsModel.transform.setTranslation(position);
            return boundsModel;
        }
        
        Vector3 C1 = new Vector3(bounds.getCorner000(tmp));
        Vector3 C2 = new Vector3(bounds.getCorner001(tmp));
        Vector3 C4 = new Vector3(bounds.getCorner011(tmp));
        Vector3 C5 = new Vector3(bounds.getCorner100(tmp));
        float width = C1.sub(C2).len();
        float height = C4.sub(C2).len();
        float depth = C5.sub(C1).len();

        // Shape renderer test
        ModelBuilder mb = new ModelBuilder();
        boundsModel = new ModelInstance(mb.createBox(width, height, depth, GL20.GL_LINES, 
                new Material(new ColorAttribute(ColorAttribute.Diffuse, Color.RED)), 
                Usage.Position | Usage.Normal));
        boundsModel.transform.setToScaling(scale, scale, scale);
        boundsModel.transform.setTranslation(position);
        return boundsModel;
    }
    
    public abstract boolean isVisible(final Camera cam);

    @Override
    public abstract void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool);
}
