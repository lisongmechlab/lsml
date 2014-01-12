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
package lisong_mechlab.model.loadout.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import lisong_mechlab.model.helpers.MockLoadoutContainer;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.ItemDB;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TopSpeedTest{
   MockLoadoutContainer mlc        = new MockLoadoutContainer();
   TopSpeed             statistics = new TopSpeed(mlc.loadout);

   @Test
   public void testCalculate_noengine() throws Exception{
      when(mlc.loadout.getEngine()).thenReturn(null);
      assertEquals(0, statistics.calculate(), 0.0);
   }

   @Test
   public void testCalculate() throws Exception{
      int rating = 300;
      double factor = 4;
      int tonnage = 30;
      for(double speedtweak : new double[] {1.0, 1.1}){
         when(mlc.loadout.getEngine()).thenReturn((Engine)ItemDB.lookup("STD ENGINE " + rating));
         when(mlc.chassi.getSpeedFactor()).thenReturn(factor);
         when(mlc.chassi.getMassMax()).thenReturn(tonnage);
         when(mlc.efficiencies.hasSpeedTweak()).thenReturn(speedtweak > 1.0);
         when(mlc.efficiencies.getSpeedModifier()).thenReturn(speedtweak);

         double expected = rating * factor / tonnage * speedtweak;
         assertEquals(expected, statistics.calculate(), 0.0);
      }
   }
}
