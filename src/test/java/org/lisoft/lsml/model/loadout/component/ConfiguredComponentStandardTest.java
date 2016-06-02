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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.model.chassi.ComponentStandard;
import org.lisoft.lsml.model.chassi.HardPoint;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.util.ListArrayUtils;
import org.mockito.Matchers;

/**
 * Test suite for {@link ConfiguredComponent}.
 *
 * @author Emily Björk
 */
public class ConfiguredComponentStandardTest extends ConfiguredComponentTest {
    protected boolean baydoors = false;
    protected ComponentStandard stdInternal;
    protected List<HardPoint> hardPoints = new ArrayList<>();

    @Before
    public void setup() {
        stdInternal = mock(ComponentStandard.class);
        internal = stdInternal;
        when(internal.isAllowed(Matchers.any(Item.class))).thenReturn(true);
    }

    @Test
    public void testCanEquip_AllHardpointsTaken() {
        final Item item = mock(Item.class);
        when(item.getSlots()).thenReturn(1);
        when(item.getHardpointType()).thenReturn(HardPointType.ENERGY);

        when(stdInternal.getHardPointCount(HardPointType.ENERGY)).thenReturn(1);
        hardPoints.add(new HardPoint(HardPointType.ENERGY));
        final ConfiguredComponentStandard cut = makeDefaultCUT();
        cut.addItem(item);

        assertEquals(EquipResult.make(location, EquipResultType.NoFreeHardPoints), cut.canEquip(item));
    }

    /**
     * C.A.S.E. is allowed (provided internal component allows it).
     */
    @Test
    public final void testCanEquip_CASEAllowed() {
        assertEquals(EquipResult.SUCCESS, makeDefaultCUT().canEquip(ItemDB.CASE));
    }

    @Test
    public void testCanEquip_EngineHS() {
        final Engine engine = mock(Engine.class);
        when(engine.getSlots()).thenReturn(slots);
        when(engine.getNumHeatsinkSlots()).thenReturn(2);
        when(engine.getHardpointType()).thenReturn(HardPointType.NONE);

        final HeatSink heatSink = mock(HeatSink.class);
        when(heatSink.getSlots()).thenReturn(3);
        when(heatSink.getHardpointType()).thenReturn(HardPointType.NONE);

        final ConfiguredComponentStandard cut = makeDefaultCUT();
        cut.addItem(engine);

        assertEquals(EquipResult.SUCCESS, cut.canEquip(heatSink));
        cut.addItem(heatSink);
        assertEquals(EquipResult.SUCCESS, cut.canEquip(heatSink));
        cut.addItem(heatSink);
        assertEquals(EquipResult.make(location, EquipResultType.NotEnoughSlots), cut.canEquip(heatSink));
    }

    /**
     * An engine can be equipped in CT if the existing heat sinks would fit in the internal heat sink slots, test for
     * DHS as well.
     */
    @Test
    public final void testCanEquip_EngineWithLotsOfDHS() {
        final int hsSlots = 4;
        slots = hsSlots * ItemDB.DHS.getSlots();

        final Engine item = mock(Engine.class);
        when(item.getHardpointType()).thenReturn(HardPointType.NONE);
        when(item.getSlots()).thenReturn(slots);
        when(item.getNumHeatsinkSlots()).thenReturn(hsSlots);
        when(internal.isAllowed(item)).thenReturn(true);
        final ConfiguredComponentStandard cut = makeDefaultCUT();

        for (int i = 0; i < hsSlots; ++i) {
            cut.addItem(ItemDB.DHS);
        }
        assertEquals(EquipResult.SUCCESS, cut.canEquip(item));
    }

    /**
     * An engine can be equipped in CT if the existing heat sinks would fit in the internal heat sink slots.
     */
    @Test
    public final void testCanEquip_EngineWithLotsOfHS() {
        slots = 8;

        final Engine item = mock(Engine.class);
        final int hsSlots = 4;
        final int freeSlots = 2;
        when(item.getHardpointType()).thenReturn(HardPointType.NONE);
        when(item.getSlots()).thenReturn(slots - freeSlots);
        when(item.getNumHeatsinkSlots()).thenReturn(hsSlots);
        when(internal.isAllowed(item)).thenReturn(true);
        final ConfiguredComponentStandard cut = makeDefaultCUT();

        for (int i = 0; i < hsSlots + freeSlots; ++i) {
            cut.addItem(ItemDB.SHS);
        }
        assertEquals(EquipResult.SUCCESS, cut.canEquip(item));
    }

    /**
     * The engine can't be equipped if non-hs junk makes it not possible to fit it.
     */
    @Test
    public final void testCanEquip_EngineWithOtherJunkAndHS() {
        slots = 8;

        final Engine item = mock(Engine.class);
        final int hsSlots = 4;
        final int freeSlots = 2;
        when(item.getHardpointType()).thenReturn(HardPointType.NONE);
        when(item.getSlots()).thenReturn(slots - freeSlots);
        when(item.getNumHeatsinkSlots()).thenReturn(hsSlots);
        when(internal.isAllowed(item)).thenReturn(true);
        final ConfiguredComponentStandard cut = makeDefaultCUT();

        final Item junk = mock(Item.class);
        when(junk.getHardpointType()).thenReturn(HardPointType.NONE);
        when(junk.getSlots()).thenReturn(freeSlots + 1);

        for (int i = 0; i < hsSlots + freeSlots; ++i) {
            cut.addItem(ItemDB.DHS);
        }
        cut.addItem(junk);

        // 8 total slots
        // 4*3 SHS + 1*3 junk = 7 used slots
        // Engine takes 6 and consumes 4 SHS, + 3 from junk = 9 slots, won't fit.

        assertEquals(EquipResult.make(location, EquipResultType.NotEnoughSlots), cut.canEquip(item));
    }

    /**
     * The engine can't be equipped if non-hs junk makes it not possible to fit it.
     */
    @Test
    public final void testCanEquip_EngineWithOtherJunkAndNoHS() {
        slots = 8;

        final Engine item = mock(Engine.class);
        final int hsSlots = 4;
        final int freeSlots = 2;
        when(item.getHardpointType()).thenReturn(HardPointType.NONE);
        when(item.getSlots()).thenReturn(slots - freeSlots);
        when(item.getNumHeatsinkSlots()).thenReturn(hsSlots);
        when(internal.isAllowed(item)).thenReturn(true);
        final ConfiguredComponentStandard cut = makeDefaultCUT();

        final Item junk = mock(Item.class);
        when(junk.getHardpointType()).thenReturn(HardPointType.NONE);
        when(junk.getSlots()).thenReturn(freeSlots + 1);

        cut.addItem(junk);

        // 8 total slots
        // 4*3 SHS + 1*3 junk = 7 used slots
        // Engine takes 6 and consumes 4 SHS, + 3 from junk = 9 slots, won't fit.

        assertEquals(EquipResult.make(location, EquipResultType.NotEnoughSlots), cut.canEquip(item));
    }

    /**
     * If there are more heat sinks that can fit in the engine...
     */
    @Test
    public final void testCanEquip_EngineWithTooManyHS() {
        slots = 8;

        final Engine item = mock(Engine.class);
        final int hsSlots = 4;
        final int freeSlots = 1;
        when(item.getHardpointType()).thenReturn(HardPointType.NONE);
        when(item.getSlots()).thenReturn(slots - freeSlots);
        when(item.getNumHeatsinkSlots()).thenReturn(hsSlots);
        when(internal.isAllowed(item)).thenReturn(true);
        final ConfiguredComponentStandard cut = makeDefaultCUT();

        for (int i = 0; i < hsSlots + freeSlots; ++i) {
            cut.addItem(ItemDB.SHS);
        }
        cut.addItem(ItemDB.SHS); // One heat sink too many
        assertEquals(EquipResult.make(location, EquipResultType.NotEnoughSlots), cut.canEquip(item));
    }

    @Test
    public void testCanEquip_HasHardpoint() {
        final Item item = mock(Item.class);
        when(item.getSlots()).thenReturn(1);
        when(item.getHardpointType()).thenReturn(HardPointType.ENERGY);

        when(stdInternal.getHardPointCount(HardPointType.ENERGY)).thenReturn(1);
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

    /**
     * Having C.A.S.E. does not prohibit other items.
     */
    @Test
    public final void testCanEquip_OneCASE() {
        final ConfiguredComponent cut = makeDefaultCUT();
        cut.addItem(ItemDB.CASE);

        final Item item = mock(Item.class);
        when(item.getHardpointType()).thenReturn(HardPointType.NONE);
        when(item.getSlots()).thenReturn(1);

        assertEquals(EquipResult.SUCCESS, cut.canEquip(item));
    }

    /**
     * We do not allow two C.A.S.E. in the same component as that is just bonkers.
     */
    @Test
    public final void testCanEquip_TwoCASE() {
        final ConfiguredComponent cut = makeDefaultCUT();
        cut.addItem(ItemDB.CASE);
        assertEquals(EquipResult.make(location, EquipResultType.ComponentAlreadyHasCase), cut.canEquip(ItemDB.CASE));
    }

    @Test
    public void testCopyCtorEquals() {
        final ConfiguredComponentStandard cut = makeDefaultCUT();
        assertEquals(cut, new ConfiguredComponentStandard(cut));
    }

    @Test
    public void testGetHardPointCount() {
        when(stdInternal.getHardPointCount(HardPointType.ENERGY)).thenReturn(7);
        assertEquals(7, makeDefaultCUT().getHardPointCount(HardPointType.ENERGY));
    }

    @Test
    public void testGetHardPoints() {
        hardPoints.add(new HardPoint(HardPointType.BALLISTIC));
        hardPoints.add(new HardPoint(HardPointType.ECM));
        hardPoints.add(new HardPoint(HardPointType.ENERGY));
        hardPoints.add(new HardPoint(HardPointType.ENERGY));

        assertTrue(ListArrayUtils.equalsUnordered(hardPoints, new ArrayList<>(makeDefaultCUT().getHardPoints())));
    }

    @Test
    public void testHasMissileBayDoors() {
        assertEquals(baydoors, makeDefaultCUT().hasMissileBayDoors());
        baydoors = !baydoors;
        assertEquals(baydoors, makeDefaultCUT().hasMissileBayDoors());
    }

    @Override
    protected ConfiguredComponentStandard makeDefaultCUT() {
        when(internal.getLocation()).thenReturn(location);
        when(internal.getSlots()).thenReturn(slots);
        when(internal.getFixedItemSlots()).thenReturn(internalFixedSlots);
        when(internal.getFixedItems()).thenReturn(internalFixedItems);
        when(internal.getArmourMax()).thenReturn(maxArmour);
        when(stdInternal.getHardPoints()).thenReturn(hardPoints);
        when(stdInternal.hasMissileBayDoors()).thenReturn(baydoors);
        return new ConfiguredComponentStandard(stdInternal, manualArmour);
    }

}
