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

import java.awt.Toolkit;
import java.util.function.Predicate;

import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageReception;
import org.lisoft.lsml.view_fx.LiSongMechLab;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * 
 * @author Emily Björk
 *
 */
public class LsmlPropertyImpl<T> implements MessageReceiver {

    @FunctionalInterface
    interface QuietSettable<T> {
        void call(T aNewValue);
    }

    @FunctionalInterface
    interface ReadOpeartion<T> {
        T call();
    }

    @FunctionalInterface
    interface ValidatedWriteOpeartion<T> {
        boolean call(T aNewValue) throws Exception;
    }

    private final Predicate<Message>         messageFilter;
    private final ValidatedWriteOpeartion<T> writeOperation;
    private final ReadOpeartion<T>           readOperation;
    private final ChangeListener<T>          changeListener;
    private final QuietSettable<T>           settable;
    private boolean                          squelch = false;

    public LsmlPropertyImpl(MessageReception aMessageReception, ReadOpeartion<T> aReadOp,
            ValidatedWriteOpeartion<T> aWriteOp, Predicate<Message> aMessageFilter, QuietSettable<T> aSettable) {
        aMessageReception.attach(this);
        readOperation = aReadOp;
        writeOperation = aWriteOp;
        messageFilter = aMessageFilter;
        settable = aSettable;

        changeListener = new ChangeListener<T>() {
            @Override
            public void changed(ObservableValue<? extends T> aObservable, T aOldValue, T aNewValue) {
                if (squelch)
                    return;
                try {
                    if (!writeOperation.call(aNewValue)) {
                        settable.call(aOldValue);
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
                catch (Exception e) {
                    settable.call(aOldValue);
                    LiSongMechLab.showError(e);
                }
            }
        };
    }

    @Override
    public void receive(Message aMsg) {
        if (messageFilter.test(aMsg)) {
            settable.call(readOperation.call());
        }
    }

}
