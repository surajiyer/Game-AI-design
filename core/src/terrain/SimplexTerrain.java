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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/**
 *
 * @author S.S.Iyer
 */
public class SimplexTerrain implements RenderableProvider {
    /** The height map */
    final HeightMap heightMap;
    
    /** Meshes array which is used to store the terrain mesh */
    public final Mesh[] meshes;
    
    /** Horizontal and vertical scaling factor for terrain */
    float WIDTH_SCALE, HEIGHT_SCALE;
    
    /** Number of times the terrain has been generated. */
    static int generateCount = 0;
    
    /** Terrain coloring lookup texture */
    public final Texture lookup;
    
    /** The {@link Shader} to be used to render this */
    public boolean useShader = true;
    public Shader shader;
    
    /** The primitive type, OpenGL constant */
    public boolean drawWireFrame;
    
    public final boolean hasNormal, hasTexture;
    
    public SimplexTerrain(HeightMap hm, float WIDTH_SCALE, float HEIGHT_SCALE, 
            boolean useNormal, FileHandle lookupFile) {
        heightMap = hm;
        meshes = new Mesh[hm.getDepth()-1];
        this.WIDTH_SCALE = WIDTH_SCALE;
        this.HEIGHT_SCALE = HEIGHT_SCALE;
        shader = new SimplexShader();
        shader.init();
        hasTexture = lookupFile != null;
        lookup = hasTexture ? new Texture(lookupFile) : null;
        hasNormal = useNormal;
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
        VertexAttributes va = new VertexAttributes(
                VertexAttribute.Position(),
                VertexAttribute.TexCoords(0),
                VertexAttribute.Normal());
        float[] verts = new float[2*w*va.vertexSize/Float.BYTES];
        Vector3 vertex, upper, lower, left, right, 
                north, east, south, west, normal, tmp;
        
        // build a new terrain mesh
        for(int z=0; z < h-1; z++) {
            vertexCount = 0;
            for(int x=0; x < w; x++) {
                // SET LOWER VERTEX
                vertex = new Vector3(x, heightMap.getHeight(x, z), z);
                verts[vertexCount++] = vertex.x;
                verts[vertexCount++] = vertex.y * HEIGHT_SCALE;
                verts[vertexCount++] = vertex.z;
                
                // SET TEXTURE COORDINATES OF LOWER VERTEX
                verts[vertexCount++] = vertex.y * texWidth;
                verts[vertexCount++] = 0;
                
                // SET NORMALS OF LOWER VERTEX
                upper = new Vector3(x, heightMap.getHeight(x, z+1), z+1);
                lower = (z < 1 ? null : new Vector3(x, heightMap.getHeight(x, z-1), z-1));
                left = (x < 1 ? null : new Vector3(x-1, heightMap.getHeight(x-1, z), z));
                right = (x < w-1 ? new Vector3(x+1, heightMap.getHeight(x+1, z), z) : null);
                north = upper.sub(vertex);
                east = right == null ? Vector3.Zero : right.sub(vertex);
                south = lower == null ? Vector3.Zero : lower.sub(vertex);
                west = left == null ? Vector3.Zero : left.sub(vertex);
                normal = north.add(east).add(south).add(west).nor();
                verts[vertexCount++] = normal.x;
                verts[vertexCount++] = normal.y;
                verts[vertexCount++] = normal.z;
                
                // SET UPPER VERTEX
                verts[vertexCount++] = upper.x;
                verts[vertexCount++] = upper.y * HEIGHT_SCALE;
                verts[vertexCount++] = upper.z;
                
                // SET TEXTURE COORDINATES OF UPPER VERTEX
                verts[vertexCount++] = upper.y * texWidth;
                verts[vertexCount++] = 0;
                
                // SET NORMALS OF UPPER VERTEX
                tmp = upper;
                upper = (z < h-2 ? new Vector3(x, heightMap.getHeight(x, z+2), z+2): null);
                lower = vertex;
                left = (x < 1 ? null : new Vector3(x-1, heightMap.getHeight(x-1, z+1), z+1));
                right = (x < w-1 ? new Vector3(x+1, heightMap.getHeight(x+1, z+1), z+1) : null);
                north = upper == null ? Vector3.Zero : upper.sub(tmp);
                east = right == null ? Vector3.Zero : right.sub(tmp);
                south = lower.sub(tmp);
                west = left == null ? Vector3.Zero : left.sub(tmp);
                normal = north.add(east).add(south).add(west).nor();
                verts[vertexCount++] = normal.x;
                verts[vertexCount++] = normal.y;
                verts[vertexCount++] = normal.z;
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
        Material m = new Material(new ColorAttribute(ColorAttribute.Diffuse,
                    MathUtils.random(0.5f, 1f),
                    MathUtils.random(0.5f, 1f), 
                    MathUtils.random(0.5f, 1f), 1));
        for (Mesh mesh : meshes) {
            Renderable renderable = pool.obtain();
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
            renderable.material = m;
            renderables.add(renderable);
        }
    }
}
