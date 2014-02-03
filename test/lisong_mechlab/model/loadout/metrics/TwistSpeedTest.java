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
package lisong_mechlab.model.loadout.metrics;

import static org.junit.Assert.assertEquals;
import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.loadout.Loadout;

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
      Loadout loadout = Mockito.mock(Loadout.class);
      Chassi chassi = Mockito.mock(Chassi.class);
      Mockito.when(loadout.getChassi()).thenReturn(chassi);
      Mockito.when(loadout.getEngine()).thenReturn(null);
      double factor = 0.2;
      int mass = 50;
      Mockito.when(chassi.getTurnFactor()).thenReturn(factor);
      Mockito.when(chassi.getMassMax()).thenReturn(mass);

      TwistSpeed cut = new TwistSpeed(loadout);
      assertEquals(0, cut.calculate(), 0.0);
   }

   @Test
   public final void testCalculate() throws Exception{
      Loadout loadout = Mockito.mock(Loadout.class);
      Chassi chassi = Mockito.mock(Chassi.class);
      Engine engine = Mockito.mock(Engine.class);
      Mockito.when(loadout.getChassi()).thenReturn(chassi);
      Mockito.when(loadout.getEngine()).thenReturn(engine);
      double factor = 0.2;
      int rating = 300;
      int mass = 50;
      Mockito.when(chassi.getTwistFactor()).thenReturn(factor);
      Mockito.when(chassi.getMassMax()).thenReturn(mass);
      Mockito.when(engine.getRating()).thenReturn(rating);

      TwistSpeed cut = new TwistSpeed(loadout);
      assertEquals(factor * rating / mass, cut.calculate(), 0.0);
   }

}
