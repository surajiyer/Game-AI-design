package mechanics;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import utils.Drawables;

/**
 *
 * @author Kevin van Eenige and Daniël van der Laan
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
