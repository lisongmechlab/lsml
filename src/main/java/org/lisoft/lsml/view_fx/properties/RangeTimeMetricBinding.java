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

import org.lisoft.lsml.messages.LoadoutMessage;
import org.lisoft.lsml.messages.LoadoutMessage.Type;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.metrics.RangeMetric;
import org.lisoft.lsml.model.metrics.RangeTimeMetric;

import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * This class wraps a {@link RangeMetric} in an {@link DoubleExpression } so that it may easily be used in JavaFX.
 *
 * @author Li Song
 * @param <T>
 *            The {@link RangeMetric} to wrap
 *
 */
public class RangeTimeMetricBinding<T extends RangeTimeMetric> extends RangeMetricBinding<T> {
    private final SimpleDoubleProperty time;

    public RangeTimeMetricBinding(MessageXBar aXBar, T aMetric, Predicate<Message> aFilter) {
        super(aXBar, aMetric, aFilter);

        time = new SimpleDoubleProperty(aMetric.getTime()) {
            @Override
            protected void invalidated() {
                aMetric.changeTime(get());
                RangeTimeMetricBinding.this.invalidate();
                super.invalidated();

                // Force the metrics (and everything else) to update.
                aXBar.post(new LoadoutMessage(null, Type.UPDATE));
            }
        };
    }

    public void setTime(double aTime) {
        time.set(aTime);
    }

    public DoubleProperty timeProperty() {
        return time;
    }
}
