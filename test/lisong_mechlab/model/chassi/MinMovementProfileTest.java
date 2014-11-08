/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
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
package lisong_mechlab.model.chassi;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lisong_mechlab.model.quirks.MovementQuirk;
import lisong_mechlab.model.quirks.Quirks;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test suite for {@link MinMovementProfile}.
 * 
 * @author Emily Björk
 */
public class MinMovementProfileTest {

    @Test
    public void testGetMovementArchetype() {
        MovementProfile base = Mockito.mock(MovementProfile.class);
        Quirks arm1 = Mockito.mock(Quirks.class);
        Quirks arm2 = Mockito.mock(Quirks.class);
        Quirks leg1 = Mockito.mock(Quirks.class);
        Quirks leg2 = Mockito.mock(Quirks.class);

        List<Quirks> arm = new ArrayList<>();
        List<Quirks> leg = new ArrayList<>();
        List<List<Quirks>> groups = new ArrayList<>();
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
    public void testGetMaxMovementSpeed() {
        MovementProfile base = Mockito.mock(MovementProfile.class);
        Quirks arm1 = Mockito.mock(Quirks.class);
        Quirks arm2 = Mockito.mock(Quirks.class);
        Quirks leg1 = Mockito.mock(Quirks.class);
        Quirks leg2 = Mockito.mock(Quirks.class);
        
        MovementQuirk q1 = Mockito.mock(MovementQuirk.class);
        MovementQuirk q2 = Mockito.mock(MovementQuirk.class);
        MovementQuirk q3 = Mockito.mock(MovementQuirk.class);

        List<Quirks> arm = new ArrayList<>();
        List<Quirks> leg = new ArrayList<>();
        List<Quirks> torso = new ArrayList<>(); // Empty groups should be handled correctly
        List<List<Quirks>> groups = new ArrayList<>();
        groups.add(arm);
        groups.add(leg);
        groups.add(torso);
        arm.add(arm1);
        arm.add(arm2);
        leg.add(leg1);
        leg.add(leg2);

        MinMovementProfile cut = new MinMovementProfile(base, groups);

        Mockito.when(base.getTorsoPitchSpeed()).thenReturn(3.0);
        Mockito.when(arm1.getQuirksByType(MovementModifier.class)).thenReturn(Arrays.asList((MovementModifier)q1, (MovementModifier)q3));
        Mockito.when(arm2.getQuirksByType(MovementModifier.class)).thenReturn(Arrays.asList((MovementModifier)q2));
        Mockito.when(leg1.getQuirksByType(MovementModifier.class)).thenReturn(Arrays.asList((MovementModifier)q2));
        Mockito.when(leg2.getQuirksByType(MovementModifier.class)).thenReturn(Arrays.asList((MovementModifier)q3));
        Mockito.when(q1.extraTorsoPitchSpeed(3.0)).thenReturn(-0.5);
        Mockito.when(q2.extraTorsoPitchSpeed(3.0)).thenReturn(0.2);
        Mockito.when(q3.extraTorsoPitchSpeed(3.0)).thenReturn(0.4);

        // Arm 1 and leg1 will give min of 3.1
        assertEquals(3.1, cut.getTorsoPitchSpeed(), 0.0);
    }
}
