/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
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
package lisong_mechlab.model.loadout.part;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.chassi.InternalPart;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.EngineType;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.OperationStack;
import lisong_mechlab.model.loadout.OperationStack.Operation;
import lisong_mechlab.util.ArrayUtils;
import lisong_mechlab.util.MessageXBar;

/**
 * This class represents a configured {@link InternalPart}.
 * <p>
 * This class is immutable. The only way to alter it is by creating instances of the relevant {@link Operation}s and
 * adding them to an {@link OperationStack}.
 * 
 * @author Li Song
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

   // TODO: Should be replaced with upgrade handlers
   public final static double            ARMOR_PER_TON   = 32.0;
   public final static Internal          ENGINE_INTERNAL = new Internal("mdf_Engine", "mdf_EngineDesc", 3);

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

   public boolean canAddItem(Item anItem){
      if( anItem instanceof Internal ){
         return false; // Can't add internals!
      }
      else if( anItem instanceof HeatSink ){
         return checkHeatsinkRules((HeatSink)anItem);
      }
      else if( anItem instanceof Engine ){
         return checkEngineRules((Engine)anItem);
      }
      else if( anItem instanceof JumpJet ){
         return checkJumpJetRules((JumpJet)anItem);
      }
      else{
         // Case can only be put in side torsii
         if( anItem == ItemDB.lookup("C.A.S.E.") ){
            if( internalPart.getType() != Part.LeftTorso && internalPart.getType() != Part.RightTorso ){
               return false;
            }
         }
         return checkCommonRules(anItem);
      }
   }

   private boolean checkCommonRules(Item anItem){
      // Check enough free mass
      if( anItem.getMass(loadout.getUpgrades()) > loadout.getFreeMass() ){
         return false;
      }

      // Check enough free critical slots
      if( getNumCriticalSlotsFree() < anItem.getNumCriticalSlots(loadout.getUpgrades()) ){
         return false;
      }

      if( loadout.getNumCriticalSlotsFree() < anItem.getNumCriticalSlots(loadout.getUpgrades()) ){
         return false;
      }

      // Check enough free hard points
      if( anItem.getHardpointType() != HardpointType.NONE
          && getNumItemsOfHardpointType(anItem.getHardpointType()) >= getInternalPart().getNumHardpoints(anItem.getHardpointType()) ){
         return false; // Not enough hard points!
      }
      return true;
   }

   private boolean checkEngineRules(Engine engine){
      if( getInternalPart().getType() != Part.CenterTorso ){
         return false; // Engines only in CT!
      }

      // XL engines need 3 additional slots in RT/LT
      if( engine.getType() == EngineType.XL ){
         if( loadout.getPart(Part.LeftTorso).getNumCriticalSlotsFree() < 3 ){
            return false;
         }
         if( loadout.getPart(Part.RightTorso).getNumCriticalSlotsFree() < 3 ){
            return false;
         }
         if( loadout.getNumCriticalSlotsFree() < 3 * 2 + engine.getNumCriticalSlots(loadout.getUpgrades()) ){
            // XL engines return same number of slots as standard engine, check enough slots to cover the
            // side torsi.
            return false;
         }
      }

      if( engine.getRating() > loadout.getChassi().getEngineMax() || engine.getRating() < loadout.getChassi().getEngineMin() ){
         return false; // Too low/high engine rating!
      }
      return checkCommonRules(engine);
   }

   private boolean checkHeatsinkRules(HeatSink anItem){
      // Don't allow standard heat sinks when double heat sinks are upgraded etc.
      if( loadout.getUpgrades().hasDoubleHeatSinks() && anItem != ItemDB.lookup(3001) ){
         return false;
      }
      if( !loadout.getUpgrades().hasDoubleHeatSinks() && anItem != ItemDB.lookup(3000) ){
         return false;
      }

      // Allow engine slot heat sinks even if there are no critical slots
      if( getNumEngineHeatsinks() < getNumEngineHeatsinksMax() && anItem.getMass(loadout.getUpgrades()) <= loadout.getFreeMass() ){
         return true;
      }
      return checkCommonRules(anItem);
   }

   private boolean checkJumpJetRules(JumpJet aItem){
      Part type = getInternalPart().getType();
      switch( type ){
         case RightTorso:
         case CenterTorso:
         case LeftTorso:
         case RightLeg:
         case LeftLeg:
            return loadout.getJumpJetCount() + 1 <= loadout.getChassi().getMaxJumpJets() && checkCommonRules(aItem);
         default:
            return false;
      }
   }

   @Override
   public boolean equals(Object obj){
      if( this == obj )
         return true;
      if( !(obj instanceof LoadoutPart) )
         return false;
      LoadoutPart that = (LoadoutPart)obj;

      // @formatter:off
      return // loadout.equals(that.loadout) && // Two LoadoutParts can be equal without having equal Loadouts.
             internalPart.equals(that.internalPart) &&
             ArrayUtils.equalsUnordered(items, that.items) &&
             armor.equals(that.armor) &&
             engineHeatsinks == that.engineHeatsinks;
      // @formatter:on;
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

   @Deprecated
   public int getItemCriticalSlots(int index){
      return getItemCriticalSlots(items.get(index));
   }

   @Deprecated
   public int getItemCriticalSlots(Item anItem){
      return anItem.getNumCriticalSlots(loadout.getUpgrades());
   }

   @Deprecated
   public String getItemDisplayName(int index){
      return getItemDisplayName(items.get(index));
   }

   @Deprecated
   public String getItemDisplayName(Item anItem){
      return anItem.getName(loadout.getUpgrades());
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
