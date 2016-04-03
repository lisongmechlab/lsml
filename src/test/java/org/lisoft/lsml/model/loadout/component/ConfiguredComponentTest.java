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
package org.lisoft.lsml.model.loadout.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.chassi.Component;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.util.ListArrayUtils;
import org.mockito.Mockito;

/**
 * Test suite for {@link ConfiguredComponent}.
 * 
 * @author Emily Björk
 */
public abstract class ConfiguredComponentTest {
    protected int        slots              = 12;
    protected Location   location           = Location.LeftArm;
    protected Component  internal           = null;
    protected boolean    manualArmor        = false;
    protected int        internalFixedSlots = 0;
    protected List<Item> internalFixedItems = new ArrayList<>();
    protected int        maxArmor           = 32;

    protected abstract ConfiguredComponent makeDefaultCUT();

    /**
     * Simple items without any requirements are equippable.
     */
    @Test
    public final void testCanEquip_Simple() {
        Item item = Mockito.mock(Item.class);
        Mockito.when(item.getHardpointType()).thenReturn(HardPointType.NONE);
        Mockito.when(item.getSlots()).thenReturn(1);

        assertSame(EquipResult.SUCCESS, makeDefaultCUT().canEquip(item));
    }

    /**
     * No item is equippable if the internal component can't support it.
     */
    @Test
    public final void testCanEquip_NoInternalSupport() {
        Item item = Mockito.mock(Item.class);
        Mockito.when(item.getHardpointType()).thenReturn(HardPointType.NONE);
        Mockito.when(item.getSlots()).thenReturn(1);

        Mockito.when(internal.isAllowed(item)).thenReturn(false);
        assertEquals(EquipResult.make(location, EquipResultType.NotSupported), makeDefaultCUT().canEquip(item));
    }

    /**
     * Item's are not equippable if there is no space for them
     */
    @Test
    public final void testCanEquip_NoSpace() {
        // Fixed items setup
        internalFixedSlots = 2;
        Item fixed1 = Mockito.mock(Item.class);
        Mockito.when(fixed1.getSlots()).thenReturn(internalFixedSlots);
        internalFixedItems.add(fixed1);

        // Setup existing items in the component
        Item item1 = Mockito.mock(Item.class);
        Mockito.when(item1.getHardpointType()).thenReturn(HardPointType.NONE);
        Mockito.when(item1.getSlots()).thenReturn(slots / 4);

        int freeSlots = 2;
        Item item2 = Mockito.mock(Item.class);
        Mockito.when(item2.getHardpointType()).thenReturn(HardPointType.NONE);
        Mockito.when(item2.getSlots()).thenReturn(slots - slots / 4 - freeSlots - internalFixedSlots);

        ConfiguredComponent cut = makeDefaultCUT();
        cut.addItem(item1);
        cut.addItem(item2);

        // Item to add
        Item item = Mockito.mock(Item.class);
        Mockito.when(item.getHardpointType()).thenReturn(HardPointType.NONE);

        // Test tight fit.
        Mockito.when(item.getSlots()).thenReturn(freeSlots);
        assertEquals(EquipResult.SUCCESS, cut.canEquip(item));

        // Test too big
        Mockito.when(item.getSlots()).thenReturn(freeSlots + 1);
        assertEquals(EquipResult.make(location, EquipResultType.NotEnoughSlots), cut.canEquip(item));
    }

    @Test
    public final void testAddRemoveCanRemoveItem() throws Exception {
        ConfiguredComponent cut = makeDefaultCUT();
        assertFalse(cut.canRemoveItem(ItemDB.CASE));
        cut.addItem(ItemDB.CASE);
        assertTrue(cut.canRemoveItem(ItemDB.CASE));
        cut.removeItem(ItemDB.CASE);
        assertFalse(cut.canRemoveItem(ItemDB.CASE));
    }

    @Test
    public final void testAddRemoveCanRemoveItem_Internals() throws Exception {
        ConfiguredComponent cut = makeDefaultCUT();
        assertFalse(cut.canRemoveItem(ConfiguredComponent.ENGINE_INTERNAL));
        cut.addItem(ConfiguredComponent.ENGINE_INTERNAL);
        assertFalse(cut.canRemoveItem(ConfiguredComponent.ENGINE_INTERNAL));
    }

    // TODO: //Write tests for remove item with xl sides and remove HS with engine HS present.

    /**
     * Engine internals shall be counted as a normal item for now.
     * 
     * XXX: Consider special casing engine internals to be counted into the fixed items. Would solve some problems
     */
    @Test
    public final void testAddRemoveItems_EngineInternals() {
        ConfiguredComponent cut = makeDefaultCUT();

        assertEquals(0, cut.addItem(ItemDB.AMS));
        assertEquals(1, cut.addItem(ItemDB.AMS));
        assertEquals(2, cut.addItem(ItemDB.AMS));
        assertEquals(3, cut.addItem(ConfiguredComponent.ENGINE_INTERNAL));
        assertEquals(4, cut.addItem(ConfiguredComponent.ENGINE_INTERNAL_CLAN));

        assertEquals(4, cut.removeItem(ConfiguredComponent.ENGINE_INTERNAL_CLAN));
        assertEquals(3, cut.removeItem(ConfiguredComponent.ENGINE_INTERNAL));
    }

    /**
     * Add/Remove item for heat sinks in the engine should return special -1.
     */
    @Test
    public final void testAddRemoveItems_EngineHS() {
        slots = 8;
        ConfiguredComponent cut = makeDefaultCUT();

        Engine engine = mock(Engine.class);
        int hsSlots = 4;
        int freeSlots = 2;
        when(engine.getHardpointType()).thenReturn(HardPointType.NONE);
        when(engine.getSlots()).thenReturn(slots - freeSlots);
        when(engine.getNumHeatsinkSlots()).thenReturn(hsSlots);

        int i = 0;
        assertEquals(i++, cut.addItem(ItemDB.AMS));
        assertEquals(i++, cut.addItem(ItemDB.SHS));
        assertEquals(i++, cut.addItem(ItemDB.SHS));
        assertEquals(i++, cut.addItem(ItemDB.SHS));
        assertEquals(i++, cut.addItem(ItemDB.SHS));
        assertEquals(1, cut.addItem(engine)); // 4 SHS are removed and put into engine.
        assertEquals(2, cut.addItem(ItemDB.SHS)); // Doesn't fit in HS slots

        assertEquals(2, cut.removeItem(ItemDB.SHS)); // External first.
        assertEquals(0, cut.removeItem(ItemDB.AMS));
        assertEquals(-1, cut.removeItem(ItemDB.SHS));
        assertEquals(-1, cut.removeItem(ItemDB.SHS));
        assertEquals(-1, cut.addItem(ItemDB.SHS));
        assertEquals(-1, cut.addItem(ItemDB.SHS));
        assertEquals(1, cut.addItem(ItemDB.SHS));
    }

    @Test
    public final void testAddRemoveItems() {
        ConfiguredComponent cut = makeDefaultCUT();

        assertEquals(0, cut.addItem(ItemDB.BAP));
        assertEquals(1, cut.addItem(ItemDB.CASE));
        assertEquals(2, cut.addItem(ItemDB.AMS));

        assertEquals(1, cut.removeItem(ItemDB.CASE));
        assertEquals(0, cut.removeItem(ItemDB.BAP));
        assertEquals(1, cut.addItem(ItemDB.AMS));
        assertEquals(1, cut.removeItem(ItemDB.AMS));
        assertEquals(0, cut.removeItem(ItemDB.AMS));
    }

    @Test
    public final void testHasManualArmor() throws Exception {
        assertEquals(manualArmor, makeDefaultCUT().hasManualArmor());
        manualArmor = !manualArmor;
        assertEquals(manualArmor, makeDefaultCUT().hasManualArmor());
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testSetGetArmor_WrongSide() throws Exception {
        location = Location.LeftArm;
        makeDefaultCUT().setArmor(ArmorSide.FRONT, maxArmor / 2, manualArmor);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testSetGetArmor_WrongSide2() throws Exception {
        location = Location.CenterTorso;
        makeDefaultCUT().setArmor(ArmorSide.ONLY, maxArmor / 2, manualArmor);
    }

    @Test
    public final void testSetGetArmor_SingleSided() throws Exception {
        location = Location.LeftArm;

        ConfiguredComponent cut = makeDefaultCUT();
        assertEquals(0, cut.getArmor(ArmorSide.ONLY));
        cut.setArmor(ArmorSide.ONLY, maxArmor / 2, manualArmor);

        assertEquals(manualArmor, cut.hasManualArmor());
        assertEquals(maxArmor / 2, cut.getArmor(ArmorSide.ONLY));
    }

    @Test
    public final void testSetGetArmor_DoubleSided() throws Exception {
        location = Location.CenterTorso;

        maxArmor = 2 * 2 * 10;
        ConfiguredComponent cut = makeDefaultCUT();
        assertEquals(0, cut.getArmor(ArmorSide.FRONT));
        assertEquals(0, cut.getArmor(ArmorSide.BACK));
        cut.setArmor(ArmorSide.FRONT, maxArmor / 2, manualArmor);
        cut.setArmor(ArmorSide.BACK, maxArmor / 4, manualArmor);

        assertEquals(manualArmor, cut.hasManualArmor());
        assertEquals(maxArmor / 2, cut.getArmor(ArmorSide.FRONT));
        assertEquals(maxArmor / 4, cut.getArmor(ArmorSide.BACK));
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testGetArmorMax_WrongSide() throws Exception {
        location = Location.CenterTorso;
        maxArmor = 2 * 2 * 10;
        makeDefaultCUT().getArmorMax(ArmorSide.ONLY);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testGetArmorMax_WrongSide2() throws Exception {
        location = Location.LeftArm;
        maxArmor = 2 * 2 * 10;
        makeDefaultCUT().getArmorMax(ArmorSide.FRONT);
    }

    @Test
    public final void testGetArmorMax_SingleSided() throws Exception {
        location = Location.LeftArm;
        ConfiguredComponent cut = makeDefaultCUT();
        cut.setArmor(ArmorSide.ONLY, maxArmor / 2, manualArmor);
        assertEquals(maxArmor, cut.getArmorMax(ArmorSide.ONLY));
    }

    @Test
    public final void testGetArmorMax_DoubleSided() throws Exception {
        location = Location.CenterTorso;
        maxArmor = 2 * 2 * 2 * 2 * 2 * 2;
        ConfiguredComponent cut = makeDefaultCUT();
        cut.setArmor(ArmorSide.FRONT, maxArmor / 8, manualArmor);
        cut.setArmor(ArmorSide.BACK, maxArmor / 4, manualArmor);

        assertEquals(maxArmor - maxArmor / 4, cut.getArmorMax(ArmorSide.FRONT));
        assertEquals(maxArmor - maxArmor / 8, cut.getArmorMax(ArmorSide.BACK));
    }

    @Test
    public final void testGetArmorTotal_SingleSided() throws Exception {
        location = Location.LeftArm;
        ConfiguredComponent cut = makeDefaultCUT();
        cut.setArmor(ArmorSide.ONLY, maxArmor / 2, manualArmor);
        assertEquals(maxArmor / 2, cut.getArmorTotal());
    }

    @Test
    public final void testGetArmorTotal_DoubleSided() throws Exception {
        location = Location.CenterTorso;
        maxArmor = 4 * 10;
        ConfiguredComponent cut = makeDefaultCUT();
        cut.setArmor(ArmorSide.FRONT, maxArmor / 4, manualArmor);
        cut.setArmor(ArmorSide.BACK, 2 * maxArmor / 4, manualArmor);
        assertEquals(maxArmor * 3 / 4, cut.getArmorTotal());
    }

    @Test
    public final void testGetEngineHeatSinks() throws Exception {
        HeatSink fixed1 = Mockito.mock(HeatSink.class);
        internalFixedItems.add(fixed1);

        Engine fixed2 = Mockito.mock(Engine.class);
        Mockito.when(fixed2.getNumHeatsinkSlots()).thenReturn(3);
        internalFixedItems.add(fixed2);

        HeatSink item1 = Mockito.mock(HeatSink.class);
        Item item2 = Mockito.mock(Item.class);

        ConfiguredComponent cut = makeDefaultCUT();
        cut.addItem(item1);
        cut.addItem(item2);

        assertEquals(2, cut.getEngineHeatSinks());
    }

    @Test
    public final void testGetEngineHeatSinks_overflow() throws Exception {
        HeatSink fixed1 = Mockito.mock(HeatSink.class);
        internalFixedItems.add(fixed1);

        Engine fixed2 = Mockito.mock(Engine.class);
        Mockito.when(fixed2.getNumHeatsinkSlots()).thenReturn(2);
        internalFixedItems.add(fixed2);

        HeatSink item1 = Mockito.mock(HeatSink.class);
        Item item2 = Mockito.mock(Item.class);

        ConfiguredComponent cut = makeDefaultCUT();
        cut.addItem(item1);
        cut.addItem(item1);
        cut.addItem(item1);
        cut.addItem(item2);

        assertEquals(2, cut.getEngineHeatSinks());
    }

    @Test
    public final void testGetEngineHeatSinksMax_fixedEngine() throws Exception {
        Engine fixed1 = Mockito.mock(Engine.class);
        Mockito.when(fixed1.getNumHeatsinkSlots()).thenReturn(3);
        internalFixedItems.add(fixed1);

        Item fixed2 = Mockito.mock(Item.class);
        internalFixedItems.add(fixed2);

        assertEquals(3, makeDefaultCUT().getEngineHeatSinksMax());
    }

    @Test
    public final void testGetEngineHeatSinksMax_userEngine() throws Exception {
        Item item1 = Mockito.mock(Item.class);
        Engine item2 = Mockito.mock(Engine.class);
        Mockito.when(item2.getNumHeatsinkSlots()).thenReturn(2);

        ConfiguredComponent cut = makeDefaultCUT();
        cut.addItem(item1);
        cut.addItem(item2);

        assertEquals(2, cut.getEngineHeatSinksMax());
    }

    @Test
    public final void testGetInternalComponent() throws Exception {
        assertSame(internal, makeDefaultCUT().getInternalComponent());
    }

    @Test
    public final void testGetItemMass() throws Exception {
        Item fixed1 = Mockito.mock(Item.class);
        Mockito.when(fixed1.getMass()).thenReturn(2.0);
        internalFixedItems.add(fixed1);

        Item fixed2 = Mockito.mock(Item.class);
        Mockito.when(fixed2.getMass()).thenReturn(3.0);
        internalFixedItems.add(fixed2);

        Item item1 = Mockito.mock(Item.class);
        Mockito.when(item1.getMass()).thenReturn(5.0);
        Item item2 = Mockito.mock(Item.class);
        Mockito.when(item2.getMass()).thenReturn(7.0);

        ConfiguredComponent cut = makeDefaultCUT();
        cut.addItem(item1);
        cut.addItem(item2);

        assertEquals(17.0, cut.getItemMass(), 0.0);
    }

    @Test
    public final void testGetItemsEquipped() throws Exception {
        Item fixed1 = Mockito.mock(Item.class);
        Mockito.when(fixed1.getMass()).thenReturn(2.0);
        internalFixedItems.add(fixed1);

        Item fixed2 = Mockito.mock(Item.class);
        Mockito.when(fixed2.getMass()).thenReturn(3.0);
        internalFixedItems.add(fixed2);

        Item item1 = Mockito.mock(Item.class);
        Mockito.when(item1.getMass()).thenReturn(5.0);
        Item item2 = Mockito.mock(Item.class);
        Mockito.when(item2.getMass()).thenReturn(7.0);

        ConfiguredComponent cut = makeDefaultCUT();
        cut.addItem(item1);
        cut.addItem(item2);

        List<Item> ans = new ArrayList<>();
        ans.add(item1);
        ans.add(item2);

        assertTrue(ListArrayUtils.equalsUnordered(ans, cut.getItemsEquipped()));
    }

    @Test
    public final void testGetItemsFixed() throws Exception {
        Item fixed1 = Mockito.mock(Item.class);
        Mockito.when(fixed1.getMass()).thenReturn(2.0);
        internalFixedItems.add(fixed1);

        Item fixed2 = Mockito.mock(Item.class);
        Mockito.when(fixed2.getMass()).thenReturn(3.0);
        internalFixedItems.add(fixed2);

        Item item1 = Mockito.mock(Item.class);
        Mockito.when(item1.getMass()).thenReturn(5.0);
        Item item2 = Mockito.mock(Item.class);
        Mockito.when(item2.getMass()).thenReturn(7.0);

        ConfiguredComponent cut = makeDefaultCUT();
        cut.addItem(item1);
        cut.addItem(item2);

        List<Item> ans = new ArrayList<>();
        ans.add(fixed1);
        ans.add(fixed2);

        assertTrue(ListArrayUtils.equalsUnordered(ans, new ArrayList<>(cut.getItemsFixed())));
    }

    @Test
    public final void testGetItemsOfHardpointType() throws Exception {
        Item fixed1 = Mockito.mock(Item.class);
        Mockito.when(fixed1.getHardpointType()).thenReturn(HardPointType.ENERGY);
        internalFixedItems.add(fixed1);

        Item fixed2 = Mockito.mock(Item.class);
        Mockito.when(fixed2.getHardpointType()).thenReturn(HardPointType.BALLISTIC);
        internalFixedItems.add(fixed2);

        Item item1 = Mockito.mock(Item.class);
        Mockito.when(item1.getHardpointType()).thenReturn(HardPointType.ENERGY);
        Item item2 = Mockito.mock(Item.class);
        Mockito.when(item2.getHardpointType()).thenReturn(HardPointType.ENERGY);

        ConfiguredComponent cut = makeDefaultCUT();
        cut.addItem(item1);
        cut.addItem(item2);

        assertEquals(3, cut.getItemsOfHardpointType(HardPointType.ENERGY));
    }

    @Test
    public final void testGetSlotsFreeUsed() throws Exception {
        slots = 20;
        internalFixedSlots = 5;

        Item fixed1 = Mockito.mock(Item.class);
        Mockito.when(fixed1.getSlots()).thenReturn(2);
        internalFixedItems.add(fixed1);

        Item fixed2 = Mockito.mock(Item.class);
        Mockito.when(fixed2.getSlots()).thenReturn(3);
        internalFixedItems.add(fixed2);

        Item item1 = Mockito.mock(Item.class);
        Mockito.when(item1.getSlots()).thenReturn(5);
        Item item2 = Mockito.mock(Item.class);
        Mockito.when(item2.getSlots()).thenReturn(7);

        ConfiguredComponent cut = makeDefaultCUT();
        cut.addItem(item1);
        cut.addItem(item2);

        assertEquals(17, cut.getSlotsUsed());
        assertEquals(slots - 17, cut.getSlotsFree());
    }

    @Test
    public void testGetSlotsFreeUsed_EngineHS() {
        Engine engine = Mockito.mock(Engine.class);
        Mockito.when(engine.getSlots()).thenReturn(7);
        Mockito.when(engine.getNumHeatsinkSlots()).thenReturn(2);

        HeatSink heatSink = Mockito.mock(HeatSink.class);
        Mockito.when(heatSink.getSlots()).thenReturn(3);

        ConfiguredComponent cut = makeDefaultCUT();
        cut.addItem(engine);
        cut.addItem(heatSink);
        cut.addItem(heatSink);
        cut.addItem(heatSink);

        assertEquals(10, cut.getSlotsUsed());
        assertEquals(slots - 10, cut.getSlotsFree());
    }
}
