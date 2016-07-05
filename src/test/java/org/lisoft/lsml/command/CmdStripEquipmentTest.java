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
package org.lisoft.lsml.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.export.Base64LoadoutCoder;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.util.CommandStack;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link CmdStripEquipment}.
 *
 * @author Emily Björk
 */
@SuppressWarnings("javadoc")
@RunWith(MockitoJUnitRunner.class)
public class CmdStripEquipmentTest {

    @Mock
    private MessageDelivery messageDelivery;

    /**
     * Stripping a loadout shall remove all upgrades, items and armour.
     *
     * @throws Exception
     */
    @Test
    public void testStrip() throws Exception {
        // Setup
        final Loadout cut = DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("AS7-BH"));
        // Has Endo-Steel standard and lots of stuff

        assertTrue(cut.getMass() > 99.0);

        // Execute
        final CommandStack opStack = new CommandStack(0);
        opStack.pushAndApply(new CmdStripEquipment(cut, messageDelivery));

        // Verify
        for (final ConfiguredComponent loadoutPart : cut.getComponents()) {
            assertEquals(0.0, loadoutPart.getItemMass(), 0.0);
        }
    }

    /**
     * Stripping a loadout shall remove all upgrades, items and armour.
     *
     * @throws Exception
     */
    @Test
    public void testStrip_OmniMech() throws Exception {
        // Setup
        final LoadoutOmniMech cut = (LoadoutOmniMech) DefaultLoadoutFactory.instance
                .produceStock(ChassisDB.lookup("TBR-PRIME"));
        cut.getUpgrades().setGuidance(UpgradeDB.ARTEMIS_IV);

        assertTrue(cut.getMass() > 59.0);

        // Execute
        final CommandStack opStack = new CommandStack(0);
        opStack.pushAndApply(new CmdStripEquipment(cut, messageDelivery));

        // Verify
        for (final ConfiguredComponent loadoutPart : cut.getComponents()) {
            if (loadoutPart.getInternalComponent().getLocation() == Location.CenterTorso) {
                assertEquals(31.5, loadoutPart.getItemMass(), 0.0);
            }
            else {
                assertEquals(0.0, loadoutPart.getItemMass(), 0.0);
            }
        }
    }

    @Test
    public void testStripMech() throws Exception {
        final Base64LoadoutCoder coder = new Base64LoadoutCoder(null);
        final Loadout loadout = coder.parse("lsml://rR4AEURNB1QScQtNB1REvqCEj9P37332SAXGzly5WoqI0fyo");
        final Loadout loadoutOriginal = DefaultLoadoutFactory.instance.produceClone(loadout);
        loadoutOriginal.setName(loadout.getName());
        final CommandStack stack = new CommandStack(1);

        stack.pushAndApply(new CmdStripEquipment(loadout, messageDelivery));

        final double expected = loadout.getUpgrades().getStructure().getStructureMass(loadout.getChassis())
                + loadout.getUpgrades().getArmour().getArmourMass(loadout.getArmour());
        assertEquals(expected, loadout.getMass(), 0.0);

        stack.undo();

        assertEquals(loadoutOriginal, loadout);
    }
}
