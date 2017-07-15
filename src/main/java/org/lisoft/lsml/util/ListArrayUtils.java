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
package org.lisoft.lsml.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class contains some static methods for making dealing with arrays and lists easier.
 *
 * @author Li Song
 */
public class ListArrayUtils {

    public static boolean containsByToString(Object aValue, Collection<?> aCollection) {
        if (null == aCollection) {
            return false;
        }

        final String string = aValue.toString();
        for (final Object v : aCollection) {
            if (v == null != (string == null)) {
                continue;
            }

            if (v == null || v.toString().equals(string)) {
                return true;
            }
        }
        return false;
    }

    public static <T, E> int countByType(List<E> aList, Class<T> clazz) {
        int ans = 0;
        for (final E e : aList) {
            if (clazz.isAssignableFrom(e.getClass())) {
                ans++;
            }
        }
        return ans;
    }

    public static boolean equalsUnordered(List<?> aLeft, List<?> aRight) {
        final List<?> t = new ArrayList<>(aLeft);
        for (final Object i : aRight) {
            if (!t.remove(i)) {
                return false;
            }
        }
        return t.isEmpty();
    }

    @SuppressWarnings("unchecked")
    // It is checked.
    public static <T, E> List<T> filterByType(List<E> aList, Class<T> clazz) {
        final List<T> ans = new ArrayList<>();
        for (final E e : aList) {
            if (clazz.isAssignableFrom(e.getClass())) {
                ans.add((T) e);
            }
        }
        return ans;
    }
}
