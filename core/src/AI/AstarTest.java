/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AI;

/**
 * @author Mike de Brouwer
 * 
 */

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.IntArray;
import java.util.Random;
import mechanics.FlagList;
import terrain.SimplexNoise;
import utils.GameInfo;


public class AstarTest extends ApplicationAdapter {
	ShapeRenderer shapes;
	Astar astar;
        PathCostArray pathcostarray;
        FlagList flagList;
        private final float sqrt2 = 1.41421356f; // approximate the square root of 2 so we don't have to calculate it each time while maintianing high accuracy
	boolean[] map;
        int nrOfFlags = 5;
        int nrOfFlagCoordinates = nrOfFlags * 2;   // amount of x and y coordinates of flags
        int[] flagLocations = new int[nrOfFlagCoordinates]; // place for the x and y coordinates of flags
        int widthField = 280;
        int heightField = 200;
        int nrDirections = 8; 
        int lastColumn = widthField - 1;
        int lastRow = heightField - 1; 
        private final int[][] heightMap = new int[widthField][heightField];
        float[][][] tileCost = new float[widthField][heightField][nrDirections];
        float[][] pathCostArray = new float[nrOfFlags][nrOfFlags];
        int[] closestFlagArray = new int[nrOfFlags];
        float pathCost; 

        

        @Override
	public void create () {
            shapes = new ShapeRenderer();
            // randomly generate obstacles              
            map = new boolean[widthField * heightField];
            // get the fags list
            flagList = GameInfo.flagList;//new FlagList(nrOfFlags);
            flagLocations = flagList.getAllFlagCoordinates();
            
        float[][] t = SimplexNoise.generateOctavedSimplexNoise(widthField, heightField, 6, 0.5f, 0.007f); // SIMPLEX NOISE
        float[][] r = SimplexNoise.generateRidgedNoise(widthField, heightField, 0.002f); // RIDGED NOISE
        float[][] f = SimplexNoise.combineNoise(t, r);
        // generate heightmap
        for(int i = 0; i < widthField; i++) {
            for(int j = 0; j < heightField; j++) {
                    heightMap[i][j] = (int)(f[i][j] / 2); 
                    //System.out.println(heightMap[i + j * widthField]);
            }
        }
        // generate an array that has all the tilecosts from a position to the 8 tiles next to it. 
        tileCost = TileCostArray.generateTileCostArray(widthField, heightField, heightMap, nrDirections, lastRow, lastColumn);
        // Pathcost from all flags to all flags.
        pathcostarray = new PathCostArray(widthField, heightField, flagLocations, nrOfFlags, tileCost);
        //pathCostArray = pathcostarray.generatePathCostArray();
        /*
        closestFlagArray = pathcostarray.generateClosestFlagArray(0);
        closestFlagArray = pathcostarray.generateClosestFlagArray(1);
        closestFlagArray = pathcostarray.generateClosestFlagArray(2);
        closestFlagArray = pathcostarray.generateClosestFlagArray(3);
        closestFlagArray = pathcostarray.generateClosestFlagArray(4);
        */
        closestFlagArray = pathcostarray.generateClosestFlagArrayAtLocation(0,0);
        /*
        for(int m = 0; m < nrOfFlags; m++) {
                for(int k = 0; k < nrOfFlags; k++){
                    System.out.println("Pathcost of " + m + " to " + k + " is: " + pathCostArray[m][k]);     
                }
        }*/

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
                        pathCost = pathCost + (tileCost[x][y][0] * sqrt2);
                    } if(((path.get(i) - path.get(i + 2)) == 0) && ((path.get(i + 1) - path.get(i + 3)) == 1)) {
                        pathCost = pathCost + (tileCost[x][y][1] * 10);
                    } if(((path.get(i) - path.get(i + 2)) == 1) && ((path.get(i + 1) - path.get(i + 3)) == 1)) {
                        pathCost = pathCost + (tileCost[x][y][2] * sqrt2);
                    } if(((path.get(i) - path.get(i + 2)) == 1) && ((path.get(i + 1) - path.get(i + 3)) == 0)) {
                        pathCost = pathCost + (tileCost[x][y][3] * 10);
                    } if(((path.get(i) - path.get(i + 2)) == 1) && ((path.get(i + 1) - path.get(i + 3)) == -1)) {
                        pathCost = pathCost + (tileCost[x][y][4] * sqrt2);
                    } if(((path.get(i) - path.get(i + 2)) == 0) && ((path.get(i + 1) - path.get(i + 3)) == -1)) {
                        pathCost = pathCost + (tileCost[x][y][5] * 10);
                    } if(((path.get(i) - path.get(i + 2)) == -1) && ((path.get(i + 1) - path.get(i + 3)) == -1)) {
                        pathCost = pathCost + (tileCost[x][y][6] * sqrt2);
                    } if(((path.get(i) - path.get(i + 2)) == -1) && ((path.get(i + 1) - path.get(i + 3)) == 0)) {
                        pathCost = pathCost + (tileCost[x][y][7] * 10);
                    }
                    
                }
                //System.out.println("Pathcost is: " + pathCost);
            }    
}    

