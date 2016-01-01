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

import org.lisoft.lsml.view_fx.GarageController;
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
 * @author Emily Björk
 */
public class FxmlHelpers {
    public final static URL GARAGE_MECH_LIST_VIEW = GarageController.class
            .getResource("/org/lisoft/lsml/view_fx/GarageListView.fxml");

    public static void loadFxmlControl(Object aControl) throws IOException {
        String fxmlFile = aControl.getClass().getSimpleName() + ".fxml";
        FXMLLoader fxmlLoader = new FXMLLoader(aControl.getClass().getResource(fxmlFile));
        fxmlLoader.setControllerFactory((aClass) -> aControl);
        fxmlLoader.setRoot(aControl);
        fxmlLoader.load();
    }

    public static void polishStage(Stage aStage, Parent aRoot) {
        polishStage(aStage, new Scene(aRoot));
    }

    public static void polishStage(Stage aStage, Scene aScene) {
        aStage.setScene(aScene);
        aStage.sizeToScene();
        aStage.show();
        aStage.toFront();
        aStage.getIcons().add(new Image(LiSongMechLab.class.getResourceAsStream("/resources/icon.png")));
    }

    public static void bindTogglable(CheckBox aCheckBox, BooleanExpression aBooleanExpression,
            Predicate<Boolean> aSuccess) {
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
