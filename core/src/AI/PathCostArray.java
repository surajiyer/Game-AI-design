
package AI;

import com.badlogic.gdx.utils.IntArray;
import java.util.Arrays;
/**
 *
 * @author Mike de Brouwer
 * Gives the path costs from a flag to every other flag
 * in a float[][] array. [1][2] is the distance from flag 1 to flag 2
 * it uses this in generateclosestflag to calculate the closest flags. 
 */
public class PathCostArray {
    private final float sqrt2 = 1.41421356f; // approximate the square root of 2 so we don't have to calculate it each time while maintianing high accuracy
    Astar astar;
    private final int width;
    private final int height;
    private final int[] flagLocations;
    private final int nrOfFlags;
    private final float[][][] tileCost;
    float pathCost;
    

    public PathCostArray(int width, int height, int[] flagLocations, int nrOfFlags, float[][][] tileCost) { 
        this.width = width;
        this.height = height;
        this.flagLocations = flagLocations;
        this.nrOfFlags = nrOfFlags;
        this.tileCost = tileCost;
        astar = new Astar(width, height);
    }     
    public float[][] generatePathCostArray() {
        final float[][] pathCostArray = new float[nrOfFlags][nrOfFlags];
        for(int i = 0; i < flagLocations.length; i+=2) { 
            for(int j = 0; j < flagLocations.length; j+=2) {
                IntArray path = astar.getPath(flagLocations[i], flagLocations[i + 1], flagLocations[j], flagLocations[j + 1], tileCost);
                float pathCost = 0;
                for (int k = 0, n = path.size; k < n - 2; k += 2) {
                    int x = path.get(k);
                    int y = path.get(k + 1);
                        if(((path.get(k) - path.get(k + 2)) == -1) && ((path.get(k + 1) - path.get(k + 3)) == 1)) {
                            pathCost = pathCost + (tileCost[x][y][0] * sqrt2);
                        } if(((path.get(k) - path.get(k + 2)) == 0) && ((path.get(k + 1) - path.get(k + 3)) == 1)) {
                            pathCost = pathCost + (tileCost[x][y][1]);
                        } if(((path.get(k) - path.get(k + 2)) == 1) && ((path.get(k + 1) - path.get(k + 3)) == 1)) {
                            pathCost = pathCost + (tileCost[x][y][2] * sqrt2);
                        } if(((path.get(k) - path.get(k + 2)) == 1) && ((path.get(k + 1) - path.get(k + 3)) == 0)) {
                            pathCost = pathCost + (tileCost[x][y][3]);
                        } if(((path.get(k) - path.get(k + 2)) == 1) && ((path.get(k + 1) - path.get(k + 3)) == -1)) {
                            pathCost = pathCost + (tileCost[x][y][4] * sqrt2);
                        } if(((path.get(k) - path.get(k + 2)) == 0) && ((path.get(k + 1) - path.get(k + 3)) == -1)) {
                            pathCost = pathCost + (tileCost[x][y][5]);
                        } if(((path.get(k) - path.get(k + 2)) == -1) && ((path.get(k + 1) - path.get(k + 3)) == -1)) {
                            pathCost = pathCost + (tileCost[x][y][6] * sqrt2);
                        } if(((path.get(k) - path.get(k + 2)) == -1) && ((path.get(k + 1) - path.get(k + 3)) == 0)) {
                            pathCost = pathCost + (tileCost[x][y][7]);
                        }
                } 
                pathCostArray[i / 2][j / 2] = pathCost;
            }
        }  
        /*
        for(int m = 0; m < nrOfFlags; m++) {
            for(int k = 0; k < nrOfFlags; k++){
                System.out.println("Pathcost of " + m + " to " + k + " is: " + pathCostArray[m][k]);     
            }
        }*/
        return pathCostArray;
    } 
    public int[] generateClosestFlagArray(int currentFlag) {
        final float[][] interMediaryArray = new float[nrOfFlags][2];   // Tracks pathcosts and nr of the flag
        final int[] closestFlagArray = new int[nrOfFlags - 1];
        float[][] pathCostArray = generatePathCostArray(); 
        for (int i = 0; i < nrOfFlags; i++) {
            interMediaryArray[i][1] = i;
            interMediaryArray[i][0] = pathCostArray[currentFlag][i];  
        }
        System.out.println("currentFlag " + currentFlag);
        for (final float[] arr : interMediaryArray) {
            //System.out.println(Arrays.toString(arr));
        }
        java.util.Arrays.sort(interMediaryArray, new java.util.Comparator<float[]>() {
            @Override
            public int compare(float[] a, float[] b) {
                return Float.compare(a[0], b[0]);
            }
        });
        for (final float[] arr : interMediaryArray) {
            //System.out.println(Arrays.toString(arr));
        }
        for (int i = 0; i < nrOfFlags - 1; i++) {
            closestFlagArray[i] = (int)interMediaryArray[i + 1][1]; 
            System.out.println(closestFlagArray[i]);
        }
        
    return closestFlagArray; 
    }
    public float[] getPathCost(int startX, int startY) {
        final float[] getPathCost = new float[nrOfFlags];
        for(int i = 0; i < flagLocations.length; i+=2) { 
                IntArray path = astar.getPath(startX, startY, flagLocations[i], flagLocations[i + 1], tileCost);
                pathCost = 0;
                for (int k = 0, n = path.size; k < n - 2; k += 2) {
                    int x = path.get(k);
                    int y = path.get(k + 1);
                        if(((path.get(k) - path.get(k + 2)) == -1) && ((path.get(k + 1) - path.get(k + 3)) == 1)) {
                            pathCost = pathCost + (tileCost[x][y][0] * sqrt2);
                        } if(((path.get(k) - path.get(k + 2)) == 0) && ((path.get(k + 1) - path.get(k + 3)) == 1)) {
                            pathCost = pathCost + (tileCost[x][y][1]);
                        } if(((path.get(k) - path.get(k + 2)) == 1) && ((path.get(k + 1) - path.get(k + 3)) == 1)) {
                            pathCost = pathCost + (tileCost[x][y][2] * sqrt2);
                        } if(((path.get(k) - path.get(k + 2)) == 1) && ((path.get(k + 1) - path.get(k + 3)) == 0)) {
                            pathCost = pathCost + (tileCost[x][y][3]);
                        } if(((path.get(k) - path.get(k + 2)) == 1) && ((path.get(k + 1) - path.get(k + 3)) == -1)) {
                            pathCost = pathCost + (tileCost[x][y][4] * sqrt2);
                        } if(((path.get(k) - path.get(k + 2)) == 0) && ((path.get(k + 1) - path.get(k + 3)) == -1)) {
                            pathCost = pathCost + (tileCost[x][y][5]);
                        } if(((path.get(k) - path.get(k + 2)) == -1) && ((path.get(k + 1) - path.get(k + 3)) == -1)) {
                            pathCost = pathCost + (tileCost[x][y][6] * sqrt2);
                        } if(((path.get(k) - path.get(k + 2)) == -1) && ((path.get(k + 1) - path.get(k + 3)) == 0)) {
                            pathCost = pathCost + (tileCost[x][y][7]);
                        }
                } 
                getPathCost[i / 2] = pathCost;
        }  
        return getPathCost;
    } 
    public int[] generateClosestFlagArrayAtLocation(int x, int y) {
        final float[][] interMediaryArray = new float[nrOfFlags][2];   // Tracks pathcosts and nr of the flag
        final int[] closestFlagArray = new int[nrOfFlags];
        float[] pathCostArray = getPathCost(x,y); 
        for (int i = 0; i < nrOfFlags; i++) {
            interMediaryArray[i][1] = i;
            interMediaryArray[i][0] = pathCostArray[i];  
        }
        for (final float[] arr : interMediaryArray) {
            System.out.println(Arrays.toString(arr));
        }
        java.util.Arrays.sort(interMediaryArray, new java.util.Comparator<float[]>() {
            @Override
            public int compare(float[] a, float[] b) {
                return Float.compare(a[0], b[0]);
            }
        });
        for (final float[] arr : interMediaryArray) {
            System.out.println(Arrays.toString(arr));
        }
        for (int i = 0; i < nrOfFlags; i++) {
            closestFlagArray[i] = (int)interMediaryArray[i][1]; 
            System.out.println(closestFlagArray[i]);
        }
        
    return closestFlagArray; 
    }
}