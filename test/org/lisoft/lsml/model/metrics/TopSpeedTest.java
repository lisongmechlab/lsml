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

import java.util.Collection;

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
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link TopSpeed}.
 * 
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.class)
public class TopSpeedTest {
    int                  mass      = 30;
    int                  rating    = 300;
    double               moveSpeed = 4.0;
    @Mock
    MovementProfile      movementProfile;
    @Mock
    Engine               engine;
    @Mock
    Loadout          loadout;
    @Mock
    Collection<Modifier> modifiers;
    @Mock
    Chassis          chassis;

    @Before
    public void setup() {
        Mockito.when(engine.getRating()).thenReturn(rating);
        Mockito.when(chassis.getMassMax()).thenReturn(mass);
        Mockito.when(movementProfile.getSpeedFactor(modifiers)).thenReturn(moveSpeed);
        Mockito.when(loadout.getModifiers()).thenReturn(modifiers);
        Mockito.when(loadout.getChassis()).thenReturn(chassis);
        Mockito.when(loadout.getEngine()).thenReturn(engine);
        Mockito.when(loadout.getMovementProfile()).thenReturn(movementProfile);
    }

    @Test
    public void testCalculate_noengine() throws Exception {
        loadout = Mockito.mock(Loadout.class);

        TopSpeed cut = new TopSpeed(loadout);

        assertEquals(0, cut.calculate(), 0.0);
    }

    @Test
    public void testCalculate() throws Exception {
        TopSpeed cut = new TopSpeed(loadout);
        double expected = rating * moveSpeed / mass;
        assertEquals(expected, cut.calculate(), 0.0);
    }
}
