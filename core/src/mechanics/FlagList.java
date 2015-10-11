/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mechanics;

import java.util.List;
import java.util.Random;

/**
 *
 * @author s138699
 */
public class FlagList {

    private Flag[] flagList;
    Random random = new Random();
    
    public FlagList(int NROF_FLAGS) {
        flagList = new Flag[NROF_FLAGS];
        for (int i = 0; i < NROF_FLAGS; i++) {
            int x = random.nextInt((50 - 10) + 1) + 10;
            int y = 0;
            int z = random.nextInt((50 - 10) + 1) + 10;
            Flag flag = new Flag(x, y, z, i, 5);
            flagList[i] = flag;
        }
    }

    public Flag[] getList() {
        return flagList;
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
