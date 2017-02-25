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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.Component;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.JumpJet;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.model.upgrades.ArmourUpgrade;
import org.lisoft.lsml.model.upgrades.GuidanceUpgrade;
import org.lisoft.lsml.model.upgrades.HeatSinkUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.lisoft.lsml.util.ListArrayUtils;
import org.mockito.Mockito;

/**
 * Test suite for {@link Loadout}
 *
 * @author Li Song
 */
public abstract class LoadoutTest {
    protected int mass = 75;
    protected int chassisSlots = 10;
    protected String chassisName = "chassis";
    protected String chassisShortName = "short chassis";
    protected MessageXBar xBar;
    protected Chassis chassis;
    protected ConfiguredComponent[] components;
    protected Component[] internals;
    protected HeatSinkUpgrade heatSinks;
    protected StructureUpgrade structure;
    protected ArmourUpgrade armour;
    protected GuidanceUpgrade guidance;
    protected WeaponGroups weaponGroups;
    protected Upgrades upgrades;

    @Before
    public void setup() {
        xBar = Mockito.mock(MessageXBar.class);
        structure = Mockito.mock(StructureUpgrade.class);
        armour = Mockito.mock(ArmourUpgrade.class);
        heatSinks = Mockito.mock(HeatSinkUpgrade.class);
        weaponGroups = Mockito.mock(WeaponGroups.class);
        guidance = Mockito.mock(GuidanceUpgrade.class);
        upgrades = Mockito.mock(Upgrades.class);

        Mockito.when(upgrades.getArmour()).thenReturn(armour);
        Mockito.when(upgrades.getGuidance()).thenReturn(guidance);
        Mockito.when(upgrades.getHeatSink()).thenReturn(heatSinks);
        Mockito.when(upgrades.getStructure()).thenReturn(structure);
    }

    @Test
    public void testCanEquipDirectly() throws Exception {
        final Item item = makeTestItem(0.0, 0, HardPointType.NONE, true, true, true);
        assertEquals(EquipResult.SUCCESS, makeDefaultCUT().canEquipDirectly(item));
    }

    @Test
    public void testCanEquipDirectly_ComponentError() throws Exception {
        final Item item = makeTestItem(0.0, 0, HardPointType.NONE, true, true, false);

        final EquipResult.EquipResultType resultTypes[] = new EquipResult.EquipResultType[] {
                EquipResult.EquipResultType.NoFreeHardPoints, EquipResult.EquipResultType.NoComponentSupport,
                EquipResult.EquipResultType.NotEnoughSlots, EquipResult.EquipResultType.ComponentAlreadyHasCase };

        int typeIndex = 0;
        for (final ConfiguredComponent component : components) {
            Mockito.when(component.getInternalComponent().getLocation()).thenReturn(Location.CenterTorso);
            final EquipResult result = EquipResult.make(component.getInternalComponent().getLocation(),
                    resultTypes[typeIndex]);
            Mockito.when(component.canEquip(item)).thenReturn(result);
            typeIndex = (typeIndex + 1) % resultTypes.length;
        }
        assertEquals(EquipResult.make(EquipResultType.NoFreeHardPoints), makeDefaultCUT().canEquipDirectly(item));
    }

    @Test
    public void testCanEquipDirectly_EngineHs() throws Exception {
        final int componentSlots = 3;
        chassisSlots = componentSlots * components.length;
        final HeatSink item = makeTestItem(0.0, 3, HardPointType.NONE, true, true, true, HeatSink.class);
        for (final ConfiguredComponent component : components) {
            Mockito.when(component.getSlotsUsed()).thenReturn(componentSlots);
            Mockito.when(component.getSlotsFree()).thenReturn(0);

            if (component == components[Location.CenterTorso.ordinal()]) {
                Mockito.when(component.canEquip(item)).thenReturn(EquipResult.SUCCESS);
                Mockito.when(component.getEngineHeatSinksMax()).thenReturn(1);
            }
            else {
                Mockito.when(component.canEquip(item)).thenReturn(EquipResult.make(EquipResultType.NotEnoughSlots));
            }
        }

        assertEquals(EquipResult.SUCCESS, makeDefaultCUT().canEquipDirectly(item));
    }

    @Test
    public void testCanEquipDirectly_EnoughGlobalSlots() throws Exception {
        final int componentSlots = 3;
        chassisSlots = componentSlots * components.length;
        final Item item = makeTestItem(0.0, components.length, HardPointType.NONE, true, true, false);
        for (final ConfiguredComponent component : components) {
            Mockito.when(component.getSlotsUsed()).thenReturn(componentSlots - 1);
            Mockito.when(component.getSlotsFree()).thenReturn(1);
            Mockito.when(component.canEquip(item)).thenReturn(EquipResult.SUCCESS);
        }

        assertEquals(EquipResult.make(EquipResultType.Success), makeDefaultCUT().canEquipDirectly(item));
    }

    @Test
    public void testCanEquipDirectly_NoEngineHs() throws Exception {
        final int componentSlots = 3;
        chassisSlots = componentSlots * components.length;
        final HeatSink item = makeTestItem(0.0, 3, HardPointType.NONE, true, true, true, HeatSink.class);
        for (final ConfiguredComponent component : components) {
            Mockito.when(component.getSlotsUsed()).thenReturn(componentSlots);
            Mockito.when(component.getSlotsFree()).thenReturn(0);
            Mockito.when(component.canEquip(item)).thenReturn(EquipResult.make(EquipResultType.NotEnoughSlots));
        }

        assertEquals(EquipResult.make(EquipResultType.NotEnoughSlots), makeDefaultCUT().canEquipDirectly(item));
    }

    @Test
    public void testCanEquipDirectly_NotCompatibleUpgrades() throws Exception {
        final Item item = makeTestItem(0.0, 0, HardPointType.NONE, false, true, true);
        assertEquals(EquipResult.make(EquipResultType.IncompatibleUpgrades), makeDefaultCUT().canEquipDirectly(item));
    }

    @Test
    public void testCanEquipDirectly_NotSupportedByChassis() throws Exception {
        final Item item = makeTestItem(0.0, 0, HardPointType.NONE, true, false, true);
        assertEquals(EquipResult.make(EquipResultType.NotSupported), makeDefaultCUT().canEquipDirectly(item));
    }

    @Test
    public void testCanEquipDirectly_NotTooHeavy() throws Exception {
        final Loadout cut = makeDefaultCUT();
        final Item item = makeTestItem(mass - cut.getMass(), 0, HardPointType.NONE, true, true, true);
        assertEquals(EquipResult.SUCCESS, cut.canEquipDirectly(item));
    }

    @Test
    public void testCanEquipDirectly_TooFewSlots() throws Exception {
        final int componentSlots = 3;
        chassisSlots = componentSlots * components.length;
        final Item item = makeTestItem(0.0, components.length + 1, HardPointType.NONE, true, true, false);
        for (final ConfiguredComponent component : components) {
            Mockito.when(component.getSlotsUsed()).thenReturn(componentSlots - 1);
            Mockito.when(component.getSlotsFree()).thenReturn(1);
            Mockito.when(component.canEquip(item)).thenReturn(EquipResult.SUCCESS);
        }

        assertEquals(EquipResult.make(EquipResultType.NotEnoughSlots), makeDefaultCUT().canEquipDirectly(item));
    }

    @Test
    public void testCanEquipDirectly_TooHeavy() throws Exception {
        final Item item = makeTestItem(Math.nextAfter((double) mass, Double.POSITIVE_INFINITY), 0, HardPointType.NONE,
                true, true, true);
        assertEquals(EquipResult.make(EquipResultType.TooHeavy), makeDefaultCUT().canEquipDirectly(item));
    }

    @Test
    public void testCanEquipGlobal_NoFreeHardpoints() {
        final Item item = makeTestItem(0.0, 0, HardPointType.ECM, true, true, true);

        assertEquals(EquipResult.make(EquipResultType.NoFreeHardPoints), makeDefaultCUT().canEquipDirectly(item));
    }

    @Test
    public void testCanEquipGlobal_NotAllowedByChassis() {
        final Item item = makeTestItem(0.0, 0, HardPointType.NONE, true, false, true);
        assertEquals(EquipResult.make(EquipResultType.NotSupported), makeDefaultCUT().canEquipDirectly(item));
    }

    @Test
    public void testConstruct_Empty() {
        final Loadout cut = makeDefaultCUT();

        assertEquals(0, cut.getArmour());
        assertEquals(chassis, cut.getChassis());
        assertEquals(0.0, cut.getMass(), 0.0); // The default upgrade has zero mass
        assertEquals(chassisSlots, cut.getFreeSlots()); // No internals
        assertEquals(0, cut.getSlotsUsed());
    }

    @Test
    public final void testGetArmour() throws Exception {
        Mockito.when(components[0].getArmourTotal()).thenReturn(2);
        Mockito.when(components[3].getArmourTotal()).thenReturn(3);
        Mockito.when(components[5].getArmourTotal()).thenReturn(7);

        assertEquals(12, makeDefaultCUT().getArmour());
    }

    @Test
    public void testGetCandidateLocationsForItem_AlreadyHasEngine() throws Exception {
        final Engine item = makeTestItem(0.0, 0, HardPointType.NONE, true, true, true, Engine.class);
        final List<Item> items = new ArrayList<>();
        items.add(item);
        Mockito.when(components[Location.CenterTorso.ordinal()].getItemsEquipped()).thenReturn(items);

        assertTrue(makeDefaultCUT().getCandidateLocationsForItem(item).isEmpty());
    }

    @Test
    public void testGetCandidateLocationsForItem_FreeHardPoints() {
        final Loadout cut = makeDefaultCUT();

        final Location allowed[] = new Location[] { Location.CenterTorso, Location.LeftArm };
        final List<ConfiguredComponent> expected = new ArrayList<>();

        final Item item = makeTestItem(0.0, 0, HardPointType.ENERGY, true, true, true);
        for (final Location location : Location.values()) {
            final int loc = location.ordinal();
            final Component compInternal = internals[loc];
            final ConfiguredComponent compConfigured = components[loc];

            final int freeHardPoints = Arrays.asList(allowed).contains(location) ? 1 : 0;
            if (freeHardPoints > 0) {
                expected.add(compConfigured);
            }

            Mockito.when(compInternal.isAllowed(item, cut.getEngine())).thenReturn(true);
            Mockito.when(compConfigured.getHardPointCount(HardPointType.ENERGY)).thenReturn(freeHardPoints);
            Mockito.when(compConfigured.getItemsOfHardpointType(HardPointType.ENERGY)).thenReturn(0);
            Mockito.when(compConfigured.toString()).thenReturn(location.longName());
        }

        assertTrue(ListArrayUtils.equalsUnordered(expected, cut.getCandidateLocationsForItem(item)));
    }

    @Test
    public void testGetCandidateLocationsForItem_HardPointNone() {
        final Loadout cut = makeDefaultCUT();

        final Location allowed[] = new Location[] { Location.CenterTorso, Location.LeftArm };
        final List<ConfiguredComponent> expected = new ArrayList<>();

        final Item item = makeTestItem(0.0, 0, HardPointType.NONE, true, true, true);
        for (final Location location : Location.values()) {
            final int loc = location.ordinal();
            final Component compInternal = internals[loc];
            final ConfiguredComponent compConfigured = components[loc];

            final boolean isAllowed = Arrays.asList(allowed).contains(location);
            if (isAllowed) {
                expected.add(compConfigured);
            }

            Mockito.when(compInternal.isAllowed(item, cut.getEngine())).thenReturn(isAllowed);
            Mockito.when(compConfigured.toString()).thenReturn(location.longName());
        }

        assertTrue(ListArrayUtils.equalsUnordered(expected, cut.getCandidateLocationsForItem(item)));
    }

    @Test
    public void testGetCandidateLocationsForItem_NoFreeHardPoints() {
        final Loadout cut = makeDefaultCUT();
        final Item item = makeTestItem(0.0, 0, HardPointType.ENERGY, true, true, true);
        for (final Location location : Location.values()) {
            final int loc = location.ordinal();
            final Component compInternal = internals[loc];
            final ConfiguredComponent compConfigured = components[loc];

            Mockito.when(compInternal.isAllowed(item, cut.getEngine())).thenReturn(true);
            Mockito.when(compConfigured.getHardPointCount(HardPointType.ENERGY)).thenReturn(2);
            Mockito.when(compConfigured.getItemsOfHardpointType(HardPointType.ENERGY)).thenReturn(2);
        }

        assertTrue(cut.getCandidateLocationsForItem(item).isEmpty());
    }

    @Test
    public void testGetCandidateLocationsForItem_NoHardPoints() {
        final Loadout cut = makeDefaultCUT();

        final Item item = makeTestItem(0.0, 0, HardPointType.ENERGY, true, true, true);
        for (final Location location : Location.values()) {
            final int loc = location.ordinal();
            final Component compInternal = internals[loc];
            final ConfiguredComponent compConfigured = components[loc];

            Mockito.when(compInternal.isAllowed(item, cut.getEngine())).thenReturn(true);
            Mockito.when(compConfigured.getHardPointCount(HardPointType.ENERGY)).thenReturn(0);
            Mockito.when(compConfigured.getItemsOfHardpointType(HardPointType.ENERGY)).thenReturn(0);
        }

        assertTrue(cut.getCandidateLocationsForItem(item).isEmpty());
    }

    @Test
    public void testGetCandidateLocationsForItem_NoInternalSupport() {
        final Loadout cut = makeDefaultCUT();

        final Item item = makeTestItem(0.0, 0, HardPointType.ENERGY, true, true, true);
        for (final Location location : Location.values()) {
            final int loc = location.ordinal();
            final Component compInternal = internals[loc];
            final ConfiguredComponent compConfigured = components[loc];

            Mockito.when(compInternal.isAllowed(item, cut.getEngine())).thenReturn(false);
            Mockito.when(compConfigured.getHardPointCount(HardPointType.ENERGY)).thenReturn(2);
            Mockito.when(compConfigured.getItemsOfHardpointType(HardPointType.ENERGY)).thenReturn(0);
        }

        assertTrue(cut.getCandidateLocationsForItem(item).isEmpty());
    }

    @Test
    public void testGetCandidateLocationsForItem_NotGloballyFeasible_TooFewSlots() throws Exception {
        final int componentSlots = 3;
        chassisSlots = componentSlots * components.length;
        final Item item = makeTestItem(0.0, chassisSlots - 1, HardPointType.NONE, true, true, false);
        for (final ConfiguredComponent component : components) {
            Mockito.when(component.getSlotsUsed()).thenReturn(componentSlots - 1);
            Mockito.when(component.getSlotsFree()).thenReturn(1);
            Mockito.when(component.canEquip(item)).thenReturn(EquipResult.SUCCESS);
        }

        // Execute + Verify
        assertTrue(makeDefaultCUT().getCandidateLocationsForItem(item).isEmpty());
    }

    @Test
    public void testGetCandidateLocationsForItem_NotGloballyFeasible_TooHeavy() throws Exception {
        final Item item = makeTestItem(mass, 0, HardPointType.NONE, true, true, true, Item.class);
        assertTrue(makeDefaultCUT().getCandidateLocationsForItem(item).isEmpty());
    }

    @Test
    public final void testGetChassis() throws Exception {
        assertSame(chassis, makeDefaultCUT().getChassis());
    }

    @Test
    public final void testGetComponent() throws Exception {
        for (final Location loc : Location.values()) {
            assertSame(components[loc.ordinal()], makeDefaultCUT().getComponent(loc));
        }
    }

    @Test
    public final void testGetComponents() throws Exception {
        final Collection<?> ans = makeDefaultCUT().getComponents();
        assertEquals(components.length, ans.size());

        for (final ConfiguredComponent component : components) {
            assertTrue(ans.contains(component));
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public final void testGetComponents_Immutable() throws Exception {
        makeDefaultCUT().getComponents().add(null);
    }

    @Test
    public final void testGetEfficiencies() throws Exception {
        assertNotSame(makeDefaultCUT(), makeDefaultCUT()); // Unique

        final Loadout cut = makeDefaultCUT();
        assertSame(cut.getEfficiencies(), cut.getEfficiencies()); // Stable
    }

    @Test
    public final void testGetHardpointsCount() throws Exception {
        Mockito.when(components[0].getHardPointCount(HardPointType.ENERGY)).thenReturn(2);
        Mockito.when(components[1].getHardPointCount(HardPointType.ENERGY)).thenReturn(3);
        Mockito.when(components[1].getHardPointCount(HardPointType.BALLISTIC)).thenReturn(5);
        Mockito.when(components[2].getHardPointCount(HardPointType.MISSILE)).thenReturn(7);

        assertEquals(5, makeDefaultCUT().getHardpointsCount(HardPointType.ENERGY));
        assertEquals(5, makeDefaultCUT().getHardpointsCount(HardPointType.BALLISTIC));
        assertEquals(7, makeDefaultCUT().getHardpointsCount(HardPointType.MISSILE));
    }

    @Ignore
    // Needs to be tested in subclasses due to handling of engines.
    @Test
    public final void testGetHeatsinksCount() throws Exception {
        final List<Item> empty = new ArrayList<>();
        final List<Item> fixed1 = new ArrayList<>();
        final List<Item> fixed2 = new ArrayList<>();
        final List<Item> equipped1 = new ArrayList<>();
        final List<Item> equipped2 = new ArrayList<>();

        final Engine engine = Mockito.mock(Engine.class);
        Mockito.when(engine.getNumInternalHeatsinks()).thenReturn(3);

        fixed1.add(ItemDB.BAP);
        fixed1.add(ItemDB.CASE);

        fixed2.add(ItemDB.SHS);

        equipped1.add(ItemDB.AMS);
        equipped1.add(ItemDB.DHS);
        equipped1.add(engine);

        equipped2.add(ItemDB.DHS);
        equipped2.add(ItemDB.DHS);
        equipped2.add(ItemDB.DHS);

        Mockito.when(components[0].getItemsFixed()).thenReturn(fixed1);
        Mockito.when(components[0].getItemsEquipped()).thenReturn(equipped1);
        Mockito.when(components[1].getItemsFixed()).thenReturn(empty);
        Mockito.when(components[1].getItemsEquipped()).thenReturn(empty);
        Mockito.when(components[2].getItemsFixed()).thenReturn(empty);
        Mockito.when(components[2].getItemsEquipped()).thenReturn(equipped2);
        Mockito.when(components[3].getItemsFixed()).thenReturn(fixed2);
        Mockito.when(components[3].getItemsEquipped()).thenReturn(empty);

        for (int i = 4; i < Location.values().length; ++i) {
            Mockito.when(components[i].getItemsFixed()).thenReturn(empty);
            Mockito.when(components[i].getItemsEquipped()).thenReturn(empty);
        }

        assertEquals(8, makeDefaultCUT().getHeatsinksCount());
    }

    @Test
    public void testGetItemsOfHardPointType() throws Exception {
        final HardPointType pointType = HardPointType.ENERGY;
        for (final ConfiguredComponent component : components) {
            Mockito.when(component.getItemsOfHardpointType(pointType)).thenReturn(2);
        }
        assertEquals(components.length * 2, makeDefaultCUT().getItemsOfHardPointType(pointType));
    }

    @Test
    public final void testGetJumpJetCount() throws Exception {
        final List<Item> empty = new ArrayList<>();
        final List<Item> fixed1 = new ArrayList<>();
        final List<Item> fixed2 = new ArrayList<>();
        final List<Item> equipped1 = new ArrayList<>();
        final List<Item> equipped2 = new ArrayList<>();

        final JumpJet jj = Mockito.mock(JumpJet.class);

        fixed1.add(ItemDB.BAP);
        fixed1.add(jj);

        fixed2.add(ItemDB.SHS);

        equipped1.add(ItemDB.AMS);
        equipped1.add(jj);
        equipped1.add(jj);

        equipped2.add(ItemDB.DHS);
        equipped2.add(ItemDB.DHS);
        equipped2.add(ItemDB.DHS);

        Mockito.when(components[0].getItemsFixed()).thenReturn(fixed1);
        Mockito.when(components[0].getItemsEquipped()).thenReturn(equipped1);
        Mockito.when(components[1].getItemsFixed()).thenReturn(empty);
        Mockito.when(components[1].getItemsEquipped()).thenReturn(empty);
        Mockito.when(components[2].getItemsFixed()).thenReturn(empty);
        Mockito.when(components[2].getItemsEquipped()).thenReturn(equipped2);
        Mockito.when(components[3].getItemsFixed()).thenReturn(fixed2);
        Mockito.when(components[3].getItemsEquipped()).thenReturn(empty);

        for (int i = 4; i < Location.values().length; ++i) {
            Mockito.when(components[i].getItemsFixed()).thenReturn(empty);
            Mockito.when(components[i].getItemsEquipped()).thenReturn(empty);
        }

        assertEquals(3, makeDefaultCUT().getJumpJetCount());
    }

    @Test
    public final void testGetJumpJetCount_NoJJ() throws Exception {
        assertEquals(0, makeDefaultCUT().getJumpJetCount());
    }

    @Test
    public final void testGetMassFreeMass() throws Exception {
        Mockito.when(components[0].getItemMass()).thenReturn(2.0);
        Mockito.when(components[3].getItemMass()).thenReturn(3.0);
        Mockito.when(components[5].getItemMass()).thenReturn(7.0);

        Mockito.when(components[0].getArmourTotal()).thenReturn(10);
        Mockito.when(components[3].getArmourTotal()).thenReturn(13);
        Mockito.when(components[5].getArmourTotal()).thenReturn(19);

        Mockito.when(structure.getStructureMass(chassis)).thenReturn(7.3);
        Mockito.when(armour.getArmourMass(42)).thenReturn(4.6);

        assertEquals(23.9, makeDefaultCUT().getMass(), 1E-9);

        Mockito.verify(armour).getArmourMass(42);
        Mockito.verify(structure).getStructureMass(chassis);

        assertEquals(mass - 23.9, makeDefaultCUT().getFreeMass(), 1E-9);
    }

    @Test
    public final void testGetName() throws Exception {
        assertEquals(chassisShortName, makeDefaultCUT().getName());
    }

    @Test
    public void testGetWeaponGroups() {
        final Loadout cut = makeDefaultCUT();
        final WeaponGroups cut_weaponGroups = cut.getWeaponGroups();
        assertNotNull(cut_weaponGroups);
    }

    /**
     * items() shall return an {@link Iterable} that will include all {@link Item}s on the loadout.
     */
    @Test
    public final void testItems_AllItemsAccounted() {
        final List<Item> fixed0 = new ArrayList<>();
        final List<Item> fixed1 = new ArrayList<>();
        final List<Item> fixed2 = new ArrayList<>();
        final List<Item> fixed3 = new ArrayList<>();
        final List<Item> fixed4 = new ArrayList<>();
        final List<Item> fixed5 = new ArrayList<>();
        final List<Item> fixed6 = new ArrayList<>();
        final List<Item> fixed7 = new ArrayList<>();
        final List<Item> equipped0 = new ArrayList<>();
        final List<Item> equipped1 = new ArrayList<>();
        final List<Item> equipped2 = new ArrayList<>();
        final List<Item> equipped3 = new ArrayList<>();
        final List<Item> equipped4 = new ArrayList<>();
        final List<Item> equipped5 = new ArrayList<>();
        final List<Item> equipped6 = new ArrayList<>();
        final List<Item> equipped7 = new ArrayList<>();

        // Non-sense, unique items
        fixed0.add(ItemDB.BAP);
        fixed1.add(ItemDB.CASE);
        fixed2.add(ItemDB.ECM);
        fixed3.add(ItemDB.AMS);
        fixed4.add(ItemDB.C_AMS);
        fixed5.add(ItemDB.DHS);
        fixed6.add(ItemDB.HA);
        fixed7.add(ItemDB.LAA);

        equipped0.add(ItemDB.SHS);
        equipped1.add(ItemDB.UAA);
        equipped2.add(ItemDB.lookup("AC/2"));
        equipped3.add(ItemDB.lookup("AC/5"));
        equipped4.add(ItemDB.lookup("AC/10"));
        equipped5.add(ItemDB.lookup("AC/20"));
        equipped6.add(ItemDB.lookup("SMALL LASER"));
        equipped7.add(ItemDB.lookup("MEDIUM LASER"));
        equipped7.add(ItemDB.lookup("LARGE LASER"));

        final List<Item> expected = new ArrayList<>();
        expected.addAll(fixed0);
        expected.addAll(fixed1);
        expected.addAll(fixed2);
        expected.addAll(fixed3);
        expected.addAll(fixed4);
        expected.addAll(fixed5);
        expected.addAll(fixed6);
        expected.addAll(fixed7);
        expected.addAll(equipped0);
        expected.addAll(equipped1);
        expected.addAll(equipped2);
        expected.addAll(equipped3);
        expected.addAll(equipped4);
        expected.addAll(equipped5);
        expected.addAll(equipped6);
        expected.addAll(equipped7);

        Mockito.when(components[0].getItemsFixed()).thenReturn(fixed0);
        Mockito.when(components[0].getItemsEquipped()).thenReturn(equipped0);
        Mockito.when(components[1].getItemsFixed()).thenReturn(fixed1);
        Mockito.when(components[1].getItemsEquipped()).thenReturn(equipped1);
        Mockito.when(components[2].getItemsFixed()).thenReturn(fixed2);
        Mockito.when(components[2].getItemsEquipped()).thenReturn(equipped2);
        Mockito.when(components[3].getItemsFixed()).thenReturn(fixed3);
        Mockito.when(components[3].getItemsEquipped()).thenReturn(equipped3);
        Mockito.when(components[4].getItemsFixed()).thenReturn(fixed4);
        Mockito.when(components[4].getItemsEquipped()).thenReturn(equipped4);
        Mockito.when(components[5].getItemsFixed()).thenReturn(fixed5);
        Mockito.when(components[5].getItemsEquipped()).thenReturn(equipped5);
        Mockito.when(components[6].getItemsFixed()).thenReturn(fixed6);
        Mockito.when(components[6].getItemsEquipped()).thenReturn(equipped6);
        Mockito.when(components[7].getItemsFixed()).thenReturn(fixed7);
        Mockito.when(components[7].getItemsEquipped()).thenReturn(equipped7);

        final List<Item> ans = new ArrayList<>();
        for (final Item item : makeDefaultCUT().items()) {
            ans.add(item);
        }

        assertTrue(ListArrayUtils.equalsUnordered(expected, ans));
    }

    /**
     * items() shall function correctly even if there are no items on the loadout.
     */
    @Test
    public final void testItems_Empty() {
        final List<Item> empty = new ArrayList<>();
        final List<Item> expected = new ArrayList<>();

        for (int i = 0; i < 8; ++i) {
            Mockito.when(components[i].getItemsFixed()).thenReturn(empty);
            Mockito.when(components[i].getItemsEquipped()).thenReturn(empty);
        }

        final List<Item> ans = new ArrayList<>();
        for (final Item item : makeDefaultCUT().items()) {
            ans.add(item);
        }

        assertTrue(ListArrayUtils.equalsUnordered(expected, ans));
    }

    /**
     * items() shall return an {@link Iterable} that will include all {@link Item}s on the loadout.
     */
    @Test
    public final void testItems_Filter() {
        final List<Item> fixed0 = new ArrayList<>();
        final List<Item> fixed1 = new ArrayList<>();
        final List<Item> fixed2 = new ArrayList<>();
        final List<Item> fixed3 = new ArrayList<>();
        final List<Item> fixed4 = new ArrayList<>();
        final List<Item> fixed5 = new ArrayList<>();
        final List<Item> fixed6 = new ArrayList<>();
        final List<Item> fixed7 = new ArrayList<>();
        final List<Item> equipped0 = new ArrayList<>();
        final List<Item> equipped1 = new ArrayList<>();
        final List<Item> equipped2 = new ArrayList<>();
        final List<Item> equipped3 = new ArrayList<>();
        final List<Item> equipped4 = new ArrayList<>();
        final List<Item> equipped5 = new ArrayList<>();
        final List<Item> equipped6 = new ArrayList<>();
        final List<Item> equipped7 = new ArrayList<>();

        // Non-sense, unique items
        fixed0.add(ItemDB.BAP);
        fixed1.add(ItemDB.lookup("AC/2"));
        fixed2.add(ItemDB.ECM);
        fixed3.add(ItemDB.AMS);
        fixed4.add(ItemDB.lookup("SMALL LASER"));
        fixed5.add(ItemDB.DHS);
        fixed6.add(ItemDB.HA);
        fixed7.add(ItemDB.LAA);

        equipped0.add(ItemDB.SHS);
        equipped1.add(ItemDB.UAA);
        equipped2.add(ItemDB.CASE);
        equipped3.add(ItemDB.lookup("AC/5"));
        equipped4.add(ItemDB.lookup("AC/10"));
        equipped5.add(ItemDB.lookup("AC/20"));
        equipped6.add(ItemDB.C_AMS);
        equipped7.add(ItemDB.lookup("MEDIUM LASER"));
        equipped7.add(ItemDB.lookup("LARGE LASER"));

        final List<Item> expected = new ArrayList<>();
        expected.add(ItemDB.lookup("AC/2"));
        expected.add(ItemDB.AMS);
        expected.add(ItemDB.lookup("SMALL LASER"));
        expected.add(ItemDB.lookup("AC/5"));
        expected.add(ItemDB.lookup("AC/10"));
        expected.add(ItemDB.lookup("AC/20"));
        expected.add(ItemDB.C_AMS);
        expected.add(ItemDB.lookup("MEDIUM LASER"));
        expected.add(ItemDB.lookup("LARGE LASER"));

        Mockito.when(components[0].getItemsFixed()).thenReturn(fixed0);
        Mockito.when(components[0].getItemsEquipped()).thenReturn(equipped0);
        Mockito.when(components[1].getItemsFixed()).thenReturn(fixed1);
        Mockito.when(components[1].getItemsEquipped()).thenReturn(equipped1);
        Mockito.when(components[2].getItemsFixed()).thenReturn(fixed2);
        Mockito.when(components[2].getItemsEquipped()).thenReturn(equipped2);
        Mockito.when(components[3].getItemsFixed()).thenReturn(fixed3);
        Mockito.when(components[3].getItemsEquipped()).thenReturn(equipped3);
        Mockito.when(components[4].getItemsFixed()).thenReturn(fixed4);
        Mockito.when(components[4].getItemsEquipped()).thenReturn(equipped4);
        Mockito.when(components[5].getItemsFixed()).thenReturn(fixed5);
        Mockito.when(components[5].getItemsEquipped()).thenReturn(equipped5);
        Mockito.when(components[6].getItemsFixed()).thenReturn(fixed6);
        Mockito.when(components[6].getItemsEquipped()).thenReturn(equipped6);
        Mockito.when(components[7].getItemsFixed()).thenReturn(fixed7);
        Mockito.when(components[7].getItemsEquipped()).thenReturn(equipped7);

        final List<Item> ans = new ArrayList<>();
        for (final Weapon item : makeDefaultCUT().items(Weapon.class)) {
            ans.add(item);
        }

        assertTrue(ListArrayUtils.equalsUnordered(expected, ans));
    }

    /**
     * items() shall function correctly even if no item is included in filter.
     */
    @Test
    public final void testItems_FilterEmpty() {
        final List<Item> empty = new ArrayList<>();
        empty.add(ItemDB.SHS);
        final List<Item> expected = new ArrayList<>();

        for (int i = 0; i < 8; ++i) {
            Mockito.when(components[i].getItemsFixed()).thenReturn(empty);
            Mockito.when(components[i].getItemsEquipped()).thenReturn(empty);
        }

        final List<Item> ans = new ArrayList<>();
        for (final Item item : makeDefaultCUT().items(Weapon.class)) {
            ans.add(item);
        }

        assertTrue(ListArrayUtils.equalsUnordered(expected, ans));
    }

    @Test
    public final void testToString() throws Exception {
        final Loadout cut = makeDefaultCUT();
        final String name = "mamboyeeya";
        cut.setName(name);

        assertEquals(name + " (" + chassis.getNameShort() + ")", cut.toString());
    }

    protected abstract Loadout makeDefaultCUT();

    protected Item makeTestItem(double aMass, int aNumCriticals, HardPointType aHardPointType, boolean aIsCompatible,
            boolean aIsAllowed, boolean aIsAllowedOnAllComponents) {
        return makeTestItem(aMass, aNumCriticals, aHardPointType, aIsCompatible, aIsAllowed, aIsAllowedOnAllComponents,
                Item.class);
    }

    protected <T extends Item> T makeTestItem(double aMass, int aNumCriticals, HardPointType aHardPointType,
            boolean aIsCompatible, boolean aIsAllowed, boolean aIsAllowedOnAllComponents, Class<T> aClass) {
        final T item = Mockito.mock(aClass);
        Mockito.when(item.getMass()).thenReturn(aMass);
        Mockito.when(item.getHardpointType()).thenReturn(aHardPointType);
        Mockito.when(item.isCompatible(any(Upgrades.class))).thenReturn(aIsCompatible);
        Mockito.when(item.getSlots()).thenReturn(aNumCriticals);
        Mockito.when(chassis.isAllowed(item)).thenReturn(aIsAllowed);
        if (aIsAllowedOnAllComponents) {
            for (final ConfiguredComponent component : components) {
                Mockito.when(component.canEquip(item)).thenReturn(EquipResult.SUCCESS);
            }
        }
        return item;
    }
}
