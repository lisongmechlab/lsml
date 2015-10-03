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
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.chassi.ChassisDB;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.item.ItemDB;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.component.ComponentMessage;
import org.lisoft.lsml.model.loadout.component.ComponentMessage.Type;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.parsing.export.Base64LoadoutCoder;
import org.lisoft.lsml.util.DecodingException;
import org.lisoft.lsml.util.OperationStack;
import org.lisoft.lsml.util.message.MessageXBar;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link OpDistributeArmor}.
 * 
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.class)
public class OpDistributeArmorTest {
    @Mock
    MessageXBar xBar;

    OperationStack stack = new OperationStack(0);

    private LoadoutBase<?> loadLink(String aLsml) throws DecodingException {
        Base64LoadoutCoder coder = new Base64LoadoutCoder();
        LoadoutBase<?> loadout = coder.parse(aLsml);
        for (ConfiguredComponentBase part : loadout.getComponents()) {
            if (part.getInternalComponent().getLocation().isTwoSided()) {
                stack.pushAndApply(
                        new OpSetArmor(null, loadout, part, ArmorSide.FRONT, part.getArmor(ArmorSide.FRONT), false));
            }
            else {
                stack.pushAndApply(new OpSetArmor(null, loadout, part, ArmorSide.ONLY, part.getArmorTotal(), false));
            }
        }
        return loadout;
    }

    @BeforeClass
    static public void setup() {
        // Make sure everything is parsed so that we don't cause a timeout due to game-file parsing
        ItemDB.lookup("LRM 20");
        ChassisDB.lookup("AS7-D-DC");
    }

    /**
     * The operator shall succeed at placing the armor points somewhere on an empty loadout.
     */
    @Test
    public void testArmorDistributor_Distribute() {
        // Setup
        LoadoutStandard loadout = (LoadoutStandard) DefaultLoadoutFactory.instance
                .produceEmpty(ChassisDB.lookup("HGN-733C")); // 90 tons, 9
        // tons
        // internals

        // Execute (10 tons of armor)
        OpDistributeArmor cut = new OpDistributeArmor(loadout, 32 * 10, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(9.0 + 10, loadout.getMass(), 0.0);
        assertEquals(320, loadout.getArmor());
        Mockito.verify(xBar, Mockito.atLeastOnce())
                .post(new ComponentMessage(loadout.getComponent(Location.CenterTorso), Type.ArmorChanged, false));
    }

    /**
     * The operator shall always max CT if possible.
     */
    @Test
    public void testArmorDistributor_CT_Priority() {
        // Setup
        LoadoutStandard loadout = (LoadoutStandard) DefaultLoadoutFactory.instance
                .produceEmpty(ChassisDB.lookup("HGN-733C"));

        // Execute
        OpDistributeArmor cut = new OpDistributeArmor(loadout, 110, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertTrue(loadout.getComponent(Location.CenterTorso).getArmorTotal() > 90);
    }

    /**
     * The operator shall provide protection for components linking important components.
     */
    @Test
    public void testArmorDistributor_Link_Priority() {
        // Setup
        LoadoutStandard loadout = (LoadoutStandard) DefaultLoadoutFactory.instance
                .produceEmpty(ChassisDB.lookup("HGN-733C"));
        stack.pushAndApply(
                new OpAddItem(xBar, loadout, loadout.getComponent(Location.RightArm), ItemDB.lookup("AC/20")));

        // Execute
        OpDistributeArmor cut = new OpDistributeArmor(loadout, 350, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        int raArmor = loadout.getComponent(Location.RightArm).getArmorTotal();
        int rtArmor = loadout.getComponent(Location.RightTorso).getArmorTotal();
        assertTrue(rtArmor > 40);
        assertTrue(raArmor > 20);
    }

    /**
     * The operator shall not barf if there is not enough free tonnage to accommodate the request. It shall allocate as
     * much as possible to fill the loadout.
     */
    @Test
    public void testArmorDistributor_NotEnoughTonnage() {
        // Setup
        LoadoutStandard loadout = (LoadoutStandard) DefaultLoadoutFactory.instance
                .produceEmpty(ChassisDB.lookup("LCT-3M"));
        stack.pushAndApply(
                new OpAddItem(xBar, loadout, loadout.getComponent(Location.RightArm), ItemDB.lookup("ER PPC")));
        stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.CenterTorso),
                ItemDB.lookup("STD ENGINE 190")));

        // Execute
        OpDistributeArmor cut = new OpDistributeArmor(loadout, 138, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertTrue(loadout.getFreeMass() < loadout.getUpgrades().getArmor().getArmorMass(1));
        assertTrue(32 < loadout.getArmor());
    }

    /**
     * The operator shall not barf if there is not enough free tonnage to accommodate the request. It shall allocate as
     * much as possible to fill the loadout.
     * 
     * @throws DecodingException
     */
    @Test
    public void testArmorDistributor_NotEnoughTonnage2() throws DecodingException {
        // Setup
        LoadoutBase<?> loadout = loadLink("lsml://rRsAkAtICFASaw1ICFALuihsfxmYtWt+nq0w9U1oz8oflBb6erRaKQ==");

        // Execute
        OpDistributeArmor cut = new OpDistributeArmor(loadout, 500, 8.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertTrue(loadout.getFreeMass() < loadout.getUpgrades().getArmor().getArmorMass(1));
    }

    /**
     * If the budget given is larger than can be assigned due to manually set parts, the operator shall assign max armor
     * to the remaining parts.
     */
    @Test
    public void testArmorDistributor_RespectManual_TooBigBudget() {
        // Setup
        LoadoutStandard loadout = (LoadoutStandard) DefaultLoadoutFactory.instance
                .produceEmpty(ChassisDB.lookup("HGN-733C")); // 90 tons, 9
        // tons
        // internals
        stack.pushAndApply(
                new OpSetArmor(xBar, loadout, loadout.getComponent(Location.LeftLeg), ArmorSide.ONLY, 64, true));
        stack.pushAndApply(
                new OpSetArmor(xBar, loadout, loadout.getComponent(Location.RightLeg), ArmorSide.ONLY, 64, true));

        // Execute
        OpDistributeArmor cut = new OpDistributeArmor(loadout, 558, 1.0, xBar); // 558 is max
        stack.pushAndApply(cut);

        // Verify
        assertEquals(558 - 2 * (76 - 64), loadout.getArmor());
    }

    /**
     * The operator shall not barf if the manually set armors take up more than the available budget.
     */
    @Test
    public void testArmorDistributor_RespectManual_NegativeBudget() {
        // Setup
        LoadoutStandard loadout = (LoadoutStandard) DefaultLoadoutFactory.instance
                .produceEmpty(ChassisDB.lookup("HGN-733C")); // 90 tons, 9
        // tons
        // internals
        stack.pushAndApply(
                new OpSetArmor(xBar, loadout, loadout.getComponent(Location.LeftLeg), ArmorSide.ONLY, 64, true));
        stack.pushAndApply(
                new OpSetArmor(xBar, loadout, loadout.getComponent(Location.RightLeg), ArmorSide.ONLY, 64, true));

        // Execute
        OpDistributeArmor cut = new OpDistributeArmor(loadout, 32, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(9.0 + 4.0, loadout.getMass(), 0.0);
        assertEquals(128, loadout.getArmor());
    }

    /**
     * The operator shall take already existing armor amounts into account when deciding on how much armor to
     * distribute.
     */
    @Test
    public void testArmorDistributor_RespectManual_CorrectTotal() {
        // Setup
        LoadoutStandard loadout = (LoadoutStandard) DefaultLoadoutFactory.instance
                .produceEmpty(ChassisDB.lookup("HGN-733C")); // 90 tons, 9
        // tons
        // internals
        stack.pushAndApply(
                new OpSetArmor(xBar, loadout, loadout.getComponent(Location.LeftLeg), ArmorSide.ONLY, 70, true));
        stack.pushAndApply(
                new OpSetArmor(xBar, loadout, loadout.getComponent(Location.RightLeg), ArmorSide.ONLY, 70, true));

        // Execute (15 tons of armor, would be 76 on each leg if they weren't manual)
        OpDistributeArmor cut = new OpDistributeArmor(loadout, 480, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(9.0 + 15, loadout.getMass(), 0.0);
        assertEquals(480, loadout.getArmor());

        assertEquals(70, loadout.getComponent(Location.LeftLeg).getArmorTotal());
        assertEquals(70, loadout.getComponent(Location.RightLeg).getArmorTotal());
    }

    /**
     * The operator shall not add armor to manually assigned locations.
     */
    @Test
    public void testArmorDistributor_RespectManual_DoNotAdd() {
        // Setup
        LoadoutStandard loadout = (LoadoutStandard) DefaultLoadoutFactory.instance
                .produceEmpty(ChassisDB.lookup("HGN-733C")); // 90 tons, 9
        // tons
        // internals
        stack.pushAndApply(
                new OpSetArmor(xBar, loadout, loadout.getComponent(Location.LeftLeg), ArmorSide.ONLY, 70, true));
        stack.pushAndApply(
                new OpSetArmor(xBar, loadout, loadout.getComponent(Location.RightLeg), ArmorSide.ONLY, 70, true));

        // Execute (15 tons of armor, would be 76 on each leg if they weren't manual)
        OpDistributeArmor cut = new OpDistributeArmor(loadout, 480, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(9.0 + 15, loadout.getMass(), 0.0);
        assertEquals(480, loadout.getArmor());

        assertEquals(70, loadout.getComponent(Location.LeftLeg).getArmorTotal());
        assertEquals(70, loadout.getComponent(Location.RightLeg).getArmorTotal());
    }

    /**
     * The operator shall not remove armor from manually assigned locations.
     */
    @Test
    public void testArmorDistributor_RespectManual_DoNotRemove() {
        // Setup
        LoadoutStandard loadout = (LoadoutStandard) DefaultLoadoutFactory.instance
                .produceEmpty(ChassisDB.lookup("HGN-733C")); // 90 tons, 9
        // tons
        // internals
        stack.pushAndApply(
                new OpSetArmor(xBar, loadout, loadout.getComponent(Location.LeftLeg), ArmorSide.ONLY, 70, true));
        stack.pushAndApply(
                new OpSetArmor(xBar, loadout, loadout.getComponent(Location.RightLeg), ArmorSide.ONLY, 70, true));

        // Execute (5 tons of armor, would be 47 on each leg if they weren't manual)
        OpDistributeArmor cut = new OpDistributeArmor(loadout, 160, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(9.0 + 5, loadout.getMass(), 0.0);
        assertEquals(160, loadout.getArmor());

        assertEquals(70, loadout.getComponent(Location.LeftLeg).getArmorTotal());
        assertEquals(70, loadout.getComponent(Location.RightLeg).getArmorTotal());
    }

    /**
     * The operator shall respect the front-back ratio.
     */
    @Test
    public void testArmorDistributor_FrontBackRatio() {
        // Setup
        final double frontBackRatio = 5.0;
        LoadoutStandard loadout = (LoadoutStandard) DefaultLoadoutFactory.instance
                .produceEmpty(ChassisDB.lookup("HGN-733C")); // 90 tons, 9
        // tons
        // internals

        // Execute (10 tons of armor)
        OpDistributeArmor cut = new OpDistributeArmor(loadout, 32 * 10, frontBackRatio, xBar);
        stack.pushAndApply(cut);

        // Verify
        int front = loadout.getComponent(Location.CenterTorso).getArmor(ArmorSide.FRONT);
        int back = loadout.getComponent(Location.CenterTorso).getArmor(ArmorSide.BACK);

        int tolerance = 1;
        double lb = (double) (front - tolerance) / (back + tolerance);
        double ub = (double) (front + tolerance) / (back - tolerance);

        assertTrue(lb < frontBackRatio);
        assertTrue(ub > frontBackRatio);
    }

    /**
     * Values that are even half tons shall not be rounded down.
     */
    @Test
    public void testArmorDistributor_EvenHalfNoRoundDown() {
        // Setup
        LoadoutStandard loadout = (LoadoutStandard) DefaultLoadoutFactory.instance
                .produceEmpty(ChassisDB.lookup("HGN-733C")); // 90 tons, 9
        // tons
        // internals

        // Execute (10.75 tons of armor)
        OpDistributeArmor cut = new OpDistributeArmor(loadout, 32 * 10 + 16, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(9.0 + 10.5, loadout.getMass(), 0.0);
        assertEquals(336, loadout.getArmor());
    }

    /**
     * Values that are not even half tons shall be rounded down.
     */
    @Test
    public void testArmorDistributor_RoundDown() {
        // Setup
        LoadoutStandard loadout = (LoadoutStandard) DefaultLoadoutFactory.instance
                .produceEmpty(ChassisDB.lookup("HGN-733C")); // 90 tons, 9
        // tons
        // internals

        // Execute (10.75 tons of armor)
        OpDistributeArmor cut = new OpDistributeArmor(loadout, 32 * 10 + 16 + 8, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        double evenHalfTons = (int) (loadout.getMass() * 2.0) / 2.0;
        double diffTons = loadout.getMass() - evenHalfTons;
        assertTrue(Math.abs(diffTons) < loadout.getUpgrades().getArmor().getArmorMass(1));
    }

    /**
     * The operation shall round down to the closest half ton, even if quarter ton items are present.
     * 
     * @throws DecodingException
     */
    @Test
    public void testArmorDistributor_RoundDownQuarterTons() throws DecodingException {
        // Setup
        LoadoutBase<?> loadout = loadLink("lsml://rgC0CCwECQc7BSwECAAP6zHaJmuzrtq69oNmgrsUyma7Wuws");

        // Execute
        OpDistributeArmor cut = new OpDistributeArmor(loadout, 192, 8.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(184, loadout.getArmor());
    }

    /**
     * The operator shall not barf if there is not enough free tonnage to accommodate the request. It shall allocate as
     * much as possible to fill the loadout.
     * 
     * @throws DecodingException
     */
    @Test
    public void testArmorDistributor_RoundDown2() throws DecodingException {
        // Setup
        LoadoutBase<?> loadout = loadLink("lsml://rRoASDtFBzsSaQtFBzs7uihs/fvfSpVl5eXD0kVtiMPfhQ==");

        // Execute
        OpDistributeArmor cut = new OpDistributeArmor(loadout, 558, 8.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(544, loadout.getArmor());
    }

    /**
     * The operator shall not touch a manually set torso even if the attached arm contains items.
     * 
     * @throws DecodingException
     */
    @Test
    public void testArmorDistributor_LeaveManualTorsoAloneWhenAutomaticArm() throws DecodingException {
        // Setup
        LoadoutBase<?> loadout = loadLink("lsml://rR4AmwAWARgMTQc5AxcXvqGwRth8SJKlRH9zYKcU");

        stack.pushAndApply(
                new OpSetArmor(null, loadout, loadout.getComponent(Location.Head), ArmorSide.ONLY, 12, true));
        stack.pushAndApply(
                new OpSetArmor(null, loadout, loadout.getComponent(Location.RightArm), ArmorSide.ONLY, 0, true));
        stack.pushAndApply(
                new OpSetArmor(null, loadout, loadout.getComponent(Location.LeftTorso), ArmorSide.FRONT, 56, true));
        stack.pushAndApply(
                new OpSetArmor(null, loadout, loadout.getComponent(Location.LeftTorso), ArmorSide.BACK, 3, true));

        // Execute
        OpDistributeArmor cut = new OpDistributeArmor(loadout, 278, 8.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(272, loadout.getArmor()); // 278 rounded down to even half tons is 272
    }

    /**
     * The operator shall do nothing if the requested amount of armor is less than manually set amount.
     * 
     * @throws DecodingException
     */
    @Test
    public void testArmorDistributor_RequestSmallerThanManuallySet() throws DecodingException {
        // Setup
        LoadoutBase<?> loadout = loadLink("lsml://rR4AmwAWARgMTQc5AxcXvqGwRth8SJKlRH9zYKcU");

        stack.pushAndApply(
                new OpSetArmor(null, loadout, loadout.getComponent(Location.Head), ArmorSide.ONLY, 12, true));
        stack.pushAndApply(
                new OpSetArmor(null, loadout, loadout.getComponent(Location.RightArm), ArmorSide.ONLY, 0, true));
        stack.pushAndApply(
                new OpSetArmor(null, loadout, loadout.getComponent(Location.LeftTorso), ArmorSide.FRONT, 56, true));
        stack.pushAndApply(
                new OpSetArmor(null, loadout, loadout.getComponent(Location.LeftTorso), ArmorSide.BACK, 3, true));

        // Execute
        OpDistributeArmor cut = new OpDistributeArmor(loadout, 50, 8.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(71, loadout.getArmor()); // 71 points of manual armor set
    }

    /**
     * Old armor values on automatically managed parts should be cleared
     */
    @Test
    public void testArmorDistributor_ClearOld() {
        // Setup
        LoadoutStandard loadout = (LoadoutStandard) DefaultLoadoutFactory.instance
                .produceEmpty(ChassisDB.lookup("CPLT-A1")); // 65 tons, 6.5
        // tons
        // internals
        stack.pushAndApply(
                new OpSetArmor(xBar, loadout, loadout.getComponent(Location.LeftArm), ArmorSide.ONLY, 2, false));
        stack.pushAndApply(
                new OpSetArmor(xBar, loadout, loadout.getComponent(Location.RightArm), ArmorSide.ONLY, 2, false));

        // Execute (6.5 tons of armor)
        OpDistributeArmor cut = new OpDistributeArmor(loadout, 208, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(6.5 + 6.5, loadout.getMass(), 0.0);
        assertEquals(208, loadout.getArmor());
    }

    /**
     * Shield arms should get lower priority.
     */
    @Test
    public void testArmorDistributor_ShieldArm() {
        // Setup
        LoadoutStandard loadout = (LoadoutStandard) DefaultLoadoutFactory.instance
                .produceEmpty(ChassisDB.lookup("BNC-3S")); // 95 tons, 9.5
        // tons
        // internals
        stack.pushAndApply(new OpAddItem(xBar, loadout, loadout.getComponent(Location.LeftArm), ItemDB.lookup("PPC")));

        // Execute (10.0 tons of armor)
        OpDistributeArmor cut = new OpDistributeArmor(loadout, 32 * 10, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        ConfiguredComponentBase shieldArm = loadout.getComponent(Location.RightArm);
        ConfiguredComponentBase weaponArm = loadout.getComponent(Location.LeftArm);

        assertTrue(shieldArm.getArmorTotal() < weaponArm.getArmorTotal() / 2);
    }

    /**
     * The operation shall succeed even if there is already max armor on the mech. (More on front parts than rear)
     */
    @Test
    public void testArmorDistributor_AlreadyMaxArmor_FrontRear() {
        // Setup
        LoadoutStandard loadout = (LoadoutStandard) DefaultLoadoutFactory.instance
                .produceEmpty(ChassisDB.lookup("HGN-733C")); // 90 tons, 9
        // tons
        // internals
        stack.pushAndApply(new OpSetMaxArmor(loadout, xBar, 5.0, false));

        // Execute (10.0 tons of armor)
        OpDistributeArmor cut = new OpDistributeArmor(loadout, 32 * 10, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(9.0 + 10.0, loadout.getMass(), 0.0);
        assertEquals(320, loadout.getArmor());
    }

    /**
     * The operation shall succeed even if there is already max armor on the mech. (More on rear parts than front)
     */
    @Test
    public void testArmorDistributor_AlreadyMaxArmor_RearFront() {
        // Setup
        LoadoutStandard loadout = (LoadoutStandard) DefaultLoadoutFactory.instance
                .produceEmpty(ChassisDB.lookup("HGN-733C")); // 90 tons, 9
        // tons
        // internals
        stack.pushAndApply(new OpSetMaxArmor(loadout, xBar, 0.2, false));

        // Execute (10.0 tons of armor)
        OpDistributeArmor cut = new OpDistributeArmor(loadout, 32 * 10, 1.0, xBar);
        stack.pushAndApply(cut);

        // Verify
        assertEquals(9.0 + 10.0, loadout.getMass(), 0.0);
        assertEquals(320, loadout.getArmor());
    }
}
