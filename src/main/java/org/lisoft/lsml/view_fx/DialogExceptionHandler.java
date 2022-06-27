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
package org.lisoft.lsml.view_fx;

import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
import org.lisoft.lsml.view_fx.controls.LsmlAlert;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class handles any exceptions that were not caught and informs the user of a potential problem.
 *
 * @author Li Song
 */
public class DialogExceptionHandler implements UncaughtExceptionHandler {

    private long lastMessage = 0;

    @Override
    public void uncaughtException(final Thread aThread, final Throwable aThrowable) {
        Platform.runLater(() -> {
            informUser(aThrowable);
        });
    }

    protected void informUser(Throwable aThrowable) {
        try {
            final long previousMessage = lastMessage;
            lastMessage = System.currentTimeMillis();
            if (lastMessage - previousMessage < 50) {
                return;
            }

            // Borrowed verbatim from:
            // http://code.makery.ch/blog/javafx-dialogs-official/
            final LsmlAlert alert = new LsmlAlert(null, AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Li Song Mechlab has encountered an unexpected error.");
            alert.setContentText("In most cases LSML can still continue to function normally.\n" +
                                 "However as a safety precaution it is recommended to \"save as\" your garage and restart LSML as soon as possible.\n\n" +
                                 "Please copy the below error text and report it to: https://github.com/lisongmechlab/lsml/issues");

            // Create expandable Exception.
            final String stackTrace = LsmlAlert.exceptionStackTrace(aThrowable);
            final String newline = System.getProperty("line.separator");
            final String exceptionText = Stream.of(stackTrace.split(newline))
                                               .filter(line -> !line.contains("javafx.") &&
                                                               !line.contains("sun.reflect."))
                                               .collect(Collectors.joining(newline));

            alert.setExpandableContent("The exception stacktrace was:", exceptionText);
            alert.show();
        } catch (final Throwable t) {
            // Exceptions must not escape this function.
            t.printStackTrace(System.err);
        }
    }

}
