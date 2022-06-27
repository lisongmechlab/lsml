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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.lisoft.lsml.messages.ApplicationMessage;
import org.lisoft.lsml.messages.ApplicationMessage.Type;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.view_fx.Settings;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

/**
 * This is an abstract base class for Controllers that are at the root of the stage and have custom window decorations.
 *
 * @author Li Song
 */
public abstract class AbstractFXStageController extends AbstractFXController implements MessageReceiver {
    /**
     * How many pixels high the grab area for moving the window at the top is if the titleBar region is not specified.
     */
    private static final double DEFAULT_MOVE_AREA_HEIGHT = 20;
    /**
     * A new CSS pseudo class for maximised windows.
     */
    private static final PseudoClass PC_MAXIMISED = PseudoClass.getPseudoClass("maximized");
    /**
     * How many pixels thick the edge around the window border where you can click and resize the window is.
     */
    private static final double RESIZE_EDGE = 2.0;
    protected final MessageXBar globalXBar;
    protected final Settings settings;
    private final BooleanProperty maximized = new SimpleBooleanProperty(false);
    private Cursor currentCursor = Cursor.DEFAULT;
    private double mousePrevMouseAbsX;
    private double mousePrevMouseAbsY;
    private Rectangle2D savedBounds = null;
    private LSMLStage stage;
    @FXML
    private Region titleBar;

    public AbstractFXStageController(Settings aSettings, MessageXBar aXBar) {
        settings = aSettings;
        globalXBar = aXBar;
        maximized.addListener((aObs, aOld, aNew) -> {
            final Rectangle2D newBounds;
            if (aNew) {
                final ObservableList<Screen> screensForRectangle = Screen.getScreensForRectangle(mousePrevMouseAbsX,
                                                                                                 mousePrevMouseAbsY, 1,
                                                                                                 1);
                final Screen screen = screensForRectangle.get(0);

                newBounds = screen.getVisualBounds();
                savedBounds = new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
            } else {
                newBounds = savedBounds;
                savedBounds = null;
            }

            stage.setX(newBounds.getMinX());
            stage.setY(newBounds.getMinY());
            stage.setWidth(newBounds.getWidth());
            stage.setHeight(newBounds.getHeight());
            root.pseudoClassStateChanged(PC_MAXIMISED, aNew);
        });

        if (null != globalXBar) {
            globalXBar.attach(this);
        }
    }

    public Stage createStage(Window aOptionalOwner) {
        stage = new LSMLStage(this, aOptionalOwner, settings);
        onShow(stage);
        return stage;
    }

    public void onMouseClicked(MouseEvent e) {
        if (FxControlUtils.isDoubleClick(e)) {
            final Insets padding = root.getPadding();
            if (e.getScreenY() - stage.getY() < getResizeAreaHeight() + padding.getTop()) {
                windowMaximize();
            }
        }
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

        //System.out.println(String.format("[%f, %f]",newMouseAbsX, newMouseAbsY));

        if (currentCursor == Cursor.MOVE) {
            stage.setX(stage.getX() + dX);
            stage.setY(stage.getY() + dY);
        }

        if (currentCursor == Cursor.N_RESIZE || currentCursor == Cursor.NE_RESIZE ||
            currentCursor == Cursor.NW_RESIZE) {
            final double newHeight = stage.getHeight() - dY;
            if (newHeight >= stage.getMinHeight()) {
                stage.setY(stage.getY() + dY);
                stage.setHeight(newHeight);
            }
        }

        if (currentCursor == Cursor.S_RESIZE || currentCursor == Cursor.SE_RESIZE ||
            currentCursor == Cursor.SW_RESIZE) {
            final double newHeight = stage.getHeight() + dY;
            if (newHeight >= stage.getMinHeight()) {
                stage.setHeight(newHeight);
            }
        }

        if (currentCursor == Cursor.E_RESIZE || currentCursor == Cursor.NE_RESIZE ||
            currentCursor == Cursor.SE_RESIZE) {
            final double newWidth = stage.getWidth() + dX;
            if (newWidth >= stage.getMinWidth()) {
                stage.setWidth(newWidth);
            }
        }

        if (currentCursor == Cursor.W_RESIZE || currentCursor == Cursor.NW_RESIZE ||
            currentCursor == Cursor.SW_RESIZE) {
            final double newWidth = stage.getWidth() - dX;
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
        final boolean topMoveEdge = relY <= getResizeAreaHeight();
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
                if (leftEdge) {
                    newCursor = Cursor.NW_RESIZE;
                } else if (rightEdge) {
                    newCursor = Cursor.NE_RESIZE;
                } else if (topEdge) {
                    newCursor = Cursor.N_RESIZE;
                } else {
                    newCursor = Cursor.MOVE;
                }
            } else if (bottomEdge) {
                if (leftEdge) {
                    newCursor = Cursor.SW_RESIZE;
                } else if (rightEdge) {
                    newCursor = Cursor.SE_RESIZE;
                } else {
                    newCursor = Cursor.S_RESIZE;
                }
            } else {
                if (leftEdge) {
                    newCursor = Cursor.W_RESIZE;
                } else if (rightEdge) {
                    newCursor = Cursor.E_RESIZE;
                } else {
                    newCursor = Cursor.DEFAULT;
                }
            }
        } else {
            newCursor = Cursor.DEFAULT;
        }

        if (currentCursor != newCursor) {
            root.setCursor(newCursor);
            currentCursor = newCursor;
        }
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof ApplicationMessage) {
            final ApplicationMessage msg = (ApplicationMessage) aMsg;
            if (msg.getType() == Type.CLOSE_OVERLAY) {
                closeOverlay(msg.getOrigin());
            }
        }
    }

    @FXML
    public void windowClose() {
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        // stage.close();
    }

    @FXML
    public void windowIconify() {
        stage.setIconified(!stage.isIconified());
    }

    @FXML
    public void windowMaximize() {
        maximized.set(!maximized.get());
    }

    protected void closeOverlay(final AbstractFXController aOverlayController) {
        closeOverlay(aOverlayController.getView());
    }

    protected void closeOverlay(final Node aOverlayRoot) {
        final Pane container = (Pane) root;
        container.getChildren().remove(aOverlayRoot);
        for (final Node node : container.getChildren()) {
            node.setDisable(false);
        }
    }

    protected Stage getStage() {
        return stage;
    }

    protected boolean isOverlayOpen(final AbstractFXController aOverlayController) {
        return ((Pane) root).getChildren().contains(aOverlayController.getView());
    }

    abstract protected void onShow(LSMLStage aStage);

    protected void openOverlay(AbstractFXController aOverlayController, boolean aDisableRoot) {
        final Region overlay = aOverlayController.getView();
        final Pane container = (Pane) root;
        if (!container.getChildren().contains(overlay)) {
            for (final Node node : container.getChildren()) {
                node.setDisable(aDisableRoot);
            }
            container.getChildren().add(overlay);
        }
    }

    private double getResizeAreaHeight() {
        if (null != titleBar) {
            return titleBar.getHeight();
        }
        return DEFAULT_MOVE_AREA_HEIGHT;
    }

    private void restoreCursorDefault() {
        if (currentCursor != Cursor.DEFAULT) {
            root.setCursor(Cursor.DEFAULT);
            currentCursor = Cursor.DEFAULT;
        }
    }
}
