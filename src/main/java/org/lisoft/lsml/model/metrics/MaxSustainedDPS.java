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

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.mwo_data.equipment.Engine;
import org.lisoft.mwo_data.equipment.Weapon;
import org.lisoft.mwo_data.modifiers.Modifier;

/**
 * This {@link Metric} calculates the maximal DPS that a {@link LoadoutStandard} can sustain
 * indefinitely assuming that the pilot is moving at full throttle.
 *
 * @author Li Song
 */
public class MaxSustainedDPS extends AbstractRangeMetric {
  private final HeatDissipation dissipation;
  private final int weaponGroup;

  /**
   * Creates a new {@link MaxSustainedDPS} that calculates the maximal possible sustained DPS for
   * the given loadout using all weapons.
   *
   * @param aLoadout The loadout to calculate for.
   * @param aHeatDissipation A metric that calculates the effective heat dissipation for the
   *     loadout.
   */
  public MaxSustainedDPS(final Loadout aLoadout, final HeatDissipation aHeatDissipation) {
    this(aLoadout, aHeatDissipation, -1);
  }

  /**
   * Creates a new {@link MaxSustainedDPS} that calculates the maximal possible sustained DPS for
   * the given weapon group.
   *
   * @param aLoadout The loadout to calculate for.
   * @param aHeatDissipation A metric that calculates the effective heat dissipation for the
   *     loadout.
   * @param aGroup The weapon group to calculate the metric for.
   */
  public MaxSustainedDPS(
      final Loadout aLoadout, final HeatDissipation aHeatDissipation, int aGroup) {
    super(aLoadout);
    dissipation = aHeatDissipation;
    weaponGroup = aGroup;
  }

  @Override
  public double calculate(double aRange) {
    checkRange(aRange);
    double ans = 0.0;
    final Map<Weapon, Double> dd = getWeaponRatios(aRange);
    final Collection<Modifier> modifiers = loadout.getAllModifiers();
    for (final Map.Entry<Weapon, Double> entry : dd.entrySet()) {
      final Weapon weapon = entry.getKey();
      final double ratio = entry.getValue();
      final double rangeEffectivity = weapon.getRangeEffectiveness(aRange, modifiers);
      ans += rangeEffectivity * weapon.getStat("d/s", modifiers) * ratio;
    }
    return ans;
  }

  /**
   * Calculates the ratio with each weapon should be fired to obtain the maximal sustained DPS. A
   * ratio of 0.0 means the weapon is never fired and a ratio of 0.5 means the weapon is fired every
   * 2 cool downs and a ratio of 1.0 means the weapon is fired every time it is available. This
   * method assumes that the engine is at full throttle.
   *
   * @param aRange The range to calculate for.
   * @return A {@link Map} with {@link Weapon} as key and a {@link Double} as value representing a %
   *     of how often the weapon is used.
   */
  public Map<Weapon, Double> getWeaponRatios(final double aRange) {
    final Collection<Modifier> modifiers = loadout.getAllModifiers();
    double heatLeft = dissipation.calculate();
    final Engine engine = loadout.getEngine();
    if (null != engine) {
      heatLeft -= engine.getHeat(modifiers);
    }

    final Comparator<Weapon> byDPH =
        (aO1, aO2) -> {
          // Note: D/H == DPS / HPS so we're ordering by highest DPS per HPS.
          final double rangeFactor1 =
              aRange >= 0.0 ? aO1.getRangeEffectiveness(aRange, modifiers) : 1.0;
          final double rangeFactor2 =
              aRange >= 0.0 ? aO2.getRangeEffectiveness(aRange, modifiers) : 1.0;
          // Note that getStat(d/h) may return +Infinity for some weapons (e.g. Machine Gun), if in
          // that case
          // rangeFactor is 0.0 then 0.0*Infinity will result in NaN which will ruin the sorting.
          // Avoid this by
          // shorting out getStat(d/h) if the range factor is 0.0.
          final double dps1 = rangeFactor1 == 0 ? 0 : rangeFactor1 * aO1.getStat("d/h", modifiers);
          final double dps2 = rangeFactor2 == 0 ? 0 : rangeFactor2 * aO2.getStat("d/h", modifiers);
          return Double.compare(dps2, dps1);
        };

    final Stream<Weapon> weapons;
    if (weaponGroup < 0) {
      weapons = StreamSupport.stream(loadout.items(Weapon.class).spliterator(), false);
    } else {
      weapons = loadout.getWeaponGroups().getWeapons(weaponGroup, loadout).stream();
    }

    final List<Weapon> filteredWeapons = weapons.filter(Weapon::isOffensive).sorted(byDPH).toList();

    final Map<Weapon, Double> ans = new HashMap<>();
    for (final Weapon weapon : filteredWeapons) {
      final double heat = weapon.getStat("h/s", modifiers);
      final double ratio;

      if (heatLeft == 0) {
        ratio = 0;
      } else if (heat < heatLeft) {
        ratio = 1.0;
        heatLeft -= heat;
      } else {
        ratio = heatLeft / heat;
        heatLeft = 0;
      }

      final Double oldValue = ans.computeIfAbsent(weapon, aWeapon -> 0.0);
      ans.put(weapon, ratio + oldValue);
    }
    return ans;
  }
}
