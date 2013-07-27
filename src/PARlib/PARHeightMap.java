/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package PARlib;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import java.util.logging.Logger;
import java.util.logging.Level;



/**
 *
 * @author temp
 */
public class PARHeightMap extends AbstractHeightMap{
  
    private static final Logger logger = Logger.getLogger(PARHeightMap.class.getName());
    
    public PARHeightMap(float[][] sourceMap) {
	
	
	
    }
    
@Override
public boolean load() 
{ 
    return true;
}
    
}
