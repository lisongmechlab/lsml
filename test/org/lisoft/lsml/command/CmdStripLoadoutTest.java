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
package org.lisoft.lsml.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.messages.ArmorMessage;
import org.lisoft.lsml.messages.ArmorMessage.Type;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.export.Base64LoadoutCoder;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.util.CommandStack;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link CmdStripLoadout}.
 * 
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.class)
public class CmdStripLoadoutTest {

    @Mock
    private MessageDelivery messageDelivery;

    /**
     * Stripping a loadout shall remove all upgrades, items and armor.
     * @throws Exception 
     */
    @Test
    public void testStrip_OmniMech() throws Exception {
        // Setup
        LoadoutOmniMech cut = (LoadoutOmniMech) DefaultLoadoutFactory.instance
                .produceStock(ChassisDB.lookup("TBR-PRIME"));
        cut.getUpgrades().setGuidance(UpgradeDB.ARTEMIS_IV);

        assertTrue(cut.getMass() > 59.0);

        // Execute
        CommandStack opStack = new CommandStack(0);
        opStack.pushAndApply(new CmdStripLoadout(cut, messageDelivery));

        // Verify
        for (ConfiguredComponentBase loadoutPart : cut.getComponents()) {
            if (loadoutPart.getInternalComponent().getLocation() == Location.CenterTorso) {
                assertEquals(31.5, loadoutPart.getItemMass(), 0.0);
            }
            else {
                assertEquals(0.0, loadoutPart.getItemMass(), 0.0);
            }
            assertEquals(0, loadoutPart.getArmorTotal());
        }
        assertEquals(UpgradeDB.STD_GUIDANCE, cut.getUpgrades().getGuidance());
        assertEquals(cut.getChassis().getFixedStructureType(), cut.getUpgrades().getStructure());
        assertEquals(cut.getChassis().getFixedArmorType(), cut.getUpgrades().getArmor());
        assertEquals(cut.getChassis().getFixedHeatSinkType(), cut.getUpgrades().getHeatSink());
    }

    /**
     * Stripping a loadout shall remove all upgrades, items and armor.
     * 
     * @throws Exception
     */
    @Test
    public void testStrip() throws Exception {
        // Setup
        LoadoutBase<?> cut = DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("AS7-BH"));
        // Has Endo-Steel standard and lots of stuff

        assertTrue(cut.getMass() > 99.0);

        // Execute
        CommandStack opStack = new CommandStack(0);
        opStack.pushAndApply(new CmdStripLoadout(cut, messageDelivery));

        // Verify
        for (ConfiguredComponentBase loadoutPart : cut.getComponents()) {
            assertEquals(0.0, loadoutPart.getItemMass(), 0.0);
            assertEquals(0, loadoutPart.getArmorTotal());
        }
        assertEquals(UpgradeDB.STD_GUIDANCE, cut.getUpgrades().getGuidance());
        assertEquals(UpgradeDB.IS_STD_STRUCTURE, cut.getUpgrades().getStructure());
        assertEquals(UpgradeDB.IS_STD_ARMOR, cut.getUpgrades().getArmor());
        assertEquals(UpgradeDB.IS_SHS, cut.getUpgrades().getHeatSink());
    }

    @Test
    public void testStripMech() throws Exception {
        Base64LoadoutCoder coder = new Base64LoadoutCoder();
        LoadoutBase<?> loadout = coder.parse("lsml://rR4AEURNB1QScQtNB1REvqCEj9P37332SAXGzly5WoqI0fyo");
        LoadoutBase<?> loadoutOriginal = DefaultLoadoutFactory.instance.produceClone(loadout);
        CommandStack stack = new CommandStack(1);

        stack.pushAndApply(new CmdStripLoadout(loadout, messageDelivery));

        assertEquals(loadout.getMass(), loadout.getChassis().getMassMax() * 0.1, 0.0);
        assertSame(UpgradeDB.IS_STD_ARMOR, loadout.getUpgrades().getArmor());
        assertSame(UpgradeDB.IS_STD_STRUCTURE, loadout.getUpgrades().getStructure());
        assertSame(UpgradeDB.STD_GUIDANCE, loadout.getUpgrades().getGuidance());
        assertSame(UpgradeDB.IS_SHS, loadout.getUpgrades().getHeatSink());

        stack.undo();

        assertEquals(loadoutOriginal, loadout);
    }

    /**
     * The strip operation shall leave armor untouched if requested.
     * @throws Exception 
     */
    @Test
    public void testStrip_LeaveArmor() throws Exception {
        // Setup
        LoadoutBase<?> cut = DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("AS7-BH"));
        // Has Endo-Steel standard and lots of stuff

        assertTrue(cut.getMass() > 99.0);

        // Execute
        CommandStack opStack = new CommandStack(0);
        opStack.pushAndApply(new CmdStripLoadout(cut, messageDelivery, false));

        // Verify
        for (ConfiguredComponentBase loadoutPart : cut.getComponents()) {
            assertEquals(0.0, loadoutPart.getItemMass(), 0.0);
            assertNotEquals(0, loadoutPart.getArmorTotal());
        }
        assertEquals(UpgradeDB.STD_GUIDANCE, cut.getUpgrades().getGuidance());
        assertEquals(UpgradeDB.IS_STD_STRUCTURE, cut.getUpgrades().getStructure());
        assertEquals(UpgradeDB.IS_STD_ARMOR, cut.getUpgrades().getArmor());
        assertEquals(UpgradeDB.IS_SHS, cut.getUpgrades().getHeatSink());

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageDelivery, atLeastOnce()).post(messageCaptor.capture());
        for (Message message : messageCaptor.getAllValues()) {
            if (message instanceof ArmorMessage) {
                ArmorMessage componentMessage = (ArmorMessage) message;
                assertNotEquals(Type.ARMOR_CHANGED, componentMessage.type);
            }
        }
    }
}
