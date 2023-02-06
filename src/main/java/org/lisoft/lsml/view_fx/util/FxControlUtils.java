/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.view_fx.util;

import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.beans.binding.BooleanExpression;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.Garage;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.garage.GaragePath;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.controls.GarageTreeCell;
import org.lisoft.lsml.view_fx.controls.GarageTreeItem;

/**
 * Assorted helper methods for dealing with FXML.
 *
 * @author Li Song
 */
public class FxControlUtils {
  /**
   * This method facilitates a kind of three-way binding that may fail. The value of the check-box
   * is bound to a {@link BooleanExpression}, an attempt to change the value of the check-box will
   * be sent to a predicate that may fail. If the predicate fails the check-box retains its old
   * value.
   *
   * @param aCheckBox The {@link CheckBox} to bind.
   * @param aBooleanExpression The {@link BooleanExpression} to bind to.
   * @param aSuccess A predicate that performs the action and returns <code>true</code> if it
   *     succeeded.
   */
  public static void bindTogglable(
      final CheckBox aCheckBox,
      final BooleanExpression aBooleanExpression,
      final Predicate<Boolean> aSuccess) {
    aCheckBox.setSelected(aBooleanExpression.get());
    aBooleanExpression.addListener((aObservable, aOld, aNew) -> aCheckBox.setSelected(aNew));

    aCheckBox.setOnAction(
        e -> {
          final boolean value = aCheckBox.isSelected();
          final boolean oldValue = aBooleanExpression.get();
          if (value != oldValue && !aSuccess.test(value)) {
            aCheckBox.setSelected(oldValue);
          }
        });
  }

  /**
   * This method facilitates a kind of three-way binding that may fail. The value of the toggle
   * button is bound to a {@link BooleanExpression}, an attempt to change the value of the toggle
   * button will be sent to a predicate that may fail. If the predicate fails the toggle button
   * retains its old value.
   *
   * @param aToggleButton The {@link ToggleButton} to bind.
   * @param aBooleanExpression The {@link BooleanExpression} to bind to.
   * @param aSuccess A predicate that performs the action and returns <code>true</code> if it
   *     succeeded.
   */
  public static void bindTogglable(
      final ToggleButton aToggleButton,
      final BooleanExpression aBooleanExpression,
      final Predicate<Boolean> aSuccess) {
    aToggleButton.setSelected(aBooleanExpression.get());
    aBooleanExpression.addListener((aObservable, aOld, aNew) -> aToggleButton.setSelected(aNew));

    aToggleButton.setOnAction(
        e -> {
          final boolean value = aToggleButton.isSelected();
          final boolean oldValue = aBooleanExpression.get();
          if (value != oldValue && !aSuccess.test(value)) {
            aToggleButton.setSelected(oldValue);
          }
        });
  }

  public static void escapeWindow(KeyEvent aEvent, Node aRoot, Runnable aCloseAction) {
    if (aEvent.getCode() == KeyCode.ESCAPE) {
      if (!FxControlUtils.isEditingSomething(aRoot.getScene())) {
        aCloseAction.run();
        aEvent.consume();
        return;
      }
      aRoot.requestFocus();
    }
  }

  public static void fixComboBox(ComboBox<?> aComboBox) {
    // Focusing an editable ComboBox should select all text
    aComboBox
        .focusedProperty()
        .addListener(
            (aObs, aOld, aNew) -> {
              if (!aOld && aNew) {
                // Gained focus
                if (aComboBox.isEditable()) {
                  Platform.runLater(() -> aComboBox.getEditor().selectAll());
                }
              }
            });
  }

  /**
   * Fixes issues with the JavaFX spinner class:
   *
   * <ul>
   *   <li>Commit edit value on focus lost.
   *   <li>Revert to last valid on invalid input.
   * </ul>
   *
   * @param aSpinner The spinner to adjust.
   */
  public static <T> void fixSpinner(Spinner<T> aSpinner) {

    final SpinnerValueFactory<T> factory = aSpinner.getValueFactory();
    final TextFormatter<T> formatter =
        new TextFormatter<>(factory.getConverter(), factory.getValue());
    aSpinner.getEditor().setTextFormatter(formatter);
    factory.valueProperty().bindBidirectional(formatter.valueProperty());

    aSpinner
        .valueProperty()
        .addListener(
            (aObs, aOld, aNew) -> {
              if (aNew == null) {
                aSpinner.getValueFactory().setValue(aOld);
              }
            });
  }

  /**
   * Make the default JavaFX text field suck less:
   *
   * <ul>
   *   <li>Commit text entry on focus lost (instead of cancelling).
   *   <li>Enter will commit and lose focus
   *   <li>Escape will cancel and lose focus
   *   <li>Makes sure that a text formatter is always present to avoid NPEs
   *   <li>
   * </ul>
   *
   * @param aTextField The text field to be fixed.
   */
  public static void fixTextField(TextField aTextField) {
    if (null == aTextField.getTextFormatter()) {
      aTextField.setTextFormatter(new TextFormatter<>(TextFormatter.IDENTITY_STRING_CONVERTER));
    }

    // Commit text on focus lost
    aTextField
        .focusedProperty()
        .addListener(
            (aObs, aOld, aNew) -> {
              final EventHandler<ActionEvent> action = aTextField.getOnAction();
              if (aOld && !aNew && null != action) {
                action.handle(new ActionEvent(aTextField, aTextField));
              }
            });

    aTextField.setOnKeyPressed(
        aEvent -> {
          // Enter commits and removes focus
          if (aEvent.getCode() == KeyCode.ENTER) {
            aTextField.getParent().requestFocus();
            aEvent.consume();
          }
          // Escape aborts edit and removes focus
          if (aEvent.getCode() == KeyCode.ESCAPE) {
            aTextField.cancelEdit();
            aTextField.getParent().requestFocus();
            aEvent.consume();
          }
        });
  }

  /**
   * @return The URI path to the base style sheet.
   */
  public static String getBaseStyleSheet() {
    return "view/BaseStyle.css";
  }

  /**
   * @return The URI path to the loadout style sheet.
   */
  public static String getLoadoutStyleSheet() {
    return "view/LoadoutStyle.css";
  }

  public static boolean isDoubleClick(MouseEvent aEvent) {
    return aEvent.getButton() == MouseButton.PRIMARY && aEvent.getClickCount() % 2 == 0;
  }

  /**
   * Checks if the user is currently editing something in the given scene.
   *
   * @param aScene The scene to check.
   * @return True if the user is currently editing any text input type control in the scene.
   */
  public static boolean isEditingSomething(Scene aScene) {
    final Node focusOwner = aScene.getFocusOwner();
    if (focusOwner instanceof final TextInputControl control) {
      if (control.isEditable()) {
        return true;
      }
    }

    if (focusOwner instanceof final Spinner<?> spinner) {
      return spinner.isEditable();
    }
    return false;
  }

  public static void resizeComboBoxToContent(ComboBox<?> aComboBox) {

    aComboBox
        .getEditor()
        .prefColumnCountProperty()
        .bind(aComboBox.getSelectionModel().selectedItemProperty().asString().length());
  }

  /**
   * Sets up a {@link TreeView} to show garage contents.
   *
   * @param aTreeView The {@link TreeView} to set up.
   * @param aRoot The root {@link GarageDirectory} of {@link Garage} to show.
   * @param aXBar A {@link MessageXBar} to listen to changes and to send updates on.
   * @param aStack A {@link CommandStack} to use for executing changes to the garage on.
   * @param aShowValues <code>true</code> if the loadouts or drop-ships should be shown in the tree.
   * @param aClazz The class of T.
   * @param aErrorReporter An instance of {@link ErrorReporter} to report any errors on.
   */
  public static <T extends NamedObject> void setupGarageTree(
      TreeView<GaragePath<T>> aTreeView,
      GarageDirectory<T> aRoot,
      MessageXBar aXBar,
      CommandStack aStack,
      boolean aShowValues,
      Class<T> aClazz,
      ErrorReporter aErrorReporter) {
    aTreeView.setRoot(new GarageTreeItem<>(aXBar, new GaragePath<>(aRoot), aShowValues, aClazz));
    aTreeView.getRoot().setExpanded(true);
    aTreeView.setShowRoot(true);
    aTreeView.setCellFactory(aView -> new GarageTreeCell<>(aXBar, aStack, aErrorReporter));
    aTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    aTreeView.setEditable(true);
  }
}
