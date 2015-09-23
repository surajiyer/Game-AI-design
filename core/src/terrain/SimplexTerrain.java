/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terrain;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
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
    
    /** Texture coordinates attribute (T.u, T.v) */
    public static final int TEXTURE_COMPONENTS = 2;
    
    /** Total number of components */
    public static final int NUM_COMPONENTS = POSITION_COMPONENTS 
            /*+ NORMAL_COMPONENTS*/ + TEXTURE_COMPONENTS;
    
    /** The number of vertices per triangle */
    public static final int VERTEICES_PER_TRIANGLE = 2;
    
    /** The maximum number of triangles our mesh will hold */
    public final int MAX_TRIS;

    /** The maximum number of vertices our mesh will hold */
    public final int MAX_VERTS;
    
    /** Number of times the terrain has been generated. */
    static int generateCount = 0;
    
    /** Horizontal and vertical scaling factor for terrain */
    float WIDTH_SCALE, HEIGHT_SCALE;
    
    /** Terrain coloring lookup texture */
    public final Texture lookup;
    
    // temporary variables
    static Triangle tmp = new Triangle();
    static VertexInfo[] tmp1 = new VertexInfo[3];
    
    public SimplexTerrain(HeightMap hm, float WIDTH_SCALE, float HEIGHT_SCALE, FileHandle lookupFile) {
        heightMap = hm;
        this.WIDTH_SCALE = WIDTH_SCALE;
        this.HEIGHT_SCALE = HEIGHT_SCALE;
        for(int i=0; i < tmp1.length; i++) {
            tmp1[i] = new VertexInfo();
        }
        primitiveType = GL20.GL_TRIANGLE_STRIP;
        MAX_TRIS = hm.getWidth()*hm.getDepth();
        MAX_VERTS = MAX_TRIS * VERTEICES_PER_TRIANGLE;
        meshPartOffset = NUM_COMPONENTS;
        meshPartSize = MAX_VERTS;
        shader = new SimplexShader();
        lookup = new Texture(lookupFile);
        this.generate();
    }

    /** length of the map on the x-axis in world units */
    public float getWidth() {
        return heightMap.getWidth() * WIDTH_SCALE;
    }
    
    /** length of the map on the z-axis in world units */
    public float getDepth() {
        return heightMap.getDepth() * WIDTH_SCALE;
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
        return new Vector3(getWidth() / 2f, 
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
        
        // build a new terrain mesh
        int k = 0;
        int w = heightMap.getWidth();
        int h = heightMap.getDepth();
        int texWidth = lookup.getWidth();
        for(int z=0; z < h; z++) {
            for(int x=0; x < w; x++) {
                // Set the 3 vertices of the triangle
                tmp1[0].setPos(x,
                        heightMap.getHeight(x, z, WIDTH_SCALE)*HEIGHT_SCALE, 
                        z);
                tmp1[1].setPos(x,
                        heightMap.getHeight(x, z+(z==h-1?0:1), WIDTH_SCALE)*HEIGHT_SCALE, 
                        z+1);
                tmp1[2].setPos(x+1,
                        heightMap.getHeight(x+(x==w-1?0:1), z, WIDTH_SCALE)*HEIGHT_SCALE, 
                        z);
                
                // Set texture coordinates per vertex
                tmp1[0].setUV(heightMap.getHeight(x, z, WIDTH_SCALE), 0);
                tmp1[1].setUV(heightMap.getHeight(x+(x==w-1?0:1), z, WIDTH_SCALE), 0);
                tmp1[2].setUV(heightMap.getHeight(x, z+(z==h-1?0:1), WIDTH_SCALE), 0);
                
                // create a new triangle (automatically sets the face normal)
                tmp.set(tmp1[0], tmp1[1], tmp1[2]);
                
                // for each vertex, set the facenormal and add it to the float 
                // vertex array
                for(int i=0; i<tmp1.length-1; i++) {
                    tmp1[i].setNor(tmp.faceNormal);
                    verts[k++] = tmp1[i].position.x;
                    verts[k++] = tmp1[i].position.y;
                    verts[k++] = tmp1[i].position.z;
//                    verts[k++] = tmp1[i].normal.x;
//                    verts[k++] = tmp1[i].normal.y;
//                    verts[k++] = tmp1[i].normal.z;
                    verts[k++] = tmp1[i].uv.x * texWidth;
                    verts[k++] = tmp1[i].uv.y;
                }
            }
        }
        
        // Calculate the normals in the mesh
        //this.calcNormal(verts, w, h);
        
        // Generate a new mesh with the right attributes
        mesh = new Mesh( true, MAX_VERTS, 0,
                new VertexAttribute( Usage.Position, POSITION_COMPONENTS, 
                        ShaderProgram.POSITION_ATTRIBUTE ),
//                new VertexAttribute( Usage.Normal, NORMAL_COMPONENTS, 
//                        ShaderProgram.NORMAL_ATTRIBUTE ),
                new VertexAttribute( Usage.TextureCoordinates, TEXTURE_COMPONENTS, 
                        ShaderProgram.TEXCOORD_ATTRIBUTE+"0" ));
        
        // Set the vertices of the terrain in the mesh
        mesh.setVertices(verts);
        
        // Scale the mesh according to the width and height scale factors
        this.scale(WIDTH_SCALE);   
        
        // Return the finalized mesh
        return mesh;
    }
    
    void calcNormal(float[] verts, int w, int h) {
        float[] norms = new float[verts.length];
        int k=0;
        for(int i=0; i < verts.length; i+=2*POSITION_COMPONENTS) {
            tmp1[0].setPos(verts[i], verts[i+1], verts[i+2]);
            tmp1[1].setPos(verts[i+3], verts[i+4], verts[i+5]);
            tmp1[1].setPos(verts[i+6], verts[i+7], verts[i+8]);
            tmp.set(tmp1[0], tmp1[1], tmp1[2]);
            for(int j = 0; j < NORMAL_COMPONENTS; j++) {
                norms[k++] = tmp.faceNormal.x;
                norms[k++] = tmp.faceNormal.y;
                norms[k++] = tmp.faceNormal.z;
            }
            tmp1[0].setPos(verts[i+6], verts[i+7], verts[i+8]);
            tmp1[2].setPos(verts[i+9], verts[i+10], verts[i+11]);
            tmp.set(tmp1[0], tmp1[1], tmp1[2]);
            for(int j = 0; j < NORMAL_COMPONENTS; j++) {
                norms[k++] = tmp.faceNormal.x;
                norms[k++] = tmp.faceNormal.y;
                norms[k++] = tmp.faceNormal.z;
            }
        }
    }
    
    public void scale(float widthScale) {
        WIDTH_SCALE = widthScale;
        if(mesh != null)
            mesh.scale(WIDTH_SCALE, WIDTH_SCALE, WIDTH_SCALE);
    }
    
    public void dispose() {
        shader.dispose();
        mesh.dispose();
    }
}
