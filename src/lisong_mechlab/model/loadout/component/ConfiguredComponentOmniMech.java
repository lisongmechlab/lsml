/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Emily Björk
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
package lisong_mechlab.model.loadout.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lisong_mechlab.model.chassi.ComponentOmniMech;
import lisong_mechlab.model.chassi.HardPoint;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.chassi.OmniPod;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.LoadoutOmniMech;

/**
 * This class models a configured {@link OmniPod} on an {@link LoadoutOmniMech}.
 * 
 * @author Emily Björk
 */
public class ConfiguredComponentOmniMech extends ConfiguredComponentBase{
   private OmniPod omniPod;

   public ConfiguredComponentOmniMech(ComponentOmniMech aComponentOmniMech, boolean aAutoArmor, OmniPod aOmniPod){
      super(aComponentOmniMech, aAutoArmor);
      if( null == aOmniPod ){
         throw new NullPointerException("aOmniPod must not be null!");
      }
      omniPod = aOmniPod;
   }

   public ConfiguredComponentOmniMech(ConfiguredComponentOmniMech aConfiguredOmnipod){
      super(aConfiguredOmnipod);
      omniPod = aConfiguredOmnipod.omniPod;
   }

   @Override
   public ComponentOmniMech getInternalComponent(){
      return (ComponentOmniMech)super.getInternalComponent();
   }

   @Override
   public int getHardPointCount(HardPointType aHardpointType){
      return omniPod.getHardPointCount(aHardpointType);
   }

   @Override
   public Collection<HardPoint> getHardPoints(){
      return omniPod.getHardPoints();
   }

   @Override
   public List<Item> getItemsFixed(){
      for(Item item : getInternalComponent().getFixedItems()){
         if(getInternalComponent().shouldRemoveArmActuators(item)){
            return stripHALAA();
         }
      }
      
      for(Item item : getItemsEquipped()){
         if(getInternalComponent().shouldRemoveArmActuators(item)){
            return stripHALAA();
         }
      }
      return getInternalComponent().getFixedItems();
   }

   /**
    * @return The currently mounted {@link OmniPod}.
    */
   public OmniPod getOmniPod(){
      return omniPod;
   }
   
   @Override
   public int getSlotsUsed(){
      return super.getSlotsUsed() + getInternalComponent().getDynamicArmorSlots() + getInternalComponent().getDynamicStructureSlots();
   }

   /**
    * @param aOmniPod
    *           The {@link OmniPod} to set for this component.
    */
   public void setOmniPod(OmniPod aOmniPod){
      if( null == aOmniPod )
         throw new NullPointerException("aOmniPod must not be null.");
      omniPod = aOmniPod;
   }

   @Override
   public boolean hasMissileBayDoors(){
      return getOmniPod().hasMissileBayDoors();
   }
   
   private List<Item> stripHALAA(){
      List<Item> ans = new ArrayList<>(getInternalComponent().getFixedItems());
      ans.remove(ItemDB.lookup("@mdf_LAA"));
      ans.remove(ItemDB.lookup("@mdf_HA"));
      return ans;
   }
}
