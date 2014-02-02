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
package lisong_mechlab.model.loadout;

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
import lisong_mechlab.model.loadout.LoadoutPart.Message.Type;
import lisong_mechlab.model.loadout.OperationStack.CompositeOperation;
import lisong_mechlab.model.loadout.OperationStack.Operation;
import lisong_mechlab.util.ArrayUtils;
import lisong_mechlab.util.MessageXBar;

/**
 * This class represents a configured {@link InternalPart}.
 * <p>
 * This class is immutable. The only way to alter it is by creating instances of the nested {@link Operation}s and
 * adding them to an {@link OperationStack}.
 * 
 * @author Li Song
 */
public class LoadoutPart{
   /**
    * This {@link Operation} will remove all items and armor of this component.
    * 
    * @author Li Song
    */
   public class StripPartOperation extends CompositeOperation{
      public StripPartOperation(){
         super("strip part");
         int hsSkipp = getNumEngineHeatsinks();
         for(Item item : getItems()){
            if( !(item instanceof Internal) ){
               if(item instanceof HeatSink){
                  if(hsSkipp > 0){
                     hsSkipp--;
                     continue;
                  }                     
               }
               addOp(new RemoveItemOperation(item));
            }
         }
         if( internalPart.getType().isTwoSided() ){
            addOp(new SetArmorOperation(ArmorSide.FRONT, 0));
            addOp(new SetArmorOperation(ArmorSide.BACK, 0));
         }
         else{
            addOp(new SetArmorOperation(ArmorSide.ONLY, 0));
         }
      }
   }

   /**
    * This {@link Operation} will change the armor of a {@link LoadoutPart}.
    * 
    * @author Li Song
    */
   public class SetArmorOperation extends Operation{
      private final ArmorSide side;
      private final int       amount;
      private final int       oldAmount;

      /**
       * Sets the armor for a given side of the component. Throws if the operation will fail.
       * 
       * @param anArmorSide
       *           The side to set the armor for.
       * @param anArmorAmount
       *           The amount to set the armor to.
       * @throws IllegalArgumentException
       *            Thrown if the component can't take any more armor or if the loadout doesn't have enough free tonnage
       *            to support the armor.
       */
      public SetArmorOperation(ArmorSide anArmorSide, int anArmorAmount){
         side = anArmorSide;
         amount = anArmorAmount;
         oldAmount = armor.get(side);

         if( amount > getArmorMax(side) ){
            throw new IllegalArgumentException("Exceeded max armor! Max allowed: " + getArmorMax(side) + " Was: " + amount);
         }

         // TODO: This is an ugly way to check if the loadout can hold that amount of armor
         armor.put(side, amount);
         if( amount >= oldAmount && loadout.getFreeMass() < 0 ){
            armor.put(side, oldAmount);
            throw new IllegalArgumentException("Not enough tonnage to add more armor!");
         }
         armor.put(side, oldAmount);
      }

      @Override
      public String describe(){
         return "change armor";
      }

      @Override
      protected void apply(){
         if( amount != oldAmount ){
            armor.put(side, amount);
            xBar.post(new Message(LoadoutPart.this, Type.ArmorChanged));
         }
      }

      @Override
      protected void undo(){
         if( amount != oldAmount ){
            armor.put(side, oldAmount);
            xBar.post(new Message(LoadoutPart.this, Type.ArmorChanged));
         }
      }
   }

   /**
    * A helper class for implementing {@link Operation}s that affect items on a {@link LoadoutPart}.
    * 
    * @author Li Song
    */
   public abstract class ItemOperation extends Operation{
      protected final Item item;
      private int          numEngineHS = 0;

      /**
       * Creates a new {@link ItemOperation}. The deriving classes shall throw if the the operation with the given item
       * would violate the {@link Loadout} or {@link LoadoutPart} invariant.
       * 
       * @param anItem
       *           The item that shall be affected.
       */
      public ItemOperation(Item anItem){
         item = anItem;
      }

      /**
       * Removes an item without checks. Will count up the numEngineHS variable to the number of heat sinks removed.
       */
      protected void removeItem(){
         if( item instanceof Engine ){
            Engine engine = (Engine)item;
            if( engine.getType() == EngineType.XL ){
               loadout.getPart(Part.LeftTorso).items.remove(ENGINE_INTERNAL);
               loadout.getPart(Part.RightTorso).items.remove(ENGINE_INTERNAL);
            }

            int engineHsLeft = getNumEngineHeatsinks();
            while( engineHsLeft > 0 ){
               engineHsLeft--;
               numEngineHS++;
               if( loadout.getUpgrades().hasDoubleHeatSinks() )
                  items.remove(ItemDB.DHS);
               else
                  items.remove(ItemDB.SHS);
            }
         }
         items.remove(item);
         xBar.post(new Message(LoadoutPart.this, Type.ItemRemoved));
      }

      /**
       * Adds an item without checks. Will add numEngineHS heat sinks if the item is an engine.
       */
      protected void addItem(){
         if( item instanceof Engine ){
            Engine engine = (Engine)item;
            if( engine.getType() == EngineType.XL ){
               loadout.getPart(Part.LeftTorso).items.add(ENGINE_INTERNAL);
               loadout.getPart(Part.RightTorso).items.add(ENGINE_INTERNAL);
            }
            while( numEngineHS > 0 ){
               numEngineHS--;
               if( loadout.getUpgrades().hasDoubleHeatSinks() )
                  items.add(ItemDB.DHS);
               else
                  items.add(ItemDB.SHS);
            }
         }
         items.add(item);
         xBar.post(new Message(LoadoutPart.this, Type.ItemAdded));
      }
   }

   /**
    * This {@link Operation} adds an {@link Item} to a {@link LoadoutPart}.
    * 
    * @author Li Song
    */
   public class AddItemOperation extends ItemOperation{

      /**
       * Creates a new operation.
       * 
       * @param anItem
       *           The {@link Item} to add.
       */
      public AddItemOperation(Item anItem){
         super(anItem);
         if( internalPart.getInternalItems().contains(item) || item instanceof Internal )
            throw new IllegalArgumentException("Can't add internals to a loadout!");
      }

      @Override
      public String describe(){
         return "add " + item.getName(loadout.getUpgrades()) + " to " + internalPart.getType();
      }

      @Override
      public void undo(){
         removeItem();
      }

      @Override
      public void apply(){
         if( !canAddItem(item) )
            throw new IllegalArgumentException("Can't add " + item + "!");
         addItem();
      }
   }

   /**
    * This {@link Operation} removes an {@link Item} from a {@link LoadoutPart}.
    * 
    * @author Li Song
    */
   public class RemoveItemOperation extends ItemOperation{
      /**
       * Creates a new operation.
       * 
       * @param anItem
       *           The {@link Item} to remove.
       */
      public RemoveItemOperation(Item anItem){
         super(anItem);
      }

      @Override
      public String describe(){
         return "remove " + item.getName(loadout.getUpgrades()) + " from " + internalPart.getType();
      }

      @Override
      public void undo(){
         addItem();
      }

      @Override
      public void apply(){
         if( !items.contains(item) )
            throw new IllegalArgumentException("Can't remove " + item + "!");
         removeItem();
      }
   }

   public static class Message implements MessageXBar.Message{
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
      public String toString(){
         return type.toString() + " for " + part.getInternalPart().getType().toString() + " of " + part.getLoadout();
      }

      public enum Type{
         ItemAdded, ItemRemoved, ArmorChanged, ItemsChanged
      }

      public final LoadoutPart part;
      public final Type        type;

      @Override
      public boolean isForMe(Loadout aLoadout){
         return aLoadout.getPartLoadOuts().contains(part);
      }
   }

   public final static double            ARMOR_PER_TON   = 32.0;                                           // TODO:
                                                                                                            // Should
                                                                                                            // be
                                                                                                            // replaced
                                                                                                            // with
                                                                                                            // upgrade
                                                                                                            // handlers
   public final static Internal          ENGINE_INTERNAL = new Internal("mdf_Engine", "mdf_EngineDesc", 3);

   private final transient MessageXBar   xBar;
   private final transient Loadout       loadout;
   private final InternalPart            internalPart;
   private final List<Item>              items           = new ArrayList<Item>();
   private final Map<ArmorSide, Integer> armor           = new TreeMap<ArmorSide, Integer>();
   private int                           engineHeatsinks = 0;

   LoadoutPart(Loadout aLoadOut, InternalPart anInternalPart, MessageXBar aXBar){
      internalPart = anInternalPart;
      items.addAll(internalPart.getInternalItems());
      loadout = aLoadOut;
      xBar = aXBar;

      if( internalPart.getType().isTwoSided() ){
         armor.put(ArmorSide.FRONT, 0);
         armor.put(ArmorSide.BACK, 0);
      }
      else{
         armor.put(ArmorSide.ONLY, 0);
      }
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

   public InternalPart getInternalPart(){
      return internalPart;
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

   public int getNumItemsOfHardpointType(HardpointType aHardpointType){
      int hardpoints = 0;
      for(Item it : items){
         if( it.getHardpointType() == aHardpointType ){
            hardpoints++;
         }
      }
      return hardpoints;
   }

   public List<Item> getItems(){
      return Collections.unmodifiableList(items);
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

   public int getArmorTotal(){
      int sum = 0;
      for(Integer i : armor.values()){
         sum += i;
      }
      return sum;
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
      if( anArmorSide == ArmorSide.ONLY ){
         return internalPart.getArmorMax();
      }
      else if( anArmorSide == ArmorSide.FRONT ){
         return internalPart.getArmorMax() - getArmor(ArmorSide.BACK);
      }
      else if( anArmorSide == ArmorSide.BACK ){
         return internalPart.getArmorMax() - getArmor(ArmorSide.FRONT);
      }
      throw new UnsupportedOperationException("Unknown side!");
   }

   public double getItemMass(){
      double ans = engineHeatsinks * 1.0;
      for(Item item : items){
         ans += item.getMass(loadout.getUpgrades());
      }
      return ans;
   }

   public Loadout getLoadout(){
      return loadout;
   }

   public int getNumEngineHeatsinksMax(){
      for(Item item : items){
         if( item instanceof Engine ){
            return ((Engine)item).getNumHeatsinkSlots();
         }
      }
      return 0;
   }

   public int getNumEngineHeatsinks(){
      int ans = 0;
      for(Item i : items){
         if( i instanceof HeatSink )
            ans++;
      }
      return Math.min(ans, getNumEngineHeatsinksMax());
   }

   public String getItemDisplayName(Item anItem){
      return anItem.getName(loadout.getUpgrades());
   }

   public String getItemDisplayName(int index){
      return getItemDisplayName(items.get(index));
   }

   public int getItemCriticalSlots(Item anItem){
      return anItem.getNumCriticalSlots(loadout.getUpgrades());
   }

   public int getItemCriticalSlots(int index){
      return getItemCriticalSlots(items.get(index));
   }

   private boolean checkCommonRules(Item anItem){
      // Check enough free mass
      if( loadout.getMass() + anItem.getMass(loadout.getUpgrades()) > loadout.getChassi().getMassMax() ){
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
      if( getNumEngineHeatsinks() < getNumEngineHeatsinksMax()
          && loadout.getMass() + anItem.getMass(loadout.getUpgrades()) <= loadout.getChassi().getMassMax() ){
         return true;
      }
      return checkCommonRules(anItem);
   }
}
