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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.messages.ComponentMessage;
import org.lisoft.lsml.messages.ComponentMessage.Type;
import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.messages.UpgradesMessage;
import org.lisoft.lsml.messages.UpgradesMessage.ChangeMsg;
import org.lisoft.lsml.model.chassi.ChassisBase;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.util.CommandStack;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

/**
 * Test suite for {@link CmdLoadStock}.
 * 
 * @author Emily Björk
 */
@RunWith(JUnitParamsRunner.class)
public class CmdLoadStockTest {
    private MessageXBar xBar;

    @Before
    public void setup() {
        xBar = mock(MessageXBar.class);
    }

    /**
     * Loading stock configuration shall succeed even if the loadout isn't empty to start with.
     * 
     * @throws Exception
     */
    @Test
    public void testNotEmpty() throws Exception {
        // Setup
        ChassisStandard chassi = (ChassisStandard) ChassisDB.lookup("JR7-F");
        LoadoutBase<?> loadout = DefaultLoadoutFactory.instance.produceStock(chassi);
        CommandStack opstack = new CommandStack(0);
        assertTrue(loadout.getMass() > 34.9);

        // Execute
        opstack.pushAndApply(new CmdLoadStock(chassi, loadout, xBar));
    }

    public Object[] allChassis() {
        List<ChassisBase> chassii = new ArrayList<>();
        chassii.addAll(ChassisDB.lookup(ChassisClass.LIGHT));
        chassii.addAll(ChassisDB.lookup(ChassisClass.MEDIUM));
        chassii.addAll(ChassisDB.lookup(ChassisClass.HEAVY));
        chassii.addAll(ChassisDB.lookup(ChassisClass.ASSAULT));
        return chassii.toArray();
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
    public void testApply(ChassisBase aChassis) throws Exception {
        // Setup
        final LoadoutBase<?> loadout = DefaultLoadoutFactory.instance.produceEmpty(aChassis);

        // Execute
        CommandStack opstack = new CommandStack(0);
        opstack.pushAndApply(new CmdLoadStock(aChassis, loadout, xBar));

        // Verify (What the hell is up with the misery's stock loadout with almost one ton free mass and not full
        // armor?!)
        assertTrue(loadout.getFreeMass() < 0.5 || (loadout.getName().contains("STK-M") && loadout.getFreeMass() < 1));
        for (ConfiguredComponentBase part : loadout.getComponents()) {
            verify(xBar, atLeast(1)).post(new ComponentMessage(part, Type.ArmorChanged, true));
        }
        verify(xBar, atLeast(1)).post(any(ItemMessage.class));
        verify(xBar, atLeast(1)).post(new UpgradesMessage(Matchers.any(ChangeMsg.class), loadout.getUpgrades()));
    }

    /**
     * Loading stock shall handle Artemis changes on February 4th patch.
     * 
     * @throws Exception
     */
    @Test
    public void testApply_artemisFeb4() throws Exception {
        // Setup
        LoadoutBase<?> loadout = DefaultLoadoutFactory.instance.produceEmpty(ChassisDB.lookup("CN9-D"));

        // Execute
        CommandStack opstack = new CommandStack(0);
        opstack.pushAndApply(new CmdLoadStock(loadout.getChassis(), loadout, xBar));

        assertTrue(loadout.getComponent(Location.LeftTorso).getItemsEquipped()
                .contains(ItemDB.lookup("LRM 10 + ARTEMIS")));
    }

    /**
     * Actuator state shall be set on arms for OmniMechs.
     * 
     * @throws Exception
     */
    @Test
    public void testApply_ActuatorState() throws Exception {
        // Setup
        LoadoutOmniMech loadout = (LoadoutOmniMech) DefaultLoadoutFactory.instance
                .produceEmpty(ChassisDB.lookup("SCR-PRIME(S)"));

        // Execute
        CommandStack opstack = new CommandStack(0);
        opstack.pushAndApply(new CmdLoadStock(loadout.getChassis(), loadout, xBar));

        assertFalse(loadout.getComponent(Location.LeftArm).getToggleState(ItemDB.HA));
        assertFalse(loadout.getComponent(Location.LeftArm).getToggleState(ItemDB.LAA));
        assertTrue(loadout.getComponent(Location.RightArm).getToggleState(ItemDB.HA));
        assertTrue(loadout.getComponent(Location.RightArm).getToggleState(ItemDB.LAA));
    }

    /**
     * Undoing load stock shall produce previous loadout.
     * 
     * @throws Exception
     */
    @Test
    public void testUndo() throws Exception {
        // Setup
        ChassisStandard chassi = (ChassisStandard) ChassisDB.lookup("JR7-F");
        LoadoutStandard reference = (LoadoutStandard) DefaultLoadoutFactory.instance.produceEmpty(chassi);
        LoadoutStandard loadout = (LoadoutStandard) DefaultLoadoutFactory.instance.produceEmpty(chassi);
        CommandStack opstack = new CommandStack(1);
        opstack.pushAndApply(new CmdLoadStock(loadout.getChassis(), loadout, xBar));

        // Execute
        opstack.undo();

        // Verify
        assertEquals(reference, loadout);
    }

    /**
     * Loading stock shall succeed even if there is an automatic armor distribution going on.
     * 
     * @throws Exception
     */
    @Test
    public void testApply_InPresenceOfAutomaticArmor() throws Exception {
        // Setup
        final LoadoutBase<?> loadout = DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("BNC-3S"));
        final CommandStack stack = new CommandStack(0);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock aInvocation) throws Throwable {
                Message aMsg = (Message) aInvocation.getArguments()[0];
                if (aMsg.isForMe(loadout) && aMsg instanceof ComponentMessage) {
                    ComponentMessage message = (ComponentMessage) aMsg;
                    if (!message.manualArmor)
                        return null;
                    stack.pushAndApply(new CmdDistributeArmor(loadout, loadout.getChassis().getArmorMax(), 10, xBar));
                }
                return null;
            }
        }).when(xBar).post(Matchers.any(ComponentMessage.class));

        // Execute
        CmdLoadStock cut = new CmdLoadStock(loadout.getChassis(), loadout, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(95.0, loadout.getMass(), 0.01);
        assertEquals(480, loadout.getArmor());
    }
}
