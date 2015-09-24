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

/** @author Nathan Sweet */
public class AstarTest extends ApplicationAdapter {
	ShapeRenderer shapes;
	Astar astar;
	boolean[] map;
        int widthField = 80;
        int heigthField = 60;

        @Override
	public void create () {
		shapes = new ShapeRenderer();
                    
		map = new boolean[widthField * heigthField];
                for(int i = 0; i < widthField; i++) {
                    for(int j = 0; j < heigthField; j++) {
                        if(Math.random() > 0.95) { 
                            map[i + j * widthField] = true;
                        }
                    }
                }
               
		astar = new Astar(widthField, heigthField) {
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
                //draw mouse pointer
		int startX = Gdx.input.getX() / cellWidth;
		int startY = (height - Gdx.input.getY()) / cellHeight;
		shapes.setColor(Color.GREEN);
		shapes.rect(startX * cellWidth, startY * cellHeight, cellWidth, cellHeight);
                // target 1
		int targetX1 = 0; 
		int targetY1 = 0;
		shapes.setColor(Color.BLUE);
		shapes.rect(targetX1 * cellWidth, targetY1 * cellHeight, cellWidth, cellHeight);
                
                // target 2
                int targetX2 = widthField - 1;
		int targetY2 = heigthField - 1;
		shapes.setColor(Color.PINK);
		shapes.rect(targetX2 * cellWidth, targetY2 * cellHeight, cellWidth, cellHeight);
                
                // target 3
                int targetX3 = widthField - 1;
		int targetY3 = 0;
		shapes.setColor(Color.PINK);
		shapes.rect(targetX3 * cellWidth, targetY3 * cellHeight, cellWidth, cellHeight);
                
                // target 4
                int targetX4 = 0;
		int targetY4 = heigthField - 1;
		shapes.setColor(Color.PINK);
		shapes.rect(targetX4 * cellWidth, targetY4 * cellHeight, cellWidth, cellHeight);
                
                
                
                
                                   
                // draw path to all targets
		if (startX >= 0 && startY >= 0 && startX < mapWidth && startY < mapHeight) { 
			shapes.setColor(Color.BLUE);
			IntArray path1 = astar.getPath(startX, startY, targetX1, targetY1);
			for (int i = 0, n = path1.size; i < n; i += 2) {
				int x = path1.get(i);
				int y = path1.get(i + 1);
				shapes.circle(x * cellWidth + cellWidth / 2, y * cellHeight + cellHeight / 2, cellWidth / 2, 30);
			}
                        shapes.setColor(Color.PINK);
			IntArray path2 = astar.getPath(startX, startY, targetX2, targetY2);
			for (int i = 0, n = path2.size; i < n; i += 2) {
				int x = path2.get(i);
				int y = path2.get(i + 1);
				shapes.circle(x * cellWidth + cellWidth / 2, y * cellHeight + cellHeight / 2, cellWidth / 2, 30);
			}
                             shapes.setColor(Color.GREEN);
			IntArray path3 = astar.getPath(startX, startY, targetX3, targetY3);
			for (int i = 0, n = path3.size; i < n; i += 2) {
				int x = path3.get(i);
				int y = path3.get(i + 1);
				shapes.circle(x * cellWidth + cellWidth / 2, y * cellHeight + cellHeight / 2, cellWidth / 2, 30);
			}
                             shapes.setColor(Color.RED);
			IntArray path4 = astar.getPath(startX, startY, targetX4, targetY4);
			for (int i = 0, n = path4.size; i < n; i += 2) {
				int x = path4.get(i);
				int y = path4.get(i + 1);
				shapes.circle(x * cellWidth + cellWidth / 2, y * cellHeight + cellHeight / 2, cellWidth / 2, 30);
			}
		}
                

		shapes.end();
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
		public IntArray getPath (int startX, int startY, int targetX, int targetY) {
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
				if (x < lastColumn) {
					addNode(node, x + 1, y, 10);
					if (y < lastRow) addNode(node, x + 1, y + 1, 14); // Diagonals cost more, roughly equivalent to sqrt(2).
					if (y > 0) addNode(node, x + 1, y - 1, 14);
				}
				if (x > 0) {
					addNode(node, x - 1, y, 10);
					if (y < lastRow) addNode(node, x - 1, y + 1, 14);
					if (y > 0) addNode(node, x - 1, y - 1, 14);
				}
				if (y < lastRow) addNode(node, x, y + 1, 10);
				if (y > 0) addNode(node, x, y - 1, 10);
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

		static private class PathNode extends Node {
			int runID, closedID, x, y, pathCost;
			PathNode parent;

			public PathNode (float value) {
				super(value);
			}
		}
	}
}



