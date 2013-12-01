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

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.item.BallisticWeapon;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.MissileWeapon;
import lisong_mechlab.model.loadout.Loadout;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MaxDPSTest{
   @Mock
   private Loadout loadout;
   @InjectMocks
   private MaxDPS  cut;

   @Test
   public void testCalculate_AMS() throws Exception{
      BallisticWeapon mg = (BallisticWeapon)ItemDB.lookup("MACHINE GUN");
      List<Item> items = new ArrayList<>();
      when(loadout.getAllItems()).thenReturn(items);
      items.add(ItemDB.AMS);
      items.add(mg);

      assertEquals(mg.getStat("d/s", null), cut.calculate(), 0.0);
   }

   @Test
   public void testCalculate() throws Exception{
      BallisticWeapon mg = (BallisticWeapon)ItemDB.lookup("MACHINE GUN");
      MissileWeapon lrm20 = (MissileWeapon)ItemDB.lookup("LRM20");
      List<Item> items = new ArrayList<>();
      when(loadout.getAllItems()).thenReturn(items);
      items.add(ItemDB.AMS);
      items.add(mg);
      items.add(lrm20);
      items.add(ItemDB.lookup("STD ENGINE 300")); // Unrelated items shall not skew the values
      items.add(ItemDB.lookup("AMS AMMO"));

      assertEquals(mg.getStat("d/s", null) + lrm20.getStat("d/s", null), cut.calculate(), 0.0);
   }

}
