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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Implements a message passing framework for an UI where the components don't have to know about each other, only about
 * the crossbar.
 *
 * @author Emily Björk
 */
public class MessageXBar implements MessageReception, MessageDelivery {
    private static final boolean debug = false;
    private transient final Map<Class<? extends MessageReceiver>, Double> perf_walltime = debug ? new HashMap<>()
            : null;
    private transient final Map<Class<? extends MessageReceiver>, Integer> perf_calls = debug ? new HashMap<>() : null;
    private transient final List<WeakReference<MessageReceiver>> readers = new ArrayList<>();
    private boolean dispatching = false;
    private transient final Queue<Message> messages = new ArrayDeque<>();

    @Override
    public void attach(MessageReceiver aReader) {
        attach(new WeakReference<>(aReader));
    }

    @Override
    public void attach(WeakReference<MessageReceiver> aWeakReference) {
        if (dispatching) {
            throw new IllegalStateException("Attach from call to post!");
        }

        if (debug) {
            for (final WeakReference<MessageReceiver> reader : readers) {
                if (reader.get() == aWeakReference.get()) {
                    throw new RuntimeException("Double registration of reader!");
                }
            }
        }

        readers.add(aWeakReference);
    }

    @Override
    public void detach(MessageReceiver aReader) {
        if (dispatching) {
            throw new IllegalStateException("Detach from call to post!");
        }
        dispatching = true;
        final Iterator<WeakReference<MessageReceiver>> it = readers.iterator();
        while (it.hasNext()) {
            final WeakReference<MessageReceiver> ref = it.next();
            if (ref.get() == aReader) {
                it.remove();
            }
        }
        dispatching = false;
    }

    @Override
    public void post(Message aMessage) {
        if (dispatching) {
            messages.add(aMessage);
        }
        else {
            dispatchMessage(aMessage);
            while (!messages.isEmpty()) {
                dispatchMessage(messages.remove());
            }
        }
    }

    private void dispatchMessage(Message aMessage) {
        if (dispatching) {
            throw new IllegalStateException("Recursive dispatch!");
        }
        dispatching = true;
        try {
            final Iterator<WeakReference<MessageReceiver>> it = readers.iterator();
            while (it.hasNext()) {
                final WeakReference<MessageReceiver> ref = it.next();
                final MessageReceiver reader = ref.get();
                if (reader == null) {
                    it.remove();
                    continue;
                }
                if (debug) {
                    final long startNs = System.nanoTime();
                    reader.receive(aMessage);
                    final long endNs = System.nanoTime();
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
                }
                else {
                    reader.receive(aMessage);
                }
            }
        }
        catch (final Throwable t) {
            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), t);
        }
        finally {
            dispatching = false;
        }
    }
}
