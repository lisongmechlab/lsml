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
package org.lisoft.lsml.model.upgrades;

import org.junit.Test;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.database.UpgradeDB;
import org.lisoft.lsml.model.item.Ammunition;

import static org.junit.Assert.*;

/**
 * Test suite for {@link GuidanceUpgrade}.
 *
 * @author Li Song
 */
public class GuidanceUpgradeTest {

    @Test
    public final void testGetSpreadFactor_Artemis() {
        final GuidanceUpgrade cut = UpgradeDB.ARTEMIS_IV;
        final double ans = cut.getSpreadFactor();
        assertTrue(ans < 0.9); // Reasonable range of values.
        assertTrue(ans > 0.4);
    }

    @Test
    public final void testGetSpreadFactor_NoArtemis() {
        final GuidanceUpgrade cut = UpgradeDB.STD_GUIDANCE;
        final double ans = cut.getSpreadFactor();
        assertEquals(1.0, ans, 0.0);
    }

    @Test
    public void testUpgrade() throws Exception {
        final GuidanceUpgrade artemis = UpgradeDB.ARTEMIS_IV;
        final GuidanceUpgrade standard = UpgradeDB.STD_GUIDANCE;

        // Standard -> Artemis
        assertSame(ItemDB.lookup("C-LRM AMMO + ART. IV"), artemis.upgrade((Ammunition) ItemDB.lookup("C-LRM AMMO")));
        assertSame(ItemDB.lookup("C-NARC AMMO"), artemis.upgrade((Ammunition) ItemDB.lookup("C-NARC AMMO")));
        assertSame(ItemDB.lookup("C-S-SRM AMMO"), artemis.upgrade((Ammunition) ItemDB.lookup("C-S-SRM AMMO")));
        assertSame(ItemDB.lookup("C-SRM AMMO + ART. IV"), artemis.upgrade((Ammunition) ItemDB.lookup("C-SRM AMMO")));

        assertSame(ItemDB.lookup("C-LRM AMMO+ART. IV (1/2)"),
                   artemis.upgrade((Ammunition) ItemDB.lookup("C-LRM AMMO (1/2)")));
        assertSame(ItemDB.lookup("C-NARC AMMO (1/2)"),
                   artemis.upgrade((Ammunition) ItemDB.lookup("C-NARC AMMO (1/2)")));
        assertSame(ItemDB.lookup("C-S-SRM AMMO (1/2)"),
                   artemis.upgrade((Ammunition) ItemDB.lookup("C-S-SRM AMMO (1/2)")));
        assertSame(ItemDB.lookup("C-SRM AMMO+ART. IV (1/2)"),
                   artemis.upgrade((Ammunition) ItemDB.lookup("C-SRM AMMO (1/2)")));

        assertSame(ItemDB.lookup("LRM AMMO + ARTEMIS IV"), artemis.upgrade((Ammunition) ItemDB.lookup("LRM AMMO")));
        assertSame(ItemDB.lookup("NARC AMMO"), artemis.upgrade((Ammunition) ItemDB.lookup("NARC AMMO")));
        assertSame(ItemDB.lookup("STREAK SRM AMMO"), artemis.upgrade((Ammunition) ItemDB.lookup("STREAK SRM AMMO")));
        assertSame(ItemDB.lookup("SRM AMMO + ARTEMIS IV"), artemis.upgrade((Ammunition) ItemDB.lookup("SRM AMMO")));

        assertSame(ItemDB.lookup("LRM AMMO + ARTEMIS IV (1/2)"),
                   artemis.upgrade((Ammunition) ItemDB.lookup("LRM AMMO (1/2)")));
        assertSame(ItemDB.lookup("NARC AMMO (1/2)"), artemis.upgrade((Ammunition) ItemDB.lookup("NARC AMMO (1/2)")));
        assertSame(ItemDB.lookup("STREAK SRM AMMO (1/2)"),
                   artemis.upgrade((Ammunition) ItemDB.lookup("STREAK SRM AMMO (1/2)")));
        assertSame(ItemDB.lookup("SRM AMMO + ART. IV (1/2)"),
                   artemis.upgrade((Ammunition) ItemDB.lookup("SRM AMMO (1/2)")));

        // Artemis -> Standard
        assertSame(ItemDB.lookup("C-LRM AMMO"), standard.upgrade((Ammunition) ItemDB.lookup("C-LRM AMMO + ART. IV")));
        assertSame(ItemDB.lookup("C-NARC AMMO"), standard.upgrade((Ammunition) ItemDB.lookup("C-NARC AMMO")));
        assertSame(ItemDB.lookup("C-S-SRM AMMO"), standard.upgrade((Ammunition) ItemDB.lookup("C-S-SRM AMMO")));
        assertSame(ItemDB.lookup("C-SRM AMMO"), standard.upgrade((Ammunition) ItemDB.lookup("C-SRM AMMO + ART. IV")));

        assertSame(ItemDB.lookup("C-LRM AMMO (1/2)"),
                   standard.upgrade((Ammunition) ItemDB.lookup("C-LRM AMMO+ART. IV (1/2)")));
        assertSame(ItemDB.lookup("C-NARC AMMO (1/2)"),
                   standard.upgrade((Ammunition) ItemDB.lookup("C-NARC AMMO (1/2)")));
        assertSame(ItemDB.lookup("C-S-SRM AMMO (1/2)"),
                   standard.upgrade((Ammunition) ItemDB.lookup("C-S-SRM AMMO (1/2)")));
        assertSame(ItemDB.lookup("C-SRM AMMO (1/2)"),
                   standard.upgrade((Ammunition) ItemDB.lookup("C-SRM AMMO+ART. IV (1/2)")));

        assertSame(ItemDB.lookup("LRM AMMO"), standard.upgrade((Ammunition) ItemDB.lookup("LRM AMMO + ARTEMIS IV")));
        assertSame(ItemDB.lookup("NARC AMMO"), standard.upgrade((Ammunition) ItemDB.lookup("NARC AMMO")));
        assertSame(ItemDB.lookup("STREAK SRM AMMO"), standard.upgrade((Ammunition) ItemDB.lookup("STREAK SRM AMMO")));
        assertSame(ItemDB.lookup("SRM AMMO"), standard.upgrade((Ammunition) ItemDB.lookup("SRM AMMO + ARTEMIS IV")));

        assertSame(ItemDB.lookup("LRM AMMO (1/2)"),
                   standard.upgrade((Ammunition) ItemDB.lookup("LRM AMMO + ARTEMIS IV (1/2)")));
        assertSame(ItemDB.lookup("NARC AMMO (1/2)"), standard.upgrade((Ammunition) ItemDB.lookup("NARC AMMO (1/2)")));
        assertSame(ItemDB.lookup("STREAK SRM AMMO (1/2)"),
                   standard.upgrade((Ammunition) ItemDB.lookup("STREAK SRM AMMO (1/2)")));
        assertSame(ItemDB.lookup("SRM AMMO (1/2)"),
                   standard.upgrade((Ammunition) ItemDB.lookup("SRM AMMO + ART. IV (1/2)")));
    }
}
