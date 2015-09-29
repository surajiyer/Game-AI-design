package terrain;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import static java.lang.Math.abs;
import javax.imageio.ImageIO;
import java.util.*;


public class SimplexNoise { // Simplex noise in 2D, 3D and 4D
    
// PUT IN CREATE METHOD OF BASIC3DTEST
//        float[][] t = SimplexNoise.generateOctavedSimplexNoise(280, 200, 6, 0.5f, 0.007f); // SIMPLEX NOISE
//        float[][] r = SimplexNoise.generateRidgedNoise(280, 200, 0.002f); // RIDGED NOISE
//        float[][] f = SimplexNoise.combineNoise(t, r);
//        SimplexNoise.createImage(t, "heightmaps/111terrain.png");
//        SimplexNoise.createImage(r, "heightmaps/111river.png");
//        SimplexNoise.createImage(f, "heightmaps/111final.png");

    private static int grad3[][] = {{1,1,0},{-1,1,0},{1,-1,0},{-1,-1,0},
        {1,0,1},{-1,0,1},{1,0,-1},{-1,0,-1},
        {0,1,1},{0,-1,1},{0,1,-1},{0,-1,-1}
    };


    private static int p[] = {151,160,137,91,90,15,
                              131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,8,99,37,240,21,10,23,
                              190, 6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,35,11,32,57,177,33,
                              88,237,149,56,87,174,20,125,136,171,168, 68,175,74,165,71,134,139,48,27,166,
                              77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,55,46,245,40,244,
                              102,143,54, 65,25,63,161, 1,216,80,73,209,76,132,187,208, 89,18,169,200,196,
                              135,130,116,188,159,86,164,100,109,198,173,186, 3,64,52,217,226,250,124,123,
                              5,202,38,147,118,126,255,82,85,212,207,206,59,227,47,16,58,17,182,189,28,42,
                              223,183,170,213,119,248,152, 2,44,154,163, 70,221,153,101,155,167, 43,172,9,
                              129,22,39,253, 19,98,108,110,79,113,224,232,178,185, 112,104,218,246,97,228,
                              251,34,242,193,238,210,144,12,191,179,162,241, 81,51,145,235,249,14,239,107,
                              49,192,214, 31,181,199,106,157,184, 84,204,176,115,121,50,45,127, 4,150,254,
                              138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180
                             };

    static int[] shuffleArray(int[] ar) {
        Random rnd = new Random();

        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
        return ar;
    }

    // To remove the need for index wrapping, double the permutation table length
    private static int perm[] = new int[512];
    static {
        for(int i=0; i<512; i++) perm[i]=shuffleArray(p)[i & 255];
    }

    private static int fastfloor(double x) {
        return x>0 ? (int)x : (int)x-1;
    }

    private static double dot(int g[], double x, double y) {
        return g[0]*x + g[1]*y;
    }

    // 2D simplex noise
    public static double noise(double xin, double yin) {
        double n0, n1, n2; // Noise contributions from the three corners

        // Skew the input space to determine which simplex cell we're in
        final double F2 = 0.5*(Math.sqrt(3.0)-1.0);
        double s = (xin+yin)*F2; // Hairy factor for 2D
        int i = fastfloor(xin+s);
        int j = fastfloor(yin+s);
        final double G2 = (3.0-Math.sqrt(3.0))/6.0;
        double t = (i+j)*G2;
        double X0 = i-t; // Unskew the cell origin back to (x,y) space
        double Y0 = j-t;
        double x0 = xin-X0; // The x,y distances from the cell origin
        double y0 = yin-Y0;

        // For the 2D case, the simplex shape is an equilateral triangle.
        // Determine which simplex we are in.
        int i1, j1; // Offsets for second (middle) corner of simplex in (i,j) coords
        if(x0>y0) {
            i1=1;    // lower triangle, XY order: (0,0)->(1,0)->(1,1)
            j1=0;
        } else {
            i1=0;    // upper triangle, YX order: (0,0)->(0,1)->(1,1)
            j1=1;
        }

        // A step of (1,0) in (i,j) means a step of (1-c,-c) in (x,y), and
        // a step of (0,1) in (i,j) means a step of (-c,1-c) in (x,y), where
        // c = (3-sqrt(3))/6
        double x1 = x0 - i1 + G2; // Offsets for middle corner in (x,y) unskewed coords
        double y1 = y0 - j1 + G2;
        double x2 = x0 - 1.0 + 2.0 * G2; // Offsets for last corner in (x,y) unskewed coords
        double y2 = y0 - 1.0 + 2.0 * G2;

        // Work out the hashed gradient indices of the three simplex corners
        int ii = i & 255;
        int jj = j & 255;
        int gi0 = perm[ii+perm[jj]] % 12;
        int gi1 = perm[ii+i1+perm[jj+j1]] % 12;
        int gi2 = perm[ii+1+perm[jj+1]] % 12;

        // Calculate the contribution from the three corners
        double t0 = 0.5 - x0*x0-y0*y0;
        if(t0<0) n0 = 0.0;
        else {
            t0 *= t0;
            n0 = t0 * t0 * dot(grad3[gi0], x0, y0); // (x,y) of grad3 used for 2D gradient
        }

        double t1 = 0.5 - x1*x1-y1*y1;
        if(t1<0) n1 = 0.0;
        else {
            t1 *= t1;
            n1 = t1 * t1 * dot(grad3[gi1], x1, y1);
        }

        double t2 = 0.5 - x2*x2-y2*y2;
        if(t2<0) n2 = 0.0;
        else {
            t2 *= t2;
            n2 = t2 * t2 * dot(grad3[gi2], x2, y2);
        }

        // Add contributions from each corner to get the final noise value.
        // The result is scaled to return values in the interval [-1,1].
        return 70.0 * (n0 + n1 + n2);
    }

    public static float[][] generateSimplexNoise(int width, int height) {
        float[][] simplexNoise = new float[width][height];
        float frequency = 5.0f / (float) width;

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                simplexNoise[x][y] = (float) noise(x * frequency,y * frequency);
                simplexNoise[x][y] = (simplexNoise[x][y] + 1) / 2f;   //generate values between 0 and 1
            }
        }

        System.out.println("Simplex noise generated");

        return simplexNoise;
    }

    public static float[][] generateOctavedSimplexNoise(int width, int height, int octaves, float roughness, float scale) {
        int high = 255;
        int low = 100;
        float[][] terrainNoise = new float[width][height];
        float layerFrequency = scale;
        float layerWeight = 1;
        float weightSum = 0;

        for (int octave = 0; octave < octaves; octave++) {
            //Calculate single layer/octave of simplex noise, then add it to total noise
            for(int x = 0; x < width; x++) {
                for(int y = 0; y < height; y++) {
                    terrainNoise[x][y] += ( (float) noise(x * layerFrequency,y * layerFrequency) * layerWeight);   
                }
            }

            //Increase variables with each incrementing octave
            weightSum += layerWeight;
            layerWeight *= roughness;
            layerFrequency *= 2;
        }
        
        //Normalize noise
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                terrainNoise[x][y] = (terrainNoise[x][y] / weightSum);
                terrainNoise[x][y] = terrainNoise[x][y] * (high - low) / 2  + (high + low) / 2;
                //System.out.println("Height value at " + x + "," + y + " is: " + terrainNoise[x][y]);
            }
        }
        
        System.out.println("Simplex noise generated");
        return terrainNoise;
    }
    
    
    public static float[][] generateRidgedNoise(int width, int height, float scale) {
        float[][] ridgedNoise = new float[width][height];
        float layerFrequency = scale;
        int high = 255;
        int low = 0;

            //Calculate single layer/octave of simplex noise, then add it to total noise
            for(int x = 0; x < width; x++) {
                for(int y = 0; y < height; y++) {
                    ridgedNoise[x][y] = 255 * (1 - abs((float) noise(x * layerFrequency,y * layerFrequency)));
                    //System.out.println("Height value at " + x + "," + y + " is: " + ridgedNoise[x][y]);
                }
            }

            //Normalize noise
            for(int x = 0; x < width; x++) {
                for(int y = 0; y < height; y++) {
                    //ridgedNoise[x][y] = ridgedNoise[x][y] * (high - low) / 2  + (high + low) / 2;
                    //System.out.println("Height value at " + x + "," + y + " is: " + ridgedNoise[x][y]);
            }
        }

        System.out.println("Ridged noise generated");

        return ridgedNoise;
    }
    
    public static float[][] combineNoise(float[][] terrain, float[][] rivers) {
        int width = terrain.length;
        int height = terrain[0].length;
        
        float[][] totalNoise = new float[terrain.length][terrain[0].length];
        float store = 0;
        
        for(int x = 0; x < width; x++) {
                for(int y = 0; y < height; y++) {
                    store =  (terrain[x][y] + 2*rivers[x][y])/3;
//                    if(store > 255) {
//                        store -= 255;
//                    }
                    store = -1*(store-255);
                    totalNoise[x][y] = store;
                }
            }
        
        return totalNoise;
    }
    
    public static void createImage(float[][] simplexNoise, String path) {
        int width = simplexNoise.length;
        int height = simplexNoise[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Color blue = new Color(0,0,255);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(simplexNoise[x][y] < 50) {
                    image.setRGB(x, y, blue.getRGB());
                } else {
                int grayscale = (int) (simplexNoise[x][y]);
                //System.out.println(grayscale);
                int rgb = 65536 * grayscale + 256 * grayscale + grayscale;
                image.setRGB(x, y, rgb);
                }
            }
        }

        File ImageFile = new File(path);
        try {
            ImageIO.write(image, "png", ImageFile);
            System.out.println("Image created");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}