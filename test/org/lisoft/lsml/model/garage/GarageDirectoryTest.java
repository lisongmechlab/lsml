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
package org.lisoft.lsml.model.garage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.lisoft.lsml.model.loadout.LoadoutBase;

public class GarageDirectoryTest {
    private GarageDirectory<Object> cut = new GarageDirectory<>();

    @Test
    public void testDefaultState() {
        assertEquals("Unnamed Directory", cut.getName());
        assertTrue(cut.getDirectories().isEmpty());
        assertTrue(cut.getValues().isEmpty());
    }

    @Test
    public void testDefaultState_NamedCtor() {
        cut = new GarageDirectory<>("Name");
        assertEquals("Name", cut.getName());
        assertTrue(cut.getDirectories().isEmpty());
        assertTrue(cut.getValues().isEmpty());
    }

    @Test
    public void testSetName() {
        cut.setName("FooBar");
        assertEquals("FooBar", cut.getName());
    }

    @Test
    public void testToString() {
        cut.setName("Bar");
        assertEquals(cut.getName(), cut.toString());
    }

    @Test
    public void testAddDirectory() {
        GarageDirectory<Object> child = new GarageDirectory<>();
        assertTrue(cut.getDirectories().add(child));
        assertTrue(cut.getDirectories().contains(child));
    }

    @Test
    public void testAddContent() {
        LoadoutBase<?> loadout = mock(LoadoutBase.class);

        assertTrue(cut.getValues().add(loadout));
        assertTrue(cut.getValues().contains(loadout));
    }

    @Test
    public void testContains() {
        cut.getValues().add("FOO");
        assertTrue(cut.contains("FOO"));
        assertFalse(cut.contains("FOo"));
    }

    @Test
    public void testContains_Recursive() {
        GarageDirectory<Object> child1 = mock(GarageDirectory.class);
        GarageDirectory<Object> child2 = mock(GarageDirectory.class);
        Object key = "Foo";

        cut.getDirectories().add(child1);
        cut.getDirectories().add(child2);

        assertFalse(cut.contains(key));

        verify(child1).contains(key);
        verify(child2).contains(key);
    }

    @Test
    public void testContains_RecursiveFound() {
        GarageDirectory<Object> child1 = mock(GarageDirectory.class);
        GarageDirectory<Object> child2 = mock(GarageDirectory.class);
        Object key = "Foo";

        when(child2.contains(key)).thenReturn(true);

        cut.getDirectories().add(child1);
        cut.getDirectories().add(child2);

        assertTrue(cut.contains(key));

        verify(child1).contains(key);
        verify(child2).contains(key);
    }

    @Test
    public void testEquals_Null() {
        assertFalse(cut.equals(null));
    }

    @Test
    public void testEquals_Self() {
        assertTrue(cut.equals(cut));
    }

    @Test
    public void testEquals_WrongClass() {
        assertFalse(cut.equals("Foo"));
    }

    /**
     * Two {@link GarageDirectory} are equal if their contents are deeply equal, they have the same name and their child
     * directories are also recursively equal.
     */
    @Test
    public void testEquals_Equals() {
        GarageDirectory<String> cut1 = new GarageDirectory<>("Foo");
        GarageDirectory<String> cut11 = new GarageDirectory<>("Foo1");
        GarageDirectory<String> cut12 = new GarageDirectory<>("Foo2");
        cut11.getValues().add("Hello");
        cut11.getValues().add("World");
        cut12.getValues().add("Holas");
        cut12.getValues().add("Muchachos");
        cut1.getValues().add("Good");
        cut1.getValues().add("Morning");
        cut1.getValues().add("Sun shine!");
        cut1.getDirectories().add(cut11);
        cut11.getDirectories().add(cut12);

        GarageDirectory<String> cut2 = new GarageDirectory<>("Foo");
        GarageDirectory<String> cut21 = new GarageDirectory<>("Foo1");
        GarageDirectory<String> cut22 = new GarageDirectory<>("Foo2");
        cut21.getValues().add("Hello");
        cut21.getValues().add("World");
        cut22.getValues().add("Holas");
        cut22.getValues().add("Muchachos");
        cut2.getValues().add("Good");
        cut2.getValues().add("Morning");
        cut2.getValues().add("Sun shine!");
        cut2.getDirectories().add(cut21);
        cut21.getDirectories().add(cut22);

        assertTrue(cut1.equals(cut2));
    }

    @Test
    public void testEquals_NameDiffers() {
        GarageDirectory<Object> cut1 = new GarageDirectory<>("Foo");
        GarageDirectory<Object> cut2 = new GarageDirectory<>("Bar");
        assertFalse(cut1.equals(cut2));
    }

    @Test
    public void testEquals_ValueOrderDiffers() {
        GarageDirectory<String> cut1 = new GarageDirectory<>("Foo");
        cut1.getValues().add("Good");
        cut1.getValues().add("Morning");
        cut1.getValues().add("Sun shine!");

        GarageDirectory<String> cut2 = new GarageDirectory<>("Foo");
        cut2.getValues().add("Sun shine!");
        cut2.getValues().add("Good");
        cut2.getValues().add("Morning");

        assertTrue(cut1.equals(cut2));
    }

    @Test
    public void testEquals_RecursiveDiffers() {
        GarageDirectory<String> cut1 = new GarageDirectory<>("Foo");
        GarageDirectory<String> cut11 = new GarageDirectory<>("Foo1");
        cut11.getValues().add("Hello");
        cut1.getDirectories().add(cut11);

        GarageDirectory<String> cut2 = new GarageDirectory<>("Foo");
        GarageDirectory<String> cut21 = new GarageDirectory<>("Foo1");
        cut21.getValues().add("Hello");
        cut21.getValues().add("World");
        cut2.getDirectories().add(cut21);

        assertFalse(cut1.equals(cut2));
    }

    @Test
    public void testEquals_RecursiveOrderDiffers() {
        GarageDirectory<String> cut1 = new GarageDirectory<>("Foo");
        GarageDirectory<String> cut11 = new GarageDirectory<>("Foo1");
        cut11.getValues().add("World");
        cut11.getValues().add("Hello");
        cut1.getDirectories().add(cut11);

        GarageDirectory<String> cut2 = new GarageDirectory<>("Foo");
        GarageDirectory<String> cut21 = new GarageDirectory<>("Foo1");
        cut21.getValues().add("Hello");
        cut21.getValues().add("World");
        cut2.getDirectories().add(cut21);

        assertTrue(cut1.equals(cut2));
    }
}
