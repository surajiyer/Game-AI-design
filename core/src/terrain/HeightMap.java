/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terrain;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 *
 * @author S.S.Iyer
 */
public class HeightMap {
    /** The 2D array of heights for each data point */
    float[][] heights;
    
    /** Computed minimum and maximum height in the heightmap */
    float MIN_HEIGHT, MAX_HEIGHT;
    
    /** Number of points along the x-axis and the z-axis of the height map */
    int WIDTH, DEPTH;

    /**
     * Creates a heightmap with default values
     * @param file - 8-bit PNG file heightmap data
     */
    public HeightMap(FileHandle file) {
        this(file, true, 0);
    }

    /**
     *
     * @param file 8-bit PNG file heightmap data
     * @param whiteHigh height is represented by white (instead of black)
     * @param smoothingPasses
     */
    public HeightMap(FileHandle file, boolean whiteHigh, int smoothingPasses) {
        // Load heightmap file
        Pixmap pix = new Pixmap(file);
        if (pix.getFormat() != Pixmap.Format.Alpha) {
            throw new GdxRuntimeException("Pixmap must be format Pixmap.Alpha (8-bit Grayscale), not: " + pix.getFormat());
        }
        
        // Get width and height of the heightmap
        WIDTH = pix.getWidth();
        DEPTH = pix.getHeight();
        System.out.println("w,h: " + WIDTH + ", " + DEPTH);
        
        // create new heightmap 2d array
        heights = new float[WIDTH][DEPTH];
        
        // load the heightmap into the 2d array
        int height;
        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                height = -1 * pix.getPixel(x, z);
                if (whiteHigh)
                    height = 256 - height;
                heights[z][x] = height/256f;
            }
        }
        
        // Smooth the terrain
        smoothVertexPositions(smoothingPasses);
    }

    /** Set the minimum and maximum height of the heightmap. */
    private void setMinMaxHeights() {
        MIN_HEIGHT = Float.MAX_VALUE;
        MAX_HEIGHT = Float.MIN_VALUE;
        for (float[] height : heights) {
            for (int z = 0; z < height.length; z++) {
                float y = height[z];
                if (y < MIN_HEIGHT) MIN_HEIGHT = y;
                if (y > MAX_HEIGHT) MAX_HEIGHT = y;
            }
        }
    }

    /** 
     * Create smoother terrain using averaging of height values
     * @param passes number of smoothing passes (higher = smoother)
     */
    private void smoothVertexPositions(int passes) {
        for (int i = 0; i < passes; i++) {
            // smooth along x
            for (int x = 0; x < heights.length; x++) {
                for (int z = 1; z < heights[0].length - 1; z++) {
                    float prev = heights[x][z - 1];
                    float y = heights[x][z];
                    float next = heights[x][z + 1];
                    float yAvg = (next + prev) / 2f;
                    heights[x][z] = (y + yAvg) / 2f;
                }
            }
            // smooth along z
            for (int z = 0; z < heights[0].length; z++) {
                for (int x = 1; x < heights.length - 1; x++) {
                    float prev = heights[x - 1][z];
                    float y = heights[x][z];
                    float next = heights[x + 1][z];
                    float yAvg = (next + prev) / 2f;
                    heights[x][z] = (y + yAvg) / 2f;
                }
            }
        }
        setMinMaxHeights();
    }

    public float[][] getData() {
        return heights;
    }

    private Vector3 tmp = new Vector3();
    private Vector3 tmp2 = new Vector3();
    private Vector3 tmp3 = new Vector3();
    private Vector3 tmp4 = new Vector3();

    /** 
     * Get height for single point at (x,z) coordinates. It does not interpolate 
     * using neighbors. Parameters assume heightmap starts at origin (0,0).
     * 
     * @param x
     * @param z
     * @return 
     */
    public float getHeight(int x, int z) {
        if (x < 0 || z < 0 || z >= DEPTH || x >= WIDTH) {
            throw new IllegalArgumentException("Given heightmap coordinate does not exist.");
        }
        return heights[x][z];
    }

    /**
     * Get the interpolated height for (x,z) coordinates, accounting for scale, 
     * interpolated using neighbors. This will give the interpolated height when 
     * the parameters lie somewhere between actual heightmap data points.
     * parameters assume heightmap's origin is at world coordinates (x:0, z:0)
     * @return the scale-adjusted interpolated height at specified world coordinates
     */
//    public float getInterpolatedHeight(float xf, float zf) {
//        Vector3 a = tmp;
//        Vector3 b = tmp2;
//        Vector3 c = tmp3;
//        Vector3 d = tmp4;
//
//        float baseX = (float) Math.floor(xf / WIDTH_SCALE);
//        float baseZ = (float) Math.floor(zf / WIDTH_SCALE);
//        float x = baseX * WIDTH_SCALE;
//        float z = baseZ * WIDTH_SCALE;
//        float x2 = x + WIDTH_SCALE;
//        float z2 = z + WIDTH_SCALE;
//
//        a.set(x,  getHeight(x , z2), z2);
//        b.set(x,  getHeight(x ,  z),  z);
//        c.set(x2, getHeight(x2,  z),  z);
//        d.set(x2, getHeight(x2, z2), z2);
//
//        float zFrac = 1f - (zf - z) / WIDTH_SCALE;
//        float xFrac = (xf - x) / WIDTH_SCALE;
//
//        float y = (1f - zFrac) * ((1-xFrac) * a.y + xFrac * d.y)
//                  + zFrac * ((1-xFrac) * b.y + xFrac * c.y);
//
//        return y;
//    }

    /** @return lowest elevation in the height map */
    public float getMin() {
        return MIN_HEIGHT;
    }

    /** @return height elevation in the height map */
    public float getMax() {
        return MAX_HEIGHT;
    }

    /** @return length of the map on the x axis in number of points */
    public int getWidth() {
        return WIDTH;
    }

    /** @return length of the map on the z axis in number of points */
    public int getDepth() {
        return DEPTH;
    }

    /** @return the number of data points in the height map */
    public int getNumPoints() {
        return WIDTH * DEPTH;
    }

    @Override
    public String toString() {
        return String.format("[HeightMap] min/max: %.1f, %.1f - points: %d",
                MIN_HEIGHT, MAX_HEIGHT, getNumPoints());
    }
}
