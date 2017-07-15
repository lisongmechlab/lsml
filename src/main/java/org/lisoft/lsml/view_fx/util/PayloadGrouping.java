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
package org.lisoft.lsml.view_fx.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisOmniMech;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.database.ChassisDB;
import org.lisoft.lsml.model.metrics.PayloadStatistics;
import org.lisoft.lsml.model.metrics.TopSpeed;
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

        public Curve(Collection<Chassis> aChassisGroup) {
            for (final Chassis entry : aChassisGroup) {
                addChassis(entry);
            }
        }

        void addChassis(Chassis aChassis) {
            if (aChassis instanceof ChassisStandard) {
                final ChassisStandard c = (ChassisStandard) aChassis;

                for (int r = c.getEngineMin(); r <= c.getEngineMax(); r += 5) {
                    final double speed = TopSpeed.calculate(r, c.getMovementProfileBase(), c.getMassMax(), null);
                    addSpeed(speed, aChassis, r);
                }
            }
            else if (aChassis instanceof ChassisOmniMech) {
                final ChassisOmniMech c = (ChassisOmniMech) aChassis;
                final int r = c.getFixedEngine().getRating();
                final int mass = c.getMassMax();
                final double minSpeed = TopSpeed.calculate(r, c.getMovementProfileMin(), mass, null);
                final double maxSpeed = TopSpeed.calculate(r, c.getMovementProfileMax(), mass, null);
                addSpeed(minSpeed, aChassis, r);
                if (maxSpeed != minSpeed) {
                    addSpeed(maxSpeed, aChassis, r);
                }
            }
            else {
                throw new IllegalArgumentException();
            }
        }

        void addSpeed(double aSpeed, Chassis aChassis, int aRating) {
            for (int i = 0; i < points.size(); ++i) {
                final DataPoint section = points.get(i);
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
            final DataPoint cp = new DataPoint(aSpeed, aRating);
            cp.chassis.add(aChassis);
            points.add(cp);
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

    public void addToGraph(XYChart<Double, Double> aChart) {
        final Curve curve = new Curve(chassisList);

        XYChart.Series<Double, Double> series = null;

        DataPoint previousPoint = null;
        double previousPayload = 0.0;
        for (final DataPoint point : curve.points) {
            final double payload = getPayLoad(point.chassis.get(0), point.rating);
            if (payload < 0) {
                continue;
            }

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
            final ChassisStandard chassisStd = (ChassisStandard) aChassis;
            final ChassisStandard chassisRep = (ChassisStandard) representant;

            if (chassisRep.getMassMax() == chassisStd.getMassMax() && // Must have same mass
                    getSpeedFactor(chassisRep) == getSpeedFactor(chassisStd) && // Must have same speed factor
                    (!payloadStatistics.isFerroFibrous() || chassisRep.getFaction() == chassisStd.getFaction())) {
                chassisList.add(chassisStd);
                return true;
            }
        }
        else if (aChassis instanceof ChassisOmniMech) {
            final ChassisOmniMech chassisOmni = (ChassisOmniMech) aChassis;
            final ChassisOmniMech chassisRep = (ChassisOmniMech) representant;
            // Fixed items are considered a part of the payload, they're just forced upon you.
            // chassisRep.getFixedMass() == chassisOmni.getFixedMass()
            if (chassisRep.getMassMax() == chassisOmni.getMassMax() && // Must have same mass
                    chassisRep.getFixedEngine() == chassisOmni.getFixedEngine() && // Must have same engine
                    getSpeedFactor(chassisRep) == getSpeedFactor(chassisOmni) && // Must have same speed factor
                    chassisRep.getFaction() == chassisOmni.getFaction()) { // Must have same faction
                chassisList.add(chassisOmni);
                return true;
            }
        }
        else {
            throw new IllegalArgumentException("Unknown chassis type!");
        }
        return false;
    }

    private List<Chassis> allChassisOfSeries(Entry<String, List<Chassis>> series) {
        final List<Chassis> allInSeries = new ArrayList<>();
        for (final Chassis cb : ChassisDB.lookupSeries(series.getKey())) {
            if (!cb.getVariantType().isVariation()) {
                allInSeries.add(cb);
            }
        }
        return allInSeries;
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
        catch (final Throwable t) {
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
            final ChassisStandard chassisStandard = (ChassisStandard) aChassis;
            return aChassis.getMovementProfileBase().getSpeedFactor(chassisStandard.getQuirks());
        }
        else if (aChassis instanceof ChassisOmniMech) {
            final ChassisOmniMech omniMech = (ChassisOmniMech) aChassis;
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
        final Map<String, List<Chassis>> bySeries = new HashMap<>();
        for (final Chassis chassisX : aChassisGroup) {
            List<Chassis> series = bySeries.get(chassisX.getSeriesName());
            if (series == null) {
                series = new ArrayList<>();
                bySeries.put(chassisX.getSeriesName(), series);
            }
            series.add(chassisX);
        }

        final StringBuilder sb = new StringBuilder();
        boolean firstSeries = true;
        for (final Entry<String, List<Chassis>> series : bySeries.entrySet()) {
            if (!firstSeries) {
                sb.append(", ");
            }
            firstSeries = false;

            final List<Chassis> allInSeries = allChassisOfSeries(series);

            String seriesName = allInSeries.get(0).getNameShort();
            for (final Chassis chassis : allInSeries) {
                int i = 0;
                final String name = chassis.getNameShort();
                while (i < seriesName.length() && i < name.length() && seriesName.charAt(i) == name.charAt(i)) {
                    i++;
                }
                seriesName = seriesName.substring(0, i);
            }

            if (seriesName.endsWith("-")) {
                seriesName = seriesName.substring(0, seriesName.length() - 1);
            }

            if (series.getValue().containsAll(allInSeries)) {
                sb.append(seriesName).append("-*");
            }
            else {
                sb.append(seriesName).append(" (");
                boolean first = true;
                for (final Chassis e : series.getValue()) {
                    if (!first) {
                        sb.append(", ");
                    }
                    first = false;
                    sb.append(e.getNameShort().substring(e.getNameShort().lastIndexOf('-'), e.getNameShort().length()));
                }
                sb.append(")");
            }
        }
        return sb.toString();
    }
}
