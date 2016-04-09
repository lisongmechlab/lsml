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
package org.lisoft.lsml.view_fx.style;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * This class will replace the window decorations on the given {@link Region}. The {@link Region} will have CSS
 * pseudoclass "maximized" when it is maximized. Typically one should style it like so:
 * 
 * 
 * <pre>
 * .decor-root{
 *     -fx-background-insets: 40px;
 *     -fx-padding: 40px;
 *     -fx-effect: dropshadow(three-pass-box, black, 8, 0.0, 0.0, 0.0);
 * }
 *
 * .decor-root:maximized{
 *     -fx-background-insets: 0px;
 *     -fx-padding: 0px;
 *     -fx-effect: null;
 * }
 * </pre>
 * 
 * @author Li Song
 */
public class WindowDecoration {

    private final static PseudoClass PC_MAXIMIZED = PseudoClass.getPseudoClass("maximized");
    private final static double MOVE_EDGE = 13.0;
    private final static double RESIZE_EDGE = 2.0;
    private final Stage stage;
    private final BooleanProperty maximized = new SimpleBooleanProperty(false);

    private double mousePrevMouseAbsX;
    private double mousePrevMouseAbsY;
    private Rectangle2D savedBounds = null;
    private Cursor currentCursor = Cursor.DEFAULT;

    public WindowDecoration(Stage aStage, Region aSceneRoot) {
        stage = aStage;
        stage.initStyle(StageStyle.TRANSPARENT);

        maximized.addListener((aObs, aOld, aNew) -> {
            final Rectangle2D newBounds;
            if (aNew) {
                ObservableList<Screen> screensForRectangle = Screen.getScreensForRectangle(stage.getX(), stage.getY(),
                        stage.getWidth(), stage.getHeight());
                Screen screen = screensForRectangle.get(0);

                newBounds = screen.getVisualBounds();
                savedBounds = new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
            }
            else {
                newBounds = savedBounds;
                savedBounds = null;
            }

            stage.setX(newBounds.getMinX());
            stage.setY(newBounds.getMinY());
            stage.setWidth(newBounds.getWidth());
            stage.setHeight(newBounds.getHeight());
            aSceneRoot.pseudoClassStateChanged(PC_MAXIMIZED, aNew);
        });

        aSceneRoot.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                // Double click on primary
                final Insets padding = aSceneRoot.getPadding();
                if (e.getScreenY() - stage.getY() < MOVE_EDGE + padding.getTop()) {
                    windowMaximize();
                }
            }
        });

        aSceneRoot.setOnMouseMoved(e -> {
            if (maximized.getValue()) {
                if (currentCursor != Cursor.DEFAULT) {
                    aSceneRoot.setCursor(Cursor.DEFAULT);
                    currentCursor = Cursor.DEFAULT;
                }
                return;
            }

            double dX = e.getScreenX() - stage.getX();
            double dY = e.getScreenY() - stage.getY();
            double h = stage.getHeight();
            double w = stage.getWidth();

            mousePrevMouseAbsX = e.getScreenX();
            mousePrevMouseAbsY = e.getScreenY();

            final Insets padding = aSceneRoot.getPadding();
            final boolean topEdge = dY <= (RESIZE_EDGE + padding.getTop()) && dY >= padding.getTop();
            final boolean topMoveEdge = dY <= (MOVE_EDGE + padding.getTop()) && dY >= padding.getTop();
            final boolean bottomEdge = dY >= h - (RESIZE_EDGE + padding.getBottom()) && dY >= padding.getBottom();
            final boolean leftEdge = dX <= (RESIZE_EDGE + padding.getLeft()) && dY >= padding.getLeft();
            final boolean rightEdge = dX >= w - (RESIZE_EDGE + padding.getRight()) && dY >= padding.getRight();

            final Cursor newCursor;
            if (topEdge || topMoveEdge) {
                if (leftEdge)
                    newCursor = Cursor.NW_RESIZE;
                else if (rightEdge)
                    newCursor = Cursor.NE_RESIZE;
                else if (topEdge)
                    newCursor = Cursor.N_RESIZE;
                else
                    newCursor = Cursor.MOVE;
            }
            else if (bottomEdge) {
                if (leftEdge)
                    newCursor = Cursor.SW_RESIZE;
                else if (rightEdge)
                    newCursor = Cursor.SE_RESIZE;
                else
                    newCursor = Cursor.S_RESIZE;
            }
            else {
                if (leftEdge)
                    newCursor = Cursor.W_RESIZE;
                else if (rightEdge)
                    newCursor = Cursor.E_RESIZE;
                else
                    newCursor = Cursor.DEFAULT;
            }

            if (currentCursor != newCursor) {
                aSceneRoot.setCursor(newCursor);
                currentCursor = newCursor;
            }
        });

        aSceneRoot.setOnMouseDragged(e -> {
            if (maximized.getValue()) {
                return;
            }

            final double newMouseAbsX = e.getScreenX();
            final double newMouseAbsY = e.getScreenY();
            final double dX = newMouseAbsX - mousePrevMouseAbsX;
            final double dY = newMouseAbsY - mousePrevMouseAbsY;
            mousePrevMouseAbsX = newMouseAbsX;
            mousePrevMouseAbsY = newMouseAbsY;

            if (currentCursor == Cursor.MOVE) {
                stage.setX(stage.getX() + dX);
                stage.setY(stage.getY() + dY);
            }

            if (currentCursor == Cursor.N_RESIZE || currentCursor == Cursor.NE_RESIZE
                    || currentCursor == Cursor.NW_RESIZE) {
                double newHeight = stage.getHeight() - dY;
                if (newHeight >= stage.getMinHeight()) {
                    stage.setY(stage.getY() + dY);
                    stage.setHeight(newHeight);
                }
            }

            if (currentCursor == Cursor.S_RESIZE || currentCursor == Cursor.SE_RESIZE
                    || currentCursor == Cursor.SW_RESIZE) {
                double newHeight = stage.getHeight() + dY;
                if (newHeight >= stage.getMinHeight()) {
                    stage.setHeight(newHeight);
                }
            }

            if (currentCursor == Cursor.E_RESIZE || currentCursor == Cursor.NE_RESIZE
                    || currentCursor == Cursor.SE_RESIZE) {
                double newWidth = stage.getWidth() + dX;
                if (newWidth >= stage.getMinWidth()) {
                    stage.setWidth(newWidth);
                }
            }

            if (currentCursor == Cursor.W_RESIZE || currentCursor == Cursor.NW_RESIZE
                    || currentCursor == Cursor.SW_RESIZE) {
                double newWidth = stage.getWidth() - dX;
                if (newWidth >= stage.getMinWidth()) {
                    stage.setX(stage.getX() + dX);
                    stage.setWidth(newWidth);
                }
            }
        });
    }

    public void windowClose() {
        stage.close();
    }

    public void windowIconify() {
        stage.setIconified(!stage.isIconified());
    }

    public void windowMaximize() {
        maximized.set(!maximized.get());
    }
}
