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
package org.lisoft.lsml.view.graphs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.lisoft.lsml.model.chassi.ChassisBase;
import org.lisoft.lsml.model.chassi.ChassisOmniMech;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.metrics.PayloadStatistics;
import org.lisoft.lsml.model.metrics.TopSpeed;
import org.lisoft.lsml.model.modifiers.Efficiencies;
import org.lisoft.lsml.model.modifiers.MechEfficiencyType;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;
import org.lisoft.lsml.util.ListArrayUtils;
import org.lisoft.lsml.view.graphs.PayloadGraphPanel.TonnageCurve.CurvePoint;

/**
 * Will draw a payload over speed graph for selected chassis.
 * 
 * @author Li Song
 */
public class PayloadGraphPanel extends ChartPanel {
    public static class TonnageCurve {
        public static class CurvePoint {
            final double            speed;
            final int               rating;
            final List<ChassisBase> chassis = new ArrayList<>();

            public CurvePoint(double aSpeed, int aRating) {
                speed = aSpeed;
                rating = aRating;
            }

            public CurvePoint(double aSpeed, int aRating, Collection<ChassisBase> aEntries) {
                this(aSpeed, aRating);
                chassis.addAll(aEntries);
            }

            @Override
            public String toString() {
                return Double.toString(speed);
            }

        }

        private final List<CurvePoint> points = new ArrayList<>();

        public TonnageCurve(Collection<ChassisBase> aChassisGroup, Efficiencies aEfficiencies) {
            for (ChassisBase entry : aChassisGroup) {
                addChassis(entry, aEfficiencies);
            }
        }

        void addSpeed(double aSpeed, ChassisBase aChassis, int aRating) {
            for (int i = 0; i < points.size(); ++i) {
                CurvePoint section = points.get(i);
                if (aSpeed < section.speed) {
                    CurvePoint cp = new CurvePoint(aSpeed, aRating);
                    if (i == 0) {
                        cp = new CurvePoint(aSpeed, aRating);
                    }
                    else {
                        cp = new CurvePoint(aSpeed, aRating, section.chassis);
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
            CurvePoint cp = new CurvePoint(aSpeed, aRating);
            cp.chassis.add(aChassis);
            points.add(cp);
        }

        void addChassis(ChassisBase aChassis, Efficiencies aEffs) {
            if (aChassis instanceof ChassisStandard) {
                ChassisStandard c = (ChassisStandard) aChassis;
                List<Modifier> modifiers = new ArrayList<>(c.getQuirks());
                modifiers.addAll(aEffs.getModifiers());

                for (int r = c.getEngineMin(); r <= c.getEngineMax(); r += 5) {
                    double speed = TopSpeed.calculate(r, c.getMovementProfileBase(), c.getMassMax(), modifiers);
                    addSpeed(speed, aChassis, r);
                }
            }
            else if (aChassis instanceof ChassisOmniMech) {
                ChassisOmniMech c = (ChassisOmniMech) aChassis;
                int r = c.getFixedEngine().getRating();
                double minSpeed = TopSpeed.calculate(r, c.getMovementProfileMin(), c.getMassMax(),
                        aEffs.getModifiers());
                double maxSpeed = TopSpeed.calculate(r, c.getMovementProfileMax(), c.getMassMax(),
                        aEffs.getModifiers());
                addSpeed(minSpeed, aChassis, r);
                addSpeed(maxSpeed, aChassis, r);
            }
            else {
                throw new IllegalArgumentException();
            }
        }
    }

    private final PayloadStatistics             payloadStatistics;
    private final Efficiencies                  efficiencies = new Efficiencies();
    private Collection<Collection<ChassisBase>> chassisGroups;

    public PayloadGraphPanel(PayloadStatistics aPayloadStatistics, final JCheckBox aSpeedTweak) {
        super(makeChart(new DefaultTableXYDataset()));
        aSpeedTweak.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent aArg0) {
                efficiencies.setEfficiency(MechEfficiencyType.SPEED_TWEAK, aSpeedTweak.isSelected(), null);
                updateGraph();
            }
        });
        payloadStatistics = aPayloadStatistics;
    }

    public void selectChassis(Collection<Collection<ChassisBase>> aChassisCollection) {
        chassisGroups = aChassisCollection;
    }

    String makeName(List<ChassisBase> aEntries) {
        Map<String, List<ChassisBase>> bySeries = new HashMap<>();
        for (ChassisBase chassisX : aEntries) {
            List<ChassisBase> series = bySeries.get(chassisX.getSeriesName());
            if (series == null) {
                series = new ArrayList<>();
                bySeries.put(chassisX.getSeriesName(), series);
            }
            series.add(chassisX);
        }

        StringBuilder sb = new StringBuilder();
        boolean firstSeries = true;
        for (java.util.Map.Entry<String, List<ChassisBase>> series : bySeries.entrySet()) {
            if (!firstSeries)
                sb.append(", ");
            firstSeries = false;
            List<ChassisBase> allInSeries = new ArrayList<>();
            for (ChassisBase cb : ChassisDB.lookupSeries(series.getKey())) {
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
                for (ChassisBase e : series.getValue()) {
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

    private double getPayLoad(ChassisBase aRepresetant, int aRating) {
        try {
            if (aRepresetant instanceof ChassisStandard) {
                return payloadStatistics.calculate((ChassisStandard) aRepresetant, aRating);
            }
            return payloadStatistics.calculate((ChassisOmniMech) aRepresetant);
        }
        catch (Throwable t) {
            // Eat it, engine didn't exist.
            return -1.0; // Cannot carry any payload if no engine exists.
        }
    }

    private void makeSeriesFromGroup(Collection<ChassisBase> chassisGroup, DefaultTableXYDataset dataset) {
        TonnageCurve curve = new TonnageCurve(chassisGroup, efficiencies);

        XYSeries series = null;
        CurvePoint previousPoint = null;
        double previousPayload = 0.0;
        for (CurvePoint point : curve.points) {
            final double payload = getPayLoad(point.chassis.get(0), point.rating);
            if (payload < 0)
                continue;

            if (series == null) {
                series = new XYSeries(makeName(point.chassis), false, false);
            }

            if (previousPoint != null && !ListArrayUtils.equalsUnordered(previousPoint.chassis, point.chassis)) {
                dataset.addSeries(series);
                series = new XYSeries(makeName(point.chassis), false, false);
                series.add(previousPoint.speed, previousPayload);
            }

            series.add(point.speed, payload);
            previousPayload = payload;
            previousPoint = point;
        }
        dataset.addSeries(series);
    }

    public void updateGraph() {
        DefaultTableXYDataset dataset = new DefaultTableXYDataset();

        // Generate curves by group
        for (Collection<ChassisBase> chassisGroup : chassisGroups) {
            Map<Double, List<ChassisBase>> bySpeedQuirk = new HashMap<>();

            for (ChassisBase chassis : chassisGroup) {
                if (chassis instanceof ChassisStandard) {
                    ChassisStandard cs = (ChassisStandard) chassis;

                    double speedQuirkValue = 0.0;
                    for (Modifier modifier : cs.getQuirks()) {
                        if (modifier.getDescription().getSelectors()
                                .contains(ModifierDescription.SEL_MOVEMENT_MAX_SPEED)) {
                            speedQuirkValue = modifier.getValue();
                            break;
                        }
                    }
                    List<ChassisBase> group = bySpeedQuirk.get(speedQuirkValue);
                    if (null == group) {
                        group = new ArrayList<>();
                        bySpeedQuirk.put(speedQuirkValue, group);
                    }
                    group.add(cs);
                }
                else if (chassis instanceof ChassisOmniMech) {
                    List<ChassisBase> group = bySpeedQuirk.get(0.0);
                    if (null == group) {
                        group = new ArrayList<>();
                        bySpeedQuirk.put(0.0, group);
                    }
                    group.add(chassis);
                }
            }

            for (List<ChassisBase> group : bySpeedQuirk.values()) {
                makeSeriesFromGroup(group, dataset);
            }
        }

        setChart(makeChart(dataset));
        XYPlot plot = (XYPlot) getChart().getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
        renderer.setBaseShapesFilled(false);
        plot.setRenderer(renderer);
    }

    private static JFreeChart makeChart(XYDataset aDataset) {
        JFreeChart chart = ChartFactory.createXYLineChart("Comparing payload tonnage for given speeds", "km/h",
                "payload tons", aDataset, PlotOrientation.VERTICAL, true, false, false);

        return chart;
    }
}
