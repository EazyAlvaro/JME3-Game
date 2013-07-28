package PARlib;

import java.util.ArrayList;
import PARlib.Items.Item;

/**
 * This class is intended to handle all inventory management unique to the player
 * @author 
 */
public class PlayerCharacter  {
    private WorldObjectManager WOManager;
    private InventoryManager invManager;
    
    public PlayerCharacter(WorldObjectManager wom, InventoryManager inv) {
        this.WOManager = wom;
        this.invManager = inv;
    }
    
    public void addItem(Item item) {
        this.invManager.add(item);
    }
    
    public InventoryManager getInventoryManager() {
        return this.invManager;
    }
    
    public ArrayList getInventoryAsArrayList() {
        return invManager.getArrayList();
    }
    
}
