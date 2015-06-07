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
package org.lisoft.lsml.model.chassi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.EngineType;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.ItemDB;
import org.lisoft.lsml.model.item.JumpJet;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.upgrades.ArmorUpgrade;
import org.lisoft.lsml.model.upgrades.HeatSinkUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * A test suite for {@link ChassisOmniMech}.
 * 
 * @author Emily Björk
 */
public class ChassisOmniMechTest extends ChassisBaseTest {
    private ArmorUpgrade              armorType;
    private ComponentOmniMech[]       components;
    private Engine                    engine;
    private HeatSinkUpgrade           heatSinkType;
    private Map<Location, List<Item>> items;
    private StructureUpgrade          structureType;

    @Override
    @Before
    public void setup() {
        super.setup();

        engine = Mockito.mock(Engine.class);
        Mockito.when(engine.getFaction()).thenReturn(faction);
        Mockito.when(engine.getHardpointType()).thenReturn(HardPointType.NONE);
        Mockito.when(engine.getRating()).thenReturn(250);
        Mockito.when(engine.getType()).thenReturn(EngineType.XL);
        Mockito.when(engine.isCompatible(Matchers.any(Upgrades.class))).thenReturn(true);

        structureType = Mockito.mock(StructureUpgrade.class);
        armorType = Mockito.mock(ArmorUpgrade.class);
        heatSinkType = Mockito.mock(HeatSinkUpgrade.class);

        items = new HashMap<>();
        components = new ComponentOmniMech[Location.values().length];
        for (Location location : Location.values()) {
            items.put(location, new ArrayList<Item>());
            components[location.ordinal()] = Mockito.mock(ComponentOmniMech.class);
            Mockito.when(components[location.ordinal()].isAllowed(Matchers.any(Item.class))).thenReturn(true);
            Mockito.when(components[location.ordinal()].isAllowed(Matchers.any(Item.class), Matchers.any(Engine.class)))
                    .thenReturn(true);
            Mockito.when(components[location.ordinal()].getFixedItems()).thenReturn(items.get(location));
        }
        componentBases = components;

        items.get(Location.CenterTorso).add(engine);

        // Mockito.when(components[Location.Head.ordinal()].getArmorMax()).thenReturn(18);
        // Mockito.when(components[Location.LeftArm.ordinal()].getArmorMax()).thenReturn(48);
        // Mockito.when(components[Location.LeftTorso.ordinal()].getArmorMax()).thenReturn(64);
        // Mockito.when(components[Location.LeftLeg.ordinal()].getArmorMax()).thenReturn(64);
        // Mockito.when(components[Location.CenterTorso.ordinal()].getArmorMax()).thenReturn(92);
        // Mockito.when(components[Location.RightArm.ordinal()].getArmorMax()).thenReturn(48);
        // Mockito.when(components[Location.RightTorso.ordinal()].getArmorMax()).thenReturn(64);
        // Mockito.when(components[Location.RightLeg.ordinal()].getArmorMax()).thenReturn(64);

        Mockito.when(components[Location.Head.ordinal()].getDynamicArmorSlots()).thenReturn(1);
        Mockito.when(components[Location.CenterTorso.ordinal()].getDynamicArmorSlots()).thenReturn(1);
        Mockito.when(components[Location.LeftTorso.ordinal()].getDynamicArmorSlots()).thenReturn(3);
        Mockito.when(components[Location.RightArm.ordinal()].getDynamicArmorSlots()).thenReturn(2);

        Mockito.when(components[Location.LeftLeg.ordinal()].getDynamicStructureSlots()).thenReturn(1);
        Mockito.when(components[Location.RightTorso.ordinal()].getDynamicStructureSlots()).thenReturn(1);
        Mockito.when(components[Location.LeftArm.ordinal()].getDynamicStructureSlots()).thenReturn(3);
        Mockito.when(components[Location.RightLeg.ordinal()].getDynamicStructureSlots()).thenReturn(2);

        Mockito.when(structureType.getExtraSlots()).thenReturn(7);
        Mockito.when(armorType.getExtraSlots()).thenReturn(7);
    }

    @SuppressWarnings("unused")
    // Expecting exception
    @Test(expected = IllegalArgumentException.class)
    public final void testCtor_BadDynArmor() {
        Mockito.when(components[Location.Head.ordinal()].getDynamicArmorSlots()).thenReturn(13);
        new ChassisOmniMech(mwoID, mwoName, series, name, shortName, maxTons, variant, baseVariant, movementProfile,
                faction, components, maxPilotModules, maxConsumableModules, maxWeaponModules, structureType, armorType,
                heatSinkType);
    }

    @SuppressWarnings("unused")
    // Expecting exception
    @Test(expected = IllegalArgumentException.class)
    public final void testCtor_BadDynStructure() {
        Mockito.when(components[Location.Head.ordinal()].getDynamicStructureSlots()).thenReturn(13);
        new ChassisOmniMech(mwoID, mwoName, series, name, shortName, maxTons, variant, baseVariant, movementProfile,
                faction, components, maxPilotModules, maxConsumableModules, maxWeaponModules, structureType, armorType,
                heatSinkType);
    }

    @Test
    public final void testGetFixedArmorType() {
        assertSame(armorType, makeDefaultCUT().getFixedArmorType());
    }

    @Test
    public final void testGetFixedEngine() {
        assertSame(engine, makeDefaultCUT().getFixedEngine());
    }

    @Test(expected = IllegalStateException.class)
    public final void testGetFixedEngine_noEngine() {
        Item item1 = Mockito.mock(Item.class);
        HeatSink hs1 = Mockito.mock(HeatSink.class);

        items.get(Location.LeftArm).add(item1);
        items.get(Location.RightTorso).add(hs1);
        items.get(Location.RightTorso).add(item1);
        items.get(Location.CenterTorso).clear(); // Remove engine.
        items.get(Location.CenterTorso).add(hs1);
        items.get(Location.CenterTorso).add(item1);

        makeDefaultCUT().getFixedEngine();
    }

    /**
     * {@link ChassisOmniMech#getFixedMass()} shall return the mass of the chassis with all non fixed items and armor
     * removed.
     */
    @Test
    public final void testGetFixedHeatSinks() {
        ChassisOmniMech cut = makeDefaultCUT();

        List<Item> fixed1 = new ArrayList<>();
        List<Item> fixed2 = new ArrayList<>();
        List<Item> fixed3 = new ArrayList<>();

        Item item1 = Mockito.mock(Item.class);
        HeatSink hs1 = Mockito.mock(HeatSink.class);
        HeatSink hs2 = Mockito.mock(HeatSink.class);

        Mockito.when(item1.getMass()).thenReturn(1.0);
        Mockito.when(hs1.getMass()).thenReturn(2.0);
        Mockito.when(hs2.getMass()).thenReturn(3.0);

        fixed1.add(item1);
        fixed1.add(hs1);
        fixed2.add(hs1);
        fixed2.add(hs1);
        fixed3.add(hs2);

        Mockito.when(components[2].getFixedItems()).thenReturn(fixed1);
        Mockito.when(components[3].getFixedItems()).thenReturn(fixed2);
        Mockito.when(components[5].getFixedItems()).thenReturn(fixed3);

        assertEquals(4, cut.getFixedHeatSinks());
    }

    @Test
    public final void testGetFixedHeatSinkType() {
        assertSame(heatSinkType, makeDefaultCUT().getFixedHeatSinkType());
    }

    @Test
    public final void testGetFixedJumpJets() {
        List<Item> fixed1 = new ArrayList<>();
        List<Item> fixed2 = new ArrayList<>();
        List<Item> fixed3 = new ArrayList<>();

        Item item1 = Mockito.mock(Item.class);
        JumpJet jj1 = Mockito.mock(JumpJet.class);
        JumpJet jj2 = Mockito.mock(JumpJet.class);

        fixed1.add(item1);
        fixed1.add(jj1);
        fixed2.add(jj1);
        fixed2.add(jj1);
        fixed3.add(jj2);

        Mockito.when(components[2].getFixedItems()).thenReturn(fixed1);
        Mockito.when(components[3].getFixedItems()).thenReturn(fixed2);
        Mockito.when(components[5].getFixedItems()).thenReturn(fixed3);

        assertSame(4, makeDefaultCUT().getFixedJumpJets());
    }

    /**
     * {@link ChassisOmniMech#getFixedMass()} shall return the mass of the chassis with all non fixed items and armor
     * removed.
     */
    @Test
    public final void testGetFixedMass() {
        ChassisOmniMech cut = makeDefaultCUT();

        List<Item> fixed1 = new ArrayList<>();
        List<Item> fixed2 = new ArrayList<>();
        List<Item> fixed3 = new ArrayList<>();

        Item item1 = Mockito.mock(Item.class);
        Item item2 = Mockito.mock(Item.class);
        Item item3 = Mockito.mock(Item.class);

        Mockito.when(item1.getMass()).thenReturn(1.0);
        Mockito.when(item2.getMass()).thenReturn(2.0);
        Mockito.when(item3.getMass()).thenReturn(3.0);

        fixed1.add(item1);
        fixed1.add(item2);
        fixed2.add(item2);
        fixed2.add(item2);
        fixed3.add(item3);

        Mockito.when(components[2].getFixedItems()).thenReturn(fixed1);
        Mockito.when(components[3].getFixedItems()).thenReturn(fixed2);
        Mockito.when(components[5].getFixedItems()).thenReturn(fixed3);

        Mockito.when(structureType.getStructureMass(cut)).thenReturn(3.0);

        double expected = 1 * 1 + 3 * 2 + 1 * 3 + 3;

        assertEquals(expected, cut.getFixedMass(), 0.0);

        Mockito.verify(armorType, Mockito.never()).getArmorMass(Matchers.anyInt());
    }

    @Test
    public final void testGetFixedStructureType() {
        assertSame(structureType, makeDefaultCUT().getFixedStructureType());
    }

    @Test
    public final void testGetMovementProfiles() {
        ChassisOmniMech mech = (ChassisOmniMech) ChassisDB.lookup("kfx-prime");

        MovementProfile baseProfile = mech.getMovementProfileBase();

        MovementProfile max = mech.getMovementProfileMax();
        MovementProfile min = mech.getMovementProfileMin();
        Collection<Modifier> stock = mech.getStockModifiers();

        assertEquals(baseProfile.getTorsoYawSpeed(null) * 1.05, baseProfile.getTorsoYawSpeed(stock), 0.0);
        assertEquals(baseProfile.getTorsoYawMax(null) + 5, baseProfile.getTorsoYawMax(stock), 0.0);

        assertEquals(baseProfile.getTorsoYawSpeed(null) * 0.95, min.getTorsoYawSpeed(null), 0.0);
        assertEquals(baseProfile.getTorsoYawSpeed(null) * 1.10, max.getTorsoYawSpeed(null), 0.0);
    }

    @Test
    public final void testIsAllowed_CASE() {
        Item item = ItemDB.CASE;

        ChassisOmniMech cut = makeDefaultCUT();
        assertFalse(cut.isAllowed(item));
    }

    @Test
    public final void testIsAllowed_Engine() {
        assertFalse(makeDefaultCUT().isAllowed(engine));
    }

    @Test
    public final void testIsAllowed_NoComponentSupport() {
        Item item = Mockito.mock(Item.class);
        Mockito.when(item.getHardpointType()).thenReturn(HardPointType.NONE);
        Mockito.when(item.getFaction()).thenReturn(Faction.Clan);
        Mockito.when(item.isCompatible(Matchers.any(Upgrades.class))).thenReturn(true);

        ChassisOmniMech cut = makeDefaultCUT();
        assertTrue(cut.isAllowed(item)); // Item in it self is allowed

        // But no component supports it.
        for (Location location : Location.values()) {
            Mockito.when(components[location.ordinal()].isAllowed(item, null)).thenReturn(false);
        }
        assertFalse(cut.isAllowed(item));
    }

    @Override
    protected ChassisOmniMech makeDefaultCUT() {
        return new ChassisOmniMech(mwoID, mwoName, series, name, shortName, maxTons, variant, baseVariant,
                movementProfile, faction, components, maxPilotModules, maxConsumableModules, maxWeaponModules,
                structureType, armorType, heatSinkType);
    }
}
