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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MechEfficiencyTypeTest {

    private static final String NON_EXISTENT_EFFICIENCY_STRING = "foobar";

    @Test
    public void testAffectsHeat() {
        assertTrue(MechEfficiencyType.COOL_RUN.affectsHeat());
        assertTrue(MechEfficiencyType.HEAT_CONTAINMENT.affectsHeat());
        assertFalse(MechEfficiencyType.KINETIC_BURST.affectsHeat());
        assertFalse(MechEfficiencyType.HARD_BRAKE.affectsHeat());
        assertFalse(MechEfficiencyType.TWIST_X.affectsHeat());
        assertFalse(MechEfficiencyType.TWIST_SPEED.affectsHeat());
        assertFalse(MechEfficiencyType.ARM_REFLEX.affectsHeat());
        assertFalse(MechEfficiencyType.ANCHORTURN.affectsHeat());

        assertFalse(MechEfficiencyType.QUICKIGNITION.affectsHeat());
        assertTrue(MechEfficiencyType.FAST_FIRE.affectsHeat());
        assertFalse(MechEfficiencyType.PINPOINT.affectsHeat());
        assertFalse(MechEfficiencyType.SPEED_TWEAK.affectsHeat());

        assertFalse(MechEfficiencyType.MODULESLOT.affectsHeat());
    }

    @Test
    public void testFromMwo() {
        assertSame(MechEfficiencyType.COOL_RUN, MechEfficiencyType.fromMwo("eMTBasic_CoolRun"));
        assertSame(MechEfficiencyType.HEAT_CONTAINMENT, MechEfficiencyType.fromMwo("eMTBasic_HeatContainment"));
        assertSame(MechEfficiencyType.KINETIC_BURST, MechEfficiencyType.fromMwo("eMTBasic_KineticBurst"));
        assertSame(MechEfficiencyType.HARD_BRAKE, MechEfficiencyType.fromMwo("eMTBasic_HardBrake"));
        assertSame(MechEfficiencyType.TWIST_X, MechEfficiencyType.fromMwo("eMTBasic_TwistX"));
        assertSame(MechEfficiencyType.TWIST_SPEED, MechEfficiencyType.fromMwo("eMTBasic_TwistSpeed"));
        assertSame(MechEfficiencyType.ARM_REFLEX, MechEfficiencyType.fromMwo("eMTBasic_ArmReflex"));
        assertSame(MechEfficiencyType.ANCHORTURN, MechEfficiencyType.fromMwo("eMTBasic_AnchorTurn"));

        assertSame(MechEfficiencyType.QUICKIGNITION, MechEfficiencyType.fromMwo("eMTElite_QuickIgnition"));
        assertSame(MechEfficiencyType.FAST_FIRE, MechEfficiencyType.fromMwo("eMTElite_FastFire"));
        assertSame(MechEfficiencyType.PINPOINT, MechEfficiencyType.fromMwo("eMTElite_PinPoint"));
        assertSame(MechEfficiencyType.SPEED_TWEAK, MechEfficiencyType.fromMwo("eMTElite_SpeedTweak"));

        assertSame(MechEfficiencyType.MODULESLOT, MechEfficiencyType.fromMwo("eMTMaster_ModuleSlot"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromMwoBadSting() {
        MechEfficiencyType.fromMwo(NON_EXISTENT_EFFICIENCY_STRING);
    }

    @Test
    public void testFromOldName() {
        assertSame(MechEfficiencyType.SPEED_TWEAK, MechEfficiencyType.fromOldName("Speedtweak"));
        assertSame(MechEfficiencyType.COOL_RUN, MechEfficiencyType.fromOldName("coOlrun"));
        assertSame(MechEfficiencyType.HEAT_CONTAINMENT, MechEfficiencyType.fromOldName("heatcontainment"));
        assertSame(MechEfficiencyType.ANCHORTURN, MechEfficiencyType.fromOldName("anchorturn"));
        assertSame(MechEfficiencyType.FAST_FIRE, MechEfficiencyType.fromOldName("fastfire"));
        assertSame(MechEfficiencyType.TWIST_X, MechEfficiencyType.fromOldName("twistX"));
        assertSame(MechEfficiencyType.TWIST_SPEED, MechEfficiencyType.fromOldName("twistspeed"));
        assertSame(MechEfficiencyType.ARM_REFLEX, MechEfficiencyType.fromOldName("armReflex"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromOldNameBadSting() {
        MechEfficiencyType.fromOldName(NON_EXISTENT_EFFICIENCY_STRING);
    }
}
