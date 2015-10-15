package utils;

import mechanics.FlagsManager;
import mechanics.Player;
import mechanics.Score;
import terrain.VoxelWorld;

/**
 *
 * @author Kevin van Eenige and Daniël van der Laan
 */
public class GameInfo {
    
    public static FlagsManager flagsManager;
    public static Score score;
    public static VoxelWorld world;
    public static float[][] heightMap;
    public static int[][] intHeightMap;
    public static int widthField = 320;
    public static int heightField = 320;
    public static Player AI;
    
    static int latestPlayerCapture;
    static int latestAiCapture;
    
    public static void InitializeGameInfo() {
        flagsManager = new FlagsManager(5);
        score = new Score();
    }
    
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
    
}
