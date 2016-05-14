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
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageReception;
import org.lisoft.lsml.model.metrics.Metric;

import com.sun.javafx.binding.ExpressionHelper;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.value.ChangeListener;

/**
 * This class wraps a {@link Metric} in an {@link DoubleExpression} so that it may easily be used in JavaFX.
 *
 * @author Li Song
 * @param <T>
 */
public class MetricExpression<T extends Metric> extends DoubleExpression implements MessageReceiver {
    private boolean dirty = true;
    private double value;
    private ExpressionHelper<Number> helper = null;
    protected final T metric;
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

    @Override
    public void addListener(ChangeListener<? super Number> listener) {
        helper = ExpressionHelper.addListener(helper, this, listener);
    }

    @Override
    public void addListener(InvalidationListener listener) {
        helper = ExpressionHelper.addListener(helper, this, listener);
    }

    @Override
    public double get() {
        if (dirty) {
            dirty = false;
            computeValue();
        }
        return value;
    }

    /**
     * @return the metric
     */
    public T getMetric() {
        return metric;
    }

    @Override
    public void receive(Message aMsg) {
        if (!dirty && filter.test(aMsg)) {
            setDirty();
        }
    }

    @Override
    public void removeListener(ChangeListener<? super Number> listener) {
        helper = ExpressionHelper.removeListener(helper, listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        helper = ExpressionHelper.removeListener(helper, listener);
    }

    protected void computeValue() {
        final double newValue = metric.calculate();
        if (newValue != value) {
            ExpressionHelper.fireValueChangedEvent(helper);
            value = newValue;
        }
    }

    protected void setDirty() {
        dirty = true;
        ExpressionHelper.fireValueChangedEvent(helper);
    }
}
