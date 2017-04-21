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

import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.messages.NotificationMessage;
import org.lisoft.lsml.messages.NotificationMessage.Severity;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

/**
 * This is the main application for the LSML JavaFX GUI.
 *
 * FIXME: Dependency Inject stuff
 *
 * @author Emily Björk
 */
public class LiSongMechLab {
	public static final String DEVELOP_VERSION = "(develop)";

	public static boolean safeCommand(final Node aOwner, final CommandStack aStack, final Command aCommand,
			final MessageDelivery aDelivery) {
		try {
			aStack.pushAndApply(aCommand);
		} catch (final EquipException e) {
			aDelivery.post(new NotificationMessage(Severity.ERROR, null, e.getMessage()));
			return false;
		} catch (final Exception e) {
			LiSongMechLab.showError(aOwner, e);
			return false;
		}
		return true;
	}

	public static void showError(final Node aOwner, final Exception aException) {
		if (Platform.isFxApplicationThread()) {
			final Alert alert = new Alert(AlertType.ERROR, aException.getMessage(), ButtonType.CLOSE);
			if (null != aOwner && aOwner.getScene() != null) {
				alert.initOwner(aOwner.getScene().getWindow());
			}
			alert.getDialogPane().getStylesheets().addAll(FxControlUtils.getBaseStyleSheet());
			alert.showAndWait();
		} else {
			Platform.runLater(() -> showError(aOwner, aException));
		}
	}

}
