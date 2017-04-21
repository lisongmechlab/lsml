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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.messages.ArmourMessage;
import org.lisoft.lsml.messages.ArmourMessage.Type;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.ArmourSide;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.Command;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link CmdSetArmourSymmetric}.
 *
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.class)
public class CmdSetArmourSymmetricTest {
    @Mock
    MessageXBar xBar;

    private final CommandStack stack = new CommandStack(2);
    private final LoadoutFactory loadoutFactory = new DefaultLoadoutFactory();

    @Test
    public void testApply() throws Exception {
        final Loadout loadout = loadoutFactory.produceEmpty(ChassisDB.lookup("AS7-D-DC"));
        final ConfiguredComponent left = loadout.getComponent(Location.LeftTorso);
        final ConfiguredComponent right = loadout.getComponent(Location.RightTorso);
        final ArmourSide side = ArmourSide.BACK;
        final int amount = 40;
        final boolean manual = true;
        final CmdSetArmourSymmetric cut = new CmdSetArmourSymmetric(xBar, loadout, left, side, amount, manual);
        Mockito.reset(xBar);

        stack.pushAndApply(cut);

        assertTrue(left.hasManualArmour());
        assertTrue(right.hasManualArmour());
        assertEquals(amount, left.getArmour(side));
        assertEquals(amount, right.getArmour(side));
        Mockito.verify(xBar).post(new ArmourMessage(left, Type.ARMOUR_CHANGED, true));
        Mockito.verify(xBar).post(new ArmourMessage(right, Type.ARMOUR_CHANGED, true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testApply_NotSymmetric() throws Exception {
        final Loadout loadout = loadoutFactory.produceEmpty(ChassisDB.lookup("AS7-D-DC"));
        final ConfiguredComponent left = loadout.getComponent(Location.Head);
        final ArmourSide side = ArmourSide.BACK;
        final int amount = 40;
        final boolean manual = true;
        final CmdSetArmourSymmetric cut = new CmdSetArmourSymmetric(xBar, loadout, left, side, amount, manual);

        stack.pushAndApply(cut);
    }

    @Test
    public void testApply_OnlyOneSideChanges() throws Exception {
        for (final Location setSide : new Location[] { Location.LeftTorso, Location.RightTorso }) {
            final Loadout loadout = loadoutFactory.produceEmpty(ChassisDB.lookup("AS7-D-DC"));
            final ConfiguredComponent left = loadout.getComponent(Location.LeftTorso);
            final ConfiguredComponent right = loadout.getComponent(Location.RightTorso);
            final ArmourSide side = ArmourSide.BACK;
            final int amount = 40;
            final boolean manual = true;
            stack.pushAndApply(new CmdSetArmour(null, loadout, loadout.getComponent(setSide), side, amount, false));
            final CmdSetArmourSymmetric cut = new CmdSetArmourSymmetric(xBar, loadout, left, side, amount, manual);
            Mockito.reset(xBar);

            stack.pushAndApply(cut);

            assertTrue(left.hasManualArmour());
            assertTrue(right.hasManualArmour());
            assertEquals(amount, left.getArmour(side));
            assertEquals(amount, right.getArmour(side));
            Mockito.verify(xBar).post(new ArmourMessage(left, Type.ARMOUR_CHANGED, true));
            Mockito.verify(xBar).post(new ArmourMessage(right, Type.ARMOUR_CHANGED, true));
        }
    }

    /**
     * Two operations can coalescele if they refer to the same (equality is not enough) component or the opposing
     * component, same side and have the same manual status.
     */
    @Test
    public void testCanCoalescele() {
        final Loadout loadout = loadoutFactory.produceEmpty(ChassisDB.lookup("AS7-D-DC"));
        final ConfiguredComponent left = loadout.getComponent(Location.LeftTorso);
        final ConfiguredComponent right = loadout.getComponent(Location.RightTorso);
        final ConfiguredComponent arm = loadout.getComponent(Location.LeftArm);
        final int amount = 40;

        final CmdSetArmourSymmetric cut1 = new CmdSetArmourSymmetric(xBar, loadout, left, ArmourSide.BACK, amount,
                true);
        final CmdSetArmourSymmetric cut2 = new CmdSetArmourSymmetric(xBar, loadout, left, ArmourSide.BACK, amount,
                false);
        final CmdSetArmourSymmetric cut3 = new CmdSetArmourSymmetric(xBar, loadout, left, ArmourSide.BACK, amount - 1,
                true);
        final CmdSetArmourSymmetric cut4 = new CmdSetArmourSymmetric(xBar, loadout, left, ArmourSide.FRONT, amount,
                true);
        final CmdSetArmourSymmetric cut5 = new CmdSetArmourSymmetric(xBar, loadout, right, ArmourSide.BACK, amount,
                true);
        final CmdSetArmourSymmetric cut6 = new CmdSetArmourSymmetric(xBar, loadout, arm, ArmourSide.BACK, amount, true);
        final Command operation = Mockito.mock(Command.class);

        assertFalse(cut1.canCoalescele(operation)); // Wrong class
        assertFalse(cut1.canCoalescele(null)); // Null
        assertFalse(cut1.canCoalescele(cut1)); // Can't coalescele with self.
        assertFalse(cut1.canCoalescele(cut2)); // manual-ness
        assertTrue(cut1.canCoalescele(cut3)); // armour amount
        assertFalse(cut1.canCoalescele(cut4)); // Side of part
        assertTrue(cut1.canCoalescele(cut5)); // opposite part
        assertFalse(cut1.canCoalescele(cut6)); // Other part
    }
}
