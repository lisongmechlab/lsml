/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2022  Li Song
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
package org.lisoft.lsml.view_fx.controllers;

import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.export.Base64LoadoutCoder;
import org.lisoft.lsml.model.export.MWOCoder;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.controllers.mainwindow.*;
import org.lisoft.lsml.view_fx.controls.ImportMechStringDialog;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

/**
 * Controller for the main window.
 *
 * @author Li Song
 */
public class MainWindowController extends AbstractFXStageController {
  public static final KeyCodeCombination REDO_KEY_COMBINATION =
      new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN);
  public static final KeyCodeCombination UNDO_KEY_COMBINATION =
      new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN);
  private static final KeyCombination CLOSE_OVERLAY_1 = new KeyCodeCombination(KeyCode.ESCAPE);
  private static final KeyCombination CLOSE_OVERLAY_2 =
      new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN);
  private static final KeyCombination IMPORT_KEY_COMBINATION =
      new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN);
  private static final KeyCodeCombination NEW_MECH_KEY_COMBINATION =
      new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN);
  private static final KeyCombination SEARCH_KEY_COMBINATION =
      new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN);
  private final CommandStack cmdStack;
  private final ErrorReporter errorReporter;
  private final Base64LoadoutCoder lsmlCoder;
  private final MWOCoder mwoCoder;
  private final NewMechPaneController newMechPaneController;
  private final SearchResultsPaneController searchResultsPaneController;
  @FXML private Tab chassisTab;
  @FXML private Tab importExportTab;
  @FXML private Tab loadoutsTab;
  @FXML private TextField searchField;
  @FXML private Tab settingsTab;
  @FXML private Tab weaponsTab;

  @Inject
  public MainWindowController(
      @Named("global") MessageXBar aXBar,
      CommandStack aCommandStack,
      ChassisPageController aChassisPageController,
      ImportExportPageController aImportExportPageController,
      ViewLoadoutsPaneController aViewLoadoutsPaneController,
      SettingsPageController aSettingsPageController,
      WeaponsPageController aWeaponsPageController,
      NewMechPaneController aNewMechPaneController,
      SearchResultsPaneController aSearchResultsPaneController,
      Base64LoadoutCoder aLsmlCoder,
      MWOCoder aMwoCoder,
      ErrorReporter aErrorReporter) {
    super(aXBar);

    lsmlCoder = aLsmlCoder;
    mwoCoder = aMwoCoder;
    errorReporter = aErrorReporter;

    cmdStack = aCommandStack;
    newMechPaneController = aNewMechPaneController;
    searchResultsPaneController = aSearchResultsPaneController;
    StyleManager.makeOverlay(newMechPaneController.getView());
    StyleManager.makeOverlay(searchResultsPaneController.getView());

    FxControlUtils.fixTextField(searchField);

    searchResultsPaneController
        .searchStringProperty()
        .bindBidirectional(searchField.textProperty());
    searchField
        .textProperty()
        .addListener(
            (aObs, aOld, aNew) -> {
              if (aNew != null && !aNew.isEmpty()) {
                if (aOld.isEmpty()) {
                  openOverlay(searchResultsPaneController, false);
                }
              } else {
                closeOverlay(searchResultsPaneController.getView());
              }
            });

    // We're hitting a variant of https://bugs.openjdk.java.net/browse/JDK-8159802
    // Disable undo for the search text field until we can find a proper solution,
    // at any rate I don't think this shortcut is used here.
    searchField.addEventFilter(
        KeyEvent.ANY,
        e -> {
          if (e.getCode() == KeyCode.Z && e.isShortcutDown()) {
            e.consume();
          }
        });

    loadoutsTab.setContent(aViewLoadoutsPaneController.getView());
    chassisTab.setContent(aChassisPageController.getView());
    weaponsTab.setContent(aWeaponsPageController.getView());
    importExportTab.setContent(aImportExportPageController.getView());
    settingsTab.setContent(aSettingsPageController.getView());
  }

  @FXML
  public void importMechString() {
    new ImportMechStringDialog(getStage(), lsmlCoder, mwoCoder, errorReporter, globalXBar)
        .showAndImport();
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
    final ObservableMap<KeyCombination, Runnable> accelerators =
        aStage.getScene().getAccelerators();
    accelerators.put(NEW_MECH_KEY_COMBINATION, this::openNewMechOverlay);
    accelerators.put(SEARCH_KEY_COMBINATION, () -> searchField.requestFocus());
    accelerators.put(IMPORT_KEY_COMBINATION, this::importMechString);
    accelerators.put(CLOSE_OVERLAY_1, this::closeOverlay);
    accelerators.put(CLOSE_OVERLAY_2, this::closeOverlay);
    accelerators.put(REDO_KEY_COMBINATION, cmdStack::redo);
    accelerators.put(UNDO_KEY_COMBINATION, cmdStack::undo);
    aStage.setTitle("Li Song Mechlab");
  }

  private void closeOverlay() {
    closeOverlay(newMechPaneController);
    closeOverlay(searchResultsPaneController);
  }
}
