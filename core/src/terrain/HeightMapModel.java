/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import java.util.LinkedList;
import utils.Triangle;

/**
 *
 * @author S.S.Iyer
 */
public class HeightMapModel {
    public HeightMap heightMap;
    private Array<Model> models = new Array<>();
    private Texture groundTexture;
    private Material groundMat;
    Color specular = new Color(0.1f, 0.1f, 0.1f, 1f);
    public ObjectMap<GroundChunk, Array<Triangle>> groundTriangles = new ObjectMap<>();
    public static int timesCreated = 0;  // to stop creating more than once

    public GroundChunk ground;
    
    public HeightMapModel(HeightMap hm) {
        this.heightMap = hm;
        String groundTexName = "textures/hm_paint.png";
        groundTexture = new Texture(Gdx.files.internal(groundTexName), true);
        groundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        groundTexture.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Linear);
        groundMat = new Material(TextureAttribute.createDiffuse(groundTexture), 
                ColorAttribute.createSpecular(specular));
        createGround();
    }

    private void createGround() {
        if (++timesCreated > 1) {
            throw new GdxRuntimeException("can't create heightmap more than one time");
        }
        vertexInfos = new MeshPartBuilder.VertexInfo[heightMap.heights.length][heightMap.heights[0].length];
        int trisHeight = (heightMap.getWidth() - 1);
        int trisWidth = (heightMap.getWidth() - 1) * 2;
        System.out.printf("tris zHeight,xWidth: %d, %d\n", trisHeight, trisWidth);
        tris = new Triangle[trisHeight][trisWidth];
        setVertexPositions(); // iterate through height map points, setting world coordinates
        buildTriangles(); // abstraction of the triangles that create the mesh
        // useful for calculating vertex normals, since each triangle stores a face normal
        // but somewhat wasteful of memory
        // TODO: optimize
        calculateVertexNormals(); // calculate vertex normals for per-vertex lighting
        final int chunkSize = 32;
        int z = 0;
        int zRemain = (int) Math.ceil(heightMap.getDepth() / chunkSize);
        if (zRemain == 0) zRemain = 1;
        int baseXRemain = (int) Math.ceil(heightMap.getWidth() / chunkSize);
        if (baseXRemain == 0) baseXRemain = 1;
        System.out.println("z chunks: " + zRemain + ", x chunks: " + baseXRemain);
        while (zRemain > 0) {
            int xRemain = baseXRemain;
            int x = 0;
            while (xRemain > 0) {
                buildGroundModels(x, z, chunkSize);
                xRemain--;
                x += chunkSize*2;
            }
            zRemain--;
            z += chunkSize;
        }
    }

    private void buildGroundModels(final int startX, final int startZ, final int chunkSize) {
        LinkedList<MeshPartBuilder.VertexInfo> vertexQueue = new LinkedList<>();
        MeshBuilder meshb = new MeshBuilder();
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.Normal;
        float baseZ = startZ * heightMap.getWidthScale();
        float baseX = startX * heightMap.getWidthScale();
        ModelBuilder builder = new ModelBuilder();
        int zCols = 0;
        int xRows = 0;
        int lastXRowsCount = -1;
        int count = 0;
        builder.begin();
        Material material;
        Array<Triangle> triangles = new Array<>();
        meshb.begin(attributes);
        boolean addedFirst = false;
        for (int z = startZ; z < tris.length && z - startZ < chunkSize; z++) {
            zCols++;
            xRows = 0;
            // TRIANGLE_STRIP draws likes this /\/\/\/\/\/\/\
            // so we cap it with a first and last vertex to make rect shaped like this |/\/\/\/\|
            // first vertex
            // start the row
            Triangle top = null;
            Triangle bottom = null;
            top = tris[z][startX];
            if (vertexQueue.size() > 0) {
                // start with a degenerate triangle to jump to a new row
                MeshPartBuilder.VertexInfo last = vertexQueue.peekLast();
                vertexQueue.addLast(vertexQueue.getLast());
                //vertexQueue.add(top.a);
            }
            //vertexQueue.add(top.a);
            //vertexQueue.add(top.b);
            //meshb.index(meshb.vertex(top.a));
            //meshb.index(meshb.vertex(top.b));
            for (int x = startX; x < tris[0].length && x - startX < chunkSize * 2; x += 2) {
                xRows++;

                top = tris[z][x];
                bottom = tris[z][x + 1];
                // add triangles to be associated with gameobject, this is for pickray and getting mouse to world coords
                if (!triangles.contains(top, true)) {
                    triangles.add(top);
                }
                if (!triangles.contains(bottom, true)) {
                    triangles.add(bottom);
                }
                //vertexQueue.addLast(top.c);
                //vertexQueue.addLast(bottom.a);
                //meshb.index(meshb.vertex(top.c));
                //meshb.index(meshb.vertex(bottom.a));
            }
            if (xRows != lastXRowsCount && lastXRowsCount != -1) {
                throw new GdxRuntimeException("x row count discrepancy, this row: " + xRows + " , last: " + lastXRowsCount);
            }

            lastXRowsCount = xRows;

        }
        MeshPartBuilder.VertexInfo last = null;
        while (!vertexQueue.isEmpty()) {
            MeshPartBuilder.VertexInfo vert = vertexQueue.removeFirst();
            if (last != null && vert.position.equals(last.position)) {
                // degenerate triangles for TRIANGLE_STRIP will make many dupes, that is fine
                //System.out.println("duplicate: " + Tools.fmt(vert.position));
            }
            last = vert;
            meshb.index(meshb.vertex(vert));
        }
        Mesh mesh = meshb.end();
        material = groundMat;
        builder.part(Integer.toString(count), mesh, GL20.GL_TRIANGLE_STRIP, material);
        Model model = builder.end();
        //Model model = builder.createFromMesh(mesh, GL20.GL_TRIANGLES, groundMat);
        //Material myMat = new Material(ColorAttribute.createDiffuse(randColor()));
        models.add(model);
        GroundChunk inst = new GroundChunk(model);
        ground = inst;

        groundTriangles.put(inst, triangles);
        //inst.transform.setTranslation(baseX, 0f, baseZ);
        /*Tools.print(inst.center, "center");
        Tools.print(inst.dimensions, "dimensions");*/
        System.out.printf("built chunk model (%d meshes): numVerts: %d, vertexSize: %d, numIndices: %d\n",
                          model.meshes.size, model.meshes.first().getNumVertices(), model.meshes.first().getVertexSize(), model.meshes.first().getNumIndices());
    }

    private void buildTriangles() {
        //System.out.printf("chunk: startXZ: %d, %d -- length: %d\n", startX, startZ, length);
        int count = 0;
        MeshPartBuilder.VertexInfo right;
        MeshPartBuilder.VertexInfo down;
        MeshPartBuilder.VertexInfo downRight;
        for (int z = 0; z < vertexInfos.length - 1; z++) {
            for (int x = 0; x < (vertexInfos[0].length - 1); x++) {
                MeshPartBuilder.VertexInfo vert = vertexInfos[z][x];
                right = vertexInfos[z][x+1];
                down = vertexInfos[z+1][x];
                downRight = vertexInfos[z+1][x+1];
                // each z row has two trianagles forming a rect
                int xIdx = x*2;
                Triangle top = new Triangle(vert, down, right);
                Triangle bottom = new Triangle(downRight, right, down);
                count += 2;
            }
        }
        System.out.println("built " + count + " triangles");
    }

    public Triangle getTriangleAtWorldCoords(float x, float z) {
        float y = heightMap.getInterpolatedHeight(x, z);
        int ix = (int) (x * 2 / heightMap.getWidthScale());
        int iz = (int) (z / heightMap.getWidthScale());
        if (iz > tris.length -1) iz = tris.length - 1;
        if (ix > tris[0].length - 1) ix = tris[0].length - 1;
        Triangle tri = null;
        try {
            tri = tris[iz][ix];
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.printf("ix: %d, iz: %d\n", ix, iz);
            System.exit(1);
        }
        tri.getCenter(tmp);
        tmp.y += 10f;
        // for debug
        //mouseMarker.transform.setToTranslation(tmp);
        //System.out.println(tri);
        return tri;

    }

    private Triangle[] neighborStorage = new Triangle[4];

    private Triangle[] getTrianglesNeighboringVertex(int baseZ, int baseX) {
        Triangle[] neighbors = neighborStorage;
        // base triangle index
        int x = baseX * 2;
        int z = baseZ;
        int maxZ = tris.length - 1;
        int maxX = tris[0].length - 1;
        int slot = 0;
        // bottom row
        for (int i = -1; i <= 0; i++) {
            x--;
            if (z < 0 || x < 0 || z > maxZ || x > maxX) {
                neighbors[slot++] = null;
            } else {
                neighbors[slot++] = tris[z][x];
            }
        }
        // top row
        z -= 1;
        for (int i = -1; i <= 0; i++) {
            x = baseX - i;
            if (z < 0 || x < 0 || z > maxZ || x > maxX) {
                neighbors[slot++] = null;
            } else {
                neighbors[slot++] = tris[z][x];
            }
        }
        return neighbors;
    }

    private void setVertexPositions() {
        // store vertex info for second pass where we calculate vertex normals for per-vertex lighting
        float[][] pts = heightMap.heights;
        float blockSize = heightMap.getWidthScale();
        float heightScale = heightMap.getHeightScale();
        float width = heightMap.getWidthWorld();
        float depth = heightMap.getDepthWorld();
        for (int z = 0; z < pts.length; z++) {
            for (int x = 0; x < pts[z].length; x++) {
                float y = pts[z][x];
                MeshPartBuilder.VertexInfo thisVert = new MeshPartBuilder.VertexInfo();
                thisVert.setPos(x * blockSize, y * heightScale, z * blockSize);
                // set texture UV
                float u = (x * blockSize) / width;
                float v = (z * blockSize) / depth;
                float scl = heightScale;
                scl = 1f;
                thisVert.setUV(u * scl, v * scl);
                vertexInfos[z][x] = thisVert;
            }
        }
    }

    /** Return vertex normal based on average of surrounding triangle face normals */
    private Vector3 calculateNormalForVertex(int z, int x) {
        tmp.set(0f, 0f, 0f);
        Triangle[] neighbors = getTrianglesNeighboringVertex(z, x);
        int triCount = 0;
        for (int i = 0; i < neighbors.length; i++) {
            Triangle tri = neighbors[i];
            if (tri != null) {
                triCount++;
                tmp.add(tri.faceNormal);
            }
        }
        tmp.scl(1f / triCount);
        return tmp;
    }

    private void calculateVertexNormals() {
        int count = 0;
        for (int z = 0; z < vertexInfos.length; z++) {
            for (int x = 0; x < vertexInfos[z].length; x++) {
                MeshPartBuilder.VertexInfo vert = vertexInfos[z][x];
                // calculate normals
                vert.setNor(calculateNormalForVertex(z, x));

            }
        }
        System.out.print("vertex count: " + count + ", ");

    }

    // TODO
    /*
    public MeshPartBuilder.VertexInfo[] getVertexInfoSquareFromPos(float xf, float zf) {
    	int x = MathUtils.floor(xf / Main.heightMapModel.heightMap.getHeightScale());
    	int z = MathUtils.floor(zf / Main.heightMapModel.heightMap.getWidthScale());
    	squareVerts[0] = vertexInfos[z][x];
    	squareVerts[1] = vertexInfos[z+1][x];
    	squareVerts[2] = vertexInfos[z+1][x+1];
    	squareVerts[3] = vertexInfos[z][x+1];
    	return squareVerts;
    }
    /
    public GroundChunk getGroundChunkAtPoint(int screenX, int screenY) {
    	Ray ray = Main.view.cam.getPickRay(screenX, screenY);
    	int result = -1;
    	float distance = -1;
    	for (int i = 0; i < Lists.groundChunks.size; ++i) {
    		final GroundChunk chunk = Lists.groundChunks.get(i);
    		float dist2 = ray.origin.dst2(chunk.position);
    		if (distance >= 0f && dist2 > distance) continue;
    		if (Intersector.intersectRaySphere(ray, chunk.position, chunk.radius, null)) {
    			result = i;
    			distance = dist2;
    		}
    	}
    	if (result == -1) return null;
    	return Lists.groundChunks.get(result);
    }
    */

    MeshPartBuilder.VertexInfo[][] vertexInfos;
    MeshPartBuilder.VertexInfo[] squareVerts = new MeshPartBuilder.VertexInfo[4];
    public Triangle[][] tris;

    public Vector3 getSurfaceNormalFromPos(Vector3 pos) {
        //HeightMapModel.Triangle tri = Main.heightMapModel.getTriangleAtWorldCoords(pos.x, pos.z);
        //System.out.println("got tri at: " + Tools.fmt(tri.getCenter(tmp)));
        //View.touchedTris.add(tri); TODO
        //tmp.set(tri.faceNormal);
        return tmp;
    }

    private static Vector3 tmp = new Vector3();
    private static Vector3 tmp2 = new Vector3();
}