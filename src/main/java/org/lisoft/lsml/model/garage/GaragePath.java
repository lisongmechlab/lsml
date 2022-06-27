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

import org.lisoft.lsml.util.ListArrayUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * A path in a garage that can refer to either a directory or a value.
 *
 * @param <T> The type of values this garage path refers to.
 * @author Li Song
 */
public class GaragePath<T> {
    private static final char ESCAPE_SYMBOL = '$';
    private static final char PATH_SEPARATOR = '/';
    final private GarageDirectory<T> dir;
    final private GaragePath<T> parent;
    final private Optional<T> value;

    public GaragePath(GarageDirectory<T> aRoot) {
        parent = null;
        dir = aRoot;
        value = Optional.empty();
    }

    public GaragePath(GaragePath<T> aParent, GarageDirectory<T> aChild) {
        if (aParent == null) {
            throw new IllegalArgumentException("The parent must not be null!");
        }
        parent = aParent;
        dir = aChild;
        value = Optional.empty();
    }

    public GaragePath(GaragePath<T> aParent, T aValue) {
        if (aParent == null) {
            throw new IllegalArgumentException("The parent must not be null!");
        }
        parent = aParent;
        dir = aParent.dir;
        value = Optional.of(aValue);
    }

    /**
     * Creates a new path from a string and a root directory.
     *
     * @param aString The string to parse.
     * @param aRoot   The root directory.
     * @return A new {@link GaragePath} matching the string.
     * @throws IOException if the path is invalid or points to a non-existent node.
     */
    public static <T> GaragePath<T> fromPath(String aString, GarageDirectory<T> aRoot) throws IOException {
        GaragePath<T> path = new GaragePath<>(aRoot);

        final List<String> components = splitPath(aString);
        for (final Iterator<String> it = components.iterator(); it.hasNext(); ) {
            final String component = it.next();

            boolean found = false;
            for (final GarageDirectory<T> child : path.getTopDirectory().getDirectories()) {
                if (child.getName().equals(component)) {
                    path = new GaragePath<>(path, child);
                    found = true;
                    break;
                }
            }

            if (!found) {
                if (it.hasNext()) {
                    // We didn't find the path component we looked for and we were not on the last part of the path
                    // (i.e. can't be a leaf). The path is referring to a non-existent item.
                    throw new IOException("Invalid garage path!");
                }
                for (final T value : path.getTopDirectory().getValues()) {
                    if (value.toString().equals(component)) {
                        return new GaragePath<>(path, value);
                    }
                }
                throw new IOException("Invalid garage path!");
            }
        }
        return path;
    }

    /**
     * Checks to see if the given name is available under the given path.
     * <p>
     * In other words it checks if it is safe to add a entry (value or directory) with the given name, under the given
     * path.
     *
     * @param aPath The path to check in.
     * @param aName The name to check for.
     * @return <code>true</code> if the name is available for use.
     */
    public static boolean isNameAvailalble(GaragePath<?> aPath, String aName) {
        final GarageDirectory<?> dir = aPath.getTopDirectory();
        return !ListArrayUtils.containsByToString(aName, dir.getDirectories()) &&
               !ListArrayUtils.containsByToString(aName, dir.getValues());
    }

    /**
     * Splits and unescapes the given escaped garage string into path component strings.
     *
     * @param aString The string to split.
     * @return A {@link List} of patch components that are unescaped.
     * @throws IOException Thrown if the path is invalid.
     */
    public static List<String> splitPath(String aString) throws IOException {
        final List<String> ans = new ArrayList<>();
        int start = 0;
        while (start < aString.length() && aString.charAt(start) == PATH_SEPARATOR) {
            start++;
        }

        // Loop-invariant: aString.charAt(start) != PATH_SEPARATOR
        while (start < aString.length()) {
            int end = start;

            final StringBuilder sb = new StringBuilder();

            char c;
            while (end < aString.length() && (c = aString.charAt(end)) != PATH_SEPARATOR) {
                if (c == ESCAPE_SYMBOL) {
                    end += 2;
                    if (end > aString.length()) {
                        throw new IOException("Invalid string! Ended with escape char!");
                    }
                    sb.append(aString.charAt(end - 1));
                } else {
                    end += 1;
                    sb.append(c);
                }
            }

            // Fulfil loop invariant and prepare for next iteration
            while (end < aString.length() && aString.charAt(end) == PATH_SEPARATOR) {
                end++;
            }
            start = end;

            // Match component against directories first
            ans.add(sb.toString());
        }
        return ans;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GaragePath) {
            final GaragePath<?> garagePath = (GaragePath<?>) obj;
            return toPath().equals(garagePath.toPath());
        }
        return false;
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
        return dir;
    }

    public Optional<T> getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return toPath().hashCode();
    }

    public boolean isLeaf() {
        return value.isPresent();
    }

    public boolean isRoot() {
        return parent == null;
    }

    public String toPath() {
        final StringBuilder sb = new StringBuilder();
        toPath(sb);
        return sb.toString();
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
        return dir.toString();
    }

    /**
     * @param aString
     * @return
     */
    private static String escape(String aString) {
        // FIXME: I'm sure this is broken in some way, fix it later.
        return aString.replace(String.valueOf(ESCAPE_SYMBOL), "" + ESCAPE_SYMBOL + ESCAPE_SYMBOL)
                      .replace("/", ESCAPE_SYMBOL + "/");
    }
}
