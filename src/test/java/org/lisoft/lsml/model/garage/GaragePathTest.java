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
package org.lisoft.lsml.model.garage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.model.NamedObject;

public class GaragePathTest {
    final GarageDirectory<Object> testRoot = new GarageDirectory<>();

    @Before
    public void setup() throws IOException {
        testRoot.makeDirsRecursive("foo/bar/a/b");
        testRoot.makeDirsRecursive("foo/bar/b/b");
        testRoot.makeDirsRecursive("foo/bar/c/b");
        testRoot.makeDirsRecursive("foo/dar/a/d");
    }

    @Test
    public void testFromPath_Directory() throws IOException {
        // Setup
        final String basePathString = "/foo/bar/c/b";

        // Execute
        final GaragePath<Object> basePath = GaragePath.fromPath(basePathString, testRoot);

        // Verify
        assertFalse(basePath.isLeaf());
        assertFalse(basePath.isRoot());
        assertEquals("c", basePath.getParentDirectory().getName());
        assertEquals("c", basePath.getParent().getTopDirectory().getName());
        final StringBuilder sb = new StringBuilder();
        basePath.toPath(sb);
        assertEquals(basePathString, sb.toString());
    }

    @Test
    public void testFromPath_DirectoryDoubleSlashes() throws IOException {
        // Setup
        final String basePathString = "//foo//bar//c//b";

        // Execute
        final GaragePath<Object> basePath = GaragePath.fromPath(basePathString, testRoot);

        // Verify
        assertFalse(basePath.isLeaf());
        assertFalse(basePath.isRoot());
        assertEquals("c", basePath.getParentDirectory().getName());
        assertEquals("c", basePath.getParent().getTopDirectory().getName());
        final StringBuilder sb = new StringBuilder();
        basePath.toPath(sb);
        assertEquals("/foo/bar/c/b", sb.toString());
    }

    @Test
    public void testFromPath_DirectoryEmbeddedSlash() throws IOException {
        // Setup
        final GaragePath<Object> path = GaragePath.fromPath("/foo/bar", testRoot);
        path.getTopDirectory().getDirectories().add(new GarageDirectory<>("x/y$z"));

        final String basePathString = "/foo/bar/x$/y$$z";

        // Execute
        final GaragePath<Object> valuePath = GaragePath.fromPath(basePathString, testRoot);

        // Verify
        assertFalse(valuePath.isLeaf());
        assertEquals("x/y$z", valuePath.getTopDirectory().getName());
        assertEquals("bar", valuePath.getParent().getTopDirectory().getName());
        assertEquals("bar", valuePath.getParentDirectory().getName());
    }

    @Test
    public void testFromPath_DirectoryNoInitialSlash() throws IOException {
        // Setup
        final String basePathString = "foo/bar/c/b";

        // Execute
        final GaragePath<Object> basePath = GaragePath.fromPath(basePathString, testRoot);

        // Verify
        assertFalse(basePath.isLeaf());
        assertFalse(basePath.isRoot());
        assertEquals("c", basePath.getParentDirectory().getName());
        assertEquals("c", basePath.getParent().getTopDirectory().getName());
        final StringBuilder sb = new StringBuilder();
        basePath.toPath(sb);
        assertEquals("/" + basePathString, sb.toString());
    }

    @Test
    public void testFromPath_File() throws IOException {
        // Setup
        final NamedObject value = new NamedObject("named");
        final String basePathString = "/foo/bar/c/b";
        final GarageDirectory<Object> topDir = GaragePath.fromPath(basePathString, testRoot).getTopDirectory();
        topDir.getValues().add(value);

        // Execute
        final GaragePath<Object> valuePath = GaragePath.fromPath(basePathString + "/" + value.toString(), testRoot);

        // Verify
        assertTrue(valuePath.isLeaf());
        assertTrue(valuePath.getValue().isPresent());
        assertSame(value, valuePath.getValue().get());
        assertEquals("b", valuePath.getTopDirectory().getName());
        assertEquals("b", valuePath.getParent().getTopDirectory().getName());
        assertEquals("b", valuePath.getParentDirectory().getName());
    }

    @Test
    public void testFromPath_FileEmbeddedSlash() throws IOException {
        // Setup
        final NamedObject value = new NamedObject("nam/ed");
        final String basePathString = "/foo/bar/c/b";
        final GaragePath<Object> basePath = GaragePath.fromPath(basePathString, testRoot);
        final GaragePath<Object> childPath = new GaragePath<>(basePath, value);
        final GarageDirectory<Object> topDir = basePath.getTopDirectory();
        topDir.getValues().add(value);

        // Execute
        final StringBuilder sb = new StringBuilder();
        childPath.toPath(sb);
        final String childPathString = sb.toString();

        final GaragePath<Object> valuePath = GaragePath.fromPath(childPathString, testRoot);

        // Verify
        assertTrue(valuePath.isLeaf());
        assertTrue(valuePath.getValue().isPresent());
        assertSame(value, valuePath.getValue().get());
        assertEquals("b", valuePath.getTopDirectory().getName());
        assertEquals("b", valuePath.getParent().getTopDirectory().getName());
        assertEquals("b", valuePath.getParentDirectory().getName());
    }

    @Test(expected = IOException.class)
    public void testFromPath_NonExistentInnerNode() throws IOException {
        // Setup
        final String basePathString = "/foo/bar/d/b";

        GaragePath.fromPath(basePathString, testRoot);
    }

    @Test(expected = IOException.class)
    public void testFromPath_NonExistentLeaf() throws IOException {
        // Setup
        final String basePathString = "/foo/bar/c/b/x";

        GaragePath.fromPath(basePathString, testRoot);
    }

    @Test(expected = IOException.class)
    public void testFromPath_NonExistentLeafWrongName() throws IOException {
        // Setup
        final NamedObject value = new NamedObject("named");
        final String basePathString = "/foo/bar/c/b";
        final GarageDirectory<Object> topDir = GaragePath.fromPath(basePathString, testRoot).getTopDirectory();
        topDir.getValues().add(value);

        GaragePath.fromPath(basePathString + "/whatev", testRoot);
    }

    @Test
    public void testGaragePath() {
        final GarageDirectory<Object> root = new GarageDirectory<>();
        final GarageDirectory<Object> child = new GarageDirectory<>();
        final NamedObject value = new NamedObject("foo");
        root.getDirectories().add(child);
        child.getValues().add(value);

        final GaragePath<Object> pathRoot = new GaragePath<>(root);
        final GaragePath<Object> pathChild = new GaragePath<Object>(pathRoot, child);
        final GaragePath<Object> pathValue = new GaragePath<>(pathChild, value);

        assertSame(root, pathChild.getParentDirectory());
        assertSame(root, pathChild.getParent().getTopDirectory());
        assertTrue(pathValue.isLeaf());
        assertTrue(pathValue.getValue().isPresent());
        assertSame(value, pathValue.getValue().get());
        assertSame(child, pathValue.getTopDirectory());
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testGaragePath_NullParentDirectory() {
        final GarageDirectory<Object> child = new GarageDirectory<>();

        new GaragePath<Object>(null, child);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testGaragePath_NullParentValue() {
        new GaragePath<Object>(null, new NamedObject("foo"));
    }

    @Test
    public void testSplitPath_InitialEscapedSeparator() throws IOException {
        // Setup
        final String basePathString = "$/foo/bar";

        final List<String> ans = GaragePath.splitPath(basePathString);
        assertEquals(2, ans.size());
        assertEquals("/foo", ans.get(0));
        assertEquals("bar", ans.get(1));
    }

    @Test(expected = IOException.class)
    public void testSplitPath_TrailingEscape() throws IOException {
        // Setup
        final String basePathString = "/foo/bar/c/b$";

        GaragePath.splitPath(basePathString);
    }
}
