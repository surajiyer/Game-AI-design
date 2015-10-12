/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AI;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.IntArray;
import java.util.Random;
import terrain.SimplexNoise;


public class AstarTest extends ApplicationAdapter {
	ShapeRenderer shapes;
	Astar astar;
        PathCostArray pathcostarray;
	boolean[] map;
        int nrOfFlags = 4;
        int nrOfFlagCoordinates = nrOfFlags * 2;   // amount of x and y coordinates of flags
        int[] flagLocations = new int[nrOfFlagCoordinates]; // place for the x and y coordinates of flags
        int widthField = 80;
        int heightField = 60;
        int nrDirections = 8; 
        int lastColumn = widthField - 1;
        int lastRow = heightField - 1; 
        private final int[] heightMap = new int[widthField * heightField];
        float[][] tileCost = new float[heightField * widthField][nrDirections];
        double[][] pathCostArray = new double[nrOfFlags][nrOfFlags];
        float pathCost; 
        

        @Override
	public void create () {
            shapes = new ShapeRenderer();
            // randomly generate obstacles              
            map = new boolean[widthField * heightField];
            /*
            for(int i = 0; i < widthField; i++) {
                for(int j = 0; j < heightField; j++) {
                    if(Math.random() > 0.8) { 
                        map[i + j * widthField] = true;
                    }
                }
            }*/
            
            
              // randomly generate flag coordinates
            Random randomGenerator = new Random();
            for(int i = 0; i < nrOfFlagCoordinates ; i+=2) {
                flagLocations[i] = randomGenerator.nextInt(widthField);
                flagLocations[i + 1] = randomGenerator.nextInt(heightField);
            }
            
        float[][] t = SimplexNoise.generateOctavedSimplexNoise(widthField, heightField, 6, 0.5f, 0.007f); // SIMPLEX NOISE
        float[][] r = SimplexNoise.generateRidgedNoise(widthField, heightField, 0.002f); // RIDGED NOISE
        float[][] f = SimplexNoise.combineNoise(t, r);
        // generate heightmap
        for(int i = 0; i < widthField; i++) {
            for(int j = 0; j < heightField; j++) {
                    heightMap[i + j * widthField] = (int)(f[i][j] / 10); 
                    //System.out.println(heightMap[i + j * widthField]);
            }
        }
        // generate an array that has all the tilecosts from a position to the 8 tiles next to it. 
        tileCost = TileCostArray.generateTileCostArray(widthField, heightField, heightMap, nrDirections, lastRow, lastColumn);
        // Pathcost from all flags to all flags.
        pathcostarray = new PathCostArray(widthField, heightField, flagLocations, nrOfFlags, nrOfFlagCoordinates, tileCost);
        pathCostArray = pathcostarray.generatePathCostArray();
        
        for(int m = 0; m < nrOfFlags; m++) {
                for(int k = 0; k < nrOfFlags; k++){
                    System.out.println("Pathcost of " + m + " to " + k + " is: " + pathCostArray[m][k]);     
                }
        }

		astar = new Astar(widthField, heightField) {
                        @Override
			protected boolean isValid (int x, int y) {
				return !map[x + y * widthField];
			}
		};      
	}
        

        @Override
	public void resize (int width, int height) {
            shapes.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
	}
      
        
        @Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();
		int mapWidth = astar.getWidth();
		int mapHeight = astar.getHeight();
		int cellWidth = width / mapWidth;
		int cellHeight = height / mapHeight;
                // draw lines
		shapes.setColor(Color.GRAY);
		shapes.begin(ShapeType.Line);
		for (int x = 0; x < mapWidth; x++)
                    shapes.line(x * cellWidth, 0, x * cellWidth, height);
		for (int y = 0; y < mapHeight; y++)
                    shapes.line(0, y * cellHeight, width, y * cellHeight);
		shapes.end();
                // dray obstacles
                
		
		shapes.begin(ShapeType.Filled);
                
                
                // draw the heightmap
                
                shapes.setColor(Color.WHITE);
		for (int x = 0; x < mapWidth; x++) {
                    for (int y = 0; y < mapHeight; y++) {
                        int index = x + mapWidth * y;
                        if (heightMap[index] == 0)  {
                            shapes.setColor(Color.WHITE);
                            shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);   
                        }
                        if (heightMap[index] == 1)  {
                            shapes.setColor(Color.GOLDENROD);
                            shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);   
                        }
                        if (heightMap[index] == 2)  {
                            shapes.setColor(Color.PINK);
                            shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);   
                        }
                        if (heightMap[index] == 3)  {
                            shapes.setColor(Color.BLUE);
                            shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);   
                        }
                        if (heightMap[index] == 4)  {
                            shapes.setColor(Color.YELLOW);
                            shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);   
                        }
                        if (heightMap[index] == 5)  {
                            shapes.setColor(Color.PINK);
                            shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);   
                        }
                        if (heightMap[index] == 6)  {
                            shapes.setColor(Color.GRAY);
                            shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);   
                        }
                        if (heightMap[index] == 7)  {
                            shapes.setColor(Color.GREEN);
                            shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);   
                        }
                        if (heightMap[index] == 8)  {
                            shapes.setColor(Color.BROWN);
                            shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);   
                        }
                        if (heightMap[index] == 9)  {
                            shapes.setColor(Color.MAGENTA);
                            shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);   
                        }
                        if (heightMap[index] == 10)  {
                            shapes.setColor(Color.ORANGE);
                            shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);   
                        }
                        if (heightMap[index] == 11)  {
                            shapes.setColor(Color.MAROON);
                            shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);   
                        }
                        if (heightMap[index] == 12)  {
                            shapes.setColor(Color.PURPLE);
                            shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);   
                        }
                        if (heightMap[index] == 13)  {
                            shapes.setColor(Color.GOLD);
                            shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);   
                        }
                        if (heightMap[index] == 14)  {
                            shapes.setColor(Color.SKY);
                            shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);   
                        }
                        if (heightMap[index] == 15)  {
                            shapes.setColor(Color.SALMON);
                            shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);   
                        }
                        if (heightMap[index] == 16)  {
                            shapes.setColor(Color.TEAL);
                            shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);   
                        }
                        if (heightMap[index] == 17)  {
                            shapes.setColor(Color.VIOLET);
                            shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);   
                        }
                        if (heightMap[index] == 18)  {
                            shapes.setColor(Color.GOLDENROD);
                            shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);   
                        }
                        
                    }
                }
                /*
                shapes.setColor(Color.BLACK);
		for (int x = 0; x < mapWidth; x++) {
                    for (int y = 0; y < mapHeight; y++) {
                        if (map[x + y * mapWidth]) {
                            shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
                        }
                    }
                }*/
                // draw all the tilecost
                /*
                shapes.setColor(Color.WHITE);
		for (int x = 0; x < mapWidth; x++) {
                    for (int y = 0; y < mapHeight; y++) {
                        for(int m = 0; m < nrDirections; m++){
                            int index = x + mapWidth * y;
                            if (tileCost[index][m] != 1)  {
                                shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);   
                            }
                        }
                    }
                }*/
                //draw mouse pointer 
                
		int startX = Gdx.input.getX() / cellWidth;
		int startY = (height - Gdx.input.getY()) / cellHeight;
		shapes.setColor(Color.GREEN);
		shapes.rect(startX * cellWidth, startY * cellHeight, cellWidth, cellHeight);
                // draw the flags
                for(int i = 0; i < nrOfFlagCoordinates; i+=2) {
                    createFlag(flagLocations[i], flagLocations[i + 1], cellWidth, cellHeight);
                }
                // draw the paths from you to the flags
                for(int i = 0; i < nrOfFlagCoordinates; i+=2) { 
                    drawPath(startX, startY, flagLocations[i], flagLocations[i + 1], cellWidth, cellHeight);
                }
                 // draw the path from all the flags to each others
                for(int i = 0; i < nrOfFlagCoordinates; i+=2){
                    for(int j = 0; j < nrOfFlagCoordinates; j+=2){
                       drawPath(flagLocations[i], flagLocations[i + 1], flagLocations[j], flagLocations[j + 1], cellWidth, cellHeight);                                   
                    }
                }
                // print all the pathcosts from you to the flags
                for(int i = 0; i < nrOfFlagCoordinates; i+=2) { 
                    getPathCost(startX, startY, flagLocations[i], flagLocations[i + 1]);
                } 
              
		shapes.end();
                
                
            }      
             
             public void createFlag(int x, int y, int cellWidth, int cellHeight) {
                shapes.setColor(Color.BLUE);
                shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
             }
             
             public void drawPath(int startX, int startY, int targetX, int targetY, int cellWidth, int cellHeight) {
                 shapes.setColor(Color.RED);
                 IntArray path = astar.getPath(startX, startY, targetX, targetY, tileCost);
                 for (int i = 0, n = path.size; i < n; i += 2) {
                    int x = path.get(i);
                    int y = path.get(i + 1);
                    shapes.circle(x * cellWidth + cellWidth / 2, y * cellHeight + cellHeight / 2, cellWidth / 3, 30);
                }
            } 
            
            // give the path cost of the path between 2 points
            public void getPathCost(int startX, int startY, int targetX, int targetY) {
                IntArray path = astar.getPath(startX, startY, targetX, targetY, tileCost);
                pathCost = 0;
                for (int i = 0, n = path.size; i < n - 2; i += 2) {
                    int x = path.get(i);
                    int y = path.get(i + 1);
                    int index = x + widthField * y;
                    if(((path.get(i) - path.get(i + 2)) == -1) && ((path.get(i + 1) - path.get(i + 3)) == 1)) {
                        pathCost = pathCost + (tileCost[index][0] * 14);
                    } if(((path.get(i) - path.get(i + 2)) == 0) && ((path.get(i + 1) - path.get(i + 3)) == 1)) {
                        pathCost = pathCost + (tileCost[index][1] * 10);
                    } if(((path.get(i) - path.get(i + 2)) == 1) && ((path.get(i + 1) - path.get(i + 3)) == 1)) {
                        pathCost = pathCost + (tileCost[index][2] * 14);
                    } if(((path.get(i) - path.get(i + 2)) == 1) && ((path.get(i + 1) - path.get(i + 3)) == 0)) {
                        pathCost = pathCost + (tileCost[index][3] * 10);
                    } if(((path.get(i) - path.get(i + 2)) == 1) && ((path.get(i + 1) - path.get(i + 3)) == -1)) {
                        pathCost = pathCost + (tileCost[index][4] * 14);
                    } if(((path.get(i) - path.get(i + 2)) == 0) && ((path.get(i + 1) - path.get(i + 3)) == -1)) {
                        pathCost = pathCost + (tileCost[index][5] * 10);
                    } if(((path.get(i) - path.get(i + 2)) == -1) && ((path.get(i + 1) - path.get(i + 3)) == -1)) {
                        pathCost = pathCost + (tileCost[index][6] * 14);
                    } if(((path.get(i) - path.get(i + 2)) == -1) && ((path.get(i + 1) - path.get(i + 3)) == 0)) {
                        pathCost = pathCost + (tileCost[index][7] * 10);
                    }
                    
                }
                //System.out.println("Pathcost is: " + pathCost);
            }    
}    

