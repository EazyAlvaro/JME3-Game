/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PARlib;

import java.util.ArrayList;
import PARlib.Items.Item;

/**
 *
 * @author temp
 */
public class PlayerCharacter  {
    private WorldObjectManager WOManager;// = new ArrayList();
    private InventoryManager invManager;// = new InventoryManager();
    
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
