/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terrain;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import mechanics.GlobalState;
import static mechanics.GlobalState.UNITS_PER_METER;
import static mechanics.GlobalState.assetsManager;
import static terrain.VoxelChunk.CHUNK_SIZE_X;
import static terrain.VoxelChunk.CHUNK_SIZE_Y;
import static terrain.VoxelChunk.CHUNK_SIZE_Z;
import static terrain.VoxelChunk.indices;
import utils.ConcreteGameObject;
import utils.GameObject;

/**
 * Adapted from https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/g3d/voxel/VoxelWorld.java
 * @author S.S.Iyer
 */
public class VoxelWorld extends GameObject {
    public final VoxelChunk[] chunks;
    public final Texture voxelTextures;
    public final int chunksX;
    public final int chunksY;
    public final int chunksZ;
    public final int voxelsX;
    public final int voxelsY;
    public final int voxelsZ;
    public int renderedChunks;
    public int numChunks;
    
    Array<ConcreteGameObject> trees;
    public final int NROF_TREES;
    public final int NROF_TREES_TYPES;

    public VoxelWorld (Texture texture, int chunksX, int chunksY, int chunksZ, int numberOfTrees,
            int numberOfTreeTypes) {
        this.voxelTextures = texture;
        this.chunksX = chunksX;
        this.chunksY = chunksY;
        this.chunksZ = chunksZ;
        this.numChunks = chunksX * chunksY * chunksZ;
        this.chunks = new VoxelChunk[numChunks];
        this.voxelsX = chunksX * CHUNK_SIZE_X;
        this.voxelsY = chunksY * CHUNK_SIZE_Y;
        this.voxelsZ = chunksZ * CHUNK_SIZE_Z;
        
        // Create the chunk objects (does not actually generate them yet)
        int i = 0;
        for (int y = 0; y < chunksY; y++) {
            for (int z = 0; z < chunksZ; z++) {
                for (int x = 0; x < chunksX; x++) {
                    VoxelChunk chunk = new VoxelChunk(this, CHUNK_SIZE_X, CHUNK_SIZE_Y, CHUNK_SIZE_Z);
                    chunk.setPosition(tmp.set(x, y, z));
                    chunks[i++] = chunk;
                }
            }
        }
        
        // Load the indices of the voxel chunks
        short j=0;
        for (i = 0; i < indices.length; i += 6, j += 4) {
            indices[i + 0] = (short)(j + 0);
            indices[i + 1] = (short)(j + 1);
            indices[i + 2] = (short)(j + 2);
            indices[i + 3] = (short)(j + 2);
            indices[i + 4] = (short)(j + 3);
            indices[i + 5] = (short)(j + 0);
        }
        
        this.NROF_TREES = numberOfTrees;
        this.NROF_TREES_TYPES = numberOfTreeTypes;
        this.trees = new Array<>();
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

    public float getHeight (float x, float z) {
        int ix = (int) x/(int)scale;
        int iz = (int) z/(int)scale;
        if (ix < 0 || ix >= voxelsX) return 0;
        if (iz < 0 || iz >= voxelsZ) return 0;
        // FIXME optimize
        for (int y = voxelsY - 1; y > 0; y--) {
            if (get(ix, y, iz) > 0) return (y + 1)*scale;
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
    
    public void loadTrees() {
        float x, y, z;
        int type;
        ConcreteGameObject tmpObject;
        for (int i = 0; i < NROF_TREES; i++) {
            x = MathUtils.random(GlobalState.widthField)*scale;
            z = MathUtils.random(GlobalState.depthField)*scale;
            y = (int) getHeight(x, z);
            type = MathUtils.random(1, NROF_TREES_TYPES);
            if (y < 18) continue;
            tmpObject = new ConcreteGameObject(
                    assetsManager.get("trees/tree"+type+".g3db", Model.class));
            tmpObject.setPosition(tmp.set(x, y, z));
            trees.add(tmpObject);
        }
    }
    
    @Override
    public void setScale(float scl) {
        scale = scl / scale;
        worldTrans.scl(scale);
        scale = scl;
    }
    
    @Override
    public void setPosition(Vector3 pos) {
        position.set(pos);
        worldTrans.setTranslation(position);
    }

    @Override
    public boolean isVisible(Camera cam) {
        for (VoxelChunk chunk : chunks) {
            if (chunk.isVisible(cam)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    protected void calculateBounds() {
        // FIXME
//        if(isGenerated()) {
//            model.calculateBoundingBox(bounds);
//            super.calculateBounds();
//        }
    }
    
    @Override
    public void getRenderables (Array<Renderable> renderables, Pool<Renderable> pool) {
        renderedChunks = 0;
        for (VoxelChunk chunk : chunks) {
            if(!chunk.isGenerated()) {
                chunk.generate();
            }
            if(chunk.numVerts != 0) {
                chunk.getRenderables(renderables, pool);
                renderedChunks++;
            }
        }
        
        // Render trees
        for(ConcreteGameObject tree : trees) {
            tree.getRenderables(renderables, pool);
        }
    }
}