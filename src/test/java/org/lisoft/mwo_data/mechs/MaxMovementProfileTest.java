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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.lisoft.mwo_data.modifiers.Modifier;
import org.mockito.Mockito;

/**
 * Test suite for {@link MaxMovementProfile}.
 *
 * @author Li Song
 */
public class MaxMovementProfileTest {

  @Test
  public void testGetMovementArchetype() {
    MovementProfile base = Mockito.mock(MovementProfile.class);
    Collection<Modifier> arm_omnipod1 = new ArrayList<>();
    Collection<Modifier> arm_omnipod2 = new ArrayList<>();
    Collection<Modifier> leg_omnipod1 = new ArrayList<>();
    Collection<Modifier> leg_omnipod2 = new ArrayList<>();

    List<Collection<Modifier>> arm = new ArrayList<>();
    List<Collection<Modifier>> leg = new ArrayList<>();
    List<List<Collection<Modifier>>> groups = new ArrayList<>();
    groups.add(arm);
    groups.add(leg);
    arm.add(arm_omnipod1);
    arm.add(arm_omnipod2);
    leg.add(leg_omnipod1);
    leg.add(leg_omnipod2);

    MaxMovementProfile cut = new MaxMovementProfile(base, groups);

    Mockito.when(base.getMovementArchetype()).thenReturn(MovementProfile.MovementArchetype.Small);

    assertEquals(MovementProfile.MovementArchetype.Small, cut.getMovementArchetype());
  }

  @Test
  public void testGetTorsoPitchSpeed() {
    MovementProfile base = Mockito.mock(MovementProfile.class);
    Collection<Modifier> arm_omnipod1 = new ArrayList<>();
    Collection<Modifier> arm_omnipod2 = new ArrayList<>();
    Collection<Modifier> leg_omnipod1 = new ArrayList<>();
    Collection<Modifier> leg_omnipod2 = new ArrayList<>();

    // Just add some junk to the collections to make sure they don't compare equal
    arm_omnipod1.add(Mockito.mock(Modifier.class));
    arm_omnipod2.add(Mockito.mock(Modifier.class));
    leg_omnipod1.add(Mockito.mock(Modifier.class));
    leg_omnipod2.add(Mockito.mock(Modifier.class));

    List<Collection<Modifier>> arm = new ArrayList<>();
    List<Collection<Modifier>> leg = new ArrayList<>();
    List<Collection<Modifier>> torso =
        new ArrayList<>(); // Empty groups should be handled correctly
    List<List<Collection<Modifier>>> groups = new ArrayList<>();
    groups.add(arm);
    groups.add(leg);
    groups.add(torso);
    arm.add(arm_omnipod1);
    arm.add(arm_omnipod2);
    leg.add(leg_omnipod1);
    leg.add(leg_omnipod2);

    MaxMovementProfile cut = new MaxMovementProfile(base, groups);

    Mockito.when(base.getTorsoPitchSpeed(null)).thenReturn(3.0); // Base value
    Mockito.when(base.getTorsoPitchSpeed(arm_omnipod1)).thenReturn(2.8);
    Mockito.when(base.getTorsoPitchSpeed(arm_omnipod2)).thenReturn(3.1);
    Mockito.when(base.getTorsoPitchSpeed(leg_omnipod1)).thenReturn(3.3);
    Mockito.when(base.getTorsoPitchSpeed(leg_omnipod2)).thenReturn(3.6);

    // Arm 2 + Leg 2 will give max of 0.1 + 0.6
    assertEquals(3.7, cut.getTorsoPitchSpeed(null), Math.ulp(4.0));
  }
}
