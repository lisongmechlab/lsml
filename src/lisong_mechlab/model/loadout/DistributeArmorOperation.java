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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.loadout.part.SetArmorOperation;
import lisong_mechlab.model.upgrades.ArmorUpgrade;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.CompositeOperation;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * This operation will distribute a number of points of armor (rounded down to the closest half ton) on a loadout,
 * respecting manually set values.
 * 
 * @author Li Song
 */
public class DistributeArmorOperation extends CompositeOperation{
   private final Map<Part, Integer> armors = new TreeMap<>();
   private final Loadout            loadout;

   /**
    * @param aLoadout
    * @param aPointsOfArmor
    * @param aFrontRearRatio
    * @param aXBar
    */
   public DistributeArmorOperation(Loadout aLoadout, int aPointsOfArmor, double aFrontRearRatio, MessageXBar aXBar){
      super("distribute armor");
      
      loadout = aLoadout;

      int armorLeft = calculateArmorToDistribute(aLoadout, aPointsOfArmor);
      if( armorLeft < 1 ){
         return;
      }
      Map<Part, Integer> prioMap = prioritize(aLoadout);

      distribute(aLoadout, armorLeft, prioMap);
      applyArmors(aLoadout, aFrontRearRatio, aXBar);
   }

   /**
    * @see lisong_mechlab.util.OperationStack.Operation#canCoalescele(lisong_mechlab.util.OperationStack.Operation)
    */
   @Override
   public boolean canCoalescele(Operation aOperation){
      if(this == aOperation)
         return false;
      if(aOperation == null)
         return false;
      if(!(aOperation instanceof DistributeArmorOperation))
         return false;
      DistributeArmorOperation operation = (DistributeArmorOperation)aOperation;
      return loadout == operation.loadout;
   }

   private void distribute(final Loadout aLoadout, int aArmorAmount, final Map<Part, Integer> aPriorities){
      int prioSum = 0;
      for(double prio : aPriorities.values()){
         prioSum += prio;
      }

      TreeMap<Part, Integer> byPriority = new TreeMap<>(new Comparator<Part>(){
         @Override
         public int compare(Part aO1, Part aO2){
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
      for(Entry<Part, Integer> entry : byPriority.entrySet()){
         Part part = entry.getKey();
         int prio = entry.getValue();
         if( prio == 0 )
            continue;

         LoadoutPart loadoutPart = aLoadout.getPart(part);
         int armor = Math.min(loadoutPart.getInternalPart().getArmorMax(), armorLeft * prio / prioSum);
         setArmor(loadoutPart, armor);
         armorLeft -= armor;
         prioSum -= prio;
      }

      List<LoadoutPart> parts = new ArrayList<>(aLoadout.getPartLoadOuts());
      while( armorLeft > 0 && !parts.isEmpty() ){
         Iterator<LoadoutPart> it = parts.iterator();
         while( it.hasNext() ){
            LoadoutPart part = it.next();
            if( !part.allowAutomaticArmor() || getArmor(part) == part.getInternalPart().getArmorMax() )
               it.remove();
         }

         int partsLeft = parts.size();
         for(LoadoutPart loadoutPart : parts){
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
      return armorLeft;
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

   private Map<Part, Integer> prioritize(Loadout aLoadout){
      Map<Part, Integer> ans = new TreeMap<>();

      for(Part part : Part.values()){
         LoadoutPart loadoutPart = aLoadout.getPart(part);
         if( !loadoutPart.allowAutomaticArmor() )
            continue;

         // Protect engine at all costs
         if( part == Part.CenterTorso ){
            ans.put(part, 2000);
         }
         else if( loadoutPart.getItems().contains(LoadoutPart.ENGINE_INTERNAL) ){
            ans.put(part, 1000);
         }
         // Legs and head are high priority too
         else if( part == Part.LeftLeg || part == Part.RightLeg ){
            ans.put(part, 10);
         }
         else if( part == Part.Head ){
            ans.put(part, 7);
         }
         else if( loadoutPart.getItemMass() == 0.0 && !ans.containsKey(part) ){
            ans.put(part, 0);
         }
         else{
            if( part == Part.LeftArm ){
               ans.put(Part.LeftArm, 10);
               if( !ans.containsKey(Part.LeftTorso) || ans.get(Part.LeftTorso) < 10 )
                  ans.put(Part.LeftTorso, 10);
            }
            else if( part == Part.RightArm ){
               ans.put(Part.RightArm, 10);
               if( !ans.containsKey(Part.RightTorso) || ans.get(Part.RightTorso) < 10 )
                  ans.put(Part.RightTorso, 10);
            }
            else{
               ans.put(part, 10);
            }
         }
      }
      return ans;
   }
}
