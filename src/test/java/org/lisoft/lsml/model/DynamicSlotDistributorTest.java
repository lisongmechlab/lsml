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
package org.lisoft.lsml.model;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.model.chassi.ChassisOmniMech;
import org.lisoft.lsml.model.chassi.ChassisVariant;
import org.lisoft.lsml.model.chassi.ComponentOmniMech;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.helpers.MockLoadoutContainer;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentOmniMech;
import org.lisoft.lsml.model.upgrades.ArmourUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DynamicSlotDistributorTest {
    MockLoadoutContainer mlc = new MockLoadoutContainer();
    List<ConfiguredComponent> priorityOrder;
    DynamicSlotDistributor cut;

    @Before
    public void setup() {
        cut = new DynamicSlotDistributor(mlc.loadout);

        // Priority order: RA, RT, RL, HD, CT, LT, LL, LA
        priorityOrder = Arrays.asList(mlc.ra, mlc.rt, mlc.rl, mlc.hd, mlc.ct, mlc.lt, mlc.ll, mlc.la);
    }

    @Test
    public void testGetDynamicArmourSlotsForComponent_Priority() {
        when(mlc.upgrades.getStructure()).thenReturn(UpgradeDB.IS_STD_STRUCTURE);
        when(mlc.upgrades.getArmour()).thenReturn(UpgradeDB.IS_FF_ARMOUR);

        when(mlc.ra.getSlotsFree()).thenReturn(12);
        when(mlc.rt.getSlotsFree()).thenReturn(12);
        when(mlc.rl.getSlotsFree()).thenReturn(12);
        when(mlc.hd.getSlotsFree()).thenReturn(12);
        when(mlc.ct.getSlotsFree()).thenReturn(12);
        when(mlc.ll.getSlotsFree()).thenReturn(12);
        when(mlc.lt.getSlotsFree()).thenReturn(12);
        when(mlc.la.getSlotsFree()).thenReturn(12);

        for (final ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmourSlots(part));
        }

        when(mlc.ra.getSlotsFree()).thenReturn(1);
        for (final ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmourSlots(part));
        }

        when(mlc.rt.getSlotsFree()).thenReturn(1);
        for (final ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmourSlots(part));
        }

        when(mlc.rl.getSlotsFree()).thenReturn(2);
        for (final ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmourSlots(part));
        }

        when(mlc.hd.getSlotsFree()).thenReturn(1);
        for (final ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmourSlots(part));
        }

        when(mlc.ct.getSlotsFree()).thenReturn(3);
        for (final ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmourSlots(part));
        }

        when(mlc.lt.getSlotsFree()).thenReturn(1);
        for (final ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmourSlots(part));
        }

        when(mlc.ll.getSlotsFree()).thenReturn(1);
        for (final ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmourSlots(part));
        }

        when(mlc.la.getSlotsFree()).thenReturn(1);
        for (final ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmourSlots(part));
        }
        // Slot overflow, fail graciously, no exceptions thrown
    }

    @Test
    public void testGetDynamicStructureSlotsForComponent_NoUpgrades() {
        when(mlc.upgrades.getStructure()).thenReturn(UpgradeDB.IS_STD_STRUCTURE);
        when(mlc.upgrades.getArmour()).thenReturn(UpgradeDB.IS_STD_ARMOUR);

        when(mlc.ra.getSlotsFree()).thenReturn(12);
        when(mlc.rt.getSlotsFree()).thenReturn(12);
        when(mlc.rl.getSlotsFree()).thenReturn(12);
        when(mlc.hd.getSlotsFree()).thenReturn(12);
        when(mlc.ct.getSlotsFree()).thenReturn(12);
        when(mlc.ll.getSlotsFree()).thenReturn(12);
        when(mlc.lt.getSlotsFree()).thenReturn(12);
        when(mlc.la.getSlotsFree()).thenReturn(12);

        assertEquals(0, cut.getDynamicStructureSlots(mlc.ra));
        assertEquals(0, cut.getDynamicStructureSlots(mlc.rt));
        assertEquals(0, cut.getDynamicStructureSlots(mlc.rl));
        assertEquals(0, cut.getDynamicStructureSlots(mlc.hd));
        assertEquals(0, cut.getDynamicStructureSlots(mlc.ct));
        assertEquals(0, cut.getDynamicStructureSlots(mlc.lt));
        assertEquals(0, cut.getDynamicStructureSlots(mlc.ll));
        assertEquals(0, cut.getDynamicStructureSlots(mlc.la));

        assertEquals(0, cut.getDynamicArmourSlots(mlc.ra));
        assertEquals(0, cut.getDynamicArmourSlots(mlc.rt));
        assertEquals(0, cut.getDynamicArmourSlots(mlc.rl));
        assertEquals(0, cut.getDynamicArmourSlots(mlc.hd));
        assertEquals(0, cut.getDynamicArmourSlots(mlc.ct));
        assertEquals(0, cut.getDynamicArmourSlots(mlc.lt));
        assertEquals(0, cut.getDynamicArmourSlots(mlc.ll));
        assertEquals(0, cut.getDynamicArmourSlots(mlc.la));
    }

    @Test
    public void testGetDynamicStructureSlotsForComponent_Priority() {
        when(mlc.upgrades.getStructure()).thenReturn(UpgradeDB.IS_ES_STRUCTURE);
        when(mlc.upgrades.getArmour()).thenReturn(UpgradeDB.IS_STD_ARMOUR);

        when(mlc.ra.getSlotsFree()).thenReturn(12);
        when(mlc.rt.getSlotsFree()).thenReturn(12);
        when(mlc.rl.getSlotsFree()).thenReturn(12);
        when(mlc.hd.getSlotsFree()).thenReturn(12);
        when(mlc.ct.getSlotsFree()).thenReturn(12);
        when(mlc.ll.getSlotsFree()).thenReturn(12);
        when(mlc.lt.getSlotsFree()).thenReturn(12);
        when(mlc.la.getSlotsFree()).thenReturn(12);

        for (final ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
        }

        when(mlc.ra.getSlotsFree()).thenReturn(1);
        for (final ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
        }

        when(mlc.rt.getSlotsFree()).thenReturn(1);
        for (final ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
        }

        when(mlc.rl.getSlotsFree()).thenReturn(2);
        for (final ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
        }

        when(mlc.hd.getSlotsFree()).thenReturn(1);
        for (final ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
        }

        when(mlc.ct.getSlotsFree()).thenReturn(3);
        for (final ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
        }

        when(mlc.lt.getSlotsFree()).thenReturn(1);
        for (final ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
        }

        when(mlc.ll.getSlotsFree()).thenReturn(1);
        for (final ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
        }

        when(mlc.la.getSlotsFree()).thenReturn(1);
        for (final ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
        }
        // Slot overflow, fail graciously, no exceptions thrown
    }

    /**
     * Dynamic armour slots are distributed before dynamic structure (arbitrary design decision).
     */
    @Test
    public void testMixedArmourStructurePriority() {
        when(mlc.upgrades.getStructure()).thenReturn(UpgradeDB.IS_ES_STRUCTURE);
        when(mlc.upgrades.getArmour()).thenReturn(UpgradeDB.IS_FF_ARMOUR);

        when(mlc.ra.getSlotsFree()).thenReturn(4); // 4 armour
        when(mlc.rt.getSlotsFree()).thenReturn(4);
        when(mlc.rl.getSlotsFree()).thenReturn(4);
        when(mlc.hd.getSlotsFree()).thenReturn(4); // 2 armour 2 structure
        when(mlc.ct.getSlotsFree()).thenReturn(2);
        when(mlc.lt.getSlotsFree()).thenReturn(4);
        when(mlc.ll.getSlotsFree()).thenReturn(7); // 6 structure
        when(mlc.la.getSlotsFree()).thenReturn(1); // 0 structure

        assertEquals(0, cut.getDynamicStructureSlots(mlc.ra));
        assertEquals(4, cut.getDynamicArmourSlots(mlc.ra));

        assertEquals(0, cut.getDynamicStructureSlots(mlc.rt));
        assertEquals(4, cut.getDynamicArmourSlots(mlc.rt));

        assertEquals(0, cut.getDynamicStructureSlots(mlc.rl));
        assertEquals(4, cut.getDynamicArmourSlots(mlc.rl));

        assertEquals(2, cut.getDynamicStructureSlots(mlc.hd));
        assertEquals(2, cut.getDynamicArmourSlots(mlc.hd));

        assertEquals(2, cut.getDynamicStructureSlots(mlc.ct));
        assertEquals(0, cut.getDynamicArmourSlots(mlc.ct));

        assertEquals(4, cut.getDynamicStructureSlots(mlc.lt));
        assertEquals(0, cut.getDynamicArmourSlots(mlc.lt));

        assertEquals(6, cut.getDynamicStructureSlots(mlc.ll));
        assertEquals(0, cut.getDynamicArmourSlots(mlc.ll));

        assertEquals(0, cut.getDynamicStructureSlots(mlc.la));
        assertEquals(0, cut.getDynamicArmourSlots(mlc.la));
    }

    @Test
    public void testOmniMech() {
        // Prepare armour/structure
        final ComponentOmniMech[] internalComponents = new ComponentOmniMech[Location.values().length];
        final ConfiguredComponentOmniMech[] components = new ConfiguredComponentOmniMech[Location.values().length];
        for (final Location location : Location.values()) {
            internalComponents[location.ordinal()] = mock(ComponentOmniMech.class);
            when(internalComponents[location.ordinal()].getLocation()).thenReturn(location);
            components[location.ordinal()] = mock(ConfiguredComponentOmniMech.class);
            when(components[location.ordinal()].getInternalComponent())
                    .thenReturn(internalComponents[location.ordinal()]);
        }

        final int armourSlotsCount = 12;
        final ArmourUpgrade armourType = mock(ArmourUpgrade.class);
        when(armourType.getExtraSlots()).thenReturn(armourSlotsCount);
        when(internalComponents[Location.LeftLeg.ordinal()].getDynamicArmourSlots()).thenReturn(5);
        when(internalComponents[Location.LeftArm.ordinal()].getDynamicArmourSlots()).thenReturn(7);

        final int structSlotsCount = 5;
        final StructureUpgrade aStructureType = mock(StructureUpgrade.class);
        when(aStructureType.getExtraSlots()).thenReturn(structSlotsCount);
        when(internalComponents[Location.RightArm.ordinal()].getDynamicStructureSlots()).thenReturn(2);
        when(internalComponents[Location.RightLeg.ordinal()].getDynamicStructureSlots()).thenReturn(3);

        // Create chassis
        final ChassisOmniMech chassis = new ChassisOmniMech(0, "", "", "", "", 0, ChassisVariant.NORMAL, 0, null,
                Faction.INNERSPHERE, internalComponents, 0, 0, 0, aStructureType, armourType, null, false);

        // Create loadout
        final LoadoutOmniMech loadout = mock(LoadoutOmniMech.class);
        when(loadout.getComponents()).thenReturn(Arrays.asList(components));
        when(loadout.getComponent(any())).then(aInvocation -> {
            return components[aInvocation.getArgumentAt(0, Location.class).ordinal()];
        });
        when(loadout.getChassis()).thenReturn(chassis);

        // (LoadoutOmniMech)
        // factory.produceEmpty(chassisOmniMech);

        // Execute + Verify
        cut = new DynamicSlotDistributor(loadout);

        assertEquals(5, cut.getDynamicArmourSlots(loadout.getComponent(Location.LeftLeg)));
        assertEquals(7, cut.getDynamicArmourSlots(loadout.getComponent(Location.LeftArm)));

        assertEquals(2, cut.getDynamicStructureSlots(loadout.getComponent(Location.RightArm)));
        assertEquals(3, cut.getDynamicStructureSlots(loadout.getComponent(Location.RightLeg)));
    }

    /**
     * Calculates the cumulative number of slots that are free up until the argument according to the priority order of
     * components.
     *
     * @param aPart
     * @return
     */
    private int cumSlotsFree(ConfiguredComponent aPart) {
        int i = priorityOrder.indexOf(aPart);
        int sum = 0;
        while (i > 0) {
            i--;
            sum += priorityOrder.get(i).getSlotsFree();
        }
        return sum;
    }

    private String expectedStructure(int aSlotsTotal) {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (final ConfiguredComponent part : priorityOrder) {
            sb.append(part).append(" = ");
            sb.append(slotsOccupied(part, aSlotsTotal));
            sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Calculates the number of cumulative slots that are occupied by dynamic slots given the maximum number of dynamic
     * slots that can be distributed.
     *
     * @param aComponent
     * @param aSlotsTotal
     * @return
     */
    private int slotsOccupied(ConfiguredComponent aComponent, int aSlotsTotal) {
        return Math.min(aComponent.getSlotsFree(), Math.max(0, aSlotsTotal - cumSlotsFree(aComponent)));
    }
}
