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
package lisong_mechlab.model.loadout.part;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.chassi.InternalPart;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.util.ArrayUtils;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * This class represents a configured {@link InternalPart}.
 * <p>
 * This class is immutable. The only way to alter it is by creating instances of the relevant {@link Operation}s and
 * adding them to an {@link OperationStack}.
 * 
 * @author Emily Björk
 */
public class LoadoutPart{
   public static class Message implements MessageXBar.Message{
      public enum Type{
         ItemAdded, ItemRemoved, ArmorChanged, ItemsChanged
      }

      public final LoadoutPart part;

      public final Type        type;

      public Message(LoadoutPart aPart, Type aType){
         part = aPart;
         type = aType;
      }

      @Override
      public boolean equals(Object obj){
         if( obj instanceof Message ){
            Message other = (Message)obj;
            return part == other.part && type == other.type;
         }
         return false;
      }

      @Override
      public boolean isForMe(Loadout aLoadout){
         return aLoadout.getPartLoadOuts().contains(part);
      }

      @Override
      public String toString(){
         return type.toString() + " for " + part.getInternalPart().getType().toString() + " of " + part.getLoadout();
      }
   }

   public final static Internal          ENGINE_INTERNAL = new Internal("mdf_Engine", "mdf_EngineDesc", 3, 15);

   private final transient Loadout       loadout;
   private final InternalPart            internalPart;
   private int                           engineHeatsinks = 0;

   private final Map<ArmorSide, Integer> armor           = new TreeMap<ArmorSide, Integer>();
   private final List<Item>              items           = new ArrayList<Item>();

   public LoadoutPart(Loadout aLoadout, InternalPart anInternalPart){
      internalPart = anInternalPart;
      items.addAll(internalPart.getInternalItems());
      loadout = aLoadout;
      if( internalPart.getType().isTwoSided() ){
         armor.put(ArmorSide.FRONT, 0);
         armor.put(ArmorSide.BACK, 0);
      }
      else{
         armor.put(ArmorSide.ONLY, 0);
      }
   }

   public boolean canEquip(Item anItem){
      if( !internalPart.isAllowed(anItem) )
         return false;
      
      if(anItem instanceof HeatSink && getNumEngineHeatsinks() < getNumEngineHeatsinksMax()){
         return true;
      }

      // Check enough free critical slots
      if( getNumCriticalSlotsFree() < anItem.getNumCriticalSlots(loadout.getUpgrades()) ){
         return false;
      }

      // Check enough free hard points
      if( anItem.getHardpointType() != HardpointType.NONE
          && getNumItemsOfHardpointType(anItem.getHardpointType()) >= getInternalPart().getNumHardpoints(anItem.getHardpointType()) ){
         return false; // Not enough hard points!
      }
      return true;
   }

   @Override
   public boolean equals(Object obj){
      if( this == obj )
         return true;
      if( !(obj instanceof LoadoutPart) )
         return false;
      LoadoutPart that = (LoadoutPart)obj;

      if( !internalPart.equals(that.internalPart) )
         return false;
      if( !ArrayUtils.equalsUnordered(items, that.items) )
         return false;
      if( !armor.equals(that.armor) )
         return false;
      if( engineHeatsinks != that.engineHeatsinks )
         return false;
      return true;
   }

   public int getArmor(ArmorSide anArmorSide){
      return armor.get(anArmorSide);
   }

   /**
    * Will return the number of armor points that can be set on the component. Taking both armor sides into account and
    * respecting the max armor limit. Does not take free tonnage into account.
    * 
    * @param anArmorSide
    *           The {@link ArmorSide} to get the max free armor for.
    * @return The number of armor points that can be maximally set (ignoring tonnage).
    */
   public int getArmorMax(ArmorSide anArmorSide){
      switch( anArmorSide ){
         case BACK:
            return internalPart.getArmorMax() - getArmor(ArmorSide.FRONT);
         case FRONT:
            return internalPart.getArmorMax() - getArmor(ArmorSide.BACK);
         default:
         case ONLY:
            return internalPart.getArmorMax();
      }
   }

   public int getArmorTotal(){
      int sum = 0;
      for(Integer i : armor.values()){
         sum += i;
      }
      return sum;
   }

   public InternalPart getInternalPart(){
      return internalPart;
   }

   public double getItemMass(){
      double ans = engineHeatsinks * 1.0;
      for(Item item : items){
         ans += item.getMass(loadout.getUpgrades());
      }
      return ans;
   }

   public List<Item> getItems(){
      return Collections.unmodifiableList(items);
   }

   public Loadout getLoadout(){
      return loadout;
   }

   public int getNumCriticalSlotsFree(){
      return internalPart.getNumCriticalslots() - getNumCriticalSlotsUsed();
   }

   public int getNumCriticalSlotsUsed(){
      int crits = 0;
      int engineHsLeft = getNumEngineHeatsinksMax();
      for(Item item : items){
         if( item instanceof HeatSink && engineHsLeft > 0 ){
            engineHsLeft--;
            continue;
         }
         crits += item.getNumCriticalSlots(loadout.getUpgrades());
      }
      return crits;
   }

   public int getNumEngineHeatsinks(){
      int ans = 0;
      for(Item i : items){
         if( i instanceof HeatSink )
            ans++;
      }
      return Math.min(ans, getNumEngineHeatsinksMax());
   }

   public int getNumEngineHeatsinksMax(){
      for(Item item : items){
         if( item instanceof Engine ){
            return ((Engine)item).getNumHeatsinkSlots();
         }
      }
      return 0;
   }

   public int getNumItemsOfHardpointType(HardpointType aHardpointType){
      int hardpoints = 0;
      for(Item it : items){
         if( it.getHardpointType() == aHardpointType ){
            hardpoints++;
         }
      }
      return hardpoints;
   }

   @Override
   public int hashCode(){
      final int prime = 31;
      int result = 1;
      result = prime * result + ((armor == null) ? 0 : armor.hashCode());
      result = prime * result + engineHeatsinks;
      result = prime * result + ((internalPart == null) ? 0 : internalPart.hashCode());
      result = prime * result + ((items == null) ? 0 : items.hashCode());
      return result;
   }

   void setArmor(ArmorSide anArmorSide, int anAmount){
      armor.put(anArmorSide, anAmount);
   }

   void addItem(Item anItem){
      items.add(anItem);
   }

   boolean removeItem(Item anItem){
      return items.remove(anItem);
   }
}
