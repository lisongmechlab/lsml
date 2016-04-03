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
package org.lisoft.lsml.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class contains some static methods for making dealing with arrays and lists easier.
 * 
 * @author Emily Björk
 */
public class ListArrayUtils {

    public static <T, V> boolean equalsUnordered(List<T> aLeft, List<V> aRight) {
        List<T> t = new ArrayList<>(aLeft);
        for (V i : aRight) {
            if (!t.remove(i))
                return false;
        }
        return t.isEmpty();
    }

    @SuppressWarnings("unchecked")
    // It is checked.
    public static <T, E> List<T> filterByType(List<E> aList, Class<T> clazz) {
        List<T> ans = new ArrayList<>();
        for (E e : aList) {
            if (clazz.isAssignableFrom(e.getClass())) {
                ans.add((T) e);
            }
        }
        return ans;
    }

    public static <T, E> int countByType(List<E> aList, Class<T> clazz) {
        int ans = 0;
        for (E e : aList) {
            if (clazz.isAssignableFrom(e.getClass())) {
                ans++;
            }
        }
        return ans;
    }

    public static <T> boolean containsByToString(T aValue, Collection<T> aCollection) {
        if (null == aCollection)
            return false;

        String string = aValue.toString();
        for (T v : aCollection) {
            if ((v == null) != (string == null))
                continue;

            if (v == null || v.toString().equals(string)) {
                return true;
            }
        }
        return false;
    }
}
