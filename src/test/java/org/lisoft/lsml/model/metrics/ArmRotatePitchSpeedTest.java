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
package org.lisoft.lsml.model.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.junit.Test;
import org.lisoft.lsml.model.chassi.MovementProfile;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.modifiers.Modifier;

/**
 * Test suite for {@link TorsoTwistYawSpeed} {@link Metric}.
 *
 * @author Emily Björk
 */
public class ArmRotatePitchSpeedTest {

    @Test
    public final void testCalculate() {
        final double modifiedSpeed = 3.2;
        final Collection<Modifier> quirks = mock(Collection.class);
        final MovementProfile movementProfile = mock(MovementProfile.class);
        final LoadoutStandard loadout = mock(LoadoutStandard.class);

        when(loadout.getAllModifiers()).thenReturn(quirks);
        when(loadout.getMovementProfile()).thenReturn(movementProfile);
        when(movementProfile.getArmPitchSpeed(quirks)).thenReturn(modifiedSpeed);

        final ArmRotatePitchSpeed cut = new ArmRotatePitchSpeed(loadout);
        assertEquals(modifiedSpeed, cut.calculate(), 0.0);
    }
}
