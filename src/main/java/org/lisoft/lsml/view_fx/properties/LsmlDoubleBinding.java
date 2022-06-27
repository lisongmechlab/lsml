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

import javafx.beans.binding.DoubleBinding;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageReception;
import org.lisoft.lsml.view_fx.LiSongMechLab;

import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * This binding will bind to an arbitrary attribute of a loadout and provide automatic updating.
 *
 * @author Li Song
 */
public class LsmlDoubleBinding extends DoubleBinding implements MessageReceiver {
    private final Predicate<Message> invalidationFilter;
    private final Callable<Double> valueFunction;

    public LsmlDoubleBinding(MessageReception aMessageReception, Callable<Double> aValueFunction,
                             Predicate<Message> aInvalidationFilter) {
        aMessageReception.attach(this);
        valueFunction = aValueFunction;
        invalidationFilter = aInvalidationFilter;
    }

    @Override
    public void receive(Message aMsg) {
        try {
            if (invalidationFilter.test(aMsg) == true) {
                invalidate();
            }
        } catch (Exception e) {
            LiSongMechLab.showError(null, e);
        }
    }

    @Override
    protected double computeValue() {
        try {
            return valueFunction.call();
        } catch (Exception e) {
            LiSongMechLab.showError(null, e);
        }
        return 0.0;
    }
}
