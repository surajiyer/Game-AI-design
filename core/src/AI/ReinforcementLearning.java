package AI;

import java.util.Random;
import main.Basic3DTest1;
import mechanics.FlagList;
import mechanics.State;
import utils.GameInfo;

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

    public boolean isBestAct = true;
    public boolean receivedPenalty = false;
    private int numEpisodes;

    double learningRate;

    int[][] optPolicy;
    boolean isOptValCalc;
    double PRECISION = 0.01;
    FlagList flagList;

    public ReinforcementLearning(Basic3DTest1 basic) {
        start = new State(0, 0);
        currState = new State(0, 0);
        rand = new Random();
        this.basic = basic;
        flagList = GameInfo.flagList;
        policy = new int[5][5];
        qsa = new double[flagList.getList().length][flagList.getList().length][flagList.getList().length];
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

    public boolean step() {
        double transitionCost;
        int currAction;
        State nextState;

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
        
        //Select action using epsilon greedy exploration policy
        currAction = chooseAction(currState, rand.nextDouble());
        double currStateQ = qsa[currState.x][currState.y][currAction];
        int scoreDiffPrev = GameInfo.score.getCS() - GameInfo.score.getPS();
        
        //currAction = flag die je wilt capturen
        //wachten totdat ai op de flag staat
        
//        while(!waitForCapture(currAction)) {
//            
//            
//        }
        
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
        currStateQ = currStateQ * (1 - learningRate) + (learningRate * (scoreDiffNext - scoreDiffPrev));
        
        qsa[currState.x][currState.y][currAction] = currStateQ;
		//System.out.println("qsa after =="+qsa[currState.x][currState.y][0]+","+qsa[currState.x][currState.y][1]+","+qsa[currState.x][currState.y][2]+","+qsa[currState.x][currState.y][3]);			

        //policy[currState.x][currState.y] = getBestAction(qsa[currState.x][currState.y]);
        policy[currState.x][currState.y] = getBestAction(qsa[currState.x][currState.y]);
        //System.out.println("policy= "+policy[currState.x][currState.y]);
        currState.copy(nextState);

        return false;
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
        double min = actions[0];
        int bestAction = 0;
        for (int i = 1; i < actions.length; i++) {
            if (min > actions[i]) {
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
