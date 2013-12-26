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
package lisong_mechlab.model.item;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.Upgrades;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsModule;

public class JumpJet extends Module{
   private final double minTons;
   private final double maxTons;
   private final double boost_z;
   private final double duration;
   private final double heat;

   public JumpJet(ItemStatsModule aModule){
      super(aModule);
      minTons = aModule.JumpJetStats.minTons;
      maxTons = aModule.JumpJetStats.maxTons;
      boost_z = aModule.JumpJetStats.boost;
      duration = aModule.JumpJetStats.duration;
      heat = aModule.JumpJetStats.heat;
   }

   @Override
   public boolean isEquippableOn(Loadout aLoadout){
      return aLoadout.getChassi().getMaxJumpJets() > 0 && aLoadout.getChassi().getMassMax() < maxTons && aLoadout.getChassi().getMassMax() >= minTons;
   }

   public double getForce(){
      return boost_z;
   }

   public double getDuration(){
      return duration;
   }

   public double getJumpHeat(){
      return heat;
   }

   @Override
   public String getShortName(Upgrades anUpgrades){
      String name = getName(anUpgrades);
      name = name.replace("JUMP JETS", "JJ");
      name = name.replace("CLASS ", "");
      return name;
   }
}
