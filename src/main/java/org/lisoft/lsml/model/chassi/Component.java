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
package org.lisoft.lsml.model.chassi;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.Modifier;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * This is a base class for all mech components.
 *
 * @author Li Song
 */
public abstract class Component {
    @XStreamImplicit
    private final List<Item> fixedItems;
    @XStreamAsAttribute
    private final Attribute hitpoints;
    @XStreamAsAttribute
    private final Location location;
    @XStreamAsAttribute
    private final int slots;

    /**
     * Creates a new {@link Component}.
     *
     * @param aCriticalSlots The number of critical slots in the component.
     * @param aHitPoints     The number of internal hit points on the component (determines armour too).
     * @param aLocation      The location of the component.
     * @param aFixedItems    An array of fixed {@link Item}s for this component.
     */
    public Component(int aCriticalSlots, Attribute aHitPoints, Location aLocation, List<Item> aFixedItems) {
        slots = aCriticalSlots;
        hitpoints = aHitPoints;
        location = aLocation;
        fixedItems = aFixedItems;
    }

    /**
     * @return The maximum amount of armour on this component.
     */
    public int getArmourMax() {
        return calculateMaxArmour(getLocation(), hitpoints.value(null));
    }

    /**
     * @return The number of slots that are occupied by fixed items in this component.
     */
    public int getFixedItemSlots() {
        int ans = 0;
        int hs = 0;
        int hsSlots = 0;
        int hsSize = 0;
        for (final Item item : getFixedItems()) {
            ans += item.getSlots();
            if (item instanceof Engine) {
                final Engine engine = (Engine) item;
                hsSlots = engine.getNumHeatsinkSlots();
            } else if (item instanceof HeatSink) {
                hs++;
                hsSize = item.getSlots();
            }
        }
        return ans - Math.min(hs, hsSlots) * hsSize;
    }

    /**
     * @return An unmodifiable collection of all {@link Item}s this {@link ComponentOmniMech} has.
     */
    public List<Item> getFixedItems() {
        if (fixedItems == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(fixedItems);
    }

    /**
     * @param aModifiers The modifiers to use when calculating the health.
     * @return The amount of structure hit points on this component.
     */
    public double getHitPoints(Collection<Modifier> aModifiers) {
        return hitpoints.value(aModifiers);
    }

    /**
     * @return The {@link Location} this component is mounted at.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @return The total number of critical slots in this location.
     */
    public int getSlots() {
        return slots;
    }

    /**
     * Checks if a specific item is allowed on this component checking only local, static constraints. This method is
     * only useful if {@link Chassis#isAllowed(Item)} returns true.
     *
     * @param aItem The {@link Item} to check.
     * @return <code>true</code> if the given {@link Item} is allowed on this {@link ComponentStandard}.
     */
    public boolean isAllowed(Item aItem) {
        return isAllowed(aItem, null);
    }

    /**
     * Checks if a specific item is allowed on this component checking only local, static constraints. This method is
     * only useful if {@link Chassis#isAllowed(Item)} returns true.
     *
     * @param aItem   The {@link Item} to check.
     * @param aEngine If not <code>null</code>, this engine is assumed to be equipped.
     * @return <code>true</code> if the given {@link Item} is allowed on this {@link ComponentStandard}.
     */
    public boolean isAllowed(Item aItem, Engine aEngine) {
        final Optional<Collection<Location>> components = aItem.getAllowedComponents();
        return !components.isPresent() || components.get().contains(location);
    }

    @Override
    public String toString() {
        return getLocation().toString();
    }

    private static int calculateMaxArmour(Location aLocation, double aHP) {
        return aLocation == Location.Head ? 18 : (int) (aHP * 2);
    }
}
