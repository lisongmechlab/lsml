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

import javafx.beans.property.SimpleIntegerProperty;

/**
 * @author Emily Björk
 *
 */
public class LsmlIntegerProperty extends SimpleIntegerProperty implements MessageReceiver {

    @FunctionalInterface
    static interface ReadOpeartion {
        int call();
    }

    @FunctionalInterface
    static interface ValidatedWriteOpeartion {
        boolean call(Number aNewValue) throws Exception;
    }

    private final Predicate<Message>      messageFilter;
    private final ValidatedWriteOpeartion writeOperation;
    private final ReadOpeartion           readOperation;
    private boolean                       squelch = false;

    LsmlIntegerProperty(MessageReception aMessageReception, ReadOpeartion aReadOp, ValidatedWriteOpeartion aWriteOp,
            Predicate<Message> aMessageFilter) {
        aMessageReception.attach(this);
        readOperation = aReadOp;
        writeOperation = aWriteOp;
        messageFilter = aMessageFilter;

        quietSet(aReadOp.call());

        addListener((aObservable, aOldValue, aNewValue) -> {
            if (squelch)
                return;
            try {
                if (!writeOperation.call(aNewValue)) {
                    quietSet(aOldValue);
                    Toolkit.getDefaultToolkit().beep();
                }
            }
            catch (Exception e) {
                quietSet(aOldValue);
                LiSongMechLab.showError(e);
            }
        });

    }

    @Override
    public int get() {
        return readOperation.call();
    }

    private void quietSet(Number aValue) {
        squelch = true;
        setValue(aValue);
        squelch = false;
    }

    @Override
    public void receive(Message aMsg) {
        if (messageFilter.test(aMsg)) {
            quietSet(get());
        }
    }
}
