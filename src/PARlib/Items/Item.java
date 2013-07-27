/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package PARlib.Items;

import PARlib.WorldObject;
import com.jme3.scene.Spatial;

/**
 *
 * @author temp
 */
public class Item extends WorldObject {

    protected int weight;
    protected Resource resourceType;
    protected int reduction;
    protected int stack;
    protected int stack_current;
    protected boolean canActivate;

    public void Item(String desc) {
	//a`zza`z`this.Description = desc;
	this.ID = desc;
	this.Description = desc;
	  
    }
    

    public int getWeight() {
	return this.weight;
    }

    public Resource getResourceType() {
	return this.resourceType;
    }

    public void setResourceType(Resource type) {
	this.resourceType = type;
    }
    
    public int getReduction() {
	return this.reduction;
    }

    public int getStack() {
	return this.stack;
    }

    public int getStackCurrent() {
	return this.stack_current;
    }

    public boolean canActivate() {
	return this.canActivate;
    }

    public void Activate() {
    }

    public boolean add(int amnt) {
	if (stack_current + amnt <= stack) {
	    stack_current += amnt;
	    return true;
	} else {
	    return false;
	}
    }
}
