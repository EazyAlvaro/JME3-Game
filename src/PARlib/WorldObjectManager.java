package PARlib;

import PARlib.Items.Item;
import PARlib.Items.ObjectHelper;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.Iterator;
//import java.util.ListIterator;

/**
 * @author Alvaro
 */
public class WorldObjectManager {

    private ArrayList worldObjects;
    private Node rootNode;
    private ObjectHelper objectHelper;
    
    public WorldObjectManager(ArrayList wObjects, Node rootNode) {
        this.worldObjects = wObjects;
        this.rootNode = rootNode;
    }

    public void setObjectHelper(ObjectHelper objHelp) {
        this.objectHelper = objHelp;
    }
    
    /**
     * Currently spawns a trio of teapots to test/demo object spawning
     */
    public void init() {
        Spatial tp1 = objectHelper.getTeapot("teapot 1", -10, 475, 0);
        Spatial tp2 = objectHelper.getTeapot("teapot 2", 11, 450, -10);
        Spatial tp3 = objectHelper.getTeapot("teapot 3", 5, 425, -15);
        
        addItem(tp1);
        addItem(tp2);
        addItem(tp3);
        
        rootNode.attachChild(tp1);
        rootNode.attachChild(tp2);
        rootNode.attachChild(tp3);
    }
    
    public void addItem(Object item) {
        worldObjects.add(item);
    }

    public void leftClickObject(String name) {
        Iterator<Item> itr = worldObjects.iterator();
        while (itr.hasNext()) {
            if (itr.next().getID().equals(name)) {
                //System.out.println(itr.next().getDescription());
                System.out.println("WORLD OBJECT (L): " + name);
            }
        }
    }
    
    public void rightClickObject(String name, PlayerCharacter pc) {
        Iterator<Item> itr = worldObjects.iterator();
        while (itr.hasNext()) { // go through *all* WorldObjects ...
            Item i = itr.next();
            if (i.getID().equals(name)) { //  ... and if we find a matching name
                // add item to player inventory
                pc.addItem(i); 
                // remove item from list of world items
                itr.remove(); 
                // remove item from the root node
                rootNode.detachChildNamed(name); 
                System.out.println("WORLD OBJECT (R): " + name);
                pc.getInventoryManager().logInventory();
            }
        }
    }   
}
