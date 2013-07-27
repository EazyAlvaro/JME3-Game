package PARlib;

import com.jme3.terrain.heightmap.AbstractHeightMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Random;
import com.jme3.math.FastMath;
import javax.management.JMException;

/**
 *
 * @author Alvaro
 */
public class InterpolatedHeightMap extends AbstractHeightMap  {

    private float[][] output = null;//float[] newArr = new float[size*size]
    private float[][] sourceMap = null;
    private float slopeMinDelta = 0.30f;
    private float slopeMaxDelta = 0.95f;
    private static final Logger logger = Logger.getLogger(AbstractHeightMap.class.getName());
    //private int size;
    private int mult;

    public InterpolatedHeightMap(int multipl, float sourceMap[][]) throws JMException  {
	this.sourceMap = sourceMap;
	this.size = (sourceMap.length * multipl)+1;
	this.mult = multipl;


	if (size < 0 || !FastMath.isPowerOfTwo(size - 1)) {
	    throw new JMException("The size is negative or not of the form 2^N +1 (a power of two plus one) "+ this.getSize() );
	}


	output = this.setArray(sourceMap);

       
       
	slopeRougher(1);
        //smoother(2);
        //output = this.roughTerrain(0.5f);
        //smoother();

    }
    
    public static float[][] getBigSourceMap() {
        float map[][] = {
            {125, 75, 50, 25, 10, 10, 6, 12},
            {65, 25, 25, 25, 25, 25, 25, 25},
            {25, 20, 25, 3, 5, 3, 15, 3},
            {35, 50, 25, 50, 10, 12.5f, 17.5f, 50},
            {10, 10, 25, 10, 10, 10, 6, 25},
            {25, 25, 25, 25, 25, 25, 25, 25},
            {5, 3, 15, 3, 20, 3, 15, 65},
            {50, 12.5f, 17.5f, 25, 17.5f, 12.5f, 75, 85}
         };
        return map;
    }
    
    public static float[][] getSmallSourceMap() {
       float map[][] = {
        {75, 20, 25, 24},
        {25, 18, 5, 23},
        {20, 6, 17, 22},
        {25, 25, 25, 50}
        }; 
       return map;
    }
    
    

    public void setSlopeMaxDelta(float max) {
	this.slopeMaxDelta = max;
    }

    public void setSlopeMinDelta(float min) {
        this.slopeMinDelta = min;
    }
    
    
    @Override
    public boolean load() {
	return true;
    }

    @Override
    public int getSize() {
	return this.size;
    }

    public float[][] getOutput() {
	return this.output;
    }

    private float[][] setArray(float sourceMap[][]) {
	float[][] newMap = new float[size][size];

	int sourceSize = sourceMap[0].length;
	int sv = 0;
	int sh = 0;
	int c = 0;   // counter 0 to 'mult'


	for (sv = 0; sv < sourceSize; sv++) {
	    for (sh = 0; sh < sourceSize; sh++) {

		for (int i = 0; i < mult; i++) {
		    for (int j = 0; j < mult; j++) {
			newMap[sv * mult + i][sh * mult + j] = sourceMap[sv][sh];
		    }
		}

	    }
	}
	return newMap;
    }

    private float[][] roughTerrain(float rough) {
	int sourceSize = sourceMap.length;
	float[][] newMap = output;
	int sv = 0;
	int sh = 0;
	float diff = 0;
	float bump = 0;
	Random generator = new Random();

	for (sv = 1; sv < sourceSize-1 ; sv++) {

	    for (sh = 1; sh < sourceSize-1 ; sh++) {


		for (int i = 1; i < mult; i++) {
		    for (int j = 1; j < mult; j++) {
			float[][] sm = this.sourceMap;
			diff = 0;
                        
                        
			// check grid for exactly the same numbers (for roads or platforms)
			//if (sm[sv][sh] != sm[sv][sh - 1] // left-from
			//	&& sm[sv][sh] != sm[sv][sh + 1] // right-from
			//	&& sm[sv][sh] != sm[sv - 1][sh] // below-from
			//	&& sm[sv][sh] != sm[sv + 1][sh] // above from
			//	) {
			    //diff = (generator.nextFloat()*rough)-(rough/2);
			//} else { // this is TEMPORARY
			    //diff = (generator.nextFloat()*rough)-(rough/2);
			//}

			if (generator.nextFloat() < 0.25f) {
			    bump = (rough);
			} else {
			    bump = 0;
			}

			diff = (generator.nextFloat() * (rough / 2) + bump) - (rough / 2);

                        
                        if (!checkSame(sm[sv][sh], sm[sv+1][sh], sm[sv-1][sh], sm[sv][sh - 1], sm[sv + 1][sh])) {
                            if (sv * mult + i > 0 && sh * mult + j > 0) {
                                newMap[sv * mult + i][sh * mult + j] += diff;
                                //System.out.println(newMap[sv * mult + i][sh * mult + j]);
                            }
                        }
		    }
		}

	    }
	}


	return newMap;
    }
    
    /**
     * Check if a tile/heightpoint has matching neighbors on the x/z axis of the same heightvalue
     * @param center
     * @param up
     * @param down
     * @param left
     * @param right
     * @return boolean whether or not the heightpoint has at least 1 matching neighbor
     */
    public boolean checkSame(float center, float up, float down, float left, float right) {
	
        try {
	if (center==up) return true; 
        } catch (NullPointerException x) {}
        
        try {
        if (center==down) return true; 
        } catch (NullPointerException x) {}
        
        try {
        if (center==left) return true;
        } catch (NullPointerException x) {}
        
        try {
        if (center==right) return true;
	} catch (NullPointerException x) {}
        
	return false;
    }
    
     /**
     * Single use override for smoother without parameters or return values
     * 
     * @return void
     */
    private void smoother() {
       this.output = smoother(this.output,1);
    }
    
     private void smoother(int i) {
       this.output = smoother(this.output,i);
    }
    
    
    /**
     * Single use override for smoother
     * @param map
     * @return 
     */
    private float[][] smoother(float[][] map) {
        
        return smoother(map,1);
    }
    
    /**
     * @fixme RtL smoothing is missing
     * @fixme BtT smoothing is missing 
     * @param map Does not use the private output value explicitly for possible later re-use
     * @param count
     * @return 
     */
    private float[][] smoother(float[][] map, int count) {
        map = output;
	for (int c = 0; c < count; c++) { // count itertion
            
            // Horizontal works, don't fuck with it
            
	    /* Horizontal Left-to-Right (LTR) smoothing
	    for (int i = 1; i < map[i].length - 1; i++) {
		for (int j = 1; j < map[i].length - 1; j++) {

		    map[i][j] = (map[i][j - 1] + map[i][j + 1]) / 2;
		}

	    }

            // horizontal right to left
            for (int i = output[0].length-2; i >  2; i--) {
                for (int j = output[i].length-2; j >  2; j--) {
                   map[i][j] = (map[i][j + 1] + map[i][j - 1]) / 2; 
                }
            }*/
            
            
	    // Vertical Top-to-Bottom smoothing
            //int i = 1; i < map[i].length - 2; i++
	    for (int i = output[0].length-2; i <  1; i--) {
		for (int j = 1; j < map[j].length - 1; j--) {
		    //original
		    map[i][j] = (map[j - 1][i] + map[j + 1][i]) / 2;
                    
                   // map[i][j] = (map[i - 1][j] + map[i][j]) / 2;
		    //System.out.print("\nsmoothing "+i+","+j);
		}

	    }
            
            /*THE GOGGLES THEY DO NOTHING
            //Vertical bottom-to-top
            for (int i = 1; i < output[i].length - 1; i++) 
            {
                for (int j = output[0].length - 2; j >  1; j--) 
                { 
                    map[j][i] = (map[j][i - 1] + map[j][i + 1]) / 2;
                }
                
            }*/
            
          
            
            
	} // end-of count iteration
	return map;
    }

    //TODO copy this to a univeral system for my terrain alterations ... maybe
    
    private void slopeRougher(int loop) {
	//float slope = 0;
	float hi = 0;
	float lo = 0;
	//boolean flip = false;
	
        /// <editor-fold defaultstate="collapsed" desc="Rougher Loops">
	
        
	for ( int q = 0; q<loop; q++) {
        
            /** Horizontal Left to Right slope roughin **/
            for (int i = 1; i < output[i].length - 1; i++) 
            {
                for (int j = 1; j < output[i].length - 1; j++) 
                {
                    //flip = false;
                    if (output[i][j - 1] > output[i][j + 1]) {

                        hi = output[i][j - 1];
                        lo = output[i][j + 1];
                        output[i][j] = getSlope(hi,lo,output[i][j]);

                    } else if (output[i][j - 1] < output[i][j + 1]) {   
                        hi = output[i][j + 1];
                        lo = output[i][j - 1];
                        //flip = true;
                        output[i][j] = getSlope(hi,lo,output[i][j]);
                    }
                }
            } 

            /** horizontal Right To Left slope rouging   ***/

            for (int i = output[0].length-2; i >  2; i--) {
                for (int j = output[i].length-2; j >  2; j--) {

                    //System.out.println("foo!\n");
                    if (output[i][j - 1] > output[i][j + 1]) {

                        hi = output[i][j - 1];
                        lo = output[i][j + 1];
                        output[i][j] = getSlope(hi,lo,output[i][j]);

                    } else if (output[i][j - 1] < output[i][j + 1]) {   
                        hi = output[i][j + 1];
                        lo = output[i][j - 1];

                        output[i][j] = getSlope(hi,lo,output[i][j]);
                    }
                }
            } 

            //Vertical bottom to top 
            for (int i = 1; i < output[i].length - 1; i++) 
            {
                for (int j = output[i].length - 2; j >  1; j--) 
                {
                    //flip = false;
                    if (output[i-1][j ] > output[i+1][j]) {

                        hi = output[i-1][j];
                        lo = output[i+1][j];
                        output[i][j] = getSlope(hi,lo,output[i][j]);


                    } else if (output[i-1][j] < output[i+1][j]) {   
                        hi = output[i+1][j];
                        lo = output[i-1][j];
                        output[i][j] = getSlope(hi,lo,output[i][j]);
                    }
                }
            } 

          /*** Vertical top to bottom ***/
           for (int i = output[0].length-2; i >  2; i--) {

                for (int j = output[0].length - 2; j >  1; j--) 
                {               
                    if (output[i-1][j ] > output[i+1][j]) {
                        hi = output[i-1][j];
                        lo = output[i+1][j];
                        output[i][j] = getSlope(hi,lo,output[i][j]);

                    } else if (output[i-1][j] < output[i+1][j]) {  
                        hi = output[i+1][j];
                        lo = output[i-1][j];
                        output[i][j] = getSlope(hi,lo,output[i][j]);
                    }
                }
            }
            // </editor-fold>        
        }	
    }

    /**
     * Returns either the original height value or an altered one, depending on minimum and maximum slope
     * @param hi highest neighboring value used to determine slope ratio
     * @param lo lowest neighboring value used to determine slope ratio
     * @param current height value
     * @return new value for 'current'
     */
    private float getSlope(float hi, float lo, float current) {
	float slope = (hi - lo) / hi;
	Random generator = new Random();

        
	if ( ((hi - lo) > 0) && (slope > slopeMinDelta) && (slope < slopeMaxDelta)) {
	    return (hi - ((hi * slopeMinDelta))) + ((lo * slopeMinDelta) * generator.nextFloat());
	}


	return current; // nothing changes
    }

    /**
     * @return reduces the 2d array back to a 1d array 
     */
    public float[] toOneArray() {
	float[] newArr = new float[size * size];
	int c = 0;
	for (int i = 0; i < size; i++) {
	    for (int j = 0; j < size; j++) {
		newArr[c] = output[i][j];
		c++;
	    }
	}
	return newArr;
    }

    @Override
    public float[] getHeightMap() {
	return toOneArray();
    }

    private void log(String warn, String err) {
	logger.log(Level.WARNING, warn, err);
    }

    private void log(String logtxt) {
	log(logtxt, logtxt);
    }
}
