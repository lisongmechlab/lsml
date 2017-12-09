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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.model.chassi.ComponentOmniMech;
import org.lisoft.lsml.model.chassi.HardPoint;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.OmniPod;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.util.ListArrayUtils;

/**
 * Test suite for {@link ConfiguredComponentOmniMech}.
 *
 * @author Li Song
 */
@SuppressWarnings("javadoc")
public class ConfiguredComponentOmniMechTest extends ConfiguredComponentTest {

    protected ComponentOmniMech omniInternal;
    protected OmniPod omniPod;
    protected boolean missileBayDoors;
    protected List<HardPoint> hardPoints = new ArrayList<>();
    protected List<Item> togglables = new ArrayList<>();
    protected List<Item> omniPodFixed = new ArrayList<>();

    @Before
    public void setup() {
        omniInternal = mock(ComponentOmniMech.class);
        omniPod = mock(OmniPod.class);
        internal = omniInternal;
        when(internal.isAllowed(any(Item.class))).thenReturn(true);
        when(internal.isAllowed(any(Item.class), any())).thenReturn(true);
    }

    @Test
    public void testCanEquip_AllHardpointsTaken() {
        final Item item = mock(Item.class);
        when(item.getSlots()).thenReturn(1);
        when(item.getHardpointType()).thenReturn(HardPointType.ENERGY);

        when(omniPod.getHardPointCount(HardPointType.ENERGY)).thenReturn(1);
        hardPoints.add(new HardPoint(HardPointType.ENERGY));
        final ConfiguredComponentOmniMech cut = makeDefaultCUT();
        cut.addItem(item);

        assertEquals(EquipResult.make(location, EquipResultType.NoFreeHardPoints), cut.canEquip(item));
    }

    @Test
    public void testCanEquip_DynamicSlots() {
        when(omniInternal.getDynamicArmourSlots()).thenReturn(2);
        when(omniInternal.getDynamicStructureSlots()).thenReturn(3);

        final Item internalItem = mock(Internal.class);
        when(internalItem.getSlots()).thenReturn(5);
        internalFixedItems.add(internalItem);
        internalFixedSlots = 5;

        final int size = 2;
        slots = 2 + 3 + internalFixedSlots + size;

        final Item item = mock(Item.class);
        when(item.getHardpointType()).thenReturn(HardPointType.NONE);

        when(item.getSlots()).thenReturn(size);
        assertEquals(EquipResult.SUCCESS, makeDefaultCUT().canEquip(item));

        when(item.getSlots()).thenReturn(size + 1);
        assertEquals(EquipResult.make(location, EquipResultType.NotEnoughSlots), makeDefaultCUT().canEquip(item));
    }

    @Test
    public void testCanEquip_HasHardpoint() {
        final Item item = mock(Item.class);
        when(item.getSlots()).thenReturn(1);
        when(item.getHardpointType()).thenReturn(HardPointType.ENERGY);

        when(omniPod.getHardPointCount(HardPointType.ENERGY)).thenReturn(1);
        hardPoints.add(new HardPoint(HardPointType.ENERGY));

        assertEquals(EquipResult.SUCCESS, makeDefaultCUT().canEquip(item));
    }

    @Test
    public void testCanEquip_NoHardpoint() {
        final Item item = mock(Item.class);
        when(item.getSlots()).thenReturn(1);
        when(item.getHardpointType()).thenReturn(HardPointType.ENERGY);

        assertEquals(EquipResult.make(location, EquipResultType.NoFreeHardPoints), makeDefaultCUT().canEquip(item));
    }

    @Test
    public final void testChangeGetOmniPod() throws Exception {
        final ConfiguredComponentOmniMech cut = makeDefaultCUT();
        final OmniPod omniPod2 = mock(OmniPod.class);

        cut.changeOmniPod(omniPod2);
        assertSame(omniPod2, cut.getOmniPod());
    }

    @Test(expected = NullPointerException.class)
    public final void testChangeOmniPod_Null() throws Exception {
        makeDefaultCUT().changeOmniPod(null);
    }

    /**
     * The {@link ConfiguredComponentOmniMech#changeOmniPod(OmniPod)} shall throw {@link UnsupportedOperationException}
     * if the component has a fixed {@link OmniPod}.
     */
    @Test(expected = UnsupportedOperationException.class)
    public final void testChangeOmniPodFixed() throws Exception {
        setupDefaultMocks();
        when(omniInternal.hasFixedOmniPod()).thenReturn(true);
        when(omniInternal.getFixedOmniPod()).thenReturn(omniPod);
        final ConfiguredComponentOmniMech cut = new ConfiguredComponentOmniMech(omniInternal, manualArmour);
        final OmniPod omniPod2 = mock(OmniPod.class);

        cut.changeOmniPod(omniPod2);
    }

    @Test
    public final void testCopyCtor_ToggleStateNotLinked() {
        togglables.add(ItemDB.LAA);
        final ConfiguredComponentOmniMech cut = makeDefaultCUT();
        cut.setToggleState(ItemDB.LAA, true);

        final ConfiguredComponentOmniMech copy = new ConfiguredComponentOmniMech(cut);
        copy.setToggleState(ItemDB.LAA, false);
        assertNotEquals(cut, copy);
    }

    @Test
    public final void testCopyCtor_ToggleStateOff() {
        togglables.add(ItemDB.LAA);
        final ConfiguredComponentOmniMech cut = makeDefaultCUT();
        cut.setToggleState(ItemDB.LAA, false);

        final ConfiguredComponentOmniMech copy = new ConfiguredComponentOmniMech(cut);
        assertEquals(cut, copy);
    }

    @Test
    public final void testCopyCtor_ToggleStateOn() {
        togglables.add(ItemDB.LAA);
        final ConfiguredComponentOmniMech cut = makeDefaultCUT();
        cut.setToggleState(ItemDB.LAA, true);

        final ConfiguredComponentOmniMech copy = new ConfiguredComponentOmniMech(cut);
        assertEquals(cut, copy);
    }

    @Test(expected = NullPointerException.class)
    public final void testCtor_NullOmniPod() {
        omniPod = null;
        makeDefaultCUT();
    }

    @Test
    public final void testEquals_OmniPods() {
        togglables.add(ItemDB.LAA);
        final ConfiguredComponentOmniMech cut = makeDefaultCUT();
        final ConfiguredComponentOmniMech cut2 = makeDefaultCUT();
        final OmniPod pod1 = mock(OmniPod.class);
        final OmniPod pod2 = mock(OmniPod.class);
        cut.changeOmniPod(pod1);
        cut2.changeOmniPod(pod2);

        assertNotEquals(cut, cut2);
    }

    @Test
    public final void testEquals_Same() {
        togglables.add(ItemDB.LAA);
        final ConfiguredComponentOmniMech cut = makeDefaultCUT();
        final ConfiguredComponentOmniMech cut2 = makeDefaultCUT();

        assertEquals(cut, cut2);
        assertEquals(cut, cut);
        assertEquals(cut2, cut2);
    }

    @Test
    public final void testEquals_ToggleState() {
        togglables.add(ItemDB.LAA);
        final ConfiguredComponentOmniMech cut = makeDefaultCUT();
        final ConfiguredComponentOmniMech cut2 = makeDefaultCUT();
        cut.setToggleState(ItemDB.LAA, false);
        cut2.setToggleState(ItemDB.LAA, true);

        assertNotEquals(cut, cut2);
    }

    @Test
    public final void testGetHardPointCount() throws Exception {
        when(omniPod.getHardPointCount(HardPointType.MISSILE)).thenReturn(7);
        assertEquals(7, makeDefaultCUT().getHardPointCount(HardPointType.MISSILE));
    }

    @Test
    public final void testGetHardPoints() throws Exception {
        hardPoints.add(new HardPoint(HardPointType.ENERGY));
        hardPoints.add(new HardPoint(HardPointType.BALLISTIC));

        assertTrue(ListArrayUtils.equalsUnordered(hardPoints, new ArrayList<>(makeDefaultCUT().getHardPoints())));
    }

    @Test
    public final void testGetItemsFixed_OmniPod() throws Exception {
        final Item fixed1 = mock(Item.class);
        when(fixed1.getMass()).thenReturn(2.0);
        omniPodFixed.add(fixed1);

        final Item fixed2 = mock(Item.class);
        when(fixed2.getMass()).thenReturn(3.0);
        omniPodFixed.add(fixed2);

        final ConfiguredComponentOmniMech cut = makeDefaultCUT();

        final List<Item> ans = new ArrayList<>();
        ans.add(fixed1);
        ans.add(fixed2);

        assertTrue(ListArrayUtils.equalsUnordered(ans, new ArrayList<>(cut.getItemsFixed())));
    }

    @Test
    public final void testGetOmniPod() throws Exception {
        assertSame(omniPod, makeDefaultCUT().getOmniPod());
    }

    @Test
    public final void testGetOmniPodFixed() throws Exception {
        setupDefaultMocks();
        when(omniInternal.hasFixedOmniPod()).thenReturn(true);
        when(omniInternal.getFixedOmniPod()).thenReturn(omniPod);
        final ConfiguredComponentOmniMech cut = new ConfiguredComponentOmniMech(omniInternal, manualArmour);

        assertSame(omniPod, cut.getOmniPod());
    }

    // TODO: Test togglestate handling and fixeditems

    @Test
    public final void testGetSlotsUsedFree_DynamicSlots() {
        when(omniInternal.getDynamicArmourSlots()).thenReturn(2);
        when(omniInternal.getDynamicStructureSlots()).thenReturn(3);

        assertEquals(5, makeDefaultCUT().getSlotsUsed());
        assertEquals(slots - 5, makeDefaultCUT().getSlotsFree());
    }

    @Test
    public final void testHasMissileBayDoors() throws Exception {
        assertEquals(missileBayDoors, makeDefaultCUT().hasMissileBayDoors());
        missileBayDoors = !missileBayDoors;
        assertEquals(missileBayDoors, makeDefaultCUT().hasMissileBayDoors());
    }

    @Override
    protected ConfiguredComponentOmniMech makeDefaultCUT() {
        setupDefaultMocks();
        return new ConfiguredComponentOmniMech(omniInternal, manualArmour, omniPod);
    }

    protected void setupDefaultMocks() {
        when(internal.getLocation()).thenReturn(location);
        when(internal.getSlots()).thenReturn(slots);
        when(internal.getFixedItemSlots()).thenReturn(internalFixedSlots);
        when(internal.getFixedItems()).thenReturn(internalFixedItems);
        when(internal.getArmourMax()).thenReturn(maxArmour);
        if (null != omniPod) {
            when(omniPod.hasMissileBayDoors()).thenReturn(missileBayDoors);
            when(omniPod.getHardPoints()).thenReturn(hardPoints);
            when(omniPod.getToggleableItems()).thenReturn(togglables);
            when(omniPod.getFixedItems()).thenReturn(omniPodFixed);
        }
    }
}
