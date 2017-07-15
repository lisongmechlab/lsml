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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.messages.ArmourMessage;
import org.lisoft.lsml.messages.ArmourMessage.Type;
import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.database.ChassisDB;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.util.CommandStack;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

/**
 * Test suite for {@link CmdLoadStock}.
 *
 * @author Li Song
 */
@SuppressWarnings("javadoc")
@RunWith(JUnitParamsRunner.class)
public class CmdLoadStockTest {
    private static final Set<Chassis> PGI_BROKE_ME = new HashSet<>(Arrays.asList(ChassisDB.lookup("ZEU-SK")));
    private MessageXBar xBar;

    private final LoadoutFactory loadoutFactory = new DefaultLoadoutFactory();

    public Object[] allChassis() {
        final List<Chassis> chassii = new ArrayList<>();
        chassii.addAll(ChassisDB.lookup(ChassisClass.LIGHT));
        chassii.addAll(ChassisDB.lookup(ChassisClass.MEDIUM));
        chassii.addAll(ChassisDB.lookup(ChassisClass.HEAVY));
        chassii.addAll(ChassisDB.lookup(ChassisClass.ASSAULT));
        return chassii.stream().filter(c -> !PGI_BROKE_ME.contains(c)).collect(Collectors.toList()).toArray();
    }

    @Before
    public void setup() {
        xBar = mock(MessageXBar.class);
    }

    /**
     * Loading stock configuration shall produce a complete loadout for all chassis
     *
     * @param aChassis
     *            Chassis to test on.
     * @throws Exception
     */
    @Test
    @Parameters(method = "allChassis")
    public void testApply(Chassis aChassis) throws Exception {
        // Setup
        final Loadout loadout = loadoutFactory.produceEmpty(aChassis);

        // Execute
        final CommandStack opstack = new CommandStack(0);
        opstack.pushAndApply(new CmdLoadStock(aChassis, loadout, xBar));

        // Verify (What the hell is up with the misery's stock loadout with almost one ton free mass and not full
        // armour?!)
        assertTrue(loadout.getFreeMass() < 0.5 || loadout.getName().contains("STK-M") && loadout.getFreeMass() < 1);
        for (final ConfiguredComponent part : loadout.getComponents()) {
            verify(xBar, atLeast(1)).post(new ArmourMessage(part, Type.ARMOUR_CHANGED, true));
        }
        verify(xBar, atLeast(1)).post(isA(ItemMessage.class));
    }

    /**
     * Actuator state shall be set on arms for OmniMechs.
     *
     * @throws Exception
     */
    @Test
    public void testApply_ActuatorState() throws Exception {
        // Setup
        final LoadoutOmniMech loadout = (LoadoutOmniMech) loadoutFactory.produceEmpty(ChassisDB.lookup("SCR-PRIME(S)"));

        // Execute
        final CommandStack opstack = new CommandStack(0);
        opstack.pushAndApply(new CmdLoadStock(loadout.getChassis(), loadout, xBar));

        assertFalse(loadout.getComponent(Location.LeftArm).getToggleState(ItemDB.HA));
        assertFalse(loadout.getComponent(Location.LeftArm).getToggleState(ItemDB.LAA));
        assertTrue(loadout.getComponent(Location.RightArm).getToggleState(ItemDB.HA));
        assertTrue(loadout.getComponent(Location.RightArm).getToggleState(ItemDB.LAA));
    }

    /**
     * Loading stock shall handle Artemis changes on February 4th patch.
     *
     * @throws Exception
     */
    @Test
    public void testApply_artemisFeb4() throws Exception {
        // Setup
        final Loadout loadout = loadoutFactory.produceEmpty(ChassisDB.lookup("CN9-D"));

        // Execute
        final CommandStack opstack = new CommandStack(0);
        opstack.pushAndApply(new CmdLoadStock(loadout.getChassis(), loadout, xBar));

        assertTrue(loadout.getComponent(Location.LeftTorso).getItemsEquipped()
                .contains(ItemDB.lookup("LRM 10 + ARTEMIS")));
    }

    /**
     * Loading stock shall succeed even if there is an automatic armour distribution going on.
     *
     * @throws Exception
     */
    @Test
    public void testApply_InPresenceOfAutomaticArmour() throws Exception {
        // Setup
        final Loadout loadout = loadoutFactory.produceStock(ChassisDB.lookup("BNC-3S"));
        final CommandStack stack = new CommandStack(0);

        doAnswer(aInvocation -> {
            final Message aMsg = (Message) aInvocation.getArguments()[0];
            if (aMsg.isForMe(loadout) && aMsg instanceof ArmourMessage) {
                final ArmourMessage message = (ArmourMessage) aMsg;
                if (!message.manualArmour) {
                    return null;
                }
                stack.pushAndApply(new CmdDistributeArmour(loadout, loadout.getChassis().getArmourMax(), 10, xBar));
            }
            return null;
        }).when(xBar).post(any(ArmourMessage.class));

        // Execute
        final CmdLoadStock cut = new CmdLoadStock(loadout.getChassis(), loadout, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(95.0, loadout.getMass(), 0.01);
        assertEquals(480, loadout.getArmour());
    }

    /**
     * Loading stock configuration shall succeed even if the loadout isn't empty to start with.
     *
     * @throws Exception
     */
    @Test
    public void testNotEmpty() throws Exception {
        // Setup
        final ChassisStandard chassi = (ChassisStandard) ChassisDB.lookup("JR7-F");
        final Loadout loadout = loadoutFactory.produceStock(chassi);
        final CommandStack opstack = new CommandStack(0);
        assertTrue(loadout.getMass() > 34.9);

        // Execute
        opstack.pushAndApply(new CmdLoadStock(chassi, loadout, xBar));
    }

    /**
     * Undoing load stock shall produce previous loadout.
     *
     * @throws Exception
     */
    @Test
    public void testUndo() throws Exception {
        // Setup
        final ChassisStandard chassi = (ChassisStandard) ChassisDB.lookup("JR7-F");
        final LoadoutStandard reference = (LoadoutStandard) loadoutFactory.produceEmpty(chassi);
        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(chassi);
        final CommandStack opstack = new CommandStack(1);
        opstack.pushAndApply(new CmdLoadStock(loadout.getChassis(), loadout, xBar));

        // Execute
        opstack.undo();

        // Verify
        assertEquals(reference, loadout);
    }

    /**
     * Loading stock configuration shall succeed even if the loadout as armour set.
     *
     * @throws Exception
     */
    @Test
    public void testWithArmour() throws Exception {
        // Setup
        final ChassisStandard chassi = (ChassisStandard) ChassisDB.lookup("LCT-3S");
        final Loadout loadout = loadoutFactory.produceEmpty(chassi);

        new CmdSetMaxArmour(loadout, null, 4.0, false).apply();

        // Execute
        new CmdLoadStock(chassi, loadout, xBar).apply();

        assertTrue(loadout.getMass() > 19.8);
    }
}
