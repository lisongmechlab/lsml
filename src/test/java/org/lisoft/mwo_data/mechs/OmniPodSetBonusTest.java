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
package org.lisoft.mwo_data.mechs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collection;
import org.junit.Test;
import org.lisoft.mwo_data.modifiers.Modifier;

/**
 * Test suite for {@link OmniPodSetBonus}.
 *
 * @author Li Song
 */
public class OmniPodSetBonusTest {

  @Test
  public void testGetModifiers() {
    final Collection<Modifier> modifiers = new ArrayList<>();
    final Modifier modifier = mock(Modifier.class);
    final int minPieces = 6;
    modifiers.add(modifier);

    final OmniPodSetBonus cut = new OmniPodSetBonus(minPieces, modifiers);
    final Collection<Modifier> ans = cut.getModifiers();
    assertEquals(1, ans.size());
    assertTrue(ans.contains(modifier));
    assertEquals(minPieces, cut.getMinPieces());
  }
}
