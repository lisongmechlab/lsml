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

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.util.MessageXBar;

/**
 * This class is a simple container that manages upgrades for an loadout.
 * 
 * @author Emily Björk
 */
public class Upgrades{
   private ArmorUpgrade     armorType     = UpgradeDB.STANDARD_ARMOR;
   private StructureUpgrade structureType = UpgradeDB.STANDARD_STRUCTURE;
   private GuidanceUpgrade  guidanceType  = UpgradeDB.STANDARD_GUIDANCE;
   private HeatsinkUpgrade  heatSinkType  = UpgradeDB.STANDARD_HEATSINKS;

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

      public Message(ChangeMsg aChangeMsg, Upgrades anUpgrades){
         msg = aChangeMsg;
         source = anUpgrades;
      }

      @Override
      public boolean isForMe(Loadout aLoadout){
         return aLoadout.getUpgrades() == source;
      }
   }

   /**
    * @param aArmor
    * @param aStructure
    * @param aGuidance
    * @param aHeatSinks
    */
   public Upgrades(ArmorUpgrade aArmor, StructureUpgrade aStructure, GuidanceUpgrade aGuidance, HeatsinkUpgrade aHeatSinks){
      armorType = aArmor;
      structureType = aStructure;
      guidanceType = aGuidance;
      heatSinkType = aHeatSinks;
   }

   /**
    * 
    */
   public Upgrades(){
   }

   @Override
   public boolean equals(Object obj){
      if( this == obj )
         return true;
      if( obj == null )
         return false;
      if( !(obj instanceof Upgrades) )
         return false;
      Upgrades that = (Upgrades)obj;
      if( this.guidanceType != that.guidanceType )
         return false;
      if( this.heatSinkType != that.heatSinkType )
         return false;
      if( this.structureType != that.structureType )
         return false;
      if( this.armorType != that.armorType )
         return false;
      return true;
   }

   public GuidanceUpgrade getGuidance(){
      return guidanceType;
   }

   public HeatsinkUpgrade getHeatSink(){
      return heatSinkType;
   }

   public StructureUpgrade getStructure(){
      return structureType;
   }

   public ArmorUpgrade getArmor(){
      return armorType;
   }

   void setGuidance(GuidanceUpgrade aGuidanceUpgrade){
      guidanceType = aGuidanceUpgrade;
   }

   void setHeatSink(HeatsinkUpgrade aHeatsinkUpgrade){
      heatSinkType = aHeatsinkUpgrade;
   }

   void setStructure(StructureUpgrade aStructureUpgrade){
      structureType = aStructureUpgrade;
   }

   void setArmor(ArmorUpgrade anArmorUpgrade){
      armorType = anArmorUpgrade;
   }
}
