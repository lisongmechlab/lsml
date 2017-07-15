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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.junit.Test;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.chassi.MovementProfile;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.modifiers.Modifier;

/**
 * Test suite for {@link TorsoTwistYawSpeed} {@link Metric}.
 *
 * @author Li Song
 */
@SuppressWarnings("unchecked")
public class ArmRotateYawSpeedTest {

    @Test
    public final void testCalculate() {
        final int rating = 300;
        final int mass = 50;
        final double modifiedSpeed = 3.2;
        final Collection<Modifier> quirks = mock(Collection.class);
        final MovementProfile movementProfile = mock(MovementProfile.class);
        final LoadoutStandard loadout = mock(LoadoutStandard.class);
        final ChassisStandard chassis = mock(ChassisStandard.class);
        final Engine engine = mock(Engine.class);

        when(loadout.getModifiers()).thenReturn(quirks);
        when(loadout.getChassis()).thenReturn(chassis);
        when(loadout.getEngine()).thenReturn(engine);
        when(loadout.getMovementProfile()).thenReturn(movementProfile);
        when(movementProfile.getArmYawSpeed(quirks)).thenReturn(modifiedSpeed);
        when(chassis.getMassMax()).thenReturn(mass);
        when(engine.getRating()).thenReturn(rating);

        final ArmRotateYawSpeed cut = new ArmRotateYawSpeed(loadout);
        assertEquals(modifiedSpeed * rating / mass, cut.calculate(), 0.0);
    }

    /**
     * Without an engine, the twist speed shall be zero.
     */
    @Test
    public final void testCalculate_NoEngine() {
        final MovementProfile movementProfile = mock(MovementProfile.class);
        final LoadoutStandard loadout = mock(LoadoutStandard.class);
        final ChassisStandard chassis = mock(ChassisStandard.class);

        when(loadout.getChassis()).thenReturn(chassis);
        when(loadout.getEngine()).thenReturn(null);
        when(loadout.getMovementProfile()).thenReturn(movementProfile);

        final double factor = 0.2;
        final int mass = 50;
        when(movementProfile.getArmYawSpeed(null)).thenReturn(factor);
        when(chassis.getMassMax()).thenReturn(mass);

        final ArmRotateYawSpeed cut = new ArmRotateYawSpeed(loadout);
        assertEquals(0, cut.calculate(), 0.0);
    }

}
