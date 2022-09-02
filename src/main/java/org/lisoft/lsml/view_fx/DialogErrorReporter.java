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
package org.lisoft.lsml.view_fx;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Window;
import javax.inject.Inject;
import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.view_fx.controls.LsmlAlert;

/**
 * A strategy for reporting errors by a dialog to the user.
 *
 * <p>Loadout errors will be batched together and displayed when no errors have been received for a
 * short time.
 *
 * @author Li Song
 */
public class DialogErrorReporter implements ErrorReporter, Thread.UncaughtExceptionHandler {
  private static class LoadoutErrorReport {
    private final List<Throwable> errors;
    private final Loadout loadouts;
    private final Window owner;

    public LoadoutErrorReport(Window aOwner, Loadout aLoadout, List<Throwable> aErrors) {
      owner = aOwner;
      loadouts = aLoadout;
      errors = aErrors;
    }

    void render(StringBuilder aSb) {
      if (errors.isEmpty()) {
        return;
      }

      aSb.append("Errors for: ").append(loadouts.getName()).append('\n');
      for (final Throwable t : errors) {
        aSb.append(" - ").append(t.getMessage()).append('\n');
      }
    }
  }

  class RenderBatchedTask extends TimerTask {
    @Override
    public void run() {
      Platform.runLater(
          () -> {
            Window owner = null;
            String loadoutName = "multiple loadouts";
            final StringBuilder sb = new StringBuilder();
            for (final LoadoutErrorReport errorReport : batchedLoadoutErrors) {
              errorReport.render(sb);

              if (owner == null && errorReport.owner != null) {
                owner = errorReport.owner;
              }
            }
            if (batchedLoadoutErrors.size() == 1) {
              loadoutName = batchedLoadoutErrors.get(0).loadouts.getName();
            }
            batchedLoadoutErrors.clear();
            final LsmlAlert alert = new LsmlAlert(null, AlertType.INFORMATION);
            alert.setTitle("Loadout errors encountered");
            alert.setHeaderText("Errors occurred while loading " + loadoutName + ".");
            alert.setContentText("As much as possible of the loadout(s) have been loaded.");
            alert.setExpandableContent("Details:", sb.toString());
            alert.show();
          });
    }
  }

  private final List<LoadoutErrorReport> batchedLoadoutErrors = new ArrayList<>();
  private long lastMessage = 0;
  private Timer timer = null;

  @Inject
  public DialogErrorReporter() {
    // NOP
  }

  @Override
  public void error(Window aOwner, Loadout aLoadout, List<Throwable> aErrors) {
    if (Platform.isFxApplicationThread()) {
      // Running in FX application thread avoids race conditions on member variables.
      batchedLoadoutErrors.add(new LoadoutErrorReport(aOwner, aLoadout, aErrors));
      if (timer != null) {
        timer.cancel();
      }
      timer = new Timer();
      timer.schedule(new RenderBatchedTask(), 400 /* ms */);
    } else {
      Platform.runLater(() -> error(aOwner, aLoadout, aErrors));
    }
  }

  @Override
  public void error(Window aOwner, String aTitle, String aMessage, Throwable aThrowable) {
    if (Platform.isFxApplicationThread()) {
      informUser(aOwner.getScene().getRoot(), aTitle, aMessage, aThrowable);
    } else {
      Platform.runLater(() -> error(aOwner, aTitle, aMessage, aThrowable));
    }
  }

  @Override
  public void error(Throwable aThrowable) {
    error((Window) null, "Unexpected error", "An unexpected error has occurred.", aThrowable);
  }

  @Override
  public void uncaughtException(final Thread aThread, final Throwable aThrowable) {
    Platform.runLater(
        () ->
            informUser(
                null,
                "LSML has encountered an unexpected error",
                "In most cases LSML can still continue to function normally.\n"
                    + "However as a safety precaution it is recommended to \"save as\" your garage and restart LSML as soon as possible.\n\n"
                    + "Please copy the below error text and report it to: https://github.com/lisongmechlab/lsml/issues",
                aThrowable));
  }

  protected void informUser(Node aSource, String aTitle, String aMessage, Throwable aThrowable) {
    try {
      final long previousMessage = lastMessage;
      lastMessage = System.currentTimeMillis();
      if (lastMessage - previousMessage < 50) {
        return;
      }

      // Borrowed verbatim from:F
      // http://code.makery.ch/blog/javafx-dialogs-official/
      final LsmlAlert alert = new LsmlAlert(aSource, AlertType.ERROR);
      alert.setTitle(aTitle);
      alert.setHeaderText(aTitle);
      alert.setContentText(aMessage + "\n\nError: " + aThrowable.getMessage());

      // Create expandable Exception.
      final String stackTrace = LsmlAlert.exceptionStackTrace(aThrowable);
      final String exceptionText =
          Stream.of(stackTrace.split(System.getProperty("line.separator")))
              .filter(line -> !line.contains("javafx.") && !line.contains("sun.reflect."))
              .collect(Collectors.joining(System.getProperty("line.separator")));

      alert.setExpandableContent("Cause:", exceptionText);
      alert.showAndWait();
    } catch (final Throwable t) {
      // Exceptions must not escape this function.
      t.printStackTrace(System.err);
    }
  }
}
