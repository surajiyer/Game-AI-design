/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

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
    public final Vector3 position, oldPosition = new Vector3();
    public final Vector3 headDirection, oldHeadDirection = new Vector3();
    public final Vector3 bodyDirection, oldBodyDirection = new Vector3();
    public final Vector3 up;
    public int flags;

    public Player(GameObject model, Camera camera, Vector3 initPosition) {
        this.model = model;
        position = new Vector3(initPosition);
        this.model.transform.trn(position);
        bodyDirection = new Vector3(camera.direction);
        headDirection = new Vector3(camera.direction);
        up = new Vector3(Vector3.Y);
    }
    
    private final Vector3 tmp = new Vector3();
    
    public void setDirection(Vector3 tmp) {
        oldHeadDirection.set(headDirection);
        oldBodyDirection.set(bodyDirection);
        headDirection.set(tmp.nor());
        bodyDirection.set(tmp.set(headDirection).sub(up));
    }
    
    public void setBodyDirection(Vector3 tmp) {
        oldBodyDirection.set(bodyDirection);
        bodyDirection.set(tmp.nor());
    }
    
    public void update() {
        // Position
        model.transform.trn(tmp.set(position).sub(oldPosition));
        oldPosition.set(position);
        
        // Body rotation
        for(PlayerParts p : PlayerParts.values()) {
            if(p == PlayerParts.HEAD_PART) 
                model.nodes.get(PlayerParts.HEAD_PART.nodeNr).
                        calculateWorldTransform().rotate(oldHeadDirection, headDirection);
            else
                model.nodes.get(p.nodeNr).
                        calculateWorldTransform().rotate(oldBodyDirection, bodyDirection);
        }
    }

}
