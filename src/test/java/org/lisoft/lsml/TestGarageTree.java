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
package org.lisoft.lsml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.GarageDirectory;

/**
 * This class builds a garage tree structure that is usable for unit tests.
 *
 * Build the following directory tree:
 * <ul>
 * <li>/x</li>
 * <li>/w</li>
 * <li>/1/y</li>
 * <li>/1/a/</li>
 * <li>/1/b/</li>
 * <li>/2/c/</li>
 * <li>/2/d/z</li>
 * </ul>
 *
 * @author Emily Björk
 *
 */
public class TestGarageTree {
    public final GarageDirectory<NamedObject> root = new GarageDirectory<>("");
    public final GarageDirectory<NamedObject> dir1 = new GarageDirectory<>("1");
    public final GarageDirectory<NamedObject> dir2 = new GarageDirectory<>("2");
    public final GarageDirectory<NamedObject> dira = new GarageDirectory<>("a");
    public final GarageDirectory<NamedObject> dirb = new GarageDirectory<>("b");
    public final GarageDirectory<NamedObject> dirc = new GarageDirectory<>("c");
    public final GarageDirectory<NamedObject> dird = new GarageDirectory<>("d");
    public final NamedObject x = new NamedObject("x");
    public final NamedObject y = new NamedObject("y");
    public final NamedObject z = new NamedObject("z");
    public final NamedObject w = new NamedObject("w");

    public TestGarageTree() {
        root.getDirectories().add(dir1);
        root.getDirectories().add(dir2);
        dir1.getDirectories().add(dira);
        dir1.getDirectories().add(dirb);
        dir2.getDirectories().add(dirc);
        dir2.getDirectories().add(dird);
        root.getValues().add(x);
        root.getValues().add(w);
        dir1.getValues().add(y);
        dird.getValues().add(z);
    }

    public void assertUnmodified() {
        assertEquals(2, root.getValues().size());
        assertEquals(2, root.getDirectories().size());
        assertTrue(root.getValues().contains(x));
        assertTrue(root.getValues().contains(w));
        assertTrue(root.getDirectories().contains(dir1));
        assertTrue(root.getDirectories().contains(dir2));

        assertEquals(1, dir1.getValues().size());
        assertEquals(2, dir1.getDirectories().size());
        assertTrue(dir1.getValues().contains(y));
        assertTrue(dir1.getDirectories().contains(dira));
        assertTrue(dir1.getDirectories().contains(dirb));

        assertEquals(0, dira.getValues().size());
        assertEquals(0, dira.getDirectories().size());

        assertEquals(0, dirb.getValues().size());
        assertEquals(0, dirb.getDirectories().size());

        assertEquals(0, dir2.getValues().size());
        assertEquals(2, dir2.getDirectories().size());
        assertTrue(dir2.getDirectories().contains(dirc));
        assertTrue(dir2.getDirectories().contains(dird));

        assertEquals(0, dirc.getValues().size());
        assertEquals(0, dirc.getDirectories().size());

        assertEquals(1, dird.getValues().size());
        assertEquals(0, dird.getDirectories().size());
        assertTrue(dird.getValues().contains(z));

        assertEquals("", root.getName());
        assertEquals("1", dir1.getName());
        assertEquals("2", dir2.getName());
        assertEquals("a", dira.getName());
        assertEquals("b", dirb.getName());
        assertEquals("c", dirc.getName());
        assertEquals("d", dird.getName());
        assertEquals("x", x.getName());
        assertEquals("y", y.getName());
        assertEquals("z", z.getName());
        assertEquals("w", w.getName());
    }
}
