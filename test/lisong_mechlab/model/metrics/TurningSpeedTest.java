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
package lisong_mechlab.model.metrics;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.MovementProfile;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.quirks.Modifier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * A test suite for {@link TurningSpeed}.
 * 
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.class)
public class TurningSpeedTest {
    int                  mass      = 30;
    int                  rating    = 300;
    double               moveSpeed = 4.0;
    @Mock
    MovementProfile      movementProfile;
    @Mock
    Engine               engine;
    @Mock
    LoadoutBase<?>       loadout;
    @Mock
    Collection<Modifier> modifiers;
    @Mock
    ChassisBase          chassis;

    @Before
    public void setup() {
        Mockito.when(engine.getRating()).thenReturn(rating);
        Mockito.when(chassis.getMassMax()).thenReturn(mass);
        Mockito.when(movementProfile.getMaxMovementSpeed(modifiers)).thenReturn(moveSpeed);
        Mockito.when(loadout.getModifiers()).thenReturn(modifiers);
        Mockito.when(loadout.getChassis()).thenReturn(chassis);
        Mockito.when(loadout.getEngine()).thenReturn(engine);
        Mockito.when(loadout.getMovementProfile()).thenReturn(movementProfile);
    }

    /**
     * Turning speed is zero without engine.
     */
    @Test
    public final void testCalculate_NoEngine(){
        loadout = Mockito.mock(LoadoutBase.class);
        TurningSpeed cut = new TurningSpeed(loadout);
        assertEquals(0, cut.calculate(), 0.0);
    }

    @Test
    public final void testCalculate() throws Exception {
        double factor = 0.2;
        Mockito.when(movementProfile.getTurnLerpLowRate(modifiers)).thenReturn(factor * Math.PI / 180.0);

        TurningSpeed cut = new TurningSpeed(loadout);
        assertEquals(factor * rating / mass, cut.calculate(), 1E-8);
    }

}
