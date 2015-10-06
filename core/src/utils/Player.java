/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.g3d.model.Node;

/**
 *
 * @author s138699
 */
public class Player {
    
    public enum PlayerParts {
        RIGHT_HAND_PART(0), 
        LEFT_HAND_PART(1), 
        RIGHT_LEG_PART(2),
        LEFT_LEG_PART(3),
        TORSO_PART(4),
        HEAD_PART(5);
        
        int nodeNr;
        
        private PlayerParts(int nodeNr) {
            this.nodeNr = nodeNr;
        }
    }    
    
    public final GameObject model;
    public Vector3 position;
    public Vector3 bodyDirection;
    public Vector3 headDirection;
    public int flags;

    public Player(GameObject model, Vector3 position) {
        this(model, position.x, position.y, position.z);
    }
    
    public Player(GameObject model, float x, float y, float z) {
        this.model = model;
        position = new Vector3(x, y, z);
        this.model.transform.setToTranslation(x,y,z);
        bodyDirection = Vector3.X;
        headDirection = Vector3.X;
    }
    
    Vector3 oldHeadDirection;
    Vector3 oldBodyDirection;
    
    public void setHeadDirection(Vector3 tmp) {
        oldHeadDirection = headDirection;
        headDirection = tmp.nor();
    }
    
    public void setBodyDirection(Vector3 tmp) {
        oldBodyDirection = bodyDirection;
        bodyDirection = tmp.nor();
    }
    
    public void update() {
        // Position
        model.transform.trn(position);
        
        // Head rotation
        model.nodes.get(PlayerParts.HEAD_PART.nodeNr).
                calculateWorldTransform().rotate(oldHeadDirection, headDirection);
        
        // Rest of the body rotation
        for(PlayerParts p : PlayerParts.values()) {
            if(p == PlayerParts.HEAD_PART) continue;
            model.nodes.get(p.nodeNr).
                    calculateWorldTransform().rotate(oldBodyDirection, bodyDirection);
        }
    }
    
}
