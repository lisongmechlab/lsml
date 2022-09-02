/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2022  Li Song
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
package org.lisoft.lsml.view_fx.properties;

import java.util.concurrent.Callable;
import java.util.function.Predicate;
import javafx.beans.binding.ObjectBinding;
import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageReception;

/**
 * This binding will bind to an arbitrary attribute of a loadout and provide automatic updating.
 *
 * @param <T> Type of object the binding contains.
 * @author Li Song
 */
public class LsmlObjectBinding<T> extends ObjectBinding<T> implements MessageReceiver {
  private final ErrorReporter errorReporter;
  private final Predicate<Message> invalidationFilter;
  private final Callable<T> valueFunction;

  public LsmlObjectBinding(
      MessageReception aMessageReception,
      Callable<T> aValueFunction,
      Predicate<Message> aInvalidationFilter,
      ErrorReporter aErrorReporter) {
    aMessageReception.attach(this);
    valueFunction = aValueFunction;
    invalidationFilter = aInvalidationFilter;
    errorReporter = aErrorReporter;
  }

  @Override
  public void receive(Message aMsg) {
    try {
      if (invalidationFilter.test(aMsg)) {
        invalidate();
      }
    } catch (Exception e) {
      errorReporter.error(e);
    }
  }

  @Override
  protected T computeValue() {
    try {
      return valueFunction.call();
    } catch (Exception e) {
      errorReporter.error(e);
    }
    return null;
  }
}
