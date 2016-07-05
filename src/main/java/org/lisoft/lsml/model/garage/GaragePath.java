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

import java.io.IOException;
import java.util.Optional;

/**
 * A path in a garage that can refer to either a directory or a value.
 *
 * @author Emily Björk
 * @param <T>
 *            The type of values this garage path refers to.
 */
public class GaragePath<T> {
    private static final char PATH_SEPARATOR = '/';

    public static <T> GaragePath<T> fromPath(String aString, GarageDirectory<T> aRoot) throws IOException {
        final String[] pathComponents = aString.split("/"); // FIXME: This will break escaped strings!
        GaragePath<T> path = new GaragePath<>((GaragePath<T>) null, aRoot);

        for (final String pathComponent : pathComponents) {
            if (pathComponent.isEmpty()) {
                continue;
            }
            final String name = unescape(pathComponent);
            boolean found = false;
            for (final GarageDirectory<T> child : path.getTopDirectory().getDirectories()) {
                if (child.getName().equals(pathComponent)) {
                    path = new GaragePath<>(path, child);
                    found = true;
                    break;
                }
            }

            if (!found) {
                if (!pathComponent.equals(pathComponents[pathComponents.length - 1])) {
                    // We didn't find the path component we looked for and we were not on the last part of the path
                    // (i.e. can't be a leaf). The path is referring to a non-existent item.
                    throw new IOException("Invalid garage path!");
                }
                for (final T value : path.getTopDirectory().getValues()) {
                    if (value.toString().equals(name)) {
                        return new GaragePath<>(path, value);
                    }
                }
            }
        }
        return path;
    }

    /**
     * @param aString
     * @return
     */
    private static String escape(String aString) {
        // FIXME: I'm sure this is broken in some way, fix it later.
        return aString.replace("\\", "\\\\").replace("/", "\\/");
    }

    /**
     * @param aString
     * @return
     */
    private static String unescape(String aString) {
        // FIXME: I'm sure this is broken in some way, fix it later.
        return aString.replace("\\/", "/").replace("\\\\", "\\");
    }

    final private GaragePath<T> parent;

    final private Optional<GarageDirectory<T>> tld;

    final private Optional<T> value;

    public GaragePath(GarageDirectory<T> aRoot) {
        this(null, aRoot);
    }

    public GaragePath(GaragePath<T> aParent, GarageDirectory<T> aChild) {
        assert aParent == null || aParent.tld.isPresent();
        parent = aParent;
        tld = Optional.of(aChild);
        value = Optional.empty();
    }

    public GaragePath(GaragePath<T> aParent, T aValue) {
        assert aParent.tld.isPresent();
        parent = aParent;
        tld = Optional.empty();
        value = Optional.of(aValue);
    }

    public GaragePath<T> getParent() {
        return parent;
    }

    /**
     * @return The parent of this directory.
     */
    public GarageDirectory<T> getParentDirectory() {
        return parent.getTopDirectory();
    }

    public GarageDirectory<T> getTopDirectory() {
        if (tld.isPresent()) {
            return tld.get();
        }
        return parent.getTopDirectory();
    }

    public Optional<T> getValue() {
        return value;
    }

    public boolean isLeaf() {
        return value.isPresent();
    }

    public boolean isRoot() {
        return parent == null;
    }

    public void toPath(StringBuilder aSb) {
        if (!isRoot()) {
            parent.toPath(aSb);
            aSb.append(PATH_SEPARATOR).append(escape(toString()));
        }
    }

    @Override
    public String toString() {
        if (value.isPresent()) {
            return value.get().toString();
        }
        return tld.get().toString();
    }
}
