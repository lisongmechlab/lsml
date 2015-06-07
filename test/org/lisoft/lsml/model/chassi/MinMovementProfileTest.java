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
package org.lisoft.lsml.model.chassi;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifiersDB;
import org.mockito.Mockito;

/**
 * Test suite for {@link MinMovementProfile}.
 * 
 * @author Li Song
 */
public class MinMovementProfileTest {

    @Test
    public void testGetMovementArchetype() {
        MovementProfile base = Mockito.mock(MovementProfile.class);
        Collection<Modifier> arm1 = new ArrayList<>();
        Collection<Modifier> arm2 = new ArrayList<>();
        Collection<Modifier> leg1 = new ArrayList<>();
        Collection<Modifier> leg2 = new ArrayList<>();

        List<Collection<Modifier>> arm = new ArrayList<>();
        List<Collection<Modifier>> leg = new ArrayList<>();
        List<List<Collection<Modifier>>> groups = new ArrayList<>();
        groups.add(arm);
        groups.add(leg);
        arm.add(arm1);
        arm.add(arm2);
        leg.add(leg1);
        leg.add(leg2);

        MinMovementProfile cut = new MinMovementProfile(base, groups);

        Mockito.when(base.getMovementArchetype()).thenReturn(MovementArchetype.Small);

        assertEquals(MovementArchetype.Small, cut.getMovementArchetype());
    }

    @Test
    public void testGetTorsoPitchSpeed() {
        MovementProfile base = Mockito.mock(MovementProfile.class);
        Collection<Modifier> arm_omnipod1 = new ArrayList<>();
        Collection<Modifier> arm_omnipod2 = new ArrayList<>();
        Collection<Modifier> leg_omnipod1 = new ArrayList<>();
        Collection<Modifier> leg_omnipod2 = new ArrayList<>();
        
        // Just add some junk to the collections to make sure they don't compare equal
        arm_omnipod1.add(new Modifier(ModifiersDB.FAST_FIRE_DESC, 1.0));
        arm_omnipod2.add(new Modifier(ModifiersDB.FAST_FIRE_DESC, 2.0));
        leg_omnipod1.add(new Modifier(ModifiersDB.FAST_FIRE_DESC, 3.0));
        leg_omnipod2.add(new Modifier(ModifiersDB.FAST_FIRE_DESC, 4.0));
        
        List<Collection<Modifier>> arm = new ArrayList<>();
        List<Collection<Modifier>> leg = new ArrayList<>();
        List<Collection<Modifier>> torso = new ArrayList<>(); // Empty groups should be handled correctly
        List<List<Collection<Modifier>>> groups = new ArrayList<>();
        groups.add(arm);
        groups.add(leg);
        groups.add(torso);
        arm.add(arm_omnipod1);
        arm.add(arm_omnipod2);
        leg.add(leg_omnipod1);
        leg.add(leg_omnipod2);

        MinMovementProfile cut = new MinMovementProfile(base, groups);

        Mockito.when(base.getTorsoPitchSpeed(null)).thenReturn(3.0); // Base value
        Mockito.when(base.getTorsoPitchSpeed(arm_omnipod1)).thenReturn(2.8);
        Mockito.when(base.getTorsoPitchSpeed(arm_omnipod2)).thenReturn(3.1);
        Mockito.when(base.getTorsoPitchSpeed(leg_omnipod1)).thenReturn(3.3);
        Mockito.when(base.getTorsoPitchSpeed(leg_omnipod2)).thenReturn(3.6);

        // Arm 1 and leg1 will give min of 3 -0.2 + 0.3 = 3.1
        assertEquals(3.1, cut.getTorsoPitchSpeed(null), Math.ulp(4.0));
    }
}
