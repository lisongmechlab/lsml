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

package lisong_mechlab.model.metrics;

import java.util.Collection;

import lisong_mechlab.model.chassi.MovementProfile;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.modifiers.Modifier;

/**
 * This {@link Metric} calculates how fast a mech will turn (degrees per second).
 * 
 * @author Li Song
 */
public class TurningSpeed implements Metric {

    private final LoadoutBase<?> loadout;

    public TurningSpeed(LoadoutBase<?> aLoadout) {
        loadout = aLoadout;
    }

    @Override
    public double calculate() {
        Engine engine = loadout.getEngine();
        if (engine == null)
            return 0.0;
        return getTurnRateAtThrottle(0.0, engine.getRating(), loadout.getChassis().getMassMax(),
                loadout.getMovementProfile(), loadout.getModifiers());
    }

    public static double getTurnRateAtThrottle(double aThrottle, int aEngineRating, double aMassMax,
            MovementProfile aMovementProfile, Collection<Modifier> aModifiers) {
        final double k = aEngineRating / aMassMax * 180.0 / Math.PI;

        MovementProfile mp = aMovementProfile;

        if (aThrottle <= mp.getTurnLerpLowSpeed(aModifiers)) {
            return k * mp.getTurnLerpLowRate(aModifiers);
        }
        else if (aThrottle <= mp.getTurnLerpMidSpeed(aModifiers)) {
            final double f = (aThrottle - mp.getTurnLerpLowSpeed(aModifiers))
                    / (mp.getTurnLerpMidSpeed(aModifiers) - mp.getTurnLerpLowSpeed(aModifiers));
            return k
                    * (mp.getTurnLerpLowRate(aModifiers) + (mp.getTurnLerpMidRate(aModifiers) - mp
                            .getTurnLerpLowRate(aModifiers)) * f);
        }
        else if (aThrottle < mp.getTurnLerpHighSpeed(aModifiers)) {
            final double f = (aThrottle - mp.getTurnLerpMidSpeed(aModifiers))
                    / (mp.getTurnLerpHighSpeed(aModifiers) - mp.getTurnLerpMidSpeed(aModifiers));
            return k
                    * (mp.getTurnLerpMidRate(aModifiers) + (mp.getTurnLerpHighRate(aModifiers) - mp
                            .getTurnLerpMidRate(aModifiers)) * f);
        }
        else {
            return k * mp.getTurnLerpHighRate(aModifiers);
        }
    }
}
