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
import com.badlogic.gdx.utils.BinaryHeap;
import com.badlogic.gdx.utils.BinaryHeap.Node;
import com.badlogic.gdx.utils.IntArray;
import java.util.Random; 

/** @author Nathan Sweet */
public class AstarTest extends ApplicationAdapter {
	ShapeRenderer shapes;
	Astar astar;
	boolean[] map;
        int nrOfFlags = 5;
        int nrOfFlagCoordinates = nrOfFlags * 2;   // amount of x and y coordinates of flags
        int[] flagLocations = new int[nrOfFlagCoordinates]; // place for the x and y coordinates of flags
        int widthField = 80;
        int heightField = 60;
        private final int[] tileCost = new int[widthField * heightField];
        int pathCost; 
        
        
        public AstarTest() {
            // randomly generate flag coordinates
            Random randomGenerator = new Random();
            for(int i = 0; i < nrOfFlagCoordinates ; i+=2) {
                flagLocations[i] = randomGenerator.nextInt(widthField);
                flagLocations[i + 1] = randomGenerator.nextInt(heightField);
            }
            for(int x = 0; x < widthField; x++) {
                for(int y = 0; y < heightField; y++) {
                    int index = x + y * widthField;
                    if(Math.random() > 0.7) {
                        tileCost[index] = 2;
                    } else {
                        tileCost[index] = 1;
                    }
                }
            }
        }
        

        @Override
	public void create () {
            shapes = new ShapeRenderer();
            // randomly generate obstacles              
            map = new boolean[widthField * heightField];
            for(int i = 0; i < widthField; i++) {
                for(int j = 0; j < heightField; j++) {
                    if(Math.random() > 0.9) { 
                        map[i + j * widthField] = true;
                    }
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
		shapes.setColor(Color.PINK);
		shapes.begin(ShapeType.Filled);
		for (int x = 0; x < mapWidth; x++)
                    for (int y = 0; y < mapHeight; y++)
                        if (map[x + y * mapWidth]) shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
                // draw all the tilecosts
                shapes.setColor(Color.WHITE);
		for (int x = 0; x < mapWidth; x++) {
                    for (int y = 0; y < mapHeight; y++) {
                        int index = x + mapWidth * y;
                        if (tileCost[index] != 1)  {
                            shapes.rect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);   
                        }
                    }
                }
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
                
             
                // give all the pathcosts
                for(int i = 0; i < nrOfFlagCoordinates; i+=2) { 
                pathCost = astar.getPathCost(flagLocations[i], flagLocations[i + 1]);
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

        static public class Astar {
		private final int width, height;
		private final BinaryHeap<PathNode> open;
		private final PathNode[] nodes;
		int runID;
		private final IntArray path = new IntArray();
                private final IntArray openlist = new IntArray();
		private int targetX, targetY;

		public Astar (int width, int height) {
			this.width = width;
			this.height = height;
			open = new BinaryHeap(width, false);
			nodes = new PathNode[width * height];  
		}

		/** Returns x,y pairs that are the path from the target to the start.
             * @param startX
             * @param startY
             * @param targetX
             * @param targetY
             * @return  */
		public IntArray getPath (int startX, int startY, int targetX, int targetY, int[] tileCost) {
			this.targetX = targetX;
			this.targetY = targetY;

			path.clear();
			open.clear();

			runID++;
			if (runID < 0) runID = 1;

			int index = startY * width + startX;
			PathNode root = nodes[index];
			if (root == null) {
				root = new PathNode(0);
				root.x = startX;
				root.y = startY;
				nodes[index] = root;
			}
			root.parent = null;
			root.pathCost = 0;
			open.add(root, 0);

			int lastColumn = width - 1, lastRow = height - 1;
			int i = 0;
			while (open.size > 0) {
				PathNode node = open.pop();
				if (node.x == targetX && node.y == targetY) {
					while (node != root) {
						path.add(node.x);
						path.add(node.y);
						node = node.parent;
					}
					break;
				}
				node.closedID = runID;
				int x = node.x;
				int y = node.y;
                                int nodeIndex = x + y * width; 
				if (x < lastColumn) {
					addNode(node, x + 1, y, 10 * tileCost[nodeIndex]);
					if (y < lastRow) addNode(node, x + 1, y + 1, 14 * tileCost[nodeIndex]); // Diagonals cost more, roughly equivalent to sqrt(2).
					if (y > 0) addNode(node, x + 1, y - 1, 14 * tileCost[nodeIndex]);
				}
				if (x > 0) {
					addNode(node, x - 1, y, 10 * tileCost[nodeIndex]);
					if (y < lastRow) addNode(node, x - 1, y + 1, 14 * tileCost[nodeIndex]);
					if (y > 0) addNode(node, x - 1, y - 1, 14 * tileCost[nodeIndex]);
				}
				if (y < lastRow) addNode(node, x, y + 1, 10 * tileCost[nodeIndex]);
				if (y > 0) addNode(node, x, y - 1, 10 * tileCost[nodeIndex]);
				i++;
			}
			return path;
		}

		private void addNode (PathNode parent, int x, int y, int cost) {
			if (!isValid(x, y)) return;

			int pathCost = parent.pathCost + cost;
                        float heuristic = Math.abs(x - targetX) + Math.abs(y - targetY);
			float score = pathCost + heuristic; 

			int index = y * width + x;
			PathNode node = nodes[index];
			if (node != null && node.runID == runID) { // Node already encountered for this run.
				if (node.closedID != runID && pathCost < node.pathCost) { // Node isn't closed and new cost is lower.
					// Update the existing node.
					open.setValue(node, score);
					node.parent = parent;
					node.pathCost = pathCost;
				}
			} else {
				// Use node from the cache or create a new one.
				if (node == null) {
					node = new PathNode(0);
					node.x = x;
					node.y = y;
					nodes[index] = node;
				}
				open.add(node, score);
				node.runID = runID;
				node.parent = parent;
				node.pathCost = pathCost;
			}
		}

		protected boolean isValid (int x, int y) {
			return true;
		}

		public int getWidth () {
			return width;
		}

		public int getHeight () {
			return height;
		}
                
                public int getPathCost(int x, int y) {
                    int pathCost; 
                    int index = y * width + x;
                    PathNode node = nodes[index];
                    pathCost = node.pathCost;
                    return pathCost; 
                }   
                 /*public int getTileCost(int x, int y) {
                    int tileCost;
                    int index = y * width + x;
                    PathNode node = nodes[index];
                    tileCost = node.tileCost;
                    return tileCost; 
                }   
                
                public final void setTileCost(int x, int y) {
                    int tileCost = 1;
                    if(Math.random() > 0.7) {
                        tileCost = 10;
                    }
                    int index = y * width + x;
                    PathNode node = nodes[index];
                    node.tileCost = tileCost; 
                }*/

		static private class PathNode extends Node {
			int runID, closedID, x, y, pathCost;
			PathNode parent;

			public PathNode (float value) {
				super(value);
			}
		}
	}
}



