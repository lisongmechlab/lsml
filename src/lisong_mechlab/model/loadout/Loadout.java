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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.TreeMap;

import lisong_mechlab.model.Efficiencies;
import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.chassi.InternalPart;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.loadout.Loadout.Message.Type;
import lisong_mechlab.model.loadout.converters.ChassiConverter;
import lisong_mechlab.model.loadout.converters.ItemConverter;
import lisong_mechlab.model.loadout.converters.LoadoutConverter;
import lisong_mechlab.model.loadout.converters.LoadoutPartConverter;
import lisong_mechlab.model.loadout.converters.UpgradeConverter;
import lisong_mechlab.model.loadout.converters.UpgradesConverter;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * This class represents the complete state of a 'mechs configuration.
 * 
 * @author Emily Björk
 */
public class Loadout{
   public static class Message implements MessageXBar.Message{
      public enum Type{
         RENAME, CREATE
      }

      private final Loadout loadout;

      public final Type     type;

      public Message(Loadout aLoadout, Type aType){
         loadout = aLoadout;
         type = aType;
      }

      @Override
      public boolean equals(Object obj){
         if( this == obj )
            return true;
         if( obj == null )
            return false;
         if( getClass() != obj.getClass() )
            return false;
         Message other = (Message)obj;
         if( loadout == null ){
            if( other.loadout != null )
               return false;
         }
         else if( !loadout.equals(other.loadout) )
            return false;
         if( type != other.type )
            return false;
         return true;
      }

      @Override
      public int hashCode(){
         final int prime = 31;
         int result = 1;
         result = prime * result + ((loadout == null) ? 0 : loadout.hashCode());
         result = prime * result + ((type == null) ? 0 : type.hashCode());
         return result;
      }

      @Override
      public boolean isForMe(Loadout aLoadout){
         return loadout == aLoadout;
      }
   }

   public static Loadout load(File aFile, MessageXBar anXBar){
      XStream stream = loadoutXstream(anXBar);
      return (Loadout)stream.fromXML(aFile);
   }

   public static XStream loadoutXstream(MessageXBar anXBar){
      XStream stream = new XStream(new StaxDriver());
      stream.setMode(XStream.NO_REFERENCES);
      stream.registerConverter(new ChassiConverter());
      stream.registerConverter(new ItemConverter());
      stream.registerConverter(new LoadoutPartConverter(anXBar, null));
      stream.registerConverter(new LoadoutConverter(anXBar));
      stream.registerConverter(new UpgradeConverter());
      stream.registerConverter(new UpgradesConverter());
      stream.omitField(Observable.class, "changed");
      stream.omitField(Observable.class, "obs");
      stream.addImmutableType(Item.class);
      stream.alias("component", LoadoutPart.class);
      stream.alias("loadout", Loadout.class);
      stream.addImplicitMap(Loadout.class, "parts", LoadoutPart.class, "internalpart");
      return stream;
   }

   private String                       name;
   private final Chassi                 chassi;
   private final Map<Part, LoadoutPart> parts    = new TreeMap<Part, LoadoutPart>();
   private final Upgrades               upgrades = new Upgrades();
   private final Efficiencies           efficiencies;

   /**
    * Will create a new, empty load out based on the given chassi. TODO: Is anXBar really needed?
    * 
    * @param aChassi
    *           The chassi to base the load out on.
    * @param anXBar
    *           The {@link MessageXBar} to signal changes to this loadout on.
    */
   public Loadout(Chassi aChassi, MessageXBar anXBar){
      name = aChassi.getNameShort();
      chassi = aChassi;
      for(InternalPart part : chassi.getInternalParts()){
         LoadoutPart confPart = new LoadoutPart(this, part);
         parts.put(part.getType(), confPart);
      }

      anXBar.post(new Message(this, Type.CREATE));
      efficiencies = new Efficiencies(anXBar);
   }

   /**
    * Will load a stock load out for the given variation name.
    * 
    * @param aString
    *           The name of the stock variation to load.
    * @param anXBar
    * @throws Exception
    */
   public Loadout(String aString, MessageXBar anXBar) throws Exception{
      this(ChassiDB.lookup(aString), anXBar);
      OperationStack operationStack = new OperationStack(0);
      operationStack.pushAndApply(new LoadStockOperation(this, anXBar));
   }

   @Override
   public boolean equals(Object obj){
      if( this == obj )
         return true;
      if( !(obj instanceof Loadout) )
         return false;
      Loadout that = (Loadout)obj;
      if( !chassi.equals(that.chassi) )
         return false;
      if( !efficiencies.equals(that.efficiencies) )
         return false;
      if( !name.equals(that.name) )
         return false;
      if( !parts.equals(that.parts) )
         return false;
      if( !upgrades.equals(that.upgrades) )
         return false;
      return true;
   }

   public Collection<Item> getAllItems(){
      List<Item> items = new ArrayList<>();
      for(LoadoutPart part : parts.values()){
         items.addAll(part.getItems());
      }
      return items;
   }

   public int getArmor(){
      int ans = 0;
      for(LoadoutPart partConf : parts.values()){
         ans += partConf.getArmorTotal();
      }
      return ans;
   }

   public Chassi getChassi(){
      return chassi;
   }

   public Efficiencies getEfficiencies(){
      return efficiencies;
   }

   public Engine getEngine(){
      for(Item item : getPart(Part.CenterTorso).getItems()){
         if( item instanceof Engine ){
            return (Engine)item;
         }
      }
      return null;
   }

   public double getFreeMass(){
      double freeMass = chassi.getMassMax() - getMass();
      return freeMass;
   }

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

   public int getJumpJetCount(){
      int ans = 0;
      for(Item item : getAllItems()){
         if( item instanceof JumpJet )
            ans++;
      }
      return ans;
   }

   public JumpJet getJumpJetType(){
      for(Item item : getAllItems()){
         if( item instanceof JumpJet ){
            return (JumpJet)item;
         }
      }
      return null;
   }

   public double getMass(){
      double ans = upgrades.getStructure().getStructureMass(chassi);
      for(LoadoutPart partConf : parts.values()){
         ans += partConf.getItemMass();
      }
      return ans;
   }

   public String getName(){
      return name;
   }

   public int getNumCriticalSlotsFree(){
      return 12 * 5 + 6 * 3 - getNumCriticalSlotsUsed();
   }

   public int getNumCriticalSlotsUsed(){
      int ans = upgrades.getStructure().getExtraSlots() + upgrades.getArmor().getExtraSlots();
      for(LoadoutPart partConf : parts.values()){
         ans += partConf.getNumCriticalSlotsUsed();
      }
      return ans;
   }

   public LoadoutPart getPart(Part aPartType){
      return parts.get(aPartType);
   }

   public Collection<LoadoutPart> getPartLoadOuts(){
      return parts.values();
   }

   public Upgrades getUpgrades(){
      return upgrades;
   }

   @Override
   public int hashCode(){
      final int prime = 31;
      int result = 1;
      result = prime * result + ((chassi == null) ? 0 : chassi.hashCode());
      result = prime * result + ((efficiencies == null) ? 0 : efficiencies.hashCode());
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((parts == null) ? 0 : parts.hashCode());
      result = prime * result + ((upgrades == null) ? 0 : upgrades.hashCode());
      return result;
   }

   public boolean isEquippable(Item anItem){
      for(LoadoutPart part : parts.values()){
         if( part.canAddItem(anItem) )
            return true;
      }
      return false;
   }

   @Override
   public String toString(){
      if( getName().contains(chassi.getNameShort()) )
         return getName();
      return getName() + " (" + chassi.getNameShort() + ")";
   }

   /**
    * Package internal function. Use {@link RenameOperation} to change the name.
    * 
    * @param aNewName
    *           The new name of the loadout.
    */
   void rename(String aNewName){
      name = aNewName;
   }
}
