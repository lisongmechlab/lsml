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
package org.lisoft.lsml.model.loadout;

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.chassi.ComponentStandard;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.item.*;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.upgrades.UpgradesMutable;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test suite for {@link LoadoutStandard}.
 *
 * @author Li Song
 */
public class LoadoutStandardTest extends LoadoutTest {
    private final int engineMax = 400;
    private final int engineMin = 0;
    private final List<Modifier> quirks = new ArrayList<>();
    private ChassisStandard chassisStandard;
    private int maxJumpJets = 0;
    private UpgradesMutable upgradesMutable;

    @Override
    @Before
    public void setup() {
        super.setup();
        chassisStandard = mock(ChassisStandard.class);
        upgradesMutable = mock(UpgradesMutable.class);
        chassis = chassisStandard;
        internals = new ComponentStandard[Location.values().length];
        components = new ConfiguredComponentStandard[Location.values().length];
        for (final Location location : Location.values()) {
            final int loc = location.ordinal();
            internals[loc] = mock(ComponentStandard.class);
            components[loc] = mock(ConfiguredComponentStandard.class);

            when(components[loc].getInternalComponent()).thenReturn(internals[loc]);
        }
    }

    @Test
    public void testCanEquip_Engine() throws Exception {
        final Engine item = makeTestItem(0.0, 0, HardPointType.NONE, true, true, true, Engine.class);

        assertEquals(EquipResult.SUCCESS, makeDefaultCUT().canEquipDirectly(item));
    }

    @Test
    public void testCanEquip_JJ() throws Exception {
        maxJumpJets = 1;
        final JumpJet item = makeTestItem(0.0, 0, HardPointType.NONE, true, true, true, JumpJet.class);

        assertEquals(EquipResult.SUCCESS, makeDefaultCUT().canEquipDirectly(item));
    }

    @Test
    public void testCanEquip_NoJJCapactity() throws Exception {
        maxJumpJets = 0;
        final JumpJet item = makeTestItem(0.0, 0, HardPointType.NONE, true, true, true, JumpJet.class);

        assertEquals(EquipResult.make(EquipResultType.JumpJetCapacityReached), makeDefaultCUT().canEquipDirectly(item));
    }

    @Test
    public void testCanEquip_StdEngineNoSpaceCentreTorso() throws Exception {
        final int engineSlots = 4;
        final Engine engine = makeTestItem(0.0, engineSlots, HardPointType.NONE, true, true, false, Engine.class);
        when(engine.getSide()).thenReturn(Optional.empty());

        when(components[Location.CenterTorso.ordinal()].canEquip(engine)).thenReturn(
                EquipResult.make(Location.CenterTorso, EquipResultType.NotEnoughSlots));

        assertEquals(EquipResult.make(Location.CenterTorso, EquipResultType.NotEnoughSlots),
                     makeDefaultCUT().canEquipDirectly(engine));
    }

    @Test
    public void testCanEquip_TooManyEngine() throws Exception {
        final Engine item = makeTestItem(0.0, 0, HardPointType.NONE, true, true, true, Engine.class);
        final List<Item> items = new ArrayList<>();
        items.add(item);
        when(components[Location.CenterTorso.ordinal()].getItemsEquipped()).thenReturn(items);

        assertEquals(EquipResult.make(EquipResultType.EngineAlreadyEquipped), makeDefaultCUT().canEquipDirectly(item));
    }

    @Test
    public void testCanEquip_TooManyJJ() throws Exception {
        maxJumpJets = 1;
        final JumpJet item = makeTestItem(0.0, 0, HardPointType.NONE, true, true, true, JumpJet.class);
        final List<Item> items = new ArrayList<>();
        items.add(item);
        when(components[Location.CenterTorso.ordinal()].getItemsEquipped()).thenReturn(items);

        assertEquals(EquipResult.make(EquipResultType.JumpJetCapacityReached), makeDefaultCUT().canEquipDirectly(item));
    }

    @Test
    public void testCanEquip_XLEngine11SlotsFree() throws Exception {
        final int sideSlots = 3;
        final int engineSlots = 6;
        chassisSlots = sideSlots * 2 + engineSlots - 1;
        final Engine engine = makeTestItem(0.0, engineSlots, HardPointType.NONE, true, true, true, Engine.class);
        final Internal side = makeTestItem(0.0, sideSlots, HardPointType.NONE, true, true, true, Internal.class);
        when(engine.getSide()).thenReturn(Optional.of(side));
        when(components[Location.LeftTorso.ordinal()].getSlotsFree()).thenReturn(sideSlots);
        when(components[Location.RightTorso.ordinal()].getSlotsFree()).thenReturn(sideSlots);
        when(components[Location.CenterTorso.ordinal()].canEquip(engine)).thenReturn(EquipResult.SUCCESS);

        assertEquals(EquipResult.make(EquipResultType.NotEnoughSlots), makeDefaultCUT().canEquipDirectly(engine));
    }

    @Test
    public void testCanEquip_XLEngine12SlotsFree() throws Exception {
        final int sideSlots = 3;
        final int engineSlots = 6;
        chassisSlots = sideSlots * 2 + engineSlots;
        final Engine engine = makeTestItem(0.0, engineSlots, HardPointType.NONE, true, true, true, Engine.class);
        final Internal side = makeTestItem(0.0, sideSlots, HardPointType.NONE, true, true, true, Internal.class);
        when(engine.getSide()).thenReturn(Optional.of(side));
        when(components[Location.LeftTorso.ordinal()].getSlotsFree()).thenReturn(sideSlots);
        when(components[Location.RightTorso.ordinal()].getSlotsFree()).thenReturn(sideSlots);
        when(components[Location.CenterTorso.ordinal()].canEquip(engine)).thenReturn(EquipResult.SUCCESS);

        assertEquals(EquipResult.SUCCESS, makeDefaultCUT().canEquipDirectly(engine));
    }

    @Test
    public void testCanEquip_XLEngineNoSpaceLeftTorso() throws Exception {
        final int sideSlots = 3;
        final Engine engine = makeTestItem(0.0, 0, HardPointType.NONE, true, true, true, Engine.class);
        final Internal side = makeTestItem(0.0, sideSlots, HardPointType.NONE, true, true, true, Internal.class);
        when(engine.getSide()).thenReturn(Optional.of(side));
        when(components[Location.LeftTorso.ordinal()].getSlotsFree()).thenReturn(sideSlots - 1);
        when(components[Location.RightTorso.ordinal()].getSlotsFree()).thenReturn(sideSlots);

        assertEquals(EquipResult.make(Location.LeftTorso, EquipResultType.NotEnoughSlotsForXLSide),
                     makeDefaultCUT().canEquipDirectly(engine));
    }

    @Test
    public void testCanEquip_XLEngineNoSpaceRightTorso() throws Exception {
        final int sideSlots = 3;
        final Engine engine = makeTestItem(0.0, 0, HardPointType.NONE, true, true, true, Engine.class);
        final Internal side = makeTestItem(0.0, sideSlots, HardPointType.NONE, true, true, true, Internal.class);
        when(engine.getSide()).thenReturn(Optional.of(side));
        when(components[Location.LeftTorso.ordinal()].getSlotsFree()).thenReturn(sideSlots);
        when(components[Location.RightTorso.ordinal()].getSlotsFree()).thenReturn(sideSlots - 1);

        assertEquals(EquipResult.make(Location.RightTorso, EquipResultType.NotEnoughSlotsForXLSide),
                     makeDefaultCUT().canEquipDirectly(engine));
    }

    @Test
    public final void testGetAllModifiers() {
        final Modifier quirk1 = mock(Modifier.class);
        final Modifier modifier1 = mock(Modifier.class);
        final Modifier modifier2 = mock(Modifier.class);
        final Modifier modifier3 = mock(Modifier.class);
        final ActiveProbe item1 = mock(ActiveProbe.class); // Use ActiveProbe as it implements ModifierEquipment
        final ActiveProbe item2 = mock(ActiveProbe.class);
        when(item1.getModifiers()).thenReturn(Arrays.asList(modifier1, modifier2));
        when(item2.getModifiers()).thenReturn(Arrays.asList(modifier3));
        when(components[3].getItemsEquipped()).thenReturn(Arrays.asList(item1, item2));
        quirks.add(quirk1);

        final Collection<Modifier> modifiers = makeDefaultCUT().getAllModifiers();
        assertTrue(modifiers.contains(modifier1));
        assertTrue(modifiers.contains(modifier2));
        assertTrue(modifiers.contains(modifier3));
        assertTrue(modifiers.contains(quirk1));
        assertEquals(4, modifiers.size());
    }

    @Test
    public final void testGetEquipmentModifiersNoChassiQuirks() {
        final Modifier quirk1 = mock(Modifier.class);
        final Modifier modifier1 = mock(Modifier.class);
        final Modifier modifier2 = mock(Modifier.class);
        final Modifier modifier3 = mock(Modifier.class);
        final ActiveProbe item1 = mock(ActiveProbe.class); // Use ActiveProbe as it implements ModifierEquipment
        final ActiveProbe item2 = mock(ActiveProbe.class);
        when(item1.getModifiers()).thenReturn(Arrays.asList(modifier1, modifier2));
        when(item2.getModifiers()).thenReturn(Arrays.asList(modifier3));
        when(components[3].getItemsEquipped()).thenReturn(Arrays.asList(item1, item2));
        when(chassisStandard.getQuirks()).thenReturn(Arrays.asList(quirk1));

        final Collection<Modifier> modifiers = makeDefaultCUT().getEquipmentModifiers();
        assertTrue(modifiers.contains(modifier1));
        assertTrue(modifiers.contains(modifier2));
        assertTrue(modifiers.contains(modifier3));
        assertFalse(modifiers.contains(quirk1));
        assertEquals(3, modifiers.size());
    }

    @Test
    public final void testGetHeatSinksCount() throws Exception {
        final List<Item> empty = new ArrayList<>();
        final List<Item> fixed1 = new ArrayList<>();
        final List<Item> fixed2 = new ArrayList<>();
        final List<Item> equipped1 = new ArrayList<>();
        final List<Item> equipped2 = new ArrayList<>();

        final Engine engine = mock(Engine.class);
        when(engine.getNumInternalHeatsinks()).thenReturn(3);

        fixed1.add(ItemDB.BAP);
        fixed1.add(ItemDB.CASE);

        fixed2.add(ItemDB.SHS);

        equipped1.add(ItemDB.AMS);
        equipped1.add(ItemDB.DHS);
        equipped1.add(engine);

        equipped2.add(ItemDB.DHS);
        equipped2.add(ItemDB.DHS);
        equipped2.add(ItemDB.DHS);

        when(components[0].getItemsFixed()).thenReturn(fixed1);
        when(components[0].getItemsEquipped()).thenReturn(empty);
        when(components[1].getItemsFixed()).thenReturn(empty);
        when(components[1].getItemsEquipped()).thenReturn(empty);
        when(components[2].getItemsFixed()).thenReturn(empty);
        when(components[2].getItemsEquipped()).thenReturn(equipped2); // 3 DHS
        when(components[3].getItemsFixed()).thenReturn(fixed2); // 1 SHS
        when(components[3].getItemsEquipped()).thenReturn(empty);
        when(components[4].getItemsFixed()).thenReturn(empty); // 1 SHS
        when(components[4].getItemsEquipped()).thenReturn(equipped1); // 1 DHS + Engine (CT)

        for (int i = 5; i < Location.values().length; ++i) {
            when(components[i].getItemsFixed()).thenReturn(empty);
            when(components[i].getItemsEquipped()).thenReturn(empty);
        }

        assertEquals(8, makeDefaultCUT().getTotalHeatSinksCount());
        assertEquals(5, makeDefaultCUT().getExternalHeatSinksCount());
    }

    @Test
    public void testGetSlotsUsed() throws Exception {
        final Integer armourSlots = 12;
        when(armour.getDynamicSlots()).thenReturn(armourSlots);
        final Integer structureSlots = 15;
        when(structure.getExtraSlots()).thenReturn(structureSlots);

        when(components[0].getSlotsUsed()).thenReturn(2);
        when(components[4].getSlotsUsed()).thenReturn(5);

        final int expectedSlots = armourSlots + structureSlots + 2 + 5;

        assertEquals(expectedSlots, makeDefaultCUT().getSlotsUsed());
    }

    @Override
    protected Loadout makeDefaultCUT() {
        when(chassis.getName()).thenReturn(chassisName);
        when(chassis.getShortName()).thenReturn(chassisShortName);
        when(chassis.getMassMax()).thenReturn(mass);
        when(chassis.getSlotsTotal()).thenReturn(chassisSlots);

        when(chassisStandard.getQuirks()).thenReturn(quirks);
        when(chassisStandard.getJumpJetsMax()).thenReturn(maxJumpJets);
        when(chassisStandard.getEngineMin()).thenReturn(engineMin);
        when(chassisStandard.getEngineMax()).thenReturn(engineMax);

        when(upgradesMutable.getArmour()).thenReturn(armour);
        when(upgradesMutable.getHeatSink()).thenReturn(heatSinks);
        when(upgradesMutable.getStructure()).thenReturn(structure);
        return new LoadoutStandard((ConfiguredComponentStandard[]) components, (ChassisStandard) chassis,
                                   upgradesMutable, weaponGroups);
    }
}
