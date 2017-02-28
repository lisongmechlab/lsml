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
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageReception;
import org.lisoft.lsml.model.metrics.Metric;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.DoubleExpression;

/**
 * This class wraps a {@link Metric} in an {@link DoubleExpression} so that it may easily be used in JavaFX.
 *
 * @author Emily Björk
 * @param <T>
 *            The metric type to create an expression for.
 */
public class MetricExpression<T extends Metric> extends DoubleBinding implements MessageReceiver {
    private final T metric;
    private final Predicate<Message> filter;

    /**
     * Creates a new {@link MetricExpression}.
     *
     * @param aMessageReception
     *            The {@link MessageReception} to listen to messages on.
     * @param aMetric
     *            The {@link Metric} to wrap.
     * @param aFilter
     *            A {@link Predicate} which returns true if the given message might have affected the {@link Metric}.
     *
     */
    public MetricExpression(MessageReception aMessageReception, T aMetric, Predicate<Message> aFilter) {
        aMessageReception.attach(this);
        metric = aMetric;
        filter = aFilter;
    }

    /**
     * @return the metric
     */
    public T getMetric() {
        return metric;
    }

    @Override
    public void receive(Message aMsg) {
        if (isValid() && filter.test(aMsg)) {
            invalidate();
        }
    }

    @Override
    protected double computeValue() {
        return metric.calculate();
    }
}
