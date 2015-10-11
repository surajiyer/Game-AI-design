/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mechanics;

import java.util.List;

/**
 *
 * @author s138699
 */
public class Score {
    private int playerScore;
    private int computerScore;
    private String pString;
    private String cString;
    private int scoreTime;
    boolean toScore;
    
    public Score() {
        playerScore = 0;
        computerScore = 0;
        scoreTime = 3;
        toScore = true;
    }
    
    public int getPS() {
        return playerScore;
    }
    
    public int getCS() {
        return computerScore;
    }
    
    public void setPS(int score) {
        playerScore = score;
        pString = "Player Score: " + playerScore;
    } 
    
    public void setCS(int score) {
        computerScore = score;
        cString = "AI Score: " + computerScore;
    }
    
    public void addPS(int score) {
        playerScore += score;
        pString = "Player Score: " + playerScore;
    } 
    
    public void addCS(int score) {
        computerScore += score;
        cString = "AI Score: " + computerScore;
    }    
    
    public void updateScore(long elapsedTime, FlagList flagList) { 
        if(elapsedTime%scoreTime == 0 && toScore && elapsedTime != 0) {
            for(int i = 0; i < flagList.getList().length; i++) {
                if(flagList.getOccupant(i).equals("Player")) {
                    addPS(flagList.getFlagWeight(i));
                }else if(flagList.getOccupant(i).equals("AI")){
                    addCS(flagList.getFlagWeight(i));
                }
            }
            toScore = false;
        } else if (elapsedTime%scoreTime != 0) {
            toScore = true;
        }
    }
}
