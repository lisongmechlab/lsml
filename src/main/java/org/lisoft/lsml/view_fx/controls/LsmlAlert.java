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
package org.lisoft.lsml.view_fx.controls;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.lisoft.lsml.view_fx.controllers.LSMLStage;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * This class applies some standard attributes and settings to the standard {@link Alert} to reduce code duplication
 * throughout LSML.
 *
 * @author Li Song
 */
public class LsmlAlert extends Alert {

    /**
     * Constructs a new alert.
     *
     * @param aSource
     *            The scene node that this alert originates from.
     * @param aAlertType
     *            The type of the alert.
     * @see Alert#Alert(AlertType)
     */
    public LsmlAlert(Node aSource, AlertType aAlertType) {
        super(aAlertType);
        setupThis(aSource);
    }

    /**
     * Constructs a new alert.
     *
     * @param aSource
     *            The scene node that this alert originates from.
     * @param aAlertType
     *            The type of the alert.
     * @param aContentText
     *            The text in the content field.
     * @param aButtons
     *            The buttons which you would like to appear in the dialog.
     * @see Alert#Alert(AlertType, String, ButtonType...)
     */
    public LsmlAlert(Node aSource, AlertType aAlertType, String aContentText, ButtonType... aButtons) {
        super(aAlertType, aContentText, aButtons);
        setupThis(aSource);
    }

    /**
     * Constructs a new alert.
     *
     * @param aSource
     *            The window that this alert originates from.
     * @param aAlertType
     *            The type of the alert.
     * @param aContentText
     *            The text in the content field.
     * @param aButtons
     *            The buttons which you would like to appear in the dialog.
     * @see Alert#Alert(AlertType, String, ButtonType...)
     */
    public LsmlAlert(Window aSource, AlertType aAlertType, String aContentText, ButtonType... aButtons) {
        super(aAlertType, aContentText, aButtons);
        setupThis(aSource);
    }

    /**
     * Performs the setup to make this alert look like one of us
     *
     * @param aSource
     */
    private void setupThis(Node aSource) {
        if (null != aSource && null != aSource.getScene()) {
            setupThis(aSource.getScene().getWindow());
        }
        setupThis((Window) null);
    }

    /**
     * Performs the setup to make this alert look like one of us
     *
     * @param aSource
     */
    private void setupThis(Window aSource) {
        if (null != aSource) {
            initOwner(aSource);
        }

        final Window thisWindow = getDialogPane().getScene().getWindow();
        ((Stage) thisWindow).getIcons().add(LSMLStage.LSML_ICON);
        getDialogPane().getStylesheets().addAll(FxControlUtils.getBaseStyleSheet());
    }

    public static String exceptionStackTrace(Throwable aThrowable) {
        try (final StringWriter sw = new StringWriter(); final PrintWriter pw = new PrintWriter(sw);) {
            aThrowable.printStackTrace(pw);
            return sw.toString();
        }
        catch (final IOException e) {
            e.printStackTrace();
            return "Failed to generate stack trace!";
        }
    }

    public void setExpandableContent(String aLabel, String aBody) {
        final Label label = new Label(aLabel);

        final TextArea textArea = new TextArea(aBody);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        final GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        getDialogPane().setExpandableContent(expContent);
    }
}
