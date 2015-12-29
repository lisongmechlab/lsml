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
package org.lisoft.lsml.view_fx;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * This class handles any exceptions that were not caught and informs the user of a potential problem.
 * 
 * @author Emily Björk
 */
public class DefaultExceptionHandler implements UncaughtExceptionHandler {

    private long lastMessage = 0;

    @Override
    public void uncaughtException(final Thread aThread, final Throwable aThrowable) {
        Platform.runLater(() -> {
            informUser(aThrowable);
        });
    }

    protected void informUser(Throwable aThrowable) {
        long previousMessage = lastMessage;
        lastMessage = System.currentTimeMillis();
        if (lastMessage - previousMessage < 50) {
            return;
        }

        // Borrowed verbatim from: http://code.makery.ch/blog/javafx-dialogs-official/
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Unexpected error");
        alert.setHeaderText("Li Song Mechlab has encountered an unexpected error.");
        alert.setContentText("In most cases LSML can still continue to function normally.\n"
                + "However as a safety precaution it is recommended to \"save as\" your garage and restart LSML as soon as possible.\n\n"
                + "Please copy the below error text and email to: lisongmechlab@gmail.com.");

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        aThrowable.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }

}
