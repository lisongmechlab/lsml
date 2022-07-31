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
     * Each mech is required to have 10 heat sinks, and the first 10 count as engine heat sinks with different capacity
     */
    static final int MANDATORY_ENGINE_HEAT_SINKS = 10;
    /**
     * Each mech has a base heat capacity of 30.
     */
    private static final double BASE_HEAT_CAPACITY = 30;
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

        // Update (2022-07-31): The pilot skills now use `maxheat` like the mech quirks do and `heatlimit` quirk
        // has been renamed to `maxheat`.
        //
        // Based on testing in issue #770 we have concluded that the `maxheat` modifier only applies to the base
        // heat capacity.
        //
        // Testing methodology below:
        //
        // We have previously verified that alpha heat, and it's assumed to be correctly computed.
        //
        // Three hypoetheses:
        // H1: `maxheat` applies only to base capacity of 30.
        // H2: `maxheat` applies to base capacity and internal heatsinks.
        // H3: `maxheat` applies to the total heat capacity.
        //
        // We perform 4 experiments where a specific loadout is taken into a game, and we fire a full alpha when at
        // 0% heat and while standing still and observe whether we have a shutdown.
        //
        // E1: CRB-27B - A<582000|I@|Edp00|i^|i^|i^q00r00|I@|I@s00|a?|i^|i^t00u00v00w000000
        // Alpha heat: 57.228
        // Heat capacity under H1, H2 and H3: 57, 60, 60.38
        // Result: No shutdown. H1 is ruled out, because the heat cap is lower than alpha heat, but we observed no
        // shut down.
        //
        // E2: Same as E1 but remove 1 DHS.
        // Heat capacity under H1, H2 and H3: 56.5, 59.5, 59.80
        // Result: Shutdown. H2 and H3 are ruled out, because the heat cap is higher than alpha heat,
        // but we observed a shutdown.
        //
        // E3: CRB-27B - A<580000|I@|Edp00|h^|h^q00r00|a?s00|a?|a?|h^|h^|h^t00u00v00w000000.
        // Alpha heat: 61.028
        // Heat capacity under H1, H2 and H3: 61.25, 64.64, 65.26
        // Result: No shutdown. Heat cap is at least 61.029.
        //
        // E4: Same as E3 but remove 1 SHS.
        // Heat capacity under H1, H2 and H3: 60.4, 63.78, 64.29
        // Result: Shutdown. H1 holds, H2 and H3 discarded.
        //
        //
        // All three hypotheses are ruled out at this point. However, if the heat generation precedes heat sinking
        // which precedes the check for a shutdown condition, and the server tick rate is 10 Hz, then H1 would
        // survive E1 and E1 would be only surviving hypothesis. I have reached out to PGI with a request for
        // confirmation.
        //
        // We take H1 as the truth for now:
        final Attribute base_capacity = new Attribute(BASE_HEAT_CAPACITY, ModifierDescription.SEL_HEAT_LIMIT);

        // Environmental effects and engine throttle do not impact heat capacity, this was tested with:
        // http://t.li-soft.org/?l=rwNFOEIGRhJjCUIGRDiSpSnKUISkDGFuyjGMPWLQlcw9b14BcA%3D%3D
        // which had 100% heat from an alpha at the time of writing. Testing on both hot (Terra Therma) and cold
        // (Frozen City) maps while moving and stationary showed no difference in either the heat display
        // (all peaked at 99) or made the mech overheat. Also verified with single vs double.
        // As of my testing at 2022-01-30, on live servers, I believe that the math here is accurate.
        return base_capacity.value(modifiers) + externalCapacity + engineCapacity;
    }
}
