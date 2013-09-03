package lisong_mechlab.view.equipment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TreeModelEvent;

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.MessageXBar.Message;
import lisong_mechlab.model.MessageXBar.Reader;
import lisong_mechlab.model.item.AmmoWeapon;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.view.LoadoutFrame;

class EquippableItemsCathegory extends AbstractTreeCathegory implements Reader{

   private final List<Item> allItems;
   private final List<Item> equippableItems = new ArrayList<>();
   private Loadout          loadout         = null;

   public EquippableItemsCathegory(List<Item> anItemList, String aName, TreeCathegory aParent, EquipmentTreeModel aModel, MessageXBar anXBar){
      super(aName, aParent, aModel);
      HashSet<Item> h = new HashSet<>(anItemList); // Get rid of duplicates
      allItems = new ArrayList<>(h);
      determineEquippable();
      anXBar.attach(this);
   }

   @Override
   public int getChildCount(){
      return equippableItems.size();
   }

   @Override
   public int getIndex(Object aChild){
      return equippableItems.indexOf(aChild);
   }

   @Override
   public Object getChild(int aIndex){
      Item item = equippableItems.get(aIndex);
      if(loadout != null)
         return item.getName(loadout.getUpgrades());
      return item.getName();
   }

   private void determineEquippable(){
      equippableItems.clear();
      for(Item item : allItems){
         if( item instanceof Ammunition )
            continue;
         if( loadout == null || item.isEquippableOn(loadout) ){
            equippableItems.add(item);
            if( item instanceof AmmoWeapon )
               equippableItems.add(((AmmoWeapon)item).getAmmoType());
         }
      }
      HashSet<Item> h = new HashSet<>(equippableItems); // Get rid of duplicates
      equippableItems.clear();
      equippableItems.addAll(h);
      Collections.sort(equippableItems);
      getModel().notifyTreeChange(new TreeModelEvent(this, getPath()));
   }

   @Override
   public void internalFrameActivated(InternalFrameEvent aArg0){
      LoadoutFrame frame = (LoadoutFrame)aArg0.getInternalFrame();
      loadout = frame.getLoadout();
      determineEquippable();
   }

   @Override
   public void internalFrameClosed(InternalFrameEvent aArg0){
      loadout = null;
      determineEquippable();
   }

   @Override
   public void internalFrameDeiconified(InternalFrameEvent aArg0){
      LoadoutFrame frame = (LoadoutFrame)aArg0.getInternalFrame();
      loadout = frame.getLoadout();
      determineEquippable();
   }

   @Override
   public void internalFrameOpened(InternalFrameEvent aArg0){
      LoadoutFrame frame = (LoadoutFrame)aArg0.getInternalFrame();
      loadout = frame.getLoadout();
      determineEquippable();
   }

   @Override
   public void receive(Message aMsg){
      SwingUtilities.invokeLater(new Runnable(){
         @Override
         public void run(){
            determineEquippable();
         }
      });
   }
}
