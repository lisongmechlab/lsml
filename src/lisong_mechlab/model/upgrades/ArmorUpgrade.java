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

import lisong_mechlab.mwo_data.helpers.ItemStatsUpgradeType;

/**
 * Represents an upgrade to a 'mechs armor.
 * 
 * @author Li Song
 */
public class ArmorUpgrade extends Upgrade{
   private final int    slots;
   private final double armorPerTon;

   public ArmorUpgrade(ItemStatsUpgradeType aUpgradeType){
      super(aUpgradeType);
      slots = aUpgradeType.UpgradeTypeStats.slots;
      armorPerTon = aUpgradeType.UpgradeTypeStats.pointMultiplier * 16;
      // (LoadoutPart.ARMOR_PER_TON * (loadoutPart.getLoadout().getUpgrades().hasFerroFibrous() ? 1.12 : 1));
   }

   /**
    * @return The number of extra slots required by this upgrade.
    */
   public int getExtraSlots(){
      return slots;
   }

   /**
    * @return The number of points of armor per ton from this armor type.
    */
   public double getArmorPerTon(){
      return armorPerTon;
   }

   /**
    * Calculates the mass of the given amount of armor points.
    * 
    * @param aArmor
    *           The amount of armor.
    * @return The mass of the given armor amount.
    */
   public double getArmorMass(int aArmor){
      return aArmor / armorPerTon;
   }
}
