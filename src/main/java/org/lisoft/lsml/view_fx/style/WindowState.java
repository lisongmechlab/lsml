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
package org.lisoft.lsml.view_fx.style;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * This class will replace the window decorations on the given {@link Region}.
 * 
 * The following are needed for the decoration to work:
 * <ul>
 * <li>A root pane - The paddings are obtained from this pane to</li>
 * </ul>
 * 
 * The {@link Region} will have CSS pseudoclass "maximized" when it is maximized. Typically one should style it like so:
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
 * @author Emily Björk
 */
public class WindowState {
    private final static PseudoClass PC_MAXIMIZED = PseudoClass.getPseudoClass("maximized");
    private final static double MOVE_EDGE = 20.0;
    private final static double RESIZE_EDGE = 2.0;
    private final Stage stage;
    private final BooleanProperty maximized = new SimpleBooleanProperty(false);

    private double mousePrevMouseAbsX;
    private double mousePrevMouseAbsY;
    private Rectangle2D savedBounds = null;
    private Cursor currentCursor = Cursor.DEFAULT;
    private Region root;

    public WindowState(Stage aStage, Region aSceneRoot) {
        stage = aStage;
        root = aSceneRoot;
        maximized.addListener((aObs, aOld, aNew) -> {
            final Rectangle2D newBounds;
            if (aNew) {

                ObservableList<Screen> screensForRectangle = Screen.getScreensForRectangle(mousePrevMouseAbsX,
                        mousePrevMouseAbsY, stage.getWidth(), stage.getHeight());
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
            root.pseudoClassStateChanged(PC_MAXIMIZED, aNew);
        });
    }

    public void onMouseDragged(MouseEvent e) {
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
    }

    public void onMouseMoved(MouseEvent e) {
        if (maximized.getValue()) {
            restoreCursorDefault();
            return;
        }

        final Insets padding = root.getPadding();
        final double relX = e.getScreenX() - stage.getX() - padding.getLeft();
        final double relY = e.getScreenY() - stage.getY() - padding.getTop();
        final double h = stage.getHeight() - (padding.getBottom() + padding.getTop());
        final double w = stage.getWidth() - (padding.getLeft() + padding.getRight());

        mousePrevMouseAbsX = e.getScreenX();
        mousePrevMouseAbsY = e.getScreenY();

        final boolean inside = relX >= 0.0 && relY >= 0.0 && relX < w && relY < h;
        final boolean topEdge = relY <= RESIZE_EDGE;
        final boolean topMoveEdge = relY <= MOVE_EDGE;
        final boolean bottomEdge = relY >= h - RESIZE_EDGE;
        final boolean leftEdge = relX <= RESIZE_EDGE;
        final boolean rightEdge = relX >= w - RESIZE_EDGE;

        // Give priority to controls under the mouse
        if (topMoveEdge && inside) {
            Node nodeUnderMouse = e.getPickResult().getIntersectedNode();
            while (null != nodeUnderMouse && !(nodeUnderMouse instanceof Control)) {
                nodeUnderMouse = nodeUnderMouse.getParent();
            }

            if (null != nodeUnderMouse) {
                restoreCursorDefault();
                return;
            }
        }

        final Cursor newCursor;
        if (inside) {
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
        }
        else {
            newCursor = Cursor.DEFAULT;
        }

        if (currentCursor != newCursor) {
            root.setCursor(newCursor);
            currentCursor = newCursor;
        }
    }

    public void onMouseClicked(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
            final Insets padding = root.getPadding();
            if (e.getScreenY() - stage.getY() < MOVE_EDGE + padding.getTop()) {
                windowMaximize();
            }
        }
    }

    public void restoreCursorDefault() {
        if (currentCursor != Cursor.DEFAULT) {
            root.setCursor(Cursor.DEFAULT);
            currentCursor = Cursor.DEFAULT;
        }
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
