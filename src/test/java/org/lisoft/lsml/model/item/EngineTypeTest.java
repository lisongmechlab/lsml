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

import org.junit.Test;

public class EngineTypeTest {

    @Test
    public void testClampRating() {
        assertEquals(EngineType.XL.minRating(), EngineType.XL.clampRating(0));
        assertEquals(EngineType.LE.minRating(), EngineType.LE.clampRating(0));
        assertEquals(EngineType.STD.minRating(), EngineType.STD.clampRating(0));

        assertEquals(EngineType.XL.minRating(), EngineType.XL.clampRating(EngineType.XL.minRating() - 5));
        assertEquals(EngineType.LE.minRating(), EngineType.LE.clampRating(EngineType.LE.minRating() - 5));
        assertEquals(EngineType.STD.minRating(), EngineType.STD.clampRating(EngineType.STD.minRating() - 5));

        assertEquals(EngineType.XL.minRating() + 5, EngineType.XL.clampRating(EngineType.XL.minRating() + 5));
        assertEquals(EngineType.LE.minRating() + 5, EngineType.LE.clampRating(EngineType.LE.minRating() + 5));
        assertEquals(EngineType.STD.minRating() + 5, EngineType.STD.clampRating(EngineType.STD.minRating() + 5));
    }

    @Test
    public void testMinRating() {
        assertEquals(100, EngineType.XL.minRating());
        assertEquals(100, EngineType.LE.minRating());
        assertEquals(60, EngineType.STD.minRating());
    }

}
