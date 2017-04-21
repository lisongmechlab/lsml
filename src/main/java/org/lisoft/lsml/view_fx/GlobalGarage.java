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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.command.CmdGarageAddDirectory;
import org.lisoft.lsml.command.CmdGarageMultiRemove;
import org.lisoft.lsml.command.CmdGarageRemove;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.Garage;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.garage.GaragePath;
import org.lisoft.lsml.model.garage.GarageSerialiser;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.controls.LsmlAlert;

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
 * FIXME: The whole garage system is a right mess and should be rewritten at some point.
 *
 * @author Emily Björk
 */
@Singleton
public class GlobalGarage {
    /**
     *
     */
    private static final String DEFAULT_NEW_FOLDER_NAME = "New Folder";
    private final static ExtensionFilter LSML_EXT = new ExtensionFilter("LSML Garage 1.0", "*.xml");
    private final static ExtensionFilter LSML_EXT2 = new ExtensionFilter("LSML Garage 2.0", "*.lsxml");

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

        final GarageDirectory<T> newDir = new GarageDirectory<>(DEFAULT_NEW_FOLDER_NAME);
        LiSongMechLab.safeCommand(aOwner, aStack, new CmdGarageAddDirectory<>(aXBar, path, newDir), aXBar);
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

        final LsmlAlert alert = new LsmlAlert(aOwner, Alert.AlertType.CONFIRMATION);
        alert.setContentText("Are you sure you want to delete: " + path.toString());
        alert.showAndWait().ifPresent(aButton -> {
            if (aButton == ButtonType.OK) {
                LiSongMechLab.safeCommand(aOwner, aStack, new CmdGarageRemove<>(aXBar, path), aXBar);
            }
        });
    }

    public static <T extends NamedObject> void remove(List<GaragePath<T>> aPaths, Node aOwner, CommandStack aStack,
            MessageDelivery aXBar) {
        final LsmlAlert alert = new LsmlAlert(aOwner, Alert.AlertType.CONFIRMATION);
        final StringBuilder sb = new StringBuilder();
        final List<GaragePath<T>> paths = aPaths.stream().filter(path -> path != null && !path.isRoot())
                .collect(Collectors.toList());
        sb.append("Are you sure you want to delete: \n");
        for (final GaragePath<T> garagePath : paths) {
            garagePath.toPath(sb);
            sb.append('\n');
        }
        alert.setContentText(sb.toString());
        alert.showAndWait().ifPresent(aButton -> {
            if (aButton == ButtonType.OK) {
                LiSongMechLab.safeCommand(aOwner, aStack, new CmdGarageMultiRemove<>(aXBar, aPaths), aXBar);
            }
        });
    }

    private final Settings settings;
    private final GarageSerialiser garageSerialiser;
    private final ErrorReporter errorReporter;

    private Garage garage;
    private File garageFile;
    private GaragePath<Loadout> defaultSaveTo;

    @Inject
    public GlobalGarage(Settings aSettings, ErrorReporter aErrorReporter, GarageSerialiser aGarageSerialiser) {
        settings = aSettings;
        garageSerialiser = aGarageSerialiser;
        errorReporter = aErrorReporter;
    }

    /**
     * Performs a very insistent save to make sure that user data is not lost. Typically used when the application
     * exits.
     */
    public void exitSave() {
        try {
            saveGarage();
        }
        catch (final IOException e) {
            errorReporter.error("Unable to save garage", "LSML was unable to write to: " + garageFile.getAbsolutePath(),
                    e);

            boolean successfull = false;
            while (!successfull) {
                try {
                    saveGarageAs(null);
                    successfull = true;
                }
                catch (final IOException e1) {
                    errorReporter.error("Unable to save garage",
                            "LSML was unable to write to: " + garageFile.getAbsolutePath()
                                    + ". Please check write permissions or try another location.",
                            e1);
                }
            }
        }
    }

    /**
     * @return The default folder to save new loadouts to, as set by {@link #setDefaultSaveToFolder(GaragePath)}. If no
     *         folder has been set, will return the root folder.
     */
    public GaragePath<Loadout> getDefaultSaveTo() {
        if (defaultSaveTo != null) {
            return defaultSaveTo;
        }
        return new GaragePath<>(garage.getLoadoutRoot());
    }

    /**
     * @return the garage
     */
    public Garage getGarage() {
        return garage;
    }

    /**
     * Makes sure that a garage is available by either loading the previously opened garage or creating a new one.
     *
     * @param aOwner
     *            A {@link Node} that is used for positioning dialog boxes spawned in the process.
     * @return <code>true</code> if a garage file is present. If this returns <code>false</code>, the application should
     *         terminate in an orderly manner.
     */
    public boolean loadLastOrNew(Node aOwner) {
        if (!autoLoadLastGarage(aOwner)) {
            try {
                if (!newGarage(null)) {
                    return false;
                }
            }
            catch (final IOException e1) {
                errorReporter.error("Unable to create new garage", "LSML needs a garage to function", e1);
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a new empty garage and shows a common file dialog to the user to save it somewhere.
     *
     * @param aOwnerWindow
     *            The window that is opening the new garage dialog.
     * @throws FileNotFoundException
     *             if the file given by the user for some reason couldn't be found.
     * @throws IOException
     *             if the file given by the user couldn't be written.
     * @return <code>true</code> if a new garage was created and written.
     */
    public boolean newGarage(Window aOwnerWindow) throws FileNotFoundException, IOException {
        garage = new Garage();
        return writeGarageDialog("Create new garage...", aOwnerWindow);
    }

    /**
     * Opens an existing garage, will show a common dialog.
     *
     * @param aOwnerWindow
     *            The window that is opening the open dialog.
     */
    public void openGarage(Window aOwnerWindow) {
        if (null != garage) {
            try {
                boolean saved = false;
                boolean cancel = false;
                while (!saved && !cancel) {
                    final LsmlAlert saveConfirm = new LsmlAlert(aOwnerWindow, AlertType.CONFIRMATION,
                            "Save current garage?");
                    final Optional<ButtonType> result = saveConfirm.showAndWait();
                    if (result.isPresent()) {
                        if (ButtonType.OK == result.get()) {
                            if (null == garageFile) {
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
            catch (final IOException e) {
                errorReporter.error("Unable to save currently open garage",
                        "LSML was unable to save to current garage, to prevent data loss the load operation was cancelled.",
                        e);
            }
        }

        final FileChooser fileChooser = garageFileChooser("Open Garage");
        fileChooser.getExtensionFilters().add(LSML_EXT);
        final File file = fileChooser.showOpenDialog(aOwnerWindow);

        if (null != file) {
            try (FileInputStream fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis);) {
                garage = garageSerialiser.load(bis);
                garageFile = file;
                settings.getString(Settings.CORE_GARAGE_FILE).setValue(garageFile.getAbsolutePath());
            }
            catch (final Exception e) {
                errorReporter.error("Unable to open garage",
                        "LSML was unable to open the selected garage file: " + file.getAbsolutePath(), e);
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
     *             if the file given by the user couldn't be written.
     */
    public boolean saveGarageAs(Window aOwnerWindow) throws IOException {
        return writeGarageDialog("Save garage as...", aOwnerWindow);
    }

    /**
     * Sets the default folder to save new loadouts to.
     *
     * @param aDirectory
     *            The folder to save to.
     */
    public void setDefaultSaveToFolder(GaragePath<Loadout> aDirectory) {
        defaultSaveTo = aDirectory;
    }

    private boolean autoLoadLastGarage(Node aOwner) {
        if (!Platform.isFxApplicationThread()) {
            throw new RuntimeException("Autoload garage wasn't called on the FX application thread!");
        }
        do {
            final String garageFileName = settings.getString(Settings.CORE_GARAGE_FILE).getValue();
            garageFile = new File(garageFileName);
            if (garageFile.exists()) {
                try (FileInputStream fis = new FileInputStream(garageFile);
                        BufferedInputStream bis = new BufferedInputStream(fis);) {
                    garage = garageSerialiser.load(bis);
                }
                catch (final Exception e) {
                    errorReporter.error("Unable to open garage",
                            "Could not read from file: " + garageFile.getAbsolutePath(), e);
                    return false;
                }
            }
            else {
                final ButtonType openGarage = new ButtonType("Open Garage...");
                final ButtonType newGarage = new ButtonType("New Garage...");
                final ButtonType exit = new ButtonType("Exit", ButtonData.CANCEL_CLOSE);

                final LsmlAlert alert = new LsmlAlert(aOwner, AlertType.NONE);
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
                        try {
                            newGarage(null);
                        }
                        catch (final Exception e) {
                            errorReporter.error("Unable create new garage", "Could not write to file", e);
                            return false;
                        }
                    }
                    else {
                        return false;
                    }
                }
                else {
                    return false;
                }
            }
        } while (garageFile == null || !garageFile.exists());
        return garage != null;
    }

    private boolean confirmOverwrite(Window aOwnerWindow) {
        final LsmlAlert confirmOverwrite = new LsmlAlert(aOwnerWindow, AlertType.CONFIRMATION,
                "Overwrite selected garage?");
        final Optional<ButtonType> result = confirmOverwrite.showAndWait();
        return result.isPresent() && ButtonType.OK != result.get();
    }

    private FileChooser garageFileChooser(String aTitle) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(aTitle);
        fileChooser.getExtensionFilters().addAll(LSML_EXT2);

        if (null != garageFile && garageFile.exists()) {
            fileChooser.setInitialDirectory(garageFile.getParentFile());
        }
        else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }
        return fileChooser;
    }

    private void writeGarage(File file) throws IOException, FileNotFoundException {
        try (FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);) {
            garageSerialiser.save(bos, garage);
            garageFile = file;
            final Property<String> garageProp = settings.getString(Settings.CORE_GARAGE_FILE);
            garageProp.setValue(file.getAbsolutePath());
        }
    }

    private boolean writeGarageDialog(String aTitle, Window aOwnerWindow) throws IOException, FileNotFoundException {
        final FileChooser fileChooser = garageFileChooser(aTitle);
        final File file = fileChooser.showSaveDialog(aOwnerWindow);
        if (null != file && (!file.exists() || confirmOverwrite(aOwnerWindow))) {
            writeGarage(file);
            return true;
        }
        return false;
    }

}
