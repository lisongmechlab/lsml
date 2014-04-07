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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.loadout.part.SetArmorOperation;
import lisong_mechlab.model.upgrades.ArmorUpgrade;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.CompositeOperation;

/**
 * This operation will distribute a number of points of armor (rounded down to the closest half ton) on a loadout,
 * respecting manually set values.
 * 
 * @author Li Song
 */
public class DistributeArmorOperation extends CompositeOperation{
   private final static List<Part>  CRITICAL_PARTS = Arrays.asList(Part.Head, Part.LeftLeg, Part.RightLeg, Part.CenterTorso);
   private final static Part[]      PART_ORDER     = new Part[] {Part.Head, Part.LeftArm, Part.RightArm, Part.LeftTorso, Part.RightTorso,
         Part.LeftLeg, Part.RightLeg, Part.CenterTorso};

   private final Map<Part, Integer> armors         = new TreeMap<>();

   /**
    * @param aLoadout
    * @param aPointsOfArmor
    * @param aFrontRearRatio
    * @param aXBar
    */
   public DistributeArmorOperation(Loadout aLoadout, int aPointsOfArmor, double aFrontRearRatio, MessageXBar aXBar){
      super("distribute armor");
      final double armorPerTon = aLoadout.getUpgrades().getArmor().getArmorPerTon();
      final double armorTons = aPointsOfArmor / armorPerTon;
      final int armorHalfTons = (int)(armorTons * 2.0);
      int armorLeft = (int)(armorPerTon * armorHalfTons / 2.0);

      // We can't apply more armor than we can carry
      {
         ArmorUpgrade armorUpgrade = aLoadout.getUpgrades().getArmor();
         int maxArmorTonnage = (int)((aLoadout.getFreeMass() + armorUpgrade.getArmorMass(aLoadout.getArmor())) * armorPerTon);
         armorLeft = Math.min(maxArmorTonnage, armorLeft);
      }

      int maxArmorPoints = 0;

      // Discount armor that is manually fixed.
      for(Part part : Part.values()){
         final LoadoutPart loadoutPart = aLoadout.getPart(part);
         if( !loadoutPart.allowAutomaticArmor() ){
            armorLeft -= loadoutPart.getArmorTotal();
         }
         else{
            maxArmorPoints += loadoutPart.getInternalPart().getArmorMax();
         }
      }
      armorLeft = Math.min(maxArmorPoints, armorLeft);

      // First pass, make sure all parts with equipment and critical parts get maximum armor.
      if( armorLeft > 0 )
         armorLeft = distributeArmor(aLoadout, armorLeft);

      // Second pass, spread out remaining armor on components that are not at max yet.
      if( armorLeft > 0 )
         armorLeft = distributeArmorLeftOvers(aLoadout, armorLeft);

      applyArmors(aLoadout, aFrontRearRatio, aXBar);
   }

   private boolean isCritical(LoadoutPart aPart){
      if( CRITICAL_PARTS.contains(aPart.getInternalPart().getType()) )
         return true;
      if( aPart.getItems().contains(LoadoutPart.ENGINE_INTERNAL) )
         return true;
      return false;
   }

   private List<Part> getInterestingParts(Loadout aLoadout){
      List<Part> parts = new ArrayList<>(Arrays.asList(PART_ORDER));

      Iterator<Part> iterator = parts.iterator();
      while( iterator.hasNext() ){
         Part next = iterator.next();
         LoadoutPart loadoutPart = aLoadout.getPart(next);
         if( !loadoutPart.allowAutomaticArmor() )
            iterator.remove();
      }
      return parts;
   }

   private int distributeArmor(Loadout aLoadout, int aArmorLeft){
      List<Part> parts = getInterestingParts(aLoadout);
      int partsLeft = parts.size();
      for(Part part : parts){
         final LoadoutPart loadoutPart = aLoadout.getPart(part);
         if( 0.0 == loadoutPart.getItemMass() && !isCritical(loadoutPart) ){
            partsLeft -= 1;
            continue;
         }
         final int armor = Math.min(loadoutPart.getInternalPart().getArmorMax(), aArmorLeft / partsLeft);
         aArmorLeft -= armor;
         partsLeft -= 1;
         setArmor(loadoutPart, armor);
      }
      return aArmorLeft;
   }

   private int distributeArmorLeftOvers(Loadout aLoadout, int aArmorLeft){
      List<Part> parts = getInterestingParts(aLoadout);
      Iterator<Part> iterator = parts.iterator();
      while( aArmorLeft > 0 && !parts.isEmpty() ){
         while( iterator.hasNext() ){
            Part next = iterator.next();
            LoadoutPart loadoutPart = aLoadout.getPart(next);
            if( loadoutPart.getInternalPart().getArmorMax() == getArmor(loadoutPart) )
               iterator.remove();
         }
         int partsLeft = parts.size();
         for(Part part : parts){
            final LoadoutPart loadoutPart = aLoadout.getPart(part);
            // TODO add test for when armor divided by parts is less than 1
            final int additionalArmor = Math.min(loadoutPart.getInternalPart().getArmorMax() - getArmor(loadoutPart), (aArmorLeft + partsLeft - 1)
                                                                                                                      / partsLeft);
            if( additionalArmor <= 0 )
               continue;
            aArmorLeft -= additionalArmor;
            partsLeft -= 1;
            setArmor(loadoutPart, getArmor(loadoutPart) + additionalArmor);
         }
      }
      return aArmorLeft;
   }

   private int getArmor(LoadoutPart aPart){
      Integer stored = armors.get(aPart.getInternalPart().getType());
      if( stored != null )
         return stored;
      return 0;
   }

   private void setArmor(LoadoutPart aPart, int armor){
      armors.put(aPart.getInternalPart().getType(), armor);
   }

   private void applyArmors(Loadout aLoadout, double aFrontRearRatio, MessageXBar aXBar){
      for(Part part : Part.values()){
         final LoadoutPart loadoutPart = aLoadout.getPart(part);

         if( !loadoutPart.allowAutomaticArmor() )
            continue;
         if( loadoutPart.getInternalPart().getType().isTwoSided() ){
            addOp(new SetArmorOperation(aXBar, loadoutPart, ArmorSide.BACK, 0, false));
            addOp(new SetArmorOperation(aXBar, loadoutPart, ArmorSide.FRONT, 0, false));
         }
         else{
            addOp(new SetArmorOperation(aXBar, loadoutPart, ArmorSide.ONLY, 0, false));
         }
      }
      
      for(Part part : Part.values()){
         final LoadoutPart loadoutPart = aLoadout.getPart(part);

         if( !loadoutPart.allowAutomaticArmor() )
            continue;

         int armor = getArmor(loadoutPart);
         if( loadoutPart.getInternalPart().getType().isTwoSided() ){
            // 1) front + back = max
            // 2) front / back = ratio
            // front = back * ratio
            // front = max - back
            // = > back * ratio = max - back
            int back = (int)(armor / (aFrontRearRatio + 1));
            int front = armor - back;

            addOp(new SetArmorOperation(aXBar, loadoutPart, ArmorSide.FRONT, front, false));
            addOp(new SetArmorOperation(aXBar, loadoutPart, ArmorSide.BACK, back, false));
         }
         else{
            addOp(new SetArmorOperation(aXBar, loadoutPart, ArmorSide.ONLY, armor, false));
         }
      }
   }
}
