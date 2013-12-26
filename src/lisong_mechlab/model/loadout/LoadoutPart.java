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
import lisong_mechlab.model.item.AmmoWeapon;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.EngineType;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.loadout.LoadoutPart.Message.Type;
import lisong_mechlab.util.ArrayUtils;
import lisong_mechlab.util.MessageXBar;

/**
 * This class represents a configured {@link InternalPart}.
 * <p>
 * TODO: Change structure for "Loadoutpart has an internal part" to "Loadout part is an internal part"
 * 
 * @author Emily Björk
 */
public class LoadoutPart implements MessageXBar.Reader{
   public class AddItemAction implements UndoAction{
      private final Item item;

      AddItemAction(Item anItem){
         item = anItem;
      }

      @Override
      public String describe(){
         return "Undo add " + item.getName(loadout.getUpgrades()) + " to " + internalPart.getType();
      }

      @Override
      public void undo(){
         removeItem(item, false);
      }

      @Override
      public boolean affects(Loadout aLoadout){
         return loadout == aLoadout;
      }
   }

   public class RemoveItemAction implements UndoAction{
      private final Item item;

      RemoveItemAction(Item anItem){
         item = anItem;
      }

      @Override
      public String describe(){
         return "Undo remove " + item.getName(loadout.getUpgrades()) + " from " + internalPart.getType();
      }

      @Override
      public void undo(){
         addItem(item, false);
      }

      @Override
      public boolean affects(Loadout aLoadout){
         return loadout == aLoadout;
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

   public final static double            ARMOR_PER_TON   = 32.0;
   public final static Internal          ENGINE_INTERNAL = new Internal("mdf_Engine", "mdf_EngineDesc", 3);

   private final transient UndoStack     undoStack;
   private final transient MessageXBar   xBar;
   private final transient Loadout       loadout;
   private final InternalPart            internalPart;
   private final List<Item>              items           = new ArrayList<Item>();
   private final Map<ArmorSide, Integer> armor           = new TreeMap<ArmorSide, Integer>();
   private int                           engineHeatsinks = 0;

   LoadoutPart(Loadout aLoadOut, InternalPart anInternalPart, MessageXBar aXBar, UndoStack anUndoStack){
      internalPart = anInternalPart;
      items.addAll(internalPart.getInternalItems());
      loadout = aLoadOut;
      undoStack = anUndoStack;
      xBar = aXBar;
      xBar.attach(this);

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

   public void addItem(String aString, boolean isUndoable){
      addItem(ItemDB.lookup(aString), isUndoable);
   }

   public void addItem(Item anItem, boolean isUndoable){
      if( !canAddItem(anItem) ){
         throw new IllegalArgumentException("Can't add " + anItem + "!");
      }
      if( anItem instanceof Engine ){
         Engine engine = (Engine)anItem;
         if( engine.getType() == EngineType.XL ){
            loadout.getPart(Part.LeftTorso).items.add(ENGINE_INTERNAL);
            loadout.getPart(Part.RightTorso).items.add(ENGINE_INTERNAL);
         }
      }
      items.add(anItem);
      xBar.post(new Message(this, Type.ItemAdded));
      if( isUndoable )
         undoStack.pushAction(new AddItemAction(anItem));
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
         // Case can only be put in side torsi
         if( anItem == ItemDB.lookup("C.A.S.E.") ){
            if( internalPart.getType() != Part.LeftTorso && internalPart.getType() != Part.RightTorso ){
               return false;
            }
         }
         return checkCommonRules(anItem);
      }
   }

   public void removeItem(Item anItem, boolean isUndoable){
      if( internalPart.getInternalItems().contains(anItem) || anItem instanceof Internal ){
         return; // Don't remove internals!
      }

      if( anItem instanceof Engine ){
         Engine engine = (Engine)anItem;
         if( !items.contains(engine) )
            return; // Don't remove anything we don't have (only dangerous if we accidentally remove LT/RT engine
                    // sides).

         if( engine.getType() == EngineType.XL ){
            loadout.getPart(Part.LeftTorso).items.remove(ENGINE_INTERNAL);
            loadout.getPart(Part.RightTorso).items.remove(ENGINE_INTERNAL);
         }

         int engineHsLeft = getNumEngineHeatsinks();
         while( engineHsLeft > 0 ){
            engineHsLeft--;
            if( loadout.getUpgrades().hasDoubleHeatSinks() )
               removeItem(ItemDB.DHS, isUndoable);
            else
               removeItem(ItemDB.SHS, isUndoable);
         }
      }
      if( items.remove(anItem) ){
         xBar.post(new Message(this, Type.ItemRemoved));
         if( isUndoable )
            undoStack.pushAction(new RemoveItemAction(anItem));
      }
   }

   public void removeAllItems(){
      if( getItems().isEmpty() )
         return;

      items.clear();
      items.addAll(internalPart.getInternalItems());
      xBar.post(new Message(this, Type.ItemRemoved));
   }

   /**
    * Sets the armor for a given side of the component. Throws if the operation fails.
    * 
    * @param anArmorSide
    *           The side to set the armor for.
    * @param anArmorAmount
    *           The amount to set the armor to.
    * @throws IllegalArgumentException
    *            Thrown if the component can't take any more armor or if the loadout doesn't have enough free tonnage to
    *            support the armor.
    */
   public void setArmor(ArmorSide anArmorSide, int anArmorAmount) throws IllegalArgumentException{
      if( anArmorAmount > getArmorMax(anArmorSide) ){
         throw new IllegalArgumentException("Exceeded max armor! Max allowed: " + getArmorMax(anArmorSide) + " Was: " + anArmorAmount);
      }
      int oldArmor = armor.get(anArmorSide);
      armor.put(anArmorSide, anArmorAmount);
      if( anArmorAmount >= oldArmor && loadout.getFreeMass() < 0 ){
         armor.put(anArmorSide, oldArmor);
         throw new IllegalArgumentException("Not enough tonnage to add more armor!");
      }
      xBar.post(new Message(this, Type.ArmorChanged));
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

   @Override
   public void receive(MessageXBar.Message aMsg){
      if( aMsg.isForMe(loadout) && aMsg instanceof Upgrades.Message ){
         Upgrades.Message msg = (Upgrades.Message)aMsg;

         if( msg.msg == Upgrades.Message.ChangeMsg.HEATSINKS ){
            if( loadout.getUpgrades().hasDoubleHeatSinks() )
               while( items.remove(ItemDB.SHS) ){/* No-Op */}
            else
               while( items.remove(ItemDB.DHS) ){/* No-Op */}
         }
         else if( msg.msg == Upgrades.Message.ChangeMsg.GUIDANCE ){
            boolean changed = false;

            for(AmmoWeapon weapon : ItemDB.lookup(AmmoWeapon.class)){
               Upgrades oldUpgrades = new Upgrades(null);
               oldUpgrades.setArtemis(!loadout.getUpgrades().hasArtemis());
               Ammunition oldAmmoType = weapon.getAmmoType(oldUpgrades);
               Ammunition newAmmoType = weapon.getAmmoType(loadout.getUpgrades());
               if( oldAmmoType == newAmmoType )
                  continue;

               while( items.remove(oldAmmoType) ){
                  items.add(newAmmoType);
                  changed = true;
               }
            }
            if( changed )
               xBar.post(new Message(this, Type.ItemsChanged));

            // loadout.getUpgrades().setArtemis(false);

         }

      }
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
