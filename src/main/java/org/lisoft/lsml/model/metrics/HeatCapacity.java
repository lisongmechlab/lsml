/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.model.metrics;

import java.util.Collection;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.mwo_data.equipment.HeatSink;
import org.lisoft.mwo_data.modifiers.Attribute;
import org.lisoft.mwo_data.modifiers.Modifier;
import org.lisoft.mwo_data.modifiers.ModifierDescription;

/**
 * This {@link Metric} calculates the total heat capacity of a {@link LoadoutStandard}.
 *
 * @author Li Song
 */
public class HeatCapacity implements Metric {
  /**
   * Each mech is required to have 10 heat sinks, and the first 10 count as engine heat sinks with
   * different capacity
   */
  static final int MANDATORY_ENGINE_HEAT_SINKS = 10;
  /** Each mech has a base heat capacity of 30. */
  private static final double BASE_HEAT_CAPACITY = 30;

  private final Loadout loadout;

  public HeatCapacity(final Loadout aLoadout) {
    loadout = aLoadout;
  }

  @Override
  public double calculate() {
    final Collection<Modifier> modifiers = loadout.getAllModifiers();
    final HeatSink protoHeatSink = loadout.getUpgrades().getHeatSink().getHeatSinkType();

    final int externalHeatSinks =
        Math.max(0, loadout.getTotalHeatSinksCount() - MANDATORY_ENGINE_HEAT_SINKS);
    final int internalHeatSinks = loadout.getTotalHeatSinksCount() - externalHeatSinks;
    final double engineCapacity = internalHeatSinks * protoHeatSink.getEngineCapacity();
    final double externalCapacity = externalHeatSinks * protoHeatSink.getCapacity();

    // 2022-08-09: Li Song has asked PGI for clarification. The answer is that the mech quirk
    // called `maxheat` applies to the base heat capacity of 30. And the "Heat Containment"
    // pilot skill (that's also called `maxheat` in the game files) applies to the total
    // heat capacity (i.e. after heat sinks). However, the answer was not clear on if the
    // total formula is:
    // A) (30*(1+x) + heat sinks)*(1+y) or
    // B) (30 + heat sinks)*(1+y) + 30*x.
    // I.e. do they compound multiplicative or additively. More testing or followup question needed.
    //
    // In the same response PGI also noted that overheat occurs on 101% heat due to rounding;
    // this explains inconsistencies in previous experiments.
    final Attribute base_capacity =
        new Attribute(BASE_HEAT_CAPACITY, ModifierDescription.SEL_HEAT_LIMIT);

    // Environmental effects and engine throttle do not impact heat capacity, this was tested with:
    // http://t.li-soft.org/?l=rwNFOEIGRhJjCUIGRDiSpSnKUISkDGFuyjGMPWLQlcw9b14BcA%3D%3D
    // which had 100% heat from an alpha at the time of writing. Testing on both hot (Terra Therma)
    // and cold
    // (Frozen City) maps while moving and stationary showed no difference in either the heat
    // display
    // (all peaked at 99) or made the mech overheat. Also verified with single vs double.
    return base_capacity.value(modifiers) + externalCapacity + engineCapacity;
  }
}
