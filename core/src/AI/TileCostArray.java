
package AI;

/**
 *
 * @Mike de Brouwer
 * returns a float array of the movement costs from every tile to all possible neighbouring tiles. 
 */
public class TileCostArray {   
    
    public static float[][][] generateTileCostArray(int width, int height, int[][] heightMap, int nrDirections, int lastRow, int lastColumn) {
        float[][][] tileCost = new float[width][height][nrDirections];
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                for(int m = 0; m < nrDirections; m++) {
                    if(y < lastRow) {
                        if(x > 0){
                            if(m == 0){
                                if((heightMap[x][y] - heightMap[x - 1][y + 1]) == 0) {
                                    tileCost[x][y][m] = 1f;
                                }
                                if((heightMap[x][y] - heightMap[x - 1][y + 1]) == 1) {
                                    tileCost[x][y][m] = 1.5f;
                                }
                                if((heightMap[x][y] - heightMap[x - 1][y + 1]) == -1) {
                                    tileCost[x][y][m] = 0.5f;
                                } 
                            }
                        }
                        if(m == 1){
                            if((heightMap[x][y] - heightMap[x][y + 1]) == 0) {
                                tileCost[x][y][m] = 1f;
                            }
                            if((heightMap[x][y] - heightMap[x][y + 1]) == 1) {
                                tileCost[x][y][m] = 1.5f;
                            }
                            if((heightMap[x][y] - heightMap[x][y + 1]) == -1) {
                                tileCost[x][y][m] = 0.5f;
                            } 
                        }
                        if(x < lastColumn) {
                            if(m == 2){
                                if((heightMap[x][y] - heightMap[x + 1][y + 1]) == 0) {
                                    tileCost[x][y][m] = 1f;
                                }
                                if((heightMap[x][y] - heightMap[x + 1][y + 1]) == 1) {
                                    tileCost[x][y][m] = 1.5f;
                                }
                                if((heightMap[x][y] - heightMap[x + 1][y + 1]) == -1) {
                                    tileCost[x][y][m] = 0.5f;
                                }
                            }
                        }
                    }
                    if(x < lastColumn) {
                        if(m == 3){
                            if((heightMap[x][y] - heightMap[x + 1][y]) == 0) {
                                tileCost[x][y][m] = 1f;
                            }
                            if((heightMap[x][y] - heightMap[x + 1][y]) == 1) {
                                tileCost[x][y][m] = 1.5f;
                            }
                            if((heightMap[x][y] - heightMap[x + 1][y]) == -1) {
                                tileCost[x][y][m] = 0.5f;
                            }   
                        }
                        if(y > 0) {
                            if(m == 4){
                                if((heightMap[x][y] - heightMap[x + 1][y - 1]) == 0) {
                                    tileCost[x][y][m] = 1f;
                                }
                                if((heightMap[x][y] - heightMap[x + 1][y - 1]) == 1) {
                                    tileCost[x][y][m] = 1.5f;
                                }
                                if((heightMap[x][y] - heightMap[x + 1][y - 1]) == -1) {
                                    tileCost[x][y][m] = 0.5f;
                                }                       
                            }
                        }
                    }
                    if(y > 0) {
                        if(m == 5){
                            if((heightMap[x][y] - heightMap[x][y - 1]) == 0) {
                                tileCost[x][y][m] = 1f;
                            }
                            if((heightMap[x][y] - heightMap[x][y - 1]) == 1) {
                                tileCost[x][y][m] = 1.5f;
                            }
                            if((heightMap[x][y] - heightMap[x][y - 1]) == -1) {
                                tileCost[x][y][m] = 0.5f;
                            }                          
                        }
                        if(x > 0) {
                            if(m == 6){
                                if((heightMap[x][y] - heightMap[x - 1][y - 1]) == 0) {
                                    tileCost[x][y][m] = 1f;
                                }
                                if((heightMap[x][y] - heightMap[x - 1][y - 1]) == 1) {
                                    tileCost[x][y][m] = 1.5f;
                                }
                                if((heightMap[x][y] - heightMap[x - 1][y - 1]) == -1) {
                                    tileCost[x][y][m] = 0.5f;
                                }                       
                            }
                        }
                    }
                    if(x > 0) {
                        if(m == 7){
                            if((heightMap[x][y] - heightMap[x - 1][y]) == 0) {
                                tileCost[x][y][m] = 1f;
                            }
                            if((heightMap[x][y] - heightMap[x - 1][y]) == 1) {
                                tileCost[x][y][m] = 1.5f;
                            }
                            if((heightMap[x][y] - heightMap[x - 1][y]) == -1) {
                                tileCost[x][y][m] = 0.5f;
                            }                     
                        }
                    }
                    if(heightMap[x][y] < 20) {
                        tileCost[x][y][m] = 0.25f; 
                    }
                }
            }
        }
        return tileCost; 
    }
}
