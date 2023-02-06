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
package org.lisoft.lsml.model.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.mwo_data.equipment.Internal;
import org.lisoft.lsml.mwo_data.equipment.Item;
import org.lisoft.lsml.mwo_data.mechs.Upgrades;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * A test suite for {@link CriticalStrikeProbability}.
 *
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class CriticalStrikeProbabilityTest {
  @InjectMocks CriticalStrikeProbability cut;
  List<Item> items = new ArrayList<>();
  @Mock Loadout loadout;
  @Mock ConfiguredComponent loadoutPart;
  @Mock Upgrades upgrades;

  @Before
  public void setup() {
    when(loadoutPart.getItemsEquipped()).thenReturn(items);
    when(loadout.getUpgrades()).thenReturn(upgrades);
  }

  @Test
  public void testCritChances() {
    double totalCrit = 0;
    for (final double v : CriticalStrikeProbability.CRITICAL_HIT_CHANCE) {
      totalCrit += v;
    }
    assertEquals(1.0 - totalCrit, CriticalStrikeProbability.MISS_CHANCE, 0.0);
  }

  /**
   * Easy case. When there is only one item, the chance to crit is the sum of the crit probabilities
   * for 1, 2 and 3 hits.
   */
  @Test
  public void testOneItem() {
    final Item i = Mockito.mock(Item.class);
    Mockito.when(i.getSlots()).thenReturn(5);
    Mockito.when(i.isCrittable()).thenReturn(true);
    items.add(i);

    assertEquals(0.25 + 0.14 + 0.03, cut.calculate(i), 0.0);
  }

  /**
   * When two or more items are involved it's a bit more difficult.
   *
   * <p>There are four disjunct cases that can happen: 0 hits, 1 hit, 2 hits, 3 hits with 58%, 25%,
   * 14% and 3% probability respectively (sum 100%). The chance that the item is hit by at least one
   * of those hits is the weighted sum of the above cases as they are disjunct. I.e.
   *
   * <p>P_atleastonce = 0.58*0 + 0.25*P_hit + 0.14*(1-(1-P_hit)^2) + 0.03*(1-(1-P_hit)^3)
   */
  @Test
  public void testTwoItems_R() {
    final Item i0 = Mockito.mock(Item.class);
    final Item i1 = Mockito.mock(Item.class);
    Mockito.when(i0.getSlots()).thenReturn(5);
    Mockito.when(i0.isCrittable()).thenReturn(true);
    Mockito.when(i1.getSlots()).thenReturn(15);
    Mockito.when(i1.isCrittable()).thenReturn(true);
    items.add(i0);
    items.add(i1);

    final double p_hit0 = 5.0 / 20;
    final double p_hit1 = 15.0 / 20;
    double ans0 = 0, ans1 = 0;

    // 1 crit hit: 25%
    ans0 = p_hit0 * 0.25;
    ans1 = p_hit1 * 0.25;

    // 2 crit hits: 14%
    ans0 += 0.14 * (1 - Math.pow(1 - p_hit0, 2));
    ans1 += 0.14 * (1 - Math.pow(1 - p_hit1, 2));

    // 3 crit hits: 3%
    ans0 += 0.03 * (1 - Math.pow(1 - p_hit0, 3));
    ans1 += 0.03 * (1 - Math.pow(1 - p_hit1, 3));

    assertEquals(ans0, cut.calculate(i0), 0.000001);
    assertEquals(ans1, cut.calculate(i1), 0.000001);
  }

  /** Internal items do not affect the crit rolls. */
  @Test
  public void testUncrittableItems() {
    final Item i = Mockito.mock(Item.class);
    final Item nocrit = Mockito.mock(Internal.class);
    Mockito.when(i.getSlots()).thenReturn(5);
    Mockito.when(i.isCrittable()).thenReturn(true);
    Mockito.when(nocrit.getSlots()).thenReturn(5);
    Mockito.when(nocrit.isCrittable()).thenReturn(false);
    items.add(i);
    items.add(nocrit);

    assertEquals(0.25 + 0.14 + 0.03, cut.calculate(i), 0.0);
  }
}
