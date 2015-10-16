package mechanics;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import mechanics.Flag.Occupant;

/**
 *
 * @author Kevin van Eenige and Daniël van der Laan
 */
public class FlagsManager {
    
    private final Array<Flag> flagList;
    public final int NROF_FLAGS;
    private final Array<Vector3> flagPositions;
    private final Vector3 tmp = new Vector3();

    public FlagsManager(int NROF_FLAGS) {
        this.NROF_FLAGS = NROF_FLAGS;
        flagList = new Array<>();
        flagPositions = new Array<>();
    }
    
    public void generateFlags() {
        flagList.clear();
        flagPositions.clear();
        int quadrant;
        for (int i = 0; i < NROF_FLAGS; i++) {
            quadrant = i % 5;
            if (quadrant == 0) {
                int x = MathUtils.random((GlobalState.widthField/3 - 20)) + 20;
                int y = 0;
                int z = MathUtils.random((GlobalState.heightField/2 - 20)) + 20;
                flagPositions.add(tmp.set(x,y,z));
                flagList.add(new Flag(tmp, 5));
            } else if (quadrant == 1) {
                int x = MathUtils.random((GlobalState.widthField/3 - 20)) + 20;
                int y = 0;
                int z = MathUtils.random((GlobalState.heightField-20 - GlobalState.heightField/2)) + GlobalState.heightField/2;
                flagPositions.add(tmp.set(x,y,z));
                flagList.add(new Flag(tmp.set(x,y,z), 5));
            } else if (quadrant == 2) {
                int x = MathUtils.random((GlobalState.widthField/3*2 - GlobalState.widthField/3)) + GlobalState.widthField/3;
                int y = 0;
                int z = MathUtils.random((GlobalState.heightField/4*3 - GlobalState.heightField/4)) + GlobalState.heightField/4;
                flagPositions.add(tmp.set(x,y,z));
                flagList.add(new Flag(tmp.set(x,y,z), 5));
            } else if (quadrant == 3) {
                int x = MathUtils.random((GlobalState.widthField-20 - GlobalState.widthField/3*2)) + GlobalState.widthField/3*2;
                int y = 0;
                int z = MathUtils.random((GlobalState.heightField/2 - 20)) + 20;
                flagPositions.add(tmp.set(x,y,z));
                flagList.add(new Flag(tmp.set(x,y,z), 5));
            } else if(quadrant == 4) {
                int x = MathUtils.random((GlobalState.widthField-20 - GlobalState.widthField/3*2)) + GlobalState.widthField/3*2;
                int y = 0;
                int z = MathUtils.random((GlobalState.heightField-20 - GlobalState.heightField/2)) + GlobalState.heightField/2;
                flagPositions.add(tmp.set(x,y,z));
                flagList.add(new Flag(tmp.set(x,y,z), 5));
            }
        }
    }
    
    public int getNumberOfFlags() {
        return NROF_FLAGS;
    }

    public Array<Flag> getFlagsList() {
        return flagList;
    }

    public Array<Vector3> getFlagPositions() {
        return flagPositions;
    }

    public void setOccupant(int index, Occupant occupant) {
        flagList.get(index).setOccupant(occupant);
    }

    public Occupant getOccupant(int index) {
        return flagList.get(index).getOccupant();
    }

    public int getFlagWeight(int index) {
        return flagList.get(index).getScoreWeight();
    }

    public Vector3 getFlagPosition(int index) {
        return flagList.get(index).getPosition();
    }

}
