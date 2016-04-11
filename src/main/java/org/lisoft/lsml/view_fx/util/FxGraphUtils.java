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
package org.lisoft.lsml.view_fx.util;

import javafx.collections.ObservableList;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;

/**
 * This class contains utility functions for dealing with graphs in JavaFX.
 * 
 * @author Emily Björk
 */
public class FxGraphUtils {

    /**
     * Sets up axis bounds by quantising continuous upper and lower bounds.
     * 
     * @param aAxis
     *            The {@link Axis} to set the bounds on.
     * @param aLB
     *            The (non-quantised) lower bound.
     * @param aUB
     *            The (non-quantised) upper bound.
     * @param aStep
     *            The quantisation step.
     */
    public static void setAxisBound(Axis<? extends Number> aAxis, double aLB, double aUB, double aStep) {
        double lb = Math.floor(aLB / aStep) * aStep;
        double ub = Math.ceil(aUB / aStep) * aStep;

        NumberAxis numberAxis = (NumberAxis) aAxis;
        numberAxis.setAutoRanging(false);
        numberAxis.setLowerBound(lb);
        numberAxis.setUpperBound(ub);
        numberAxis.setTickUnit(aStep);
    }

    /**
     * Will setup the X and Y {@link Axis} to be tight around the data given by <code>aData</code>. The axis values are
     * quantised as by {@link #setAxisBound(Axis, double, double, double)} with the respective X and Y step.
     * 
     * @param aXAxis
     *            The X axis to set the bounds on.
     * @param aYAxis
     *            The Y axis to set the bounds on.
     * @param aXStep
     *            The X axis quantisation step.
     * @param aYStep
     *            The Y axis quantisation step.
     * @param aData
     *            The data to calculate the tight bounds from.
     */
    public static void setTightBounds(Axis<? extends Number> aXAxis, Axis<? extends Number> aYAxis, double aXStep,
            double aYStep, ObservableList<Series<Double, Double>> aData) {

        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (Series<Double, Double> series : aData) {
            for (Data<Double, Double> point : series.getData()) {
                minX = Math.min(minX, point.getXValue().doubleValue());
                maxX = Math.max(maxX, point.getXValue().doubleValue());
                minY = Math.min(minY, point.getYValue().doubleValue());
                maxY = Math.max(maxY, point.getYValue().doubleValue());
            }
        }

        setAxisBound(aXAxis, minX, maxX, aXStep);
        setAxisBound(aYAxis, minY, maxY, aYStep);
    }

}
