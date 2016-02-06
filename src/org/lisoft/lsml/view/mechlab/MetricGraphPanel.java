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
package org.lisoft.lsml.view.mechlab;

import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.lisoft.lsml.model.metrics.VariableMetric;

/**
 * This class creates a {@link ChartPanel} that visualizes a {@link VariableMetric}.
 * 
 * @author Li Song
 *
 */
public class MetricGraphPanel extends ChartPanel {

    private final VariableMetric metric;
    private final String         title;

    public MetricGraphPanel(String aTitle, VariableMetric aMetric) {
        super(ChartFactory.createXYLineChart(aTitle, aMetric.getArgumentName(), aMetric.getMetricName(),
                new XYSeriesCollection(), PlotOrientation.VERTICAL, false, false, false));
        metric = aMetric;
        title = aTitle;

        // Set these to some low value to prevent the diagram from being non-uniformly scaled to fit.
        setMinimumDrawHeight(100);
        setMinimumDrawWidth(100);

        update();
    }

    public void update() {
        XYSeriesCollection seriesCollection = new XYSeriesCollection();
        XYSeries series = new XYSeries("", true, false);

        List<Double> values = metric.getArgumentValues();
        for (Double value : values) {
            series.add(value.doubleValue(), metric.calculate(value));
        }

        seriesCollection.addSeries(series);

        JFreeChart chart = ChartFactory.createXYLineChart(title, metric.getArgumentName(), metric.getMetricName(),
                seriesCollection, PlotOrientation.VERTICAL, false, false, false);

        StyleManager.styleSmallGraph(chart, getBackground());
        setChart(chart);
    }
}
