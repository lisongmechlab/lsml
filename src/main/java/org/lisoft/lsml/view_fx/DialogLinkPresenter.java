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

import java.awt.Desktop;
import java.net.URI;

import javax.inject.Inject;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;

/**
 * This {@link LinkPresenter} presents the link in a modal dialog.
 *
 * @author Li Song
 */
public class DialogLinkPresenter implements LinkPresenter {

	private final ErrorReporter errorReporter;

	@Inject
	public DialogLinkPresenter(ErrorReporter aErrorReporter) {
		errorReporter = aErrorReporter;
	}

	@Override
	public void show(String aTitle, String aContent, String aLink, Node aOwner) {
		final Hyperlink hyperlink = new Hyperlink(aLink);
		hyperlink.setOnAction((aEvent) -> {
			try {
				Desktop.getDesktop().browse(new URI(aLink));
			} catch (final Exception e) {
				errorReporter.error("Couldn't open broser",
						"LSML was unable to open link in the default browser. Please open the link manually.", e);
			}
		});

		final MenuItem mi = new MenuItem("Copy link");
		mi.setOnAction((aEvent) -> {
			final ClipboardContent content = new ClipboardContent();
			content.putString(aLink);
			Clipboard.getSystemClipboard().setContent(content);
		});
		final ContextMenu cm = new ContextMenu(mi);
		hyperlink.setContextMenu(cm);

		final VBox content = new VBox();
		content.getChildren().add(new Label("Right click to copy:"));
		content.getChildren().add(hyperlink);

		final Alert alert = new Alert(AlertType.INFORMATION, aLink, ButtonType.OK);
		alert.setTitle(aTitle);
		alert.setHeaderText(aContent);
		alert.getDialogPane().setContent(content);
		alert.show();
	}

}
