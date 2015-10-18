/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terrain;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import utils.Cube;
import utils.Cube.CubeType;
import static utils.Cube.texture;
import utils.GameObject;

/**
 *
 * @author S.S.Iyer
 */
public class Chunk extends GameObject {
    
    public static final int CHUNK_SIZE = 16;
    
    //private final Cube[] cubes;
    private Mesh mesh;
    public boolean isActive;
    
    public Chunk() {
        this(Vector3.Zero);
    }
    
    public Chunk(final Vector3 pos) {
        //cubes = new Cube[CHUNK_SIZE*CHUNK_SIZE*CHUNK_SIZE];
        mesh = null;
        position.set(pos);
        worldTrans.setTranslation(position);
    }
    
    /** @param height the height of the cube
     * @return the type of the cube */
    public CubeType getCubeType(int height) {
        int random = MathUtils.random(10) - 5;
        if(height < 20)
            return CubeType.WATER;
        else if(height < 40)
            return CubeType.SAND;
        else
            return CubeType.GRASS;
    }
    
    private Mesh generate() {
        Cube cube;
        Mesh tmpMesh;
        MeshBuilder meshBuilder = new MeshBuilder();
        meshBuilder.begin(Usage.Position | Usage.Normal | Usage.TextureCoordinates);
        for(int Y = 0; Y < CHUNK_SIZE; Y++) {
            for(int Z = 0; Z < CHUNK_SIZE; Z++) {
                for(int X = 0; X < CHUNK_SIZE; X++) {
                    cube = new Cube(getCubeType(Y+(int)position.y), tmp.set(X, Y, Z));
                    tmpMesh = cube.generate();
                    tmpMesh.transform(cube.worldTrans);
                    meshBuilder.addMesh(tmpMesh);
                    //cubes[Y*CHUNK_SIZE*CHUNK_SIZE  + Z*CHUNK_SIZE + X] = cube;
                }
            }
        }
        mesh = meshBuilder.end();
        calculateBounds();
        return mesh;
    }
    
    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        Renderable renderable = pool.obtain();
        renderable.worldTransform.set(worldTrans);
        renderable.material = texture == null ? new Material(new ColorAttribute(ColorAttribute.Diffuse, 
                    MathUtils.random(0.5f, 1f),
                    MathUtils.random(0.5f, 1f),
                    MathUtils.random(0.5f, 1f), 1))
                : new Material(TextureAttribute.createDiffuse(texture));
        renderable.mesh = isGenerated() ? mesh : generate();
        renderable.meshPartOffset = 0;
        renderable.meshPartSize = mesh.getNumIndices();
        renderable.primitiveType = GL20.GL_TRIANGLES;
        renderables.add(renderable);
    }
    
    /** @return if the cube is generated or not. */
    public boolean isGenerated() {
        return mesh != null;
    }
    
    public void regenerate() {
        mesh = null;
    }
    
    @Override
    public void setPosition(Vector3 pos) {
        position.set(pos);
        worldTrans.setTranslation(position);
    }
    
    @Override
    public void setScale(float scl) {
        scale = scl / scale;
        worldTrans.scl(scl);
        scale = scl;
    }
    
    @Override
    protected void calculateBounds() {
        mesh.calculateBoundingBox(bounds);
        super.calculateBounds();
    }

    @Override
    public boolean isVisible(Camera cam) {
        worldTrans.getTranslation(tmp);
        tmp.add(center);
        isActive = cam.frustum.sphereInFrustum(tmp, radius);
        return isActive;
    }
}
