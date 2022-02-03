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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class wraps the application global garage state. In essence this is a singleton object which should be injected
 * through DI.
 * <p>
 * FIXME: The whole garage system is a right mess and should be rewritten at some point.
 *
 * @author Li Song
 */
@Singleton
public class GlobalGarage {
    private static final String DEFAULT_NEW_FOLDER_NAME = "New Folder";
    private static final ExtensionFilter LSML_EXT = new ExtensionFilter("LSML Garage 1.0", "*.xml");
    private static final ExtensionFilter LSML_EXT2 = new ExtensionFilter("LSML Garage 2.0", "*.lsxml");
    private final ErrorReporter errorReporter;
    private final GarageSerialiser garageSerialiser;
    private final Settings settings;
    private File autoSaveGarageFile;
    private GaragePath<Loadout> defaultSaveTo;
    private Garage garage;
    private File garageFile;

    @Inject
    public GlobalGarage(Settings aSettings, ErrorReporter aErrorReporter, GarageSerialiser aGarageSerializer) {
        settings = aSettings;
        garageSerialiser = aGarageSerializer;
        errorReporter = aErrorReporter;
    }

    /**
     * Adds a new folder under the given path. The folder gets a default name.
     *
     * @param path   The path to add the folder to. Must not be <code>null</code>.
     * @param aOwner The node that is initiating the request (for positioning dialogue)
     * @param aStack A {@link CommandStack} to execute commands through.
     * @param aXBar  A {@link MessageDelivery} to send messages to.
     */
    static public <T extends NamedObject> void addFolder(GaragePath<T> path, Node aOwner, CommandStack aStack,
                                                         MessageDelivery aXBar) {
        if (path.isLeaf()) {
            return;
        }

        final GarageDirectory<T> newDir = new GarageDirectory<>(DEFAULT_NEW_FOLDER_NAME);
        LiSongMechLab.safeCommand(aOwner, aStack, new CmdGarageAddDirectory<>(aXBar, path, newDir), aXBar);
    }

    static public <T extends NamedObject> void remove(List<GaragePath<T>> aPaths, Node aOwner, CommandStack aStack,
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

    /**
     * Removes the garage object denoted by the given path from the garage (which is identified by the path).
     *
     * @param path   The path to remove. If <code>null</code> or <code>root</code> then this is a no-op.
     * @param aOwner The node that is initiating the request (for positioning dialogue)
     * @param aStack A {@link CommandStack} to execute commands through.
     * @param aXBar  A {@link MessageDelivery} to send messages to.
     */
    static public <T extends NamedObject> void remove(GaragePath<T> path, Node aOwner, CommandStack aStack,
                                                      MessageDelivery aXBar) {
        if (path == null || path.isRoot()) {
            return;
        }

        final LsmlAlert alert = new LsmlAlert(aOwner, Alert.AlertType.CONFIRMATION);
        alert.setContentText("Are you sure you want to delete: " + path);
        alert.showAndWait().ifPresent(aButton -> {
            if (aButton == ButtonType.OK) {
                LiSongMechLab.safeCommand(aOwner, aStack, new CmdGarageRemove<>(aXBar, path), aXBar);
            }
        });
    }

    /**
     * Saves a copy of the garage to an adjacent auto save file.
     */
    public void autoSave() {
        if (null == autoSaveGarageFile) {
            if (null != garageFile) {
                autoSaveGarageFile = deriveAutoSaveGarageFileName(garageFile);
            }
        }

        if (null != autoSaveGarageFile) {
            try {
                writeGarage(autoSaveGarageFile);
            } catch (Exception e) {
                throw new RuntimeException("Unable to auto save garage!", e);
            }
        }
    }

    /**
     * Performs a very insistent save to make sure that user data is not lost. Typically, used when the application
     * exits.
     */
    public void exitSave() {
        boolean success = saveGarage();
        while (!success) {
            success = saveGarageAs(null);
        }
    }

    /**
     * @return The default folder to save new loadouts to, as set by {@link #setDefaultSaveToFolder(GaragePath)}. If no
     * folder has been set, will return the root folder.
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
     * @param aOwner A {@link Node} that is used for positioning dialog boxes spawned in the process.
     * @return <code>true</code> if a garage file is present. If this returns <code>false</code>, the application should
     * terminate in an orderly manner.
     */
    public boolean loadLastOrNew(Node aOwner) {
        if (!Platform.isFxApplicationThread()) {
            throw new RuntimeException("Autoload garage wasn't called on the FX application thread!");
        }

        boolean giveUp = false;
        while (!giveUp && (null == garageFile || !garageFile.exists() || null == garage)) {
            // Throughout this loop body several dialogues can change settings or the values of members
            // These values must be read in every loop.
            garageFile = new File(settings.getString(Settings.CORE_GARAGE_FILE).getValue());
            if (garageFile.exists()) {
                autoSaveGarageFile = deriveAutoSaveGarageFileName(garageFile);
                if (autoSaveGarageFile.exists() && !restoreAutoSaveOrLoadGarage(aOwner)) {
                    giveUp = true;
                } else {
                    tryReadGarage(garageFile);
                }
            } else if (!openOrCreateNewGarage(aOwner)) {
                giveUp = true;
            }
        }
        if (giveUp) {
            LsmlAlert alert = new LsmlAlert(aOwner, AlertType.ERROR);
            alert.setHeaderText("LSML cannot function without a valid garage file to store your 'Mechs in.");
            alert.setContentText("Application will now exit.");
            alert.showAndWait();
        }
        return !giveUp;
    }

    /**
     * Creates a new empty garage and shows a common file dialog to the user to save it somewhere.
     *
     * @param aOwnerWindow The window that is opening the new garage dialog.
     * @return <code>true</code> if a new garage was created and written, <code>false</code> if the user aborted and
     * nothing was changed.
     */
    public boolean newGarage(Window aOwnerWindow) {
        if (null != garage) {
            askToSaveGarage(aOwnerWindow);
        }
        Garage oldGarage = garage;
        garage = new Garage();
        if (!saveGarageAs(aOwnerWindow)) {
            garage = oldGarage;
            return false;
        }
        return true;
    }

    /**
     * Opens an existing garage, will show a common dialog.
     *
     * @param aOwnerWindow The window that is opening the open dialog.
     * @return <code>true</code> if the garage was opened, <code>false</code> if the user aborted the flow.
     */
    public boolean openGarage(Window aOwnerWindow) {
        if (null != garage) {
            askToSaveGarage(aOwnerWindow);
        }

        final FileChooser fileChooser = garageFileChooser("Open Garage");
        fileChooser.getExtensionFilters().add(LSML_EXT);
        final File file = fileChooser.showOpenDialog(aOwnerWindow);

        if (null != file) {
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {
                garage = garageSerialiser.load(bis);
                garageFile = file;
                autoSaveGarageFile = deriveAutoSaveGarageFileName(file);
                settings.getString(Settings.CORE_GARAGE_FILE).setValue(garageFile.getAbsolutePath());
                return true;
            } catch (final Exception e) {
                errorReporter.error("Unable to open garage",
                                    "LSML was unable to open the selected garage file: " + file.getAbsolutePath(), e);
            }
        }
        return false; // No file chosen
    }

    /**
     * Saves the garage to the current file, if it exists.
     *
     * @return <code>true</code> if the garage was saved, otherwise (including if there was no file path to save to) it
     * returns <code>false</code>.
     */
    public boolean saveGarage() {
        if (null == garageFile) {
            return false;
        }
        try {
            writeGarage(garageFile);
            //noinspection ResultOfMethodCallIgnored
            autoSaveGarageFile.delete();
            return true;
        } catch (final Exception e) {
            errorReporter.error("Unable to save garage",
                                "LSML was unable to save your garage file: " + garageFile.getAbsolutePath(), e);
            return false;
        }
    }

    /**
     * Will save the current garage as a new file. If successful, the {@link Settings#CORE_GARAGE_FILE} property is
     * updated.
     *
     * @param aOwnerWindow The window that is opening the save dialog.
     * @return <code>true</code> if the garage was written to a file, <code>false</code> otherwise.
     */
    public boolean saveGarageAs(Window aOwnerWindow) {
        final File file = garageFileChooser("Save garage as...").showSaveDialog(aOwnerWindow);
        if (null != file && (!file.exists() || confirmOverwrite(aOwnerWindow))) {
            try {
                writeGarage(file);
                garageFile = file;
                autoSaveGarageFile = deriveAutoSaveGarageFileName(file);
                return true;
            } catch (Exception e) {
                errorReporter.error("Unable to save garage as",
                                    "LSML was unable to save the current garage to: " + file.getAbsolutePath() +
                                    ". Old garage path will continue to be used.", e);
            }
        }
        return false;
    }

    /**
     * Sets the default folder to save new loadouts to.
     *
     * @param aDirectory The folder to save to.
     */
    public void setDefaultSaveToFolder(GaragePath<Loadout> aDirectory) {
        defaultSaveTo = aDirectory;
    }

    static private File deriveAutoSaveGarageFileName(File file) {
        return new File(file.getParentFile(), file.getName() + ".auto_save");
    }

    private void askToSaveGarage(Window aOwnerWindow) {
        boolean done = false;
        while (!done) {
            final LsmlAlert saveConfirm = new LsmlAlert(aOwnerWindow, AlertType.CONFIRMATION, "Save current garage?");
            final Optional<ButtonType> result = saveConfirm.showAndWait();
            if (result.isPresent()) {
                if (ButtonType.OK == result.get()) {
                    if (null == garageFile) {
                        done = saveGarageAs(aOwnerWindow);
                    } else {
                        done = saveGarage();
                    }
                } else {
                    done = true; // User wishes to not save
                }
            }
        }
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
        } else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }
        return fileChooser;
    }

    private boolean openOrCreateNewGarage(Node aOwner) {
        final ButtonType openGarage = new ButtonType("Open Garage...");
        final ButtonType newGarage = new ButtonType("New Garage...");
        final ButtonType exit = new ButtonType("Exit", ButtonData.CANCEL_CLOSE);

        final LsmlAlert alert = new LsmlAlert(aOwner, AlertType.NONE);
        alert.setTitle("Select Garage...");
        alert.setHeaderText("Please select or create a new garage to use.");
        alert.setContentText("LSML stores your 'Mechs and Drop Ships in a 'garage'. " +
                             "Your garage is automatically loaded when you open" +
                             " LSML and automatically saved when you close LSML.");
        alert.getButtonTypes().setAll(newGarage, openGarage, exit);
        final Optional<ButtonType> selection = alert.showAndWait();
        if (selection.isPresent()) {
            if (openGarage == selection.get()) {
                openGarage(null);
                return true;
            } else if (newGarage == selection.get()) {
                newGarage(null);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean restoreAutoSaveOrLoadGarage(Node aOwner) {
        final String autoSaveAge = autoSaveGarageFile.lastModified() > garageFile.lastModified() ? "MORE RECENT" :
                "OLDER";
        final ButtonType replace = new ButtonType("Load auto save");
        final ButtonType remove = new ButtonType("Load normal save");
        final ButtonType exit = new ButtonType("Exit without changes", ButtonData.CANCEL_CLOSE);
        final LsmlAlert alert = new LsmlAlert(aOwner, AlertType.WARNING);
        alert.setTitle("Auto saved garage found...");
        alert.setHeaderText("LSML has detected an auto save of your garage");
        alert.setContentText("This typically happens when LSML terminated unexpectedly. " +
                             "Loading the auto save IRREVERSIBLY REPLACES your current garage file. " +
                             "To inspect the files before deciding, exit LSML program now. " + "The auto save is " +
                             autoSaveAge + " than your garage. " + "What would you like to do?");
        alert.getButtonTypes().setAll(replace, remove, exit);
        final Optional<ButtonType> selection = alert.showAndWait();
        if (selection.isPresent()) {
            if (replace == selection.get()) {
                tryReadGarage(autoSaveGarageFile);
            } else if (remove == selection.get()) {
                //noinspection ResultOfMethodCallIgnored
                autoSaveGarageFile.delete();
                tryReadGarage(garageFile);
            } else {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private void tryReadGarage(File file) {
        try (FileInputStream fis = new FileInputStream(file); BufferedInputStream bis = new BufferedInputStream(fis)) {
            garage = garageSerialiser.load(bis);
            if (null == garage) {
                throw new IOException("XStream returned null!");
            }
        } catch (final Exception e) {
            errorReporter.error("Unable to open garage", "Could not read from file: " + file.getAbsolutePath(), e);
        }
    }

    private void writeGarage(File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            garageSerialiser.save(bos, garage);
            garageFile = file;
            final Property<String> garageProp = settings.getString(Settings.CORE_GARAGE_FILE);
            garageProp.setValue(file.getAbsolutePath());
        }
    }
}
