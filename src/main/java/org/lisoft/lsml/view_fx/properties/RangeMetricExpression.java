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
package org.lisoft.lsml.view_fx.properties;

import java.util.function.Predicate;

import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReception;
import org.lisoft.lsml.model.metrics.RangeMetric;

import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;

/**
 * This class wraps a {@link RangeMetric} in an {@link DoubleExpression } so that it may easily be used in JavaFX.
 *
 * @author Li Song
 * @param <T>
 *            The {@link RangeMetric} to wrap
 *
 */
public class RangeMetricExpression<T extends RangeMetric> extends MetricExpression<T> {

    /**
     * The {@link RangeMetric} has two range attributes. One is the user set range which can be a fixed value or
     * "optimal" to let the metric choose the best range. The other is the actual range
     * ({@link RangeMetric#getDisplayRange()}) used for the computations which may be different from the user set range
     * ({@link RangeMetric#getUserRange()}) if "optimal" is chosen.
     *
     * The value of {@link RangeMetric#getDisplayRange()} is only available after a call to
     * {@link RangeMetric#calculate()} has been made after the range was changed. And may change after each subsequent
     * call to calculate as equipment changes may affect the optimal range.
     *
     * So the displayed range property may change each time the value property of the metric changes or when the range
     * is changed manually (which may sometimes, but not always, induce a change in the value property).
     *
     * @author Li Song
     */
    public class RangeProperty extends DoublePropertyBase {
        private boolean isValid = true;
        private double prevDisplayRange = getMetric().getDisplayRange();

        @Override
        public double get() {
            if (!isValid) {
                prevDisplayRange = getMetric().getDisplayRange();
                isValid = true;
            }
            return prevDisplayRange;
        }

        @Override
        public Object getBean() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void set(double aDesiredValue) {
            if (getMetric().getUserRange() == aDesiredValue) {
                // Metric was last computed with the desired value, this is a NO-OP.
                return;
            }

            // This change may result in a change to the metric value.
            // We need to invalidate the MetricExpression and then force it to evaluate
            // so that getCurrentRange() will be up to date.
            getMetric().setUserRange(aDesiredValue);
            RangeMetricExpression.this.invalidate();
            RangeMetricExpression.this.get(); // Will call computeValue() further down
        }

        private void displayRangeHasChanged() {
            if (isValid) {
                isValid = false;
                invalidate();
                fireValueChangedEvent();
            }
        }
    }

    private final RangeProperty range = new RangeProperty();

    public RangeMetricExpression(MessageReception aMessageReception, T aMetric, Predicate<Message> aFilter) {
        super(aMessageReception, aMetric, aFilter);
    }

    public DoubleProperty rangeProperty() {
        return range;
    }

    public void setRange(double aRange) {
        range.set(aRange);
    }

    @Override
    protected double computeValue() {
        final double val = super.computeValue();

        // The metric value may be recomputed for a number of reasons. However the range
        // doesn't necessarily update for all re-computations.
        if (range.prevDisplayRange != getMetric().getDisplayRange()) {
            range.displayRangeHasChanged();
        }
        return val;
    }
}
