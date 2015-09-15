/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terrain;

/**
 *
 * @author Daniël
 */
public class TerrainGenerator {
   
    final int TERRAIN_WIDTH;
    final int TERRAIN_BREADTH;
    
    TerrainGenerator(int width, int breadth) {
        TERRAIN_WIDTH = width;
        TERRAIN_BREADTH = breadth;
    }
    
    public void generateTerrain() {
        SimplexNoise.generateSimplexNoise(TERRAIN_WIDTH*4, TERRAIN_BREADTH*4);
    }    
}
