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

import static org.junit.Assert.assertEquals;
import lisong_mechlab.model.chassi.ChassisStandard;
import lisong_mechlab.model.chassi.MovementProfile;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.loadout.LoadoutStandard;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test suite for {@link TwistSpeed} {@link Metric}.
 * 
 * @author Li Song
 */
public class TwistSpeedTest{

   @Test
   public final void testCalculate_NoEngine() throws Exception{
      MovementProfile movementProfile = Mockito.mock(MovementProfile.class);
      LoadoutStandard loadout = Mockito.mock(LoadoutStandard.class);
      ChassisStandard chassi = Mockito.mock(ChassisStandard.class);
      
      Mockito.when(loadout.getChassis()).thenReturn(chassi);
      Mockito.when(loadout.getEngine()).thenReturn(null);
      Mockito.when(loadout.getMovementProfile()).thenReturn(movementProfile);
      
      double factor = 0.2;
      int mass = 50;
      Mockito.when(movementProfile.getTorsoYawSpeed()).thenReturn(factor);
      Mockito.when(chassi.getMassMax()).thenReturn(mass);

      TwistSpeed cut = new TwistSpeed(loadout);
      assertEquals(0, cut.calculate(), 0.0);
   }

   @Test
   public final void testCalculate() throws Exception{
      MovementProfile movementProfile = Mockito.mock(MovementProfile.class);
      LoadoutStandard loadout = Mockito.mock(LoadoutStandard.class);
      ChassisStandard chassi = Mockito.mock(ChassisStandard.class);
      
      Engine engine = Mockito.mock(Engine.class);
      Mockito.when(loadout.getChassis()).thenReturn(chassi);
      Mockito.when(loadout.getEngine()).thenReturn(engine);
      Mockito.when(loadout.getMovementProfile()).thenReturn(movementProfile);
      
      double factor = 0.2;
      int rating = 300;
      int mass = 50;
      Mockito.when(movementProfile.getTorsoYawSpeed()).thenReturn(factor);
      Mockito.when(chassi.getMassMax()).thenReturn(mass);
      Mockito.when(engine.getRating()).thenReturn(rating);

      TwistSpeed cut = new TwistSpeed(loadout);
      assertEquals(factor * rating / mass, cut.calculate(), 0.0);
   }

}
