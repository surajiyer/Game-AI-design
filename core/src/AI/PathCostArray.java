
package AI;

import com.badlogic.gdx.utils.IntArray;
import java.util.Arrays;
/**
 *
 * @author Mike de Brouwer
 * Gives the path costs from a flag to every other flag
 * in a float[][] array. [1][2] is the distance from flag 1 to flag 2
 */
public class PathCostArray {
    Astar astar;
    private final int width;
    private final int height;
    private final int[] flagLocations;
    private final int nrOfFlags;
    private final int nrOfFlagCoordinates;
    private final float[][] tileCost;
    

    public PathCostArray(int width, int height, int[] flagLocations, int nrOfFlags, int nrOfFlagCoordinates, float[][] tileCost) { 
        this.width = width;
        this.height = height;
        this.flagLocations = flagLocations;
        this.nrOfFlags = nrOfFlags;
        this.nrOfFlagCoordinates = nrOfFlagCoordinates;
        this.tileCost = tileCost;
        astar = new Astar(width, height);
    }     
    public double[][] generatePathCostArray() {
        final double[][] pathCostArray = new double[nrOfFlags][nrOfFlags];
        for(int i = 0; i < nrOfFlagCoordinates; i+=2) { 
            for(int j = 0; j < nrOfFlagCoordinates; j+=2) {
                IntArray path = astar.getPath(flagLocations[i], flagLocations[i + 1], flagLocations[j], flagLocations[j + 1], tileCost);
                float pathCost = 0;
                for (int k = 0, n = path.size; k < n - 2; k += 2) {
                    int x = path.get(k);
                    int y = path.get(k + 1);
                    int index = x + width * y;
                        if(((path.get(k) - path.get(k + 2)) == -1) && ((path.get(k + 1) - path.get(k + 3)) == 1)) {
                            pathCost = pathCost + (tileCost[index][0] * 14);
                        } if(((path.get(k) - path.get(k + 2)) == 0) && ((path.get(k + 1) - path.get(k + 3)) == 1)) {
                            pathCost = pathCost + (tileCost[index][1] * 10);
                        } if(((path.get(k) - path.get(k + 2)) == 1) && ((path.get(k + 1) - path.get(k + 3)) == 1)) {
                            pathCost = pathCost + (tileCost[index][2] * 14);
                        } if(((path.get(k) - path.get(k + 2)) == 1) && ((path.get(k + 1) - path.get(k + 3)) == 0)) {
                            pathCost = pathCost + (tileCost[index][3] * 10);
                        } if(((path.get(k) - path.get(k + 2)) == 1) && ((path.get(k + 1) - path.get(k + 3)) == -1)) {
                            pathCost = pathCost + (tileCost[index][4] * 14);
                        } if(((path.get(k) - path.get(k + 2)) == 0) && ((path.get(k + 1) - path.get(k + 3)) == -1)) {
                            pathCost = pathCost + (tileCost[index][5] * 10);
                        } if(((path.get(k) - path.get(k + 2)) == -1) && ((path.get(k + 1) - path.get(k + 3)) == -1)) {
                            pathCost = pathCost + (tileCost[index][6] * 14);
                        } if(((path.get(k) - path.get(k + 2)) == -1) && ((path.get(k + 1) - path.get(k + 3)) == 0)) {
                            pathCost = pathCost + (tileCost[index][7] * 10);
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
        final double[][] interMediaryArray = new double[nrOfFlags][2];   // Tracks pathcosts and nr of the flag
        final int[] closestFlagArray = new int[nrOfFlags - 1];
        double[][] pathCostArray = generatePathCostArray(); 
        for (int i = 0; i < nrOfFlags; i++) {
            interMediaryArray[i][1] = i + 1;
            interMediaryArray[i][0] = pathCostArray[currentFlag - 1][i];  
        }
        for (final double[] arr : interMediaryArray) {
            System.out.println(Arrays.toString(arr));
        }
        java.util.Arrays.sort(interMediaryArray, new java.util.Comparator<double[]>() {
            @Override
            public int compare(double[] a, double[] b) {
                return Double.compare(a[0], b[0]);
            }
        });
        for (final double[] arr : interMediaryArray) {
            System.out.println(Arrays.toString(arr));
        }
        for (int i = 0; i < nrOfFlags - 1; i++) {
            closestFlagArray[i] = (int)interMediaryArray[i + 1][1]; 
            System.out.println(closestFlagArray[i]);
        }
        
    return closestFlagArray; 
    }
}