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
package lisong_mechlab.model.metrics.helpers;

import static org.junit.Assert.assertEquals;
import lisong_mechlab.model.item.BallisticWeapon;
import lisong_mechlab.model.item.ItemDB;

import org.junit.Test;

/**
 * This class implements a test suite for {@link DoubleFireBurstSignal}.
 * 
 * @author Emily Björk
 */
public class DoubleFireBurstSignalTest {
    final BallisticWeapon uac5 = (BallisticWeapon) ItemDB.lookup("ULTRA AC/5");

    @SuppressWarnings("unused")
    // Expecting exception
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidWeapon() {
        new DoubleFireBurstSignal((BallisticWeapon) ItemDB.lookup("AC/20"), null, null, 0);
    }

    @Test
    public void testOneCooldown() {
        DoubleFireBurstSignal cut = new DoubleFireBurstSignal(uac5, null, null, 0);

        double p_jam = uac5.getJamProbability();
        double expected = (p_jam + (1 - p_jam) * 2) * uac5.getDamagePerShot();
        assertEquals(expected, cut.integrateFromZeroTo(uac5.getRawSecondsPerShot(null, null) / 2), 0.0);
    }
}
