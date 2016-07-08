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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.lisoft.lsml.model.item.MASC;
import org.lisoft.lsml.model.loadout.Loadout;

/**
 * Test suite for MASC speed calculation metric
 *
 * @author Emily Björk
 *
 */
public class MASCSpeedTest {

    @Test
    public void testMasc() {
        final MASC masc = mock(MASC.class);
        when(masc.getSpeedBoost()).thenReturn(0.2);

        final Loadout loadout = mock(Loadout.class);
        when(loadout.items(MASC.class)).thenReturn(Arrays.asList(masc));

        final TopSpeed topSpeed = mock(TopSpeed.class);
        when(topSpeed.calculate()).thenReturn(100.0);

        final Metric cut = new MASCSpeed(loadout, topSpeed);

        assertEquals(120.0, cut.calculate(), 0.0);
    }

    @Test
    public void testNoMasc() {
        final TopSpeed topSpeed = mock(TopSpeed.class);
        final Loadout loadout = mock(Loadout.class);
        when(loadout.items(any())).thenReturn(Collections.emptyList());

        final Metric cut = new MASCSpeed(loadout, topSpeed);

        assertTrue(Double.isNaN(cut.calculate()));
    }
}
