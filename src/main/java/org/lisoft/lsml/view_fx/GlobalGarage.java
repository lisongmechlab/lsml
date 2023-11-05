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
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import org.lisoft.lsml.application.ApplicationSingleton;
import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.command.CmdGarageAddDirectory;
import org.lisoft.lsml.command.CmdGarageMultiRemove;
import org.lisoft.lsml.command.CmdGarageRemove;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.Garage;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.garage.GaragePath;
import org.lisoft.lsml.model.garage.GarageSerializer;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.controls.LsmlAlert;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class wraps the application global garage state. In essence this is a singleton object which should be injected
 * through DI.
 *
 * @author Li Song
 */
@ApplicationSingleton
public class GlobalGarage {
    private static class GarageOnDisk {
        private final File autoSaveFile;
        private final File currentFile;
        private final Garage garage;
        private final GarageSerializer serializer;

        private GarageOnDisk(File aFile, GarageSerializer aGarageSerializer, Settings aSettings) throws IOException {
            currentFile = aFile;
            serializer = aGarageSerializer;
            autoSaveFile = deriveAutoSaveGarageFileName(currentFile);
            try (FileInputStream fis = new FileInputStream(currentFile);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {
                garage = serializer.load(bis);
                if (null == garage) {
                    throw new IOException("XStream returned null!");
                }
            }
            // Set the last opened garage as the currently loaded file
            aSettings.getString(Settings.CORE_GARAGE_FILE).setValue(currentFile.getAbsolutePath());
        }

        static private File deriveAutoSaveGarageFileName(File file) {
            return new File(file.getParentFile(), file.getName() + ".auto_save");
        }

        static private boolean hasAutoSaveFor(File aFile) {
            File expectedAutoSaveName = deriveAutoSaveGarageFileName(aFile);
            return expectedAutoSaveName.exists() && expectedAutoSaveName.lastModified() > aFile.lastModified();
        }

        static private GarageOnDisk newGarage(File aFile, GarageSerializer aGarageSerializer, Settings aSettings)
            throws IOException {
            save(aFile, new Garage(), aGarageSerializer);
            return new GarageOnDisk(aFile, aGarageSerializer, aSettings);
        }

        static private File recoverAutoSaveFor(File aOldFile) throws IOException {
            if (!hasAutoSaveFor(aOldFile)) {
                throw new IOException("Cannot restore non-existent auto save!");
            }

            Date date = Calendar.getInstance().getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh_mm_ss");
            String strDate = dateFormat.format(date);
            File aOldBackup = new File(aOldFile.getParentFile(),
                                       aOldFile.getName() + " " + strDate + ".backup_before_auto_save_recovery");
            try {
                Files.copy(aOldFile.toPath(), aOldBackup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                try {
                    Files.delete(aOldBackup.toPath());
                } catch (IOException ex) {
                    // Eat the exception the file might not exist.
                }
                throw e;
            }
            File expectedAutoSaveName = deriveAutoSaveGarageFileName(aOldFile);
            Files.copy(expectedAutoSaveName.toPath(), aOldFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return aOldBackup;
        }

        static private void save(File aFile, Garage aGarage, GarageSerializer aGarageSerializer) throws IOException {
            try (FileOutputStream fos = new FileOutputStream(aFile);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                aGarageSerializer.save(bos, aGarage);
            }
        }

        static private GarageOnDisk saveAs(File aFile, GarageOnDisk aGarageFile, Settings aSettings)
            throws IOException {
            save(aFile, aGarageFile.garage, aGarageFile.serializer);
            return new GarageOnDisk(aFile, aGarageFile.serializer, aSettings);
        }

        private void autoSave() throws IOException {
            save(autoSaveFile, garage, serializer);
        }

        private void save() throws IOException {
            save(currentFile, garage, serializer);
            // If the save was successful (didn't throw), delete the last auto save

            //noinspection ResultOfMethodCallIgnored -- We don't really care if the deletion succeeded.
            autoSaveFile.delete();
        }
    }

    private static final String DEFAULT_NEW_FOLDER_NAME = "New Folder";
    private static final ExtensionFilter LSML_EXT = new ExtensionFilter("LSML Garage 1.0", "*.xml");
    private static final ExtensionFilter LSML_EXT2 = new ExtensionFilter("LSML Garage 2.0", "*.lsxml");
    private final ErrorReporter reporter;
    private final GarageSerializer serializer;
    private final Settings settings;
    private GaragePath<Loadout> defaultSaveTo;
    private GarageOnDisk garageFile = null;

    @Inject
    public GlobalGarage(Settings aSettings, ErrorReporter aErrorReporter, GarageSerializer aGarageSerializer) {
        settings = aSettings;
        serializer = aGarageSerializer;
        reporter = aErrorReporter;
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

        int newFolderNr = 1;
        GarageDirectory<T> newDir = new GarageDirectory<>(DEFAULT_NEW_FOLDER_NAME);
        while(!GaragePath.isNameAvailalble(path, newDir.getName())){
            newDir = new GarageDirectory<>(DEFAULT_NEW_FOLDER_NAME + " " + newFolderNr);
            newFolderNr++;
        }

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
        if (garageFile != null) {
            try {
                garageFile.autoSave();
            } catch (IOException e) {
                reporter.error("Unable to create auto save", e.getMessage(), e);
            }
        }
    }

    /**
     * Ask the user to either create a new garage or open an existing one.
     *
     * @param aOwner The window that is opening the new garage dialog.
     * @return true if a garage was opened/created, false if the user chose to exit instead
     */
    public boolean createOrOpen(Window aOwner) {
        while (true) {
            final LsmlAlert selectGarage = new LsmlAlert(aOwner.getScene().getRoot(), AlertType.NONE);
            selectGarage.setTitle("Select Garage...");
            selectGarage.setHeaderText("Please select or create a new garage to use.");
            selectGarage.setContentText(
                "LSML stores all your 'Mechs in a 'garage'. Your garage is automatically loaded when you open " +
                "LSML and automatically saved when you close LSML. A garage is necessary to use LSML.");

            final ButtonType openGarage = new ButtonType("Open Garage...");
            final ButtonType newGarage = new ButtonType("New Garage...");
            final ButtonType exit = new ButtonType("Exit", ButtonData.CANCEL_CLOSE);
            selectGarage.getButtonTypes().setAll(newGarage, openGarage, exit);

            final Optional<ButtonType> selection = selectGarage.showAndWait();
            if (selection.isPresent()) {
                if (openGarage == selection.get()) {
                    if (openGarage(aOwner)) {
                        return true;
                    }
                } else if (newGarage == selection.get()) {
                    if (newGarage(aOwner)) {
                        return true;
                    }
                } else {
                    break; // User clicked "exit" button
                }
            } else {
                break; // User closed dialog with "X" button.
            }
        }
        LsmlAlert alert = new LsmlAlert(aOwner.getScene().getRoot(), AlertType.ERROR);
        alert.setHeaderText("LSML cannot function without a valid garage file to store your 'Mechs in.");
        alert.setContentText("Application will now exit.");
        alert.showAndWait();
        return false;
    }

    /**
     * Performs a very insistent save to make sure that user data is not lost. Typically, used when the application
     * exits.
     *
     * @param aOwner The window that is opening the new garage dialog.
     */
    public void exitSave(Window aOwner) {
        boolean success = saveGarage(aOwner);
        while (!success) {
            success = saveGarageAs(null);
        }
    }

    /**
     * @return The default folder to save new loadouts to, as set by {@link #setDefaultSaveToFolder(GaragePath)}. If no
     * folder has been set, will return the root folder.
     */
    public Optional<GaragePath<Loadout>> getDefaultSaveTo() {
        if (defaultSaveTo != null) {
            return Optional.of(defaultSaveTo);
        } else if (garageFile != null) {
            return Optional.of(new GaragePath<>(garageFile.garage.getLoadoutRoot()));
        }
        return Optional.empty();
    }

    /**
     * @return the garage
     */
    public Garage getGarage() {
        if (null != garageFile && null != garageFile.garage) {
            return garageFile.garage;
        }
        throw new RuntimeException("Asked to get garage before the initial garage has been loaded. " +
                                   "This is a bug, please report it on GitHub.");
    }

    /**
     * Creates a new empty garage and shows a common file dialog to the user to save it somewhere.
     *
     * @param aOwner The window that is opening the new garage dialog.
     * @return true if a new garage was created and written, false on user abort.
     */
    public boolean newGarage(Window aOwner) {
        if (null != garageFile) {
            askToSaveGarage(aOwner);
        }

        while (true) {
            Optional<File> fileOptional = chooseGarageTarget("Select new file to store garage to...", aOwner);
            if (!fileOptional.isPresent()) {
                break; // User cancelled
            }
            try {
                garageFile = GarageOnDisk.newGarage(fileOptional.get(), serializer, settings);
                return true;
            } catch (IOException e) {
                reporter.error("Unable to save new garage", "Couldn't write the selected garage file.", e);
            }
        }
        return false;
    }

    /**
     * Opens an existing garage, will show a common dialog.
     *
     * @param aOwner The window that is opening the open dialog.
     * @return true if the garage was opened, false on user abort.
     */
    public boolean openGarage(Window aOwner) {
        if (null != garageFile) {
            askToSaveGarage(aOwner);
        }

        while (true) {
            final FileChooser fileChooser = garageFileChooser("Open Garage");
            fileChooser.getExtensionFilters().add(LSML_EXT);

            final File file = fileChooser.showOpenDialog(aOwner);
            if (file == null) {
                break; // User cancelled
            }
            try {
                garageFile = new GarageOnDisk(file, serializer, settings);
                return true;
            } catch (Exception e) {
                reporter.error("Unable to open garage",
                               "LSML was unable to open the selected garage file: " + file.getAbsolutePath() +
                               ".\n\nPlease try another garage or cancel. ", e);
            }
        }
        return false;
    }

    /**
     * Try to open the previously used garage.
     *
     * @param aOwner A {@link Node} that is used for positioning dialog boxes spawned in the process.
     * @return <code>true</code> if a garage file was loaded, <code>false</code> otherwise.
     */
    public boolean openLastGarage(Node aOwner) {
        if (!Platform.isFxApplicationThread()) {
            throw new RuntimeException("Autoload garage wasn't called on the FX application thread!");
        }

        final File lastOpenedGarageFile = new File(settings.getString(Settings.CORE_GARAGE_FILE).getValue());

        if (GarageOnDisk.hasAutoSaveFor(lastOpenedGarageFile)) {
            try {
                File backupFile = GarageOnDisk.recoverAutoSaveFor(lastOpenedGarageFile);
                LsmlAlert alert = new LsmlAlert(aOwner, AlertType.WARNING);
                alert.setHeaderText("A recent auto save was loaded");
                alert.setContentText("LSML has detected an existing auto save with a more recent date than the " +
                                     "currently selected garage. The current garage has been copied to: " +
                                     backupFile.getAbsolutePath() + " and the auto save has been recovered to: " +
                                     lastOpenedGarageFile.getAbsolutePath());
                alert.showAndWait();
            } catch (IOException e) {
                reporter.error("Unable to restore auto save!",
                               "LSML detected an auto save from a recent crash, tried to recover it " +
                               "and failed. Your garage files have not bee modified. Please take a backup of: " +
                               lastOpenedGarageFile.getAbsolutePath() +
                               " and the auto save so that they might be manually repaired.", e);
                return false;
            }
            // lastOpenedGarageFile has now been replaced by the auto save and normal opening procedure can continue
        }

        if (lastOpenedGarageFile.exists()) {
            try {
                garageFile = new GarageOnDisk(lastOpenedGarageFile, serializer, settings);
            } catch (IOException e) {
                reporter.error("Unable to load garage!",
                               "Please make a backup of: " + lastOpenedGarageFile.getAbsolutePath() +
                               " and any auto save files so that they might be manually repaired. " +
                               "You can remove the above file to let LSML start with a clean garage.\n\n" +
                               "LSML will now exit to prevent any further damage to your garage.", e);
            }
        }
        return garageFile != null;
    }

    /**
     * Saves the garage to the current file, if it exists.
     *
     * @param aOwner The window that is opening the save dialog.
     * @return true if no garage or garage has been persisted somehow, false if the user aborted on error.
     */
    public boolean saveGarage(Window aOwner) {
        if (null == garageFile) {
            return true;
        }

        try {
            garageFile.save();
            return true;
        } catch (IOException e) {
            reporter.error("Unable to save garage",
                           "LSML was unable to save your garage file: " + garageFile.currentFile.getAbsolutePath() +
                           ". Please chose another file to save to.", e);
            return saveGarageAs(aOwner);
        }
    }

    /**
     * Will save the current garage as a new file. Retrying on errors.
     *
     * @param aOwner The window that is opening the save dialog.
     * @return true if the garage file was saved to a new file, false if the user aborted.
     */
    public boolean saveGarageAs(Window aOwner) {
        while (true) {
            Optional<File> fileOptional = chooseGarageTarget("Save garage as...", aOwner);
            if (!fileOptional.isPresent()) {
                break; // User cancelled
            }

            final File file = fileOptional.get();
            try {
                garageFile = GarageOnDisk.saveAs(file, garageFile, settings);
                return true;
            } catch (IOException e) {
                reporter.error("Unable to save garage as",
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

    private void askToSaveGarage(Window aOwner) {
        final LsmlAlert saveConfirm = new LsmlAlert(aOwner, AlertType.CONFIRMATION, "Save current garage?");
        final Optional<ButtonType> result = saveConfirm.showAndWait();
        if (result.isPresent() && ButtonType.OK == result.get()) {
            saveGarage(aOwner);
        }
    }

    /**
     * Presents a dialog for the user to save a garage to. Will retry the dialogue on invalid
     * selections until the user hits cancel. Will ask for confirmation on overwrite.
     *
     * @param aTitle The title to display for the dialogue.
     * @return optionally a file if the user didn't cancel.
     */
    private Optional<File> chooseGarageTarget(String aTitle, Window aOwner) {
        while (true) {
            FileChooser chooser = garageFileChooser(aTitle);
            final File file = chooser.showSaveDialog(aOwner);
            if (file == null) {
                break; // User cancelled dialog
            }

            if (file.isDirectory()) {
                LsmlAlert alert = new LsmlAlert(aOwner.getScene().getRoot(), AlertType.ERROR);
                alert.setHeaderText("No file selected.");
                alert.setContentText("Please select try again or cancel.");
                alert.showAndWait();
            } else if (file.exists()) {
                if (confirmOverwrite(aOwner)) {
                    return Optional.of(file);
                }
            } else {
                return Optional.of(file);
            }
        }
        return Optional.empty();
    }

    private boolean confirmOverwrite(Window aOwner) {
        final LsmlAlert confirmOverwrite = new LsmlAlert(aOwner, AlertType.CONFIRMATION, "Overwrite selected garage?");
        final Optional<ButtonType> result = confirmOverwrite.showAndWait();
        return result.isPresent() && ButtonType.OK == result.get();
    }

    private FileChooser garageFileChooser(String aTitle) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(aTitle);
        fileChooser.getExtensionFilters().addAll(LSML_EXT2);

        final String lastGaragePath = settings.getString(Settings.CORE_GARAGE_FILE).getValue();
        final File lastGarageFile = new File(lastGaragePath);

        if (lastGarageFile.exists() && lastGarageFile.getParentFile().isDirectory()) {
            fileChooser.setInitialDirectory(lastGarageFile.getParentFile());
        } else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }
        return fileChooser;
    }
}
