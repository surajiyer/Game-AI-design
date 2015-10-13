/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import terrain.CubeShader;


/**
 * Adapted from https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/g3d/voxel/Cube.java
 * @author S.S.Iyer
 */
public class Cube extends GameObject {
    
    public static final int NROF_FACES = 6;
    public static final int NROF_VERTICES_PER_FACE = 4;
    public static final int VERTEX_SIZE = 8;
    
    public enum CubeType {
        
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
        
        private CubeType(float[] topUV, float[] bottomUV, float[] leftUV,
            float[] rightUV, float[] frontUV, float[] backUV) {
            this.topUV = topUV;
            this.bottomUV = bottomUV;
            this.leftUV = leftUV;
            this.rightUV = rightUV;
            this.frontUV = frontUV;
            this.backUV = backUV;
        }
    }
    
    public static Texture texture = null;
    private Shader shader;
    private Mesh mesh;
    public final CubeType cubeType;
    public final Matrix4 worldTrans;
    private boolean isActive;
    
    public Cube(CubeType type) {
        this(type, Vector3.Zero, 1f);
    }
    
    public Cube(CubeType type, final Vector3 pos) {
        this(type, pos, 1f);
    }
    
    public Cube (final CubeType type, final Vector3 pos, final float scl) {
        mesh = null;
        cubeType = type;
        isActive = false;
        worldTrans = new Matrix4();
        setPosition(pos);
        setScale(scl);
        shader = new CubeShader();
        shader.init();
    }

    /** 
     * Creates a cube mesh
     * @return the mesh for chaining
     */
    public Mesh generate () {
        float[] vertices = new float[NROF_VERTICES_PER_FACE * VERTEX_SIZE];
        short[] indices = new short[6];
        MeshBuilder meshBuilder = new MeshBuilder();
        meshBuilder.begin(Usage.Position | Usage.Normal | Usage.TextureCoordinates);
        createTop(meshBuilder, vertices, indices);
        createBottom(meshBuilder, vertices, indices);
        createLeft(meshBuilder, vertices, indices);
        createRight(meshBuilder, vertices, indices);
        createFront(meshBuilder, vertices, indices);
        createBack(meshBuilder, vertices, indices);
        mesh = meshBuilder.end();
        calculateDimensions();
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
        //renderable.shader = shader;
        renderables.add(renderable);
    }
    
    /** @return if the cube is generated or not */
    public boolean isGenerated() {
        return mesh != null;
    }
    
    public void regenerate() {
        mesh = null;
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
    protected void calculateDimensions() {
        if(isGenerated()) {
            mesh.calculateBoundingBox(bounds);
            super.calculateDimensions();
        }
    }

    @Override
    public boolean isVisible(Camera cam) {
        worldTrans.getTranslation(tmp);
        tmp.add(center);
        isActive = cam.frustum.sphereInFrustum(tmp, radius);
        return isActive;
    }
    
    /** Create the top face of the cube */
    private void createTop (MeshBuilder meshBuilder, float[] vertices, short[] indices) {
        int vertexOffset = 0;
        
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = cubeType.topUV[0];
        vertices[vertexOffset++] = cubeType.topUV[1];

        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = cubeType.topUV[0]+CubeType.texSize;
        vertices[vertexOffset++] = cubeType.topUV[1];

        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = cubeType.topUV[0]+CubeType.texSize;
        vertices[vertexOffset++] = cubeType.topUV[1]+CubeType.texSize;

        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = cubeType.topUV[0];
        vertices[vertexOffset++] = cubeType.topUV[1]+CubeType.texSize;
        
        indices[0] = (short)(0);
        indices[1] = (short)(1);
        indices[2] = (short)(2);
        indices[3] = (short)(2);
        indices[4] = (short)(3);
        indices[5] = (short)(0);
        
        Mesh mesh = new Mesh(true, vertexOffset / VERTEX_SIZE, indices.length, 
                meshBuilder.getAttributes());
        mesh.setVertices(vertices);
        mesh.setIndices(indices);
        meshBuilder.addMesh(mesh);
    }
    
    /** Create the bottom face of the cube */
    private void createBottom (MeshBuilder meshBuilder, float[] vertices, short[] indices) {
        int vertexOffset = 0;
        
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = cubeType.bottomUV[0];
        vertices[vertexOffset++] = cubeType.bottomUV[1];

        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = cubeType.bottomUV[0]+CubeType.texSize;
        vertices[vertexOffset++] = cubeType.bottomUV[1];

        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = cubeType.bottomUV[0]+CubeType.texSize;
        vertices[vertexOffset++] = cubeType.bottomUV[1]+CubeType.texSize;

        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = cubeType.bottomUV[0];
        vertices[vertexOffset++] = cubeType.bottomUV[1]+CubeType.texSize;
        
        indices[0] = (short)(0);
        indices[1] = (short)(1);
        indices[2] = (short)(2);
        indices[3] = (short)(2);
        indices[4] = (short)(3);
        indices[5] = (short)(0);
        
        Mesh mesh = new Mesh(true, vertexOffset / VERTEX_SIZE, indices.length, 
                meshBuilder.getAttributes());
        mesh.setVertices(vertices);
        mesh.setIndices(indices);
        meshBuilder.addMesh(mesh);
    }
    
    /** Create the left face of the cube */
    private void createLeft (MeshBuilder meshBuilder, float[] vertices, short[] indices) {
        int vertexOffset = 0;
        
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = cubeType.leftUV[0];
        vertices[vertexOffset++] = cubeType.leftUV[1]+CubeType.texSize;

        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = cubeType.leftUV[0];
        vertices[vertexOffset++] = cubeType.leftUV[1];

        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = cubeType.leftUV[0]+CubeType.texSize;
        vertices[vertexOffset++] = cubeType.leftUV[1];

        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = cubeType.leftUV[0]+CubeType.texSize;
        vertices[vertexOffset++] = cubeType.leftUV[1]+CubeType.texSize;
        
        indices[0] = (short)(0);
        indices[1] = (short)(1);
        indices[2] = (short)(2);
        indices[3] = (short)(2);
        indices[4] = (short)(3);
        indices[5] = (short)(0);
        
        Mesh mesh = new Mesh(true, vertexOffset / VERTEX_SIZE, indices.length, 
                meshBuilder.getAttributes());
        mesh.setVertices(vertices);
        mesh.setIndices(indices);
        meshBuilder.addMesh(mesh);
    }
    
    /** Create the right face of the cube */
    private void createRight (MeshBuilder meshBuilder, float[] vertices, short[] indices) {
        int vertexOffset = 0;
        
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = cubeType.rightUV[0]+CubeType.texSize;
        vertices[vertexOffset++] = cubeType.rightUV[1]+CubeType.texSize;

        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = cubeType.rightUV[0];
        vertices[vertexOffset++] = cubeType.rightUV[1]+CubeType.texSize;

        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = cubeType.rightUV[0];
        vertices[vertexOffset++] = cubeType.rightUV[1];

        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = cubeType.rightUV[0]+CubeType.texSize;
        vertices[vertexOffset++] = cubeType.rightUV[1];
        
        indices[0] = (short)(0);
        indices[1] = (short)(1);
        indices[2] = (short)(2);
        indices[3] = (short)(2);
        indices[4] = (short)(3);
        indices[5] = (short)(0);
        
        Mesh mesh = new Mesh(true, vertexOffset / VERTEX_SIZE, indices.length, 
                meshBuilder.getAttributes());
        mesh.setVertices(vertices);
        mesh.setIndices(indices);
        meshBuilder.addMesh(mesh);
    }
    
    /** Create the front face of the cube */
    private void createFront (MeshBuilder meshBuilder, float[] vertices, short[] indices) {
        int vertexOffset = 0;
        
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = cubeType.frontUV[0]+CubeType.texSize;
        vertices[vertexOffset++] = cubeType.frontUV[1]+CubeType.texSize;

        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = cubeType.frontUV[0];
        vertices[vertexOffset++] = cubeType.frontUV[1]+CubeType.texSize;

        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = cubeType.frontUV[0];
        vertices[vertexOffset++] = cubeType.frontUV[1];

        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = cubeType.frontUV[0]+CubeType.texSize;
        vertices[vertexOffset++] = cubeType.frontUV[1];
        
        indices[0] = (short)(0);
        indices[1] = (short)(1);
        indices[2] = (short)(2);
        indices[3] = (short)(2);
        indices[4] = (short)(3);
        indices[5] = (short)(0);
        
        Mesh mesh = new Mesh(true, vertexOffset / VERTEX_SIZE, indices.length, 
                meshBuilder.getAttributes());
        mesh.setVertices(vertices);
        mesh.setIndices(indices);
        meshBuilder.addMesh(mesh);
    }
    
    /** Create the bottom face of the cube */
    private void createBack (MeshBuilder meshBuilder, float[] vertices, short[] indices) {
        int vertexOffset = 0;
        
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = cubeType.backUV[0];
        vertices[vertexOffset++] = cubeType.backUV[1]+CubeType.texSize;

        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = cubeType.backUV[0];
        vertices[vertexOffset++] = cubeType.backUV[1];

        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = cubeType.backUV[0]+CubeType.texSize;
        vertices[vertexOffset++] = cubeType.backUV[1];

        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 1;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = 0;
        vertices[vertexOffset++] = -1;
        vertices[vertexOffset++] = cubeType.backUV[0]+CubeType.texSize;
        vertices[vertexOffset++] = cubeType.backUV[1]+CubeType.texSize;
        
        indices[0] = (short)(0);
        indices[1] = (short)(1);
        indices[2] = (short)(2);
        indices[3] = (short)(2);
        indices[4] = (short)(3);
        indices[5] = (short)(0);
        
        Mesh mesh = new Mesh(true, vertexOffset / VERTEX_SIZE, indices.length, 
                meshBuilder.getAttributes());
        mesh.setVertices(vertices);
        mesh.setIndices(indices);
        meshBuilder.addMesh(mesh);
    }
}