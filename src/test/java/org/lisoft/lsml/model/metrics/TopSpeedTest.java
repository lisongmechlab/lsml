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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.MovementProfile;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Test suite for {@link TopSpeed}.
 *
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.class)
public class TopSpeedTest {
    @Mock
    Chassis chassis;
    @Mock
    Engine engine;
    @Mock
    Loadout loadout;
    int mass = 30;
    @Mock
    Collection<Modifier> modifiers;
    double moveSpeed = 4.0;
    @Mock
    MovementProfile movementProfile;
    int rating = 300;

    @Before
    public void setup() {
        Mockito.when(engine.getRating()).thenReturn(rating);
        Mockito.when(chassis.getMassMax()).thenReturn(mass);
        Mockito.when(movementProfile.getSpeedFactor(modifiers)).thenReturn(moveSpeed);
        Mockito.when(loadout.getAllModifiers()).thenReturn(modifiers);
        Mockito.when(loadout.getChassis()).thenReturn(chassis);
        Mockito.when(loadout.getEngine()).thenReturn(engine);
        Mockito.when(loadout.getMovementProfile()).thenReturn(movementProfile);
    }

    @Test
    public void testCalculate() throws Exception {
        TopSpeed cut = new TopSpeed(loadout);
        double expected = rating * moveSpeed / mass;
        assertEquals(expected, cut.calculate(), 0.0);
    }

    @Test
    public void testCalculate_noengine() throws Exception {
        loadout = Mockito.mock(Loadout.class);

        TopSpeed cut = new TopSpeed(loadout);

        assertEquals(0, cut.calculate(), 0.0);
    }
}
