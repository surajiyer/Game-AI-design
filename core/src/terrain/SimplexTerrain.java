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
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import utils.Triangle;

/**
 *
 * @author S.S.Iyer
 */
public class SimplexTerrain implements RenderableProvider {
    /** The height map */
    final HeightMap heightMap;
    
    /** The primitive type, OpenGL constant */
    public boolean drawWireFrame;
    
    /** The {@link Shader} to be used to render this */
    public boolean useShader = true;
    public Shader shader;
    
    /** Total number of components per vertex */
    public static final int NUM_COMPONENTS = 
            VertexAttribute.Position().numComponents 
            /*+ VertexAttribute.Normal().numComponents*/ 
            + VertexAttribute.TexCoords(0).numComponents;
    
    /** The number of vertices per triangle */
    public static final int VERTICES_PER_TRIANGLE = 2;
    
    /** The maximum number of triangles our mesh will hold */
    public final int MAX_TRIS;

    /** The maximum number of vertices our mesh will hold */
    public final int MAX_VERTS;
    
    /** Number of times the terrain has been generated. */
    static int generateCount = 0;
    
    /** Horizontal and vertical scaling factor for terrain */
    float WIDTH_SCALE, HEIGHT_SCALE;
    
    public final Mesh[] meshes;
    
    /** Terrain coloring lookup texture */
    public final Texture lookup;
    
    /** Temporary variables */
    static Triangle tmp = new Triangle();
    static VertexInfo[] tmp1 = new VertexInfo[VERTICES_PER_TRIANGLE];
    
    public SimplexTerrain(HeightMap hm, float WIDTH_SCALE, float HEIGHT_SCALE, FileHandle lookupFile) {
        heightMap = hm;
        meshes = new Mesh[hm.getDepth()-1];
        this.WIDTH_SCALE = WIDTH_SCALE;
        this.HEIGHT_SCALE = HEIGHT_SCALE;
        for(int i=0; i < tmp1.length; i++) {
            tmp1[i] = new VertexInfo();
        }
        MAX_TRIS = hm.getWidth()*hm.getDepth();
        MAX_VERTS = MAX_TRIS * VERTICES_PER_TRIANGLE;
        shader = new SimplexShader();
        shader.init();
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
     * Generates a meshes which contain the terrain from the provided 
     * height map.
     */
    public void generate() {
        // if terrain is already generated
        if(generateCount++ > 0) {
            return;
        }
        
        // 3 vertex per triangle
        int vertexCount;
        int w = heightMap.getWidth();
        int h = heightMap.getDepth();
        int texWidth = lookup.getWidth();
        float[] verts = new float[2*w*NUM_COMPONENTS];
        VertexAttributes va = new VertexAttributes(VertexAttribute.Position(), 
                                                   //VertexAttribute.Normal(),
                                                   VertexAttribute.TexCoords(0));
        
        // build a new terrain mesh
        for(int z=0; z < h-1; z++) {
            vertexCount = 0;
            for(int x=0; x < w; x++) {
                // VERTICES
                tmp1[0].setPos(x, heightMap.getHeight(x,   z)*HEIGHT_SCALE,   z);
                tmp1[1].setPos(x, heightMap.getHeight(x, z+1)*HEIGHT_SCALE, z+1);
                
                // TEXTURE COORDINATES
                tmp1[0].setUV(heightMap.getHeight(x,   z), 0);
                tmp1[1].setUV(heightMap.getHeight(x, z+1), 0);
                
                // For each vertex, set the facenormal and add it to the float 
                // vertex array
                for (VertexInfo v : tmp1) {
                    verts[vertexCount++] = v.position.x;
                    verts[vertexCount++] = v.position.y;
                    verts[vertexCount++] = v.position.z;
                    verts[vertexCount++] = v.uv.x * texWidth;
                    verts[vertexCount++] = v.uv.y;
                }
            }
            meshes[z] = new Mesh(true, verts.length, 0, va).setVertices(verts);
        }
    }
    
    public void dispose() {
        shader.dispose();
        for (Mesh mesh : meshes) {
            mesh.dispose();
        }
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        Renderable renderable = pool.obtain();
        for (Mesh mesh : meshes) {
            renderable.mesh = mesh;
            renderable.meshPartOffset = 0;
            renderable.meshPartSize = mesh.getNumVertices();
            if(drawWireFrame) {
                renderable.primitiveType = GL20.GL_LINE_STRIP;
            } else {
                renderable.primitiveType = GL20.GL_TRIANGLE_STRIP;
            }
            renderable.userData = lookup;
            if(useShader)
                renderable.shader = shader;
            renderable.material = new Material(new ColorAttribute(ColorAttribute.Diffuse,
                    MathUtils.random(0.5f, 1f),
                    MathUtils.random(0.5f, 1f), 
                    MathUtils.random(0.5f, 1f), 1));
            renderables.add(renderable);
        }
    }
}
