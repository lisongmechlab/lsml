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
package lisong_mechlab.model.loadout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.ChassisClass;
import lisong_mechlab.model.chassi.ChassisDB;
import lisong_mechlab.model.chassi.ChassisOmniMech;
import lisong_mechlab.model.chassi.ChassisStandard;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.component.ComponentBuilder;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase.ComponentMessage.Type;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.model.upgrades.Upgrades.UpgradesMessage.ChangeMsg;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.util.message.Message;
import lisong_mechlab.util.message.MessageXBar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test suite for {@link OpLoadStock}.
 * 
 * @author Emily Björk
 */
@RunWith(JUnitParamsRunner.class)
public class OpLoadStockTest {
    private MessageXBar xBar;

    @Before
    public void setup() {
        xBar = Mockito.mock(MessageXBar.class);
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
        LoadoutStandard loadout = new LoadoutStandard(chassi);
        OperationStack opstack = new OperationStack(0);
        opstack.pushAndApply(new OpLoadStock(chassi, loadout, xBar));

        assertTrue(loadout.getMass() > 34.9);

        // Execute
        opstack.pushAndApply(new OpLoadStock(chassi, loadout, xBar));
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
        final LoadoutBase<?> loadout;
        if (aChassis instanceof ChassisStandard) {
            loadout = new LoadoutStandard((ChassisStandard) aChassis);
        }
        else if (aChassis instanceof ChassisOmniMech) {
            loadout = new LoadoutOmniMech(ComponentBuilder.getOmniComponentFactory(), (ChassisOmniMech) aChassis);
        }
        else {
            fail("Unknown chassis type");
            return;
        }

        // Execute
        OperationStack opstack = new OperationStack(0);
        opstack.pushAndApply(new OpLoadStock(aChassis, loadout, xBar));

        // Verify (What the hell is up with the misery's stock loadout with almost one ton free mass and not full
        // armor?!)
        assertTrue(loadout.getFreeMass() < 0.5 || (loadout.getName().contains("STK-M") && loadout.getFreeMass() < 1));
        for (ConfiguredComponentBase part : loadout.getComponents()) {
            Mockito.verify(xBar, Mockito.atLeast(1)).post(
                    new ConfiguredComponentBase.ComponentMessage(part, Type.ArmorChanged));
        }
        Mockito.verify(xBar, Mockito.atLeast(1)).post(
                new ConfiguredComponentBase.ComponentMessage(Matchers.any(ConfiguredComponentBase.class),
                        Type.ItemAdded));
        Mockito.verify(xBar, Mockito.atLeast(1)).post(
                new Upgrades.UpgradesMessage(Matchers.any(ChangeMsg.class), loadout.getUpgrades()));
    }

    /**
     * Loading stock shall handle artemis changes on February 4th patch.
     * 
     * @throws Exception
     */
    @Test
    public void testApply_artemisFeb4() throws Exception {
        // Setup
        LoadoutStandard loadout = new LoadoutStandard((ChassisStandard) ChassisDB.lookup("CN9-D"));

        // Execute
        OperationStack opstack = new OperationStack(0);
        opstack.pushAndApply(new OpLoadStock(loadout.getChassis(), loadout, xBar));

        assertTrue(loadout.getComponent(Location.LeftTorso).getItemsEquipped()
                .contains(ItemDB.lookup("LRM 10 + ARTEMIS")));
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
        LoadoutStandard reference = new LoadoutStandard(chassi);
        LoadoutStandard loadout = new LoadoutStandard(chassi);
        OperationStack opstack = new OperationStack(1);
        opstack.pushAndApply(new OpLoadStock(loadout.getChassis(), loadout, xBar));

        // Execute
        opstack.undo();

        // Verify
        assertEquals(reference, loadout);
    }

    /**
     * Loading stock shall succeed even if there is an automatic armor distribution thinggie going on.
     * 
     * @throws Exception
     */
    @Test
    public void testApply_InPresenceOfAutomaticArmor() throws Exception {
        // Setup
        final LoadoutBase<?> loadout = new LoadoutStandard("BNC-3S");
        final OperationStack stack = new OperationStack(0);

        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock aInvocation) throws Throwable {
                Message aMsg = (Message) aInvocation.getArguments()[0];
                if (aMsg.isForMe(loadout) && aMsg instanceof ConfiguredComponentBase.ComponentMessage) {
                    ConfiguredComponentBase.ComponentMessage message = (ConfiguredComponentBase.ComponentMessage) aMsg;
                    if (message.automatic)
                        return null;
                    stack.pushAndApply(new OpDistributeArmor(loadout, loadout.getChassis().getArmorMax(), 10, xBar));
                }
                return null;
            }
        }).when(xBar).post(Matchers.any(ConfiguredComponentBase.ComponentMessage.class));

        // Execute
        OpLoadStock cut = new OpLoadStock(loadout.getChassis(), loadout, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(95.0, loadout.getMass(), 0.01);
        assertEquals(480, loadout.getArmor());
    }
}
