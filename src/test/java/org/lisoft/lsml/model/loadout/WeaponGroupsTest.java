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
package org.lisoft.lsml.model.loadout;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.mwo_data.equipment.Weapon;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * A Unit Test suite for {@link WeaponGroups}.
 *
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class WeaponGroupsTest {
  private final List<Weapon> weapons = new ArrayList<>();
  private WeaponGroups cut;
  @Mock private Loadout loadout;

  @Before
  public void setup() {
    Mockito.when(loadout.items(Weapon.class)).thenReturn(weapons);
    cut = new WeaponGroups();
  }

  /** Test the default constructed state. */
  @Test
  public final void testConstructor() {
    for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
      for (int j = 0; j < WeaponGroups.MAX_WEAPONS; ++j) {
        assertFalse(cut.isInGroup(i, i));
      }
    }
  }

  /** Test the copy constructed state. */
  @Test
  public final void testCopyConstructor() {
    final Loadout loadout2 = Mockito.mock(Loadout.class);
    final List<Weapon> weapons2 = new ArrayList<>();
    Mockito.when(loadout2.items(Weapon.class)).thenReturn(weapons2);

    cut.setGroup(0, 0, true);
    cut.setGroup(5, 15, true);

    final Weapon w0 = Mockito.mock(Weapon.class);
    final Weapon w1 = Mockito.mock(Weapon.class);
    Mockito.when(w0.isOffensive()).thenReturn(true);
    Mockito.when(w1.isOffensive()).thenReturn(true);
    weapons.add(w0);
    weapons2.add(w1);

    final WeaponGroups copy = new WeaponGroups(cut);

    // Using weapons from new loadout
    assertEquals(Arrays.asList(w1), copy.getWeaponOrder(loadout2));

    // Groups are copied
    assertTrue(copy.isInGroup(0, 0));
    assertTrue(copy.isInGroup(5, 15));
    assertFalse(copy.isInGroup(0, 1)); // Implicitly false.
  }

  /** Test that a weapon can be in many groups and any group can have multiple weapons. */
  @Test
  public final void testGroup() {
    cut.setGroup(0, 0, true);
    cut.setGroup(1, 0, true);

    cut.setGroup(5, 15, true);
    cut.setGroup(5, 3, true);

    assertTrue(cut.isInGroup(0, 0));
    assertTrue(cut.isInGroup(1, 0));

    assertTrue(cut.isInGroup(5, 15));
    assertTrue(cut.isInGroup(5, 3));

    assertFalse(cut.isInGroup(4, 4));
  }

  /** Test that the correct weapons are returned for each group. */
  @Test
  public final void testGroupWeapons() {
    final Weapon w0 = Mockito.mock(Weapon.class);
    final Weapon w1 = Mockito.mock(Weapon.class);
    final Weapon w2 = Mockito.mock(Weapon.class);
    final Weapon w3 = Mockito.mock(Weapon.class);
    final Weapon w4 = Mockito.mock(Weapon.class);

    Mockito.when(w0.toString()).thenReturn("w0");
    Mockito.when(w1.toString()).thenReturn("w1");
    Mockito.when(w2.toString()).thenReturn("w2");
    Mockito.when(w3.toString()).thenReturn("w3");
    Mockito.when(w4.toString()).thenReturn("w4");

    Mockito.when(w0.isOffensive()).thenReturn(true);
    Mockito.when(w2.isOffensive()).thenReturn(true);
    Mockito.when(w3.isOffensive()).thenReturn(true);
    Mockito.when(w4.isOffensive()).thenReturn(true);

    weapons.add(w0);
    weapons.add(w1);
    weapons.add(w2);
    weapons.add(w3);
    weapons.add(w4);

    cut.setGroup(0, 0, true);
    cut.setGroup(0, 1, true); // w1 is not offensive, so it is not enumerated.
    cut.setGroup(5, 2, true);
    cut.setGroup(5, 3, true);

    final List<Weapon> ans0 = (List<Weapon>) cut.getWeapons(0, loadout);
    assertEquals(Arrays.asList(w0, w2), ans0);

    final List<Weapon> ans1 = (List<Weapon>) cut.getWeapons(5, loadout);
    assertEquals(Arrays.asList(w3, w4), ans1);
  }

  /**
   * Test that weapons are returned in the order of the iterator of loadout.item(weapon) and that
   * only offensive weapons are included.
   */
  @Test
  public final void testWeaponOrder() {
    final Weapon w0 = Mockito.mock(Weapon.class);
    final Weapon w1 = Mockito.mock(Weapon.class);
    final Weapon w2 = Mockito.mock(Weapon.class);

    Mockito.when(w0.isOffensive()).thenReturn(true);
    Mockito.when(w2.isOffensive()).thenReturn(true);

    weapons.add(w0);
    weapons.add(w1);
    weapons.add(w2);

    final List<Weapon> ans = cut.getWeaponOrder(loadout);

    assertEquals(Arrays.asList(w0, w2), ans);
  }
}
