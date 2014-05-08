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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.loadout.part.ConfiguredComponent;
import lisong_mechlab.model.loadout.part.OpSetArmor;
import lisong_mechlab.model.upgrades.ArmorUpgrade;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.CompositeOperation;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * This operation will distribute a number of points of armor (rounded down to the closest half ton) on a loadout,
 * respecting manually set values.
 * 
 * @author Emily Björk
 */
public class OpDistributeArmor extends CompositeOperation{
   private final Map<Location, Integer> armors = new HashMap<>(Location.values().length);
   private final Loadout            loadout;

   /**
    * @param aLoadout
    * @param aPointsOfArmor
    * @param aFrontRearRatio
    * @param aXBar
    */
   public OpDistributeArmor(Loadout aLoadout, int aPointsOfArmor, double aFrontRearRatio, MessageXBar aXBar){
      super("distribute armor");

      loadout = aLoadout;

      int armorLeft = calculateArmorToDistribute(aLoadout, aPointsOfArmor);
      if( armorLeft > 0 ){
         Map<Location, Integer> prioMap = prioritize(aLoadout);
         distribute(aLoadout, armorLeft, prioMap);
      }
      applyArmors(aLoadout, aFrontRearRatio, aXBar);
   }

   /**
    * @see lisong_mechlab.util.OperationStack.Operation#canCoalescele(lisong_mechlab.util.OperationStack.Operation)
    */
   @Override
   public boolean canCoalescele(Operation aOperation){
      if( this == aOperation )
         return false;
      if( aOperation == null )
         return false;
      if( !(aOperation instanceof OpDistributeArmor) )
         return false;
      OpDistributeArmor operation = (OpDistributeArmor)aOperation;
      return loadout == operation.loadout;
   }

   private void distribute(final Loadout aLoadout, int aArmorAmount, final Map<Location, Integer> aPriorities){
      int prioSum = 0;
      for(double prio : aPriorities.values()){
         prioSum += prio;
      }

      TreeMap<Location, Integer> byPriority = new TreeMap<>(new Comparator<Location>(){
         @Override
         public int compare(Location aO1, Location aO2){
            int c = -aPriorities.get(aO1).compareTo(aPriorities.get(aO2));
            if( c == 0 ){
               int d = Integer.compare(aLoadout.getChassi().getInternalPart(aO1).getArmorMax(), aLoadout.getChassi().getInternalPart(aO2)
                                                                                                        .getArmorMax());
               if( d == 0 )
                  return aO1.compareTo(aO2);
               return d;
            }
            return c;
         }
      });
      byPriority.putAll(aPriorities);

      int armorLeft = aArmorAmount;
      for(Entry<Location, Integer> entry : byPriority.entrySet()){
         Location part = entry.getKey();
         int prio = entry.getValue();
         if( prio == 0 )
            continue;

         ConfiguredComponent loadoutPart = aLoadout.getPart(part);
         int armor = Math.min(loadoutPart.getInternalPart().getArmorMax(), armorLeft * prio / prioSum);
         setArmor(loadoutPart, armor);
         armorLeft -= armor;
         prioSum -= prio;
      }

      List<ConfiguredComponent> parts = new ArrayList<>(aLoadout.getPartLoadOuts());
      while( armorLeft > 0 && !parts.isEmpty() ){
         Iterator<ConfiguredComponent> it = parts.iterator();
         while( it.hasNext() ){
            ConfiguredComponent part = it.next();
            if( !part.allowAutomaticArmor() || getArmor(part) == part.getInternalPart().getArmorMax() )
               it.remove();
         }

         int partsLeft = parts.size();
         for(ConfiguredComponent loadoutPart : parts){
            int additionalArmor = Math.min(loadoutPart.getInternalPart().getArmorMax() - getArmor(loadoutPart), armorLeft / partsLeft);
            setArmor(loadoutPart, getArmor(loadoutPart) + additionalArmor);
            armorLeft -= additionalArmor;
            partsLeft--;
         }
      }
   }

   private int calculateArmorToDistribute(Loadout aLoadout, int aPointsOfArmor){
      final ArmorUpgrade armorUpgrade = aLoadout.getUpgrades().getArmor();
      final double armorPerTon = armorUpgrade.getArmorPerTon();
      final double armorTons = aPointsOfArmor / armorPerTon;
      final int armorHalfTons = (int)(armorTons * 2.0);
      int armorLeft = (int)(armorPerTon * armorHalfTons / 2.0);

      // We can't apply more armor than we can carry
      int maxArmorTonnage = (int)((aLoadout.getFreeMass() + armorUpgrade.getArmorMass(aLoadout.getArmor())) * armorPerTon);
      armorLeft = Math.min(maxArmorTonnage, armorLeft);

      int maxArmorPoints = 0;

      // Discount armor that is manually fixed.
      for(Location part : Location.values()){
         final ConfiguredComponent loadoutPart = aLoadout.getPart(part);
         if( !loadoutPart.allowAutomaticArmor() ){
            armorLeft -= loadoutPart.getArmorTotal();
         }
         else{
            maxArmorPoints += loadoutPart.getInternalPart().getArmorMax();
         }
      }
      armorLeft = Math.min(maxArmorPoints, armorLeft);
      return armorLeft;
   }

   private int getArmor(ConfiguredComponent aPart){
      Integer stored = armors.get(aPart.getInternalPart().getLocation());
      if( stored != null )
         return stored;
      return 0;
   }

   private void setArmor(ConfiguredComponent aPart, int armor){
      armors.put(aPart.getInternalPart().getLocation(), armor);
   }

   private void applyArmors(Loadout aLoadout, double aFrontRearRatio, MessageXBar aXBar){
      for(Location part : Location.values()){
         final ConfiguredComponent loadoutPart = aLoadout.getPart(part);

         if( !loadoutPart.allowAutomaticArmor() )
            continue;
         if( loadoutPart.getInternalPart().getLocation().isTwoSided() ){
            addOp(new OpSetArmor(aXBar, loadoutPart, ArmorSide.BACK, 0, false));
            addOp(new OpSetArmor(aXBar, loadoutPart, ArmorSide.FRONT, 0, false));
         }
         else{
            addOp(new OpSetArmor(aXBar, loadoutPart, ArmorSide.ONLY, 0, false));
         }
      }

      for(Location part : Location.values()){
         final ConfiguredComponent loadoutPart = aLoadout.getPart(part);

         if( !loadoutPart.allowAutomaticArmor() )
            continue;

         int armor = getArmor(loadoutPart);
         if( loadoutPart.getInternalPart().getLocation().isTwoSided() ){
            // 1) front + back = max
            // 2) front / back = ratio
            // front = back * ratio
            // front = max - back
            // = > back * ratio = max - back
            int back = (int)(armor / (aFrontRearRatio + 1));
            int front = armor - back;

            addOp(new OpSetArmor(aXBar, loadoutPart, ArmorSide.FRONT, front, false));
            addOp(new OpSetArmor(aXBar, loadoutPart, ArmorSide.BACK, back, false));
         }
         else{
            addOp(new OpSetArmor(aXBar, loadoutPart, ArmorSide.ONLY, armor, false));
         }
      }
   }

   private Map<Location, Integer> prioritize(Loadout aLoadout){
      Map<Location, Integer> ans = new HashMap<>(Location.values().length);

      for(Location part : Location.values()){
         ConfiguredComponent loadoutPart = aLoadout.getPart(part);
         if( !loadoutPart.allowAutomaticArmor() )
            continue;

         // Protect engine at all costs
         if( part == Location.CenterTorso ){
            ans.put(part, 2000);
         }
         else if( loadoutPart.getItems().contains(ConfiguredComponent.ENGINE_INTERNAL) ){
            ans.put(part, 1000);
         }
         // Legs and head are high priority too
         else if( part == Location.LeftLeg || part == Location.RightLeg ){
            ans.put(part, 10);
         }
         else if( part == Location.Head ){
            ans.put(part, 7);
         }
         else if( loadoutPart.getItemMass() == 0.0 && !ans.containsKey(part) ){
            ans.put(part, 0);
         }
         else{
            if( part == Location.LeftArm ){
               ans.put(Location.LeftArm, 10);
               if( aLoadout.getPart(Location.LeftTorso).allowAutomaticArmor() && (!ans.containsKey(Location.LeftTorso) || ans.get(Location.LeftTorso) < 10) )
                  ans.put(Location.LeftTorso, 10);
            }
            else if( part == Location.RightArm ){
               ans.put(Location.RightArm, 10);
               if( aLoadout.getPart(Location.RightTorso).allowAutomaticArmor() && (!ans.containsKey(Location.RightTorso) || ans.get(Location.RightTorso) < 10) )
                  ans.put(Location.RightTorso, 10);
            }
            else{
               ans.put(part, 10);
            }
         }
      }
      return ans;
   }
}
