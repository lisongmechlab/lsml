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
package lisong_mechlab.model.upgrades;

import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.upgrades.Upgrades.Message;
import lisong_mechlab.model.upgrades.Upgrades.Message.ChangeMsg;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * This {@link Upgrades} can change the armor type of a {@link LoadoutStandard}.
 * 
 * @author Emily
 */
public class OpSetArmorType extends OpUpgradeBase{
   final ArmorUpgrade oldValue;
   final ArmorUpgrade newValue;

   /**
    * Creates a {@link OpSetArmorType} that only affects a stand-alone {@link Upgrades} object This is useful only for
    * altering {@link Upgrades} objects which are not attached to a {@link LoadoutStandard} in any way.
    * 
    * @param anUpgrades
    *           The {@link Upgrades} object to alter with this {@link Operation}.
    * @param anArmorUpgrade
    *           The new armor type when this upgrades has been applied.
    */
   public OpSetArmorType(Upgrades anUpgrades, ArmorUpgrade anArmorUpgrade){
      super(anUpgrades, anArmorUpgrade.getName());
      oldValue = upgrades.getArmor();
      newValue = anArmorUpgrade;
   }

   /**
    * Creates a new {@link OpSetStructureType} that will change the armor type of a {@link LoadoutStandard}.
    * 
    * @param anXBar
    *           A {@link MessageXBar} to signal changes in internal structure on.
    * @param aLoadout
    *           The {@link LoadoutStandard} to alter.
    * @param anArmorUpgrade
    *           The new armor type this upgrades is applied.
    */
   public OpSetArmorType(MessageXBar anXBar, LoadoutBase<?, ?> aLoadout, ArmorUpgrade anArmorUpgrade){
      super(anXBar, aLoadout, anArmorUpgrade.getName());
      oldValue = upgrades.getArmor();
      newValue = anArmorUpgrade;
   }

   @Override
   protected void apply(){
      set(newValue);
   }

   @Override
   protected void undo(){
      set(oldValue);
   }

   protected void set(ArmorUpgrade aValue){
      if( aValue != upgrades.getArmor() ){
         ArmorUpgrade old = upgrades.getArmor();
         upgrades.setArmor(aValue);

         try{
            verifyLoadoutInvariant();
         }
         catch( Exception e ){
            upgrades.setArmor(old);
            throw new IllegalArgumentException("Couldn't change armour type: ", e);
         }

         if( xBar != null )
            xBar.post(new Message(ChangeMsg.ARMOR, upgrades));
      }
   }
}
