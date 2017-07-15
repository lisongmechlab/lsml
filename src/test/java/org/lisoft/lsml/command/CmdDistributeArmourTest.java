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

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.messages.ArmourMessage;
import org.lisoft.lsml.messages.ArmourMessage.Type;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.ArmourSide;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.database.ChassisDB;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.TestHelpers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link CmdDistributeArmour}.
 *
 * @author Emily Björk
 */
@SuppressWarnings("javadoc")
@RunWith(MockitoJUnitRunner.class)
public class CmdDistributeArmourTest {
    @BeforeClass
    static public void setup() throws Exception {
        // Make sure everything is parsed so that we don't cause a timeout due to game-file parsing
        ItemDB.lookup("LRM 20");
        ChassisDB.lookup("AS7-D-DC");
    }

    @Mock
    private MessageXBar xBar;
    private final CommandStack stack = new CommandStack(0);
    private final LoadoutFactory loadoutFactory = new DefaultLoadoutFactory();

    /**
     * The operation shall succeed even if there is already max armour on the mech. (More on front parts than rear)
     *
     * @throws Exception
     */
    @Test
    public void testArmourDistributor_AlreadyMaxArmour_FrontRear() throws Exception {
        // Setup
        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(ChassisDB.lookup("HGN-733C"));
        // 90 tons, 9 tons internals
        stack.pushAndApply(new CmdSetMaxArmour(loadout, xBar, 5.0, false));

        // Execute (10.0 tons of armour)
        final CmdDistributeArmour cut = new CmdDistributeArmour(loadout, 32 * 10, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(9.0 + 10.0, loadout.getMass(), 0.0);
        assertEquals(320, loadout.getArmour());
    }

    /**
     * The operation shall succeed even if there is already max armour on the mech. (More on rear parts than front)
     *
     * @throws Exception
     */
    @Test
    public void testArmourDistributor_AlreadyMaxArmour_RearFront() throws Exception {
        // Setup
        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(ChassisDB.lookup("HGN-733C"));
        // 90 tons, 9 tons internals
        stack.pushAndApply(new CmdSetMaxArmour(loadout, xBar, 0.2, false));

        // Execute (10.0 tons of armour)
        final CmdDistributeArmour cut = new CmdDistributeArmour(loadout, 32 * 10, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(9.0 + 10.0, loadout.getMass(), 0.0);
        assertEquals(320, loadout.getArmour());
    }

    /**
     * Old armour values on automatically managed parts should be cleared
     *
     * @throws Exception
     */
    @Test
    public void testArmourDistributor_ClearOld() throws Exception {
        // Setup
        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(ChassisDB.lookup("CPLT-A1"));
        // 90 tons, 9 tons internals
        stack.pushAndApply(
                new CmdSetArmour(xBar, loadout, loadout.getComponent(Location.LeftArm), ArmourSide.ONLY, 2, false));
        stack.pushAndApply(
                new CmdSetArmour(xBar, loadout, loadout.getComponent(Location.RightArm), ArmourSide.ONLY, 2, false));

        // Execute (6.5 tons of armour)
        final CmdDistributeArmour cut = new CmdDistributeArmour(loadout, 208, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(6.5 + 6.5, loadout.getMass(), 0.0);
        assertEquals(208, loadout.getArmour());
    }

    /**
     * The operator shall always max CT if possible.
     *
     * @throws Exception
     */
    @Test
    public void testArmourDistributor_CT_Priority() throws Exception {
        // Setup
        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(ChassisDB.lookup("HGN-733C"));

        // Execute
        final CmdDistributeArmour cut = new CmdDistributeArmour(loadout, 110, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertTrue(loadout.getComponent(Location.CenterTorso).getArmourTotal() > 90);
    }

    /**
     * The operator shall succeed at placing the armour points somewhere on an empty loadout.
     *
     * @throws Exception
     */
    @Test
    public void testArmourDistributor_Distribute() throws Exception {
        // Setup
        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(ChassisDB.lookup("HGN-733C"));
        // 90 tons, 9 tons internals

        // Execute (10 tons of armour)
        final CmdDistributeArmour cut = new CmdDistributeArmour(loadout, 32 * 10, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(9.0 + 10, loadout.getMass(), 0.0);
        assertEquals(320, loadout.getArmour());
        Mockito.verify(xBar, Mockito.atLeastOnce())
                .post(new ArmourMessage(loadout.getComponent(Location.CenterTorso), Type.ARMOUR_CHANGED, false));
    }

    /**
     * Values that are even half tons shall not be rounded down.
     *
     * @throws Exception
     */
    @Test
    public void testArmourDistributor_EvenHalfNoRoundDown() throws Exception {
        // Setup
        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(ChassisDB.lookup("HGN-733C"));
        // 90 tons, 9 tons internals

        // Execute (10.75 tons of armour)
        final CmdDistributeArmour cut = new CmdDistributeArmour(loadout, 32 * 10 + 16, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(9.0 + 10.5, loadout.getMass(), 0.0);
        assertEquals(336, loadout.getArmour());
    }

    /**
     * The operator shall respect the front-back ratio.
     *
     * @throws Exception
     */
    @Test
    public void testArmourDistributor_FrontBackRatio() throws Exception {
        // Setup
        final double frontBackRatio = 5.0;
        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(ChassisDB.lookup("HGN-733C"));
        // 90 tons, 9 tons internals

        // Execute (10 tons of armour)
        final CmdDistributeArmour cut = new CmdDistributeArmour(loadout, 32 * 10, frontBackRatio, xBar);
        stack.pushAndApply(cut);

        // Verify
        final int front = loadout.getComponent(Location.CenterTorso).getArmour(ArmourSide.FRONT);
        final int back = loadout.getComponent(Location.CenterTorso).getArmour(ArmourSide.BACK);

        final int tolerance = 1;
        final double lb = (double) (front - tolerance) / (back + tolerance);
        final double ub = (double) (front + tolerance) / (back - tolerance);

        assertTrue(lb < frontBackRatio);
        assertTrue(ub > frontBackRatio);
    }

    /**
     * The operator shall not touch a manually set torso even if the attached arm contains items.
     *
     * @throws Exception
     */
    @Test
    public void testArmourDistributor_LeaveManualTorsoAloneWhenAutomaticArm() throws Exception {
        // Setup
        final Loadout loadout = loadLink("lsml://rR4AmwAWARgMTQc5AxcXvqGwRth8SJKlRH9zYKcU");

        stack.pushAndApply(
                new CmdSetArmour(null, loadout, loadout.getComponent(Location.Head), ArmourSide.ONLY, 12, true));
        stack.pushAndApply(
                new CmdSetArmour(null, loadout, loadout.getComponent(Location.RightArm), ArmourSide.ONLY, 0, true));
        stack.pushAndApply(
                new CmdSetArmour(null, loadout, loadout.getComponent(Location.LeftTorso), ArmourSide.FRONT, 56, true));
        stack.pushAndApply(
                new CmdSetArmour(null, loadout, loadout.getComponent(Location.LeftTorso), ArmourSide.BACK, 3, true));

        // Execute
        final CmdDistributeArmour cut = new CmdDistributeArmour(loadout, 278, 8.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(272, loadout.getArmour()); // 278 rounded down to even half tons is 272
    }

    /**
     * The operator shall provide protection for components linking important components.
     *
     * @throws Exception
     */
    @Test
    public void testArmourDistributor_Link_Priority() throws Exception {
        // Setup
        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(ChassisDB.lookup("HGN-733C"));
        stack.pushAndApply(
                new CmdAddItem(xBar, loadout, loadout.getComponent(Location.RightArm), ItemDB.lookup("AC/20")));

        // Execute
        final CmdDistributeArmour cut = new CmdDistributeArmour(loadout, 350, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        final int raArmour = loadout.getComponent(Location.RightArm).getArmourTotal();
        final int rtArmour = loadout.getComponent(Location.RightTorso).getArmourTotal();
        assertTrue(rtArmour > 40);
        assertTrue(raArmour > 20);
    }

    /**
     * The operator shall not barf if there is not enough free tonnage to accommodate the request. It shall allocate as
     * much as possible to fill the loadout.
     *
     * @throws Exception
     */
    @Test
    public void testArmourDistributor_NotEnoughTonnage() throws Exception {
        // Setup
        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(ChassisDB.lookup("LCT-3M"));
        stack.pushAndApply(
                new CmdAddItem(xBar, loadout, loadout.getComponent(Location.RightArm), ItemDB.lookup("ER PPC")));
        stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.CenterTorso),
                ItemDB.lookup("STD ENGINE 190")));

        // Execute
        final CmdDistributeArmour cut = new CmdDistributeArmour(loadout, 138, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertTrue(loadout.getFreeMass() < loadout.getUpgrades().getArmour().getArmourMass(1));
        assertTrue(32 < loadout.getArmour());
    }

    /**
     * The operator shall not barf if there is not enough free tonnage to accommodate the request. It shall allocate as
     * much as possible to fill the loadout.
     *
     * @throws Exception
     */
    @Test
    public void testArmourDistributor_NotEnoughTonnage2() throws Exception {
        // Setup
        final Loadout loadout = loadLink("lsml://rRsAkAtICFASaw1ICFALuihsfxmYtWt+nq0w9U1oz8oflBb6erRaKQ==");

        // Execute
        final CmdDistributeArmour cut = new CmdDistributeArmour(loadout, 500, 8.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertTrue(loadout.getFreeMass() < loadout.getUpgrades().getArmour().getArmourMass(1));
    }

    /**
     * The operator shall do nothing if the requested amount of armour is less than manually set amount.
     *
     * @throws Exception
     */
    @Test
    public void testArmourDistributor_RequestSmallerThanManuallySet() throws Exception {
        // Setup
        final Loadout loadout = loadLink("lsml://rR4AmwAWARgMTQc5AxcXvqGwRth8SJKlRH9zYKcU");

        stack.pushAndApply(
                new CmdSetArmour(null, loadout, loadout.getComponent(Location.Head), ArmourSide.ONLY, 12, true));
        stack.pushAndApply(
                new CmdSetArmour(null, loadout, loadout.getComponent(Location.RightArm), ArmourSide.ONLY, 0, true));
        stack.pushAndApply(
                new CmdSetArmour(null, loadout, loadout.getComponent(Location.LeftTorso), ArmourSide.FRONT, 56, true));
        stack.pushAndApply(
                new CmdSetArmour(null, loadout, loadout.getComponent(Location.LeftTorso), ArmourSide.BACK, 3, true));

        // Execute
        final CmdDistributeArmour cut = new CmdDistributeArmour(loadout, 50, 8.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(71, loadout.getArmour()); // 71 points of manual armour set
    }

    /**
     * The operator shall take already existing armour amounts into account when deciding on how much armour to
     * distribute.
     *
     * @throws Exception
     */
    @Test
    public void testArmourDistributor_RespectManual_CorrectTotal() throws Exception {
        // Setup
        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(ChassisDB.lookup("HGN-733C"));
        // 90 tons, 9 tons internals
        stack.pushAndApply(
                new CmdSetArmour(xBar, loadout, loadout.getComponent(Location.LeftLeg), ArmourSide.ONLY, 70, true));
        stack.pushAndApply(
                new CmdSetArmour(xBar, loadout, loadout.getComponent(Location.RightLeg), ArmourSide.ONLY, 70, true));

        // Execute (15 tons of armour, would be 76 on each leg if they weren't manual)
        final CmdDistributeArmour cut = new CmdDistributeArmour(loadout, 480, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(9.0 + 15, loadout.getMass(), 0.0);
        assertEquals(480, loadout.getArmour());

        assertEquals(70, loadout.getComponent(Location.LeftLeg).getArmourTotal());
        assertEquals(70, loadout.getComponent(Location.RightLeg).getArmourTotal());
    }

    /**
     * The operator shall not add armour to manually assigned locations.
     *
     * @throws Exception
     */
    @Test
    public void testArmourDistributor_RespectManual_DoNotAdd() throws Exception {
        // Setup
        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(ChassisDB.lookup("HGN-733C"));
        // 90 tons, 9 tons internals
        stack.pushAndApply(
                new CmdSetArmour(xBar, loadout, loadout.getComponent(Location.LeftLeg), ArmourSide.ONLY, 70, true));
        stack.pushAndApply(
                new CmdSetArmour(xBar, loadout, loadout.getComponent(Location.RightLeg), ArmourSide.ONLY, 70, true));

        // Execute (15 tons of armour, would be 76 on each leg if they weren't manual)
        final CmdDistributeArmour cut = new CmdDistributeArmour(loadout, 480, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(9.0 + 15, loadout.getMass(), 0.0);
        assertEquals(480, loadout.getArmour());

        assertEquals(70, loadout.getComponent(Location.LeftLeg).getArmourTotal());
        assertEquals(70, loadout.getComponent(Location.RightLeg).getArmourTotal());
    }

    /**
     * The operator shall not remove armour from manually assigned locations.
     *
     * @throws Exception
     */
    @Test
    public void testArmourDistributor_RespectManual_DoNotRemove() throws Exception {
        // Setup
        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(ChassisDB.lookup("HGN-733C"));
        // 90 tons, 9 tons internals
        stack.pushAndApply(
                new CmdSetArmour(xBar, loadout, loadout.getComponent(Location.LeftLeg), ArmourSide.ONLY, 70, true));
        stack.pushAndApply(
                new CmdSetArmour(xBar, loadout, loadout.getComponent(Location.RightLeg), ArmourSide.ONLY, 70, true));

        // Execute (5 tons of armour, would be 47 on each leg if they weren't manual)
        final CmdDistributeArmour cut = new CmdDistributeArmour(loadout, 160, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(9.0 + 5, loadout.getMass(), 0.0);
        assertEquals(160, loadout.getArmour());

        assertEquals(70, loadout.getComponent(Location.LeftLeg).getArmourTotal());
        assertEquals(70, loadout.getComponent(Location.RightLeg).getArmourTotal());
    }

    /**
     * The operator shall not barf if the manually set armours take up more than the available budget.
     *
     * @throws Exception
     */
    @Test
    public void testArmourDistributor_RespectManual_NegativeBudget() throws Exception {
        // Setup
        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(ChassisDB.lookup("HGN-733C"));
        // 90 tons, 9 tons internals
        stack.pushAndApply(
                new CmdSetArmour(xBar, loadout, loadout.getComponent(Location.LeftLeg), ArmourSide.ONLY, 64, true));
        stack.pushAndApply(
                new CmdSetArmour(xBar, loadout, loadout.getComponent(Location.RightLeg), ArmourSide.ONLY, 64, true));

        // Execute
        final CmdDistributeArmour cut = new CmdDistributeArmour(loadout, 32, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(9.0 + 4.0, loadout.getMass(), 0.0);
        assertEquals(128, loadout.getArmour());
    }

    /**
     * If the budget given is larger than can be assigned due to manually set parts, the operator shall assign max
     * armour to the remaining parts.
     *
     * @throws Exception
     */
    @Test
    public void testArmourDistributor_RespectManual_TooBigBudget() throws Exception {
        // Setup
        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(ChassisDB.lookup("HGN-733C"));
        // 90 tons, 9 tons internals
        stack.pushAndApply(
                new CmdSetArmour(xBar, loadout, loadout.getComponent(Location.LeftLeg), ArmourSide.ONLY, 64, true));
        stack.pushAndApply(
                new CmdSetArmour(xBar, loadout, loadout.getComponent(Location.RightLeg), ArmourSide.ONLY, 64, true));

        // Execute
        final CmdDistributeArmour cut = new CmdDistributeArmour(loadout, 558, 1.0, xBar); // 558 is max
        stack.pushAndApply(cut);

        // Verify
        assertEquals(558 - 2 * (76 - 64), loadout.getArmour());
    }

    /**
     * Values that are not even half tons shall be rounded down.
     *
     * @throws Exception
     */
    @Test
    public void testArmourDistributor_RoundDown() throws Exception {
        // Setup
        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(ChassisDB.lookup("HGN-733C"));
        // 90 tons, 9 tons internals

        // Execute (10.75 tons of armour)
        final CmdDistributeArmour cut = new CmdDistributeArmour(loadout, 32 * 10 + 16 + 8, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        final double evenHalfTons = (int) (loadout.getMass() * 2.0) / 2.0;
        final double diffTons = loadout.getMass() - evenHalfTons;
        assertTrue(Math.abs(diffTons) < loadout.getUpgrades().getArmour().getArmourMass(1));
    }

    /**
     * The operator shall not barf if there is not enough free tonnage to accommodate the request. It shall allocate as
     * much as possible to fill the loadout.
     *
     * @throws Exception
     */
    @Test
    public void testArmourDistributor_RoundDown2() throws Exception {
        // Setup
        final Loadout loadout = loadLink("lsml://rRoASDtFBzsSaQtFBzs7uihs/fvfSpVl5eXD0kVtiMPfhQ==");

        // Execute
        final CmdDistributeArmour cut = new CmdDistributeArmour(loadout, 558, 8.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(544, loadout.getArmour());
    }

    /**
     * The operation shall round down to the closest half ton, even if quarter ton items are present.
     *
     * @throws Exception
     */
    @Test
    public void testArmourDistributor_RoundDownQuarterTons() throws Exception {
        // Setup
        final Loadout loadout = loadLink("lsml://rgC0CCwECQc7BSwECAAP6zHaJmuzrtq69oNmgrsUyma7Wuws");

        // Execute
        final CmdDistributeArmour cut = new CmdDistributeArmour(loadout, 192, 8.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(184, loadout.getArmour());
    }

    /**
     * Shield arms should get lower priority.
     *
     * @throws Exception
     */
    @Test
    public void testArmourDistributor_ShieldArm() throws Exception {
        // Setup
        final LoadoutStandard loadout = (LoadoutStandard) loadoutFactory.produceEmpty(ChassisDB.lookup("BNC-3S"));

        // 95 tons, 9.5 tons internals
        stack.pushAndApply(new CmdAddItem(xBar, loadout, loadout.getComponent(Location.LeftArm), ItemDB.lookup("PPC")));

        // Execute (10.0 tons of armour)
        final CmdDistributeArmour cut = new CmdDistributeArmour(loadout, 32 * 10, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        final ConfiguredComponent shieldArm = loadout.getComponent(Location.RightArm);
        final ConfiguredComponent weaponArm = loadout.getComponent(Location.LeftArm);

        assertTrue(shieldArm.getArmourTotal() < weaponArm.getArmourTotal() / 2);
    }

    private Loadout loadLink(String aLsml) throws Exception {
        final Loadout loadout = TestHelpers.parse(aLsml);
        for (final ConfiguredComponent part : loadout.getComponents()) {
            if (part.getInternalComponent().getLocation().isTwoSided()) {
                stack.pushAndApply(new CmdSetArmour(null, loadout, part, ArmourSide.FRONT,
                        part.getArmour(ArmourSide.FRONT), false));
            }
            else {
                stack.pushAndApply(
                        new CmdSetArmour(null, loadout, part, ArmourSide.ONLY, part.getArmourTotal(), false));
            }
        }
        return loadout;
    }
}
