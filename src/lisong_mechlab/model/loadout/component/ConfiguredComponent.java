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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.chassi.InternalComponent;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.util.ArrayUtils;
import lisong_mechlab.util.MessageXBar;

/**
 * This class represents a configured {@link InternalComponent}.
 * <p>
 * This class is immutable. The only way to alter it is by creating instances of the relevant {@link Operation}s and
 * adding them to an {@link OperationStack}.
 * 
 * @author Emily Björk
 */
/**
 * @author Emily
 */
public class ConfiguredComponent{
   public static class Message implements MessageXBar.Message{
      public enum Type{
         ArmorChanged, ArmorDistributionUpdateRequest, ItemAdded, ItemRemoved, ItemsChanged
      }

      /**
       * True if this message was automatically in response to a change.
       */
      public final boolean             automatic;

      public final ConfiguredComponent component;

      public final Type                type;

      public Message(ConfiguredComponent aComponent, Type aType){
         this(aComponent, aType, false);
      }

      public Message(ConfiguredComponent aComponent, Type aType, boolean aAutomatic){
         component = aComponent;
         type = aType;
         automatic = aAutomatic;
      }

      @Override
      public boolean affectsHeatOrDamage(){
         return type != Type.ArmorChanged;
      }

      @Override
      public boolean equals(Object obj){
         if( obj instanceof Message ){
            Message other = (Message)obj;
            return component == other.component && type == other.type && automatic == other.automatic;
         }
         return false;
      }

      @Override
      public boolean isForMe(LoadoutBase aLoadout){
         return aLoadout.getComponents().contains(component);
      }

      @Override
      public String toString(){
         return type.toString() + " for " + component.getInternalComponent().getLocation().toString();
      }
   }

   public final static Internal              ENGINE_INTERNAL      = (Internal)ItemDB.lookup("mdf_Engine");
   public final static Internal              ENGINE_INTERNAL_CLAN = (Internal)ItemDB.lookup("mdf_Engine");
   private final TreeMap<ArmorSide, Integer> armor                = new TreeMap<ArmorSide, Integer>();
   private boolean                           autoArmor            = false;

   private int                               engineHeatsinks      = 0;
   private final InternalComponent           internalComponent;
   private final List<Item>                  items                = new ArrayList<Item>();

   /**
    * Copy constructor. Performs a deep copy of the argument with a new {@link LoadoutStandard} value.
    * 
    * @param aLoadoutPart
    *           The {@link ConfiguredComponent} to copy.
    */
   public ConfiguredComponent(ConfiguredComponent aLoadoutPart){
      internalComponent = aLoadoutPart.internalComponent;
      engineHeatsinks = aLoadoutPart.engineHeatsinks;
      autoArmor = aLoadoutPart.autoArmor;

      for(Map.Entry<ArmorSide, Integer> e : aLoadoutPart.armor.entrySet()){
         armor.put(e.getKey(), new Integer(e.getValue()));
      }

      for(Item item : aLoadoutPart.items){
         items.add(item);
      }
   }

   public ConfiguredComponent(InternalComponent aInternalPart, boolean aAutoArmor){
      internalComponent = aInternalPart;
      items.addAll(internalComponent.getInternalItems());
      autoArmor = aAutoArmor;
      if( internalComponent.getLocation().isTwoSided() ){
         armor.put(ArmorSide.FRONT, 0);
         armor.put(ArmorSide.BACK, 0);
      }
      else{
         armor.put(ArmorSide.ONLY, 0);
      }
   }

   /**
    * Adds a new item to this component. This method is unchecked and can put the component into an illegal state. It is
    * the caller's responsibility to make sure local and global conditions are met before adding an item.
    * <p>
    * This is package visibility as it's intended use is only from {@link OpAddItem}, {@link OpRemoveItem} and
    * relatives.
    * <p>
    * Please note that {@link #canAddItem(Item)} must return true prior to a call to {@link #addItem(Item)}.
    * 
    * @param aItem
    *           The item to add.
    */
   void addItem(Item aItem){
      items.add(aItem);
   }

   /**
    * Checks if all local conditions for the item to be equipped on this component are full filled. Before an item can
    * be equipped, global conditions on the loadout must also be checked by {@link LoadoutBase#canEquip(Item)}.
    * 
    * @param aItem
    *           The item to check with.
    * @return <code>true</code> if local constraints allow the item to be equipped here.
    */
   public boolean canAddItem(Item aItem){
      if( !getInternalComponent().isAllowed(aItem) )
         return false;

      if( aItem instanceof HeatSink && getEngineHeatsinks() < getEngineHeatsinksMax() ){
         return true;
      }

      // Check enough free critical slots
      if( getSlotsFree() < aItem.getNumCriticalSlots() ){
         return false;
      }

      if( aItem == ItemDB.CASE && items.contains(ItemDB.CASE) )
         return false;

      // Check enough free hard points
      if( aItem.getHardpointType() != HardPointType.NONE
          && getItemsOfHardpointType(aItem.getHardpointType()) >= getInternalComponent().getHardPointCount(aItem.getHardpointType()) ){
         return false; // Not enough hard points!
      }
      return true;
   }

   /**
    * Checks if the {@link Item} denoted by the given index can be removed by the user from this component. The index is
    * into the list returned by the last call to {@link #getItemsAll()}.
    * <p>
    * Please note that internal items and other items that are fixed are at the head of the list returned by
    * {@link #getItemsAll()}, when locating the index of an item you want to remove, please use
    * {@link List#lastIndexOf(Object)}.
    * 
    * @param aItemIndex
    *           The index of the item to check if it can removed.
    * @return <code>true</code> if the item can be removed, <code>false</code> otherwise.
    */
   public boolean canRemoveItem(int aItemIndex){
      if( aItemIndex < internalComponent.getInternalItems().size() ){
         return false;
      }
      else if( aItemIndex >= items.size() ){
         return false;
      }
      else if( items.get(aItemIndex) == ENGINE_INTERNAL ){
         return false;
      }
      return true;
   }

   /**
    * Removes an item from this component. This method is unchecked and can put the component into an illegal state. It
    * is the caller's responsibility to make sure local and global conditions are met before removing an item.
    * <p>
    * This is package visibility as it's intended use is only from {@link OpAddItem}, {@link OpRemoveItem} and
    * relatives.
    * <p>
    * Please note that {@link #canRemoveItem(int)} must return true for the removed index prior to a call to
    * {@link #removeItem(int)}.
    * 
    * @param aItemIndex
    *           The index of the item to remove.
    */
   void removeItem(int aItemIndex){
      items.remove(aItemIndex);
   }

   /**
    * @return <code>true</code> if this component allows armor to be manually adjusted.
    */
   public boolean allowAutomaticArmor(){
      return autoArmor;
   }

   @Override
   public boolean equals(Object aObject){
      if( this == aObject )
         return true;
      if( !(aObject instanceof ConfiguredComponent) )
         return false;
      ConfiguredComponent that = (ConfiguredComponent)aObject;

      if( !internalComponent.equals(that.internalComponent) )
         return false;
      if( !ArrayUtils.equalsUnordered(items, that.items) )
         return false;
      if( !armor.equals(that.armor) )
         return false;
      if( engineHeatsinks != that.engineHeatsinks )
         return false;
      if( autoArmor != that.autoArmor )
         return false;
      return true;
   }

   /**
    * @param aArmorSide
    *           The {@link ArmorSide} to query. Querying the wrong side results in a {@link NullPointerException}.
    * @return The current amount of armor on the given side of this component.
    */
   public int getArmor(ArmorSide aArmorSide){
      return armor.get(aArmorSide);
   }

   /**
    * Will return the number of armor points that can be set on the component. Taking both armor sides into account and
    * respecting the max armor limit. Does not take free tonnage into account.
    * 
    * @param aArmorSide
    *           The {@link ArmorSide} to get the max free armor for.
    * @return The number of armor points that can be maximally set (ignoring tonnage).
    */
   public int getArmorMax(ArmorSide aArmorSide){
      switch( aArmorSide ){
         case BACK:
            return getInternalComponent().getArmorMax() - getArmor(ArmorSide.FRONT);
         case FRONT:
            return getInternalComponent().getArmorMax() - getArmor(ArmorSide.BACK);
         default:
         case ONLY:
            return getInternalComponent().getArmorMax();
      }
   }

   /**
    * @return The total number of armor points on this component.
    */
   public int getArmorTotal(){
      int sum = 0;
      for(Integer i : armor.values()){
         sum += i;
      }
      return sum;
   }

   /**
    * @return The number of heat sinks inside the engine (if any) equipped on this component.
    */
   public int getEngineHeatsinks(){
      int ans = 0;
      for(Item i : items){
         if( i instanceof HeatSink )
            ans++;
      }
      return Math.min(ans, getEngineHeatsinksMax());
   }

   /**
    * @return The maximal number of heat sinks that the engine (if any) equipped on this component can sustain.
    */
   public int getEngineHeatsinksMax(){
      for(Item item : items){
         if( item instanceof Engine ){
            return ((Engine)item).getNumHeatsinkSlots();
         }
      }
      return 0;
   }

   /**
    * @return The internal component that is backing this component.
    */
   public InternalComponent getInternalComponent(){
      return internalComponent;
   }

   /**
    * @return The sum of the mass of all items on this component.
    */
   public double getItemMass(){
      double ans = engineHeatsinks * 1.0;
      for(Item item : items){
         ans += item.getMass();
      }
      return ans;
   }

   /**
    * @return An unmodifiable {@link List} of all items on this component, including internals.
    */
   public List<Item> getItemsAll(){
      return Collections.unmodifiableList(items);
   }

   /**
    * @param aHardpointType
    *           The type of {@link HardPointType} to count.
    * @return The number of items of the given hard point of type that are equipped.
    */
   public int getItemsOfHardpointType(HardPointType aHardpointType){
      int hardpoints = 0;
      for(Item it : items){
         if( it.getHardpointType() == aHardpointType ){
            hardpoints++;
         }
      }
      return hardpoints;
   }

   /**
    * @return The number of critical slots locally available on this component. Note: may be less than globally
    *         available slots as this doesn't take floating slots (such as dynamic armor on standard mechs) into
    *         account.
    */
   public int getSlotsFree(){
      return getInternalComponent().getSlots() - getSlotsUsed();
   }

   /**
    * @return The number of critical slots that are used in this component, not counting floating slots used by dynamic
    *         armor or structure.
    */
   public int getSlotsUsed(){
      int crits = 0;
      int engineHsLeft = getEngineHeatsinksMax();
      for(Item item : items){
         if( item instanceof HeatSink && engineHsLeft > 0 ){
            engineHsLeft--;
            continue;
         }
         crits += item.getNumCriticalSlots();
      }
      return crits;
   }

   @Override
   public int hashCode(){
      final int prime = 31;
      int result = 1;
      result = prime * result + ((armor == null) ? 0 : armor.hashCode());
      result = prime * result + engineHeatsinks;
      result = prime * result + ((internalComponent == null) ? 0 : internalComponent.hashCode());
      result = prime * result + ((items == null) ? 0 : items.hashCode());
      return result;
   }

   void setArmor(ArmorSide aArmorSide, int aAmount, boolean aAllowAutomaticArmor){
      armor.put(aArmorSide, aAmount);
      autoArmor = aAllowAutomaticArmor;
   }

   @Override
   public String toString(){
      StringBuilder sb = new StringBuilder();
      if( getInternalComponent().getLocation().isTwoSided() ){
         sb.append(getArmor(ArmorSide.FRONT)).append("/").append(getArmor(ArmorSide.BACK));
      }
      else{
         sb.append(getArmor(ArmorSide.ONLY));
      }
      sb.append(" ");
      for(Item item : items){
         if( item instanceof Internal )
            continue;
         sb.append(item).append(",");
      }
      return sb.toString();
   }
}
