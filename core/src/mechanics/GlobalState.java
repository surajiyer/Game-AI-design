/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mechanics;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import mechanics.Flag.Occupant;
import utils.ConcreteGameObject;
import utils.GameController;

/**
 *
 * @author S.S.Iyer
 */
public class GlobalState {
    public final static AssetManager assetsManager = new AssetManager();
    public final static int UNITS_PER_METER = 16;
    public static boolean fullScreen = false;
    public static boolean descendLimit = true;
    public static boolean enableWireframe = false;
    public static boolean cursorCaught = true;
    public static boolean isFirstPerson = false;
    public static int oldWidth, oldHeight;
    public static int visibleCount;
    public static float[][] heightMap;
    public static int[][] intHeightMap;
    public static int widthField = 320;
    public static int heightField = 320;
    public static int latestPlayerCapture;
    public static int latestAiCapture;
    public final static ScoreBoard scoreBoard = new ScoreBoard();
    public final static FlagsManager flagsManager = new FlagsManager(5);
    public final static Minimap miniMap = new Minimap();
    public final static GameController gameController = new GameController();
    
    public static void dispose() {
        assetsManager.dispose();
    }
}
