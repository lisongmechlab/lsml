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

import java.util.List;
import java.util.Optional;

import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutBuilder.ErrorReportingCallback;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * This class will report errors for a {@link Loadout} to the user through a
 * dialog box.
 *
 * @author Emily Björk
 */
public class DefaultLoadoutErrorReporter implements ErrorReportingCallback {
	// FIXME: Replace by dependency injection framework.
	@Deprecated
	static public final DefaultLoadoutErrorReporter instance = new DefaultLoadoutErrorReporter();

	@Override
	public void report(Optional<Loadout> aLoadout, List<Throwable> aErrors) {
		if (aErrors.isEmpty()) {
			return;
		}

		final VBox box = new VBox();
		for (final Throwable t : aErrors) {
			box.getChildren().add(new Label(t.getMessage()));
		}
		final Alert alert = new Alert(AlertType.INFORMATION);
		alert.getDialogPane().setContent(box);
		if (aLoadout.isPresent()) {
			alert.setHeaderText("Errors occurred while loading " + aLoadout.get().getName() + ".");
		} else {
			alert.setHeaderText("Errors occurred while loading loadouts.");
		}
		alert.showAndWait();
	}
}
