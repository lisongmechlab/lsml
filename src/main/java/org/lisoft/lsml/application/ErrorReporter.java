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
package org.lisoft.lsml.application;

import java.util.List;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.stage.Window;
import org.lisoft.lsml.model.loadout.Loadout;

/**
 * This interface allows provides a way for objects to report errors to the user.
 *
 * @author Li Song
 */
public interface ErrorReporter {

  /**
   * Report an error caused by an exception.
   *
   * @param aOwner the {@link Node} that this error originated from.
   * @param aTitle How the error is titled.
   * @param aMessage A detailed message.
   * @param aThrowable A {@link Throwable} that caused the error.
   */
  default void error(Node aOwner, String aTitle, String aMessage, Throwable aThrowable) {
    if (Platform.isFxApplicationThread()) {
      final Window w =
          null != aOwner && aOwner.getScene() != null ? aOwner.getScene().getWindow() : null;
      error(w, aTitle, aMessage, aThrowable);
    } else {
      Platform.runLater(() -> error(aOwner, aTitle, aMessage, aThrowable));
    }
  }

  /**
   * Report an error caused by an exception.
   *
   * @param aTitle How the error is titled.
   * @param aMessage A detailed message.
   * @param aThrowable A {@link Throwable} that caused the error.
   */
  default void error(String aTitle, String aMessage, Throwable aThrowable) {
    error((Window) null, aTitle, aMessage, aThrowable);
  }

  /**
   * Report an error caused by an exception.
   *
   * @param aOwner The {@link Window} that this error originated from.
   * @param aTitle How the error is titled.
   * @param aMessage A detailed message.
   * @param aThrowable A {@link Throwable} that caused the error.
   */
  void error(Window aOwner, String aTitle, String aMessage, Throwable aThrowable);

  /**
   * Report a batch of errors related to a loadout.
   *
   * @param aOwner The {@link Window} that this error originated from.
   * @param aLoadout The {@link Loadout} that caused the errors.
   * @param aErrors A {@link List} of exceptions to report.
   */
  void error(Window aOwner, Loadout aLoadout, List<Throwable> aErrors);

  /**
   * Reports an unexpected error with a generic error message.
   *
   * @param aThrowable A {@link Throwable} that caused the error.
   */
  void error(Throwable aThrowable);
}
