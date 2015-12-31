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

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.lisoft.lsml.model.garage.MechGarage;
import org.lisoft.lsml.model.item.Equipment;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.view.ProgramInit;
import org.lisoft.lsml.view.SplashScreen;
import org.lisoft.lsml.view_fx.controls.FixedRowsListView;
import org.lisoft.lsml.view_fx.loadout.LoadoutWindow;
import org.lisoft.lsml.view_fx.loadout.component.ComponentPane;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.lsml.view_fx.util.FxmlHelpers;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * This is the main application for the LSML JavaFX GUI.
 * 
 * @author Emily Björk
 */
public class LiSongMechLab extends Application {

    private static ObservableList<String> active_style_sheets;

    public static final DataFormat        ITEM_DATA_FORMAT   = new DataFormat("item.custom");
    public static final DataFormat        MODULE_DATA_FORMAT = new DataFormat("module.custom");

    public static boolean safeCommand(CommandStack aStack, Command aCommand) {
        try {
            aStack.pushAndApply(aCommand);
        }
        catch (Exception e) {
            LiSongMechLab.showError(e);
            return false;
        }
        return true;
    }

    public static void addEquipmentDrag(Dragboard aDragboard, Equipment aItem) {
        // Pack the data
        ClipboardContent cc = new ClipboardContent();
        cc.putString(Integer.toString(aItem.getMwoId()));
        aDragboard.setContent(cc);

        // Create an off-screen scene and add a label representing our item.
        Label label = new Label(aItem.getName());
        label.getStyleClass().add(StyleManager.CSS_CLASS_EQUIPPED);
        StyleManager.changeStyle(label, aItem);
        if (aItem instanceof Item) {
            label.setPrefHeight(FixedRowsListView.DEFAULT_HEIGHT * ((Item) aItem).getNumCriticalSlots());
        }
        else {
            label.setPrefHeight(FixedRowsListView.DEFAULT_HEIGHT);
        }
        label.setPrefWidth(ComponentPane.ITEM_WIDTH);
        Scene scene = new Scene(label);
        scene.getStylesheets().setAll(active_style_sheets);

        // Take a snapshot of the scene using transparent as the background fill
        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(Color.TRANSPARENT);
        aDragboard.setDragView(label.snapshot(sp, null));
    }

    public static void openLoadout(LoadoutBase<?> aLoadout, MechGarage aGarage) throws IOException {
        Stage stage = new Stage();
        LoadoutWindow root = new LoadoutWindow(aLoadout, aGarage, stage);
        FxmlHelpers.polishStage(stage, root);
    }

    public static void showError(Exception aException) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR, aException.getMessage(), ButtonType.CLOSE);
            alert.getDialogPane().getStylesheets().addAll(active_style_sheets);
            alert.showAndWait();
        });
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());
        ProgramInit.setAppUserModelID();
        ProgramInit.checkCliArguments(args);
        ProgramInit.checkForUpdates();
        SplashScreen.showSplash();
        try {
            long startTimeMs = new Date().getTime();
            ProgramInit.loadGameFiles();
            long endTimeMs = new Date().getTime();
            long sleepTimeMs = Math.max(0, ProgramInit.MIN_SPLASH_TIME_MS - (endTimeMs - startTimeMs));
            Thread.sleep(sleepTimeMs);
        }
        catch (Exception e) {
            System.exit(1);
        }
        finally {
            SplashScreen.closeSplash();
        }

        try (InputStream rajdReg = LiSongMechLab.class.getResourceAsStream("/resources/Rajdhani-Medium.ttf");
                InputStream isBold = LiSongMechLab.class.getResourceAsStream("/resources/Quark-Bold.otf");
                InputStream isLight = LiSongMechLab.class.getResourceAsStream("/resources/Quark-Light.otf");) {
            Font.loadFont(isBold, 0);
            Font.loadFont(isLight, 0);
            Font.loadFont(rajdReg, 0);
        }
        catch (IOException e) {
            showError(e);
        }

        launch(args);
    }

    @Override
    public void start(Stage aStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/org/lisoft/lsml/view_fx/GarageWindow.fxml"));
        active_style_sheets = root.getStylesheets();
        aStage.setTitle("Li Song Mechlab");
        FxmlHelpers.polishStage(aStage, root);
    }

}
