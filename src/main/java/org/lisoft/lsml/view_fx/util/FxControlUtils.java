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
package org.lisoft.lsml.view_fx.util;

import java.io.IOException;
import java.net.URL;
import java.util.function.Predicate;

import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.Garage;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.garage.GaragePath;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.GarageTreeCell;
import org.lisoft.lsml.view_fx.GarageTreeItem;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.lsml.view_fx.style.WindowState;

import javafx.application.Platform;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Assorted helper methods for dealing with FXML.
 *
 * @author Emily Björk
 */
public class FxControlUtils {
    /**
     * This method facilitates a kind of a three-way binding that may fail. The value of the check-box is bound to a
     * {@link BooleanExpression}, an attempt to change the value of the check-box will be sent to a predicate that may
     * fail. If the predicate fails the check-box retains it's old value.
     *
     * @param aCheckBox
     *            The {@link CheckBox} to bind.
     * @param aBooleanExpression
     *            The {@link BooleanExpression} to bind to.
     * @param aSuccess
     *            A predicate that performs the action and returns <code>true</code> if it succeeded.
     */
    public static void bindTogglable(final CheckBox aCheckBox, final BooleanExpression aBooleanExpression,
            final Predicate<Boolean> aSuccess) {
        aCheckBox.setSelected(aBooleanExpression.get());
        aBooleanExpression.addListener((aObservable, aOld, aNew) -> {
            aCheckBox.setSelected(aNew);
        });

        aCheckBox.setOnAction(e -> {
            final boolean value = aCheckBox.isSelected();
            final boolean oldValue = aBooleanExpression.get();
            if (value != oldValue && !aSuccess.test(value)) {
                aCheckBox.setSelected(oldValue);
            }
        });
    }

    /**
     * This method facilitates a kind of a three-way binding that may fail. The value of the toggle button is bound to a
     * {@link BooleanExpression}, an attempt to change the value of the toggle button will be sent to a predicate that
     * may fail. If the predicate fails the toggle button retains it's old value.
     *
     * @param aToggleButton
     *            The {@link ToggleButton} to bind.
     * @param aBooleanExpression
     *            The {@link BooleanExpression} to bind to.
     * @param aSuccess
     *            A predicate that performs the action and returns <code>true</code> if it succeeded.
     */
    public static void bindTogglable(final ToggleButton aToggleButton, final BooleanExpression aBooleanExpression,
            final Predicate<Boolean> aSuccess) {
        aToggleButton.setSelected(aBooleanExpression.get());
        aBooleanExpression.addListener((aObservable, aOld, aNew) -> {
            aToggleButton.setSelected(aNew);
        });

        aToggleButton.setOnAction(e -> {
            final boolean value = aToggleButton.isSelected();
            final boolean oldValue = aBooleanExpression.get();
            if (value != oldValue && !aSuccess.test(value)) {
                aToggleButton.setSelected(oldValue);
            }
        });
    }

    /**
     * Fixes issues with the JavaFX spinner class:
     *
     * <ul>
     * <li>Commit edit value on focus lost.</li>
     * <li>Revert to last valid on invalid input.</li>
     * </ul>
     *
     * @param aSpinner
     *            The spinner to adjust.
     */
    public static <T> void fixSpinner(Spinner<T> aSpinner) {

        final SpinnerValueFactory<T> factory = aSpinner.getValueFactory();
        final TextFormatter<T> formatter = new TextFormatter<>(factory.getConverter(), factory.getValue());
        aSpinner.getEditor().setTextFormatter(formatter);
        factory.valueProperty().bindBidirectional(formatter.valueProperty());

        aSpinner.valueProperty().addListener((aObs, aOld, aNew) -> {
            if (aNew == null) {
                aSpinner.getValueFactory().setValue(aOld);
            }
        });

        // aSpinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
        // if (!aSpinner.isEditable())
        // return;
        // String text = aSpinner.getEditor().getText();
        // SpinnerValueFactory<T> valueFactory = aSpinner.getValueFactory();
        // if (valueFactory != null) {
        // StringConverter<T> converter = valueFactory.getConverter();
        // if (converter != null) {
        // T value = converter.fromString(text);
        // if (value != null && !valueFactory.getValue().equals(value)) {
        // valueFactory.setValue(value);
        // }
        // }
        // }
        // });
    }

    public static void fixTextField(TextField aTextField) {
        aTextField.focusedProperty().addListener((aObs, aOld, aNew) -> {
            if (aOld == true && aNew == false) {
                aTextField.getOnAction().handle(new ActionEvent(aTextField, aTextField));
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
     * Will load a FXML file and set the argument as the controller. The name of the FXML file is determined from the
     * class name of the controller passed in as argument. The FXML file is sought for under resources: "view/".
     *
     * This is a convenient way of creating a custom control. Example:
     *
     * <pre>
     * class MyControl extends BorderPane{ // Must inherit from the root type in the FXML file.
     *     public MyControl{
     *         loadFxmlControl(this);
     *     }
     * }
     *
     * View in: "view/MyControl.fxml", must use 'fx:root' construct.
     * </pre>
     *
     * @param aControl
     *            The controller.
     */
    public static void loadFxmlControl(final Object aControl) {
        final String fxmlFile = "view/" + aControl.getClass().getSimpleName() + ".fxml";
        final URL fxmlResource = ClassLoader.getSystemClassLoader().getResource(fxmlFile);
        if (null == fxmlResource) {
            throw new IllegalArgumentException("Unable to load FXML file: " + fxmlFile);
        }
        final FXMLLoader fxmlLoader = new FXMLLoader(fxmlResource);
        fxmlLoader.setControllerFactory((aClass) -> aControl);
        fxmlLoader.setRoot(aControl);
        try {
            fxmlLoader.load();
        }
        catch (final IOException e) {
            // Failure to load XML is a program error and cannot be recovered from, promote to unchecked.
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets up a {@link TreeView} to show garage contents.
     *
     * @param aTreeView
     *            The {@link TreeView} to set up.
     * @param aRoot
     *            The root {@link GarageDirectory} of {@link Garage} to show.
     * @param aXBar
     *            A {@link MessageXBar} to listen to changes and to send updates on.
     * @param aStack
     *            A {@link CommandStack} to use for executing changes to the garage on.
     * @param aShowValues
     *            <code>true</code> if the loadouts or drop ships should be shown in the tree.
     */
    public static <T extends NamedObject> void setupGarageTree(TreeView<GaragePath<T>> aTreeView,
            GarageDirectory<T> aRoot, MessageXBar aXBar, CommandStack aStack, boolean aShowValues) {
        aTreeView.setRoot(new GarageTreeItem<>(aXBar, new GaragePath<T>(null, aRoot), aShowValues));
        aTreeView.getRoot().setExpanded(true);
        aTreeView.setShowRoot(true);
        aTreeView.setCellFactory(aView -> new GarageTreeCell<>(aXBar, aStack, aTreeView));
        aTreeView.setEditable(true);
    }

    /**
     * Sets up a stage with the given {@link Parent} as {@link Scene} root and applies styling to the stage.
     *
     * @param aStage
     *            The stage to set up.
     * @param aRoot
     *            The scene root.
     * @param aWindowState
     *            A {@link WindowState} that contains the current status of the custom stage decorations.
     * @param aCompactUI
     *            If <code>true</code> creates the stage in compact mode.
     */
    public static void setupStage(final Stage aStage, final Region aRoot, WindowState aWindowState,
            Property<Boolean> aCompactUI) {

        StyleManager.addClass(aRoot, StyleManager.CLASS_DECOR_ROOT);

        final Scene scene = new Scene(aRoot);
        scene.setFill(Color.TRANSPARENT);
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, aEvent -> {
            aWindowState.onMouseMoved(aEvent);
        });
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, aEvent -> {
            aWindowState.onMouseDragged(aEvent);
        });
        scene.addEventFilter(MouseEvent.MOUSE_CLICKED, aEvent -> {
            aWindowState.onMouseClicked(aEvent);
        });

        StyleManager.setCompactStyle(scene, aCompactUI.getValue());
        aCompactUI.addListener((aObs, aOld, aNew) -> {
            StyleManager.setCompactStyle(scene, aNew);
        });

        aStage.initStyle(StageStyle.TRANSPARENT);
        aStage.getIcons().add(new Image(ClassLoader.getSystemClassLoader().getResourceAsStream("icon.png")));
        aStage.setScene(scene);
        aStage.sizeToScene();
        aStage.show();
        aStage.toFront();

        final ObservableList<Screen> screens = Screen.getScreensForRectangle(aStage.getX(), aStage.getY(),
                aStage.getWidth(), aStage.getHeight());
        final Screen screen = screens.get(0);
        final Rectangle2D screenBounds = screen.getVisualBounds();

        if (aCompactUI.getValue() == false && (aStage.getHeight() / screenBounds.getHeight() > 0.95
                || aStage.getWidth() / screenBounds.getWidth() > 0.95)) {
            Platform.runLater(() -> {
                final Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Small screem detected");
                alert.setHeaderText(
                        "The screen has been detected to be too small to show the current window in it's correct size.");
                alert.setContentText("Would you like to enable compact mode to make windows more compact?");
                alert.showAndWait().ifPresent(aButton -> {
                    if (aButton == ButtonType.OK) {
                        aCompactUI.setValue(true);
                    }
                });
            });
        }

        if (aStage.getY() < 0) {
            aStage.setY(0);
        }

        if (aStage.getX() < 0) {
            aStage.setX(0);
        }

        final Orientation bias = aRoot.getContentBias();
        final double minWidth;
        final double minHeight;
        if (bias == Orientation.VERTICAL) {
            minHeight = aRoot.minHeight(-1);
            minWidth = aRoot.minWidth(minHeight);
        }
        else {
            minWidth = aRoot.minWidth(-1);
            minHeight = aRoot.minHeight(minWidth);
        }
        aStage.setMinWidth(minWidth);
        aStage.setMinHeight(minHeight);
    }

    /**
     * Sets up a {@link ToggleButton} to show different text depending on if it is selected or not.
     *
     * @param aButton
     *            The {@link ToggleButton} to set up.
     * @param aSelected
     *            The text to show if the toggle is selected.
     * @param aUnSelected
     *            The text to show if the toggle is unselected.
     */
    public static void setupToggleText(ToggleButton aButton, String aSelected, String aUnSelected) {
        final StringBinding textBinding = FxBindingUtils.bindToggledText(aButton.selectedProperty(), aSelected,
                aUnSelected);
        aButton.textProperty().bind(textBinding);
    }
}
