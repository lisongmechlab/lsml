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

import java.lang.reflect.Field;

/**
 * This class provides some static utility functions for dealing with reflection.
 *
 * @author Emily Björk
 */
public class ReflectionUtil {

    public static <T, U> U getField(Class<T> aClass, T aObject, String aField, Class<U> aFieldClass)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        final Field f = aClass.getDeclaredField(aField);
        final Object value = f.get(aObject);

        if (aFieldClass.isAssignableFrom(value.getClass())) {
            return aFieldClass.cast(value);
        }

        throw new ClassCastException(aField + " isn't of type: " + aFieldClass.getSimpleName());
    }

    public static <T> void setField(Class<T> aClass, T aObject, String aField, Object aValue)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        final Field f = aClass.getDeclaredField(aField);
        f.set(aObject, aValue);
    }

}
