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

import org.lisoft.lsml.view_fx.util.FxControlUtils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

/**
 * A strategy for reporting errors by a dialog to the user.
 *
 * @author Li Song
 */
public class DialogErrorReporter implements ErrorReporter {

	@Override
	public void error(Window aOwner, String aTitle, String aMessage, Throwable aThrowable) {
		if (Platform.isFxApplicationThread()) {
			final Alert alert = new Alert(AlertType.ERROR, aThrowable.getMessage(), ButtonType.CLOSE);
			if (null != aOwner) {
				alert.initOwner(aOwner);
			}
			alert.getDialogPane().getStylesheets().addAll(FxControlUtils.getBaseStyleSheet());
			alert.showAndWait();
		} else {
			Platform.runLater(() -> error(aOwner, aTitle, aMessage, aThrowable));
		}
	}

	@Override
	public void fatal(Window aOwner, String aTitle, String aMessage, Throwable aThrowable) {
		error(aOwner, aTitle, aMessage, aThrowable);
		System.exit(0);
	}
}
