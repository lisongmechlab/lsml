/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.mwo_data.equipment;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EngineTypeTest {

  @Test
  public void testClampRating() {
    assertEquals(Engine.EngineType.XL.minRating(), Engine.EngineType.XL.clampRating(0));
    assertEquals(Engine.EngineType.LE.minRating(), Engine.EngineType.LE.clampRating(0));
    assertEquals(Engine.EngineType.STD.minRating(), Engine.EngineType.STD.clampRating(0));

    assertEquals(
        Engine.EngineType.XL.minRating(),
        Engine.EngineType.XL.clampRating(Engine.EngineType.XL.minRating() - 5));
    assertEquals(
        Engine.EngineType.LE.minRating(),
        Engine.EngineType.LE.clampRating(Engine.EngineType.LE.minRating() - 5));
    assertEquals(
        Engine.EngineType.STD.minRating(),
        Engine.EngineType.STD.clampRating(Engine.EngineType.STD.minRating() - 5));

    assertEquals(
        Engine.EngineType.XL.minRating() + 5,
        Engine.EngineType.XL.clampRating(Engine.EngineType.XL.minRating() + 5));
    assertEquals(
        Engine.EngineType.LE.minRating() + 5,
        Engine.EngineType.LE.clampRating(Engine.EngineType.LE.minRating() + 5));
    assertEquals(
        Engine.EngineType.STD.minRating() + 5,
        Engine.EngineType.STD.clampRating(Engine.EngineType.STD.minRating() + 5));
  }

  @Test
  public void testMinRating() {
    assertEquals(100, Engine.EngineType.XL.minRating());
    assertEquals(100, Engine.EngineType.LE.minRating());
    assertEquals(60, Engine.EngineType.STD.minRating());
  }
}
