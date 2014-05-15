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
package lisong_mechlab.model.chassi;

/**
 * @author Emily
 *
 */
public class ChassisClan extends ChassisBase{

   /**
    * @param aMwoID
    * @param aMwoName
    * @param aSeries
    * @param aName
    * @param aShortName
    * @param aParts
    * @param aMaxJumpJets
    * @param aMaxTons
    * @param aVariant
    * @param aBaseVariant
    * @param aMovementProfile
    */
   public ChassisClan(int aMwoID, String aMwoName, String aSeries, String aName, String aShortName, InternalComponent[] aParts, int aMaxJumpJets,
                          int aMaxTons, ChassisVariant aVariant, int aBaseVariant, MovementProfile aMovementProfile){
      super(aMwoID, aMwoName, aSeries, aName, aShortName, aParts, aMaxJumpJets, aMaxTons, aVariant, aBaseVariant, aMovementProfile);
      // TODO Auto-generated constructor stub
   }

}
