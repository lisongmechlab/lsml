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
import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.upgrades.Upgrades;

import org.junit.Test;
import org.mockito.Mockito;

public class PayloadStatisticsTest{

   @Test
   public final void testChangeUseXLEngine() throws Exception{
      Chassi jm6_a = ChassiDB.lookup("JM6-A");
      Upgrades upgrades = Mockito.mock(Upgrades.class);
      PayloadStatistics cut = new PayloadStatistics(false, false, upgrades);

      cut.changeUseXLEngine(true);

      assertEquals(46.0, cut.calculate(jm6_a, 250), 0.0);
   }

   @Test
   public final void testChangeUseMaxArmor() throws Exception{
      Chassi jm6_a = ChassiDB.lookup("JM6-A");
      Upgrades upgrades = Mockito.mock(Upgrades.class);
      PayloadStatistics cut = new PayloadStatistics(false, false, upgrades);

      cut.changeUseMaxArmor(true);

      assertEquals(26.81, cut.calculate(jm6_a, 250), 0.01);
   }

   @Test
   public final void testChangeUpgrades() throws Exception{
      Chassi jm6_a = ChassiDB.lookup("JM6-A");
      Upgrades upgrades = Mockito.mock(Upgrades.class);
      PayloadStatistics cut = new PayloadStatistics(false, false, upgrades);
      Upgrades upgradesNew = Mockito.mock(Upgrades.class);
      Mockito.when(upgradesNew.hasEndoSteel()).thenReturn(true);

      cut.changeUpgrades(upgradesNew);

      assertEquals(43.0, cut.calculate(jm6_a, 250), 0.0);
   }

   @Test
   public final void testCalculate_smallEngine() throws Exception{
      Chassi jm6_a = ChassiDB.lookup("JM6-A");
      Upgrades upgrades = Mockito.mock(Upgrades.class);

      PayloadStatistics cut = new PayloadStatistics(false, false, upgrades);
      assertEquals(45.0, cut.calculate(jm6_a, 200), 0.0); // Needs two additional heat sinks
      assertEquals(44.0, cut.calculate(jm6_a, 205), 0.0); // Needs two additional heat sinks
      assertEquals(42.5, cut.calculate(jm6_a, 220), 0.0); // Needs two additional heat sinks
   }

   @Test
   public final void testCalculate() throws Exception{
      Chassi jm6_a = ChassiDB.lookup("JM6-A");
      Upgrades upgrades = Mockito.mock(Upgrades.class);

      PayloadStatistics cut = new PayloadStatistics(false, false, upgrades);
      assertEquals(40.0, cut.calculate(jm6_a, 250), 0.0);
      assertEquals(33.5, cut.calculate(jm6_a, 300), 0.0);
   }

   @Test
   public final void testCalculate_xl() throws Exception{
      Chassi jm6_a = ChassiDB.lookup("JM6-A");
      Upgrades upgrades = Mockito.mock(Upgrades.class);

      PayloadStatistics cut = new PayloadStatistics(true, false, upgrades);
      assertEquals(46.0, cut.calculate(jm6_a, 250), 0.0);
      assertEquals(43.0, cut.calculate(jm6_a, 300), 0.0);
   }

   @Test
   public final void testCalculate_maxArmor() throws Exception{
      Chassi jm6_a = ChassiDB.lookup("JM6-A");
      Upgrades upgrades = Mockito.mock(Upgrades.class);

      PayloadStatistics cut = new PayloadStatistics(false, true, upgrades);
      assertEquals(26.81, cut.calculate(jm6_a, 250), 0.01);
      assertEquals(20.31, cut.calculate(jm6_a, 300), 0.01);
   }

   @Test
   public final void testCalculate_ferroMaxArmor() throws Exception{
      Chassi jm6_a = ChassiDB.lookup("JM6-A");
      Upgrades upgrades = Mockito.mock(Upgrades.class);
      Mockito.when(upgrades.hasFerroFibrous()).thenReturn(true);

      PayloadStatistics cut = new PayloadStatistics(false, true, upgrades);
      assertEquals(28.23, cut.calculate(jm6_a, 250), 0.01);
      assertEquals(21.73, cut.calculate(jm6_a, 300), 0.01);
   }

   @Test
   public final void testCalculate_endo() throws Exception{
      Chassi jm6_a = ChassiDB.lookup("JM6-A");
      Upgrades upgrades = Mockito.mock(Upgrades.class);
      Mockito.when(upgrades.hasEndoSteel()).thenReturn(true);

      PayloadStatistics cut = new PayloadStatistics(false, false, upgrades);
      assertEquals(43.0, cut.calculate(jm6_a, 250), 0.0);
      assertEquals(36.5, cut.calculate(jm6_a, 300), 0.0);
   }
}
