/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

/**
 *
 * @author s138699
 */
public class Score {
    public static int playerScore;
    public static int computerScore;
    
    public Score() {
        playerScore = 0;
        computerScore = 0;
    }
    
    public int getPS() {
        return playerScore;
    }
    
    public int getCS() {
        return computerScore;
    }
    
    public void setPS(int score) {
        playerScore = score;
    } 
    
    public void setCS(int score) {
        computerScore = score;
    }    
}
