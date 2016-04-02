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
public class ArmRotateYawSpeedTest {

    /**
     * Without an engine, the twist speed shall be zero.
     */
    @Test
    public final void testCalculate_NoEngine() {
        MovementProfile movementProfile = Mockito.mock(MovementProfile.class);
        LoadoutStandard loadout = Mockito.mock(LoadoutStandard.class);
        ChassisStandard chassi = Mockito.mock(ChassisStandard.class);

        Mockito.when(loadout.getChassis()).thenReturn(chassi);
        Mockito.when(loadout.getEngine()).thenReturn(null);
        Mockito.when(loadout.getMovementProfile()).thenReturn(movementProfile);

        double factor = 0.2;
        int mass = 50;
        Mockito.when(movementProfile.getArmYawSpeed(null)).thenReturn(factor);
        Mockito.when(chassi.getMassMax()).thenReturn(mass);

        ArmRotateYawSpeed cut = new ArmRotateYawSpeed(loadout);
        assertEquals(0, cut.calculate(), 0.0);
    }

    @Test
    public final void testCalculate() {
        final int rating = 300;
        final int mass = 50;
        final double modifiedSpeed = 3.2;
        Collection<Modifier> quirks = Mockito.mock(Collection.class);
        MovementProfile movementProfile = Mockito.mock(MovementProfile.class);
        LoadoutStandard loadout = Mockito.mock(LoadoutStandard.class);
        ChassisStandard chassi = Mockito.mock(ChassisStandard.class);
        Engine engine = Mockito.mock(Engine.class);

        Mockito.when(loadout.getModifiers()).thenReturn(quirks);
        Mockito.when(loadout.getChassis()).thenReturn(chassi);
        Mockito.when(loadout.getEngine()).thenReturn(engine);
        Mockito.when(loadout.getMovementProfile()).thenReturn(movementProfile);
        Mockito.when(movementProfile.getArmYawSpeed(quirks)).thenReturn(modifiedSpeed);
        Mockito.when(chassi.getMassMax()).thenReturn(mass);
        Mockito.when(engine.getRating()).thenReturn(rating);

        ArmRotateYawSpeed cut = new ArmRotateYawSpeed(loadout);
        assertEquals(modifiedSpeed * rating / mass, cut.calculate(), 0.0);
    }

}
