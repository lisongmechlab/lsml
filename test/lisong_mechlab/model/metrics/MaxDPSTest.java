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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link MaxDPS} {@link Metric}.
 * 
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.class)
public class MaxDPSTest{
   @Mock
   private Loadout    loadout;
   @InjectMocks
   private MaxDPS     cut;
   private List<Item> items = new ArrayList<>();

   @Before
   public void setup(){
      when(loadout.getAllItems()).thenReturn(items);
   }

   /**
    * AMS is not counted into DPS.
    */
   @Test
   public void testCalculate_AMS(){
      items.add(ItemDB.AMS);
      assertEquals(0.0, cut.calculate(0), 0.0);
   }

   /**
    * Non-Weapon types do not cause exceptions or affect the result.
    */
   @Test
   public void testCalculate_otherItems(){
      items.add(ItemDB.ECM);
      items.add(ItemDB.BAP);
      assertEquals(0.0, cut.calculate(0), 0.0);
   }

   /**
    * {@link MaxDPS#calculate(double)} shall calculate the maximal DPS at a given range.
    */
   @Test
   public void testCalculate(){
      Weapon ac5 = (Weapon)ItemDB.lookup("AC/5");
      Weapon lrm20 = (Weapon)ItemDB.lookup("LRM20");
      Weapon slas = (Weapon)ItemDB.lookup("SMALL LASER");
      items.add(ac5);
      items.add(lrm20);
      items.add(slas);

      double dps_ac5 = ac5.getStat("d/s", null, null);
      double dps_lrm20 = lrm20.getStat("d/s", null, null);
      double dps_slas = slas.getStat("d/s", null, null);

      assertEquals(dps_ac5 + dps_slas, cut.calculate(0), 0.0);
      assertEquals(dps_ac5 + dps_slas, cut.calculate(90), 0.0);
      assertEquals(dps_ac5 + dps_lrm20, cut.calculate(200), 0.0);
   }

}
