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

import lisong_mechlab.model.pilot.PilotSkillTree;
import lisong_mechlab.mwo_data.helpers.ItemStatsModule;

/**
 * FIXME Needs to be fixed after second clan patch
 * 
 * @author Emily Björk
 */
public class TargetingComputer extends Module implements WeaponModifier{

   public TargetingComputer(ItemStatsModule aModule){
      super(aModule);
   }

   @Override
   public boolean affectsWeapon(Weapon aWeapon){
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public double extraMaxRange(Weapon aWeapon, double aRange, PilotSkillTree aPilotSkillTree){
      // TODO Auto-generated method stub
      return 0;
   }

   @Override
   public double extraLongRange(Weapon aWeapon, double aRange, PilotSkillTree aPilotSkillTree){
      // TODO Auto-generated method stub
      return 0;
   }

   @Override
   public double extraHeat(Weapon aWeapon, double aHeat, PilotSkillTree aPilotSkillTree){
      // TODO Auto-generated method stub
      return 0;
   }

   @Override
   public double extraCooldown(Weapon aWeapon, double aCooldown, PilotSkillTree aPilotSkillTree){
      // TODO Auto-generated method stub
      return 0;
   }

}
