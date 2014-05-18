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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lisong_mechlab.model.Efficiencies;
import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.chassi.InternalComponent;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.chassi.MovementProfile;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.EngineType;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.loadout.component.ComponentBuilder;
import lisong_mechlab.model.loadout.component.ConfiguredComponent;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.util.MessageXBar;

/**
 * This base class right 'ere models contains the common functionality needed for both clan and inner sphere mechs.
 * 
 * @author Emily Björk
 * @param <T>
 *           The type of the {@link ConfiguredComponent} in this loadout.
 * @param <U>
 *           The type of {@link InternalComponent} in T.
 */
public abstract class LoadoutBase<T extends ConfiguredComponent, U extends InternalComponent> {
   private String             name;
   private final T[]          components;
   private final ChassisBase  chassisBase;
   private final Efficiencies efficiencies;

   protected LoadoutBase(ComponentBuilder.Factory<T, U> aFactory, ChassisBase aChassisBase, MessageXBar aXBar){
      name = aChassisBase.getNameShort();
      chassisBase = aChassisBase;
      efficiencies = new Efficiencies(aXBar);
      components = aFactory.defaultComponents(chassisBase);
   }

   protected LoadoutBase(ComponentBuilder.Factory<T, U> aFactory, LoadoutBase<T, U> aLoadoutBase){
      name = aLoadoutBase.name;
      chassisBase = aLoadoutBase.chassisBase;
      efficiencies = new Efficiencies(aLoadoutBase.efficiencies);
      components = aFactory.cloneComponents(aLoadoutBase);
   }

   @Override
   public boolean equals(Object obj){
      if( this == obj )
         return true;
      if( !this.getClass().isAssignableFrom(obj.getClass()) )
         return false;
      @SuppressWarnings("unchecked")
      // I just checked it above...
      LoadoutBase<T, U> that = (LoadoutBase<T, U>)obj;
      if( !chassisBase.equals(that.chassisBase) )
         return false;
      if( !efficiencies.equals(that.efficiencies) )
         return false;
      if( !name.equals(that.name) )
         return false;
      if( !components.equals(that.components) )
         return false;
      return true;
   }

   @Override
   public String toString(){
      if( getName().contains(getChassis().getNameShort()) )
         return getName();
      return getName() + " (" + getChassis().getNameShort() + ")";
   }

   @Override
   public int hashCode(){
      final int prime = 31;
      int result = 1;
      result = prime * result + ((chassisBase == null) ? 0 : chassisBase.hashCode());
      result = prime * result + ((efficiencies == null) ? 0 : efficiencies.hashCode());
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((components == null) ? 0 : components.hashCode());
      return result;
   }

   /**
    * @return A {@link Collection} of all the {@link Item}s on this loadout, including {@link Internal}s.
    */
   public Collection<Item> getAllItems(){
      List<Item> items = new ArrayList<>();
      for(T component : components){
         items.addAll(component.getItemsAll());
      }
      return items;
   }

   /**
    * @return The total number of armor points on this loadout.
    */
   public int getArmor(){
      int ans = 0;
      for(T component : components){
         ans += component.getArmorTotal();
      }
      return ans;
   }

   /**
    * TODO: This should be replaced by a pilot skill tree.
    * 
    * @return The {@link Efficiencies} for this loadout.
    */
   public Efficiencies getEfficiencies(){
      return efficiencies;
   }

   /**
    * @return The {@link Engine} equipped on this loadout, or <code>null</code> if no engine is equipped.
    */
   public abstract Engine getEngine();

   /**
    * @return The current mass of the loadout.
    */
   public double getMass(){
      double ans = getUpgrades().getStructure().getStructureMass(chassisBase);
      for(T component : components){
         ans += component.getItemMass();
      }
      ans += getUpgrades().getArmor().getArmorMass(getArmor());
      return ans;
   }

   /**
    * @return The amount of free tonnage the loadout can still support.
    */
   public double getFreeMass(){
      double freeMass = chassisBase.getMassMax() - getMass();
      return freeMass;
   }

   /**
    * @return The base chassis of this loadout.
    */
   public ChassisBase getChassis(){
      return chassisBase;
   }

   /**
    * @return The user given name of the loadout.
    */
   public String getName(){
      return name;
   }

   /**
    * @return The number of globally available critical slots.
    */
   public int getNumCriticalSlotsFree(){
      return chassisBase.getCriticalSlotsTotal() - getNumCriticalSlotsUsed();
   }

   /**
    * @return The number of globally used critical slots.
    */
   public int getNumCriticalSlotsUsed(){
      int ans = getUpgrades().getStructure().getExtraSlots() + getUpgrades().getArmor().getExtraSlots();
      for(T component : components){
         ans += component.getSlotsUsed();
      }
      return ans;
   }

   /**
    * @param aLocation
    *           The location to get the component for.
    * @return The component at the given location
    */
   public T getComponent(Location aLocation){
      return components[aLocation.ordinal()];
   }

   /**
    * Assigns the internal component vector. Mostly useful for omnimechs.
    * 
    * @param aComponent
    *           The component to set, location is figured out from the component's internal component.
    */
   protected void setComponent(T aComponent){
      components[aComponent.getInternalComponent().getLocation().ordinal()] = aComponent;
   }

   /**
    * @return A {@link Collection} of all the configured components.
    */
   public Collection<T> getComponents(){
      return Collections.unmodifiableList(Arrays.asList(components));
   }

   /**
    * @return The {@link Upgrades} that are equipped on this loadout.
    */
   public abstract Upgrades getUpgrades();

   /**
    * @param aHardpointType
    *           The type of hard points to count.
    * @return The number of hard points of the given type.
    */
   public int getHardpointsCount(HardPointType aHardpointType){
      // Note: This has been moved from chassis base because for omnimechs, the hard point count depends on which
      // omnipods are equipped.
      int sum = 0;
      for(T component : components){
         sum += component.getInternalComponent().getHardPointCount(aHardpointType);
      }
      return sum;
   }

   /**
    * @return The maximal number of jump jets the loadout can support.
    */
   abstract public int getJumpJetsMax();

   /**
    * @return The total number of heat sinks equipped.
    */
   public int getHeatsinksCount(){
      int ans = 0;
      for(Item item : getAllItems()){
         if( item instanceof HeatSink ){
            ans++;
         }
         else if( item instanceof Engine ){
            ans += ((Engine)item).getNumInternalHeatsinks();
         }
      }
      return ans;
   }

   /**
    * @return The total number of jump jets equipped.
    */
   public int getJumpJetCount(){
      int ans = 0;
      for(Item item : getAllItems()){
         if( item instanceof JumpJet )
            ans++;
      }
      return ans;
   }

   /**
    * @return The type of jump jets equipped. TODO: Is this really necessary?
    */
   @Deprecated
   public JumpJet getJumpJetType(){
      for(Item item : getAllItems()){
         if( item instanceof JumpJet ){
            return (JumpJet)item;
         }
      }
      return null;
   }

   /**
    * Gets a {@link List} of {@link ConfiguredComponent}s that could possibly house the given item.
    * <p>
    * This method checks necessary but not sufficient constraints. In other words, the {@link ConfiguredComponent}s in
    * the returned list may or may not be able to hold the {@link Item}. But the {@link ConfiguredComponent}s not in the
    * list are unable to hold the {@link Item}.
    * <p>
    * This method is mainly useful for limiting search spaces for various optimization algorithms.
    * 
    * @param anItem
    *           The {@link Item} to find candidate {@link ConfiguredComponent}s for.
    * @return A {@link List} of {@link ConfiguredComponent}s that might be able to hold the {@link Item}.
    */
   public List<ConfiguredComponent> getCandidateLocationsForItem(Item anItem){
      List<ConfiguredComponent> candidates = new ArrayList<>();
      if( !canEquipGlobal(anItem) )
         return candidates;

      int globalFreeHardPoints = 0;
      HardPointType hardpointType = anItem.getHardpointType();

      for(ConfiguredComponent part : components){
         if( part.getInternalComponent().isAllowed(anItem) ){
            candidates.add(part);
         }

         if( hardpointType != HardPointType.NONE ){
            final int localFreeHardPoints = part.getInternalComponent().getHardPointCount(hardpointType)
                                            - part.getItemsOfHardpointType(hardpointType);
            globalFreeHardPoints += localFreeHardPoints;
         }
      }

      if( hardpointType != HardPointType.NONE && globalFreeHardPoints <= 0 ){
         candidates.clear();
      }

      return candidates;
   }

   /**
    * Package internal function. Use {@link OpRename} to change the name.
    * 
    * @param aNewName
    *           The new name of the loadout.
    */
   void rename(String aNewName){
      name = aNewName;
   }

   /**
    * Checks global constraints that could prevent the item from being added to this {@link LoadoutStandard}.
    * <p>
    * This includes:
    * <ul>
    * <li>Only one engine.</li>
    * <li>Max jump jet count not exceeded.</li>
    * <li>Correct jump jet type.</li>
    * <li>Enough free mass.</li>
    * <li>Enough globally free critical slots.</li>
    * <li>Enough globally free hard points of applicable type.</li>
    * </ul>
    * 
    * @param anItem
    *           The {@link Item} to check for.
    * @return <code>true</code> if the given {@link Item} is globally feasible on this loadout.
    */
   public boolean canEquip(Item anItem){
      if( !canEquipGlobal(anItem) ){
         return false;
      }

      if( anItem instanceof Engine ){
         Engine engine = (Engine)anItem;
         if( engine.getType() == EngineType.XL ){
            if( getComponent(Location.LeftTorso).getSlotsFree() < 3 ){
               return false;
            }
            if( getComponent(Location.RightTorso).getSlotsFree() < 3 ){
               return false;
            }
            if( getNumCriticalSlotsFree() < 3 * 2 + engine.getNumCriticalSlots() ){
               // XL engines return same number of slots as standard engine, check enough slots to cover the
               // side torsii.
               return false;
            }
         }
      }

      for(ConfiguredComponent part : getComponents()){
         if( part.canAddItem(anItem) )
            return true;
      }
      return false;
   }

   /**
    * Checks only global constraints against the {@link Item}. These are necessary but not sufficient conditions. Local
    * conditions are needed to be sufficient.
    * 
    * @param anItem
    *           The {@link Item} to check.
    * @return <code>true</code> if the necessary checks are passed.
    */
   protected boolean canEquipGlobal(Item anItem){
      if( anItem.getMass() > getFreeMass() )
         return false;
      if( !getChassis().isAllowed(anItem) )
         return false;
      if( !anItem.isCompatible(getUpgrades()) )
         return false;

      // Allow engine slot heat sinks as long as there is enough free mass.
      if( anItem instanceof HeatSink
          && getComponent(Location.CenterTorso).getEngineHeatsinks() < getComponent(Location.CenterTorso).getEngineHeatsinksMax() )
         return true;

      if( anItem.getNumCriticalSlots() > getNumCriticalSlotsFree() )
         return false;
      if( anItem instanceof Engine && getEngine() != null )
         return false;
      return true;
   }

   /**
    * Checks if an item can be equipped on a loadout in some way by moving other items around.
    * 
    * @param anItem
    *           The {@link Item} to check for.
    * @return <code>true</code> if the loadout can be permutated in some way that the item can be equipped.
    */
   public boolean hasEquippablePermutation(Item anItem){
      if( !canEquipGlobal(anItem) )
         return false;

      List<ConfiguredComponent> candidates = getCandidateLocationsForItem(anItem);

      for(ConfiguredComponent candidate : candidates){
         if( candidate.canAddItem(anItem) ){
            return true;
         }

         int slotsFree[] = new int[Location.values().length];
         for(Location part : Location.values()){
            slotsFree[part.ordinal()] = getComponent(part).getSlotsFree();
         }

         // Attempt to move items by taking the largest ones first and perform bin packing
         // with First Fit Decreasing heuristic.
         List<Item> itemsBySlots = new ArrayList<>(candidate.getItemsAll());
         Collections.sort(itemsBySlots, new Comparator<Item>(){
            @Override
            public int compare(Item aO1, Item aO2){
               return Integer.compare(aO1.getNumCriticalSlots(), aO2.getNumCriticalSlots());
            }
         });

         // There are enough free hard points in the loadout to contain this item and there
         // are enough globally free slots and free tonnage. Engine and jump jet constraints
         // are already checked. It is enough if the candidate part has enough slots free.
         int candidateSlotsFree = candidate.getSlotsFree();
         while( candidateSlotsFree < anItem.getNumCriticalSlots() && !itemsBySlots.isEmpty() ){
            Item toBeRemoved = itemsBySlots.remove(0);
            if( toBeRemoved instanceof Internal )
               continue;

            // Find first bin where it can be put
            for(ConfiguredComponent part : getComponents()){
               if( part == candidate )
                  continue;
               final int partOrdinal = part.getInternalComponent().getLocation().ordinal();
               final int numCrits = toBeRemoved.getNumCriticalSlots();
               if( slotsFree[partOrdinal] >= numCrits ){
                  HardPointType needHp = toBeRemoved.getHardpointType();
                  if( needHp != HardPointType.NONE
                      && part.getInternalComponent().getHardPointCount(needHp) - part.getItemsOfHardpointType(needHp) < 1 ){
                     continue;
                  }
                  slotsFree[partOrdinal] -= numCrits;
                  candidateSlotsFree += numCrits;
                  break;
               }
            }
         }
         if( candidateSlotsFree >= anItem.getNumCriticalSlots() ){
            return true;
         }
      }
      return false;
   }

   public abstract MovementProfile getMovementProfile();

   /**
    * @param aXBar
    *           A {@link MessageXBar} to send messages on.
    * @return A deep copy of <code>this</code>.
    */
   public abstract LoadoutBase<?, ?> clone(MessageXBar aXBar);
}
