/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 *
 * @author Daniël
 */
public class Drawables {
    
    public static ModelInstance drawBoundingBox(ModelInstance model, boolean notCentered) {
        // BoundingBox test
        BoundingBox box = model.calculateBoundingBox(new BoundingBox());
        
        Vector3 C1 = box.getCorner000(new Vector3());
        Vector3 C2 = box.getCorner001(new Vector3());
        Vector3 C3 = box.getCorner010(new Vector3());
        Vector3 C4 = box.getCorner011(new Vector3());
        Vector3 C5 = box.getCorner100(new Vector3());
        Vector3 C6 = box.getCorner101(new Vector3());
        Vector3 C7 = box.getCorner110(new Vector3());
        Vector3 C8 = box.getCorner111(new Vector3());
        float width = C1.sub(C2).len();
        float height = C4.sub(C2).len();
        float depth = C5.sub(C1).len();

        // Shape renderer test
        ModelBuilder mb = new ModelBuilder();
        ModelInstance m = new ModelInstance(mb.createBox(width, height, depth, GL20.GL_LINES, 
                new Material(new ColorAttribute(ColorAttribute.Diffuse, Color.RED)), 
                Usage.Position | Usage.Normal));
        if(notCentered)
            m.transform.setToTranslation(0, height/2f, 0);
        return m;
        
    }
    
        public static ModelInstance drawBoundingBox(ModelInstance model, boolean notCentered, boolean extend) {
        // BoundingBox test
        BoundingBox box = model.calculateBoundingBox(new BoundingBox());
        box.set(new Vector3(0,0,0), new Vector3(100,100,100));
        
        
        Vector3 C1 = box.getCorner000(new Vector3());
        Vector3 C2 = box.getCorner001(new Vector3());
        Vector3 C3 = box.getCorner010(new Vector3());
        Vector3 C4 = box.getCorner011(new Vector3());
        Vector3 C5 = box.getCorner100(new Vector3());
        Vector3 C6 = box.getCorner101(new Vector3());
        Vector3 C7 = box.getCorner110(new Vector3());
        Vector3 C8 = box.getCorner111(new Vector3());
        float width = C1.sub(C2).len();
        float height = C4.sub(C2).len();
        float depth = C5.sub(C1).len();

        // Shape renderer test
        ModelBuilder mb = new ModelBuilder();
        ModelInstance m = new ModelInstance(mb.createBox(width, height, depth, GL20.GL_LINES, 
                new Material(new ColorAttribute(ColorAttribute.Diffuse, Color.RED)), 
                Usage.Position | Usage.Normal));
        if(notCentered)
            m.transform.setToTranslation(0, height/2f, 0);
        return m;
        
    }
}
