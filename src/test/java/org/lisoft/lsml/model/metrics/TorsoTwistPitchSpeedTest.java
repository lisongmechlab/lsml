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
package org.lisoft.lsml.model.metrics;

import org.junit.Test;
import org.lisoft.lsml.model.chassi.MovementProfile;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.modifiers.Modifier;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test suite for {@link TorsoTwistPitchSpeed} {@link Metric}.
 *
 * @author Li Song
 */
@SuppressWarnings("unchecked")
public class TorsoTwistPitchSpeedTest {
    @Test
    public final void testCalculate() {
        final double modifiedSpeed = 3.2;
        final Collection<Modifier> quirks = mock(Collection.class);
        final MovementProfile movementProfile = mock(MovementProfile.class);
        final LoadoutStandard loadout = mock(LoadoutStandard.class);

        when(loadout.getAllModifiers()).thenReturn(quirks);
        when(loadout.getMovementProfile()).thenReturn(movementProfile);
        when(movementProfile.getTorsoPitchSpeed(quirks)).thenReturn(modifiedSpeed);

        final TorsoTwistPitchSpeed cut = new TorsoTwistPitchSpeed(loadout);
        assertEquals(modifiedSpeed, cut.calculate(), 0.0);
    }
}
