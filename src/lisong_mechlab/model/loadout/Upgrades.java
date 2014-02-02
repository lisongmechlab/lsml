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

import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.MissileWeapon;
import lisong_mechlab.model.loadout.OperationStack.CompositeOperation;
import lisong_mechlab.model.loadout.Upgrades.Message.ChangeMsg;
import lisong_mechlab.model.loadout.part.AddItemOperation;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.loadout.part.RemoveItemOperation;
import lisong_mechlab.util.MessageXBar;

public class Upgrades{
   private boolean                     artemis;
   private boolean                     ferroFibrous;
   private boolean                     endoSteel;
   private boolean                     dhs;

   private transient final MessageXBar xBar;

   public static class Message implements MessageXBar.Message{
      public final ChangeMsg msg;
      private final Upgrades source;

      public enum ChangeMsg{
         GUIDANCE, STRUCTURE, ARMOR, HEATSINKS
      }

      @Override
      public boolean equals(Object obj){
         if( obj instanceof Message ){
            Message other = (Message)obj;
            return msg == other.msg && source == other.source;
         }
         return false;
      }

      Message(ChangeMsg aChangeMsg, Upgrades anUpgrades){
         msg = aChangeMsg;
         source = anUpgrades;
      }

      @Override
      public boolean isForMe(Loadout aLoadout){
         return aLoadout.getUpgrades() == source;
      }
   }

   public Upgrades(MessageXBar anXBar){
      xBar = anXBar;
   }

   @Override
   public int hashCode(){
      final int prime = 31;
      int result = 1;
      result = prime * result + (artemis ? 1231 : 1237);
      result = prime * result + (dhs ? 1231 : 1237);
      result = prime * result + (endoSteel ? 1231 : 1237);
      result = prime * result + (ferroFibrous ? 1231 : 1237);
      return result;
   }

   @Override
   public boolean equals(Object obj){
      if( this == obj )
         return true;
      if( obj == null )
         return false;
      if( !(obj instanceof Upgrades) )
         return false;
      Upgrades other = (Upgrades)obj;
      if( artemis != other.artemis )
         return false;
      if( dhs != other.dhs )
         return false;
      if( endoSteel != other.endoSteel )
         return false;
      if( ferroFibrous != other.ferroFibrous )
         return false;
      return true;
   }

   public boolean hasArtemis(){
      return artemis;
   }

   public boolean hasDoubleHeatSinks(){
      return dhs;
   }

   public boolean hasEndoSteel(){
      return endoSteel;
   }

   public boolean hasFerroFibrous(){
      return ferroFibrous;
   }

   private abstract class UpgradeOperation extends CompositeOperation{
      final Loadout loadout;

      public UpgradeOperation(Loadout aLoadout, String aDescription){
         super(aDescription);
         loadout = aLoadout;
      }

      protected void verifyLoadoutInvariant(){
         if( loadout.getFreeMass() < 0 ){
            throw new IllegalArgumentException("Not enough tonnage!");
         }
         if( loadout.getNumCriticalSlotsFree() < 0 ){
            throw new IllegalArgumentException("Not enough free slots!");
         }
         for(LoadoutPart loadoutPart : loadout.getPartLoadOuts()){
            if( loadoutPart.getNumCriticalSlotsFree() < 0 ){
               throw new IllegalArgumentException("Not enough free slots!");
            }
         }
      }
   }

   public class SetArtemisOperation extends UpgradeOperation{
      final boolean oldValue;
      final boolean newValue;

      public SetArtemisOperation(Loadout aLoadout, boolean anArtemis){
         super(aLoadout, anArtemis ? "enable artemis" : "disable artemis");
         oldValue = artemis;
         newValue = anArtemis;
      }

      @Override
      protected void apply(){
         set(newValue);
         super.apply();
      }

      @Override
      protected void undo(){
         super.undo();
         set(oldValue);
      }

      protected void set(boolean aValue){
         if( aValue != artemis ){
            boolean old = artemis;
            artemis = aValue;

            try{
               verifyLoadoutInvariant();
            }
            catch( Exception e ){
               artemis = old;
               throw new IllegalArgumentException("Couldn't change artemis: ", e);
            }

            for(MissileWeapon weapon : ItemDB.lookup(MissileWeapon.class)){
               artemis = old;
               Ammunition oldAmmo = weapon.getAmmoType(Upgrades.this);
               artemis = aValue;
               Ammunition newAmmo = weapon.getAmmoType(Upgrades.this);
               if( oldAmmo != newAmmo ){
                  for(LoadoutPart loadoutPart : loadout.getPartLoadOuts()){
                     for(Item item : loadoutPart.getItems()){
                        if( item == oldAmmo ){
                           addOp(new RemoveItemOperation(xBar, loadoutPart, oldAmmo));
                           addOp(new AddItemOperation(xBar, loadoutPart, newAmmo));
                        }
                     }
                  }
               }
            }

            if( xBar != null )
               xBar.post(new Message(ChangeMsg.GUIDANCE, Upgrades.this));
         }
      }
   }

   public class SetDHSOperation extends UpgradeOperation{
      final boolean oldValue;
      final boolean newValue;

      public SetDHSOperation(Loadout aLoadout, boolean aDHS){
         super(aLoadout, aDHS ? "enable DHS" : "disable DHS");
         oldValue = dhs;
         newValue = aDHS;

         if( oldValue != newValue ){
            for(LoadoutPart loadoutPart : loadout.getPartLoadOuts()){
               int hsRemoved = 0;
               for(Item item : loadoutPart.getItems()){
                  if( item instanceof HeatSink ){
                     addOp(new RemoveItemOperation(xBar, loadoutPart, item));
                     hsRemoved++;
                  }
               }

               HeatSink oldHsType = oldValue ? ItemDB.DHS : ItemDB.SHS;
               HeatSink newHsType = newValue ? ItemDB.DHS : ItemDB.SHS;
               int slotsFree = oldHsType.getNumCriticalSlots(Upgrades.this) * hsRemoved + loadoutPart.getNumCriticalSlotsFree();
               int hsToAdd = Math.min(hsRemoved, slotsFree / newHsType.getNumCriticalSlots(Upgrades.this));
               while( hsToAdd > 0 ){
                  hsToAdd--;
                  addOp(new AddItemOperation(xBar, loadoutPart, newHsType));
               }
            }
         }
      }

      @Override
      protected void apply(){
         set(newValue);
         super.apply();
      }

      @Override
      protected void undo(){
         super.undo();
         set(oldValue);
      }

      protected void set(boolean aValue){
         if( aValue != dhs ){
            boolean old = dhs;
            dhs = aValue;

            try{
               verifyLoadoutInvariant();
            }
            catch( Exception e ){
               dhs = old;
               throw new IllegalArgumentException("Couldn't change heat sinks: ", e);
            }

            if( xBar != null )
               xBar.post(new Message(ChangeMsg.HEATSINKS, Upgrades.this));
         }
      }
   }

   public class SetEndoSteelOperation extends UpgradeOperation{
      final boolean oldValue;
      final boolean newValue;

      public SetEndoSteelOperation(Loadout aLoadout, boolean aEndoSteel){
         super(aLoadout, aEndoSteel ? "enable Endo Steel" : "disable Endo Steel");
         oldValue = endoSteel;
         newValue = aEndoSteel;
      }

      @Override
      protected void apply(){
         set(newValue);
      }

      @Override
      protected void undo(){
         set(oldValue);
      }

      protected void set(boolean aValue){
         if( aValue != endoSteel ){
            boolean old = endoSteel;
            endoSteel = aValue;

            try{
               verifyLoadoutInvariant();
            }
            catch( Exception e ){
               endoSteel = old;
               throw new IllegalArgumentException("Couldn't change internal structure: ", e);
            }

            if( xBar != null )
               xBar.post(new Message(ChangeMsg.STRUCTURE, Upgrades.this));
         }
      }
   }

   public class SetFerroFibrousOperation extends UpgradeOperation{
      final boolean oldValue;
      final boolean newValue;

      public SetFerroFibrousOperation(Loadout aLoadout, boolean aFerroFibrous){
         super(aLoadout, aFerroFibrous ? "enable Ferro Fibrous" : "disable Ferro Fibrous");
         oldValue = ferroFibrous;
         newValue = aFerroFibrous;
      }

      @Override
      protected void apply(){
         set(newValue);
      }

      @Override
      protected void undo(){
         set(oldValue);
      }

      protected void set(boolean aValue){
         if( aValue != ferroFibrous ){
            boolean old = ferroFibrous;
            ferroFibrous = aValue;

            try{
               verifyLoadoutInvariant();
            }
            catch( Exception e ){
               ferroFibrous = old;
               throw new IllegalArgumentException("Couldn't change armour type: ", e);
            }

            if( xBar != null )
               xBar.post(new Message(ChangeMsg.ARMOR, Upgrades.this));
         }
      }
   }
}
