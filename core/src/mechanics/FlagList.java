package mechanics;

import java.util.Random;
import terrain.VoxelWorld;
import utils.GameInfo;

/**
 *
 * @author Kevin van Eenige and Daniël van der Laan
 */
public class FlagList {

    private Flag[] flagList;
    Random random = new Random();
    int[] allCoordinates;
    VoxelWorld voxelworld;

    public FlagList(int NROF_FLAGS, VoxelWorld voxelworld) {
        this.voxelworld = voxelworld;
        flagList = new Flag[NROF_FLAGS];
        allCoordinates = new int[NROF_FLAGS * 2];
        for (int i = 0; i < NROF_FLAGS; i++) {
            if (i == 0) {
                int x = random.nextInt((GameInfo.widthField/3 - 20) + 1) + 20;
                allCoordinates[i * 2] = x;
                int z = random.nextInt((GameInfo.heightField/2 - 20) + 1) + 20;
                allCoordinates[i * 2 + 1] = z;
                int y = (int) voxelworld.getHeight(x, z);
                Flag flag = new Flag(x, y, z, i, 5);
                flagList[i] = flag;
            } else if (i == 1) {
                int x = random.nextInt((GameInfo.widthField/3 - 20) + 1) + 20;
                allCoordinates[i * 2] = x;
                int z = random.nextInt((GameInfo.heightField-20 - GameInfo.heightField/2) + 1) + GameInfo.heightField/2;
                allCoordinates[i * 2 + 1] = z;
                int y = (int) voxelworld.getHeight(x, z);
                Flag flag = new Flag(x, y, z, i, 5);
                flagList[i] = flag;
            } else if (i == 2) {
                int x = random.nextInt((GameInfo.widthField/3*2 - GameInfo.widthField/3) + 1) + GameInfo.widthField/3;
                allCoordinates[i * 2] = x;
                int z = random.nextInt((GameInfo.heightField/4*3 - GameInfo.heightField/4) + 1) + GameInfo.heightField/4;
                allCoordinates[i * 2 + 1] = z;
                int y = (int) voxelworld.getHeight(x, z);
                Flag flag = new Flag(x, y, z, i, 5);
                flagList[i] = flag;
            } else if (i == 3) {
                int x = random.nextInt((GameInfo.widthField-20 - GameInfo.widthField/3*2) + 1) + GameInfo.widthField/3*2;
                allCoordinates[i * 2] = x;
                int z = random.nextInt((GameInfo.heightField/2 - 20) + 1) + 20;
                allCoordinates[i * 2 + 1] = z;
                int y = (int) voxelworld.getHeight(x, z);
                Flag flag = new Flag(x, y, z, i, 5);
                flagList[i] = flag;
            } else if(i==4) {
                int x = random.nextInt((GameInfo.widthField-20 - GameInfo.widthField/3*2) + 1) + GameInfo.widthField/3*2;
                allCoordinates[i * 2] = x;
                int z = random.nextInt((GameInfo.heightField-20 - GameInfo.heightField/2) + 1) + GameInfo.heightField/2;
                allCoordinates[i * 2 + 1] = z;
                int y = (int) voxelworld.getHeight(x, z);
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
