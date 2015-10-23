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
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Pool;
import mechanics.GlobalState;
import static mechanics.GlobalState.UNITS_PER_METER;
import utils.GameObject;


/**
 * Adapted from https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/g3d/voxel/VoxelChunk.java
 * But heavily modified
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
    
    public static final short[] indices;
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
    
    static {
        // Load the indices of the voxel chunks
        indices = new short[CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z * NROF_FACES * 2];
        int i, j=0;
        for (i = 0; i < indices.length; i += 6, j += 4) {
            indices[i + 0] = (short)(j + 0);
            indices[i + 1] = (short)(j + 1);
            indices[i + 2] = (short)(j + 2);
            indices[i + 3] = (short)(j + 2);
            indices[i + 4] = (short)(j + 3);
            indices[i + 5] = (short)(j + 0);
        }
    }
    
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
        this.localTrans = new Matrix4();
        this.mesh = new Mesh(true, CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z * NROF_FACES 
                * NROF_VERTICES_PER_FACE, indices.length, VertexAttribute.Position(), 
                VertexAttribute.Normal(), VertexAttribute.TexCoords(0));
        this.mesh.setIndices(indices);
        this.regenerate = true;
    }
    
    public Mesh generate() {
        mesh.setVertices(calculateVertices(), 0, numVerts * VERTEX_SIZE);
        if(numVerts != 0) calculateBounds();
        regenerate = false;
        return mesh;
    }
    
    /** @param height the height of the cube
     * @return the type of the cube */
    public VoxelType getCubeType(float height) {
        int random = MathUtils.random(6) - 3;
        if(height < 10)
            return VoxelType.WATER;
        else if(height < 13)
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
    public void calculateBounds() {
        mesh.calculateBoundingBox(bounds);
        super.calculateBounds();
    }
    
    @Override
    public void setPosition(Vector3 pos) {
        position.set(pos).scl(width, height, depth);
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
        renderable.material = parent.voxelTextures == null ? material : 
                new Material(TextureAttribute.createDiffuse(parent.voxelTextures));
        renderable.mesh = mesh;
        renderable.meshPartOffset = 0;
        renderable.meshPartSize = numVerts / NROF_VERTICES_PER_FACE * 6;
        if(GlobalState.enableWireframe) {
            renderable.primitiveType = GL20.GL_LINE_STRIP;
        } else {
            renderable.primitiveType = GL20.GL_TRIANGLES;
        }
        renderables.add(renderable);
    }
    
    /** 
     * Creates a mesh out of the chunk, returning the number of indices produced
     * @return the number of vertices produced 
     */
    public float[] calculateVertices () {
        FloatArray vertices = new FloatArray(CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z 
                * VERTEX_SIZE);
        int i = 0;
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < depth; z++) {
                for (int x = 0; x < width; x++, i++) {
                    
                    if (voxels[i] == 0) continue;
                    voxelType = getCubeType(position.y+y);
                    
                    if (y >= height - 1 || voxels[i + topOffset] == 0) {
                        createTop(x, y, z, vertices);
                    }
                    
                    if (y <= 0 || voxels[i + bottomOffset] == 0) {
                        createBottom(x, y, z, vertices);
                    }
                    
                    if (x <= 0 || voxels[i + leftOffset] == 0) {
                        createLeft(x, y, z, vertices);
                    }
                    
                    if (x >= width - 1 || voxels[i + rightOffset] == 0) {
                        createRight(x, y, z, vertices);
                    }
                    
                    if (z <= 0 || voxels[i + frontOffset] == 0) {
                        createFront(x, y, z, vertices);
                    }
                    
                    if (z >= depth - 1 || voxels[i + backOffset] == 0) {
                        createBack(x, y, z, vertices);
                    }
                }
            }
        }
        
        numVerts = (vertices.size / VERTEX_SIZE);
        return vertices.toArray();
    }
    
    public void createTop (int x, int y, int z, FloatArray vertices) {
        vertices.add(x);
        vertices.add(y + 1);
        vertices.add(z);
        vertices.add(0);
        vertices.add(1);
        vertices.add(0);
        vertices.add(voxelType.topUV[0]);
        vertices.add(voxelType.topUV[1]);

        vertices.add(x + 1);
        vertices.add(y + 1);
        vertices.add(z);
        vertices.add(0);
        vertices.add(1);
        vertices.add(0);
        vertices.add(voxelType.topUV[0]+VoxelType.texSize);
        vertices.add(voxelType.topUV[1]);

        vertices.add(x + 1);
        vertices.add(y + 1);
        vertices.add(z + 1);
        vertices.add(0);
        vertices.add(1);
        vertices.add(0);
        vertices.add(voxelType.topUV[0]+VoxelType.texSize);
        vertices.add(voxelType.topUV[1]+VoxelType.texSize);

        vertices.add(x);
        vertices.add(y + 1);
        vertices.add(z + 1);
        vertices.add(0);
        vertices.add(1);
        vertices.add(0);
        vertices.add(voxelType.topUV[0]);
        vertices.add(voxelType.topUV[1]+VoxelType.texSize);
    }

    public void createBottom (int x, int y, int z, FloatArray vertices) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(z);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(voxelType.bottomUV[0]);
        vertices.add(voxelType.bottomUV[1]);

        vertices.add(x);
        vertices.add(y);
        vertices.add(z + 1);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(voxelType.bottomUV[0]+VoxelType.texSize);
        vertices.add(voxelType.bottomUV[1]);

        vertices.add(x + 1);
        vertices.add(y);
        vertices.add(z + 1);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(voxelType.bottomUV[0]+VoxelType.texSize);
        vertices.add(voxelType.bottomUV[1]+VoxelType.texSize);

        vertices.add(x + 1);
        vertices.add(y);
        vertices.add(z);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(voxelType.bottomUV[0]);
        vertices.add(voxelType.bottomUV[1]+VoxelType.texSize);
    }

    public void createLeft (int x, int y, int z, FloatArray vertices) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(z);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(voxelType.leftUV[0]);
        vertices.add(voxelType.leftUV[1]+VoxelType.texSize);

        vertices.add(x);
        vertices.add(y + 1);
        vertices.add(z);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(voxelType.leftUV[0]);
        vertices.add(voxelType.leftUV[1]);

        vertices.add(x);
        vertices.add(y + 1);
        vertices.add(z + 1);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(voxelType.leftUV[0]+VoxelType.texSize);
        vertices.add(voxelType.leftUV[1]);

        vertices.add(x);
        vertices.add(y);
        vertices.add(z + 1);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(voxelType.leftUV[0]+VoxelType.texSize);
        vertices.add(voxelType.leftUV[1]+VoxelType.texSize);
    }

    public void createRight (int x, int y, int z, FloatArray vertices) {
        vertices.add(x + 1);
        vertices.add(y);
        vertices.add(z);
        vertices.add(1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(voxelType.rightUV[0]+VoxelType.texSize);
        vertices.add(voxelType.rightUV[1]+VoxelType.texSize);

        vertices.add(x + 1);
        vertices.add(y);
        vertices.add(z + 1);
        vertices.add(1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(voxelType.rightUV[0]);
        vertices.add(voxelType.rightUV[1]+VoxelType.texSize);

        vertices.add(x + 1);
        vertices.add(y + 1);
        vertices.add(z + 1);
        vertices.add(1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(voxelType.rightUV[0]);
        vertices.add(voxelType.rightUV[1]);

        vertices.add(x + 1);
        vertices.add(y + 1);
        vertices.add(z);
        vertices.add(1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(voxelType.rightUV[0]+VoxelType.texSize);
        vertices.add(voxelType.rightUV[1]);
    }

    public void createFront (int x, int y, int z, FloatArray vertices) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(z);
        vertices.add(0);
        vertices.add(0);
        vertices.add(1);
        vertices.add(voxelType.frontUV[0]+VoxelType.texSize);
        vertices.add(voxelType.frontUV[1]+VoxelType.texSize);

        vertices.add(x + 1);
        vertices.add(y);
        vertices.add(z);
        vertices.add(0);
        vertices.add(0);
        vertices.add(1);
        vertices.add(voxelType.frontUV[0]);
        vertices.add(voxelType.frontUV[1]+VoxelType.texSize);

        vertices.add(x + 1);
        vertices.add(y + 1);
        vertices.add(z);
        vertices.add(0);
        vertices.add(0);
        vertices.add(1);
        vertices.add(voxelType.frontUV[0]);
        vertices.add(voxelType.frontUV[1]);

        vertices.add(x);
        vertices.add(y + 1);
        vertices.add(z);
        vertices.add(0);
        vertices.add(0);
        vertices.add(1);
        vertices.add(voxelType.frontUV[0]+VoxelType.texSize);
        vertices.add(voxelType.frontUV[1]);
    }

    public void createBack (int x, int y, int z, FloatArray vertices) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(z + 1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(voxelType.backUV[0]);
        vertices.add(voxelType.backUV[1]+VoxelType.texSize);
        
        vertices.add(x);
        vertices.add(y + 1);
        vertices.add(z + 1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(voxelType.backUV[0]);
        vertices.add(voxelType.backUV[1]);
        
        vertices.add(x + 1);
        vertices.add(y + 1);
        vertices.add(z + 1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(voxelType.backUV[0]+VoxelType.texSize);
        vertices.add(voxelType.backUV[1]);
        
        vertices.add(x + 1);
        vertices.add(y);
        vertices.add(z + 1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(voxelType.backUV[0]+VoxelType.texSize);
        vertices.add(voxelType.backUV[1]+VoxelType.texSize);
    }
}