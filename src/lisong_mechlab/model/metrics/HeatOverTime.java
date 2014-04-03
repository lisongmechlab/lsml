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
package lisong_mechlab.model.metrics;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.item.EnergyWeapon;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;

/**
 * This class calculates the accurate heat generation over time for a {@link Loadout} assuming all guns fire as often as
 * possible with engine at max speed and without jump jets.
 * 
 * @author Li Song
 */
public class HeatOverTime implements TimeMetric, MessageXBar.Reader{

   private abstract class HeatEvent{
      abstract double heatUntil(double aTime);
   }

   private class PeriodicHeatImpulse extends HeatEvent{
      private final double period;
      private final double impulse;

      PeriodicHeatImpulse(double aPeriod, double aImpulse){
         period = aPeriod;
         impulse = aImpulse;
      }

      @Override
      double heatUntil(double aTime){
         return (int)(aTime / period + 1) * impulse;
      }

   }

   private class PeriodicContinuousHeat extends HeatEvent{
      private final double period;
      private final double duration;
      private final double effect;

      PeriodicContinuousHeat(double aPeriod, double aDuration, double aEftect){
         period = aPeriod;
         duration = aDuration;
         effect = aEftect;
      }

      @Override
      double heatUntil(double aTime){
         int periods = (int)(aTime / period);
         double sum = effect == 0.0 ? 0 : periods * duration * effect; // Whole periods this far
         double partialTime = Math.min(aTime - periods * period, duration);
         return sum + partialTime * effect;
      }
   }

   private final Loadout         loadout;
   private final List<HeatEvent> heatEvents = new ArrayList<>();

   /**
    * Creates a new calculator object
    * 
    * @param aLoadout
    * @param aXBar 
    */
   public HeatOverTime(Loadout aLoadout, MessageXBar aXBar){
      loadout = aLoadout;
      updateEvents();
      aXBar.attach(this);
   }

   @Override
   public double calculate(double aTime){
      double ans = 0;
      for(HeatEvent event : heatEvents){
         ans += event.heatUntil(aTime);
      }
      return ans;
   }

   @Override
   public void receive(Message aMsg){
      if(aMsg.isForMe(loadout)){
         if(aMsg instanceof LoadoutPart.Message){
            LoadoutPart.Message m = (LoadoutPart.Message)aMsg;
            if(m.type != LoadoutPart.Message.Type.ArmorChanged){
               updateEvents();
            }
         }
         else{
            updateEvents();
         }
      }
   }

   private void updateEvents(){
      heatEvents.clear();
      for(Item item : loadout.getAllItems()){
         if( item instanceof Weapon ){
            Weapon weapon = (Weapon)item;
            
            if( weapon instanceof EnergyWeapon ){
               EnergyWeapon energyWeapon = (EnergyWeapon)weapon;
               if( energyWeapon.getDuration() > 0 ){
                  heatEvents.add(new PeriodicContinuousHeat(energyWeapon.getSecondsPerShot(loadout.getEfficiencies()), energyWeapon.getDuration(),
                                                            energyWeapon.getHeat() / energyWeapon.getDuration()));
                  continue;
               }
            }            
            heatEvents.add(new PeriodicHeatImpulse(weapon.getSecondsPerShot(loadout.getEfficiencies()), weapon.getHeat()));
         }if( item instanceof Engine){
            heatEvents.add(new PeriodicContinuousHeat(10, 10, ((Engine)item).getHeat()));
         }
      }
   }
}
