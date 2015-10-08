/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector3;

/**
 *
 * @author s138699
 */
public class Player {
    
    public enum PlayerParts {
        RIGHT_HAND_PART1(0),
        RIGHT_HAND_PART2(1),
        LEFT_HAND_PART1(2),
        LEFT_HAND_PART2(3),
        RIGHT_LEG_PART1(4),
        RIGHT_LEG_PART2(5),
        LEFT_LEG_PART1(6),
        LEFT_LEG_PART2(7),
        TORSO_PART1(8),
        TORSO_PART2(9),
        HEAD_PART1(10),
        HEAD_PART2(11);
        
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
        Node n;
        for(PlayerParts p : PlayerParts.values()) {
            n = model.nodes.get(p.nodeNr);
            if(p == PlayerParts.HEAD_PART1) 
                n.localTransform.rotate(up, 30);
            else
                n.globalTransform.rotate(oldBodyDirection, bodyDirection);
            n.calculateTransforms(false);
        }
    }

}
