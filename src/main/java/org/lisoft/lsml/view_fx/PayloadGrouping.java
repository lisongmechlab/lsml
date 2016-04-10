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
package org.lisoft.lsml.view_fx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisOmniMech;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.metrics.PayloadStatistics;
import org.lisoft.lsml.model.metrics.TopSpeed;
import org.lisoft.lsml.model.modifiers.Efficiencies;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.util.ListArrayUtils;

import javafx.scene.chart.XYChart;

/**
 * This class models a group of chassis that will result in a (partially) shared payload curve.
 * 
 * @author Li Song
 *
 */
public class PayloadGrouping {
    /**
     * This class models a (partially) shared curve for several chassis.
     * 
     * @author Li Song
     */
    public static class Curve {
        private final List<DataPoint> points = new ArrayList<>();

        public Curve(Collection<Chassis> aChassisGroup, Efficiencies aEfficiencies) {
            for (Chassis entry : aChassisGroup) {
                addChassis(entry, aEfficiencies);
            }
        }

        void addSpeed(double aSpeed, Chassis aChassis, int aRating) {
            for (int i = 0; i < points.size(); ++i) {
                DataPoint section = points.get(i);
                if (aSpeed < section.speed) {
                    DataPoint cp = new DataPoint(aSpeed, aRating);
                    if (i == 0) {
                        cp = new DataPoint(aSpeed, aRating);
                    }
                    else {
                        cp = new DataPoint(aSpeed, aRating, section.chassis);
                    }
                    cp.chassis.add(aChassis);
                    points.add(i, cp);
                    return;
                }
                else if (aSpeed == section.speed) {
                    section.chassis.add(aChassis);
                    return;
                }
            }
            DataPoint cp = new DataPoint(aSpeed, aRating);
            cp.chassis.add(aChassis);
            points.add(cp);
        }

        void addChassis(Chassis aChassis, Efficiencies aEfficiencies) {
            List<Modifier> modifiers = aEfficiencies.getModifiers();
            if (aChassis instanceof ChassisStandard) {
                ChassisStandard c = (ChassisStandard) aChassis;
                modifiers.addAll(c.getQuirks());

                for (int r = c.getEngineMin(); r <= c.getEngineMax(); r += 5) {
                    double speed = TopSpeed.calculate(r, c.getMovementProfileBase(), c.getMassMax(), modifiers);
                    addSpeed(speed, aChassis, r);
                }
            }
            else if (aChassis instanceof ChassisOmniMech) {
                ChassisOmniMech c = (ChassisOmniMech) aChassis;
                int r = c.getFixedEngine().getRating();
                int mass = c.getMassMax();
                double minSpeed = TopSpeed.calculate(r, c.getMovementProfileMin(), mass, modifiers);
                double maxSpeed = TopSpeed.calculate(r, c.getMovementProfileMax(), mass, modifiers);
                addSpeed(minSpeed, aChassis, r);
                if (maxSpeed != minSpeed) {
                    addSpeed(maxSpeed, aChassis, r);
                }
            }
            else {
                throw new IllegalArgumentException();
            }
        }
    }

    /**
     * This class models a data point on such a shared curve.
     * 
     * @author Li Song
     */
    public static class DataPoint {
        final public double speed;
        final public int rating;
        final public List<Chassis> chassis = new ArrayList<>();

        public DataPoint(double aSpeed, int aRating) {
            speed = aSpeed;
            rating = aRating;
        }

        public DataPoint(double aSpeed, int aRating, Collection<Chassis> aEntries) {
            this(aSpeed, aRating);
            chassis.addAll(aEntries);
        }

        @Override
        public String toString() {
            return Double.toString(speed);
        }
    }

    private final List<Chassis> chassisList = new ArrayList<>();
    private final Chassis representant;
    private final PayloadStatistics payloadStatistics;

    /**
     * Creates a new payload grouping based on the given representative chassis and payload calculation class.
     * 
     * @param aRepresentant
     *            The representative {@link Chassis}.
     * @param aPayloadStatistics
     *            The payload calculation strategy.
     */
    public PayloadGrouping(Chassis aRepresentant, PayloadStatistics aPayloadStatistics) {
        representant = aRepresentant;
        payloadStatistics = aPayloadStatistics;
        offer(representant);
    }

    /**
     * Offer the given {@link Chassis} to this grouping.
     * 
     * @param aChassis
     *            The {@link Chassis} to offer.
     * @return <code>true</code> if the {@link Chassis} was added to this group.
     */
    public boolean offer(Chassis aChassis) {
        if (aChassis.getClass() != representant.getClass()) {
            return false;
        }

        if (aChassis instanceof ChassisStandard) {
            ChassisStandard chassisStd = (ChassisStandard) aChassis;
            ChassisStandard chassisRep = (ChassisStandard) representant;

            if (chassisRep.getMassMax() == chassisStd.getMassMax() && // Must have same mass
                    getSpeedFactor(chassisRep) == getSpeedFactor(chassisStd)) { // Must have same speed factor
                chassisList.add(chassisStd);
                return true;
            }
        }
        else if (aChassis instanceof ChassisOmniMech) {
            ChassisOmniMech chassisOmni = (ChassisOmniMech) aChassis;
            ChassisOmniMech chassisRep = (ChassisOmniMech) representant;
            // Fixed items are considered a part of the payload, they're just forced upon you.
            // chassisRep.getFixedMass() == chassisOmni.getFixedMass()
            if (chassisRep.getMassMax() == chassisOmni.getMassMax() && // Must have same mass
                    chassisRep.getFixedEngine() == chassisOmni.getFixedEngine() && // Must have same engine
                    getSpeedFactor(chassisRep) == getSpeedFactor(chassisOmni)) {// Must have same speed factor
                chassisList.add(chassisOmni);
                return true;
            }
        }
        else {
            throw new IllegalArgumentException("Unknown chassis type!");
        }
        return false;
    }

    public void addToGraph(Efficiencies aEfficiencies, XYChart<Double, Double> aChart) {
        Curve curve = new Curve(chassisList, aEfficiencies);

        XYChart.Series<Double, Double> series = null;

        DataPoint previousPoint = null;
        double previousPayload = 0.0;
        for (DataPoint point : curve.points) {
            final double payload = getPayLoad(point.chassis.get(0), point.rating);
            if (payload < 0)
                continue;

            if (series == null) {
                series = new XYChart.Series<>();
                series.setName(makeGroupName(point.chassis));
            }

            if (previousPoint != null && !ListArrayUtils.equalsUnordered(previousPoint.chassis, point.chassis)) {
                aChart.getData().add(series);
                series = new XYChart.Series<>();
                series.setName(makeGroupName(point.chassis));
                series.getData().add(new XYChart.Data<>(previousPoint.speed, previousPayload));
            }

            series.getData().add(new XYChart.Data<>(point.speed, payload));
            previousPayload = payload;
            previousPoint = point;
        }
        aChart.getData().add(series);
    }

    /**
     * Calculate the payload for a given chassis and assumed engine rating.
     * 
     * @param aChassis
     *            The {@link Chassis} to calculate the payload for.
     * @param aRating
     *            The engine rating to use (only makes sense for standard chassis).
     * @return The payload for the chassis.
     */
    private double getPayLoad(Chassis aChassis, int aRating) {
        try {
            if (aChassis instanceof ChassisStandard) {
                return payloadStatistics.calculate((ChassisStandard) aChassis, aRating);
            }
            return payloadStatistics.calculate((ChassisOmniMech) aChassis);
        }
        catch (Throwable t) {
            return -1.0; // Cannot carry any payload if no engine exists.
        }
    }

    /**
     * Calculates the speed factor of a given chassis taking fixed quirks into account.
     * 
     * @param aChassis
     *            The chassis to calculate for.
     * @return The maximal speed factor for the chassis.
     */
    private double getSpeedFactor(Chassis aChassis) {
        if (aChassis instanceof ChassisStandard) {
            ChassisStandard chassisStandard = (ChassisStandard) aChassis;
            return aChassis.getMovementProfileBase().getSpeedFactor(chassisStandard.getQuirks());
        }
        else if (aChassis instanceof ChassisOmniMech) {
            ChassisOmniMech omniMech = (ChassisOmniMech) aChassis;
            return omniMech.getMovementProfileMax().getSpeedFactor(null);
        }
        throw new IllegalArgumentException("Unknown chassis type!");
    }

    /**
     * Generates a string that describes the given group of {@link Chassis}.
     * 
     * @param aChassisGroup
     *            The group to describe
     * 
     * @return A {@link String} with the name.
     */
    private String makeGroupName(Collection<Chassis> aChassisGroup) {
        Map<String, List<Chassis>> bySeries = new HashMap<>();
        for (Chassis chassisX : aChassisGroup) {
            List<Chassis> series = bySeries.get(chassisX.getSeriesName());
            if (series == null) {
                series = new ArrayList<>();
                bySeries.put(chassisX.getSeriesName(), series);
            }
            series.add(chassisX);
        }

        StringBuilder sb = new StringBuilder();
        boolean firstSeries = true;
        for (Entry<String, List<Chassis>> series : bySeries.entrySet()) {
            if (!firstSeries)
                sb.append(", ");
            firstSeries = false;
            List<Chassis> allInSeries = new ArrayList<>();
            for (Chassis cb : ChassisDB.lookupSeries(series.getKey())) {
                if (!cb.getVariantType().isVariation()) {
                    allInSeries.add(cb);
                }
            }

            if (series.getValue().containsAll(allInSeries)) {
                sb.append(series.getValue().get(0).getNameShort().split("-")[0]).append("-*");
            }
            else {
                sb.append(series.getValue().get(0).getNameShort().split("-")[0]).append(" (");
                boolean first = true;
                for (Chassis e : series.getValue()) {
                    if (!first)
                        sb.append(", ");
                    first = false;
                    sb.append(e.getNameShort().split("-")[1]);
                }
                sb.append(")");
            }
        }
        return sb.toString();
    }
}
