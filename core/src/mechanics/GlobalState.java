/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mechanics;

import com.badlogic.gdx.assets.AssetManager;
import terrain.VoxelWorld;
import utils.GameController;

/**
 *
 * @author S.S.Iyer
 */
public class GlobalState {
    public final static AssetManager assetsManager = new AssetManager();
    public final static int UNITS_PER_METER = 16;
    //public final static float[] fogColour = new float[]{0.13f, 0.13f, 0.13f, 1.0f};
    public final static float[] fogColour = new float[]{0.54f, 0.62f, 0.69f, 1f};
    public static boolean fullScreen = false;
    public static boolean descendLimit = true;
    public static boolean enableWireframe = false;
    public static boolean cursorCaught = true;
    public static boolean isFirstPerson = false;
    public static int oldWidth, oldHeight;
    public static int visibleCount;
    public static float worldScale;
    public static float[][] heightMap;
    public static int[][] intHeightMap;
    public static int widthField;
    public static int depthField;
    public static int latestPlayerCapture;
    public static int latestAiCapture;
    public final static ScoreBoard scoreBoard;
    public final static FlagsManager flagsManager;
    public final static Minimap miniMap;
    public final static GameController gameController;
    public static VoxelWorld voxelWorld;
    public static int gameCount = 0;
    public static boolean started = false;
    public static boolean respawnP = true;
    public static boolean respawnAI = true;
    public static boolean treeLocations[][];
    
    static {
        scoreBoard = new ScoreBoard(250);
        flagsManager = new FlagsManager(5);
        miniMap = new Minimap();
        gameController = new GameController();
    }
    
    public static void init(VoxelWorld world) {
        voxelWorld = world;
        widthField = world.chunksX * UNITS_PER_METER;
        depthField = world.chunksZ * UNITS_PER_METER;
    }
    
    public static void dispose() {
        assetsManager.dispose();
    }
}
