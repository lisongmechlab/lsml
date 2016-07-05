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
package org.lisoft.lsml.model.loadout;

import java.util.Iterator;

import org.lisoft.lsml.model.item.Item;

/**
 * This is a glue class to get {@link Iterable}s for different types of {@link Item}s on a loadout.
 *
 * @author Emily Björk
 * @param <T>
 *            A type that any {@link Item} iterated over must implement.
 *
 */
public class LoadoutIterable<T> implements Iterable<T> {
    private final Loadout loadout;
    private final Class<T> filter;

    public LoadoutIterable(Loadout aLoadout, Class<T> aFilter) {
        loadout = aLoadout;
        filter = aFilter;
    }

    @Override
    public Iterator<T> iterator() {
        return new LoadoutIterator<>(loadout, filter);
    }
}