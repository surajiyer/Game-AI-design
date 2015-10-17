package AI;

import com.badlogic.gdx.utils.BinaryHeap;
import com.badlogic.gdx.utils.IntArray;

/**
 * Based on the work of Nathan Sweet at https://gist.github.com/NathanSweet/7587981
 * @author Mike de Brouwer 
 */
public class Astar {
    private final float sqrt2 = 1.41421356f; // approximate the square root of 2 so we don't have to calculate it each time while maintianing high accuracy
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
     * @param tileCost
     * @return  
     */
     public IntArray getPath (int startX, int startY, int targetX, int targetY, 
             float[][][] tileCost) {
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

        int lastColumn = width - 1;
        int lastRow = height - 1;
        int i = 0;
        while (open.size > 0) {
            PathNode node = open.pop();
            if (node.x == targetX && node.y == targetY) {
                while (node != root) {
                        path.add(node.x);
                        path.add(node.y);
                        node = node.parent;
                }
                path.add(node.x);
                path.add(node.y);
                break;
            }
            node.closedID = runID;
            int x = node.x;
            int y = node.y; 
            if (x < lastColumn) {
                addNode(node, x + 1, y, tileCost[x][y][3]);
                if (y < lastRow) addNode(node, x + 1, y + 1, sqrt2 * tileCost[x][y][2]); // Diagonals cost more, roughly equivalent to sqrt(2).
                if (y > 0) addNode(node, x + 1, y - 1, sqrt2 * tileCost[x][y][4]);
            }
            if (x > 0) {
                addNode(node, x - 1, y, tileCost[x][y][7]);
                if (y < lastRow) addNode(node, x - 1, y + 1, sqrt2 * tileCost[x][y][0]);
                if (y > 0) addNode(node, x - 1, y - 1, sqrt2 * tileCost[x][y][6]);
            }
            if (y < lastRow) addNode(node, x, y + 1, tileCost[x][y][1]);
            if (y > 0) addNode(node, x, y - 1, tileCost[x][y][5]);
            i++;
        }
        return path;
     }

    private void addNode (PathNode parent, int x, int y, float cost) {
        if (!isValid(x, y)) return;

        float pathCost = parent.pathCost + cost;
        int dx = Math.abs(x - targetX);
        int dy = Math.abs(y - targetY);
        float heuristic = (Math.abs(dx - dy) + (Math.min(dy,dx) * sqrt2)) * 1.001f;
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

    static private class PathNode extends BinaryHeap.Node {
        int runID, closedID, x, y;
        float pathCost;
        PathNode parent;

        public PathNode (float value) {
                super(value);
        }
    }
}


