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
import java.util.List;

import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.item.Item;

/**
 * This class is used to iterate over all {@link Item}s of a given type that are equipped on a loadout.
 * 
 * @author Emily Björk
 * @param <T>
 *            A type that the wanted items must implement.
 *
 */
public class LoadoutIterator<T> implements Iterator<T> {
    private enum IterationState {
        Fixed, Equipped
    }

    private final static Location[] LOCATION_ORDER = Location.values();
    private List<Item> items;
    private IterationState state = IterationState.Fixed;
    private int index = 0;
    private final Loadout loadout;
    private final Class<T> filter;
    private Location currentLocation = LOCATION_ORDER[0];

    LoadoutIterator(Loadout aLoadout, Class<T> aFilter) {
        loadout = aLoadout;
        filter = aFilter;
        items = loadout.getComponent(currentLocation).getItemsFixed();
    }

    LoadoutIterator(Loadout aLoadout) {
        this(aLoadout, null);
    }

    @Override
    public boolean hasNext() {
        return null != getNextItem();
    }

    @Override
    public T next() {
        T ans = getNextItem();
        index++;
        return ans;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    private T getNextItem() {
        while (true) {
            if (index < items.size()) {
                Item item = items.get(index);
                if (filter == null || filter.isAssignableFrom(item.getClass())) {
                    return (T) item; // This cast is checked
                }
                index++;
            }
            else {
                index = 0;
                if (state == IterationState.Fixed) {
                    state = IterationState.Equipped;
                    items = loadout.getComponent(currentLocation).getItemsEquipped();
                }
                else {
                    if (currentLocation.ordinal() == LOCATION_ORDER.length - 1) {
                        return null; // End of items
                    }
                    currentLocation = LOCATION_ORDER[currentLocation.ordinal() + 1];
                    state = IterationState.Fixed;
                    items = loadout.getComponent(currentLocation).getItemsFixed();
                }
            }
        }
    }
}
