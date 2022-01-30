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
package org.lisoft.lsml.model.metrics;

import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;

import java.util.Collection;

/**
 * This {@link Metric} calculates the total heat capacity of a {@link LoadoutStandard}.
 *
 * @author Li Song
 */
public class HeatCapacity implements Metric {
    /**
     * Each mech has a base heat capacity of 30.
     */
    private static final double BASE_HEAT_CAPACITY = 30;
    /**
     * Each mech is required to have 10 heat sinks, and the first 10 count as engine heat sinks with different capacity
     */
    private static final int MANDATORY_ENGINE_HEAT_SINKS = 10;
    private final Loadout loadout;

    public HeatCapacity(final Loadout aLoadout) {
        loadout = aLoadout;
    }

    @Override
    public double calculate() {
        final Collection<Modifier> modifiers = loadout.getAllModifiers();
        final HeatSink protoHeatSink = loadout.getUpgrades().getHeatSink().getHeatSinkType();

        final int externalHeatSinks = Math.max(0, loadout.getTotalHeatSinksCount() - MANDATORY_ENGINE_HEAT_SINKS);
        final int internalHeatSinks = loadout.getTotalHeatSinksCount() - externalHeatSinks;
        final double engineCapacity = internalHeatSinks * protoHeatSink.getEngineCapacity();
        final double externalCapacity = externalHeatSinks * protoHeatSink.getCapacity();

        // As of writing (2022-01-30) the `heatlimit` quirk had zero occurrences in the mech MDF files when I searched
        // through them. It appears that this quirk is no longer present. However, this selector is still used for
        // heat containment pilot skill. After testing with 15% heat containment by firing an alpha strike with:
        // http://t.li-soft.org/?l=rwNFOEIGRhJjCUIGRDiSpSnKUIykBbsrXXXXXXXUevXXXXXXXXUW6mpK5tdR63r11BcM
        // which at the time of writing had an alpha of 50 heat and a capacity of 67.
        //
        // If the modifier applies to all the capacity, an alpha should do: 50/(1.15*67) ~= 65% of total heat.
        // If the modifier applies to the base and mandatory heat sinks: 50/(1.15(30 + 10*2.25) + 14.5) ~= 67%.
        // If the modifier applies to only the base 30, an alpha should do: 50/(1.15*30 +37) ~=70%.
        //
        // Measured: 67%. Thus the heat containment modifier only applies to base capacity and mandatory heatsinks.
        final double modifiedCapacity = BASE_HEAT_CAPACITY + engineCapacity;
        final Attribute capacity = new Attribute(modifiedCapacity, ModifierDescription.SEL_HEAT_LIMIT);

        // Environmental effects and engine throttle do not impact heat capacity, this was tested with:
        // http://t.li-soft.org/?l=rwNFOEIGRhJjCUIGRDiSpSnKUISkDGFuyjGMPWLQlcw9b14BcA%3D%3D
        // which had 100% heat from an alpha at the time of writing. Testing on both hot (Terra Therma) and cold
        // (Frozen City) maps while moving and stationary showed no difference in either the heat display
        // (all peaked at 99) or made the mech overheat. Also verified with single vs double.
        // As of my testing at 2022-01-30, on live servers, I believe that the math here is accurate.
        return capacity.value(modifiers) + externalCapacity;
    }
}
