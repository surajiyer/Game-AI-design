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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import static main.VoxelTest.UNITS_PER_METER;
import utils.GameController;

/**
 * Adapted from https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/g3d/voxel/VoxelWorld.java
 * @author S.S.Iyer
 */
public class VoxelWorld implements RenderableProvider {
    
    /** Voxel chunk type */
    public enum Items {
        GRASS(0),
        SAND(1),
        STONE(2),
        DIRT(3), 
        WATER(4);
        
        int type;
        
        private Items(int type) {
            this.type = type;
        }
    }
    
    public static final int CHUNK_SIZE_X = UNITS_PER_METER;
    public static final int CHUNK_SIZE_Y = UNITS_PER_METER;
    public static final int CHUNK_SIZE_Z = UNITS_PER_METER;

    public final VoxelChunk[] chunks;
    public final Mesh[] meshes;
    public final Material[] materials;
    public final boolean[] dirty;
    public final int[] numVertices;
    public float[] vertices;
    public final int chunksX;
    public final int chunksY;
    public final int chunksZ;
    public final int voxelsX;
    public final int voxelsY;
    public final int voxelsZ;
    public int renderedChunks;
    public int numChunks;
    
    /** Textures for each face of a voxel */
    private final TextureRegion[][] tiles;
    
    /** The {@link Shader} to be used to render this */
    public boolean useShader = false;
    public Shader shader;

    public VoxelWorld (FileHandle textures, int chunksX, int chunksY, int chunksZ) {
        this.tiles = TextureRegion.split(new Texture(textures), 32, 32);
        this.chunksX = chunksX;
        this.chunksY = chunksY;
        this.chunksZ = chunksZ;
        this.numChunks = chunksX * chunksY * chunksZ;
        this.chunks = new VoxelChunk[numChunks];
        this.voxelsX = chunksX * CHUNK_SIZE_X;
        this.voxelsY = chunksY * CHUNK_SIZE_Y;
        this.voxelsZ = chunksZ * CHUNK_SIZE_Z;
        shader = new VoxelShader();
        shader.init();
        int i = 0;
        for (int y = 0; y < chunksY; y++) {
            for (int z = 0; z < chunksZ; z++) {
                for (int x = 0; x < chunksX; x++) {
                    VoxelChunk chunk = new VoxelChunk(CHUNK_SIZE_X, CHUNK_SIZE_Y, CHUNK_SIZE_Z);
                    chunk.offset.set(x * CHUNK_SIZE_X, y * CHUNK_SIZE_Y, z * CHUNK_SIZE_Z);
                    chunks[i++] = chunk;
                }
            }
        }
        int len = CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z * 6 * 6 / 3;
        short[] indices = new short[len];
        short j = 0;
        for (i = 0; i < len; i += 6, j += 4) {
            indices[i + 0] = (short)(j + 0);
            indices[i + 1] = (short)(j + 1);
            indices[i + 2] = (short)(j + 2);
            indices[i + 3] = (short)(j + 2);
            indices[i + 4] = (short)(j + 3);
            indices[i + 5] = (short)(j + 0);
        }
        
        MathUtils.random.setSeed(0);
        this.vertices = new float[VoxelChunk.VERTEX_SIZE * 6 * CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z];
        this.meshes = new Mesh[chunksX * chunksY * chunksZ];
        this.dirty = new boolean[chunksX * chunksY * chunksZ];
        this.numVertices = new int[chunksX * chunksY * chunksZ];
        this.materials = new Material[chunksX * chunksY * chunksZ];
        for (i = 0; i < meshes.length; i++) {
            meshes[i] = new Mesh(true, CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z * 6 * 4,
                    len, VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0));
            meshes[i].setIndices(indices);
            dirty[i] = true;
            numVertices[i] = 0;
            materials[i] = new Material(new ColorAttribute(ColorAttribute.Diffuse, 
                    MathUtils.random(0.5f, 1f),
                    MathUtils.random(0.5f, 1f),
                    MathUtils.random(0.5f, 1f), 1));
        }
    }

    public void set (float x, float y, float z, byte voxel) {
        int ix = (int)x;
        int iy = (int)y;
        int iz = (int)z;
        int chunkX = ix / CHUNK_SIZE_X;
        if (chunkX < 0 || chunkX >= chunksX) return;
        int chunkY = iy / CHUNK_SIZE_Y;
        if (chunkY < 0 || chunkY >= chunksY) return;
        int chunkZ = iz / CHUNK_SIZE_Z;
        if (chunkZ < 0 || chunkZ >= chunksZ) return;
        chunks[chunkX + chunkZ * chunksX + chunkY * chunksX * chunksZ].set(ix % CHUNK_SIZE_X, iy % CHUNK_SIZE_Y, iz % CHUNK_SIZE_Z,
                voxel);
    }

    public byte get (float x, float y, float z) {
        int ix = (int)x;
        int iy = (int)y;
        int iz = (int)z;
        int chunkX = ix / CHUNK_SIZE_X;
        if (chunkX < 0 || chunkX >= chunksX) return 0;
        int chunkY = iy / CHUNK_SIZE_Y;
        if (chunkY < 0 || chunkY >= chunksY) return 0;
        int chunkZ = iz / CHUNK_SIZE_Z;
        if (chunkZ < 0 || chunkZ >= chunksZ) return 0;
        return chunks[chunkX + chunkZ * chunksX + chunkY * chunksX * chunksZ].get(ix % CHUNK_SIZE_X, iy % CHUNK_SIZE_Y, iz
                % CHUNK_SIZE_Z);
    }

    public float getHighest (float x, float z) {
        int ix = (int)x;
        int iz = (int)z;
        if (ix < 0 || ix >= voxelsX) return 0;
        if (iz < 0 || iz >= voxelsZ) return 0;
        // FIXME optimize
        for (int y = voxelsY - 1; y > 0; y--) {
            if (get(ix, y, iz) > 0) return y + 1;
        }
        return 0;
    }

    public void setColumn (float x, float y, float z, byte voxel) {
        int ix = (int)x;
        int iy = (int)y;
        int iz = (int)z;
        if (ix < 0 || ix >= voxelsX) return;
        if (iy < 0 || iy >= voxelsY) return;
        if (iz < 0 || iz >= voxelsZ) return;
        // FIXME optimize
        for (; iy > 0; iy--) {
            set(ix, iy, iz, voxel);
        }
    }

    public void setVoxelChunk (float x, float y, float z, float width, float height, float depth, byte voxel) {
        int ix = (int)x;
        int iy = (int)y;
        int iz = (int)z;
        int iwidth = (int)width;
        int iheight = (int)height;
        int idepth = (int)depth;
        int startX = Math.max(ix, 0);
        int endX = Math.min(voxelsX, ix + iwidth);
        int startY = Math.max(iy, 0);
        int endY = Math.min(voxelsY, iy + iheight);
        int startZ = Math.max(iz, 0);
        int endZ = Math.min(voxelsZ, iz + idepth);
        // FIXME optimize
        for (iy = startY; iy < endY; iy++) {
            for (iz = startZ; iz < endZ; iz++) {
                for (ix = startX; ix < endX; ix++) {
                    set(ix, iy, iz, voxel);
                }
            }
        }
    }
    Vector3 tmp = new Vector3();

    @Override
    public void getRenderables (Array<Renderable> renderables, Pool<Renderable> pool) {
        renderedChunks = 0;
        for (int i = 0; i < chunks.length; i++) {
            VoxelChunk chunk = chunks[i];
            Mesh mesh = meshes[i];
            if (dirty[i]) {
                int numVerts = chunk.calculateVertices(vertices);
                numVertices[i] = numVerts / 4 * 6;
                mesh.setVertices(vertices, 0, numVerts * VoxelChunk.VERTEX_SIZE);
                //mesh.scale(UNITS_PER_METER, UNITS_PER_METER, UNITS_PER_METER);
                dirty[i] = false;
            }
            if (numVertices[i] == 0) continue;
            Renderable renderable = pool.obtain();
            renderable.worldTransform.idt();
            renderable.material = materials[i];
            renderable.mesh = mesh;
            renderable.meshPartOffset = 0;
            renderable.meshPartSize = numVertices[i];
            if(GameController.enableWireframe) {
                renderable.primitiveType = GL20.GL_LINE_STRIP;
            } else {
                renderable.primitiveType = GL20.GL_TRIANGLES;
            }
            if(useShader) {
                renderable.shader = shader;
            }
            renderables.add(renderable);
            renderedChunks++;
        }
    }
}