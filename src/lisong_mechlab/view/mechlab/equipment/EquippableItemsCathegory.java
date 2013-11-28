/*
 * @formatter:off
 * Li Song Mech Lab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Li Song
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */  
//@formatter:on
package lisong_mechlab.view.mechlab.equipment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TreeModelEvent;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.item.AmmoWeapon;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;
import lisong_mechlab.util.MessageXBar.Reader;
import lisong_mechlab.view.mechlab.LoadoutFrame;

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
      if( loadout != null )
         return item.getName(loadout.getUpgrades());
      return item.getName();
   }

   private void determineEquippable(){
      equippableItems.clear();
      for(Item item : allItems){
         if( item instanceof Ammunition ){
            continue;

         }
         else if( loadout == null || item.isEquippableOn(loadout) ){
            equippableItems.add(item);
            if( item instanceof AmmoWeapon ){
               if( loadout != null ){
                  if( loadout.getAllItems().contains(item) ){
                     equippableItems.add(((AmmoWeapon)item).getAmmoType(loadout.getUpgrades()));
                  }
               }
               else
                  equippableItems.add(((AmmoWeapon)item).getAmmoType(null));
            }
         }
      }
      HashSet<Item> h = new HashSet<>(equippableItems); // Get rid of duplicates
      equippableItems.clear();
      equippableItems.addAll(h);
      Collections.sort(equippableItems, new Comparator<Item>(){
         @Override
         public int compare(Item aO1, Item aO2){
            HardpointType h1 = (aO1 instanceof Ammunition) ? ((Ammunition)aO1).getWeaponHardpointType() : aO1.getHardpointType();
            HardpointType h2 = (aO2 instanceof Ammunition) ? ((Ammunition)aO2).getWeaponHardpointType() : aO2.getHardpointType();

            if( h1 == h2 ){
               return aO1.compareTo(aO2);
            }
            return h1.compareTo(h2);
         }
      });

      getModel().notifyTreeChange(new TreeModelEvent(this, getPath()));
   }

   private void focus(InternalFrameEvent anEvent){
      if( anEvent == null ){
         loadout = null;
         determineEquippable();
      }
      else{
         LoadoutFrame frame = (LoadoutFrame)anEvent.getInternalFrame();
         loadout = frame.getLoadout();
         determineEquippable();
      }
   }

   @Override
   public void internalFrameActivated(InternalFrameEvent aArg0){
      focus(aArg0);
   }

   @Override
   public void internalFrameDeactivated(InternalFrameEvent aE){
      focus(null);
   }

   @Override
   public void internalFrameDeiconified(InternalFrameEvent aArg0){
      focus(aArg0);
   }

   @Override
   public void internalFrameIconified(InternalFrameEvent aE){
      focus(null);
   }

   @Override
   public void receive(Message aMsg){
      // TODO: BE MORE SELECTIVE!
      SwingUtilities.invokeLater(new Runnable(){
         @Override
         public void run(){
            determineEquippable();
         }
      });
   }
}
