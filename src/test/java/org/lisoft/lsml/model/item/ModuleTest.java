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
package org.lisoft.lsml.model.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test suite for {@link Module}.
 *
 * @author Emily Björk
 */
public class ModuleTest {

    @Test
    public void testGetAllowedAmountOfType() {
        assertFalse(new Module(null, null, null, 0, 0, 0.0, null, 0.0, null, null, null, 0).getAllowedAmountOfType()
                .isPresent());
        assertFalse(new Module(null, null, null, 0, 0, 0.0, null, 0.0, null, null, null, null).getAllowedAmountOfType()
                .isPresent());

        assertTrue(new Module(null, null, null, 0, 0, 0.0, null, 0.0, null, null, null, 2).getAllowedAmountOfType()
                .isPresent());
        assertEquals(2, new Module(null, null, null, 0, 0, 0.0, null, 0.0, null, null, null, 2).getAllowedAmountOfType()
                .get().intValue());
    }

    @Test
    public void testIsSameTypeAs() {
        assertTrue(new ActiveProbe("", "", "", 0, 0, 0, null, 0, null, null, null, 1)
                .isSameTypeAs(new ActiveProbe("", "", "", 0, 0, 0, null, 0, null, null, null, 1)));
        assertFalse(new ActiveProbe("", "", "", 0, 0, 0, null, 0, null, null, null, 1)
                .isSameTypeAs(new TargetingComputer("", "", "", 0, 0, 0, null, 0, null, null, null, 1, null)));
    }
}
