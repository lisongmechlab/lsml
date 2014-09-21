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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class allows one to buffer messages for later delivery to another {@link MessageDelivery} instance.
 * 
 * @author Li Song
 *
 */
public class MessageBuffer implements MessageDelivery {
	private final List<Message>	messages	= new ArrayList<>();

	@Override
	public void post(Message aMessage) {
		messages.add(aMessage);
	}

	/**
	 * Delivers all messages to the {@link MessageDelivery} argument.
	 * 
	 * If an exception is thrown during the delivery, the process is aborted. Calling
	 * {@link #deliverTo(MessageDelivery)} again after handling the exception will continue delivering messages that
	 * were left after the message that threw. For example, if messages 0,1,2,3 are to be sent and an exception was
	 * thrown during delivery of 1, then the next call will deliver 2 and 3.
	 * 
	 * @param aMessageDelivery
	 *            The {@link MessageDelivery} to deliver the buffered messages to. A <code>null</code> argument will
	 *            clear the buffer.
	 */
	public void deliverTo(MessageDelivery aMessageDelivery) {
		Iterator<Message> it = messages.iterator();
		while (it.hasNext()) {
			Message message = it.next();
			it.remove();
			if (null != aMessageDelivery) {
				aMessageDelivery.post(message);
			}
		}
	}

	/**
	 * @return <code>true</code> if this {@link MessageBuffer} has any messages that have not been sent yet.
	 */
	public boolean hasMessages() {
		return !messages.isEmpty();
	}
}
