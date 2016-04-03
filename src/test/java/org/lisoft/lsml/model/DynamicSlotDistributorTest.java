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
import org.lisoft.lsml.model.upgrades.ArmorUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DynamicSlotDistributorTest {
    MockLoadoutContainer      mlc = new MockLoadoutContainer();
    List<ConfiguredComponent> priorityOrder;
    DynamicSlotDistributor    cut;

    @Before
    public void setup() {
        cut = new DynamicSlotDistributor(mlc.loadout);

        // Priority order: RA, RT, RL, HD, CT, LT, LL, LA
        priorityOrder = Arrays.asList(mlc.ra, mlc.rt, mlc.rl, mlc.hd, mlc.ct, mlc.lt, mlc.ll, mlc.la);
    }

    @Test
    public void testGetDynamicStructureSlotsForComponent_NoUpgrades() {
        when(mlc.upgrades.getStructure()).thenReturn(UpgradeDB.IS_STD_STRUCTURE);
        when(mlc.upgrades.getArmor()).thenReturn(UpgradeDB.IS_STD_ARMOR);

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

        assertEquals(0, cut.getDynamicArmorSlots(mlc.ra));
        assertEquals(0, cut.getDynamicArmorSlots(mlc.rt));
        assertEquals(0, cut.getDynamicArmorSlots(mlc.rl));
        assertEquals(0, cut.getDynamicArmorSlots(mlc.hd));
        assertEquals(0, cut.getDynamicArmorSlots(mlc.ct));
        assertEquals(0, cut.getDynamicArmorSlots(mlc.lt));
        assertEquals(0, cut.getDynamicArmorSlots(mlc.ll));
        assertEquals(0, cut.getDynamicArmorSlots(mlc.la));
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

    private String expectedStructure(int aSlotsTotal) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (ConfiguredComponent part : priorityOrder) {
            sb.append(part).append(" = ");
            sb.append(slotsOccupied(part, aSlotsTotal));
            sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

    @Test
    public void testGetDynamicStructureSlotsForComponent_Priority() {
        when(mlc.upgrades.getStructure()).thenReturn(UpgradeDB.IS_ES_STRUCTURE);
        when(mlc.upgrades.getArmor()).thenReturn(UpgradeDB.IS_STD_ARMOR);

        when(mlc.ra.getSlotsFree()).thenReturn(12);
        when(mlc.rt.getSlotsFree()).thenReturn(12);
        when(mlc.rl.getSlotsFree()).thenReturn(12);
        when(mlc.hd.getSlotsFree()).thenReturn(12);
        when(mlc.ct.getSlotsFree()).thenReturn(12);
        when(mlc.ll.getSlotsFree()).thenReturn(12);
        when(mlc.lt.getSlotsFree()).thenReturn(12);
        when(mlc.la.getSlotsFree()).thenReturn(12);

        for (ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
        }

        when(mlc.ra.getSlotsFree()).thenReturn(1);
        for (ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
        }

        when(mlc.rt.getSlotsFree()).thenReturn(1);
        for (ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
        }

        when(mlc.rl.getSlotsFree()).thenReturn(2);
        for (ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
        }

        when(mlc.hd.getSlotsFree()).thenReturn(1);
        for (ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
        }

        when(mlc.ct.getSlotsFree()).thenReturn(3);
        for (ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
        }

        when(mlc.lt.getSlotsFree()).thenReturn(1);
        for (ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
        }

        when(mlc.ll.getSlotsFree()).thenReturn(1);
        for (ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
        }

        when(mlc.la.getSlotsFree()).thenReturn(1);
        for (ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
        }
        // Slot overflow, fail graciously, no exceptions thrown
    }

    @Test
    public void testGetDynamicArmorSlotsForComponent_Priority() {
        when(mlc.upgrades.getStructure()).thenReturn(UpgradeDB.IS_STD_STRUCTURE);
        when(mlc.upgrades.getArmor()).thenReturn(UpgradeDB.IS_FF_ARMOR);

        when(mlc.ra.getSlotsFree()).thenReturn(12);
        when(mlc.rt.getSlotsFree()).thenReturn(12);
        when(mlc.rl.getSlotsFree()).thenReturn(12);
        when(mlc.hd.getSlotsFree()).thenReturn(12);
        when(mlc.ct.getSlotsFree()).thenReturn(12);
        when(mlc.ll.getSlotsFree()).thenReturn(12);
        when(mlc.lt.getSlotsFree()).thenReturn(12);
        when(mlc.la.getSlotsFree()).thenReturn(12);

        for (ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
        }

        when(mlc.ra.getSlotsFree()).thenReturn(1);
        for (ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
        }

        when(mlc.rt.getSlotsFree()).thenReturn(1);
        for (ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
        }

        when(mlc.rl.getSlotsFree()).thenReturn(2);
        for (ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
        }

        when(mlc.hd.getSlotsFree()).thenReturn(1);
        for (ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
        }

        when(mlc.ct.getSlotsFree()).thenReturn(3);
        for (ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
        }

        when(mlc.lt.getSlotsFree()).thenReturn(1);
        for (ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
        }

        when(mlc.ll.getSlotsFree()).thenReturn(1);
        for (ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
        }

        when(mlc.la.getSlotsFree()).thenReturn(1);
        for (ConfiguredComponent part : priorityOrder) {
            assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
        }
        // Slot overflow, fail graciously, no exceptions thrown
    }

    /**
     * Dynamic armor slots are distributed before dynamic structure (arbitrary design decision).
     */
    @Test
    public void testMixedArmorStructurePriority() {
        when(mlc.upgrades.getStructure()).thenReturn(UpgradeDB.IS_ES_STRUCTURE);
        when(mlc.upgrades.getArmor()).thenReturn(UpgradeDB.IS_FF_ARMOR);

        when(mlc.ra.getSlotsFree()).thenReturn(4); // 4 armor
        when(mlc.rt.getSlotsFree()).thenReturn(4);
        when(mlc.rl.getSlotsFree()).thenReturn(4);
        when(mlc.hd.getSlotsFree()).thenReturn(4); // 2 armor 2 structure
        when(mlc.ct.getSlotsFree()).thenReturn(2);
        when(mlc.lt.getSlotsFree()).thenReturn(4);
        when(mlc.ll.getSlotsFree()).thenReturn(7); // 6 structure
        when(mlc.la.getSlotsFree()).thenReturn(1); // 0 structure

        assertEquals(0, cut.getDynamicStructureSlots(mlc.ra));
        assertEquals(4, cut.getDynamicArmorSlots(mlc.ra));

        assertEquals(0, cut.getDynamicStructureSlots(mlc.rt));
        assertEquals(4, cut.getDynamicArmorSlots(mlc.rt));

        assertEquals(0, cut.getDynamicStructureSlots(mlc.rl));
        assertEquals(4, cut.getDynamicArmorSlots(mlc.rl));

        assertEquals(2, cut.getDynamicStructureSlots(mlc.hd));
        assertEquals(2, cut.getDynamicArmorSlots(mlc.hd));

        assertEquals(2, cut.getDynamicStructureSlots(mlc.ct));
        assertEquals(0, cut.getDynamicArmorSlots(mlc.ct));

        assertEquals(4, cut.getDynamicStructureSlots(mlc.lt));
        assertEquals(0, cut.getDynamicArmorSlots(mlc.lt));

        assertEquals(6, cut.getDynamicStructureSlots(mlc.ll));
        assertEquals(0, cut.getDynamicArmorSlots(mlc.ll));

        assertEquals(0, cut.getDynamicStructureSlots(mlc.la));
        assertEquals(0, cut.getDynamicArmorSlots(mlc.la));
    }

    @Test
    public void testOmniMech() {
        // Prepare armor/structure
        ComponentOmniMech[] internalComponents = new ComponentOmniMech[Location.values().length];
        ConfiguredComponentOmniMech[] components = new ConfiguredComponentOmniMech[Location.values().length];
        for (Location location : Location.values()) {
            internalComponents[location.ordinal()] = mock(ComponentOmniMech.class);
            when(internalComponents[location.ordinal()].getLocation()).thenReturn(location);
            components[location.ordinal()] = mock(ConfiguredComponentOmniMech.class);
            when(components[location.ordinal()].getInternalComponent())
                    .thenReturn(internalComponents[location.ordinal()]);
        }

        int armorSlotsCount = 12;
        ArmorUpgrade armorType = mock(ArmorUpgrade.class);
        when(armorType.getExtraSlots()).thenReturn(armorSlotsCount);
        when(internalComponents[Location.LeftLeg.ordinal()].getDynamicArmorSlots()).thenReturn(5);
        when(internalComponents[Location.LeftArm.ordinal()].getDynamicArmorSlots()).thenReturn(7);

        int structSlotsCount = 5;
        StructureUpgrade aStructureType = mock(StructureUpgrade.class);
        when(aStructureType.getExtraSlots()).thenReturn(structSlotsCount);
        when(internalComponents[Location.RightArm.ordinal()].getDynamicStructureSlots()).thenReturn(2);
        when(internalComponents[Location.RightLeg.ordinal()].getDynamicStructureSlots()).thenReturn(3);

        // Create chassis
        ChassisOmniMech chassis = new ChassisOmniMech(0, "", "", "", "", 0, ChassisVariant.NORMAL, 0, null,
                Faction.INNERSPHERE, internalComponents, 0, 0, 0, aStructureType, armorType, null, false);

        // Create loadout
        LoadoutOmniMech loadout = mock(LoadoutOmniMech.class);
        when(loadout.getComponents()).thenReturn(Arrays.asList(components));
        when(loadout.getComponent(any())).then(aInvocation -> {
            return components[aInvocation.getArgumentAt(0, Location.class).ordinal()];
        });
        when(loadout.getChassis()).thenReturn(chassis);

        // (LoadoutOmniMech)
        // factory.produceEmpty(chassisOmniMech);

        // Execute + Verify
        cut = new DynamicSlotDistributor(loadout);

        assertEquals(5, cut.getDynamicArmorSlots(loadout.getComponent(Location.LeftLeg)));
        assertEquals(7, cut.getDynamicArmorSlots(loadout.getComponent(Location.LeftArm)));

        assertEquals(2, cut.getDynamicStructureSlots(loadout.getComponent(Location.RightArm)));
        assertEquals(3, cut.getDynamicStructureSlots(loadout.getComponent(Location.RightLeg)));
    }
}
