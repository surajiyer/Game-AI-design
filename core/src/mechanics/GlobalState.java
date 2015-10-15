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
import utils.ConcreteGameObject;
import utils.GameController;
import utils.GameInfo;

/**
 *
 * @author S.S.Iyer
 */
public class GlobalState {
    public static int visibleCount;
    public static GameController gameController;
    public static AssetManager assetsManager;
    public static boolean assetLoading;
    public static boolean fullScreen = false;
    public static boolean descendLimit = true;
    public static boolean enableWireframe = false;
    public static boolean cursorCaught = true;
    public static boolean isFirstPerson = false;
    public static int oldWidth, oldHeight;
    public static Texture voxelTextures;
    
    public static void dispose() {
        assetsManager.dispose();
    }
    
    public static void assetsLoading(Array<ConcreteGameObject> instances) {
        Vector3 tmp = new Vector3();
        
        // Load the tower
        ConcreteGameObject gameObject = new ConcreteGameObject(assetsManager.get("tower/tower.g3db", Model.class));
        instances.add(gameObject);
        
        // Load tree 1
        gameObject = new ConcreteGameObject(assetsManager.get("trees/tree1.g3db", Model.class));
        gameObject.setPosition(tmp.set(0, 0, -12*UNITS_PER_METER));
        instances.add(gameObject);
        
        // Load tree 2
        gameObject = new ConcreteGameObject(assetsManager.get("trees/tree2.g3db", Model.class));
        gameObject.setPosition(tmp.set(-12*UNITS_PER_METER, 0, -6*UNITS_PER_METER));
        instances.add(gameObject);
        
        // Load tree 3
        gameObject = new ConcreteGameObject(assetsManager.get("trees/tree3.g3db", Model.class));
        gameObject.setPosition(tmp.set(-12*UNITS_PER_METER, 0, 6*UNITS_PER_METER));
        instances.add(gameObject);
        
        // Load tree 4
        gameObject = new ConcreteGameObject(assetsManager.get("trees/tree4.g3db", Model.class));
        gameObject.setPosition(tmp.set(0, 0, 12*UNITS_PER_METER));
        instances.add(gameObject);
        
        // Load the red flag
        Flag.redFlag = new ModelInstance(assetsManager.get("flags/flagRed.g3db", Model.class));
        
        // Load the blue flag
        Flag.blueFlag = new ModelInstance(assetsManager.get("flags/flagBlue.g3db", Model.class));
        
        // Load the uncaptured flag
        Flag.noneFlag = new ModelInstance(assetsManager.get("flags/flagNone.g3db", Model.class));
        
        // Load all the flags
        Array<Flag> flags = GameInfo.flagsManager.getList();
        for(int i = 0; i < flags.size; i++) {
            Vector3 pos = GameInfo.flagsManager.getFlagPosition(i);
            gameObject.setPosition(tmp.set(pos).scl(UNITS_PER_METER));
            instances.add(gameObject);
        }
        
        // Done loading assets
        assetLoading = false;
    }
}
