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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.mwo_data.mechs.MovementProfile;
import org.lisoft.mwo_data.modifiers.Modifier;

/**
 * This {@link Metric} calculates how fast a mech will turn (degrees per second).
 *
 * @author Li Song
 */
public class TurningSpeed implements Metric, VariableMetric {

  private final Loadout loadout;

  public TurningSpeed(Loadout aLoadout) {
    loadout = aLoadout;
  }

  public static double getTurnRateAtThrottle(
      double aThrottle, MovementProfile aMovementProfile, Collection<Modifier> aModifiers) {
    final double k = 180.0 / Math.PI;

    if (aThrottle <= aMovementProfile.getTurnLerpLowSpeed(aModifiers)) {
      return k * aMovementProfile.getTurnLerpLowRate(aModifiers);
    } else if (aThrottle <= aMovementProfile.getTurnLerpMidSpeed(aModifiers)) {
      final double f =
          (aThrottle - aMovementProfile.getTurnLerpLowSpeed(aModifiers))
              / (aMovementProfile.getTurnLerpMidSpeed(aModifiers)
                  - aMovementProfile.getTurnLerpLowSpeed(aModifiers));
      return k
          * (aMovementProfile.getTurnLerpLowRate(aModifiers)
              + (aMovementProfile.getTurnLerpMidRate(aModifiers)
                      - aMovementProfile.getTurnLerpLowRate(aModifiers))
                  * f);
    } else if (aThrottle < aMovementProfile.getTurnLerpHighSpeed(aModifiers)) {
      final double f =
          (aThrottle - aMovementProfile.getTurnLerpMidSpeed(aModifiers))
              / (aMovementProfile.getTurnLerpHighSpeed(aModifiers)
                  - aMovementProfile.getTurnLerpMidSpeed(aModifiers));
      return k
          * (aMovementProfile.getTurnLerpMidRate(aModifiers)
              + (aMovementProfile.getTurnLerpHighRate(aModifiers)
                      - aMovementProfile.getTurnLerpMidRate(aModifiers))
                  * f);
    } else {
      return k * aMovementProfile.getTurnLerpHighRate(aModifiers);
    }
  }

  @Override
  public double calculate() {
    return calculate(0.0);
  }

  @Override
  public double calculate(double aThrottle) {
    return getTurnRateAtThrottle(
        aThrottle, loadout.getMovementProfile(), loadout.getAllModifiers());
  }

  @Override
  public String getArgumentName() {
    return "Speed [km/h]";
  }

  @Override
  public List<Double> getArgumentValues() {
    final ArrayList<Double> ans = new ArrayList<>();

    final MovementProfile mp = loadout.getMovementProfile();
    final Collection<Modifier> modifiers = loadout.getAllModifiers();

    ans.add(0.0);
    ans.add(mp.getTurnLerpLowSpeed(modifiers));
    ans.add(mp.getTurnLerpMidSpeed(modifiers));
    ans.add(mp.getTurnLerpHighSpeed(modifiers));
    ans.add(1.0);

    double prev = Double.NaN;
    final Iterator<Double> it = ans.iterator();
    while (it.hasNext()) {
      final double curr = it.next();
      if (curr == prev) {
        it.remove();
      }
      prev = curr;
    }

    return ans;
  }

  @Override
  public String getMetricName() {
    return "Turning Speed [°/s]";
  }
}
