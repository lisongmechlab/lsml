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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.chassi.ComponentStandard;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.EngineType;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.JumpJet;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentStandard;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.upgrades.UpgradesMutable;
import org.mockito.Mockito;

/**
 * Test suite for {@link LoadoutStandard}.
 * 
 * @author Li Song
 */
public class LoadoutStandardTest extends LoadoutTest {
    private int engineMin = 0;
    private int engineMax = 400;
    private int maxJumpJets = 0;
    private List<Modifier> quirks = new ArrayList<>();
    private ChassisStandard chassisStandard;
    private UpgradesMutable upgradesMutable;

    @Override
    protected Loadout makeDefaultCUT() {
        Mockito.when(chassis.getName()).thenReturn(chassisName);
        Mockito.when(chassis.getNameShort()).thenReturn(chassisShortName);
        Mockito.when(chassis.getMassMax()).thenReturn(mass);
        Mockito.when(chassis.getCriticalSlotsTotal()).thenReturn(chassisSlots);

        Mockito.when(chassisStandard.getQuirks()).thenReturn(quirks);
        Mockito.when(chassisStandard.getJumpJetsMax()).thenReturn(maxJumpJets);
        Mockito.when(chassisStandard.getEngineMin()).thenReturn(engineMin);
        Mockito.when(chassisStandard.getEngineMax()).thenReturn(engineMax);

        Mockito.when(upgradesMutable.getArmour()).thenReturn(armour);
        Mockito.when(upgradesMutable.getHeatSink()).thenReturn(heatSinks);
        Mockito.when(upgradesMutable.getStructure()).thenReturn(structure);
        return new LoadoutStandard((ConfiguredComponentStandard[]) components, (ChassisStandard) chassis,
                upgradesMutable, weaponGroups);
    }

    @Override
    @Before
    public void setup() {
        super.setup();
        chassisStandard = Mockito.mock(ChassisStandard.class);
        upgradesMutable = Mockito.mock(UpgradesMutable.class);
        chassis = chassisStandard;
        internals = new ComponentStandard[Location.values().length];
        components = new ConfiguredComponentStandard[Location.values().length];
        for (Location location : Location.values()) {
            int loc = location.ordinal();
            internals[loc] = Mockito.mock(ComponentStandard.class);
            components[loc] = Mockito.mock(ConfiguredComponentStandard.class);

            Mockito.when(components[loc].getInternalComponent()).thenReturn(internals[loc]);
        }
    }

    @Test
    public void testCanEquip_NoJJCapactity() throws Exception {
        maxJumpJets = 0;
        JumpJet item = makeTestItem(0.0, 0, HardPointType.NONE, true, true, true, JumpJet.class);

        assertEquals(EquipResult.make(EquipResultType.JumpJetCapacityReached), makeDefaultCUT().canEquipDirectly(item));
    }

    @Test
    public void testCanEquip_JJ() throws Exception {
        maxJumpJets = 1;
        JumpJet item = makeTestItem(0.0, 0, HardPointType.NONE, true, true, true, JumpJet.class);

        assertEquals(EquipResult.SUCCESS, makeDefaultCUT().canEquipDirectly(item));
    }

    @Test
    public void testCanEquip_TooManyJJ() throws Exception {
        maxJumpJets = 1;
        JumpJet item = makeTestItem(0.0, 0, HardPointType.NONE, true, true, true, JumpJet.class);
        List<Item> items = new ArrayList<>();
        items.add(item);
        Mockito.when(components[Location.CenterTorso.ordinal()].getItemsEquipped()).thenReturn(items);

        assertEquals(EquipResult.make(EquipResultType.JumpJetCapacityReached), makeDefaultCUT().canEquipDirectly(item));
    }

    @Test
    public void testCanEquip_Engine() throws Exception {
        Engine item = makeTestItem(0.0, 0, HardPointType.NONE, true, true, true, Engine.class);

        assertEquals(EquipResult.SUCCESS, makeDefaultCUT().canEquipDirectly(item));
    }

    @Test
    public void testCanEquip_TooManyEngine() throws Exception {
        Engine item = makeTestItem(0.0, 0, HardPointType.NONE, true, true, true, Engine.class);
        List<Item> items = new ArrayList<>();
        items.add(item);
        Mockito.when(components[Location.CenterTorso.ordinal()].getItemsEquipped()).thenReturn(items);

        assertEquals(EquipResult.make(EquipResultType.EngineAlreadyEquipped), makeDefaultCUT().canEquipDirectly(item));
    }

    @Test
    public void testCanEquip_XLEngineNoSpaceLeftTorso() throws Exception {
        final int sideSlots = 3;
        Engine engine = makeTestItem(0.0, 0, HardPointType.NONE, true, true, true, Engine.class);
        Internal side = makeTestItem(0.0, sideSlots, HardPointType.NONE, true, true, true, Internal.class);
        Mockito.when(engine.getSide()).thenReturn(side);
        Mockito.when(engine.getType()).thenReturn(EngineType.XL);
        Mockito.when(components[Location.LeftTorso.ordinal()].getSlotsFree()).thenReturn(sideSlots - 1);
        Mockito.when(components[Location.RightTorso.ordinal()].getSlotsFree()).thenReturn(sideSlots);

        assertEquals(EquipResult.make(Location.LeftTorso, EquipResultType.NotEnoughSlotsForXLSide),
                makeDefaultCUT().canEquipDirectly(engine));
    }

    @Test
    public void testCanEquip_XLEngineNoSpaceRightTorso() throws Exception {
        final int sideSlots = 3;
        Engine engine = makeTestItem(0.0, 0, HardPointType.NONE, true, true, true, Engine.class);
        Internal side = makeTestItem(0.0, sideSlots, HardPointType.NONE, true, true, true, Internal.class);
        Mockito.when(engine.getSide()).thenReturn(side);
        Mockito.when(engine.getType()).thenReturn(EngineType.XL);
        Mockito.when(components[Location.LeftTorso.ordinal()].getSlotsFree()).thenReturn(sideSlots);
        Mockito.when(components[Location.RightTorso.ordinal()].getSlotsFree()).thenReturn(sideSlots - 1);

        assertEquals(EquipResult.make(Location.RightTorso, EquipResultType.NotEnoughSlotsForXLSide),
                makeDefaultCUT().canEquipDirectly(engine));
    }

    @Test
    public void testCanEquip_XLEngineNoSpaceCentreTorso() throws Exception {
        final int engineSlots = 4;
        Engine engine = makeTestItem(0.0, engineSlots, HardPointType.NONE, true, true, false, Engine.class);
        Mockito.when(engine.getType()).thenReturn(EngineType.STD);

        Mockito.when(components[Location.CenterTorso.ordinal()].canEquip(engine))
                .thenReturn(EquipResult.make(Location.CenterTorso, EquipResultType.NotEnoughSlots));

        assertEquals(EquipResult.make(Location.CenterTorso, EquipResultType.NotEnoughSlots),
                makeDefaultCUT().canEquipDirectly(engine));
    }

    @Test
    public void testCanEquip_XLEngine12SlotsFree() throws Exception {
        final int sideSlots = 3;
        final int engineSlots = 6;
        chassisSlots = sideSlots * 2 + engineSlots;
        Engine engine = makeTestItem(0.0, engineSlots, HardPointType.NONE, true, true, true, Engine.class);
        Internal side = makeTestItem(0.0, sideSlots, HardPointType.NONE, true, true, true, Internal.class);
        Mockito.when(engine.getSide()).thenReturn(side);
        Mockito.when(engine.getType()).thenReturn(EngineType.XL);
        Mockito.when(components[Location.LeftTorso.ordinal()].getSlotsFree()).thenReturn(sideSlots);
        Mockito.when(components[Location.RightTorso.ordinal()].getSlotsFree()).thenReturn(sideSlots);
        Mockito.when(components[Location.CenterTorso.ordinal()].canEquip(engine)).thenReturn(EquipResult.SUCCESS);

        assertEquals(EquipResult.SUCCESS, makeDefaultCUT().canEquipDirectly(engine));
    }

    @Test
    public void testCanEquip_XLEngine11SlotsFree() throws Exception {
        final int sideSlots = 3;
        final int engineSlots = 6;
        chassisSlots = sideSlots * 2 + engineSlots - 1;
        Engine engine = makeTestItem(0.0, engineSlots, HardPointType.NONE, true, true, true, Engine.class);
        Internal side = makeTestItem(0.0, sideSlots, HardPointType.NONE, true, true, true, Internal.class);
        Mockito.when(engine.getSide()).thenReturn(side);
        Mockito.when(engine.getType()).thenReturn(EngineType.XL);
        Mockito.when(components[Location.LeftTorso.ordinal()].getSlotsFree()).thenReturn(sideSlots);
        Mockito.when(components[Location.RightTorso.ordinal()].getSlotsFree()).thenReturn(sideSlots);
        Mockito.when(components[Location.CenterTorso.ordinal()].canEquip(engine)).thenReturn(EquipResult.SUCCESS);

        assertEquals(EquipResult.make(EquipResultType.NotEnoughSlots), makeDefaultCUT().canEquipDirectly(engine));
    }
}
