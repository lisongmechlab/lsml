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
package org.lisoft.lsml.model.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.lisoft.lsml.model.chassi.ChassisOmniMech;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.database.ChassisDB;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.upgrades.ArmourUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;

public class PayloadStatisticsTest {

    private final StructureUpgrade structure = mock(StructureUpgrade.class);
    private final ArmourUpgrade armour = mock(ArmourUpgrade.class);
    private final Engine fixedEngine = mock(Engine.class);

    @Test
    public final void testCalculate() throws Exception {
        final ChassisStandard jm6_a = (ChassisStandard) ChassisDB.lookup("JM6-A");

        final PayloadStatistics cut = new PayloadStatistics(false, false, false, false);
        assertEquals(40.0, cut.calculate(jm6_a, 250), 0.0);
        assertEquals(33.5, cut.calculate(jm6_a, 300), 0.0);
    }

    @Test
    public final void testCalculate_ClanUpgrades() throws Exception {
        final ChassisStandard on1_iic = (ChassisStandard) ChassisDB.lookup("ON1-IIC");

        final PayloadStatistics cut = new PayloadStatistics(true, true, true, true);

        assertEquals(38.97, cut.calculate(on1_iic, 335), 0.01);
    }

    @Test
    public final void testCalculate_Endo() throws Exception {
        final ChassisStandard jm6_a = (ChassisStandard) ChassisDB.lookup("JM6-A");

        final PayloadStatistics cut = new PayloadStatistics(false, false, true, false);
        assertEquals(43.0, cut.calculate(jm6_a, 250), 0.0);
        assertEquals(36.5, cut.calculate(jm6_a, 300), 0.0);
    }

    @Test
    public final void testCalculate_FerroMaxArmour() throws Exception {
        final ChassisStandard jm6_a = (ChassisStandard) ChassisDB.lookup("JM6-A");

        final PayloadStatistics cut = new PayloadStatistics(false, true, false, true);
        assertEquals(28.23, cut.calculate(jm6_a, 250), 0.01);
        assertEquals(21.73, cut.calculate(jm6_a, 300), 0.01);
    }

    @Test
    public final void testCalculate_MaxArmour() throws Exception {
        final ChassisStandard jm6_a = (ChassisStandard) ChassisDB.lookup("JM6-A");

        final PayloadStatistics cut = new PayloadStatistics(false, true, false, false);
        assertEquals(26.81, cut.calculate(jm6_a, 250), 0.01);
        assertEquals(20.31, cut.calculate(jm6_a, 300), 0.01);
    }

    @Test
    public final void testCalculate_SmallEngine() throws Exception {
        final ChassisStandard jm6_a = (ChassisStandard) ChassisDB.lookup("JM6-A");

        final PayloadStatistics cut = new PayloadStatistics(false, false, false, false);
        assertEquals(45.0, cut.calculate(jm6_a, 200), 0.0); // Needs two additional heat sinks
        assertEquals(44.0, cut.calculate(jm6_a, 205), 0.0); // Needs two additional heat sinks
        assertEquals(42.5, cut.calculate(jm6_a, 220), 0.0); // Needs two additional heat sinks
    }

    @Test
    public final void testCalculate_XL() throws Exception {
        final ChassisStandard jm6_a = (ChassisStandard) ChassisDB.lookup("JM6-A");

        final PayloadStatistics cut = new PayloadStatistics(true, false, false, false);
        assertEquals(46.0, cut.calculate(jm6_a, 250), 0.0);
        assertEquals(43.0, cut.calculate(jm6_a, 300), 0.0);
    }

    @Test
    public final void testOmniMech() {
        // Setup
        final int maxMass = 100;
        final double structureMass = 20;
        final double engineMass = 2.0;

        final boolean useMaxArmour = false;
        final int maxArmour = 100;
        final double armourMass = 0.0;

        final int fixedHs = 8; // < 10
        final boolean useXlEngine = false;

        verifyOmniMech(structureMass, armourMass, engineMass, maxMass, maxArmour, fixedHs, useXlEngine, useMaxArmour);

        verify(armour, never()).getArmourMass(anyInt());
    }

    @Test
    public final void testOmniMech_lotsOfHeatsinks() {
        // Setup
        final int maxMass = 100;
        final double structureMass = 20;
        final double engineMass = 2.0;

        final boolean useMaxArmour = false;
        final int maxArmour = 100;
        final double armourMass = 0.0;

        final int fixedHs = 10;
        final boolean useXlEngine = false;

        verifyOmniMech(structureMass, armourMass, engineMass, maxMass, maxArmour, fixedHs, useXlEngine, useMaxArmour);

        verify(armour, never()).getArmourMass(anyInt());
    }

    @Test
    public final void testOmniMech_MaxArmour() {
        // Setup
        final int maxMass = 100;
        final double structureMass = 20;
        final double engineMass = 2.0;

        final boolean useMaxArmour = true;
        final int maxArmour = 100;
        final double armourMass = 0.0;

        final int fixedHs = 8; // < 10
        final boolean useXlEngine = false;

        verifyOmniMech(structureMass, armourMass, engineMass, maxMass, maxArmour, fixedHs, useXlEngine, useMaxArmour);
    }

    @Test
    public final void testSetEndoSteel_IS() throws Exception {
        final ChassisStandard jm6_a = (ChassisStandard) ChassisDB.lookup("JM6-A");

        final PayloadStatistics cut = new PayloadStatistics(false, false, false, false);
        cut.setEndoSteel(true);

        assertEquals(43.0, cut.calculate(jm6_a, 250), 0.0);
    }

    @Test
    public final void testSetFerroFibrous() throws Exception {
        final ChassisStandard jm6_a = (ChassisStandard) ChassisDB.lookup("JM6-A");

        final PayloadStatistics cut = new PayloadStatistics(false, true, false, false);
        cut.setFerroFibrous(true);

        assertEquals(28.23, cut.calculate(jm6_a, 250), 0.01);
    }

    @Test
    public final void testSetMaxArmour() throws Exception {
        final ChassisStandard jm6_a = (ChassisStandard) ChassisDB.lookup("JM6-A");
        final PayloadStatistics cut = new PayloadStatistics(false, false, false, false);

        cut.setMaxArmour(true);

        assertEquals(26.81, cut.calculate(jm6_a, 250), 0.01);
    }

    @Test
    public final void testSetXLEngine() throws Exception {
        final ChassisStandard jm6_a = (ChassisStandard) ChassisDB.lookup("JM6-A");
        final PayloadStatistics cut = new PayloadStatistics(false, false, false, false);

        cut.setXLEngine(true);

        assertEquals(46.0, cut.calculate(jm6_a, 250), 0.0);
    }

    private double expectedMass(int aMaxMass, double aStructureMass, double aArmourMass, double aEngineMass,
            int aFixedHs) {
        final double mass = aStructureMass + aArmourMass + aEngineMass + Math.max(0, 10 - aFixedHs);
        return aMaxMass - mass;
    }

    private ChassisOmniMech makeOmniChassis(int maxMass, int maxArmour, int engineHs, double aStructureMass,
            double aArmourMass, double aEngineMass) {
        final ChassisOmniMech chassis = mock(ChassisOmniMech.class);
        when(chassis.getMassMax()).thenReturn(maxMass);
        when(chassis.getArmourMax()).thenReturn(maxArmour);
        when(chassis.getFixedStructureType()).thenReturn(structure);
        when(chassis.getFixedArmourType()).thenReturn(armour);
        when(chassis.getFixedEngine()).thenReturn(fixedEngine);

        when(structure.getStructureMass(chassis)).thenReturn(aStructureMass);
        when(armour.getArmourMass(maxArmour)).thenReturn(aArmourMass);

        when(fixedEngine.getMass()).thenReturn(aEngineMass);
        when(fixedEngine.getNumInternalHeatsinks()).thenReturn(engineHs);
        return chassis;
    }

    private void verifyOmniMech(final double structureMass, final double armourMass, final double engineMass,
            final int maxMass, final int maxArmour, final int fixedHs, boolean useXlEngine, boolean useMaxArmour) {
        // Setup
        final ChassisOmniMech chassis = makeOmniChassis(maxMass, maxArmour, fixedHs, structureMass, armourMass,
                engineMass);

        // Execute
        final PayloadStatistics cut = new PayloadStatistics(useXlEngine, useMaxArmour, false, false);

        // Verify
        final double expected = expectedMass(maxMass, structureMass, armourMass, engineMass, fixedHs);
        assertEquals(expected, cut.calculate(chassis), 0.0);
    }
}
