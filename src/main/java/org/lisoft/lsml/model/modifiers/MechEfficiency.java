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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class holds data for each mech efficiency.
 *
 * @author Li Song
 */
public class MechEfficiency {
    @XStreamAsAttribute
    private final double value;
    @XStreamAsAttribute
    private final double eliteValue;
    private final List<ModifierDescription> descriptions;

    /**
     * Creates a new {@link MechEfficiency} instance.
     *
     * @param aValue
     *            The value of the efficiency.
     * @param aEliteValue
     *            The value of the efficiency when all elite efficiencies are unlocked.
     * @param aDescriptions
     *            The list of {@link ModifierDescription} for this {@link MechEfficiency}.
     */
    public MechEfficiency(double aValue, double aEliteValue, List<ModifierDescription> aDescriptions) {
        descriptions = aDescriptions;
        value = aValue;
        eliteValue = aEliteValue;
    }

    /**
     * Creates and returns a {@link Collection} of new {@link Modifier}s that matches this efficiency.
     *
     * @param aElite
     *            If <code>true</code> returns a modified for the efficiency with all elite efficiencies unlocked.
     * @return A {@link Collection} of new {@link Modifier}s for this {@link MechEfficiency}.
     */
    public Collection<Modifier> makeModifiers(boolean aElite) {
        final List<Modifier> ans = new ArrayList<>();
        for (final ModifierDescription description : descriptions) {
            ans.add(new Modifier(description, aElite ? eliteValue : value));
        }
        return ans;
    }

}
