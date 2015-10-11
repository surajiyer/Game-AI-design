/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mechanics;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import utils.Drawables;

/**
 *
 * @author s138699
 */
public class Flag {

    private int x;
    private int y;
    private int z;
    private int number;
    private int scoreWeight;
    private String occupant;
    private ModelInstance flagBox;
    private ModelInstance captureBox;
    
    public Flag(int x, int y, int z, int number, int scoreWeight) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.number = number;
        this.scoreWeight = scoreWeight;
        this.occupant = "None";
    }

    public int getScoreWeight() {
        return scoreWeight;
    }

    public int getNumber() {
        return number;
    }

    public String getOccupant() {
        return occupant;
    }

    public void setOccupant(String occupant) {
        this.occupant = occupant;
    }

    public int[] getCoordinates() {
        int[] coordinates = {x,y,z};
        return coordinates;
    }
    
    public void setFlagBox(ModelInstance instance) {
        flagBox = Drawables.drawBoundingBox(instance, true);
    }
    
    public ModelInstance getFlagBox() {
        return flagBox;
    }
    
    public void setCaptureBox(ModelInstance instance) {
        captureBox = Drawables.drawBoundingBox(instance, true);
    }
    
    public ModelInstance getCaptureBox() {
        return captureBox;
    }

}
