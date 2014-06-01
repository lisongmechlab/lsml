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

import java.io.File;

import lisong_mechlab.model.chassi.ChassisDB;
import lisong_mechlab.model.chassi.ChassisStandard;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.chassi.MovementProfile;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.loadout.component.ComponentBuilder;
import lisong_mechlab.model.loadout.component.ConfiguredComponentStandard;
import lisong_mechlab.model.loadout.converters.ChassiConverter;
import lisong_mechlab.model.loadout.converters.ConfiguredComponentConverter;
import lisong_mechlab.model.loadout.converters.ItemConverter;
import lisong_mechlab.model.loadout.converters.LoadoutConverter;
import lisong_mechlab.model.loadout.converters.UpgradeConverter;
import lisong_mechlab.model.loadout.converters.UpgradesConverter;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.model.upgrades.UpgradesMutable;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * This class represents the complete state of a 'mechs configuration.
 * 
 * @author Emily Björk
 */
public class LoadoutStandard extends LoadoutBase<ConfiguredComponentStandard>{
   private final UpgradesMutable upgrades;

   public static LoadoutStandard load(File aFile, MessageXBar aXBar){
      XStream stream = loadoutXstream(aXBar);
      return (LoadoutStandard)stream.fromXML(aFile);
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
      stream.alias("loadout", LoadoutStandard.class);
      return stream;
   }

   /**
    * Will create a new, empty load out based on the given chassis. TODO: Is anXBar really needed?
    * 
    * @param aChassi
    *           The chassis to base the load out on.
    * @param aXBar
    *           The {@link MessageXBar} to signal changes to this loadout on.
    */
   public LoadoutStandard(ChassisStandard aChassi, MessageXBar aXBar){
      super(ComponentBuilder.getISComponentFactory(), aChassi, aXBar);

      upgrades = new UpgradesMutable(UpgradeDB.STANDARD_ARMOR, UpgradeDB.STANDARD_STRUCTURE, UpgradeDB.STANDARD_GUIDANCE,
                                     UpgradeDB.STANDARD_HEATSINKS);

      if( aXBar != null ){
         aXBar.post(new LoadoutMessage(this, LoadoutMessage.Type.CREATE));
      }
   }

   @Override
   public int hashCode(){
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + upgrades.hashCode();
      return result;
   }

   @Override
   public boolean equals(Object obj){
      if( this == obj )
         return true;
      if( !super.equals(obj) )
         return false;
      if( !(obj instanceof LoadoutStandard) )
         return false;
      LoadoutStandard other = (LoadoutStandard)obj;
      if( !upgrades.equals(other.upgrades) )
         return false;
      return true;
   }

   /**
    * Will load a stock load out for the given variation name.
    * 
    * @param aString
    *           The name of the stock variation to load.
    * @param aXBar
    * @throws Exception
    */
   public LoadoutStandard(String aString, MessageXBar aXBar) throws Exception{
      this((ChassisStandard)ChassisDB.lookup(aString), aXBar);
      OperationStack operationStack = new OperationStack(0);
      operationStack.pushAndApply(new OpLoadStock(getChassis(), this, aXBar));
   }

   public LoadoutStandard(LoadoutStandard aLoadout, MessageXBar aXBar){
      super(ComponentBuilder.getISComponentFactory(), aLoadout);
      upgrades = new UpgradesMutable(aLoadout.upgrades);
      if( aXBar != null ){
         aXBar.post(new LoadoutMessage(this, LoadoutMessage.Type.CREATE));
      }
   }

   @Override
   public ChassisStandard getChassis(){
      return (ChassisStandard)super.getChassis();
   }

   /**
    * @return The {@link Engine} equipped on this loadout, or <code>null</code> if no engine is equipped.
    */
   @Override
   public Engine getEngine(){
      // The engine is not among the fixed items for a standard loadout.
      for(Item item : getComponent(Location.CenterTorso).getItemsEquipped()){
         if( item instanceof Engine ){
            return (Engine)item;
         }
      }
      return null;
   }

   /**
    * Checks only global constraints against the {@link Item}. These are necessary but not sufficient conditions. Local
    * conditions are needed to be sufficient.
    * 
    * @param anItem
    *           The {@link Item} to check.
    * @return <code>true</code> if the necessary checks are passed.
    */
   @Override
   protected boolean canEquipGlobal(Item anItem){
      if( anItem instanceof JumpJet && getChassis().getJumpJetsMax() - getJumpJetCount() < 1 )
         return false;
      return super.canEquipGlobal(anItem);
   }

   @Override
   public MovementProfile getMovementProfile(){
      return getChassis().getMovementProfileBase();
   }

   @Override
   public LoadoutStandard clone(MessageXBar aXBar){
      return new LoadoutStandard(this, aXBar);
   }

   @Override
   public int getJumpJetsMax(){
      return getChassis().getJumpJetsMax();
   }

   @Override
   public UpgradesMutable getUpgrades(){
      return upgrades;
   }
}
