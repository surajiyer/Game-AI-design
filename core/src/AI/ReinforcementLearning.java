package AI;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;
import mechanics.Flag.Occupant;
import mechanics.GlobalState;
import mechanics.Player;
import mechanics.State;

/**
 *
 * @author Kevin van Eenige and Daniël van der Laan
 */
public class ReinforcementLearning {
    
    public static double PJOG = 1;
    public static double LearningRate = 2;
    public static double Epsilon = 3;
    public static double DecayingLR = 4;
    private final double maxLearningRate = 0.7;
    private double epsilon;
    private final int pathCost = 1;
    private final int[][] policy;
    private final double[][][] qsa;
    private final State start, currState;
    private final Player AIPlayer;

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
    float[][][] tileCost;
    PathCostArray pathcostarray;
    int[] closestFlagArray;
    Astar astar;

    public ReinforcementLearning(Player AIPlayer) {
        this.AIPlayer = AIPlayer;
        tileCost = TileCostArray.generateTileCostArray(GlobalState.widthField, GlobalState.depthField, 
                GlobalState.intHeightMap, 8, 
                GlobalState.widthField - 1, 
                GlobalState.depthField - 1);
        pathcostarray = new PathCostArray(GlobalState.widthField, 
                GlobalState.depthField, 
                GlobalState.flagsManager.getFlagPositions(), 
                GlobalState.flagsManager.getNumberOfFlags(), tileCost);
        closestFlagArray = new int[GlobalState.flagsManager.getNumberOfFlags()];
        closestFlagArray = pathcostarray.generateClosestFlagArrayAtLocation(pathCost, pathCost);
        start = new State(closestFlagArray[0], 0);
        currState = new State(closestFlagArray[0], 0);
        policy = new int[5][5];
        qsa = new double[GlobalState.flagsManager.getNumberOfFlags()]
                [GlobalState.flagsManager.getNumberOfFlags()]
                [GlobalState.flagsManager.getNumberOfFlags()];
        astar = new Astar(GlobalState.widthField, GlobalState.depthField);
        init();
    }

    private void init() {
        learningRate = maxLearningRate;
        numEpisodes = 0;
        //Initialise the qsa array with random numbers
        for (double[][] qsa1 : qsa) {
            for (double[] qsa11 : qsa1) {
                for (int k = 0; k < qsa11.length; k++) {
                    qsa11[k] = 0;
                }
            }
        }

        //Initialise policy for all states as -1
        for (int[] policy1 : policy) {
            for (int j = 0; j < policy1.length; j++) {
                policy1[j] = -1;
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
        // Select action using epsilon greedy exploration policy
        currAction = chooseAction(currState, MathUtils.random());
        currStateQ = qsa[currState.x][currState.y][currAction];
        scoreDiffPrev = GlobalState.scoreBoard.getCS() - GlobalState.scoreBoard.getPS();
        Vector3 tmp = new Vector3(GlobalState.flagsManager
                .getFlagsList()
                .get(currAction)
                .getPosition().scl(1/GlobalState.worldScale));
        IntArray path = astar.getPath((int) AIPlayer.getPosition().x, 
                (int) AIPlayer.getPosition().z, (int) tmp.x, (int) tmp.z, tileCost);
        return path;
    }

    public boolean evaluate() {
        //currAction = flag die je wilt capturen
        //wachten totdat ai op de flag staat

        //observeer je de nieuwe state
        //Perform choosen action based on pjog (noise of environment)
        nextState = new State(currAction, GlobalState.latestPlayerCapture);
        int scoreDiffNext = GlobalState.scoreBoard.getCS() - GlobalState.scoreBoard.getPS();
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

        isBestAct = choosenAction == bestAction;

        return choosenAction;
    }

    private int getBestAction(double[] actions) {
        closestFlagArray = pathcostarray.generateClosestFlagArray(0);
        int bestAction = 0;
        double min = actions[0];
        for (int i = 0; i < actions.length; i++) {
            if (min > actions[i] && !GlobalState.flagsManager.getOccupant(i).equals(Occupant.AI)) {
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
