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

import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoublePropertyBase;

/**
 * This class wraps a {@link RangeMetric} in an {@link DoubleExpression } so that it may easily be used in JavaFX.
 *
 * @author Emily Björk
 * @param <T>
 *            The {@link RangeMetric} to wrap
 *
 */
public class RangeMetricExpression<T extends RangeMetric> extends MetricExpression<T> {

    public class RangeProperty extends ReadOnlyDoublePropertyBase {
        @Override
        public void fireValueChangedEvent() {
            super.fireValueChangedEvent();
        }

        @Override
        public double get() {
            return getMetric().getCurrentRange();
        }

        @Override
        public Object getBean() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }
    }

    private final RangeProperty range = new RangeProperty();

    public RangeMetricExpression(MessageReception aMessageReception, T aMetric, Predicate<Message> aFilter) {
        super(aMessageReception, aMetric, aFilter);
    }

    public ReadOnlyDoubleProperty rangeProperty() {
        return range;
    }

    public void setRange(Double aRange) {
        getMetric().setRange(aRange);
        setDirty();
    }

    @Override
    protected void computeValue() {
        super.computeValue();
        range.fireValueChangedEvent();
    }
}
