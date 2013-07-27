/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PARlib;

import com.jme3.math.Vector3f;
import com.jme3.terrain.heightmap.HeightMap;
import com.jme3.terrain.heightmap.HeightMapGrid;
//import com.jme3.terrain.heightmap.HillHeightMap;
import com.jme3.terrain.heightmap.MidpointDisplacementHeightMap;


/**
 *
 * @author temp
 */
public class CustomGrid implements HeightMapGrid  {
    
    private float sourceMap2[][] = {
        {150, 100, 50, 50, 10, 10, 6, 12},
        {25, 25, 25, 25, 25, 25, 25, 25},
        {5, 15, 25, 3, 5, 3, 15, 3},
        {50, 75, 17.5f, 50, 10, 12.5f, 17.5f, 50},
        {10, 10, 6, 10, 10, 10, 6, 25},
        {25, 25, 25, 25, 25, 25, 25, 25},
        {5, 3, 15, 3, 20, 3, 15, 75},
        {50, 12.5f, 17.5f, 25, 17.5f, 12.5f, 75, 125}
    };
    
    private float sourceMap3[][] = {
        {25, 20, 23, 24},
        {24, 18, 15, 23},
        {23, 16, 17, 22},
        {24, 23, 22, 21}
    };
    
    private HeightMap heightMap ;//new InterpolatedHeightMap
    private int size;
    
    public CustomGrid() {
        try {
            //this.heightMap = new HillHeightMap(513, 15 , 25.1f, 35.1f);
            this.heightMap = new InterpolatedHeightMap(32, sourceMap3);
            this.heightMap.load();
            
        } catch (Exception x) {
            System.out.println("Heightmap did not load properly, CATCH: " + x.getMessage());
            
        }
        
        
        this.size = heightMap.getSize();
        
    }
    
    
    
    @Override
    public HeightMap getHeightMapAt(Vector3f location) {
       
        //return heightMap;*/
        return this.heightMap;
    }
    
    public void setHeightMap(HeightMap input) {
        //this.heightMap = input;
    }
    
    @Override
   public void setSize(int size) {
        this.size = size - 1;
    }
    
}
