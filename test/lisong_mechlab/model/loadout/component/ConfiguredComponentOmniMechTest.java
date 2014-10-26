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
package lisong_mechlab.model.loadout.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.chassi.ComponentOmniMech;
import lisong_mechlab.model.chassi.HardPoint;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.chassi.OmniPod;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.EquipResult;
import lisong_mechlab.model.loadout.EquipResult.Type;
import lisong_mechlab.util.ListArrayUtils;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * Test suite for {@link ConfiguredComponentOmniMech}.
 * 
 * @author Emily Björk
 */
public class ConfiguredComponentOmniMechTest extends ConfiguredComponentBaseTest {

    protected ComponentOmniMech omniInternal;
    protected OmniPod           omniPod;
    protected boolean           missileBayDoors;
    protected List<HardPoint>   hardPoints   = new ArrayList<>();
    protected List<Item>        togglables   = new ArrayList<>();
    protected List<Item>        omniPodFixed = new ArrayList<>();

    @Override
    protected ConfiguredComponentOmniMech makeDefaultCUT() {
        Mockito.when(internal.getLocation()).thenReturn(location);
        Mockito.when(internal.getSlots()).thenReturn(slots);
        Mockito.when(internal.getFixedItemSlots()).thenReturn(internalFixedSlots);
        Mockito.when(internal.getFixedItems()).thenReturn(internalFixedItems);
        Mockito.when(internal.getArmorMax()).thenReturn(maxArmor);
        if (null != omniPod) {
            Mockito.when(omniPod.hasMissileBayDoors()).thenReturn(missileBayDoors);
            Mockito.when(omniPod.getHardPoints()).thenReturn(hardPoints);
            Mockito.when(omniPod.getToggleableItems()).thenReturn(togglables);
            Mockito.when(omniPod.getFixedItems()).thenReturn(omniPodFixed);
        }
        return new ConfiguredComponentOmniMech(omniInternal, autoArmor, omniPod);
    }

    @Before
    public void setup() {
        omniInternal = Mockito.mock(ComponentOmniMech.class);
        omniPod = Mockito.mock(OmniPod.class);
        internal = omniInternal;
        Mockito.when(internal.isAllowed(Matchers.any(Item.class))).thenReturn(true);
    }

    @Test
        public void testCanEquip_AllHardpointsTaken() {
            Item item = Mockito.mock(Item.class);
            Mockito.when(item.getNumCriticalSlots()).thenReturn(1);
            Mockito.when(item.getHardpointType()).thenReturn(HardPointType.ENERGY);
    
            Mockito.when(omniPod.getHardPointCount(HardPointType.ENERGY)).thenReturn(1);
            hardPoints.add(new HardPoint(HardPointType.ENERGY));
            ConfiguredComponentOmniMech cut = makeDefaultCUT();
            cut.addItem(item);
    
            assertEquals(EquipResult.make(location, Type.NoFreeHardPoints), cut.canEquip(item));
        }

    @Test
        public void testCanEquip_DynamicSlots() {
            Mockito.when(omniInternal.getDynamicArmorSlots()).thenReturn(2);
            Mockito.when(omniInternal.getDynamicStructureSlots()).thenReturn(3);
    
            Item internalItem = Mockito.mock(Internal.class);
            Mockito.when(internalItem.getNumCriticalSlots()).thenReturn(5);
            internalFixedItems.add(internalItem);
            internalFixedSlots = 5;
    
            int size = 2;
            slots = 2 + 3 + internalFixedSlots + size;
    
            Item item = Mockito.mock(Item.class);
            Mockito.when(item.getHardpointType()).thenReturn(HardPointType.NONE);
    
            Mockito.when(item.getNumCriticalSlots()).thenReturn(size);
            assertEquals(EquipResult.SUCCESS, makeDefaultCUT().canEquip(item));
    
            Mockito.when(item.getNumCriticalSlots()).thenReturn(size + 1);
            assertEquals(EquipResult.make(location, Type.NotEnoughSlots), makeDefaultCUT().canEquip(item));
        }

    @Test
        public void testCanEquip_HasHardpoint() {
            Item item = Mockito.mock(Item.class);
            Mockito.when(item.getNumCriticalSlots()).thenReturn(1);
            Mockito.when(item.getHardpointType()).thenReturn(HardPointType.ENERGY);
    
            Mockito.when(omniPod.getHardPointCount(HardPointType.ENERGY)).thenReturn(1);
            hardPoints.add(new HardPoint(HardPointType.ENERGY));
    
            assertEquals(EquipResult.SUCCESS, makeDefaultCUT().canEquip(item));
        }

    @Test
        public void testCanEquip_NoHardpoint() {
            Item item = Mockito.mock(Item.class);
            Mockito.when(item.getNumCriticalSlots()).thenReturn(1);
            Mockito.when(item.getHardpointType()).thenReturn(HardPointType.ENERGY);
    
            assertEquals(EquipResult.make(location, Type.NoFreeHardPoints), makeDefaultCUT().canEquip(item));
        }

    @Test
    public final void testCopyCtor_ToggleStateNotLinked() {
        togglables.add(ItemDB.LAA);
        ConfiguredComponentOmniMech cut = makeDefaultCUT();
        cut.setToggleState(ItemDB.LAA, true);

        ConfiguredComponentOmniMech copy = new ConfiguredComponentOmniMech(cut);
        copy.setToggleState(ItemDB.LAA, false);
        assertNotEquals(cut, copy);
    }

    @Test
    public final void testCopyCtor_ToggleStateOff() {
        togglables.add(ItemDB.LAA);
        ConfiguredComponentOmniMech cut = makeDefaultCUT();
        cut.setToggleState(ItemDB.LAA, false);

        ConfiguredComponentOmniMech copy = new ConfiguredComponentOmniMech(cut);
        assertEquals(cut, copy);
    }

    @Test
    public final void testCopyCtor_ToggleStateOn() {
        togglables.add(ItemDB.LAA);
        ConfiguredComponentOmniMech cut = makeDefaultCUT();
        cut.setToggleState(ItemDB.LAA, true);

        ConfiguredComponentOmniMech copy = new ConfiguredComponentOmniMech(cut);
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
        ConfiguredComponentOmniMech cut = makeDefaultCUT();
        ConfiguredComponentOmniMech cut2 = makeDefaultCUT();
        OmniPod pod1 = Mockito.mock(OmniPod.class);
        OmniPod pod2 = Mockito.mock(OmniPod.class);
        cut.setOmniPod(pod1);
        cut2.setOmniPod(pod2);

        assertNotEquals(cut, cut2);
    }

    @Test
    public final void testEquals_Same() {
        togglables.add(ItemDB.LAA);
        ConfiguredComponentOmniMech cut = makeDefaultCUT();
        ConfiguredComponentOmniMech cut2 = makeDefaultCUT();

        assertEquals(cut, cut2);
        assertEquals(cut, cut);
        assertEquals(cut2, cut2);
    }

    @Test
    public final void testEquals_ToggleState() {
        togglables.add(ItemDB.LAA);
        ConfiguredComponentOmniMech cut = makeDefaultCUT();
        ConfiguredComponentOmniMech cut2 = makeDefaultCUT();
        cut.setToggleState(ItemDB.LAA, false);
        cut2.setToggleState(ItemDB.LAA, true);

        assertNotEquals(cut, cut2);
    }

    @Test
    public final void testGetHardPointCount() throws Exception {
        Mockito.when(omniPod.getHardPointCount(HardPointType.MISSILE)).thenReturn(7);
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
        Item fixed1 = Mockito.mock(Item.class);
        Mockito.when(fixed1.getMass()).thenReturn(2.0);
        omniPodFixed.add(fixed1);

        Item fixed2 = Mockito.mock(Item.class);
        Mockito.when(fixed2.getMass()).thenReturn(3.0);
        omniPodFixed.add(fixed2);

        ConfiguredComponentOmniMech cut = makeDefaultCUT();

        List<Item> ans = new ArrayList<>();
        ans.add(fixed1);
        ans.add(fixed2);

        assertTrue(ListArrayUtils.equalsUnordered(ans, new ArrayList<>(cut.getItemsFixed())));
    }

    @Test
    public final void testGetOmniPod() throws Exception {
        assertSame(omniPod, makeDefaultCUT().getOmniPod());
    }

    @Test
    public final void testGetSlotsUsedFree_DynamicSlots() {
        Mockito.when(omniInternal.getDynamicArmorSlots()).thenReturn(2);
        Mockito.when(omniInternal.getDynamicStructureSlots()).thenReturn(3);

        assertEquals(5, makeDefaultCUT().getSlotsUsed());
        assertEquals(slots - 5, makeDefaultCUT().getSlotsFree());
    }

    // TODO: Test togglestate handling and fixeditems

    @Test
    public final void testHasMissileBayDoors() throws Exception {
        assertEquals(missileBayDoors, makeDefaultCUT().hasMissileBayDoors());
        missileBayDoors = !missileBayDoors;
        assertEquals(missileBayDoors, makeDefaultCUT().hasMissileBayDoors());
    }

    @Test
    public final void testSetGetOmniPod() throws Exception {
        ConfiguredComponentOmniMech cut = makeDefaultCUT();
        OmniPod omniPod2 = Mockito.mock(OmniPod.class);

        cut.setOmniPod(omniPod2);
        assertSame(omniPod2, cut.getOmniPod());
    }

    @Test(expected = NullPointerException.class)
    public final void testSetOmniPod_Null() throws Exception {
        makeDefaultCUT().setOmniPod(null);
    }
}
