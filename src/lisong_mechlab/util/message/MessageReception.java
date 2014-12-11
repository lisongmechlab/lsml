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
package lisong_mechlab.util.message;

import java.lang.ref.WeakReference;

import lisong_mechlab.util.message.Message.Recipient;

/**
 * This interface specifies an API for receiving messages sent over a crossbar.
 * 
 * @author Li Song
 *
 */
public interface MessageReception {

    /**
     * Attaches a new {@link Message.Recipient} to this {@link MessageXBar}. The {@link Message.Recipient} is
     * automatically converted to a weak reference.
     * 
     * @see #attach(Recipient)
     * @param aReader
     *            The {@link Message.Recipient} to add.
     */
    public void attach(Message.Recipient aReader);

    /**
     * Attaches a new {@link Message.Recipient} to this {@link MessageXBar}. The {@link MessageXBar} only keeps weak
     * references so this won't prevent objects from being garbage collected.
     * 
     * @param aWeakReference
     *            The object that shall receive messages.
     */
    public void attach(WeakReference<Message.Recipient> aWeakReference);

    /**
     * Detaches a {@link Message.Recipient} from the {@link MessageXBar}.
     * 
     * @param aReader
     *            The object that shall be removed messages.
     */
    public void detach(Message.Recipient aReader);

}
