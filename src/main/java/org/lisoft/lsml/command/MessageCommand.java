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
package org.lisoft.lsml.command;

import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.util.CommandStack.Command;

/**
 * This class provides some common functionality for commands that send messages on a {@link MessageDelivery}.
 * 
 * @author Li Song
 */
public abstract class MessageCommand extends Command {
    private MessageDelivery delivery;

    /**
     * Creates a new {@link MessageCommand}.
     * 
     * @param aDelivery
     *            The {@link MessageDelivery} to send messages on.
     */
    public MessageCommand(MessageDelivery aDelivery) {
        delivery = aDelivery;
    }

    protected void post(Message aMessage) {
        if (null != delivery) {
            delivery.post(aMessage);
        }
    }
}
