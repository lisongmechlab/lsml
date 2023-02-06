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
package org.lisoft.lsml.mwo_data.mechs;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.lisoft.lsml.mwo_data.equipment.Engine;
import org.lisoft.lsml.mwo_data.equipment.HeatSink;
import org.lisoft.lsml.mwo_data.equipment.Internal;
import org.lisoft.lsml.mwo_data.equipment.Item;
import org.lisoft.lsml.mwo_data.modifiers.Attribute;
import org.lisoft.lsml.mwo_data.modifiers.ModifierDescription;
import org.mockito.Mockito;

/**
 * Test suite for {@link Component}.
 *
 * @author Li Song
 */
public abstract class ComponentTest {

  protected int criticalSlots = 5;
  protected final List<Item> fixedItems = new ArrayList<>();
  protected Attribute hp = new Attribute(15, ModifierDescription.SEL_STRUCTURE);
  protected Location location = Location.Head;

  @Test
  public void testGetArmourMax_Head() {
    hp = new Attribute(20, ModifierDescription.SEL_STRUCTURE);
    location = Location.Head;
    assertEquals(18, makeDefaultCUT().getArmourMax());
  }

  @Test
  public void testGetArmourMax_Other() {
    hp = new Attribute(20, ModifierDescription.SEL_STRUCTURE);
    location = Location.CenterTorso;
    assertEquals(40, makeDefaultCUT().getArmourMax());
  }

  @Test
  public void testGetFixedItemSlots() {
    Item item1 = Mockito.mock(Item.class);
    Item item2 = Mockito.mock(Item.class);

    Mockito.when(item1.getSlots()).thenReturn(3);
    Mockito.when(item2.getSlots()).thenReturn(4);

    fixedItems.clear();
    fixedItems.add(item1);
    fixedItems.add(item2);

    assertEquals(7, makeDefaultCUT().getFixedItemSlots());
  }

  @Test
  public void testGetFixedItemSlots_EngineHS() {
    criticalSlots = 12;
    Engine engine = Mockito.mock(Engine.class);
    HeatSink hs = Mockito.mock(HeatSink.class);
    Internal gyro = Mockito.mock(Internal.class);

    Mockito.when(engine.getSlots()).thenReturn(6);
    Mockito.when(gyro.getSlots()).thenReturn(4);
    Mockito.when(hs.getSlots()).thenReturn(2);

    Mockito.when(engine.getNumHeatsinkSlots()).thenReturn(4);

    fixedItems.clear();
    fixedItems.add(gyro);
    fixedItems.add(hs); // Add some heat sinks before the engine and some after.
    fixedItems.add(hs);
    fixedItems.add(engine);
    fixedItems.add(hs);
    fixedItems.add(hs);
    fixedItems.add(hs);

    // Gyro (4) + Engine (6) + 4x Engine HS (0) + 1x External HS (2) = 12

    assertEquals(12, makeDefaultCUT().getFixedItemSlots());
  }

  @Test
  public void testGetFixedItems() {
    assertEquals(fixedItems, makeDefaultCUT().getFixedItems());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetFixedItems_Immutable() {
    makeDefaultCUT().getFixedItems().add(null);
  }

  @Test
  public void testGetFixedItems_NotNull() {
    assertNotNull(makeDefaultCUT().getFixedItems());
  }

  @Test
  public void testGetHitPoints() {
    assertEquals(hp.value(null), makeDefaultCUT().getHitPoints(null), 0.0);
  }

  @Test
  public void testGetLocation() {
    assertEquals(location, makeDefaultCUT().getLocation());
  }

  @Test
  public void testGetSlots() {
    assertEquals(criticalSlots, makeDefaultCUT().getSlots());
  }

  /** The string representation of the component shall contain the name of the location. */
  @Test
  public void testToString() {
    assertTrue(makeDefaultCUT().toString().contains(location.toString()));
  }

  protected abstract Component makeDefaultCUT();
}
