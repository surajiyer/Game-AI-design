package mechanics;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import mechanics.Flag.Occupant;
import static mechanics.GlobalState.UNITS_PER_METER;
import terrain.VoxelWorld;

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
    
    public void generateFlags(VoxelWorld world) {
        flagList.clear();
        flagPositions.clear();
        float scale = world.getScale();
        int x = 0, y, z = 0, quadrant;
        for (int i = 0; i < NROF_FLAGS; i++) {
            quadrant = i % 5;
            if (quadrant == 0) {
                x = MathUtils.random((GlobalState.widthField/3 - 20)) + 20;
                z = MathUtils.random((GlobalState.depthField/2 - 20)) + 20;
            } else if (quadrant == 1) {
                x = MathUtils.random((GlobalState.widthField/3 - 20)) + 20;
                z = MathUtils.random((GlobalState.depthField-20 - GlobalState.depthField/2)) + GlobalState.depthField/2;
            } else if (quadrant == 2) {
                x = MathUtils.random((GlobalState.widthField/3*2 - GlobalState.widthField/3)) + GlobalState.widthField/3;
                z = MathUtils.random((GlobalState.depthField/4*3 - GlobalState.depthField/4)) + GlobalState.depthField/4;
            } else if (quadrant == 3) {
                x = MathUtils.random((GlobalState.widthField-20 - GlobalState.widthField/3*2)) + GlobalState.widthField/3*2;
                z = MathUtils.random((GlobalState.depthField/2 - 20)) + 20;
            } else if(quadrant == 4) {
                x = MathUtils.random((GlobalState.widthField-20 - GlobalState.widthField/3*2)) + GlobalState.widthField/3*2;
                z = MathUtils.random((GlobalState.depthField-20 - GlobalState.depthField/2)) + GlobalState.depthField/2;
            }
            y = (int) world.getHeight(x*=scale, z*=scale);
            flagPositions.add(tmp.set(x, y, z));
            flagList.add(new Flag(tmp, 5));
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
    
    public void captureFlag(Player player) {
        Vector3 position = player.getPosition();
        for(Flag flag : flagList) {
            if(position.dst(flag.getPosition()) > UNITS_PER_METER) continue;
            if(flag.getOccupant() == Occupant.NONE 
                || (flag.getOccupant() == Occupant.AI && player.type == Player.PlayerType.HUMAN) 
                || (flag.getOccupant() == Occupant.PLAYER && player.type == Player.PlayerType.AI)) {
                switch(player.type) {
                    case HUMAN:
                        flag.setOccupant(Occupant.PLAYER);
                        break;
                    case AI:
                        flag.setOccupant(Occupant.AI);
                }
                break;
            }
        }
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
