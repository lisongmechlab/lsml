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
package org.lisoft.lsml.view_fx.controllers;

import javax.inject.Inject;
import javax.inject.Named;

import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.Settings;
import org.lisoft.lsml.view_fx.controllers.mainwindow.ChassisPageController;
import org.lisoft.lsml.view_fx.controllers.mainwindow.ImportExportPageController;
import org.lisoft.lsml.view_fx.controllers.mainwindow.NewMechPaneController;
import org.lisoft.lsml.view_fx.controllers.mainwindow.SearchResultsPaneController;
import org.lisoft.lsml.view_fx.controllers.mainwindow.SettingsPageController;
import org.lisoft.lsml.view_fx.controllers.mainwindow.ViewDropShipsPaneController;
import org.lisoft.lsml.view_fx.controllers.mainwindow.ViewLoadoutsPaneController;
import org.lisoft.lsml.view_fx.controllers.mainwindow.WeaponsPageController;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;

/**
 * Controller for the main window.
 *
 * @author Emily Björk
 */
public class MainWindowController extends AbstractFXStageController {
	public final static KeyCodeCombination REDO_KEYCOMBINATION = new KeyCodeCombination(KeyCode.Y,
			KeyCombination.SHORTCUT_DOWN);
	public final static KeyCodeCombination UNDO_KEYCOMBINATION = new KeyCodeCombination(KeyCode.Z,
			KeyCombination.SHORTCUT_DOWN);
	private static final KeyCodeCombination NEW_MECH_KEYCOMBINATION = new KeyCodeCombination(KeyCode.N,
			KeyCombination.SHORTCUT_DOWN);
	private static final KeyCombination SEARCH_KEYCOMBINATION = new KeyCodeCombination(KeyCode.F,
			KeyCombination.SHORTCUT_DOWN);

	private static final KeyCombination CLOSE_OVERLAY_1 = new KeyCodeCombination(KeyCode.ESCAPE);
	private static final KeyCombination CLOSE_OVERLAY_2 = new KeyCodeCombination(KeyCode.W,
			KeyCombination.SHORTCUT_DOWN);
	@FXML
	private BorderPane content;
	@FXML
	private Toggle nav_chassis;
	@FXML
	private Toggle nav_dropships;
	@FXML
	private ToggleGroup nav_group;
	@FXML
	private Toggle nav_imexport;
	@FXML
	private Toggle nav_loadouts;
	@FXML
	private Toggle nav_settings;
	@FXML
	private Toggle nav_weapons;
	private final Parent page_chassis;
	private final Parent page_dropships;
	private final Parent page_imexport;
	private final Parent page_loadouts;
	private final Parent page_settings;
	private final Parent page_weapons;
	@FXML
	private TextField searchField;

	private final CommandStack cmdStack;
	private final NewMechPaneController newMechPaneController;
	private final SearchResultsPaneController searchResultsPaneController;

	@Inject
	public MainWindowController(Settings aSettings, @Named("global") MessageXBar aXBar, CommandStack aCommandStack,
			ChassisPageController aChassisPageController, ViewDropShipsPaneController aViewDropShipsPaneController,
			ImportExportPageController aImportExportPageController,
			ViewLoadoutsPaneController aViewLoadoutsPaneController, SettingsPageController aSettingsPageController,
			WeaponsPageController aWeaponsPageController, NewMechPaneController aNewMechPaneController,
			SearchResultsPaneController aSearchResultsPaneController) {
		super(aSettings, aXBar);

		cmdStack = aCommandStack;
		newMechPaneController = aNewMechPaneController;
		searchResultsPaneController = aSearchResultsPaneController;
		StyleManager.makeOverlay(newMechPaneController.getView());
		StyleManager.makeOverlay(searchResultsPaneController.getView());

		page_chassis = aChassisPageController.getView();
		page_dropships = aViewDropShipsPaneController.getView();
		page_imexport = aImportExportPageController.getView();
		page_loadouts = aViewLoadoutsPaneController.getView();
		page_settings = aSettingsPageController.getView();
		page_weapons = aWeaponsPageController.getView();
	}

	@FXML
	public void openNewDropshipOverlay() {
		final Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Coming soon!™");
		alert.setHeaderText("Drop ship mode is not yet available.");
		alert.setContentText("Drop ship mode is planned for release in 2.1");
		alert.showAndWait();
		// FIXME Set style
	}

	@FXML
	public void openNewMechOverlay() {
		openOverlay(newMechPaneController, true);
	}

	@Override
	protected void closeOverlay(final Node aOverlayRoot) {
		searchField.textProperty().setValue("");
		super.closeOverlay(aOverlayRoot);
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		FxControlUtils.fixTextField(searchField);

		searchResultsPaneController.searchStringProperty().bindBidirectional(searchField.textProperty());
		searchField.textProperty().addListener((aObs, aOld, aNew) -> {
			if (aNew != null && !aNew.isEmpty()) {
				if (aOld.isEmpty()) {
					openOverlay(searchResultsPaneController, false);
				}
			} else {
				closeOverlay(searchResultsPaneController.getView());
			}
		});

		nav_group.selectToggle(nav_loadouts);
		content.setCenter(page_loadouts);
		nav_group.selectedToggleProperty().addListener((aObservable, aOld, aNew) -> {
			if (aNew == nav_loadouts) {
				content.setCenter(page_loadouts);
			} else if (aNew == nav_dropships) {
				content.setCenter(page_dropships);
			} else if (aNew == nav_chassis) {
				content.setCenter(page_chassis);
			} else if (aNew == nav_weapons) {
				content.setCenter(page_weapons);
			} else if (aNew == nav_imexport) {
				content.setCenter(page_imexport);
			} else if (aNew == nav_settings) {
				content.setCenter(page_settings);
			} else if (aNew == null) {
				aOld.setSelected(true);
			} else {
				throw new IllegalArgumentException("Unknown toggle value! " + aNew);
			}
		});
	}

	@Override
	protected void onShow(LSMLStage aStage) {
		searchField.requestFocus();
		final ObservableMap<KeyCombination, Runnable> accelerators = aStage.getScene().getAccelerators();
		accelerators.put(NEW_MECH_KEYCOMBINATION, () -> openNewMechOverlay());
		accelerators.put(SEARCH_KEYCOMBINATION, () -> searchField.requestFocus());
		accelerators.put(CLOSE_OVERLAY_1, () -> closeOverlay());
		accelerators.put(CLOSE_OVERLAY_2, () -> closeOverlay());
		accelerators.put(REDO_KEYCOMBINATION, () -> cmdStack.redo());
		accelerators.put(UNDO_KEYCOMBINATION, () -> cmdStack.undo());
	}

	private void closeOverlay() {
		closeOverlay(newMechPaneController);
		closeOverlay(searchResultsPaneController);
	}

}
