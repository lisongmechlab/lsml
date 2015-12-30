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
package org.lisoft.lsml.model.modifiers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.messages.EfficienciesMessage;
import org.lisoft.lsml.messages.EfficienciesMessage.Type;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.BaseMovementProfile;
import org.lisoft.lsml.model.chassi.MovementArchetype;
import org.lisoft.lsml.model.chassi.MovementProfile;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EfficienciesTest {
    @Mock
    private MessageXBar  xBar;
    private Efficiencies cut;

    @Before
    public void setup() {
        cut = new Efficiencies();
    }

    @Test
    public void testEquals() {
        Efficiencies cut1 = new Efficiencies();

        assertEquals(cut, cut);
        assertEquals(cut, cut1);

        cut1.setDoubleBasics(true, null);
        assertNotEquals(cut, cut1);
        cut1.setDoubleBasics(false, null);

        for (MechEfficiencyType type : MechEfficiencyType.values()) {
            cut1.setEfficiency(type, true, null);
            assertNotEquals(cut, cut1);
            cut1.setEfficiency(type, true, null);
        }
    }

    @Test
    public void testAssign() {
        Efficiencies cut0 = new Efficiencies();
        Efficiencies cut1 = new Efficiencies();

        for (MechEfficiencyType type : MechEfficiencyType.values()) {
            cut1.setEfficiency(type, !cut0.hasEfficiency(type), null);
        }
        cut1.setDoubleBasics(!cut0.hasDoubleBasics(), null);

        cut0.assign(cut1);
        assertEquals(cut0, cut1);
    }

    @Test
    public void testSetEfficiency_NoXBar() throws Exception {
        for (MechEfficiencyType type : MechEfficiencyType.values()) {
            assertFalse(cut.hasEfficiency(type));
            cut.setEfficiency(type, false, null);
        }
        // no crash :)
    }

    @Test
    public void testSetEfficiency_NoChange() throws Exception {
        for (MechEfficiencyType type : MechEfficiencyType.values()) {
            assertFalse(cut.hasEfficiency(type));
            cut.setEfficiency(type, false, xBar);
        }
        verifyZeroInteractions(xBar);
    }

    @Test
    public void testHasSetEfficiency() throws Exception {
        InOrder order = Mockito.inOrder(xBar);
        for (MechEfficiencyType type : MechEfficiencyType.values()) {
            assertFalse(cut.hasEfficiency(type));
            cut.setEfficiency(type, true, xBar);
            order.verify(xBar).post(new EfficienciesMessage(cut, Type.Changed, type.affectsHeat()));

            assertTrue(cut.hasEfficiency(type));
            cut.setEfficiency(type, false, xBar);
            order.verify(xBar).post(new EfficienciesMessage(cut, Type.Changed, type.affectsHeat()));
            assertFalse(cut.hasEfficiency(type));
        }
    }

    @Test
    public void testSpeedModifier() throws Exception {
        Attribute attribute = new Attribute(1.0, ModifierDescription.SEL_MOVEMENT_MAX_SPEED);
        cut.setEfficiency(MechEfficiencyType.SPEED_TWEAK, true, null);

        assertEquals(1.075, attribute.value(cut.getModifiers()), 0.0);
    }

    @Test
    public void testGetHeatDissipationModifier() throws Exception {
        Attribute attribute = new Attribute(1.0, ModifierDescription.SEL_HEAT_DISSIPATION);
        cut.setEfficiency(MechEfficiencyType.COOL_RUN, true, null);

        assertEquals(1.075, attribute.value(cut.getModifiers()), 0.0);

        cut.setDoubleBasics(true, null);
        assertEquals(1.15, attribute.value(cut.getModifiers()), 0.0);
    }

    @Test
    public void testGetHeatCapacityModifier() throws Exception {
        Attribute attribute = new Attribute(1.0, ModifierDescription.SEL_HEAT_LIMIT);
        cut.setEfficiency(MechEfficiencyType.HEAT_CONTAINMENT, true, null);

        assertEquals(1.1, attribute.value(cut.getModifiers()), 0.0);

        cut.setDoubleBasics(true, null);
        assertEquals(1.2, attribute.value(cut.getModifiers()), 0.0);
    }

    @Test
    public void testSetHasDoubleBasics() throws Exception {
        // Default false
        assertEquals(false, cut.hasDoubleBasics());
        verifyZeroInteractions(xBar);

        // We want messages too!
        for (boolean b : new boolean[] { true, false }) {
            cut.setDoubleBasics(b, xBar);
            assertEquals(b, cut.hasDoubleBasics());
            verify(xBar).post(new EfficienciesMessage(cut, Type.Changed, true));
            reset(xBar);
        }

        // No messages if there was no change.
        for (boolean b : new boolean[] { true, false }) {
            cut.setDoubleBasics(b, xBar);
            reset(xBar);
            cut.setDoubleBasics(b, xBar);
            verifyZeroInteractions(xBar);
        }
    }

    @Test
    public void testGetWeaponCycletimeModifier() throws Exception {
        Attribute attribute = new Attribute(1.0, ModifierDescription.SEL_ALL_WEAPONS,
                ModifierDescription.SPEC_WEAPON_COOLDOWN);
        cut.setEfficiency(MechEfficiencyType.FAST_FIRE, true, null);

        assertEquals(0.95, attribute.value(cut.getModifiers()), 0.0);
    }

    @Test
    public void testGetWeaponCycletimeModifier_WithDoubleBasics() throws Exception {
        Attribute attribute = new Attribute(1.0, ModifierDescription.SEL_ALL_WEAPONS,
                ModifierDescription.SPEC_WEAPON_COOLDOWN);
        cut.setDoubleBasics(true, null);
        cut.setEfficiency(MechEfficiencyType.FAST_FIRE, true, null);

        assertEquals(0.95, attribute.value(cut.getModifiers()), 0.0);
    }

    @Test
    public void testTwistX_Applies() {
        cut.setEfficiency(MechEfficiencyType.TWIST_X, true, null);
        MovementProfile mp = new BaseMovementProfile(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                MovementArchetype.Medium);

        assertEquals(1.025, mp.getTorsoPitchMax(cut.getModifiers()), 0.0);
        assertEquals(1.025, mp.getTorsoYawMax(cut.getModifiers()), 0.0);
    }

    @Test
    public void testTwistX_Applies2X() {
        cut.setEfficiency(MechEfficiencyType.TWIST_X, true, null);
        cut.setDoubleBasics(true, null);
        MovementProfile mp = new BaseMovementProfile(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                MovementArchetype.Medium);

        assertEquals(1.05, mp.getTorsoPitchMax(cut.getModifiers()), 0.0);
        assertEquals(1.05, mp.getTorsoYawMax(cut.getModifiers()), 0.0);
    }

    @Test
    public void testTwistSpeed_Applies() {
        cut.setEfficiency(MechEfficiencyType.TWIST_SPEED, true, null);
        MovementProfile mp = new BaseMovementProfile(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                MovementArchetype.Medium);

        assertEquals(1.025, mp.getTorsoPitchSpeed(cut.getModifiers()), 0.0);
        assertEquals(1.025, mp.getTorsoYawSpeed(cut.getModifiers()), 0.0);
    }

    @Test
    public void testTwistSpeed_Applies2X() {
        cut.setEfficiency(MechEfficiencyType.TWIST_SPEED, true, null);
        cut.setDoubleBasics(true, null);
        MovementProfile mp = new BaseMovementProfile(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                MovementArchetype.Medium);

        assertEquals(1.05, mp.getTorsoPitchSpeed(cut.getModifiers()), 0.0);
        assertEquals(1.05, mp.getTorsoYawSpeed(cut.getModifiers()), 0.0);
    }

    @Test
    public void testArmReflex_Applies() {
        cut.setEfficiency(MechEfficiencyType.ARM_REFLEX, true, null);
        MovementProfile mp = new BaseMovementProfile(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                MovementArchetype.Medium);

        assertEquals(1.025, mp.getArmPitchSpeed(cut.getModifiers()), 0.0);
        assertEquals(1.025, mp.getArmYawSpeed(cut.getModifiers()), 0.0);
    }

    @Test
    public void testArmReflex_Applies2X() {
        cut.setEfficiency(MechEfficiencyType.ARM_REFLEX, true, null);
        cut.setDoubleBasics(true, null);
        MovementProfile mp = new BaseMovementProfile(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                MovementArchetype.Medium);

        assertEquals(1.05, mp.getArmPitchSpeed(cut.getModifiers()), 0.0);
        assertEquals(1.05, mp.getArmYawSpeed(cut.getModifiers()), 0.0);
    }

    @Test
    public void testAnchorTurn_Applies() {
        cut.setEfficiency(MechEfficiencyType.ANCHORTURN, true, null);
        MovementProfile mp = new BaseMovementProfile(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                MovementArchetype.Medium);

        assertEquals(1.1, mp.getTurnLerpLowRate(cut.getModifiers()), 0.0);
        assertEquals(1.1, mp.getTurnLerpMidRate(cut.getModifiers()), 0.0);
        assertEquals(1.1, mp.getTurnLerpHighRate(cut.getModifiers()), 0.0);
    }

    @Test
    public void testAnchorTurn_Applies2X() {
        cut.setEfficiency(MechEfficiencyType.ANCHORTURN, true, null);
        cut.setDoubleBasics(true, null);
        MovementProfile mp = new BaseMovementProfile(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                MovementArchetype.Medium);

        assertEquals(1.2, mp.getTurnLerpLowRate(cut.getModifiers()), 0.0);
        assertEquals(1.2, mp.getTurnLerpMidRate(cut.getModifiers()), 0.0);
        assertEquals(1.2, mp.getTurnLerpHighRate(cut.getModifiers()), 0.0);
    }
}
