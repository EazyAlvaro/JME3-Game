package PARlib;

import PARlib.Items.Item;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.Iterator;
//import java.util.ListIterator;

/**
 * @author temp
 */
public class WorldObjectManager {

    private ArrayList worldObjects;
    private Node rootNode;
    
    public WorldObjectManager(ArrayList wObjects, Node rootNode) {
        this.worldObjects = wObjects;
        this.rootNode = rootNode;
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
