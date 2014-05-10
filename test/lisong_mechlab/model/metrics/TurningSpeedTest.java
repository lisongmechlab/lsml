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
import lisong_mechlab.model.Efficiencies;
import lisong_mechlab.model.chassi.Chassis;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.loadout.Loadout;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * A test suite for {@link TurningSpeed}.
 * 
 * @author Li Song
 */
public class TurningSpeedTest{

   @Test
   public final void testCalculate_NoEngine() throws Exception{
      Loadout loadout = Mockito.mock(Loadout.class);
      Chassis chassi = Mockito.mock(Chassis.class);
      Mockito.when(loadout.getChassi()).thenReturn(chassi);
      Mockito.when(loadout.getEngine()).thenReturn(null);
      double factor = 0.2;
      int mass = 50;
      Mockito.when(chassi.getTurnFactor()).thenReturn(factor);
      Mockito.when(chassi.getMassMax()).thenReturn(mass);

      TurningSpeed cut = new TurningSpeed(loadout);
      assertEquals(0, cut.calculate(), 0.0);
   }

   @Test
   public final void testCalculate() throws Exception{
      Loadout loadout = Mockito.mock(Loadout.class);
      Chassis chassi = Mockito.mock(Chassis.class);
      Engine engine = Mockito.mock(Engine.class);
      Efficiencies efficiencies = Mockito.mock(Efficiencies.class);
      double modifier = 1.1;
      Mockito.when(efficiencies.getTurnSpeedModifier()).thenReturn(modifier);
      Mockito.when(loadout.getEfficiencies()).thenReturn(efficiencies);
      Mockito.when(loadout.getChassi()).thenReturn(chassi);
      Mockito.when(loadout.getEngine()).thenReturn(engine);
      double factor = 0.2;
      int rating = 300;
      int mass = 50;
      Mockito.when(chassi.getTurnFactor()).thenReturn(factor);
      Mockito.when(chassi.getMassMax()).thenReturn(mass);
      Mockito.when(engine.getRating()).thenReturn(rating);

      TurningSpeed cut = new TurningSpeed(loadout);
      assertEquals(modifier * factor * rating / mass, cut.calculate(), 0.0);
   }

}
