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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.model.helpers.MockLoadoutContainer;
import org.lisoft.lsml.model.item.ItemDB;
import org.lisoft.lsml.model.item.JumpJet;

/**
 * A test suite for {@link JumpDistance}.
 * 
 * @author Li Song
 */
public class JumpDistanceTest {
    private final MockLoadoutContainer mlc   = new MockLoadoutContainer();
    private final JumpDistance         cut   = new JumpDistance(mlc.loadout);
    private final List<JumpJet>        items = new ArrayList<>();

    @Before
    public void setup() {
        when(mlc.loadout.items(JumpJet.class)).thenReturn(items);
    }

    /**
     * The jump jet definitions in the xml list forces in kilo Newton. When weights are taken as tons, the calculations
     * can be done without compensating for factor 1000 (surprise, how convenient!). F = m*a h = a*t^2/2 = F*t*t/(2*m)
     * TODO: Does not take into account the impulse yet!
     */
    @Test
    public void testCalculate() {
        final int mass = 30;
        final int num_jj = 3;
        final JumpJet jj = (JumpJet) ItemDB.lookup("JUMP JETS - CLASS I");

        final double t = jj.getDuration();
        final double F = jj.getForce();
        final double h = F * t * t / (2 * mass) * num_jj;

        items.add(jj);
        when(mlc.chassi.getMassMax()).thenReturn(mass);
        when(mlc.loadout.getJumpJetCount()).thenReturn(num_jj);
        assertEquals(h, cut.calculate(), 0.5);
    }

    /**
     * A mech with zero jump jets shall have a jump distance of 0m.
     */
    @Test
    public void testCalculate_noJJ() {
        when(mlc.loadout.getJumpJetCount()).thenReturn(0);
        assertEquals(0.0, cut.calculate(), 0.0);
    }
}
