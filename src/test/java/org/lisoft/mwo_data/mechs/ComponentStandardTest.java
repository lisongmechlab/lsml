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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.lisoft.mwo_data.ItemDB;
import org.lisoft.mwo_data.equipment.*;

/**
 * Test suite for {@link ComponentStandard}
 *
 * @author Li Song
 */
public class ComponentStandardTest extends ComponentTest {

  private final List<HardPoint> hardPoints = new ArrayList<>();

  /** The list of hard points returned shall be immutable. */
  @Test(expected = UnsupportedOperationException.class)
  public void testGetHardPoints_Immutable() {
    makeDefaultCUT().getHardPoints().add(new HardPoint(HardPointType.ENERGY));
  }

  /**
   * A component has missile bay doors if any one of its {@link HardPoint}s has missile bay doors.
   */
  @Test
  public void testHasMissileBayDoors_HasDoors() {
    hardPoints.add(new HardPoint(HardPointType.MISSILE, 5, true));
    final ComponentStandard cut = makeDefaultCUT();
    assertTrue(cut.hasMissileBayDoors());
  }

  /**
   * A component has missile bay doors if any one of its {@link HardPoint}s has missile bay doors.
   */
  @Test
  public void testHasMissileBayDoors_NoDoors() {
    hardPoints.add(new HardPoint(HardPointType.MISSILE, 5, false));
    final ComponentStandard cut = makeDefaultCUT();
    assertFalse(cut.hasMissileBayDoors());
  }

  /**
   * Item's with no particular hard point requirements and that are small enough should be allowed.
   */
  @Test
  public void testIsAllowed_Basic() {
    final Item item = mock(Item.class);
    when(item.getHardpointType()).thenReturn(HardPointType.NONE);
    when(item.getSlots()).thenReturn(criticalSlots / 2);
    when(item.getAllowedComponents()).thenReturn(Collections.singleton(location));

    assertTrue(makeDefaultCUT().isAllowed(item));
  }

  /** C.A.S.E. is only allowed on side torso, (doesn't make sense in CT). */
  @Test
  public void testIsAllowed_CASE() {
    final List<Location> allowedLocations = new ArrayList<>();
    allowedLocations.add(Location.RightTorso);
    allowedLocations.add(Location.RightLeg);
    allowedLocations.add(Location.RightArm);
    allowedLocations.add(Location.LeftTorso);
    allowedLocations.add(Location.LeftLeg);
    allowedLocations.add(Location.LeftArm);

    for (final Location loc : Location.values()) {
      location = loc;
      if (allowedLocations.contains(loc)) {
        assertTrue(makeDefaultCUT().isAllowed(ItemDB.CASE));
      } else {
        assertFalse(makeDefaultCUT().isAllowed(ItemDB.CASE));
      }
    }
  }

  /** Double HS allowed in CT */
  @Test
  public void testIsAllowed_DhsInCt() {
    location = Location.CenterTorso;

    final HeatSink heatsink = mock(HeatSink.class);
    when(heatsink.getHardpointType()).thenReturn(HardPointType.NONE);
    when(heatsink.getSlots()).thenReturn(3);
    when(heatsink.getAllowedComponents()).thenReturn(Collections.singleton(location));

    assertTrue(makeDefaultCUT().isAllowed(heatsink));
  }

  /** Engine is only allowed in CT */
  @Test
  public void testIsAllowed_Engine() {
    final Engine engine = mock(Engine.class);
    when(engine.getHardpointType()).thenReturn(HardPointType.NONE);

    for (final Location loc : Location.values()) {
      location = loc;
      if (loc == Location.CenterTorso) {
        assertTrue(makeDefaultCUT().isAllowed(engine));
      } else {
        assertFalse(makeDefaultCUT().isAllowed(engine));
      }
    }
  }

  /** The presence of the correct hard point type shall not short circuit check for item size. */
  @Test
  public void testIsAllowed_HasHardPointsButTooBig() {
    hardPoints.add(new HardPoint(HardPointType.MISSILE, 6, false));

    final MissileWeapon missile = mock(MissileWeapon.class);
    when(missile.getHardpointType()).thenReturn(HardPointType.MISSILE);
    when(missile.getAllowedComponents()).thenReturn(Collections.singleton(location));

    assertTrue(makeDefaultCUT().isAllowed(missile));

    when(missile.getSlots()).thenReturn(criticalSlots + 1);

    assertFalse(makeDefaultCUT().isAllowed(missile));
  }

  /** Jump jets are only allowed in legs and torso. */
  @Test
  public void testIsAllowed_JumpJets() {
    final JumpJet jj = mock(JumpJet.class);
    when(jj.getHardpointType()).thenReturn(HardPointType.NONE);
    when(jj.getAllowedComponents())
        .thenReturn(
            Arrays.asList(
                Location.CenterTorso,
                Location.RightTorso,
                Location.LeftTorso,
                Location.LeftLeg,
                Location.RightLeg));
    criticalSlots = 12;

    final List<Location> allowedLocations = new ArrayList<>();
    allowedLocations.add(Location.CenterTorso);
    allowedLocations.add(Location.RightTorso);
    allowedLocations.add(Location.LeftTorso);
    allowedLocations.add(Location.LeftLeg);
    allowedLocations.add(Location.RightLeg);

    for (final Location loc : Location.values()) {
      location = loc;
      if (allowedLocations.contains(loc)) {
        assertTrue(makeDefaultCUT().isAllowed(jj));
      } else {
        assertFalse(makeDefaultCUT().isAllowed(jj));
      }
    }
  }

  /** Items that do not have a matching hard point are not allowed. */
  @Test
  public void testIsAllowed_NoHardPoints() {
    hardPoints.add(new HardPoint(HardPointType.BALLISTIC));
    hardPoints.add(new HardPoint(HardPointType.ENERGY));

    final MissileWeapon missile = mock(MissileWeapon.class);
    when(missile.getHardpointType()).thenReturn(HardPointType.MISSILE);
    when(missile.getAllowedComponents()).thenReturn(Collections.singleton(location));

    assertFalse(makeDefaultCUT().isAllowed(missile));

    hardPoints.add(new HardPoint(HardPointType.MISSILE, 6, false));
    assertTrue(makeDefaultCUT().isAllowed(missile));
  }

  /** Items that are too big to fit together with fixed items are not allowed. */
  @Test
  public void testIsAllowed_TooBig() {
    final Item fixedItem = mock(Item.class);
    fixedItems.add(fixedItem);
    final int fixedSize = criticalSlots / 2;
    when(fixedItem.getSlots()).thenReturn(fixedSize);

    final Item item = mock(Item.class);
    when(item.getHardpointType()).thenReturn(HardPointType.NONE);
    when(item.getSlots()).thenReturn(criticalSlots - fixedSize);
    when(item.getAllowedComponents()).thenReturn(Collections.singleton(location));

    assertTrue(makeDefaultCUT().isAllowed(item));

    when(item.getSlots()).thenReturn(criticalSlots - fixedSize + 1);

    assertFalse(makeDefaultCUT().isAllowed(item));
  }

  @Override
  protected ComponentStandard makeDefaultCUT() {
    return new ComponentStandard(location, criticalSlots, hp, fixedItems, hardPoints);
  }
}
