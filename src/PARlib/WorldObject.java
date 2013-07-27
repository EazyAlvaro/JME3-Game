package PARlib;

import com.jme3.scene.Spatial;


/**
 *
 * @author temp
 */
public class WorldObject {

    protected String ID;
    protected String Description;
    protected Spatial model;
    
    

    public void WorldObject(String desc, Spatial model) {
	this.Description = desc;
	this.model = model;
    }

    public Spatial getModel() {
	return this.model;
    }

    public String getID() {
	return this.ID;
    }
    
    public String getDescription() {
	return this.Description;
    }
}
