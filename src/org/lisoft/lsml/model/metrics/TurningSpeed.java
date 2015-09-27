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

package org.lisoft.lsml.model.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.lisoft.lsml.model.chassi.MovementProfile;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.modifiers.Modifier;

/**
 * This {@link Metric} calculates how fast a mech will turn (degrees per second).
 * 
 * @author Emily Björk
 */
public class TurningSpeed implements Metric, VariableMetric {

    private final LoadoutBase<?> loadout;

    public TurningSpeed(LoadoutBase<?> aLoadout) {
        loadout = aLoadout;
    }

    @Override
    public double calculate() {
        return calculate(0.0);
    }

    public static double getTurnRateAtSpeed(double aSpeed, int aEngineRating, double aMassMax,
            MovementProfile aMovementProfile, Collection<Modifier> aModifiers) {
        if (aEngineRating < 1) {
            return 0.0;
        }

        final double k = aEngineRating / aMassMax * 180.0 / Math.PI;
        final double topSpeed = TopSpeed.calculate(aEngineRating, aMovementProfile, aMassMax, aModifiers);
        final double throttle = aSpeed / topSpeed;

        MovementProfile mp = aMovementProfile;

        if (throttle <= mp.getTurnLerpLowSpeed(aModifiers)) {
            return k * mp.getTurnLerpLowRate(aModifiers);
        }
        else if (throttle <= mp.getTurnLerpMidSpeed(aModifiers)) {
            final double f = (throttle - mp.getTurnLerpLowSpeed(aModifiers))
                    / (mp.getTurnLerpMidSpeed(aModifiers) - mp.getTurnLerpLowSpeed(aModifiers));
            return k * (mp.getTurnLerpLowRate(aModifiers)
                    + (mp.getTurnLerpMidRate(aModifiers) - mp.getTurnLerpLowRate(aModifiers)) * f);
        }
        else if (throttle < mp.getTurnLerpHighSpeed(aModifiers)) {
            final double f = (throttle - mp.getTurnLerpMidSpeed(aModifiers))
                    / (mp.getTurnLerpHighSpeed(aModifiers) - mp.getTurnLerpMidSpeed(aModifiers));
            return k * (mp.getTurnLerpMidRate(aModifiers)
                    + (mp.getTurnLerpHighRate(aModifiers) - mp.getTurnLerpMidRate(aModifiers)) * f);
        }
        else {
            return k * mp.getTurnLerpHighRate(aModifiers);
        }
    }

    @Override
    public double calculate(double aValue) {
        Engine engine = loadout.getEngine();
        if (engine == null)
            return 0.0;
        return getTurnRateAtSpeed(aValue, engine.getRating(), loadout.getChassis().getMassMax(),
                loadout.getMovementProfile(), loadout.getModifiers());
    }

    @Override
    public String getMetricName() {
        return "Turning Speed [°/s]";
    }

    @Override
    public String getArgumentName() {
        return "Speed [km/h]";
    }

    @Override
    public List<Double> getArgumentValues() {
        ArrayList<Double> ans = new ArrayList<>();
        Engine engine = loadout.getEngine();

        if (engine == null)
            return ans;

        MovementProfile mp = loadout.getMovementProfile();
        Collection<Modifier> modifiers = loadout.getModifiers();
        final int rating = engine.getRating();
        final double topSpeed = TopSpeed.calculate(rating, mp, loadout.getChassis().getMassMax(), modifiers);

        ans.add(0.0);
        ans.add(mp.getTurnLerpLowSpeed(modifiers) * topSpeed);
        ans.add(mp.getTurnLerpMidSpeed(modifiers) * topSpeed);
        ans.add(mp.getTurnLerpHighSpeed(modifiers) * topSpeed);
        ans.add(topSpeed);

        double prev = Double.NaN;
        Iterator<Double> it = ans.iterator();
        while(it.hasNext()){
            double curr = it.next();
            if(curr == prev){
                it.remove();
            }
            prev = curr;
        }
        
        return ans;
    }
}
