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
package org.lisoft.lsml.view_fx;

import org.lisoft.lsml.view_fx.util.FxControlUtils;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.StringProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Handles showing a splash screen on program startup.
 *
 * @author Emily Björk
 */
public class SplashScreen {
    private static SplashScreen instance;

    public static void closeSplash() {
        if (null != instance) {
            instance.dispose();
            instance = null;
        }
    }

    public static void setProcessText(String aString) {
        if (null != instance) {
            if (Platform.isFxApplicationThread()) {
                instance.progressText.setText(aString);
            }
            else {
                Platform.runLater(() -> {
                    setProcessText(aString);
                });
            }
        }
    }

    public static void showSplash(Stage aStage) {
        if (null == instance) {
            instance = new SplashScreen(aStage);
        }
    }

    public static StringProperty subTextProperty() {
        if (null != instance) {
            return instance.progressSubText.textProperty();
        }
        throw new IllegalStateException("Cannot get text property when splash has closed.");
    }

    private final Stage stage;

    private final VBox root = new VBox();

    private final Label progressText = new Label("Reading cached game data...");

    private final Label progressSubText = new Label("...");

    public SplashScreen(Stage aStage) {
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        final Image image = new Image(ClassLoader.getSystemClassLoader().getResourceAsStream("splash.png"));
        final ImageView splash = new ImageView(image);

        root.getChildren().setAll(splash, progressText, progressSubText);

        stage = aStage;
        stage.setTitle("Loading Li Song Mechlab...");
        stage.setX(bounds.getMinX() + bounds.getWidth() / 2 - image.getWidth() / 2);
        stage.setY(bounds.getMinY() + bounds.getHeight() / 2 - image.getHeight() / 2);
        FxControlUtils.setupStage(aStage, root, null, new ReadOnlyBooleanWrapper(false), null);
    }

    private void dispose() {
        final FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), root);
        fadeSplash.setFromValue(1.0);
        fadeSplash.setToValue(0.0);
        fadeSplash.setOnFinished(actionEvent -> stage.hide());
        fadeSplash.play();
    }
}
