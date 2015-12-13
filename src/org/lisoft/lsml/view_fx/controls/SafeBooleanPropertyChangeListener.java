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
package org.lisoft.lsml.view_fx.controls;

import org.lisoft.lsml.view_fx.LiSongMechLab;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

class SafeBooleanPropertyChangeListener implements ChangeListener<Boolean> {

    @FunctionalInterface
    public static interface ChangeAction {
        void call(boolean aNewValue) throws Exception;
    }

    SafeBooleanPropertyChangeListener.ChangeAction runnable;

    public SafeBooleanPropertyChangeListener(SafeBooleanPropertyChangeListener.ChangeAction aRunnable) {
        runnable = aRunnable;
    }

    @Override
    public void changed(ObservableValue<? extends Boolean> aObservable, Boolean aOldValue, Boolean aNewValue) {
        try {
            runnable.call(aNewValue);
        }
        catch (Exception e) {
            ((BooleanProperty) aObservable).setValue(aOldValue);
            LiSongMechLab.showError(e);
        }
    }
}