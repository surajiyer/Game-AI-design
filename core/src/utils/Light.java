/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.badlogic.gdx.math.Vector3;

/**
 *
 * @author S.S.Iyer
 */
public class Light {
    private final Vector3 position;
    private final Vector3 color;
    private final Vector3 attenuation = new Vector3(1, 0, 0);
    
    public Light(Vector3 pos, Vector3 color, Vector3 attenuation) {
        this.position = new Vector3(pos);
        this.color = new Vector3(color);
        this.attenuation.set(attenuation);
    }
    
    public Vector3 getPosition() {
        return new Vector3(position);
    }
    
    public Vector3 getAttenuation() {
        return new Vector3(attenuation);
    }
    
    public void setAttenuation(Vector3 attenuation) {
        this.attenuation.set(attenuation);
    }
    
    public Vector3 getColor() {
        return new Vector3(color);
    }
    
    public void setPosition(Vector3 pos) {
        this.position.set(pos);
    }
    
    public void setColor(Vector3 color) {
        this.color.set(color);
    }
}
