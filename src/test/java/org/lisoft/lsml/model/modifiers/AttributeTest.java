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
package org.lisoft.lsml.model.modifiers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

/**
 * A test suite for {@link Attribute}.
 *
 * @author Li Song
 */
public class AttributeTest {

    @Test
    public void testEquals_AllSame() {
        final Attribute a1 = new Attribute(1.0, Arrays.asList("foo"), "bar");
        final Attribute a2 = new Attribute(1.0, Arrays.asList("foo"), "bar");

        assertEquals(a1, a2);
    }

    @Test
    public void testEquals_Null() {
        final Attribute a1 = new Attribute(1.0, Arrays.asList("foo"), "bar");
        assertFalse(a1.equals(null));
    }

    @Test
    public void testEquals_SelectorDifferentOrder() {
        final Attribute a1 = new Attribute(1.0, Arrays.asList("foo", "bar"), "bar");
        final Attribute a2 = new Attribute(1.0, Arrays.asList("bar", "foo"), "bar");
        assertTrue(a1.equals(a2));
    }

    @Test
    public void testEquals_SelectorDiffers() {
        final Attribute a1 = new Attribute(1.0, Arrays.asList("foo"), "bar");
        final Attribute a2 = new Attribute(1.0, Arrays.asList("foz"), "bar");
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEquals_Self() {
        final Attribute a1 = new Attribute(1.0, Arrays.asList("foo"), "bar");
        assertTrue(a1.equals(a1));
    }

    @Test
    public void testEquals_SpecifierDiffers() {
        final Attribute a1 = new Attribute(1.0, Arrays.asList("foo"), "bar");
        final Attribute a2 = new Attribute(1.0, Arrays.asList("foo"), "");
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testEquals_ValueDiffers() {
        final Attribute a1 = new Attribute(1.0, Arrays.asList("foo"), "bar");
        final Attribute a2 = new Attribute(1.1, Arrays.asList("foo"), "bar");

        assertFalse(a1.equals(a2));
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testEquals_WrongType() {
        final Attribute a1 = new Attribute(1.0, Arrays.asList("foo"), "bar");
        assertFalse(a1.equals(new String("foo")));
    }

    @Test
    public void testToString() {
        final double value = 3.15;
        final Attribute cut = new Attribute(value, Arrays.asList("selector"));
        assertEquals(Double.toString(value), cut.toString());
    }
}
