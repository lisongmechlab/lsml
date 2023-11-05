/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.command;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.ItemDB;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.util.TestHelpers;
import org.lisoft.mwo_data.equipment.Ammunition;
import org.lisoft.mwo_data.equipment.Item;

public class CmdFillWithItemTest {
  private final LoadoutFactory loadoutFactory = new DefaultLoadoutFactory();

  @Test
  public void testApply_AmmoHalfTonLimited() throws Exception {
    // Fill with ammo is a complex operation, mocking a whole loadout for this is too much work.
    // Hence we choose to use a concrete instance even though it is not best practice.
    // This loadout has 36 free slots and 4.5 free tons.
    final Loadout l = TestHelpers.parse("A>202000|obp00|^?q00|^?r00|^?s00|^?t00u00v00w000000");

    final Ammunition ammo = (Ammunition) ItemDB.lookup("AMS AMMO");
    final Ammunition ammoHalf = (Ammunition) ItemDB.lookup("AMS AMMO (1/2)");
    final MessageDelivery delivery = mock(MessageDelivery.class);

    final CmdFillWithItem cut = new CmdFillWithItem(delivery, l, ammo, ammoHalf, loadoutFactory);
    cut.apply();

    // Verify
    int foundAmmo = 0;
    int foundHalf = 0;
    for (final Ammunition x : l.items(Ammunition.class)) {
      if (x == ammo) {
        foundAmmo++;
      } else if (x == ammoHalf) {
        foundHalf++;
      }
    }
    assertEquals(4, foundAmmo);
    assertEquals(1, foundHalf);
    assertEquals(0.0, l.getFreeMass(), 0.0);
  }

  @Test
  public void testApply_AmmoSlotLimited() throws Exception {
    // Fill with ammo is a complex operation, mocking a whole loadout for this is too much work.
    // Hence we choose to use a concrete instance even though it is not best practice.
    // This loadout has 8 free slots and 62 free tons.
    final Loadout l = TestHelpers.parse("A>292000|ncp00q00r00s00|i^|i^t00u00v00w000000");

    final Ammunition ammo = (Ammunition) ItemDB.lookup("AMS AMMO");
    final MessageDelivery delivery = mock(MessageDelivery.class);

    final CmdFillWithItem cut = new CmdFillWithItem(delivery, l, ammo, loadoutFactory);
    cut.apply();

    int foundAmmo = 0;
    assertEquals(0, l.getFreeSlots());

    for (final Ammunition x : l.items(Ammunition.class)) {
      if (x == ammo) {
        foundAmmo++;
      }
    }
    assertEquals(8, foundAmmo);
  }

  @Test
  public void testApply_DHS() throws Exception {
    // Fill with ammo is a complex operation, mocking a whole loadout for this is too much work.
    // Hence we choose to use a concrete instance even though it is not best practice.
    // This loadout has 14 free slots, 2 engine slots and 49 free tons.
    final Loadout l = TestHelpers.parse("A>292000|Nd|i^|i^p00q00r00s00t00u00v00w000000");

    final Item dhs = ItemDB.DHS;
    final MessageDelivery delivery = mock(MessageDelivery.class);

    final CmdFillWithItem cut = new CmdFillWithItem(delivery, l, dhs, loadoutFactory);
    cut.apply();

    int found = 0;
    assertEquals(2, l.getFreeSlots());

    for (final Item x : l.items(Item.class)) {
      if (x == dhs) {
        found++;
      }
    }
    assertEquals(6, found);
  }
}
