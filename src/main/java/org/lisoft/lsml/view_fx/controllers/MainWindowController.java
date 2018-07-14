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
package org.lisoft.lsml.view_fx.controllers;

import javax.inject.Inject;
import javax.inject.Named;

import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.export.Base64LoadoutCoder;
import org.lisoft.lsml.model.export.MWOCoder;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.Settings;
import org.lisoft.lsml.view_fx.controllers.mainwindow.ChassisPageController;
import org.lisoft.lsml.view_fx.controllers.mainwindow.ImportExportPageController;
import org.lisoft.lsml.view_fx.controllers.mainwindow.NewMechPaneController;
import org.lisoft.lsml.view_fx.controllers.mainwindow.SearchResultsPaneController;
import org.lisoft.lsml.view_fx.controllers.mainwindow.SettingsPageController;
import org.lisoft.lsml.view_fx.controllers.mainwindow.ViewLoadoutsPaneController;
import org.lisoft.lsml.view_fx.controllers.mainwindow.WeaponsPageController;
import org.lisoft.lsml.view_fx.controls.ImportMechStringDialog;
import org.lisoft.lsml.view_fx.controls.LsmlAlert;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.Node;
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
 * @author Li Song
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
    private static final KeyCombination IMPORT_KEYCOMBINATION = new KeyCodeCombination(KeyCode.D,
            KeyCombination.SHORTCUT_DOWN);
    private static final KeyCombination CLOSE_OVERLAY_1 = new KeyCodeCombination(KeyCode.ESCAPE);
    private static final KeyCombination CLOSE_OVERLAY_2 = new KeyCodeCombination(KeyCode.W,
            KeyCombination.SHORTCUT_DOWN);
    @FXML
    private BorderPane content;
    @FXML
    private Toggle nav_chassis;
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
    @FXML
    private TextField searchField;

    private final CommandStack cmdStack;
    private final NewMechPaneController newMechPaneController;
    private final SearchResultsPaneController searchResultsPaneController;
    private final Base64LoadoutCoder lsmlCoder;
    private final MWOCoder mwoCoder;
    private final ErrorReporter errorReporter;

    @Inject
    public MainWindowController(Settings aSettings, @Named("global") MessageXBar aXBar, CommandStack aCommandStack,
            ChassisPageController aChassisPageController, ImportExportPageController aImportExportPageController,
            ViewLoadoutsPaneController aViewLoadoutsPaneController, SettingsPageController aSettingsPageController,
            WeaponsPageController aWeaponsPageController, NewMechPaneController aNewMechPaneController,
            SearchResultsPaneController aSearchResultsPaneController, Base64LoadoutCoder aLsmlCoder, MWOCoder aMwoCoder,
            ErrorReporter aErrorReporter) {
        super(aSettings, aXBar);

        lsmlCoder = aLsmlCoder;
        mwoCoder = aMwoCoder;
        errorReporter = aErrorReporter;

        cmdStack = aCommandStack;
        newMechPaneController = aNewMechPaneController;
        searchResultsPaneController = aSearchResultsPaneController;
        StyleManager.makeOverlay(newMechPaneController.getView());
        StyleManager.makeOverlay(searchResultsPaneController.getView());

        FxControlUtils.fixTextField(searchField);

        searchResultsPaneController.searchStringProperty().bindBidirectional(searchField.textProperty());
        searchField.textProperty().addListener((aObs, aOld, aNew) -> {
            if (aNew != null && !aNew.isEmpty()) {
                if (aOld.isEmpty()) {
                    openOverlay(searchResultsPaneController, false);
                }
            }
            else {
                closeOverlay(searchResultsPaneController.getView());
            }
        });

        nav_group.selectToggle(nav_loadouts);
        content.setCenter(aViewLoadoutsPaneController.getView());
        nav_group.selectedToggleProperty().addListener((aObservable, aOld, aNew) -> {
            if (aNew == nav_loadouts) {
                content.setCenter(aViewLoadoutsPaneController.getView());
            }
            else if (aNew == nav_chassis) {
                content.setCenter(aChassisPageController.getView());
            }
            else if (aNew == nav_weapons) {
                content.setCenter(aWeaponsPageController.getView());
            }
            else if (aNew == nav_imexport) {
                content.setCenter(aImportExportPageController.getView());
            }
            else if (aNew == nav_settings) {
                content.setCenter(aSettingsPageController.getView());
            }
            else if (aNew == null) {
                aOld.setSelected(true);
            }
            else {
                throw new IllegalArgumentException("Unknown toggle value! " + aNew);
            }
        });
    }

    @FXML
    public void openNewDropshipOverlay() {
        final LsmlAlert alert = new LsmlAlert(root, AlertType.INFORMATION);
        alert.setTitle("Coming soon!™");
        alert.setHeaderText("Drop ship mode is not yet available.");
        alert.setContentText("Drop ship mode is planned for release in 2.1");
        alert.showAndWait();
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
    protected void onShow(LSMLStage aStage) {
        searchField.requestFocus();
        final ObservableMap<KeyCombination, Runnable> accelerators = aStage.getScene().getAccelerators();
        accelerators.put(NEW_MECH_KEYCOMBINATION, () -> openNewMechOverlay());
        accelerators.put(SEARCH_KEYCOMBINATION, () -> searchField.requestFocus());
        accelerators.put(IMPORT_KEYCOMBINATION, () -> importMechString());
        accelerators.put(CLOSE_OVERLAY_1, () -> closeOverlay());
        accelerators.put(CLOSE_OVERLAY_2, () -> closeOverlay());
        accelerators.put(REDO_KEYCOMBINATION, () -> cmdStack.redo());
        accelerators.put(UNDO_KEYCOMBINATION, () -> cmdStack.undo());
        aStage.setTitle("Li Song Mechlab");
    }

    private void closeOverlay() {
        closeOverlay(newMechPaneController);
        closeOverlay(searchResultsPaneController);
    }

    @FXML
    public void importMechString() {
        new ImportMechStringDialog(getStage(), lsmlCoder, mwoCoder, errorReporter, globalXBar).showAndImport();
    }

}
