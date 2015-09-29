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
    
    /** Computed minimum height in the heightmap */
    float MIN_HEIGHT, MAX_HEIGHT;

    /**
     * Creates a heightmap with default values
     * @param file - 8-bit PNG file heightmap data
     */
    public HeightMap(FileHandle file) {
        this(file, 1f, 1f, true, 0);
    }

    /**
     *
     * @param file 8-bit PNG file heightmap data
     * @param widthScale horizontal scaling on the x-z plane (distance between points)
     * @param heightScale vertical scaling (y axis)
     * @param whiteHigh height is represented by white (instead of black)
     * @param smoothingPasses
     */
    public HeightMap(FileHandle file, float widthScale, float heightScale, boolean whiteHigh, int smoothingPasses) {
        // Load the heightmap file
        Pixmap pix = new Pixmap(file);
        if (pix.getFormat() != Pixmap.Format.Alpha) {
            throw new GdxRuntimeException("Pixmap must be format Pixmap.Alpha (8-bit Grayscale), not: " + pix.getFormat());
        }
        
        // Get the Heightmap dimensions
        int w = pix.getWidth();
        int h = pix.getHeight();
        
        // Create a 2D array representation of the heightmap
        heights = new float[h][w];
        
        // Load the 2D heightmap array
        int height;
        for (int z = 0; z < h; z++) {
            for (int x = 0; x < w; x++) {
                height = -1 * pix.getPixel(x, z);
                if (whiteHigh) 
                    height = 256 - height;
                heights[z][x] = height/255f;
            }
        }
        
        // Smoothen the terrain heights
        smoothVertexPositions(smoothingPasses);
    }
    
    /** Set the minimum and maximum height of the heightmap. */
    private void setMinMaxHeights() {
        MIN_HEIGHT = Float.MAX_VALUE;
        MAX_HEIGHT = Float.MIN_VALUE;
        for (int z = 0; z < heights.length; z++) {
            for (int x = 0; x < heights[0].length; x++) {
                float y = heights[z][x];
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
            for (int z = 0; z < heights.length; z++) {
                for (int x = 1; x < heights[z].length - 1; x += 1) {
                    float prev = heights[z][x - 1];
                    float y = heights[z][x];
                    float next = heights[z][x + 1];
                    float yAvg = (next + prev) / 2f;
                    heights[z][x] = (y + yAvg) / 2f;
                }
            }
            // smooth along z
            for (int x = 0; x < heights[0].length; x++) {
                for (int z = 1; z < heights.length - 1; z += 1) {
                    float prev = heights[z - 1][x];
                    float y = heights[z][x];
                    float next = heights[z + 1][x];
                    float yAvg = (next + prev) / 2f;
                    heights[z][x] = (y + yAvg) / 2f;
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
     * Get height for single point at x,z coords, accounting for scale, does 
     * not interpolate using neighbors. parameters assume heightmap starts at 
     * origin (0,0)
     * @param xf
     * @param zf
     * @param WIDTH_SCALE
     * @return 
     */
    public float getHeight(float xf, float zf, float WIDTH_SCALE) {
        int x = (int) Math.floor(xf / WIDTH_SCALE);
        int z = (int) Math.floor(zf / WIDTH_SCALE);
        if (x < 0) x = 0;
        if (z < 0) z = 0;
        if (z >= heights.length) {
            z = heights.length - 1;
        }
        if (x >= heights[z].length) {
            x = heights[z].length - 1;
        }
        return heights[z][x];
    }
    
    public float getHeight(int x, int z) {
        if (x < 0 || z < 0 || z >= heights.length || x >= heights[z].length) {
            throw new IllegalArgumentException("Heightmap does not contain given point.");
        }
        return heights[z][x];
    }

    /**
     * Get the interpolated height for x,z coords, accounting for scale, 
     * interpolated using neighbors. This will give the interpolated height when 
     * the parameters lie somewhere between actual heightmap data points.
     * parameters assume heightmap's origin is at world coordinates (x:0, z:0)
     * @param xf
     * @param zf
     * @param WIDTH_SCALE
     * @return the scale-adjusted interpolated height at specified world coordinates
     */
    public float getInterpolatedHeight(float xf, float zf, float WIDTH_SCALE) {
        Vector3 a = tmp;
        Vector3 b = tmp2;
        Vector3 c = tmp3;
        Vector3 d = tmp4;

        float baseX = (float) Math.floor(xf / WIDTH_SCALE);
        float baseZ = (float) Math.floor(zf / WIDTH_SCALE);
        float x = baseX * WIDTH_SCALE;
        float z = baseZ * WIDTH_SCALE;
        float x2 = x + WIDTH_SCALE;
        float z2 = z + WIDTH_SCALE;

        a.set(x,  getHeight(x , z2, WIDTH_SCALE), z2);
        b.set(x,  getHeight(x ,  z, WIDTH_SCALE),  z);
        c.set(x2, getHeight(x2,  z, WIDTH_SCALE),  z);
        d.set(x2, getHeight(x2, z2, WIDTH_SCALE), z2);

        float zFrac = 1f - (zf - z) / WIDTH_SCALE;
        float xFrac = (xf - x) / WIDTH_SCALE;

        float y = (1f - zFrac) * ((1-xFrac) * a.y + xFrac * d.y)
                  + zFrac * ((1-xFrac) * b.y + xFrac * c.y);

        return y;
    }

    /** @return lowest elevation in the height map */
    public float getMin() {
        return MIN_HEIGHT;
    }

    /** @return height elevation in the height map */
    public float getMax() {
        return MAX_HEIGHT;
    }

    /** @return length of the map on the x-axis in number of points */
    public int getWidth() {
        return heights.length;
    }

    /** @return length of the map on the z-axis in number of points */
    public int getDepth() {
        return heights[0].length;
    }

    /** @return the number of data points in the height map */
    public int getNumPoints() {
        return getWidth() * getDepth();
    }

    @Override
    public String toString() {
        return String.format("[HeightMap] min/max: %.1f, %.1f - points: %d",
                MIN_HEIGHT, MAX_HEIGHT, getNumPoints());
    }
}
