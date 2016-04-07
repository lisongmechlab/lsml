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
package org.lisoft.lsml.view_fx;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

import org.lisoft.lsml.model.garage.Garage;
import org.lisoft.lsml.model.garage.GarageSerialiser;

import javafx.beans.property.Property;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

/**
 * This class wraps the application global garage state. In essence this is a singleton object which should be injected
 * through DI.
 * 
 * @author Li Song
 */
public class GlobalGarage {
    private final static ExtensionFilter LSML_EXT         = new ExtensionFilter("LSML Garage 1.0", "*.xml");
    private final static ExtensionFilter LSML_EXT2        = new ExtensionFilter("LSML Garage 2.0", "*.lsxml");

    private final Settings               settings         = Settings.getSettings();
    private final GarageSerialiser       garageSerialiser = new GarageSerialiser();
    private Garage                       garage;
    private File                         garageFile;

    // FIXME: Get rid of this when we start using Dagger
    public final static GlobalGarage     instance;

    static {
        instance = new GlobalGarage();
        try {
            instance.autoLoadLastGarage();
        }
        catch (IOException e) {
            LiSongMechLab.showError(null, e);

            boolean success = false;
            while (!success) {
                try {
                    instance.newGarage(null);
                    success = true;
                }
                catch (IOException e1) {
                    LiSongMechLab.showError(null, e1);
                }
            }
        }
    }

    /**
     * Creates a new empty garage and shows a common file dialog to the user to save it somewhere.
     * 
     * @param aOwnerWindow
     *            The window that is opening the new garage dialog.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void newGarage(Window aOwnerWindow) throws FileNotFoundException, IOException {
        garage = new Garage();
        writeGarageDialog("Create new garage...", aOwnerWindow);
    }

    /**
     * Opens an existing garage, will show a common dialog.
     * 
     * @param aOwnerWindow
     *            The window that is opening the open dialog.
     * @throws IOException
     */
    public void openGarage(Window aOwnerWindow) throws IOException {
        if (null != garage) {
            boolean saved = false;
            boolean cancel = false;
            while (!saved && !cancel) {
                Alert saveConfirm = new Alert(AlertType.CONFIRMATION, "Save current garage?");
                Optional<ButtonType> result = saveConfirm.showAndWait();
                if (result.isPresent()) {
                    if (ButtonType.OK == result.get()) {
                        if (null != garageFile) {
                            saved = saveGarageAs(aOwnerWindow);
                        }
                        else {
                            saveGarage();
                            saved = true;
                        }
                    }
                    else {
                        cancel = true;
                        saved = false;
                    }
                }
            }
        }

        FileChooser fileChooser = garageFileChooser("Open Garage");
        fileChooser.getExtensionFilters().add(LSML_EXT);
        File file = fileChooser.showOpenDialog(aOwnerWindow);

        if (null != file) {
            try (FileInputStream fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis);) {
                garage = garageSerialiser.load(bis, DefaultLoadoutErrorReporter.instance);
                garageFile = file;
                settings.getProperty(Settings.CORE_GARAGE_FILE, String.class).setValue(garageFile.getAbsolutePath());
            }
        }
    }

    public void saveGarage() throws IOException {
        if (null != garageFile) {
            writeGarage(garageFile);
        }
    }

    /**
     * Will save the current garage as a new file. If successful, the {@link Settings#CORE_GARAGE_FILE} property is
     * updated.
     * 
     * @param aOwnerWindow
     *            The window that is opening the save dialog.
     * 
     * @return <code>true</code> if the garage was written to a file, <code>false</code> otherwise.
     * @throws IOException
     */
    public boolean saveGarageAs(Window aOwnerWindow) throws IOException {
        return writeGarageDialog("Save garage as...", aOwnerWindow);
    }

    /**
     * @return the garage
     */
    public Garage getGarage() {
        return garage;
    }

    private void writeGarage(File file) throws IOException, FileNotFoundException {
        try (FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);) {
            garageSerialiser.save(bos, garage, DefaultLoadoutErrorReporter.instance);
            garageFile = file;
            Property<String> garageProp = settings.getProperty(Settings.CORE_GARAGE_FILE, String.class);
            garageProp.setValue(file.getAbsolutePath());
        }
    }

    private boolean writeGarageDialog(String aTitle, Window aOwnerWindow) throws IOException, FileNotFoundException {
        FileChooser fileChooser = garageFileChooser(aTitle);
        File file = fileChooser.showSaveDialog(aOwnerWindow);
        if (null != file && (!file.exists() || confirmOverwrite())) {
            writeGarage(file);
            return true;
        }
        return false;
    }

    private void autoLoadLastGarage() throws IOException {
        do {
            String garageFileName = settings.getProperty(Settings.CORE_GARAGE_FILE, String.class).getValue();
            garageFile = new File(garageFileName);
            if (garageFile.exists()) {
                try (FileInputStream fis = new FileInputStream(garageFile);
                        BufferedInputStream bis = new BufferedInputStream(fis);) {
                    garage = garageSerialiser.load(bis, DefaultLoadoutErrorReporter.instance);
                }
            }
            else {
                ButtonType openGarage = new ButtonType("Open Garage...");
                ButtonType newGarage = new ButtonType("New Garage...");
                ButtonType exit = new ButtonType("Exit", ButtonData.CANCEL_CLOSE);

                Alert alert = new Alert(AlertType.NONE);
                alert.setTitle("Select Garage...");
                alert.setHeaderText("Please select or create a new garage to use.");
                alert.setContentText("LSML stores your 'Mechs and Drop Ships in a 'garage'. "
                        + "Your garage is automatically loaded when you open"
                        + " LSML and automatically saved when you close LSML.");
                alert.getButtonTypes().setAll(newGarage, openGarage, exit);
                Optional<ButtonType> selection = alert.showAndWait();
                if (selection.isPresent()) {
                    if (openGarage == selection.get()) {
                        openGarage(null);
                    }
                    else if (newGarage == selection.get()) {
                        newGarage(null);
                    }
                    else {
                        System.exit(0);
                    }
                }
                else {
                    System.exit(0);
                }
            }
        } while (garageFile == null || !garageFile.exists());
    }

    private boolean confirmOverwrite() {
        Alert confirmOverwrite = new Alert(AlertType.CONFIRMATION, "Overwrite selected garage?");
        Optional<ButtonType> result = confirmOverwrite.showAndWait();
        return result.isPresent() && ButtonType.OK != result.get();
    }

    private FileChooser garageFileChooser(String aTitle) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(aTitle);
        fileChooser.getExtensionFilters().addAll(LSML_EXT2);

        if (null != garageFile && garageFile.exists()) {
            fileChooser.setInitialDirectory(garageFile);
        }
        else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }
        return fileChooser;
    }

}
