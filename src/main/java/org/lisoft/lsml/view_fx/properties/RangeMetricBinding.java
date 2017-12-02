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
package org.lisoft.lsml.view_fx.properties;

import java.util.function.Predicate;

import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReception;
import org.lisoft.lsml.model.metrics.RangeMetric;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;

/**
 * This class wraps a {@link RangeMetric} in an {@link DoubleExpression } so that it may easily be used in JavaFX.
 *
 * @author Emily Björk
 * @param <T>
 *            The {@link RangeMetric} to wrap
 *
 */
public class RangeMetricBinding<T extends RangeMetric> extends MetricBinding<T> {
    private final SimpleDoubleProperty userRange;
    private final DoubleBinding displayRange;

    public RangeMetricBinding(MessageReception aMessageReception, T aMetric, Predicate<Message> aFilter) {
        super(aMessageReception, aMetric, aFilter);

        userRange = new SimpleDoubleProperty(aMetric.getUserRange()) {
            @Override
            protected void invalidated() {
                getMetric().setUserRange(get());
                RangeMetricBinding.this.invalidate();
                super.invalidated();
            }
        };

        displayRange = new DoubleBinding() {
            {
                bind(userRange);
            }

            @Override
            protected double computeValue() {
                return getMetric().getDisplayRange();
            }
        };
    }

    public ObservableDoubleValue displayRange() {
        return displayRange;
    }

    public void setUserRange(double aRange) {
        userRange.set(aRange);
    }

    public DoubleProperty userRangeProperty() {
        return userRange;
    }

    @Override
    protected double computeValue() {
        final double val = super.computeValue();

        // The metric value may be recomputed for a number of reasons. However the range
        // doesn't necessarily update for all re-computations.
        if (displayRange.get() != getMetric().getDisplayRange()) {
            displayRange.invalidate();
        }
        return val;
    }

}
