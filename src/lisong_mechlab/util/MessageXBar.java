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
package lisong_mechlab.util;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutStandard;

/**
 * Implements a message passing framework for an UI where the components don't have to know about each other, only about
 * the crossbar.
 * 
 * @author Emily Björk
 */
public class MessageXBar {
	private static final boolean									debug			= false;
	private transient final Map<Class<? extends Reader>, Double>	perf_walltime	= debug ? new HashMap<Class<? extends Reader>, Double>()
																							: null;
	private transient final Map<Class<? extends Reader>, Integer>	perf_calls		= debug ? new HashMap<Class<? extends Reader>, Integer>()
																							: null;
	private transient final List<WeakReference<Reader>>				readers			= new ArrayList<WeakReference<MessageXBar.Reader>>();
	private boolean													dispatching		= false;
	private transient final Queue<Message>							messages		= new ArrayDeque<>();

	/**
	 * Classes that need to be able to listen in on the {@link MessageXBar} should implement this interface.
	 * 
	 * @author Emily Björk
	 */
	public static interface Reader {
		void receive(Message aMsg);
	}

	/**
	 * A base interface for all messages sent on the {@link MessageXBar}.
	 * 
	 * @author Emily Björk
	 */
	public static interface Message {
		/**
		 * Checks if this message is related to a specific {@link LoadoutStandard}.
		 * 
		 * @param aLoadout
		 *            The {@link LoadoutStandard} to check.
		 * @return <code>true</code> if this message affects the given {@link LoadoutStandard}.
		 */
		public boolean isForMe(LoadoutBase<?> aLoadout);

		/**
		 * @return <code>true</code> if this message can affect the damage or heat output of the related
		 *         {@link LoadoutStandard}.
		 */
		public boolean affectsHeatOrDamage();
	}

	/**
	 * Sends a message to all listeners on the {@link MessageXBar}. Those listeners which have been disposed of since
	 * the last call to {@link #post(Message)} will be automatically disposed of.
	 * 
	 * @param aMessage
	 *            The message to send.
	 */
	public void post(Message aMessage) {
		if (dispatching) {
			messages.add(aMessage);
		} else {
			dispatchMessage(aMessage);
			while (!messages.isEmpty()) {
				dispatchMessage(messages.remove());
			}
		}
	}

	private void dispatchMessage(Message aMessage) {
		if (dispatching)
			throw new IllegalStateException("Recursive dispatch!");
		dispatching = true;
		try {
			Iterator<WeakReference<Reader>> it = readers.iterator();
			while (it.hasNext()) {
				WeakReference<Reader> ref = it.next();
				Reader reader = ref.get();
				if (reader == null) {
					it.remove();
					continue;
				}
				if (debug) {
					long startNs = System.nanoTime();
					reader.receive(aMessage);
					long endNs = System.nanoTime();
					Double v = perf_walltime.get(reader.getClass());
					Integer u = perf_calls.get(reader.getClass());
					if (v == null) {
						v = 0.0;
						u = 0;
					}
					v += (endNs - startNs) / 1E9;
					u += 1;
					perf_walltime.put(reader.getClass(), v);
					perf_calls.put(reader.getClass(), u);
				} else {
					reader.receive(aMessage);
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			dispatching = false;
		}
	}

	/**
	 * Attaches a new {@link Reader} to this {@link MessageXBar}. The {@link Reader} is automatically converted to a
	 * weak reference.
	 * 
	 * @see #attach(Reader)
	 * @param aReader
	 *            The {@link Reader} to add.
	 */
	public void attach(Reader aReader) {
		attach(new WeakReference<MessageXBar.Reader>(aReader));
	}

	/**
	 * Attaches a new {@link Reader} to this {@link MessageXBar}. The {@link MessageXBar} only keeps weak references so
	 * this won't prevent objects from being garbage collected.
	 * 
	 * @param aWeakReference
	 *            The object that shall receive messages.
	 */
	public void attach(WeakReference<Reader> aWeakReference) {
		if (dispatching)
			throw new IllegalStateException("Attach from call to post!");

		if (debug) {
			for (WeakReference<Reader> reader : readers) {
				if (reader.get() == aWeakReference.get()) {
					throw new RuntimeException("Double registration of reader!");
				}
			}
		}

		readers.add(aWeakReference);
	}

	/**
	 * Detaches a {@link Reader} from the {@link MessageXBar}.
	 * 
	 * @param aReader
	 *            The object that shall be removed messages.
	 */
	public void detach(Reader aReader) {
		if (dispatching)
			throw new IllegalStateException("Detach from call to post!");
		dispatching = true;
		Iterator<WeakReference<Reader>> it = readers.iterator();
		while (it.hasNext()) {
			WeakReference<Reader> ref = it.next();
			if (ref.get() == aReader) {
				it.remove();
			}
		}
		dispatching = false;
	}
}
