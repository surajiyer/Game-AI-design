/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

/**
 *
 * @author S.S.Iyer
 */
public class InfiniteGrid extends Terrain {

    final float cellSize;
    
    public InfiniteGrid(int xSize, int ySize, float cellSize) {
        ModelBuilder modelBuilder = new ModelBuilder();
        instance = new ModelInstance(
                modelBuilder.createLineGrid(xSize, ySize, cellSize, cellSize, 
                        new Material(ColorAttribute.createDiffuse(Color.GREEN)), 
                        VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal));
        this.cellSize = cellSize;
    }
    
    @Override
    public void updatePos(Vector3 pos) {
        Vector3 gridTranslation = instance.transform.getTranslation(Vector3.Zero);
        Vector3 diff = (new Vector3(pos).sub(gridTranslation));
        boolean moveGrid = false;

        if (diff.x > cellSize / 2f) {
            gridTranslation.x += cellSize;
            moveGrid = true;
        } else if (diff.x < -cellSize / 2f) {
            gridTranslation.x -= cellSize;
            moveGrid = true;
        }
         if (diff.z > cellSize / 2f) {
            gridTranslation.z += cellSize;
            moveGrid = true;
        } else if (diff.z < -cellSize / 2f) {
            gridTranslation.z -= cellSize;
            moveGrid = true;
        }

        if (moveGrid) {
            gridTranslation.y = 0;
            instance.transform.setToTranslation(gridTranslation);
        }
    }
}
