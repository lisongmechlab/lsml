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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lisong_mechlab.model.Efficiencies;
import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.ComponentBase;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.chassi.MovementProfile;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.EngineType;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.item.PilotModule;
import lisong_mechlab.model.item.WeaponModifier;
import lisong_mechlab.model.loadout.component.ComponentBuilder;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.loadout.component.ConfiguredComponentOmniMech;
import lisong_mechlab.model.loadout.component.ConfiguredComponentStandard;
import lisong_mechlab.model.loadout.converters.ChassiConverter;
import lisong_mechlab.model.loadout.converters.ConfiguredComponentConverter;
import lisong_mechlab.model.loadout.converters.ItemConverter;
import lisong_mechlab.model.loadout.converters.LoadoutConverter;
import lisong_mechlab.model.loadout.converters.UpgradeConverter;
import lisong_mechlab.model.loadout.converters.UpgradesConverter;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.util.MessageXBar;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * This base class right 'ere models contains the common functionality needed for both clan and inner sphere mechs.
 * 
 * @author Li Song
 * @param <T>
 *           The type of the {@link ConfiguredComponentBase} in this loadout.
 */
public abstract class LoadoutBase<T extends ConfiguredComponentBase> {
   private String                  name;
   private final ChassisBase       chassisBase;
   private final T[]               components;
   private final Efficiencies      efficiencies;
   private final List<PilotModule> modules;

   protected LoadoutBase(ComponentBuilder.Factory<T> aFactory, ChassisBase aChassisBase, MessageXBar aXBar){
      name = aChassisBase.getNameShort();
      chassisBase = aChassisBase;
      efficiencies = new Efficiencies(aXBar);
      modules = new ArrayList<>();
      components = aFactory.defaultComponents(chassisBase);
   }

   protected LoadoutBase(ComponentBuilder.Factory<T> aFactory, LoadoutBase<T> aLoadoutBase){
      name = aLoadoutBase.name;
      chassisBase = aLoadoutBase.chassisBase;
      efficiencies = new Efficiencies(aLoadoutBase.efficiencies);
      modules = new ArrayList<>(aLoadoutBase.modules);
      components = aFactory.cloneComponents(aLoadoutBase);
   }

   public static XStream loadoutXstream(MessageXBar aXBar){
      XStream stream = new XStream(new StaxDriver());
      stream.autodetectAnnotations(true);
      stream.setMode(XStream.NO_REFERENCES);
      stream.registerConverter(new ChassiConverter());
      stream.registerConverter(new ItemConverter());
      stream.registerConverter(new ConfiguredComponentConverter(aXBar, null));
      stream.registerConverter(new LoadoutConverter(aXBar));
      stream.registerConverter(new UpgradeConverter());
      stream.registerConverter(new UpgradesConverter());
      stream.addImmutableType(Item.class);
      stream.alias("component", ConfiguredComponentStandard.class);
      stream.alias("loadout", LoadoutBase.class);
      return stream;
   }

   @Override
   public boolean equals(Object obj){
      if( this == obj )
         return true;
      if( !this.getClass().isAssignableFrom(obj.getClass()) )
         return false;
      @SuppressWarnings("unchecked")
      // I just checked it above...
      LoadoutBase<T> that = (LoadoutBase<T>)obj;
      if( chassisBase != that.chassisBase )
         return false;
      if( !efficiencies.equals(that.efficiencies) )
         return false;
      if( !name.equals(that.name) )
         return false;
      if( !Arrays.equals(components, that.components) )
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
         items.addAll(component.getItemsFixed());
         items.addAll(component.getItemsEquipped());
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
    * @return The mass of the loadout excluding armor. This is useful to avoid floating point precision issues from
    *         irrational armor values.
    */
   public double getMassStructItems(){
      double ans = getUpgrades().getStructure().getStructureMass(chassisBase);
      for(T component : components){
         ans += component.getItemMass();
      }
      return ans;
   }

   /**
    * @return The current mass of the loadout.
    */
   public double getMass(){
      double ans = getMassStructItems();
      ans += getUpgrades().getArmor().getArmorMass(getArmor());
      return ans;
   }

   /**
    * @return The amount of free tonnage the loadout can still support.
    */
   public double getFreeMass(){
      double ans = chassisBase.getMassMax() - getMass();
      return ans;
   }

   /**
    * @return The base chassis of this loadout.
    */
   public ChassisBase getChassis(){
      return chassisBase;
   }

   /**
    * @return An unmodifiable {@link Collection} of all the equipped pilot modules.
    */
   public List<PilotModule> getModules(){
      return Collections.unmodifiableList(modules);
   }

   /**
    * @return The maximal number of modules that can be equipped on this {@link LoadoutBase}.
    */
   public abstract int getModulesMax();

   /**
    * @param aModule
    *           The module to test if it can be added to this loadout.
    * @return <code>true</code> if the given module can be added to this loadout.
    */
   public boolean canAddModule(PilotModule aModule){
      if( getModules().contains(aModule) )
         return false;
      if( !aModule.getFaction().isCompatible(getChassis().getFaction()) )
         return false;

      // TODO: Apply any additional limitations on modules
      return getModules().size() < getModulesMax();
   }

   /**
    * @param aModule
    *           The {@link PilotModule} to add to this {@link LoadoutBase}.
    */
   public void addModule(PilotModule aModule){
      modules.add(aModule);
   }

   /**
    * @param aModule
    *           The {@link PilotModule} to remove from this {@link LoadoutBase}.
    */
   public void removeModule(PilotModule aModule){
      modules.remove(aModule);
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
   public abstract int getNumCriticalSlotsUsed();

   /**
    * @param aLocation
    *           The location to get the component for.
    * @return The component at the given location
    */
   public T getComponent(Location aLocation){
      return components[aLocation.ordinal()];
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
         sum += component.getHardPointCount(aHardpointType);
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
    * Gets a {@link List} of {@link ConfiguredComponentBase}s that could possibly house the given item.
    * <p>
    * This method checks necessary but not sufficient constraints. In other words, the {@link ConfiguredComponentBase}s
    * in the returned list may or may not be able to hold the {@link Item}. But the {@link ConfiguredComponentBase}s not
    * in the list are unable to hold the {@link Item}.
    * <p>
    * This method is mainly useful for limiting search spaces for various optimization algorithms.
    * 
    * @param aItem
    *           The {@link Item} to find candidate {@link ConfiguredComponentBase}s for.
    * @return A {@link List} of {@link ConfiguredComponentBase}s that might be able to hold the {@link Item}.
    */
   public List<ConfiguredComponentBase> getCandidateLocationsForItem(Item aItem){
      List<ConfiguredComponentBase> candidates = new ArrayList<>();
      if( !canEquipGlobal(aItem) )
         return candidates;

      int globalFreeHardPoints = 0;
      HardPointType hardpointType = aItem.getHardpointType();

      for(ConfiguredComponentBase part : components){
         ComponentBase internal = part.getInternalComponent();
         if( internal.isAllowed(aItem, getEngine()) ){
            if(aItem.getHardpointType() != HardPointType.NONE && part instanceof ConfiguredComponentOmniMech){
               ConfiguredComponentOmniMech componentOmniMech = (ConfiguredComponentOmniMech)part;
               if(componentOmniMech.getOmniPod().getHardPointCount(aItem.getHardpointType()) < 1)
                  continue;
            }
            candidates.add(part);
         }

         if( hardpointType != HardPointType.NONE ){
            final int localFreeHardPoints = part.getHardPointCount(hardpointType) - part.getItemsOfHardpointType(hardpointType);
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
         // The case where adding a weapon that would cause LAA/HA to be removed will not cause an issue as omnimechs
         // where this can occur, have fixed armor and structure slots.
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

      for(ConfiguredComponentBase part : getComponents()){
         if( part.canAddItem(anItem) )
            return true;
      }
      return false;
   }

   public boolean isValidLoadout(){
      if( getFreeMass() < 0 )
         return false;

      if( getNumCriticalSlotsFree() < 0 )
         return false;

      if( getJumpJetCount() > getJumpJetsMax() )
         return false;

      if( getModules().size() > getModulesMax() )
         return false;

      if( getArmor() > getChassis().getArmorMax() )
         return false;

      for(T component : getComponents()){
         if( !component.isValidLoadout() )
            return false;
      }
      return true;
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

      if( anItem instanceof JumpJet && getJumpJetsMax() - getJumpJetCount() < 1 )
         return false;

      // Allow engine slot heat sinks as long as there is enough free mass.
      if( anItem instanceof HeatSink
          && getComponent(Location.CenterTorso).getEngineHeatsinks() < getComponent(Location.CenterTorso).getEngineHeatsinksMax() )
         return true;

      // FIXME: The case where adding a weapon that would cause LAA/HA to be removed
      // while at max global slots fails even if it might succeed.

      if( anItem.getNumCriticalSlots() > getNumCriticalSlotsFree() )
         return false;
      if( anItem instanceof Engine && getEngine() != null )
         return false;
      return true;
   }

   public abstract MovementProfile getMovementProfile();

   /**
    * @param aXBar
    *           A {@link MessageXBar} to send messages on.
    * @return A deep copy of <code>this</code>.
    */
   public abstract LoadoutBase<?> clone(MessageXBar aXBar);

   /**
    * @return A {@link List} of all {@link WeaponModifier}s that apply to this loadout.
    */
   public Collection<WeaponModifier> getWeaponModifiers(){
      List<WeaponModifier> ans = new ArrayList<>();
      for(PilotModule module : modules){
         if( module instanceof WeaponModifier )
            ans.add((WeaponModifier)module);
      }
      for(Item item : getAllItems()){
         if( item instanceof WeaponModifier )
            ans.add((WeaponModifier)item);
      }

      return ans;
   }

}
