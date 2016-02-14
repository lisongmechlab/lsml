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

import org.lisoft.lsml.view_fx.util.FxmlHelpers;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author Li Song
 */
public class WindowDecoration extends BorderPane {
    private final Stage  stage;
    private double       lastRelX;
    private double       lastRelY;

    private double       lastW;
    private double       lastH;
    private final double RESIZE_EDGE = 3.0;
    private Edge         edge;

    public static enum Edge {
        N, E, S, W, NE, NW, SE, SW, C;
    }

    public WindowDecoration(Stage aStage, Parent aRoot) {
        FxmlHelpers.loadFxmlControl(this);
        setCenter(aRoot);
        stage = aStage;

        setOnMouseMoved(e -> {
            lastRelX = e.getScreenX() - stage.getX();
            lastRelY = e.getScreenY() - stage.getY();
            lastH = stage.getHeight();
            lastW = stage.getWidth();

            if (lastRelY < RESIZE_EDGE) {
                if (lastRelX < RESIZE_EDGE) {
                    if (edge != Edge.NW) {
                        edge = Edge.NW;
                        stage.getScene().setCursor(Cursor.NW_RESIZE);
                    }
                }
                else if (lastRelX > lastW - RESIZE_EDGE) {
                    if (edge != Edge.NE) {
                        edge = Edge.NE;
                        stage.getScene().setCursor(Cursor.NE_RESIZE);
                    }
                }
                else {
                    if (edge != Edge.N) {
                        edge = Edge.N;
                        stage.getScene().setCursor(Cursor.N_RESIZE);
                    }
                }
            }
            else if (lastRelY > lastH - RESIZE_EDGE) {
                if (lastRelX < RESIZE_EDGE) {
                    if (edge != Edge.SW) {
                        edge = Edge.SW;
                        stage.getScene().setCursor(Cursor.SW_RESIZE);
                    }
                }
                else if (lastRelX > lastW - RESIZE_EDGE) {
                    if (edge != Edge.SE) {
                        edge = Edge.SE;
                        stage.getScene().setCursor(Cursor.SE_RESIZE);
                    }
                }
                else {
                    if (edge != Edge.S) {
                        edge = Edge.S;
                        stage.getScene().setCursor(Cursor.S_RESIZE);
                    }
                }
            }
            else {
                if (lastRelX < RESIZE_EDGE) {
                    if (edge != Edge.W) {
                        edge = Edge.W;
                        stage.getScene().setCursor(Cursor.W_RESIZE);
                    }
                }
                else if (lastRelX > lastW - RESIZE_EDGE) {

                    if (edge != Edge.E) {
                        edge = Edge.E;
                        stage.getScene().setCursor(Cursor.E_RESIZE);
                    }
                }
                else {
                    if (edge != Edge.C) {
                        edge = Edge.C;
                        stage.getScene().setCursor(Cursor.DEFAULT);
                    }
                }
            }

        });
        //
        // setOnMousePressed(e -> {
        // lastRelX = e.getScreenX() - stage.getX();
        // lastRelY = e.getScreenY() - stage.getY();
        // lastH = stage.getHeight();
        // lastW = stage.getWidth();
        //
        // if (lastRelY < RESIZE_EDGE) {
        // if (lastRelX < RESIZE_EDGE) {
        // edge = Edge.NW;
        // }
        // else if (lastRelX > lastW - RESIZE_EDGE) {
        // edge = Edge.NE;
        // }
        // else {
        // edge = Edge.N;
        // }
        // }
        // else if (lastRelY > lastH - RESIZE_EDGE) {
        // if (lastRelX < RESIZE_EDGE) {
        // edge = Edge.SW;
        // }
        // else if (lastRelX > lastW - RESIZE_EDGE) {
        // edge = Edge.SE;
        // }
        // else {
        // edge = Edge.S;
        // }
        // }
        // else {
        // if (lastRelX < RESIZE_EDGE) {
        // edge = Edge.W;
        // }
        // else if (lastRelX > lastW - RESIZE_EDGE) {
        // edge = Edge.E;
        // }
        // else {
        // edge = Edge.C;
        // }
        // }
        // });

        setOnMouseDragged(e -> {

            double relX = e.getScreenX() - stage.getX();
            double relY = e.getScreenY() - stage.getY();
            switch (edge) {
                case N:
                    stage.setY(e.getScreenY() - lastRelY);
                    break;
                case NE:
                    stage.setY(e.getScreenY() - lastRelY);
                    break;
                case NW:
                    stage.setY(e.getScreenY() - lastRelY);
                    break;
                case S:
                    break;
                case SE:
                    break;
                case SW:
                    break;
                case E:
                    break;
                case W:
                    stage.setWidth(lastW + relX - lastRelX);
                    break;
                case C: // Fall-through
                default:
                    stage.setX(e.getScreenX() - lastRelX);
                    stage.setY(e.getScreenY() - lastRelY);
                    break;
            }
        });
    }

    @FXML
    public void iconifyButton() {
        stage.setIconified(!stage.isIconified());
    }

    @FXML
    public void maximizeButton() {
        stage.setMaximized(!stage.isMaximized());
    }

    @FXML
    public void closeButton() {
        stage.close();
    }
}
