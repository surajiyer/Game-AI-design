/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terrain;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import utils.Triangle;

/**
 *
 * @author S.S.Iyer
 */
public class SimplexTerrain extends Renderable {
    
    /** The height map */
    final HeightMap heightMap;
    
    /** Position attribute (P.x, P.y, P.z) */
    public static final int POSITION_COMPONENTS = 3;
    
    /** Normal vector attribute (N.x, N.y, N.z) */
    public static final int NORMAL_COMPONENTS = 3;
    
    /** Total number of components */
    public static final int NUM_COMPONENTS = POSITION_COMPONENTS + NORMAL_COMPONENTS;
    
    /** The maximum number of triangles our mesh will hold */
    public final int MAX_TRIS;

    /** The maximum number of vertices our mesh will hold */
    public final int MAX_VERTS;
    
    /** Number of times the terrain has been generated. */
    static int generateCount = 0;
    
    /** Horizontal and vertical scaling factor for terrain */
    final float WIDTH_SCALE, HEIGHT_SCALE;
    
    // temporary variables
    static Triangle tmp = new Triangle();
    static VertexInfo[] tmp1 = new VertexInfo[3];
    
    public SimplexTerrain(HeightMap hm, float WIDTH_SCALE, float HEIGHT_SCALE) {
        heightMap = hm;
        primitiveType = GL20.GL_TRIANGLE_STRIP;
        MAX_TRIS = (hm.getWidth()-1)*hm.getDepth();
        MAX_VERTS = MAX_TRIS * 3;
        this.WIDTH_SCALE = WIDTH_SCALE;
        this.HEIGHT_SCALE = HEIGHT_SCALE;
        for(int i=0; i < tmp1.length; i++) {
            tmp1[i] = new VertexInfo();
        }
        shader = new SimplexShader();
        meshPartOffset = NUM_COMPONENTS;
        meshPartSize = MAX_VERTS;
        this.generate();
    }

    /** length of the map on the x axis in world units */
    public float getWidth() {
        return heightMap.getWidth()*WIDTH_SCALE;
    }
    
    /** length of the map on the z axis in world units */
    public float getDepth() {
        return heightMap.getDepth()*HEIGHT_SCALE;
    }
    
    /** Get the minimum height of the terrain. */
    private float getMinHeight() {
        return heightMap.getMin() * HEIGHT_SCALE;
    }
    
    /** Get the maximum height of the terrain. */
    private float getMaxHeight() {
        return heightMap.getMax() * HEIGHT_SCALE;
    }
    
    /** 
     * Get the center of the heightmap in world units.
     * @return 
     */
    public Vector3 getCenter() {
        return new Vector3().set(getWidth() / 2f, 
                (getMinHeight() + getMaxHeight()) / 2f, 
                getDepth() / 2f);
    }
    
    /**
     * Generate the terrain from the provided height map.
     * @return a mesh containing the terrain
     */
    public Mesh generate() {
        // if terrain is already generated
        if(generateCount++ > 0) {
            return mesh;
        }
        
        // 3 vertex per triangle
        float[] verts = new float[MAX_VERTS*NUM_COMPONENTS];
        int i = 0;
        float scale = heightMap.getNumPoints()/getWidth();
        
        // build a new terrain mesh
        for(int x=0; x < heightMap.getWidth()-1; x++) {
            for(int z=0; z < heightMap.getDepth()-1; z++) {
                // Set the 3 vertices of the triangle
                tmp1[0].setPos(x*WIDTH_SCALE, 
                        heightMap.getHeight(x, z)*HEIGHT_SCALE, 
                        z*WIDTH_SCALE);
                tmp1[1].setPos((x+1)*WIDTH_SCALE, 
                        heightMap.getHeight(x+1, z)*HEIGHT_SCALE, 
                        z*WIDTH_SCALE);
                tmp1[2].setPos(x*WIDTH_SCALE, 
                        heightMap.getHeight(x, z+1)*HEIGHT_SCALE, 
                        (z+1)*WIDTH_SCALE);
                
                // create a new triangle (automatically sets the face normal)
                tmp.set(tmp1[0], tmp1[1], tmp1[2]);
                
                // invert face normal if its the last triangle strip
                if(z == heightMap.getDepth()-1) {
                    tmp.faceNormal.scl(-1f);
                }
                
                // for each vertex, set the facenormal and add it to the float 
                // vertex array
                for(VertexInfo v : tmp1) {
                    v.setNor(tmp.faceNormal);
                    verts[i++] = v.position.x;
                    verts[i++] = v.position.y;
                    verts[i++] = v.position.z;
                    verts[i++] = v.normal.x;
                    verts[i++] = v.normal.y;
                    verts[i++] = v.normal.z;
                }
           }
        }
        
        mesh = new Mesh( true, MAX_VERTS, 0,
                new VertexAttribute( Usage.Position, POSITION_COMPONENTS, 
                        ShaderProgram.POSITION_ATTRIBUTE ),
                new VertexAttribute( Usage.Normal, NORMAL_COMPONENTS, 
                        ShaderProgram.NORMAL_ATTRIBUTE ) );
        mesh.setVertices(verts);
        
        // return the finalized mesh
        return mesh;
    }
    
    public void dispose() {
        shader.dispose();
        mesh.dispose();
    }
}
