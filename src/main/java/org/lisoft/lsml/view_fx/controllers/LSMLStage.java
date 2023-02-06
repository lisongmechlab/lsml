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
package org.lisoft.lsml.view_fx.controllers;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.lisoft.lsml.view_fx.Settings;
import org.lisoft.lsml.view_fx.controls.LsmlAlert;
import org.lisoft.lsml.view_fx.style.StyleManager;

/**
 * A augmented {@link Stage} for LSML which has custom window decoration and style. Must be
 * associated with an {@link AbstractFXStageController} to function.
 *
 * @author Li Song
 */
public class LSMLStage extends Stage {
  public static final double CHILD_WINDOW_OFFSET = 30;
  public static final Image LSML_ICON = new Image(LSMLStage.class.getResourceAsStream("/icon.png"));
  private final AbstractFXStageController controller;
  private final Region root;

  public LSMLStage(AbstractFXStageController aController, Window aOwner, Settings aSettings) {
    root = aController.getView();
    controller = aController;
    setScene(new Scene(root));
    setupWindowDecorations();
    offsetToParent(aOwner);

    setOnShown((e) -> setupMinimumSize());
    sizeToScene();
    show();
    final Rectangle2D screenBounds = getCurrentScreenBounds();
    setupCompactLayoutTriggers(aSettings, screenBounds);
    clampStageToScreen(screenBounds);

    toFront();
  }

  public void centerStage() {
    final Rectangle2D bounds = Screen.getPrimary().getBounds();
    setX(bounds.getMinX() + bounds.getWidth() / 2 - getWidth() / 2);
    setY(bounds.getMinY() + bounds.getHeight() / 2 - getHeight() / 2);
  }

  private void clampStageToScreen(final Rectangle2D screenBounds) {
    if (getY() < screenBounds.getMinY()) {
      setY(screenBounds.getMinY());
    }

    if (getX() < screenBounds.getMinX()) {
      setX(screenBounds.getMinX());
    }
  }

  private Rectangle2D getCurrentScreenBounds() {
    final ObservableList<Screen> screens =
        Screen.getScreensForRectangle(getX(), getY(), getWidth(), getHeight());
    final Screen screen = screens.get(0);
    return screen.getVisualBounds();
  }

  private void offsetToParent(Window aOwner) {
    if (null != aOwner) {
      final double offset = CHILD_WINDOW_OFFSET;
      setX(aOwner.getX() + offset);
      setY(aOwner.getY() + offset);
    }
  }

  private void setupCompactLayoutTriggers(Settings aSettings, final Rectangle2D screenBounds) {
    final Property<Boolean> useCompactLayout = aSettings.getBoolean(Settings.UI_COMPACT_LAYOUT);
    StyleManager.setCompactStyle(getScene(), useCompactLayout.getValue());
    useCompactLayout.addListener(
        (aObs, aOld, aNew) -> StyleManager.setCompactStyle(getScene(), aNew));

    if (!useCompactLayout.getValue()
        && (getHeight() / screenBounds.getHeight() > 0.95
            || getWidth() / screenBounds.getWidth() > 0.95)) {
      Platform.runLater(
          () -> {
            final LsmlAlert alert = new LsmlAlert(root, AlertType.CONFIRMATION);
            alert.setTitle("Small screen detected");
            alert.setHeaderText(
                "The screen has been detected to be too small to show the current window in it's correct size.");
            alert.setContentText(
                "Would you like to enable compact mode to make windows more compact?");
            alert
                .showAndWait()
                .ifPresent(
                    aButton -> {
                      if (aButton == ButtonType.OK) {
                        useCompactLayout.setValue(true);
                      }
                    });
          });
    }
  }

  private void setupMinimumSize() {
    final Orientation bias = root.getContentBias();
    final double minWidth;
    final double minHeight;
    if (bias == Orientation.VERTICAL) {
      minHeight = root.minHeight(-1);
      minWidth = root.minWidth(minHeight);
    } else {
      minWidth = root.minWidth(-1);
      minHeight = root.minHeight(minWidth);
    }
    setMinWidth(minWidth);
    setMinHeight(minHeight);
  }

  private void setupWindowDecorations() {
    getIcons().add(LSML_ICON);
    initStyle(StageStyle.TRANSPARENT);
    getScene().setFill(Color.TRANSPARENT);
    StyleManager.addClass(root, StyleManager.CLASS_DECOR_ROOT);

    getScene().addEventFilter(MouseEvent.MOUSE_MOVED, controller::onMouseMoved);
    getScene().addEventFilter(MouseEvent.MOUSE_DRAGGED, controller::onMouseDragged);
    getScene().addEventFilter(MouseEvent.MOUSE_CLICKED, controller::onMouseClicked);
  }
}
