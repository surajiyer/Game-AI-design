/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.math.Vector3;

/**
 *
 * @author S.S.Iyer
 */
public class Triangle {
    MeshPartBuilder.VertexInfo a, b, c;
    public Vector3 faceNormal = new Vector3();
    static Vector3 tmp = new Vector3();
    static Vector3 tmp2 = new Vector3();
    MeshPart mp;

    public Triangle(MeshPartBuilder.VertexInfo a, MeshPartBuilder.VertexInfo b, 
            MeshPartBuilder.VertexInfo c) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.calcNormal();
    }

    public void calcNormal() {
        tmp.set(b.position).sub(a.position);
        tmp2.set(c.position).sub(a.position);
        float x = tmp.y * tmp2.z - tmp.z * tmp2.y;
        float y = tmp.z * tmp2.x - tmp.x - tmp2.z;
        float z = tmp.x * tmp2.y - tmp.y * tmp2.x;
        faceNormal.set(x, y, z).nor();
    }

    public Vector3 getCenter(Vector3 out) {
        out.set(a.position).add(b.position).add(c.position);
        return out.scl(1/3f);
    }

    public Vector3 getInterpolatedNormal(Vector3 out) {
        out.set(a.normal).add(b.normal).add(c.normal);
        return out.scl(1/3f);
    }
}
