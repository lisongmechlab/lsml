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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import org.junit.Test;
import org.lisoft.lsml.model.loadout.Loadout;

public class GarageDirectoryTest {
    private GarageDirectory<Object> cut = new GarageDirectory<>();

    @Test
    public void testAddContent() {
        final Loadout loadout = mock(Loadout.class);

        assertTrue(cut.getValues().add(loadout));
        assertTrue(cut.getValues().contains(loadout));
    }

    @Test
    public void testAddDirectory() {
        final GarageDirectory<Object> child = new GarageDirectory<>();
        assertTrue(cut.getDirectories().add(child));
        assertTrue(cut.getDirectories().contains(child));
    }

    @Test
    public void testContains() {
        cut.getValues().add("FOO");
        assertTrue(cut.recursiveFind("FOO").isPresent());
        assertSame(cut, cut.recursiveFind("FOO").get());
        assertFalse(cut.recursiveFind("FOo").isPresent());
    }

    @Test
    public void testContains_Recursive() {
        final GarageDirectory<Object> child1 = mock(GarageDirectory.class);
        final GarageDirectory<Object> child2 = mock(GarageDirectory.class);
        final Object key = "Foo";

        cut.getDirectories().add(child1);
        cut.getDirectories().add(child2);
        when(child1.recursiveFind(key)).thenReturn(Optional.empty());
        when(child2.recursiveFind(key)).thenReturn(Optional.empty());

        assertFalse(cut.recursiveFind(key).isPresent());

        verify(child1).recursiveFind(key);
        verify(child2).recursiveFind(key);
    }

    @Test
    public void testContains_RecursiveFound() {
        final GarageDirectory<Object> child1 = mock(GarageDirectory.class);
        final GarageDirectory<Object> child2 = mock(GarageDirectory.class);
        final Object key = "Foo";

        when(child1.recursiveFind(key)).thenReturn(Optional.empty());
        when(child2.recursiveFind(key)).thenReturn(Optional.of(child2));

        cut.getDirectories().add(child1);
        cut.getDirectories().add(child2);

        assertTrue(cut.recursiveFind(key).isPresent());
        assertSame(child2, cut.recursiveFind(key).get());
    }

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

    /**
     * Two {@link GarageDirectory} are equal if their contents are deeply equal, they have the same name and their child
     * directories are also recursively equal.
     */
    @Test
    public void testEquals_Equals() {
        final GarageDirectory<String> cut1 = new GarageDirectory<>("Foo");
        final GarageDirectory<String> cut11 = new GarageDirectory<>("Foo1");
        final GarageDirectory<String> cut12 = new GarageDirectory<>("Foo2");
        cut11.getValues().add("Hello");
        cut11.getValues().add("World");
        cut12.getValues().add("Holas");
        cut12.getValues().add("Muchachos");
        cut1.getValues().add("Good");
        cut1.getValues().add("Morning");
        cut1.getValues().add("Sun shine!");
        cut1.getDirectories().add(cut11);
        cut11.getDirectories().add(cut12);

        final GarageDirectory<String> cut2 = new GarageDirectory<>("Foo");
        final GarageDirectory<String> cut21 = new GarageDirectory<>("Foo1");
        final GarageDirectory<String> cut22 = new GarageDirectory<>("Foo2");
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
        final GarageDirectory<Object> cut1 = new GarageDirectory<>("Foo");
        final GarageDirectory<Object> cut2 = new GarageDirectory<>("Bar");
        assertFalse(cut1.equals(cut2));
    }

    @Test
    public void testEquals_Null() {
        assertFalse(cut.equals(null));
    }

    @Test
    public void testEquals_RecursiveDiffers() {
        final GarageDirectory<String> cut1 = new GarageDirectory<>("Foo");
        final GarageDirectory<String> cut11 = new GarageDirectory<>("Foo1");
        cut11.getValues().add("Hello");
        cut1.getDirectories().add(cut11);

        final GarageDirectory<String> cut2 = new GarageDirectory<>("Foo");
        final GarageDirectory<String> cut21 = new GarageDirectory<>("Foo1");
        cut21.getValues().add("Hello");
        cut21.getValues().add("World");
        cut2.getDirectories().add(cut21);

        assertFalse(cut1.equals(cut2));
    }

    @Test
    public void testEquals_RecursiveOrderDiffers() {
        final GarageDirectory<String> cut1 = new GarageDirectory<>("Foo");
        final GarageDirectory<String> cut11 = new GarageDirectory<>("Foo1");
        cut11.getValues().add("World");
        cut11.getValues().add("Hello");
        cut1.getDirectories().add(cut11);

        final GarageDirectory<String> cut2 = new GarageDirectory<>("Foo");
        final GarageDirectory<String> cut21 = new GarageDirectory<>("Foo1");
        cut21.getValues().add("Hello");
        cut21.getValues().add("World");
        cut2.getDirectories().add(cut21);

        assertTrue(cut1.equals(cut2));
    }

    @Test
    public void testEquals_Self() {
        assertTrue(cut.equals(cut));
    }

    @Test
    public void testEquals_ValueOrderDiffers() {
        final GarageDirectory<String> cut1 = new GarageDirectory<>("Foo");
        cut1.getValues().add("Good");
        cut1.getValues().add("Morning");
        cut1.getValues().add("Sun shine!");

        final GarageDirectory<String> cut2 = new GarageDirectory<>("Foo");
        cut2.getValues().add("Sun shine!");
        cut2.getValues().add("Good");
        cut2.getValues().add("Morning");

        assertTrue(cut1.equals(cut2));
    }

    @Test
    public void testEquals_WrongClass() {
        assertFalse(cut.equals("Foo"));
    }

    @Test
    public void testMkDirsRecursive() throws IOException {
        final String path = " /path/ to/top / ";
        final GarageDirectory<Object> leaf = cut.makeDirsRecursive(path);

        assertEquals(1, cut.getDirectories().size());

        final GarageDirectory<Object> pathDir = cut.getDirectories().get(0);
        assertEquals("path", pathDir.getName());
        assertEquals(1, pathDir.getDirectories().size());

        final GarageDirectory<Object> toDir = pathDir.getDirectories().get(0);
        assertEquals("to", toDir.getName());
        assertEquals(1, toDir.getDirectories().size());

        final GarageDirectory<Object> topDir = toDir.getDirectories().get(0);
        assertEquals("top", topDir.getName());
        assertEquals(0, topDir.getDirectories().size());

        assertSame(topDir, leaf);
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
}
