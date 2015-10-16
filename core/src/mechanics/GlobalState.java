/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mechanics;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import static main.VoxelTest.UNITS_PER_METER;
import mechanics.Flag.Occupant;
import terrain.TreeList;
import terrain.VoxelWorld;
import utils.ConcreteGameObject;
import utils.GameController;

/**
 *
 * @author S.S.Iyer
 */
public class GlobalState {
    public static GameController gameController;
    public final static AssetManager assetsManager = new AssetManager();
    public static boolean assetLoading;
    public static boolean fullScreen = false;
    public static boolean descendLimit = true;
    public static boolean enableWireframe = false;
    public static boolean cursorCaught = true;
    public static boolean isFirstPerson = false;
    public static int oldWidth, oldHeight;
    public static int visibleCount;
    public static VoxelWorld voxelWorld;
    public static Texture voxelTextures;
    public final static FlagsManager flagsManager = new FlagsManager(5);
    public static Score score = new Score();
    public static float[][] heightMap;
    public static int[][] intHeightMap;
    public static int widthField = 320;
    public static int heightField = 320;
    public static Player AI;
    static int latestPlayerCapture;
    static int latestAiCapture;
    public static TreeList treeList;
    
    public static int getLatestPlayerCapture() {
        return latestPlayerCapture;
    }
    
    public static void setLatestPlayerCapture(int capture) {
        latestPlayerCapture = capture;
    }
    
    public static FlagsManager getFlagManager() {
        return flagsManager;
    }
    
    public static Score getScore() {
        return score;
    }
    
    public static void dispose() {
        assetsManager.dispose();
    }
    
    public static void assetsLoading(Array<ConcreteGameObject> instances) {
        Vector3 tmp = new Vector3();
        
        // Load the tower
        ConcreteGameObject gameObject = new ConcreteGameObject(assetsManager.get("tower/tower.g3db", Model.class));
        instances.add(gameObject);
        
        // Load trees
        int[][] trees = treeList.getTreeList();
        for (int[] tree : trees) {
            if (tree[1] >= 18) {
                if (tree[3] == 0) {
                    gameObject = new ConcreteGameObject(assetsManager.get("trees/tree1.g3db", Model.class));
                } else if (tree[3] == 1) {
                    gameObject = new ConcreteGameObject(assetsManager.get("trees/tree2.g3db", Model.class));
                } else if (tree[3] == 2) {
                    gameObject = new ConcreteGameObject(assetsManager.get("trees/tree3.g3db", Model.class));
                } else if (tree[3] == 3) {
                    gameObject = new ConcreteGameObject(assetsManager.get("trees/tree4.g3db", Model.class));
                }
                gameObject.setPosition(tmp.set(tree[0] * UNITS_PER_METER, tree[1] * UNITS_PER_METER, tree[2] * UNITS_PER_METER));
                instances.add(gameObject);
            }
        }
        
        // Load the red flag
        Flag.redFlag = new ModelInstance(assetsManager.get("flags/flagRed.g3db", Model.class));
        
        // Load the blue flag
        Flag.blueFlag = new ModelInstance(assetsManager.get("flags/flagBlue.g3db", Model.class));
        
        // Load the uncaptured flag
        Flag.noneFlag = new ModelInstance(assetsManager.get("flags/flagNone.g3db", Model.class));
        
        // Load new flags in the game
        flagsManager.setOccupant(0,Occupant.AI);
        flagsManager.setOccupant(1,Occupant.AI);
        flagsManager.setOccupant(2,Occupant.PLAYER);
        flagsManager.setOccupant(3,Occupant.PLAYER);
        flagsManager.setOccupant(4,Occupant.PLAYER);
        
        // Done loading assets
        assetLoading = false;
    }
}
