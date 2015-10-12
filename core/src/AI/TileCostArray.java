
package AI;

/**
 *
 * @Mike de Brouwer
 * returns a float array of the movement costs from every tile to all possible neighbouring tiles. 
 */
public class TileCostArray {   
    
    public static float[][] generateTileCostArray(int width, int height, int[] heightMap, int nrDirections, int lastRow, int lastColumn) {
        float[][] tileCost = new float[width * height][nrDirections];
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                int index = x + y * width;
                for(int m = 0; m < nrDirections; m++) {
                    if(y < lastRow) {
                        if(x > 0){
                            if(m == 0){
                                if((heightMap[index] - heightMap[index + width - 1]) == 0) {
                                    tileCost[index][m] = 1f;
                                }
                                if((heightMap[index] - heightMap[index + width - 1]) == 1) {
                                    tileCost[index][m] = 1.5f;
                                }
                                if((heightMap[index] - heightMap[index + width - 1]) == -1) {
                                    tileCost[index][m] = 0.5f;
                                } 
                            }
                        }
                        if(m == 1){
                            if((heightMap[index] - heightMap[index + width]) == 0) {
                                tileCost[index][m] = 1f;
                            }
                            if((heightMap[index] - heightMap[index + width]) == 1) {
                                tileCost[index][m] = 1.5f;
                            }
                            if((heightMap[index] - heightMap[index + width]) == -1) {
                                tileCost[index][m] = 0.5f;
                            } 
                        }
                        if(x < lastColumn) {
                            if(m == 2){
                                if((heightMap[index] - heightMap[index + width + 1]) == 0) {
                                    tileCost[index][m] = 1f;
                                }
                                if((heightMap[index] - heightMap[index + width + 1]) == 1) {
                                    tileCost[index][m] = 1.5f;
                                }
                                if((heightMap[index] - heightMap[index + width + 1]) == -1) {
                                    tileCost[index][m] = 0.5f;
                                }
                            }
                        }
                    }
                    if(x < lastColumn) {
                        if(m == 3){
                            if((heightMap[index] - heightMap[index + 1]) == 0) {
                                tileCost[index][m] = 1f;
                            }
                            if((heightMap[index] - heightMap[index + 1]) == 1) {
                                tileCost[index][m] = 1.5f;
                            }
                            if((heightMap[index] - heightMap[index + 1]) == -1) {
                                tileCost[index][m] = 0.5f;
                            }   
                        }
                        if(y > 0) {
                            if(m == 4){
                                if((heightMap[index] - heightMap[index + 1 - width]) == 0) {
                                    tileCost[index][m] = 1f;
                                }
                                if((heightMap[index] - heightMap[index + 1 - width]) == 1) {
                                    tileCost[index][m] = 1.5f;
                                }
                                if((heightMap[index] - heightMap[index + 1 - width]) == -1) {
                                    tileCost[index][m] = 0.5f;
                                }                       
                            }
                        }
                    }
                    if(y > 0) {
                        if(m == 5){
                            if((heightMap[index] - heightMap[index - width]) == 0) {
                                tileCost[index][m] = 1f;
                            }
                            if((heightMap[index] - heightMap[index - width]) == 1) {
                                tileCost[index][m] = 1.5f;
                            }
                            if((heightMap[index] - heightMap[index - width]) == -1) {
                                tileCost[index][m] = 0.5f;
                            }                          
                        }
                        if(x > 0) {
                            if(m == 6){
                                if((heightMap[index] - heightMap[index - width - 1]) == 0) {
                                    tileCost[index][m] = 1f;
                                }
                                if((heightMap[index] - heightMap[index - width - 1]) == 1) {
                                    tileCost[index][m] = 1.5f;
                                }
                                if((heightMap[index] - heightMap[index - width - 1]) == -1) {
                                    tileCost[index][m] = 0.5f;
                                }                       
                            }
                        }
                    }
                    if(x > 0) {
                        if(m == 7){
                            if((heightMap[index] - heightMap[index - 1]) == 0) {
                                tileCost[index][m] = 1f;
                            }
                            if((heightMap[index] - heightMap[index - 1]) == 1) {
                                tileCost[index][m] = 1.5f;
                            }
                            if((heightMap[index] - heightMap[index - 1]) == -1) {
                                tileCost[index][m] = 0.5f;
                            }                     
                        }
                    }
                    if(heightMap[index] < 50) {
                        tileCost[index][m] = 0.25f; 
                    }
                    System.out.println(tileCost[index][m]);
                }
            }
        }
        return tileCost; 
    }
}
