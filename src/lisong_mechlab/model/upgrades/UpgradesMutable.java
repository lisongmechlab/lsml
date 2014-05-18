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

/**
 * This subclass of {@link Upgrades} can be mutated by setters.
 * 
 * @author Emily Björk
 */
public class UpgradesMutable extends Upgrades{

   /**
    * Creates a new {@link UpgradesMutable}.
    * 
    * @param aArmor
    *           The initial {@link ArmorUpgrade}.
    * @param aStructure
    *           The initial {@link StructureUpgrade}.
    * @param aGuidance
    *           The initial {@link GuidanceUpgrade}.
    * @param aHeatSinks
    *           The initial {@link HeatSinkUpgrade}.
    */
   public UpgradesMutable(ArmorUpgrade aArmor, StructureUpgrade aStructure, GuidanceUpgrade aGuidance, HeatSinkUpgrade aHeatSinks){
      super(aArmor, aStructure, aGuidance, aHeatSinks);
   }

   /**
    * Copy constructor, performs a deep copy.
    * 
    * @param aUpgrades
    *           An {@link UpgradesMutable} object to copy.
    */
   public UpgradesMutable(UpgradesMutable aUpgrades){
      super(aUpgrades);
   }

   /**
    * Changes the heat sink type.
    * <p>
    * This is package visibility as it is only intended to be modified by the Op* classes.
    * 
    * @param aHeatsinkUpgrade
    *           The new {@link HeatSinkUpgrade}.
    */
   void setHeatSink(HeatSinkUpgrade aHeatsinkUpgrade){
      heatSinkType = aHeatsinkUpgrade;
   }

   /**
    * Changes the internal structure type.
    * <p>
    * This is package visibility as it is only intended to be modified by the Op* classes.
    * 
    * @param aStructureUpgrade
    *           The new {@link StructureUpgrade}.
    */
   void setStructure(StructureUpgrade aStructureUpgrade){
      structureType = aStructureUpgrade;
   }

   /**
    * Changes the armor type.
    * <p>
    * This is package visibility as it is only intended to be modified by the Op* classes.
    * 
    * @param anArmorUpgrade
    *           The new {@link ArmorUpgrade}.
    */
   void setArmor(ArmorUpgrade anArmorUpgrade){
      armorType = anArmorUpgrade;
   }
}
