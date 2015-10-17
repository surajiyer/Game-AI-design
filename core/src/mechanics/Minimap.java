/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mechanics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import static mechanics.GlobalState.worldScale;

/**
 *
 * @author S.S.Iyer
 */
public class Minimap {
    public static final int miniWidth = 800;
    public static final int miniHeight = 480;
    public static final int miniScale = 4;
    public static final int markerSize = 20;
    public static final int miniMapLeft = 0;
    public static final int miniMapRight = 200;
    public static final int miniMapTop = 480;
    public static final int miniMapBottom = 280;
    
    private final Texture playerMarker;
    private final Texture flagMarkerBlue;
    private final Texture flagMarkerRed;
    private final Texture flagMarkerGrey;
    private Texture flagTexture;
    private final Sprite mapSprite;
    private final Sprite hudMapSprite;
    private final Vector3 tmp = new Vector3();
    
    public Minimap() {
        playerMarker = new Texture(Gdx.files.internal("markers/playerdot.png"));
        mapSprite = new Sprite(new Texture(Gdx.files.internal("simplexmap.png")));
        flagMarkerBlue = new Texture(Gdx.files.internal("markers/blue.png"));
        flagMarkerRed = new Texture(Gdx.files.internal("markers/red.png"));
        flagMarkerGrey = new Texture(Gdx.files.internal("markers/grey.png"));
        hudMapSprite = new Sprite(new Texture(Gdx.files.internal("markers/hudMap.png")));
    }
    
    public void draw(SpriteBatch spriteBatch, Camera camera, Array<Player> players) {
        spriteBatch.draw(hudMapSprite, hudMapSprite.getX()-6, hudMapSprite.getY()-6);
        spriteBatch.draw(mapSprite, mapSprite.getX(), mapSprite.getY());
        
        // Load flag marker for each flag
        for (int i = 0; i < 5; i++) {
            switch (GlobalState.flagsManager.getOccupant(i)) {
                case AI:
                    flagTexture = flagMarkerRed;
                    break;
                case PLAYER:
                    flagTexture = flagMarkerBlue;
                    break;
                case NONE:
                    flagTexture = flagMarkerGrey;
                    break;
            }
            tmp.set(GlobalState.flagsManager.getFlagPosition(i)).scl(1/worldScale);
            spriteBatch.draw(flagTexture, tmp.z, tmp.x);
        }
        
        // Load markers for all players
        for(Player player : players) {
            tmp.set(player.getPosition().scl(1/worldScale));
            spriteBatch.draw(playerMarker, tmp.z, tmp.x);
        }
        
    }
}
