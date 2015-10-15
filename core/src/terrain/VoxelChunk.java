/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terrain;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import static main.VoxelTest.UNITS_PER_METER;
import mechanics.GlobalState;
import utils.GameObject;


/**
 * Adapted from https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/g3d/voxel/VoxelChunk.java
 * @author S.S.Iyer
 */
public class VoxelChunk extends GameObject {
    public enum VoxelType {
        /**
         * Direction of UV coordinates. 0 <= U, V, <= 1
         * |--->-- U
         * | ...
         * V ...
         * |
         */
        GRASS(new float[]{0, 0f},
                new float[]{0.125f, 0f},
                new float[]{0.1875f, 0f},
                new float[]{0.1875f, 0f},
                new float[]{0.1875f, 0f},
                new float[]{0.1875f, 0f}),
        SAND(new float[]{0.125f, 0.0625f},
                new float[]{0.125f, 0.0625f},
                new float[]{0.125f, 0.0625f},
                new float[]{0.125f, 0.0625f},
                new float[]{0.125f, 0.0625f},
                new float[]{0.125f, 0.0625f}),
        STONE(new float[]{0.3125f, 0.375f},
                new float[]{0.0625f, 0f},
                new float[]{0.25f, 0.375f},
                new float[]{0.25f, 0.375f},
                new float[]{0.25f, 0.375f},
                new float[]{0.25f, 0.375f}),
        SNOW(new float[]{0.125f, 0.25f},
                new float[]{0.125f, 0f},
                new float[]{0.25f, 0.25f},
                new float[]{0.25f, 0.25f},
                new float[]{0.25f, 0.25f},
                new float[]{0.25f, 0.25f}),
        WATER(new float[]{0.8125f, 0.75f},
                new float[]{0.8125f, 0.75f},
                new float[]{0.8125f, 0.75f},
                new float[]{0.8125f, 0.75f},
                new float[]{0.8125f, 0.75f},
                new float[]{0.8125f, 0.75f});
        
        public static float texSize = 0.0625f;
        float[] topUV;
        float[] bottomUV;
        float[] leftUV;
        float[] rightUV;
        float[] frontUV;
        float[] backUV;
        
        private VoxelType(float[] topUV, float[] bottomUV, float[] leftUV,
            float[] rightUV, float[] frontUV, float[] backUV) {
            this.topUV = topUV;
            this.bottomUV = bottomUV;
            this.leftUV = leftUV;
            this.rightUV = rightUV;
            this.frontUV = frontUV;
            this.backUV = backUV;
        }
    }
    
    public static final int VERTEX_SIZE = 8;
    public static final int NROF_FACES = 6;
    public static final int NROF_VERTICES_PER_FACE = 4;
    public static final int CHUNK_SIZE_X = UNITS_PER_METER;
    public static final int CHUNK_SIZE_Y = UNITS_PER_METER;
    public static final int CHUNK_SIZE_Z = UNITS_PER_METER;
    private static final float[] vertices = new float[CHUNK_SIZE_X * 
                CHUNK_SIZE_Y * CHUNK_SIZE_Z * NROF_FACES * VoxelChunk.VERTEX_SIZE];
    public static final short[] indices = new short[CHUNK_SIZE_X * CHUNK_SIZE_Y * 
            CHUNK_SIZE_Z * NROF_FACES * 2];
    private final VoxelWorld parent;
    public Mesh mesh;
    private final Matrix4 localTrans;
    public int numVerts;
    private boolean regenerate;
    public Material material;
    public VoxelType voxelType;
    public final byte[] voxels;
    public final int width;
    public final int height;
    public final int depth;
    private final int widthTimesHeight;
    private final int topOffset;
    private final int bottomOffset;
    private final int leftOffset;
    private final int rightOffset;
    private final int frontOffset;
    private final int backOffset;
    
    public VoxelChunk (VoxelWorld parent, int width, int height, int depth) {
        this.parent = parent;
        this.voxels = new byte[width * height * depth];
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.topOffset = width * depth;
        this.bottomOffset = -width * depth;
        this.leftOffset = -1;
        this.rightOffset = 1;
        this.frontOffset = -width;
        this.backOffset = width;
        this.widthTimesHeight = width * height;
        this.numVerts = 0;
        this.material = new Material(new ColorAttribute(ColorAttribute.Diffuse, 
                MathUtils.random(0.5f, 1f),
                MathUtils.random(0.5f, 1f),
                MathUtils.random(0.5f, 1f), 1));
        this.worldTrans = new Matrix4();
        this.localTrans = new Matrix4();
        this.mesh = new Mesh(true, CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z * NROF_FACES 
                * NROF_VERTICES_PER_FACE, indices.length, VertexAttribute.Position(), 
                VertexAttribute.Normal(), VertexAttribute.TexCoords(0));
        this.mesh.setIndices(indices);
        this.regenerate = true;
    }
    
    public Mesh generate() {
        mesh.setIndices(indices);
        mesh.setVertices(calculateVertices(vertices), 0, numVerts * VERTEX_SIZE);
        regenerate = false;
        return mesh;
    }
    
    /** @param height the height of the cube
     * @return the type of the cube */
    public VoxelType getCubeType(float height) {
        int random = MathUtils.random(8) - 4;
        if(height < 10 + random)
            return VoxelType.WATER;
        else if(height < 13 + random)
            return VoxelType.SAND;
        else if(height < 35 + random)
            return VoxelType.GRASS;
        else
            return VoxelType.SNOW;
    }

    public byte get (int x, int y, int z) {
        if (x < 0 || x >= width) return 0;
        if (y < 0 || y >= height) return 0;
        if (z < 0 || z >= depth) return 0;
        return getFast(x, y, z);
    }

    public byte getFast (int x, int y, int z) {
        return voxels[x + z * width + y * widthTimesHeight];
    }

    public void set (int x, int y, int z, byte voxel) {
        if (x < 0 || x >= width) return;
        if (y < 0 || y >= height) return;
        if (z < 0 || z >= depth) return;
        setFast(x, y, z, voxel);
    }

    public void setFast (int x, int y, int z, byte voxel) {
        voxels[x + z * width + y * widthTimesHeight] = voxel;
    }
    
    @Override
    public void setScale(float scl) {
        scale = scl / scale;
        localTrans.scl(scale);
        scale = scl;
    }
    
    @Override
    public void setPosition(Vector3 pos) {
        position.set(pos.scl(width, height, depth));
        localTrans.setTranslation(position);
    }
    
    public Matrix4 updateWorldTranform() {
        worldTrans.set(parent.worldTrans).mul(localTrans);
        return worldTrans;
    }
    
    @Override
    public boolean isVisible(Camera cam) {
        worldTrans.getTranslation(tmp);
        tmp.add(center);
        return cam.frustum.sphereInFrustum(tmp, radius);
    }
    
     /** @return if the cube is generated or not. */
    public boolean isGenerated() {
        return !regenerate;
    }
    
    public void regenerate() {
        regenerate = true;
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        Renderable renderable = pool.obtain();
        renderable.worldTransform.set(updateWorldTranform());
        renderable.material = GlobalState.voxelTextures == null ? material : 
                new Material(TextureAttribute.createDiffuse(GlobalState.voxelTextures));
        renderable.mesh = mesh;
        renderable.meshPartOffset = 0;
        renderable.meshPartSize = numVerts * (6 / NROF_VERTICES_PER_FACE);
        if(GlobalState.enableWireframe) {
            renderable.primitiveType = GL20.GL_LINE_STRIP;
        } else {
            renderable.primitiveType = GL20.GL_TRIANGLES;
        }
        renderables.add(renderable);
    }

    /** 
     * Creates a mesh out of the chunk, returning the number of indices produced
     * @param vertices
     * @return the number of vertices produced 
     */
    public float[] calculateVertices (float[] vertices) {
        int i = 0;
        int vertexOffset = 0;
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < depth; z++) {
                for (int x = 0; x < width; x++, i++) {
                    
                    if (voxels[i] == 0) continue;
                    voxelType = getCubeType(position.y+y);
                    
                    if(y == height - 1 || voxels[i + topOffset] == 0) 
                        vertexOffset = createTop(x, y, z, vertices, vertexOffset);
                    
                    if (y == 0 || voxels[i + bottomOffset] == 0)
                        vertexOffset = createBottom(x, y, z, vertices, vertexOffset);
                    
                    if (x == 0 || voxels[i + leftOffset] == 0)
                        vertexOffset = createLeft(x, y, z, vertices, vertexOffset);
                    
                    if (x == width - 1 || voxels[i + rightOffset] == 0) 
                        vertexOffset = createRight(x, y, z, vertices, vertexOffset);
                    
                    if (z == 0 || voxels[i + frontOffset] == 0) 
                        vertexOffset = createFront(x, y, z, vertices, vertexOffset);
                    
                    if (z == depth - 1 || voxels[i + backOffset] == 0) 
                        vertexOffset = createBack(x, y, z, vertices, vertexOffset);
                }
            }
        }
        
        numVerts = (vertexOffset / VERTEX_SIZE);
        return vertices;
    }

    public int createTop (int x, int y, int z, float[] vertices, int vertexOffset) {
        vertices[vertexOffset++] = x;
        vertices[vertexOffset++] = y + 1;
        vertices[vertexOffset++] = z;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = voxelType.topUV[0];
        vertices[vertexOffset++] = voxelType.topUV[1];

        vertices[vertexOffset++] = x + 1;
        vertices[vertexOffset++] = y + 1;
        vertices[vertexOffset++] = z;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = voxelType.topUV[0]+VoxelType.texSize;
        vertices[vertexOffset++] = voxelType.topUV[1];

        vertices[vertexOffset++] = x + 1;
        vertices[vertexOffset++] = y + 1;
        vertices[vertexOffset++] = z + 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = voxelType.topUV[0]+VoxelType.texSize;
        vertices[vertexOffset++] = voxelType.topUV[1]+VoxelType.texSize;

        vertices[vertexOffset++] = x;
        vertices[vertexOffset++] = y + 1;
        vertices[vertexOffset++] = z + 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = voxelType.topUV[0];
        vertices[vertexOffset++] = voxelType.topUV[1]+VoxelType.texSize;
        return vertexOffset;
    }

    public int createBottom (int x, int y, int z, float[] vertices, int vertexOffset) {
        vertices[vertexOffset++] = x;
        vertices[vertexOffset++] = y;
        vertices[vertexOffset++] = z;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = voxelType.bottomUV[0];
        vertices[vertexOffset++] = voxelType.bottomUV[1];

        vertices[vertexOffset++] = x;
        vertices[vertexOffset++] = y;
        vertices[vertexOffset++] = z + 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = voxelType.bottomUV[0]+VoxelType.texSize;
        vertices[vertexOffset++] = voxelType.bottomUV[1];

        vertices[vertexOffset++] = x + 1;
        vertices[vertexOffset++] = y;
        vertices[vertexOffset++] = z + 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = voxelType.bottomUV[0]+VoxelType.texSize;
        vertices[vertexOffset++] = voxelType.bottomUV[1]+VoxelType.texSize;

        vertices[vertexOffset++] = x + 1;
        vertices[vertexOffset++] = y;
        vertices[vertexOffset++] = z;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = voxelType.bottomUV[0];
        vertices[vertexOffset++] = voxelType.bottomUV[1]+VoxelType.texSize;
        return vertexOffset;
    }

    public int createLeft (int x, int y, int z, float[] vertices, int vertexOffset) {
        vertices[vertexOffset++] = x;
        vertices[vertexOffset++] = y;
        vertices[vertexOffset++] = z;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = voxelType.leftUV[0];
        vertices[vertexOffset++] = voxelType.leftUV[1]+VoxelType.texSize;

        vertices[vertexOffset++] = x;
        vertices[vertexOffset++] = y + 1;
        vertices[vertexOffset++] = z;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = voxelType.leftUV[0];
        vertices[vertexOffset++] = voxelType.leftUV[1];

        vertices[vertexOffset++] = x;
        vertices[vertexOffset++] = y + 1;
        vertices[vertexOffset++] = z + 1;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = voxelType.leftUV[0]+VoxelType.texSize;
        vertices[vertexOffset++] = voxelType.leftUV[1];

        vertices[vertexOffset++] = x;
        vertices[vertexOffset++] = y;
        vertices[vertexOffset++] = z + 1;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = voxelType.leftUV[0]+VoxelType.texSize;
        vertices[vertexOffset++] = voxelType.leftUV[1]+VoxelType.texSize;
        return vertexOffset;
    }

    public int createRight (int x, int y, int z, float[] vertices, int vertexOffset) {
        vertices[vertexOffset++] = x + 1;
        vertices[vertexOffset++] = y;
        vertices[vertexOffset++] = z;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = voxelType.rightUV[0]+VoxelType.texSize;
        vertices[vertexOffset++] = voxelType.rightUV[1]+VoxelType.texSize;

        vertices[vertexOffset++] = x + 1;
        vertices[vertexOffset++] = y;
        vertices[vertexOffset++] = z + 1;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = voxelType.rightUV[0];
        vertices[vertexOffset++] = voxelType.rightUV[1]+VoxelType.texSize;

        vertices[vertexOffset++] = x + 1;
        vertices[vertexOffset++] = y + 1;
        vertices[vertexOffset++] = z + 1;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = voxelType.rightUV[0];
        vertices[vertexOffset++] = voxelType.rightUV[1];

        vertices[vertexOffset++] = x + 1;
        vertices[vertexOffset++] = y + 1;
        vertices[vertexOffset++] = z;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = voxelType.rightUV[0]+VoxelType.texSize;
        vertices[vertexOffset++] = voxelType.rightUV[1];
        return vertexOffset;
    }

    public int createFront (int x, int y, int z, float[] vertices, int vertexOffset) {
        vertices[vertexOffset++] = x;
        vertices[vertexOffset++] = y;
        vertices[vertexOffset++] = z;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = voxelType.frontUV[0]+VoxelType.texSize;
        vertices[vertexOffset++] = voxelType.frontUV[1]+VoxelType.texSize;

        vertices[vertexOffset++] = x + 1;
        vertices[vertexOffset++] = y;
        vertices[vertexOffset++] = z;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = voxelType.frontUV[0];
        vertices[vertexOffset++] = voxelType.frontUV[1]+VoxelType.texSize;

        vertices[vertexOffset++] = x + 1;
        vertices[vertexOffset++] = y + 1;
        vertices[vertexOffset++] = z;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = voxelType.frontUV[0];
        vertices[vertexOffset++] = voxelType.frontUV[1];

        vertices[vertexOffset++] = x;
        vertices[vertexOffset++] = y + 1;
        vertices[vertexOffset++] = z;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = voxelType.frontUV[0]+VoxelType.texSize;
        vertices[vertexOffset++] = voxelType.frontUV[1];
        return vertexOffset;
    }

    public int createBack (int x, int y, int z, float[] vertices, int vertexOffset) {
        vertices[vertexOffset++] = x;
        vertices[vertexOffset++] = y;
        vertices[vertexOffset++] = z + 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = voxelType.backUV[0];
        vertices[vertexOffset++] = voxelType.backUV[1]+VoxelType.texSize;

        vertices[vertexOffset++] = x;
        vertices[vertexOffset++] = y + 1;
        vertices[vertexOffset++] = z + 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = voxelType.backUV[0];
        vertices[vertexOffset++] = voxelType.backUV[1];

        vertices[vertexOffset++] = x + 1;
        vertices[vertexOffset++] = y + 1;
        vertices[vertexOffset++] = z + 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = voxelType.backUV[0]+VoxelType.texSize;
        vertices[vertexOffset++] = voxelType.backUV[1];

        vertices[vertexOffset++] = x + 1;
        vertices[vertexOffset++] = y;
        vertices[vertexOffset++] = z + 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = voxelType.backUV[0]+VoxelType.texSize;
        vertices[vertexOffset++] = voxelType.backUV[1]+VoxelType.texSize;
        return vertexOffset;
    }
}