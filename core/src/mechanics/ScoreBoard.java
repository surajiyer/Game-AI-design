package mechanics;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.concurrent.TimeUnit;
import mechanics.Flag.Occupant;
import static mechanics.GlobalState.flagsManager;

/**
 *
 * @author Kevin van Eenige and Daniël van der Laan
 */
public class ScoreBoard {
    private int playerScore;
    private int computerScore;
    private String pString;
    private String cString;
    private int scoreTime;
    boolean toScore;
    long startTime;
    long elapsedTime; 
    
    public ScoreBoard() {
        playerScore = 0;
        computerScore = 0;
        scoreTime = 3;
        toScore = true;
        startTime = System.currentTimeMillis();
        elapsedTime = 0;
    }
    
    public void draw(SpriteBatch spriteBatch, Camera camera, BitmapFont font) {
        font.setColor(0,0,0,1);
        font.draw(spriteBatch, "" + getPS(), camera.viewportWidth/2 - 90, camera.viewportHeight - 20);
        font.draw(spriteBatch, "" + getCS(), camera.viewportWidth/2 + 80, camera.viewportHeight - 20);
        font.draw(spriteBatch, "" + String.valueOf(elapsedTime), camera.viewportWidth/2 -3, 
                camera.viewportHeight - 20);
    }
    
    public int getPS() {
        return playerScore;
    }
    
    public int getCS() {
        return computerScore;
    }
    
    public void setPS(int score) {
        playerScore = score;
        pString = "Player Score: " + playerScore;
    } 
    
    public void setCS(int score) {
        computerScore = score;
        cString = "AI Score: " + computerScore;
    }
    
    public void addPS(int score) {
        playerScore += score;
        pString = "Player Score: " + playerScore;
    } 
    
    public void addCS(int score) {
        computerScore += score;
        cString = "AI Score: " + computerScore;
    }    
    
    public void updateScore() {
        elapsedTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);
        if(elapsedTime % scoreTime == 0 && toScore && elapsedTime != 0) {
            for(int i = 0; i < flagsManager.getFlagsList().size; i++) {
                if(flagsManager.getOccupant(i) == Occupant.PLAYER) {
                    addPS(flagsManager.getFlagWeight(i));
                }else if(flagsManager.getOccupant(i) == Flag.Occupant.AI){
                    addCS(flagsManager.getFlagWeight(i));
                }
            }
            toScore = false;
        } else if (elapsedTime % scoreTime != 0) {
            toScore = true;
        }
    }
}
