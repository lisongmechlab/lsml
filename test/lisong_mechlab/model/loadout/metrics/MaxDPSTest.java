/*
 * @formatter:off
 * Li Song Mech Lab - A 'mech building tool for PGI's MechWarrior: Online.
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

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link MaxDPS} {@link Metric}.
 * 
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.class)
public class MaxDPSTest{
   @Mock
   private Loadout loadout;
   @InjectMocks
   private MaxDPS  cut;

   /**
    * AMS is not counted into DPS
    * 
    * @throws Exception
    */
   @Test
   public void testCalculate_AMS() throws Exception{
      Weapon mg = (Weapon)ItemDB.lookup("MACHINE GUN");
      List<Item> items = new ArrayList<>();
      when(loadout.getAllItems()).thenReturn(items);
      items.add(ItemDB.AMS);
      items.add(mg);

      assertEquals(mg.getStat("d/s", null, null), cut.calculate(), 0.0);
   }

   /**
    * Default behavior without arguments is to find the range with highest DPS.
    * <p>
    * This case has the ranges completely overlap.
    * 
    * @throws Exception
    */
   @Test
   public void testCalculate_overlap() throws Exception{
      Weapon ac5 = (Weapon)ItemDB.lookup("AC/5");
      Weapon lrm20 = (Weapon)ItemDB.lookup("LRM20");
      List<Item> items = new ArrayList<>();
      when(loadout.getAllItems()).thenReturn(items);
      items.add(ItemDB.AMS);
      items.add(ac5);
      items.add(lrm20);
      items.add(ItemDB.lookup("STD ENGINE 300")); // Unrelated items shall not skew the values
      items.add(ItemDB.lookup("AMS AMMO"));

      assertEquals(ac5.getStat("d/s", null, null) + lrm20.getStat("d/s", null, null), cut.calculate(), 0.0);
      assertEquals(ac5.getRangeLong(), cut.getRange(), 0.0); // AC5 range dominates
   }

   /**
    * Default behavior without arguments is to find the range with highest DPS.
    * <p>
    * This case has the ranges not overlapping.
    * 
    * @throws Exception
    */
   @Test
   public void testCalculate_noOverlap() throws Exception{
      Weapon slas = (Weapon)ItemDB.lookup("SMALL LASER");
      Weapon lrm20 = (Weapon)ItemDB.lookup("LRM20");
      List<Item> items = new ArrayList<>();
      when(loadout.getAllItems()).thenReturn(items);
      items.add(ItemDB.AMS);
      items.add(slas);
      items.add(lrm20);
      items.add(ItemDB.lookup("STD ENGINE 300")); // Unrelated items shall not skew the values
      items.add(ItemDB.lookup("AMS AMMO"));

      assertEquals(lrm20.getStat("d/s", null, null), cut.calculate(), 0.0);
      assertEquals(lrm20.getRangeLong(), cut.getRange(), 0.0); // LRM range dominates
   }

   /**
    * Default behavior without arguments is to find the range with highest DPS.
    * <p>
    * This case has a specific range to calculate at.
    * 
    * @throws Exception
    */
   @Test
   public void testCalculate_atRange() throws Exception{
      Weapon slas = (Weapon)ItemDB.lookup("SMALL LASER");
      Weapon ac5 = (Weapon)ItemDB.lookup("AC/5");
      Weapon lrm20 = (Weapon)ItemDB.lookup("LRM20");
      List<Item> items = new ArrayList<>();
      when(loadout.getAllItems()).thenReturn(items);
      items.add(ItemDB.AMS);
      items.add(slas);
      items.add(lrm20);
      items.add(ac5);

      cut.changeRange(80);
      assertEquals(ac5.getStat("d/s", null, null) + slas.getStat("d/s", null, null), cut.calculate(), 0.0);
      assertEquals(80, cut.getRange(), 0.0);

      cut.changeRange(500);
      assertEquals(ac5.getStat("d/s", null, null) + lrm20.getStat("d/s", null, null), cut.calculate(), 0.0);
      assertEquals(500, cut.getRange(), 0.0);
   }

   /**
    * Default behavior without arguments is to find the range with highest DPS.
    * <p>
    * This case checks that the default behaviour can be reactivated after having set a specific range.
    * 
    * @throws Exception
    */
   @Test
   public void testCalculate_atRangeNoMore() throws Exception{
      Weapon slas = (Weapon)ItemDB.lookup("SMALL LASER");
      Weapon ac5 = (Weapon)ItemDB.lookup("AC/5");
      Weapon lrm20 = (Weapon)ItemDB.lookup("LRM20");
      List<Item> items = new ArrayList<>();
      when(loadout.getAllItems()).thenReturn(items);
      items.add(ItemDB.AMS);
      items.add(slas);
      items.add(lrm20);
      items.add(ac5);

      cut.changeRange(80);

      cut.changeRange(0);
      assertEquals(ac5.getStat("d/s", null, null) + lrm20.getStat("d/s", null, null), cut.calculate(), 0.0);
      assertEquals(ac5.getRangeLong(), cut.getRange(), 0.0);
   }

}
