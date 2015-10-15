package AI;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;
import java.util.Random;
import main.Basic3DTest1;
import mechanics.AIController;
import mechanics.Flag;
import mechanics.Flag.Occupant;
import mechanics.FlagsManager;
import mechanics.State;
import utils.GameInfo;
import static utils.GameInfo.AI;

/**
 *
 * @author Kevin van Eenige and Daniël van der Laan
 */
public class ReinforcementLearning {

    //state should inlcude the flaglist and the position of the players.
    Basic3DTest1 basic;
    public static double PJOG = 1;
    public static double LearningRate = 2;
    public static double Epsilon = 3;
    public static double DecayingLR = 4;
    private double maxLearningRate = 0.7, pjog, epsilon;
    private final int pathCost = 1;
    private int[][] policy;
    private double[][][] qsa;
    State start, currState;
    Random rand;

    AIController aiController;

    public boolean isBestAct = true;
    public boolean receivedPenalty = false;
    private int numEpisodes;

    int currAction;
    State nextState;
    int scoreDiffPrev;
    double currStateQ;
    double transitionCost;

    double learningRate;

    int[][] optPolicy;
    boolean isOptValCalc;
    double PRECISION = 0.01;
    FlagsManager flagList;
    float[][][] tileCost = new float[GameInfo.widthField][GameInfo.heightField][8];
    PathCostArray pathcostarray;
    int[] closestFlagArray = new int[GameInfo.flagsManager.getFlagsList().size];
    Astar astar;

    public ReinforcementLearning() {
        tileCost = TileCostArray.generateTileCostArray(
                GameInfo.widthField, GameInfo.heightField, 
                GameInfo.intHeightMap, 8, 
                GameInfo.widthField - 1, 
                GameInfo.heightField - 1);
        pathcostarray = new PathCostArray(GameInfo.widthField, 
                GameInfo.heightField, 
                GameInfo.getFlagManager().getFlagPositions(), 
                GameInfo.flagsManager.getFlagsList().size, tileCost);
        closestFlagArray = pathcostarray.generateClosestFlagArrayAtLocation(pathCost, pathCost);
        start = new State(closestFlagArray[0], 0);
        currState = new State(closestFlagArray[0], 0);
        rand = new Random();
        flagList = GameInfo.flagsManager;
        policy = new int[5][5];
        qsa = new double[flagList.getFlagsList().size][flagList.getFlagsList().size][flagList.getFlagsList().size];
        astar = new Astar(GameInfo.widthField, GameInfo.heightField);
        initialize();
    }

    public void initialize() {
        learningRate = maxLearningRate;
        numEpisodes = 0;
        //Initialise the qsa array with random numbers
        for (int i = 0; i < qsa.length; i++) {
            for (int j = 0; j < qsa[i].length; j++) {
                for (int k = 0; k < qsa[i][j].length; k++) {
                    qsa[i][j][k] = 0;
                }
            }
        }

        //Initialise policy for all states as -1
        for (int i = 0; i < policy.length; i++) {
            for (int j = 0; j < policy[i].length; j++) {
                policy[i][j] = -1;
            }
        }
    }

    public IntArray step() {
//        if (reachedGoal(currState)) {
//            currState.copy(start);
//            numEpisodes++;
//            if (decayingLR) {
//                learningRate = (1000.0 * maxLearningRate) / (1000.0 + numEpisodes);
//            } else {
//                learningRate = maxLearningRate;
//            }
//            if (0 == numEpisodes % 1000) {
//                System.out.println(numEpisodes + "," + learningRate);
//            }
//            return true;
//        }
        // Pathcost from all flags to all flags.
        //Select action using epsilon greedy exploration policy
        currAction = chooseAction(currState, rand.nextDouble());
        currStateQ = qsa[currState.x][currState.y][currAction];
        scoreDiffPrev = GameInfo.score.getCS() - GameInfo.score.getPS();

        Vector3 tmp = new Vector3(GameInfo.getFlagManager()
                .getFlagsList()
                .get(currAction)
                .getPosition());
        IntArray path = astar.getPath((int) GameInfo.AI.getPosition().x, 
                (int) GameInfo.AI.getPosition().z, (int) tmp.x, (int) tmp.z, tileCost);
        System.out.println("path found");
        return path;
    }

    public boolean evaluate() {
        //currAction = flag die je wilt capturen
        //wachten totdat ai op de flag staat

        //observeer je de nieuwe state
        //Perform choosen action based on pjog (noise of environment)
        nextState = new State(currAction, GameInfo.getLatestPlayerCapture());
        int scoreDiffNext = GameInfo.score.getCS() - GameInfo.score.getPS();
        //Utility.show(" next st="+nextState.x+","+nextState.y);

        //If not a valid transition stay in same state and add penalty;
        transitionCost = pathCost;
        double nextStateQmin = getMinQsa(nextState);

        //System.out.println("qsa before=="+qsa[currState.x][currState.y][0]+","+qsa[currState.x][currState.y][1]+","+qsa[currState.x][currState.y][2]+","+qsa[currState.x][currState.y][3]);
        //DETERMINE THE VALUE OF THE ACTION
        currStateQ = currStateQ * (1 - learningRate) + (learningRate
                * (scoreDiffNext - scoreDiffPrev));

        qsa[currState.x][currState.y][currAction] = currStateQ;
		//System.out.println("qsa after =="+qsa[currState.x][currState.y][0]+","+qsa[currState.x][currState.y][1]+","+qsa[currState.x][currState.y][2]+","+qsa[currState.x][currState.y][3]);			

        //policy[currState.x][currState.y] = getBestAction(qsa[currState.x][currState.y]);
        policy[currState.x][currState.y] = getBestAction(qsa[currState.x][currState.y]);
        //System.out.println("policy= "+policy[currState.x][currState.y]);
        currState.copy(nextState);

        return true;
    }

    private int chooseAction(State currState, double randNum) {
        int bestAction = getBestAction(qsa[currState.x][currState.y]);
        double d = epsilon / 5;
        int choosenAction = bestAction;

        for (int i = 0; i < 5; i++) {
            if (randNum < (i + 1) * d) {
                choosenAction = i;
                break;
            }
        }

        if (choosenAction == bestAction) {
            isBestAct = true;
        } else {
            isBestAct = false;
        }

        return choosenAction;
    }

    private int getBestAction(double[] actions) {
        closestFlagArray = pathcostarray.generateClosestFlagArray(0);
        int bestAction = 0;
        double min = actions[0];
        for (int i = 0; i < actions.length; i++) {
            if (min > actions[i] && !GameInfo.flagsManager.getOccupant(i).equals(Occupant.AI)) {
                min = actions[i];
                bestAction = i;
            }
        }
        return bestAction;
    }

    private double getMinQsa(State st) {
        double min = qsa[st.x][st.y][0];
        int bestAction = 0;
        for (int i = 0; i < qsa[st.x][st.y].length; i++) {
            if (min > qsa[st.x][st.y][i]) {
                min = qsa[st.x][st.y][i];
                bestAction = i;
            }
        }
        return min;
    }

    public boolean waitForCapture(int flag) {
        return true;
    }

    public double[][][] getQSA() {
        return qsa;
    }
}
