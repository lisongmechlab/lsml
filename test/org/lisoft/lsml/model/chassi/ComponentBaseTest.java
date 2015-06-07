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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.mockito.Mockito;

/**
 * Test suite for {@link ComponentBase}.
 * 
 * @author Emily Björk
 */
public abstract class ComponentBaseTest {

    protected int        criticalSlots = 5;
    protected double     hp            = 15;
    protected Location   location      = Location.Head;
    protected List<Item> fixedItems    = new ArrayList<>();

    protected abstract ComponentBase makeDefaultCUT();

    @Test
    public void testGetFixedItems_NotNull() {
        assertNotNull(makeDefaultCUT().getFixedItems());
    }

    @Test
    public void testGetFixedItems() {
        assertEquals(fixedItems, makeDefaultCUT().getFixedItems());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetFixedItems_Immutable() {
        makeDefaultCUT().getFixedItems().add(null);
    }

    /**
     * The string representation of the component shall contain the name of the location.
     */
    @Test
    public void testToString() {
        assertTrue(makeDefaultCUT().toString().contains(location.toString()));
    }

    @Test
    public void testGetFixedItemSlots() {
        Item item1 = Mockito.mock(Item.class);
        Item item2 = Mockito.mock(Item.class);

        Mockito.when(item1.getNumCriticalSlots()).thenReturn(3);
        Mockito.when(item2.getNumCriticalSlots()).thenReturn(4);

        fixedItems.clear();
        fixedItems.add(item1);
        fixedItems.add(item2);

        assertEquals(7, makeDefaultCUT().getFixedItemSlots());
    }

    @Test
    public void testGetFixedItemSlots_EngineHS() {
        criticalSlots = 12;
        Engine engine = Mockito.mock(Engine.class);
        HeatSink hs = Mockito.mock(HeatSink.class);
        Internal gyro = Mockito.mock(Internal.class);

        Mockito.when(engine.getNumCriticalSlots()).thenReturn(6);
        Mockito.when(gyro.getNumCriticalSlots()).thenReturn(4);
        Mockito.when(hs.getNumCriticalSlots()).thenReturn(2);

        Mockito.when(engine.getNumHeatsinkSlots()).thenReturn(4);

        fixedItems.clear();
        fixedItems.add(gyro);
        fixedItems.add(hs); // Add some heat sinks before the engine and some after.
        fixedItems.add(hs);
        fixedItems.add(engine);
        fixedItems.add(hs);
        fixedItems.add(hs);
        fixedItems.add(hs);

        // Gyro (4) + Engine (6) + 4x Engine HS (0) + 1x External HS (2) = 12

        assertEquals(12, makeDefaultCUT().getFixedItemSlots());
    }

    @Test
    public void testGetSlots() {
        assertEquals(criticalSlots, makeDefaultCUT().getSlots());
    }

    @Test
    public void testGetLocation() {
        assertEquals(location, makeDefaultCUT().getLocation());
    }

    @Test
    public void testGetHitPoints() {
        assertEquals(hp, makeDefaultCUT().getHitPoints(), 0.0);
    }

    @Test
    public void testGetArmorMax_Head() {
        hp = 20;
        location = Location.Head;
        assertEquals(18, makeDefaultCUT().getArmorMax());
    }

    @Test
    public void testGetArmorMax_Other() {
        hp = 20;
        location = Location.CenterTorso;
        assertEquals(40, makeDefaultCUT().getArmorMax());
    }
}
