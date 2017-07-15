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

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.Test;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.chassi.MovementProfile;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.mockito.Mockito;

/**
 * Test suite for {@link TorsoTwistYawSpeed} {@link Metric}.
 *
 * @author Li Song
 */
public class TorsoTwistYawSpeedTest {

    @Test
    public final void testCalculate() {
        final int rating = 300;
        final int mass = 50;
        final double modifiedSpeed = 3.2;
        final Collection<Modifier> quirks = Mockito.mock(Collection.class);
        final MovementProfile movementProfile = Mockito.mock(MovementProfile.class);
        final LoadoutStandard loadout = Mockito.mock(LoadoutStandard.class);
        final ChassisStandard chassis = Mockito.mock(ChassisStandard.class);
        final Engine engine = Mockito.mock(Engine.class);

        Mockito.when(loadout.getModifiers()).thenReturn(quirks);
        Mockito.when(loadout.getChassis()).thenReturn(chassis);
        Mockito.when(loadout.getEngine()).thenReturn(engine);
        Mockito.when(loadout.getMovementProfile()).thenReturn(movementProfile);
        Mockito.when(movementProfile.getTorsoYawSpeed(quirks)).thenReturn(modifiedSpeed);
        Mockito.when(chassis.getMassMax()).thenReturn(mass);
        Mockito.when(engine.getRating()).thenReturn(rating);

        final TorsoTwistYawSpeed cut = new TorsoTwistYawSpeed(loadout);
        assertEquals(modifiedSpeed * rating / mass, cut.calculate(), 0.0);
    }

    /**
     * Without an engine, the twist speed shall be zero.
     */
    @Test
    public final void testCalculate_NoEngine() {
        final MovementProfile movementProfile = Mockito.mock(MovementProfile.class);
        final LoadoutStandard loadout = Mockito.mock(LoadoutStandard.class);
        final ChassisStandard chassis = Mockito.mock(ChassisStandard.class);

        Mockito.when(loadout.getChassis()).thenReturn(chassis);
        Mockito.when(loadout.getEngine()).thenReturn(null);
        Mockito.when(loadout.getMovementProfile()).thenReturn(movementProfile);

        final double factor = 0.2;
        final int mass = 50;
        Mockito.when(movementProfile.getTorsoYawSpeed(null)).thenReturn(factor);
        Mockito.when(chassis.getMassMax()).thenReturn(mass);

        final TorsoTwistYawSpeed cut = new TorsoTwistYawSpeed(loadout);
        assertEquals(0, cut.calculate(), 0.0);
    }

}
