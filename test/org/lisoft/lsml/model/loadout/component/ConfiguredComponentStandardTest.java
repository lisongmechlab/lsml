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
import org.mockito.Mockito;

/**
 * Test suite for {@link ConfiguredComponentBase}.
 * 
 * @author Emily Björk
 */
public class ConfiguredComponentStandardTest extends ConfiguredComponentBaseTest {

    protected boolean           baydoors   = false;
    protected ComponentStandard stdInternal;
    protected List<HardPoint>   hardPoints = new ArrayList<>();

    @Override
    protected ConfiguredComponentStandard makeDefaultCUT() {
        Mockito.when(internal.getLocation()).thenReturn(location);
        Mockito.when(internal.getSlots()).thenReturn(slots);
        Mockito.when(internal.getFixedItemSlots()).thenReturn(internalFixedSlots);
        Mockito.when(internal.getFixedItems()).thenReturn(internalFixedItems);
        Mockito.when(internal.getArmorMax()).thenReturn(maxArmor);
        Mockito.when(stdInternal.getHardPoints()).thenReturn(hardPoints);
        Mockito.when(stdInternal.hasMissileBayDoors()).thenReturn(baydoors);
        return new ConfiguredComponentStandard(stdInternal, manualArmor);
    }

    @Before
    public void setup() {
        stdInternal = Mockito.mock(ComponentStandard.class);
        internal = stdInternal;
        Mockito.when(internal.isAllowed(Matchers.any(Item.class))).thenReturn(true);
    }

    @Test
        public void testCanEquip_AllHardpointsTaken() {
            Item item = Mockito.mock(Item.class);
            Mockito.when(item.getNumCriticalSlots()).thenReturn(1);
            Mockito.when(item.getHardpointType()).thenReturn(HardPointType.ENERGY);
    
            Mockito.when(stdInternal.getHardPointCount(HardPointType.ENERGY)).thenReturn(1);
            hardPoints.add(new HardPoint(HardPointType.ENERGY));
            ConfiguredComponentStandard cut = makeDefaultCUT();
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
            Engine engine = Mockito.mock(Engine.class);
            Mockito.when(engine.getNumCriticalSlots()).thenReturn(slots);
            Mockito.when(engine.getNumHeatsinkSlots()).thenReturn(2);
            Mockito.when(engine.getHardpointType()).thenReturn(HardPointType.NONE);
            
            HeatSink heatSink = Mockito.mock(HeatSink.class);
            Mockito.when(heatSink.getNumCriticalSlots()).thenReturn(3);
            Mockito.when(heatSink.getHardpointType()).thenReturn(HardPointType.NONE);
    
            ConfiguredComponentStandard cut = makeDefaultCUT();
            cut.addItem(engine);
    
            assertEquals(EquipResult.SUCCESS, cut.canEquip(heatSink));
            cut.addItem(heatSink);
            assertEquals(EquipResult.SUCCESS, cut.canEquip(heatSink));
            cut.addItem(heatSink);
            assertEquals(EquipResult.make(location, EquipResultType.NotEnoughSlots), cut.canEquip(heatSink));
        }

    @Test
        public void testCanEquip_HasHardpoint() {
            Item item = Mockito.mock(Item.class);
            Mockito.when(item.getNumCriticalSlots()).thenReturn(1);
            Mockito.when(item.getHardpointType()).thenReturn(HardPointType.ENERGY);
    
            Mockito.when(stdInternal.getHardPointCount(HardPointType.ENERGY)).thenReturn(1);
            hardPoints.add(new HardPoint(HardPointType.ENERGY));
    
            assertEquals(EquipResult.SUCCESS, makeDefaultCUT().canEquip(item));
        }

    @Test
        public void testCanEquip_NoHardpoint() {
            Item item = Mockito.mock(Item.class);
            Mockito.when(item.getNumCriticalSlots()).thenReturn(1);
            Mockito.when(item.getHardpointType()).thenReturn(HardPointType.ENERGY);
    
            assertEquals(EquipResult.make(location, EquipResultType.NoFreeHardPoints), makeDefaultCUT().canEquip(item));
        }

    /**
         * Having C.A.S.E. does not prohibit other items.
         */
        @Test
        public final void testCanEquip_OneCASE() {
            ConfiguredComponentBase cut = makeDefaultCUT();
            cut.addItem(ItemDB.CASE);
    
            Item item = Mockito.mock(Item.class);
            Mockito.when(item.getHardpointType()).thenReturn(HardPointType.NONE);
            Mockito.when(item.getNumCriticalSlots()).thenReturn(1);
    
            assertEquals(EquipResult.SUCCESS, cut.canEquip(item));
        }

    /**
         * We do not allow two C.A.S.E. in the same component as that is just bonkers.
         */
        @Test
        public final void testCanEquip_TwoCASE() {
            ConfiguredComponentBase cut = makeDefaultCUT();
            cut.addItem(ItemDB.CASE);
            assertEquals(EquipResult.make(location, EquipResultType.ComponentAlreadyHasCase), cut.canEquip(ItemDB.CASE));
        }

    @Test
    public void testCopyCtorEquals() {
        ConfiguredComponentStandard cut = makeDefaultCUT();
        assertEquals(cut, new ConfiguredComponentStandard(cut));
    }

    @Test
    public void testGetHardPointCount() {
        Mockito.when(stdInternal.getHardPointCount(HardPointType.ENERGY)).thenReturn(7);
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

}
