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
package lisong_mechlab.model.upgrades;

import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.MissileWeapon;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.part.AddItemOperation;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.loadout.part.RemoveItemOperation;
import lisong_mechlab.model.upgrades.Upgrades.Message;
import lisong_mechlab.model.upgrades.Upgrades.Message.ChangeMsg;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * This {@link Operation} changes the artemis status of a {@link Loadout}.
 * 
 * @author Li Song
 */
public class SetArtemisOperation extends UpgradeOperation{
   final boolean oldValue;
   final boolean newValue;

   /**
    * Creates a {@link SetArtemisOperation} that only affects a stand-alone {@link Upgrades} object This is useful only
    * for altering {@link Upgrades} objects which are not attached to a {@link Loadout} in any way.
    * 
    * @param anUpgrades
    *           The {@link Upgrades} object to alter with this {@link Operation}.
    * @param anArtemis
    *           The new status of artemis when this upgrades has been applied.
    */
   public SetArtemisOperation(Upgrades anUpgrades, boolean anArtemis){
      super(anUpgrades, anArtemis ? "enable artemis" : "disable artemis");
      oldValue = upgrades.hasArtemis();
      newValue = anArtemis;
   }

   /**
    * Creates a new {@link SetArtemisOperation} that will change the artemis status of a {@link Loadout}.
    * 
    * @param anXBar
    *           A {@link MessageXBar} to signal changes in artemis status on.
    * @param aLoadout
    *           The {@link Loadout} to alter.
    * @param anArtemis
    *           The new artemis status after this upgrades is applied.
    */
   public SetArtemisOperation(MessageXBar anXBar, Loadout aLoadout, boolean anArtemis){
      super(anXBar, aLoadout, anArtemis ? "enable artemis" : "disable artemis");
      oldValue = upgrades.hasArtemis();
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
      if( aValue != upgrades.hasArtemis() ){
         boolean old = upgrades.hasArtemis();
         upgrades.setArtemis(aValue);

         try{
            verifyLoadoutInvariant();
         }
         catch( Exception e ){
            upgrades.setArtemis(old);
            throw new IllegalArgumentException("Couldn't change artemis: ", e);
         }

         if( loadout != null ){
            for(MissileWeapon weapon : ItemDB.lookup(MissileWeapon.class)){
               upgrades.setArtemis(old);
               Ammunition oldAmmo = weapon.getAmmoType(upgrades);
               upgrades.setArtemis(aValue);
               Ammunition newAmmo = weapon.getAmmoType(upgrades);
               if( oldAmmo == newAmmo ){
                  continue;
               }
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
            xBar.post(new Message(ChangeMsg.GUIDANCE, upgrades));
      }
   }
}
