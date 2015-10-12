/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AI;

import com.badlogic.gdx.utils.IntArray;
/**
 *
 * @author Administrator
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
                //pathCostArray[((i * nrOfFlags)/2) + (j/2)] = pathCost; 
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
}