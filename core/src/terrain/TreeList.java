/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package terrain;

import java.util.Random;
import mechanics.GlobalState;

/**
 *
 * @author s137092
 */
public class TreeList {
    
    int NROF_TREES;
    int[][] treeList;
    Random random = new Random();
    
    public TreeList(int NROF_TREES) {
        this.NROF_TREES = NROF_TREES;
        
        treeList = new int[NROF_TREES][4];
        for (int i = 0; i < NROF_TREES; i++) {
                int x = random.nextInt(320);
                int z = random.nextInt(320);
                int y = (int) GlobalState.voxelWorld.getHeight(x, z);
                int t = random.nextInt(4);
                treeList[i][0] = x;
                treeList[i][1] = y;
                treeList[i][2] = z;
                treeList[i][3] = t;
        }
    }
    
    public int[][] getTreeList() {
        return treeList;
    }
}
