package utils;

import mechanics.Flag;
import mechanics.FlagList;
import mechanics.Score;

/**
 *
 * @author s138699
 */
public class GameInfo {
    
    public static FlagList flagList;
    public static Score score;
    
    static int latestPlayerCapture;
    static int latestAiCapture;
    
    public static void InitializeGameInfo() {
        flagList = new FlagList(5);
        score = new Score();
    }
    
    public static int getLatestPlayerCapture() {
        return latestPlayerCapture;
    }
    
    public static void setLatestPlayerCapture(int capture) {
        latestPlayerCapture = capture;
    }
    
    public static FlagList getFlagList() {
        return flagList;
    }
    
    public static Score getScore() {
        return score;
    }
    
}
