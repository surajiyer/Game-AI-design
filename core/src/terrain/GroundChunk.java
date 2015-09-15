/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terrain;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 *
 * @author S.S.Iyer
 */
public class GroundChunk extends ModelInstance {
    private static final BoundingBox box = new BoundingBox();
    public Vector3 position = new Vector3();
    public Vector3 dimensions = new Vector3();
    public float radius;

    public GroundChunk (Model model) {
        super(model);
        calculateTransforms();
        calculateBoundingBox(box);
        box.getDimensions(dimensions);
        radius = dimensions.len() / 2f;
        box.getCenter(position);
    }

    public boolean isVisible (PerspectiveCamera cam) {
        return cam.frustum.sphereInFrustum(position, radius);
    }

}
