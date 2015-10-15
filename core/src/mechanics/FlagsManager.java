package mechanics;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import java.util.Random;
import utils.GameInfo;

/**
 *
 * @author Kevin van Eenige and Daniël van der Laan
 */
public class FlagsManager {
    
    private final Array<Flag> flagList;
    public final int NROF_FLAGS;
    private final Array<Vector3> flagPositions;
    private final Random random = new Random();
    private final Vector3 tmp = new Vector3();

    public FlagsManager(int NROF_FLAGS) {
        this.NROF_FLAGS = NROF_FLAGS;
        flagList = new Array<>();
        flagPositions = new Array<>();
        
        int quadrant;
        for (int i = 0; i < NROF_FLAGS; i++) {
            quadrant = i % 5;
            if (quadrant == 0) {
                int x = random.nextInt((GameInfo.widthField/3 - 20) + 1) + 20;
                int y = 0;
                int z = random.nextInt((GameInfo.heightField/2 - 20) + 1) + 20;
                flagPositions.add(tmp.set(x,y,z));
                flagList.add(new Flag(tmp, 5));
            } else if (quadrant == 1) {
                int x = random.nextInt((GameInfo.widthField/3 - 20) + 1) + 20;
                int y = 0;
                int z = random.nextInt((GameInfo.heightField-20 - GameInfo.heightField/2) + 1) + GameInfo.heightField/2;
                flagPositions.add(tmp.set(x,y,z));
                flagList.add(new Flag(tmp.set(x,y,z), 5));
            } else if (quadrant == 2) {
                int x = random.nextInt((GameInfo.widthField/3*2 - GameInfo.widthField/3) + 1) + GameInfo.widthField/3;
                int y = 0;
                int z = random.nextInt((GameInfo.heightField/4*3 - GameInfo.heightField/4) + 1) + GameInfo.heightField/4;
                flagPositions.add(tmp.set(x,y,z));
                flagList.add(new Flag(tmp.set(x,y,z), 5));
            } else if (quadrant == 3) {
                int x = random.nextInt((GameInfo.widthField-20 - GameInfo.widthField/3*2) + 1) + GameInfo.widthField/3*2;
                int y = 0;
                int z = random.nextInt((GameInfo.heightField/2 - 20) + 1) + 20;
                flagPositions.add(tmp.set(x,y,z));
                flagList.add(new Flag(tmp.set(x,y,z), 5));
            } else if(quadrant == 4) {
                int x = random.nextInt((GameInfo.widthField-20 - GameInfo.widthField/3*2) + 1) + GameInfo.widthField/3*2;
                int y = 0;
                int z = random.nextInt((GameInfo.heightField-20 - GameInfo.heightField/2) + 1) + GameInfo.heightField/2;
                flagPositions.add(tmp.set(x,y,z));
                flagList.add(new Flag(tmp.set(x,y,z), 5));
            }
        }
    }

    public Array<Flag> getFlagsList() {
        return flagList;
    }

    public Array<Vector3> getFlagPositions() {
        return flagPositions;
    }

    public void setOccupant(int index, Flag.Occupant occupant) {
        flagList.get(index).setOccupant(occupant);
    }

    public Flag.Occupant getOccupant(int index) {
        return flagList.get(index).getOccupant();
    }

    public int getFlagWeight(int index) {
        return flagList.get(index).getScoreWeight();
    }

    public Vector3 getFlagPosition(int index) {
        return flagList.get(index).getPosition();
    }

}
