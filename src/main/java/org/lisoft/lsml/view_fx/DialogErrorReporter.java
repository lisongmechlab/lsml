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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.inject.Inject;

import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.view_fx.controls.LsmlAlert;

import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Window;

/**
 * A strategy for reporting errors by a dialog to the user.
 *
 * @author Emily Björk
 */
public class DialogErrorReporter implements ErrorReporter {

    @Inject
    public DialogErrorReporter() {
        // NOP
    }

    @Override
    public void error(Window aOwner, Loadout aLoadout, List<Throwable> aErrors) {
        if (Platform.isFxApplicationThread()) {
            if (aErrors.isEmpty()) {
                return;
            }

            final VBox box = new VBox();
            for (final Throwable t : aErrors) {
                box.getChildren().add(new Label(t.getMessage()));
            }
            final LsmlAlert alert = new LsmlAlert(null, AlertType.INFORMATION);
            alert.getDialogPane().setContent(box);
            alert.setHeaderText("Errors occurred while loading " + aLoadout.getName() + ".");
            alert.showAndWait();
        }
        else {
            Platform.runLater(() -> error(aOwner, aLoadout, aErrors));
        }
    }

    @Override
    public void error(Window aOwner, String aTitle, String aMessage, Throwable aThrowable) {
        if (Platform.isFxApplicationThread()) {
            final LsmlAlert alert = new LsmlAlert(aOwner, AlertType.ERROR, aTitle, ButtonType.CLOSE);
            final VBox box = new VBox();
            final Text msgLabel = new Text(aMessage);
            msgLabel.setWrappingWidth(400);
            msgLabel.prefWidth(200);

            box.getChildren().add(msgLabel);
            if (null != aThrowable) {
                box.getChildren().add(new Label("Cause: "));
                try (final StringWriter sw = new StringWriter(); final PrintWriter pw = new PrintWriter(sw);) {
                    aThrowable.printStackTrace(pw);
                    box.getChildren().add(new Label(sw.toString()));
                }
                catch (final IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            alert.getDialogPane().setContent(box);
            alert.setHeaderText(aTitle);
            alert.showAndWait();
        }
        else {
            Platform.runLater(() -> error(aOwner, aTitle, aMessage, aThrowable));
        }
    }
}
