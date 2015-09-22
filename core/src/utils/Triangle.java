/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.math.Vector3;

/**
 *
 * @author S.S.Iyer
 */
public class Triangle {
    public MeshPartBuilder.VertexInfo v1, v2, v3;
    public Vector3 faceNormal = new Vector3();
    static Vector3 tmp = new Vector3();
    static Vector3 tmp2 = new Vector3();
    MeshPart mp;
    
    public void set(VertexInfo v1, VertexInfo v2, VertexInfo v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.calcNormal();
    }

    public void calcNormal() {
        tmp.set(v2.position).sub(v1.position);
        tmp2.set(v3.position).sub(v1.position);
        float x = tmp.y * tmp2.z - tmp.z * tmp2.y;
        float y = tmp.z * tmp2.x - tmp.x - tmp2.z;
        float z = tmp.x * tmp2.y - tmp.y * tmp2.x;
        faceNormal.set(x, y, z).nor();
    }

    public Vector3 getCenter(Vector3 out) {
        out.set(v1.position).add(v2.position).add(v3.position);
        return out.scl(1/3f);
    }

    public Vector3 getInterpolatedNormal(Vector3 out) {
        out.set(v1.normal).add(v2.normal).add(v3.normal);
        return out.scl(1/3f);
    }
}
