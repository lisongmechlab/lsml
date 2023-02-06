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
package org.lisoft.lsml.model;

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Test;
import org.lisoft.mwo_data.Environment;
import org.lisoft.mwo_data.equipment.NoSuchItemException;

/**
 * Test suite for {@link EnvironmentDB}
 *
 * @author Li Song
 */
public class EnvironmentDBTest {

  /**
   * {@link EnvironmentDB#lookup(String)} shall return an {@link Environment} with matching name if
   * found in the DB.
   */
  @Test
  public void testLookup() throws Exception {
    final Environment caustic = EnvironmentDB.lookup("caustic valley");

    assertNotEquals(0.0, caustic.getHeat(null), 0.0);
    assertEquals("CAUSTIC VALLEY", caustic.getName());
  }

  /** {@link EnvironmentDB#lookupAll()} shall return all maps in the game. */
  @Test
  public void testLookupAll() {
    final List<Environment> environments = EnvironmentDB.lookupAll();

    assertTrue(14 < environments.size());
  }

  /** {@link EnvironmentDB#lookup(String)} shall return null if the map was not found. */
  @Test(expected = NoSuchItemException.class)
  public void testLookupNull() throws Exception {
    EnvironmentDB.lookup("Mumbo jumbo therma");
  }
}
