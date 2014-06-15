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
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.metrics.helpers.IntegratedImpulseTrain;
import lisong_mechlab.model.metrics.helpers.IntegratedPulseTrain;
import lisong_mechlab.model.metrics.helpers.IntegratedSignal;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;

/**
 * This class calculates the accurate heat generation over time for a {@link LoadoutStandard} assuming all guns fire as
 * often as possible with engine at max speed and without jump jets.
 * 
 * @author Li Song
 */
public class HeatOverTime implements TimeMetric, MessageXBar.Reader{

   private final LoadoutBase<?>      loadout;
   private final List<IntegratedSignal> heatIntegrals = new ArrayList<>();

   /**
    * Creates a new calculator object
    * 
    * @param aLoadout
    * @param aXBar
    */
   public HeatOverTime(LoadoutBase<?> aLoadout, MessageXBar aXBar){
      loadout = aLoadout;
      updateEvents();
      aXBar.attach(this);
   }

   @Override
   public double calculate(double aTime){
      double ans = 0;
      for(IntegratedSignal event : heatIntegrals){
         ans += event.integrateFromZeroTo(aTime);
      }
      return ans;
   }

   @Override
   public void receive(Message aMsg){
      if( aMsg.isForMe(loadout) && aMsg.affectsHeatOrDamage() ){
         updateEvents();
      }
   }

   private void updateEvents(){
      heatIntegrals.clear();
      for(Item item : loadout.getAllItems()){
         if( item instanceof Weapon ){
            Weapon weapon = (Weapon)item;

            if( weapon instanceof EnergyWeapon ){
               EnergyWeapon energyWeapon = (EnergyWeapon)weapon;
               if( energyWeapon.getDuration() > 0 ){
                  heatIntegrals.add(new IntegratedPulseTrain(energyWeapon.getSecondsPerShot(loadout.getEfficiencies()), energyWeapon.getDuration(),
                                                             energyWeapon.getHeat(loadout.getWeaponModifiers()) / energyWeapon.getDuration()));
                  continue;
               }
            }
            heatIntegrals.add(new IntegratedImpulseTrain(weapon.getSecondsPerShot(loadout.getEfficiencies()), weapon.getHeat(loadout.getWeaponModifiers())));
         }
         if( item instanceof Engine ){
            heatIntegrals.add(new IntegratedPulseTrain(10, 10, ((Engine)item).getHeat(loadout.getWeaponModifiers())));
         }
      }
   }
}
