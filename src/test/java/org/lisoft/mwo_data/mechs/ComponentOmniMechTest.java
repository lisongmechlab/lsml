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
package org.lisoft.mwo_data.mechs;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.Test;
import org.lisoft.mwo_data.equipment.Item;

/**
 * Test suite for {@link ComponentOmniMech}.
 *
 * @author Li Song
 */
public class ComponentOmniMechTest extends ComponentTest {

  private int dynamicArmourSlots;
  private int dynamicStructureSlots;
  private OmniPod omniPod;

  @Test
  public final void testGetDynamicArmourSlots() {
    dynamicArmourSlots = 3;
    assertEquals(dynamicArmourSlots, makeDefaultCUT().getDynamicArmourSlots());
  }

  @Test
  public final void testGetDynamicStructureSlots() {
    dynamicStructureSlots = 3;
    assertEquals(dynamicStructureSlots, makeDefaultCUT().getDynamicStructureSlots());
  }

  @Test
  public final void testGetFixedOmniPod() {
    omniPod = null;
    assertNull(null, makeDefaultCUT().getFixedOmniPod());

    omniPod = mock(OmniPod.class);
    assertSame(omniPod, makeDefaultCUT().getFixedOmniPod());
  }

  @Test
  public final void testHasFixedOmniPod() {
    omniPod = null;
    assertFalse(makeDefaultCUT().hasFixedOmniPod());

    omniPod = mock(OmniPod.class);
    assertTrue(makeDefaultCUT().hasFixedOmniPod());
  }

  /** An item can't be too big considering the fixed dynamic slots and items. */
  @Test
  public final void testIsAllowed_fixedItemsAndSlots() {
    criticalSlots = 12;
    dynamicArmourSlots = 2;
    dynamicStructureSlots = 1;
    final int fixedSlots = 3;
    final int freeSlots = criticalSlots - dynamicArmourSlots - dynamicStructureSlots - fixedSlots;

    final Item fixed = mock(Item.class);
    when(fixed.getSlots()).thenReturn(fixedSlots);

    fixedItems.clear();
    fixedItems.add(fixed);

    final Item item = mock(Item.class);
    when(item.getSlots()).thenReturn(freeSlots);
    when(item.getName()).thenReturn("mock item");
    when(item.getAllowedComponents()).thenReturn(Collections.singleton(location));

    assertTrue(makeDefaultCUT().isAllowed(item));

    when(item.getSlots()).thenReturn(freeSlots + 1);
    assertFalse(makeDefaultCUT().isAllowed(item));
  }

  @Override
  protected ComponentOmniMech makeDefaultCUT() {
    return new ComponentOmniMech(
        location,
        criticalSlots,
        hp,
        fixedItems,
        omniPod,
        dynamicStructureSlots,
        dynamicArmourSlots);
  }
}
