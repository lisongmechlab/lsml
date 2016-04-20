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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

import org.lisoft.lsml.command.CmdAddGarageDirectory;
import org.lisoft.lsml.command.CmdRemoveFromGarage;
import org.lisoft.lsml.command.CmdRemoveGarageDirectory;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.Garage;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.garage.GaragePath;
import org.lisoft.lsml.model.garage.GarageSerialiser;
import org.lisoft.lsml.util.CommandStack;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.scene.Node;
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
 * @author Emily Björk
 */
public class GlobalGarage {
    private final static ExtensionFilter LSML_EXT = new ExtensionFilter("LSML Garage 1.0", "*.xml");
    private final static ExtensionFilter LSML_EXT2 = new ExtensionFilter("LSML Garage 2.0", "*.lsxml");

    // FIXME: Get rid of this when we start using Dagger
    public final static GlobalGarage instance;
    static {
        instance = new GlobalGarage();
        try {
            instance.autoLoadLastGarage();
        }
        catch (final IOException e) {
            LiSongMechLab.showError(null, e);

            boolean success = false;
            while (!success) {
                try {
                    instance.newGarage(null);
                    success = true;
                }
                catch (final IOException e1) {
                    LiSongMechLab.showError(null, e1);
                }
            }
        }
    }

    /**
     * Adds a new folder under the given path. The folder gets a default name.
     *
     * @param path
     *            The path to add the folder to. Must not be <code>null</code>.
     * @param aOwner
     *            The node that is initiating the request (for positioning dialogue)
     * @param aStack
     *            A {@link CommandStack} to execute commands through.
     * @param aXBar
     *            A {@link MessageDelivery} to send messages to.
     */
    public static <T extends NamedObject> void addFolder(GaragePath<T> path, Node aOwner, CommandStack aStack,
            MessageDelivery aXBar) {
        if (path.isLeaf()) {
            return;
        }

        final GarageDirectory<T> parent = path.getTopDirectory();
        LiSongMechLab.safeCommand(aOwner, aStack,
                new CmdAddGarageDirectory<>(aXBar, new GarageDirectory<>("New Folder"), parent));
    }

    /**
     * Removes the garage object denoted by the given path from the garage (which is identified by the path).
     *
     * @param path
     *            The path to remove. If <code>null</code> or <code>root</code> then this is a no-op.
     * @param aOwner
     *            The node that is initiating the request (for positioning dialogue)
     * @param aStack
     *            A {@link CommandStack} to execute commands through.
     * @param aXBar
     *            A {@link MessageDelivery} to send messages to.
     */
    public static <T extends NamedObject> void remove(GaragePath<T> path, Node aOwner, CommandStack aStack,
            MessageDelivery aXBar) {
        if (path == null || path.isRoot()) {
            return;
        }

        final GarageDirectory<T> dir = path.getTopDirectory();
        if (path.isLeaf()) {
            final T value = path.getValue().get();
            remove(aOwner, aStack, aXBar, dir, value);
        }
        else {
            final GarageDirectory<T> parent = path.getParentDirectory();

            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText("Are you sure you want to delete the folder: " + dir.getName());
            alert.showAndWait().ifPresent(aButton -> {
                if (aButton == ButtonType.OK) {
                    LiSongMechLab.safeCommand(aOwner, aStack, new CmdRemoveGarageDirectory<>(aXBar, dir, parent));
                }
            });
        }
    }

    public static <T extends NamedObject> void remove(Node aOwner, CommandStack aStack, MessageDelivery aXBar,
            GarageDirectory<T> dir, T value) {
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("Are you sure you want to delete the loadout: " + value.getName());
        alert.showAndWait().ifPresent(aButton -> {
            if (aButton == ButtonType.OK) {
                LiSongMechLab.safeCommand(aOwner, aStack, new CmdRemoveFromGarage<>(aXBar, dir, value));
            }
        });
    }

    private final Settings settings = Settings.getSettings();

    private final GarageSerialiser garageSerialiser = new GarageSerialiser();

    private Garage garage;

    private File garageFile;

    /**
     * @return the garage
     */
    public Garage getGarage() {
        return garage;
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
                final Alert saveConfirm = new Alert(AlertType.CONFIRMATION, "Save current garage?");
                final Optional<ButtonType> result = saveConfirm.showAndWait();
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

        final FileChooser fileChooser = garageFileChooser("Open Garage");
        fileChooser.getExtensionFilters().add(LSML_EXT);
        final File file = fileChooser.showOpenDialog(aOwnerWindow);

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

    private void autoLoadLastGarage() throws IOException {
        if (!Platform.isFxApplicationThread()) {
            throw new RuntimeException("Autoload garage wasn't called on the FX application thread!");
        }
        do {
            final String garageFileName = settings.getProperty(Settings.CORE_GARAGE_FILE, String.class).getValue();
            garageFile = new File(garageFileName);
            if (garageFile.exists()) {
                try (FileInputStream fis = new FileInputStream(garageFile);
                        BufferedInputStream bis = new BufferedInputStream(fis);) {
                    garage = garageSerialiser.load(bis, DefaultLoadoutErrorReporter.instance);
                }
            }
            else {
                final ButtonType openGarage = new ButtonType("Open Garage...");
                final ButtonType newGarage = new ButtonType("New Garage...");
                final ButtonType exit = new ButtonType("Exit", ButtonData.CANCEL_CLOSE);

                final Alert alert = new Alert(AlertType.NONE);
                alert.setTitle("Select Garage...");
                alert.setHeaderText("Please select or create a new garage to use.");
                alert.setContentText("LSML stores your 'Mechs and Drop Ships in a 'garage'. "
                        + "Your garage is automatically loaded when you open"
                        + " LSML and automatically saved when you close LSML.");
                alert.getButtonTypes().setAll(newGarage, openGarage, exit);
                final Optional<ButtonType> selection = alert.showAndWait();
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
        final Alert confirmOverwrite = new Alert(AlertType.CONFIRMATION, "Overwrite selected garage?");
        final Optional<ButtonType> result = confirmOverwrite.showAndWait();
        return result.isPresent() && ButtonType.OK != result.get();
    }

    private FileChooser garageFileChooser(String aTitle) {
        final FileChooser fileChooser = new FileChooser();
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

    private void writeGarage(File file) throws IOException, FileNotFoundException {
        try (FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);) {
            garageSerialiser.save(bos, garage, DefaultLoadoutErrorReporter.instance);
            garageFile = file;
            final Property<String> garageProp = settings.getProperty(Settings.CORE_GARAGE_FILE, String.class);
            garageProp.setValue(file.getAbsolutePath());
        }
    }

    private boolean writeGarageDialog(String aTitle, Window aOwnerWindow) throws IOException, FileNotFoundException {
        final FileChooser fileChooser = garageFileChooser(aTitle);
        final File file = fileChooser.showSaveDialog(aOwnerWindow);
        if (null != file && (!file.exists() || confirmOverwrite())) {
            writeGarage(file);
            return true;
        }
        return false;
    }

}
