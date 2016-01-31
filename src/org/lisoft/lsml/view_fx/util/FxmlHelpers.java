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
package org.lisoft.lsml.view_fx.util;

import java.io.IOException;
import java.util.function.Predicate;

import org.lisoft.lsml.view_fx.LiSongMechLab;

import javafx.beans.binding.BooleanExpression;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Assorted helper methods for dealing with FXML.
 * 
 * @author Li Song
 */
public class FxmlHelpers {
    public static void loadFxmlControl(Object aControl) {
        String fxmlFile = aControl.getClass().getSimpleName() + ".fxml";
        FXMLLoader fxmlLoader = new FXMLLoader(aControl.getClass().getResource(fxmlFile));
        fxmlLoader.setControllerFactory((aClass) -> aControl);
        fxmlLoader.setRoot(aControl);
        try {
            fxmlLoader.load();
        }
        catch (IOException e) {
            // Failure to load XML is a program error and cannot be recovered from, promote to unchecked.
            throw new RuntimeException(e);
        }
    }

    public static void createStage(Stage aStage, Parent aRoot) {
        // aRoot.getStyleClass().add(0, "root");
        // aStage.initStyle(StageStyle.UNDECORATED);
        // aStage.setScene(new Scene(new WindowDecoration(aStage, aRoot)));
        aStage.getIcons().add(new Image(LiSongMechLab.class.getResourceAsStream("/resources/icon.png")));
        aStage.setScene(new Scene(aRoot));
        aStage.sizeToScene();
        aStage.show();
        aStage.toFront();
    }

    /**
     * @param aCheckBox
     * @param aBooleanExpression
     * @param aSuccess
     */
    public static void bindTogglable(CheckBox aCheckBox, BooleanExpression aBooleanExpression,
            Predicate<Boolean> aSuccess) {
        aCheckBox.setSelected(aBooleanExpression.get());
        aBooleanExpression.addListener((aObservable, aOld, aNew) -> {
            aCheckBox.setSelected(aNew);
        });

        aCheckBox.setOnAction(e -> {
            boolean value = aCheckBox.isSelected();
            boolean oldValue = aBooleanExpression.get();
            if (value != oldValue && !aSuccess.test(value)) {
                aCheckBox.setSelected(oldValue);
            }
        });
    }

    public static void bindTogglable(ToggleButton aCheckBox, BooleanExpression aBooleanExpression,
            Predicate<Boolean> aSuccess) {
        aCheckBox.setSelected(aBooleanExpression.get());
        aBooleanExpression.addListener((aObservable, aOld, aNew) -> {
            aCheckBox.setSelected(aNew);
        });

        aCheckBox.setOnAction(e -> {
            boolean value = aCheckBox.isSelected();
            boolean oldValue = aBooleanExpression.get();
            if (value != oldValue && !aSuccess.test(value)) {
                aCheckBox.setSelected(oldValue);
            }
        });
    }
}
