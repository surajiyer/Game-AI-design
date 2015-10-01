/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

/**
 *
 * @author s138699
 */
public class Player {

//    private int x;
//    private int y;
//    private int z;
//    private int flags;
    Vector3 position = new Vector3();
    Rectangle bounds = new Rectangle();
    static final float SIZE = 0.5f; // half a unit

    public Player(Vector3 position) {
        this.position = position;
        this.bounds.height = SIZE;
        this.bounds.width = SIZE;
    }

}
