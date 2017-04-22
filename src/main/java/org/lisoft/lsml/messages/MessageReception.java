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
package org.lisoft.lsml.messages;

import java.lang.ref.WeakReference;

/**
 * This interface specifies an API for registering for messages.
 * 
 * @author Emily Björk
 */
public interface MessageReception {

    /**
     * Attaches a new {@link MessageReceiver} to this {@link MessageXBar}. The {@link MessageReceiver} is automatically
     * converted to a weak reference.
     * 
     * @see #attach(MessageReceiver)
     * @param aReader
     *            The {@link MessageReceiver} to add.
     */
    void attach(MessageReceiver aReader);

    /**
     * Attaches a new {@link MessageReceiver} to this {@link MessageXBar}. The {@link MessageXBar} only keeps weak
     * references so this won't prevent objects from being garbage collected.
     * 
     * @param aWeakReference
     *            The object that shall receive messages.
     */
    void attach(WeakReference<MessageReceiver> aWeakReference);

    /**
     * Detaches a {@link MessageReceiver} from the {@link MessageXBar}.
     * 
     * @param aReader
     *            The object that shall be removed messages.
     */
    void detach(MessageReceiver aReader);

}
