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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.messages.ArmorMessage;
import org.lisoft.lsml.messages.ArmorMessage.Type;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.Command;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link CmdSetArmorSymmetric}.
 * 
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.class)
public class CmdSetArmorSymmetricTest {
    @Mock
    MessageXBar    xBar;

    CommandStack stack = new CommandStack(2);

    /**
     * Two operations can coalescele if they refer to the same (equality is not enough) component or the opposing
     * component, same side and have the same manual status.
     */
    @Test
    public void testCanCoalescele() {
        LoadoutBase<?> loadout = DefaultLoadoutFactory.instance.produceEmpty(ChassisDB.lookup("AS7-D-DC"));
        ConfiguredComponentBase left = loadout.getComponent(Location.LeftTorso);
        ConfiguredComponentBase right = loadout.getComponent(Location.RightTorso);
        ConfiguredComponentBase arm = loadout.getComponent(Location.LeftArm);
        int amount = 40;

        CmdSetArmorSymmetric cut1 = new CmdSetArmorSymmetric(xBar, loadout, left, ArmorSide.BACK, amount, true);
        CmdSetArmorSymmetric cut2 = new CmdSetArmorSymmetric(xBar, loadout, left, ArmorSide.BACK, amount, false);
        CmdSetArmorSymmetric cut3 = new CmdSetArmorSymmetric(xBar, loadout, left, ArmorSide.BACK, amount - 1, true);
        CmdSetArmorSymmetric cut4 = new CmdSetArmorSymmetric(xBar, loadout, left, ArmorSide.FRONT, amount, true);
        CmdSetArmorSymmetric cut5 = new CmdSetArmorSymmetric(xBar, loadout, right, ArmorSide.BACK, amount, true);
        CmdSetArmorSymmetric cut6 = new CmdSetArmorSymmetric(xBar, loadout, arm, ArmorSide.BACK, amount, true);
        Command operation = Mockito.mock(Command.class);

        assertFalse(cut1.canCoalescele(operation)); // Wrong class
        assertFalse(cut1.canCoalescele(null)); // Null
        assertFalse(cut1.canCoalescele(cut1)); // Can't coalescele with self.
        assertFalse(cut1.canCoalescele(cut2)); // manual-ness
        assertTrue(cut1.canCoalescele(cut3)); // armor amount
        assertFalse(cut1.canCoalescele(cut4)); // Side of part
        assertTrue(cut1.canCoalescele(cut5)); // opposite part
        assertFalse(cut1.canCoalescele(cut6)); // Other part
    }

    @Test
    public void testApply() throws Exception {
        LoadoutBase<?> loadout = DefaultLoadoutFactory.instance.produceEmpty(ChassisDB.lookup("AS7-D-DC"));
        ConfiguredComponentBase left = loadout.getComponent(Location.LeftTorso);
        ConfiguredComponentBase right = loadout.getComponent(Location.RightTorso);
        ArmorSide side = ArmorSide.BACK;
        int amount = 40;
        boolean manual = true;
        CmdSetArmorSymmetric cut = new CmdSetArmorSymmetric(xBar, loadout, left, side, amount, manual);
        Mockito.reset(xBar);

        stack.pushAndApply(cut);

        assertTrue(left.hasManualArmor());
        assertTrue(right.hasManualArmor());
        assertEquals(amount, left.getArmor(side));
        assertEquals(amount, right.getArmor(side));
        Mockito.verify(xBar).post(new ArmorMessage(left, Type.ARMOR_CHANGED, true));
        Mockito.verify(xBar).post(new ArmorMessage(right, Type.ARMOR_CHANGED, true));
    }

    @Test
    public void testApply_OnlyOneSideChanges() throws Exception {
        for (Location setSide : new Location[] { Location.LeftTorso, Location.RightTorso }) {
            LoadoutBase<?> loadout = DefaultLoadoutFactory.instance.produceEmpty( ChassisDB.lookup("AS7-D-DC"));
            ConfiguredComponentBase left = loadout.getComponent(Location.LeftTorso);
            ConfiguredComponentBase right = loadout.getComponent(Location.RightTorso);
            ArmorSide side = ArmorSide.BACK;
            int amount = 40;
            boolean manual = true;
            stack.pushAndApply(new CmdSetArmor(null, loadout, loadout.getComponent(setSide), side, amount, false));
            CmdSetArmorSymmetric cut = new CmdSetArmorSymmetric(xBar, loadout, left, side, amount, manual);
            Mockito.reset(xBar);

            stack.pushAndApply(cut);

            assertTrue(left.hasManualArmor());
            assertTrue(right.hasManualArmor());
            assertEquals(amount, left.getArmor(side));
            assertEquals(amount, right.getArmor(side));
            Mockito.verify(xBar).post(new ArmorMessage(left, Type.ARMOR_CHANGED, true));
            Mockito.verify(xBar).post(new ArmorMessage(right, Type.ARMOR_CHANGED, true));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testApply_NotSymmetric() throws Exception {
        LoadoutBase<?> loadout = DefaultLoadoutFactory.instance.produceEmpty(ChassisDB.lookup("AS7-D-DC"));
        ConfiguredComponentBase left = loadout.getComponent(Location.Head);
        ArmorSide side = ArmorSide.BACK;
        int amount = 40;
        boolean manual = true;
        CmdSetArmorSymmetric cut = new CmdSetArmorSymmetric(xBar, loadout, left, side, amount, manual);

        stack.pushAndApply(cut);
    }
}
