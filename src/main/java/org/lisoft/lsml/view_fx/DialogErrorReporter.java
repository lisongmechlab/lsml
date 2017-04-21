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
import javafx.stage.Window;

/**
 * A strategy for reporting errors by a dialog to the user.
 *
 * @author Li Song
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
            final LsmlAlert alert = new LsmlAlert(aOwner, AlertType.ERROR, aThrowable.getMessage(), ButtonType.CLOSE);
            alert.showAndWait();
        }
        else {
            Platform.runLater(() -> error(aOwner, aTitle, aMessage, aThrowable));
        }
    }

    @Override
    public void fatal(Window aOwner, String aTitle, String aMessage, Throwable aThrowable) {
        error(aOwner, aTitle, aMessage, aThrowable);
        System.exit(0);
    }
}
