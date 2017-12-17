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
package org.lisoft.lsml.model.loadout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.lisoft.lsml.model.chassi.ArmourSide;
import org.lisoft.lsml.model.chassi.Component;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.util.ListArrayUtils;
import org.mockito.Mockito;

/**
 * Test suite for {@link ConfiguredComponent}.
 *
 * @author Emily Björk
 */
public abstract class ConfiguredComponentTest {
    protected int slots = 12;
    protected Location location = Location.LeftArm;
    protected Component internal = null;
    protected boolean manualArmour = false;
    protected int internalFixedSlots = 0;
    protected List<Item> internalFixedItems = new ArrayList<>();
    protected int maxArmour = 32;

    @Test
    public final void testAddRemoveCanRemoveItem() throws Exception {
        final ConfiguredComponent cut = makeDefaultCUT();
        assertFalse(cut.canRemoveItem(ItemDB.CASE));
        cut.addItem(ItemDB.CASE);
        assertTrue(cut.canRemoveItem(ItemDB.CASE));
        cut.removeItem(ItemDB.CASE);
        assertFalse(cut.canRemoveItem(ItemDB.CASE));
    }

    @Test
    public final void testAddRemoveCanRemoveItem_Internals() throws Exception {
        final ConfiguredComponent cut = makeDefaultCUT();
        Internal item = mock(Internal.class);
        assertFalse(cut.canRemoveItem(item));
        cut.addItem(item);
        assertFalse(cut.canRemoveItem(item));
    }

    @Test
    public final void testAddRemoveItems() {
        final ConfiguredComponent cut = makeDefaultCUT();

        assertEquals(0, cut.addItem(ItemDB.BAP));
        assertEquals(1, cut.addItem(ItemDB.CASE));
        assertEquals(2, cut.addItem(ItemDB.AMS));

        assertEquals(1, cut.removeItem(ItemDB.CASE));
        assertEquals(0, cut.removeItem(ItemDB.BAP));
        assertEquals(1, cut.addItem(ItemDB.AMS));
        assertEquals(1, cut.removeItem(ItemDB.AMS));
        assertEquals(0, cut.removeItem(ItemDB.AMS));
    }

    /**
     * Add/Remove item for heat sinks in the engine should return special -1.
     */
    @Test
    public final void testAddRemoveItems_EngineHS() {
        slots = 8;
        final ConfiguredComponent cut = makeDefaultCUT();

        final Engine engine = mock(Engine.class);
        final int hsSlots = 4;
        final int freeSlots = 2;
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

    /**
     * Engine internals shall be counted as a normal item for now.
     *
     * TODO: Change addItem() so that internals are added at the top always.
     */
    @Test
    public final void testAddRemoveItems_EngineInternals() {
        final ConfiguredComponent cut = makeDefaultCUT();

        Internal side = mock(Internal.class);
        
        assertEquals(0, cut.addItem(ItemDB.AMS));
        assertEquals(1, cut.addItem(ItemDB.AMS));
        assertEquals(2, cut.addItem(ItemDB.AMS));
        assertEquals(3, cut.addItem(side));
        assertEquals(4, cut.addItem(side));

        assertEquals(4, cut.removeItem(side));
        assertEquals(3, cut.removeItem(side));
    }

    /**
     * No item is equippable if the internal component can't support it.
     */
    @Test
    public final void testCanEquip_NoInternalSupport() {
        final Item item = Mockito.mock(Item.class);
        Mockito.when(item.getHardpointType()).thenReturn(HardPointType.NONE);
        Mockito.when(item.getSlots()).thenReturn(1);

        Mockito.when(internal.isAllowed(item)).thenReturn(false);
        when(internal.isAllowed(item, null)).thenReturn(false);
        assertEquals(EquipResult.make(location, EquipResultType.NotSupported), makeDefaultCUT().canEquip(item));
    }

    // TODO: //Write tests for remove item with xl sides and remove HS with engine HS present.

    /**
     * Item's are not equippable if there is no space for them
     */
    @Test
    public final void testCanEquip_NoSpace() {
        // Fixed items setup
        internalFixedSlots = 2;
        final Item fixed1 = Mockito.mock(Item.class);
        Mockito.when(fixed1.getSlots()).thenReturn(internalFixedSlots);
        internalFixedItems.add(fixed1);

        // Setup existing items in the component
        final Item item1 = Mockito.mock(Item.class);
        Mockito.when(item1.getHardpointType()).thenReturn(HardPointType.NONE);
        Mockito.when(item1.getSlots()).thenReturn(slots / 4);

        final int freeSlots = 2;
        final Item item2 = Mockito.mock(Item.class);
        Mockito.when(item2.getHardpointType()).thenReturn(HardPointType.NONE);
        Mockito.when(item2.getSlots()).thenReturn(slots - slots / 4 - freeSlots - internalFixedSlots);

        final ConfiguredComponent cut = makeDefaultCUT();
        cut.addItem(item1);
        cut.addItem(item2);

        // Item to add
        final Item item = Mockito.mock(Item.class);
        Mockito.when(item.getHardpointType()).thenReturn(HardPointType.NONE);

        // Test tight fit.
        Mockito.when(item.getSlots()).thenReturn(freeSlots);
        assertEquals(EquipResult.SUCCESS, cut.canEquip(item));

        // Test too big
        Mockito.when(item.getSlots()).thenReturn(freeSlots + 1);
        assertEquals(EquipResult.make(location, EquipResultType.NotEnoughSlots), cut.canEquip(item));
    }

    /**
     * Simple items without any requirements are equippable.
     */
    @Test
    public final void testCanEquip_Simple() {
        final Item item = Mockito.mock(Item.class);
        Mockito.when(item.getHardpointType()).thenReturn(HardPointType.NONE);
        Mockito.when(item.getSlots()).thenReturn(1);

        assertSame(EquipResult.SUCCESS, makeDefaultCUT().canEquip(item));
    }

    @Test
    public final void testGetArmourMax_DoubleSided() throws Exception {
        location = Location.CenterTorso;
        maxArmour = 2 * 2 * 2 * 2 * 2 * 2;
        final ConfiguredComponent cut = makeDefaultCUT();
        cut.setArmour(ArmourSide.FRONT, maxArmour / 8, manualArmour);
        cut.setArmour(ArmourSide.BACK, maxArmour / 4, manualArmour);

        assertEquals(maxArmour - maxArmour / 4, cut.getArmourMax(ArmourSide.FRONT));
        assertEquals(maxArmour - maxArmour / 8, cut.getArmourMax(ArmourSide.BACK));
    }

    @Test
    public final void testGetArmourMax_SingleSided() throws Exception {
        location = Location.LeftArm;
        final ConfiguredComponent cut = makeDefaultCUT();
        cut.setArmour(ArmourSide.ONLY, maxArmour / 2, manualArmour);
        assertEquals(maxArmour, cut.getArmourMax(ArmourSide.ONLY));
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testGetArmourMax_WrongSide() throws Exception {
        location = Location.CenterTorso;
        maxArmour = 2 * 2 * 10;
        makeDefaultCUT().getArmourMax(ArmourSide.ONLY);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testGetArmourMax_WrongSide2() throws Exception {
        location = Location.LeftArm;
        maxArmour = 2 * 2 * 10;
        makeDefaultCUT().getArmourMax(ArmourSide.FRONT);
    }

    @Test
    public final void testGetArmourTotal_DoubleSided() throws Exception {
        location = Location.CenterTorso;
        maxArmour = 4 * 10;
        final ConfiguredComponent cut = makeDefaultCUT();
        cut.setArmour(ArmourSide.FRONT, maxArmour / 4, manualArmour);
        cut.setArmour(ArmourSide.BACK, 2 * maxArmour / 4, manualArmour);
        assertEquals(maxArmour * 3 / 4, cut.getArmourTotal());
    }

    @Test
    public final void testGetArmourTotal_SingleSided() throws Exception {
        location = Location.LeftArm;
        final ConfiguredComponent cut = makeDefaultCUT();
        cut.setArmour(ArmourSide.ONLY, maxArmour / 2, manualArmour);
        assertEquals(maxArmour / 2, cut.getArmourTotal());
    }

    @Test
    public final void testGetEngineHeatSinks() throws Exception {
        final HeatSink fixed1 = Mockito.mock(HeatSink.class);
        internalFixedItems.add(fixed1);

        final Engine fixed2 = Mockito.mock(Engine.class);
        Mockito.when(fixed2.getNumHeatsinkSlots()).thenReturn(3);
        internalFixedItems.add(fixed2);

        final HeatSink item1 = Mockito.mock(HeatSink.class);
        final Item item2 = Mockito.mock(Item.class);

        final ConfiguredComponent cut = makeDefaultCUT();
        cut.addItem(item1);
        cut.addItem(item2);

        assertEquals(2, cut.getEngineHeatSinks());
    }

    @Test
    public final void testGetEngineHeatSinks_overflow() throws Exception {
        final HeatSink fixed1 = Mockito.mock(HeatSink.class);
        internalFixedItems.add(fixed1);

        final Engine fixed2 = Mockito.mock(Engine.class);
        Mockito.when(fixed2.getNumHeatsinkSlots()).thenReturn(2);
        internalFixedItems.add(fixed2);

        final HeatSink item1 = Mockito.mock(HeatSink.class);
        final Item item2 = Mockito.mock(Item.class);

        final ConfiguredComponent cut = makeDefaultCUT();
        cut.addItem(item1);
        cut.addItem(item1);
        cut.addItem(item1);
        cut.addItem(item2);

        assertEquals(2, cut.getEngineHeatSinks());
    }

    @Test
    public final void testGetEngineHeatSinksMax_fixedEngine() throws Exception {
        final Engine fixed1 = Mockito.mock(Engine.class);
        Mockito.when(fixed1.getNumHeatsinkSlots()).thenReturn(3);
        internalFixedItems.add(fixed1);

        final Item fixed2 = Mockito.mock(Item.class);
        internalFixedItems.add(fixed2);

        assertEquals(3, makeDefaultCUT().getEngineHeatSinksMax());
    }

    @Test
    public final void testGetEngineHeatSinksMax_userEngine() throws Exception {
        final Item item1 = Mockito.mock(Item.class);
        final Engine item2 = Mockito.mock(Engine.class);
        Mockito.when(item2.getNumHeatsinkSlots()).thenReturn(2);

        final ConfiguredComponent cut = makeDefaultCUT();
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
        final Item fixed1 = Mockito.mock(Item.class);
        Mockito.when(fixed1.getMass()).thenReturn(2.0);
        internalFixedItems.add(fixed1);

        final Item fixed2 = Mockito.mock(Item.class);
        Mockito.when(fixed2.getMass()).thenReturn(3.0);
        internalFixedItems.add(fixed2);

        final Item item1 = Mockito.mock(Item.class);
        Mockito.when(item1.getMass()).thenReturn(5.0);
        final Item item2 = Mockito.mock(Item.class);
        Mockito.when(item2.getMass()).thenReturn(7.0);

        final ConfiguredComponent cut = makeDefaultCUT();
        cut.addItem(item1);
        cut.addItem(item2);

        assertEquals(17.0, cut.getItemMass(), 0.0);
    }

    @Test
    public final void testGetItemsEquipped() throws Exception {
        final Item fixed1 = Mockito.mock(Item.class);
        Mockito.when(fixed1.getMass()).thenReturn(2.0);
        internalFixedItems.add(fixed1);

        final Item fixed2 = Mockito.mock(Item.class);
        Mockito.when(fixed2.getMass()).thenReturn(3.0);
        internalFixedItems.add(fixed2);

        final Item item1 = Mockito.mock(Item.class);
        Mockito.when(item1.getMass()).thenReturn(5.0);
        final Item item2 = Mockito.mock(Item.class);
        Mockito.when(item2.getMass()).thenReturn(7.0);

        final ConfiguredComponent cut = makeDefaultCUT();
        cut.addItem(item1);
        cut.addItem(item2);

        final List<Item> ans = new ArrayList<>();
        ans.add(item1);
        ans.add(item2);

        assertTrue(ListArrayUtils.equalsUnordered(ans, cut.getItemsEquipped()));
    }

    @Test
    public final void testGetItemsFixed() throws Exception {
        final Item fixed1 = Mockito.mock(Item.class);
        Mockito.when(fixed1.getMass()).thenReturn(2.0);
        internalFixedItems.add(fixed1);

        final Item fixed2 = Mockito.mock(Item.class);
        Mockito.when(fixed2.getMass()).thenReturn(3.0);
        internalFixedItems.add(fixed2);

        final Item item1 = Mockito.mock(Item.class);
        Mockito.when(item1.getMass()).thenReturn(5.0);
        final Item item2 = Mockito.mock(Item.class);
        Mockito.when(item2.getMass()).thenReturn(7.0);

        final ConfiguredComponent cut = makeDefaultCUT();
        cut.addItem(item1);
        cut.addItem(item2);

        final List<Item> ans = new ArrayList<>();
        ans.add(fixed1);
        ans.add(fixed2);

        assertTrue(ListArrayUtils.equalsUnordered(ans, new ArrayList<>(cut.getItemsFixed())));
    }

    @Test
    public final void testGetItemsOfHardpointType() throws Exception {
        final Item fixed1 = Mockito.mock(Item.class);
        Mockito.when(fixed1.getHardpointType()).thenReturn(HardPointType.ENERGY);
        internalFixedItems.add(fixed1);

        final Item fixed2 = Mockito.mock(Item.class);
        Mockito.when(fixed2.getHardpointType()).thenReturn(HardPointType.BALLISTIC);
        internalFixedItems.add(fixed2);

        final Item item1 = Mockito.mock(Item.class);
        Mockito.when(item1.getHardpointType()).thenReturn(HardPointType.ENERGY);
        final Item item2 = Mockito.mock(Item.class);
        Mockito.when(item2.getHardpointType()).thenReturn(HardPointType.ENERGY);

        final ConfiguredComponent cut = makeDefaultCUT();
        cut.addItem(item1);
        cut.addItem(item2);

        assertEquals(3, cut.getItemsOfHardpointType(HardPointType.ENERGY));
    }

    @Test
    public final void testGetSlotsFreeUsed() throws Exception {
        slots = 20;
        internalFixedSlots = 5;

        final Item fixed1 = Mockito.mock(Item.class);
        Mockito.when(fixed1.getSlots()).thenReturn(2);
        internalFixedItems.add(fixed1);

        final Item fixed2 = Mockito.mock(Item.class);
        Mockito.when(fixed2.getSlots()).thenReturn(3);
        internalFixedItems.add(fixed2);

        final Item item1 = Mockito.mock(Item.class);
        Mockito.when(item1.getSlots()).thenReturn(5);
        final Item item2 = Mockito.mock(Item.class);
        Mockito.when(item2.getSlots()).thenReturn(7);

        final ConfiguredComponent cut = makeDefaultCUT();
        cut.addItem(item1);
        cut.addItem(item2);

        assertEquals(17, cut.getSlotsUsed());
        assertEquals(slots - 17, cut.getSlotsFree());
    }

    @Test
    public void testGetSlotsFreeUsed_EngineHS() {
        final Engine engine = Mockito.mock(Engine.class);
        Mockito.when(engine.getSlots()).thenReturn(7);
        Mockito.when(engine.getNumHeatsinkSlots()).thenReturn(2);

        final HeatSink heatSink = Mockito.mock(HeatSink.class);
        Mockito.when(heatSink.getSlots()).thenReturn(3);

        final ConfiguredComponent cut = makeDefaultCUT();
        cut.addItem(engine);
        cut.addItem(heatSink);
        cut.addItem(heatSink);
        cut.addItem(heatSink);

        assertEquals(10, cut.getSlotsUsed());
        assertEquals(slots - 10, cut.getSlotsFree());
    }

    @Test
    public final void testHasManualArmour() throws Exception {
        assertEquals(manualArmour, makeDefaultCUT().hasManualArmour());
        manualArmour = !manualArmour;
        assertEquals(manualArmour, makeDefaultCUT().hasManualArmour());
    }

    @Test
    public final void testSetGetArmour_DoubleSided() throws Exception {
        location = Location.CenterTorso;

        maxArmour = 2 * 2 * 10;
        final ConfiguredComponent cut = makeDefaultCUT();
        assertEquals(0, cut.getArmour(ArmourSide.FRONT));
        assertEquals(0, cut.getArmour(ArmourSide.BACK));
        cut.setArmour(ArmourSide.FRONT, maxArmour / 2, manualArmour);
        cut.setArmour(ArmourSide.BACK, maxArmour / 4, manualArmour);

        assertEquals(manualArmour, cut.hasManualArmour());
        assertEquals(maxArmour / 2, cut.getArmour(ArmourSide.FRONT));
        assertEquals(maxArmour / 4, cut.getArmour(ArmourSide.BACK));
    }

    @Test
    public final void testSetGetArmour_SingleSided() throws Exception {
        location = Location.LeftArm;

        final ConfiguredComponent cut = makeDefaultCUT();
        assertEquals(0, cut.getArmour(ArmourSide.ONLY));
        cut.setArmour(ArmourSide.ONLY, maxArmour / 2, manualArmour);

        assertEquals(manualArmour, cut.hasManualArmour());
        assertEquals(maxArmour / 2, cut.getArmour(ArmourSide.ONLY));
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testSetGetArmour_WrongSide() throws Exception {
        location = Location.LeftArm;
        makeDefaultCUT().setArmour(ArmourSide.FRONT, maxArmour / 2, manualArmour);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testSetGetArmour_WrongSide2() throws Exception {
        location = Location.CenterTorso;
        makeDefaultCUT().setArmour(ArmourSide.ONLY, maxArmour / 2, manualArmour);
    }

    protected abstract ConfiguredComponent makeDefaultCUT();
}
