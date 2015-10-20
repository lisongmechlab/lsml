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
package org.lisoft.lsml.model.datacache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.chassi.OmniPod;

/**
 * Test suite for {@link OmniPodDB}.
 * 
 * @author Li Song
 */
public class OmniPodDBTest {

    @Test
    public void testLoadOmniPod_ECM() {
        OmniPod kfx_c_ra = OmniPodDB.lookup(30192);

        assertEquals(1, kfx_c_ra.getHardPointCount(HardPointType.ENERGY));
        assertEquals(3, kfx_c_ra.getHardPointCount(HardPointType.AMS));
        assertEquals(1, kfx_c_ra.getHardPointCount(HardPointType.ECM));
    }

    @Test
    public void testLoadOmniPod_Togglables() {
        OmniPod ans = OmniPodDB.lookup(30077); // Kitfox Prime Right Arm

        assertEquals(2, ans.getToggleableItems().size());
    }

    @Test
    public void testLookup_BySeries() {
        Collection<OmniPod> ans = OmniPodDB.lookup("kItFox", Location.RightArm);
        assertTrue(ans.size() >= 4);
    }
}
