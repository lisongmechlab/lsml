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
package org.lisoft.lsml.model.datacache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.loadout.StockLoadout;
import org.lisoft.lsml.model.loadout.StockLoadout.StockComponent;
import org.lisoft.lsml.model.loadout.StockLoadout.StockComponent.ActuatorState;

/**
 * Test suite for {@link StockLoadoutDB}.
 *
 * @author Emily Björk
 */
@SuppressWarnings("javadoc")
public class StockLoadoutDBTest {

    /**
     * Test that actuator state "Both" is correctly loaded for stock loadout.
     *
     * @throws Exception
     */
    @Test
    public void testLookup_Bug433_SCR_A() throws Exception {
        final StockLoadout stock = StockLoadoutDB.lookup(ChassisDB.lookup("SCR-A"));

        StockComponent leftArm = null;
        StockComponent rightArm = null;
        for (final StockComponent component : stock.getComponents()) {
            if (component.getLocation() == Location.LeftArm) {
                leftArm = component;
            }
            if (component.getLocation() == Location.RightArm) {
                rightArm = component;
            }
        }
        assertNotNull(leftArm);
        assertNotNull(rightArm);
        assertEquals(ActuatorState.BOTH, leftArm.getActuatorState());
        assertEquals(ActuatorState.BOTH, rightArm.getActuatorState());
    }

    /**
     * Test that actuator state "None" is correctly loaded for stock loadout.
     *
     * @throws Exception
     */
    @Test
    public void testLookup_Bug433_SCR_PRIME_S() throws Exception {
        final StockLoadout stock = StockLoadoutDB.lookup(ChassisDB.lookup("SCR-PRIME(S)"));

        StockComponent leftArm = null;
        StockComponent rightArm = null;
        for (final StockComponent component : stock.getComponents()) {
            if (component.getLocation() == Location.LeftArm) {
                leftArm = component;
            }
            if (component.getLocation() == Location.RightArm) {
                rightArm = component;
            }
        }
        assertNotNull(leftArm);
        assertNotNull(rightArm);
        assertEquals(ActuatorState.NONE, leftArm.getActuatorState());
        assertEquals(ActuatorState.BOTH, rightArm.getActuatorState());
    }

    /**
     * Test that actuator state "Only left arm" is correctly loaded for stock loadout.
     *
     * @throws Exception
     */
    @Test
    public void testLookup_Bug433_TBR_PRIME_I() throws Exception {
        final StockLoadout stock = StockLoadoutDB.lookup(ChassisDB.lookup("TBR-PRIME(I)"));

        StockComponent leftArm = null;
        StockComponent rightArm = null;
        for (final StockComponent component : stock.getComponents()) {
            if (component.getLocation() == Location.LeftArm) {
                leftArm = component;
            }
            if (component.getLocation() == Location.RightArm) {
                rightArm = component;
            }
        }
        assertNotNull(leftArm);
        assertNotNull(rightArm);
        assertEquals(ActuatorState.LAA, leftArm.getActuatorState());
        assertEquals(ActuatorState.LAA, rightArm.getActuatorState());
    }
}
