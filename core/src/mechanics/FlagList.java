package mechanics;

import java.util.Random;

/**
 *
 * @author Kevin van Eenige and Daniël van der Laan
 */
public class FlagList {

    private Flag[] flagList;
    Random random = new Random();
    int[] allCoordinates;

    public FlagList(int NROF_FLAGS) {
        flagList = new Flag[NROF_FLAGS];
        allCoordinates = new int[NROF_FLAGS * 2];
        for (int i = 0; i < NROF_FLAGS; i++) {
            if (i == 0) {
                int x = random.nextInt((93 - 10) + 1) + 10;
                allCoordinates[i * 2] = x;
                int y = 0;
                int z = random.nextInt((100 - 10) + 1) + 10;
                allCoordinates[i * 2 + 1] = z;
                Flag flag = new Flag(x, y, z, i, 5);
                flagList[i] = flag;
            } else if (i == 1) {
                int x = random.nextInt((93 - 10) + 1) + 10;
                allCoordinates[i * 2] = x;
                int y = 0;
                int z = random.nextInt((190 - 100) + 1) + 100;
                allCoordinates[i * 2 + 1] = z;
                Flag flag = new Flag(x, y, z, i, 5);
                flagList[i] = flag;
            } else if (i == 2) {
                int x = random.nextInt((186 - 93) + 1) + 93;
                allCoordinates[i * 2] = x;
                int y = 0;
                int z = random.nextInt((150 - 50) + 1) + 50;
                allCoordinates[i * 2 + 1] = z;
                Flag flag = new Flag(x, y, z, i, 5);
                flagList[i] = flag;
            } else if (i == 3) {
                int x = random.nextInt((270 - 186) + 1) + 186;
                allCoordinates[i * 2] = x;
                int y = 0;
                int z = random.nextInt((100 - 10) + 1) + 10;
                allCoordinates[i * 2 + 1] = z;
                Flag flag = new Flag(x, y, z, i, 5);
                flagList[i] = flag;
            } else if(i==4) {
                int x = random.nextInt((270 - 186) + 1) + 186;
                allCoordinates[i * 2] = x;
                int y = 0;
                int z = random.nextInt((190 - 100) + 1) + 100;
                allCoordinates[i * 2 + 1] = z;
                Flag flag = new Flag(x, y, z, i, 5);
                flagList[i] = flag;
            }
        }
    }

    public Flag[] getList() {
        return flagList;
    }

    public int[] getAllFlagCoordinates() {
        return allCoordinates;
    }

    public void setOccupant(int index, String occupant) {
        flagList[index].setOccupant(occupant);
    }

    public String getOccupant(int index) {
        return flagList[index].getOccupant();
    }

    public int getFlagWeight(int index) {
        return flagList[index].getScoreWeight();
    }

    public int[] getCoordinates(int index) {
        return flagList[index].getCoordinates();
    }

}
