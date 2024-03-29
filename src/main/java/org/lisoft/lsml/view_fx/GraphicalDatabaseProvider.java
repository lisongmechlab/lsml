/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.view_fx;

import java.io.*;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.concurrent.Task;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.model.AbstractDatabaseProvider;
import org.lisoft.lsml.view_fx.controllers.SplashScreenController;
import org.lisoft.lsml.view_fx.controls.LsmlAlert;
import org.lisoft.mwo_data.Database;
import org.lisoft.mwo_data.mwo_parser.GameVFS;
import org.lisoft.mwo_data.mwo_parser.MwoDataReader;
import org.lisoft.mwo_data.mwo_parser.ParseErrorException;

/**
 * The purpose of this class is to in some way or another provide a usable {@link Database} object
 * for the application.
 *
 * @author Li Song
 */
@Singleton
public class GraphicalDatabaseProvider extends AbstractDatabaseProvider {

  private final String currentVersion;
  private final MwoDataReader dataReader;
  private final ErrorReporter errorReporter;
  private final Settings settings;
  private final SplashScreenController splashScreen;
  private Database activeDatabase = null;

  @Inject
  public GraphicalDatabaseProvider(
      Settings aSettings,
      SplashScreenController aSplashScreen,
      ErrorReporter aErrorReporter,
      @Named("version") String aVersion,
      MwoDataReader aDataReader) {
    super(aVersion);
    settings = aSettings;
    splashScreen = aSplashScreen;
    errorReporter = aErrorReporter;
    currentVersion = aVersion;
    dataReader = aDataReader;
  }

  @Override
  public Database getDatabase() {
    if (activeDatabase == null) {
      activeDatabase = loadDatabase();
    }
    return activeDatabase;
  }

  private static <T> T runInAppThreadAndWait(Callable<T> aRunnable) {
    final Task<T> task =
        new Task<>() {
          @Override
          protected T call() throws Exception {
            return aRunnable.call();
          }
        };
    Platform.runLater(task);
    try {
      return task.get();
    } catch (InterruptedException | ExecutionException e) {
      // Programmer error
      throw new RuntimeException(e);
    }
  }

  private boolean askUserForGameInstall() {
    boolean retry = true;
    final Property<String> gameDirectory = settings.getString(Settings.CORE_GAME_DIRECTORY);
    while (retry) {
      final ButtonType useBundled = new ButtonType("Use bundled data");
      final ButtonType browse = new ButtonType("Browse...");
      final ButtonType autoDetect = new ButtonType("Auto detect");

      final Optional<ButtonType> userAction =
          runInAppThreadAndWait(
              () -> {
                final LsmlAlert alert = new LsmlAlert(null, AlertType.CONFIRMATION);
                alert.setTitle("Detecting game files...");
                alert.setHeaderText("LSML needs access to game files.");
                alert.setContentText(
                    "Normally LSML will parse your game install to find the latest 'Mech and weapon stats automatically."
                        + " To do this LSML needs to know where your game install is, you can choose to browse for it"
                        + " or use the bundled data if you do not have a game install."
                        + " You can change this from settings page.");

                alert.getButtonTypes().setAll(/*autoDetect,*/ browse, useBundled);
                return alert.showAndWait();
              });

      final ButtonType action = userAction.orElse(null);
      if (action == useBundled) {
        return false;
      } else if (action == browse) {
        final File dir =
            runInAppThreadAndWait(
                () -> {
                  final DirectoryChooser chooser = new DirectoryChooser();
                  chooser.setTitle("Browse for MWO installation directory...");
                  return chooser.showDialog(null);
                });

        retry = !GameVFS.isValidGameDirectory(dir);
        if (retry) {
          runInAppThreadAndWait(
              () -> {
                final LsmlAlert error = new LsmlAlert(null, AlertType.ERROR);
                error.setContentText("That directory is not a valid MWO installation.");
                error.showAndWait();
                return null;
              });
        } else {
          gameDirectory.setValue(dir.getAbsolutePath());
        }
      } else if (action == autoDetect) {

        Optional<Path> optionalGamePath =
            GameVFS.autoDetectGameInstall(
                (aPath) -> {
                  Platform.runLater(
                      () ->
                          splashScreen
                              .subProgressTextProperty()
                              .set(aPath.toAbsolutePath().toString()));
                  return null;
                },
                (aPath) -> runInAppThreadAndWait(() -> showConfirmGameDirDialog(aPath)));

        optionalGamePath.ifPresent(
            path ->
                settings
                    .getString(Settings.CORE_GAME_DIRECTORY)
                    .setValue(path.toAbsolutePath().toString()));

        retry = optionalGamePath.isEmpty();
        if (retry) {
          runInAppThreadAndWait(
              () -> {
                final LsmlAlert failed =
                    new LsmlAlert(
                        splashScreen.getView(),
                        AlertType.ERROR,
                        "Auto detection failed, no game install detected");
                failed.showAndWait();
                return null;
              });
        }
      } else if (action == null) {
        return false;
      } else {
        throw new IllegalArgumentException("Unknown action: " + action);
      }
    }
    return true;
  }

  private boolean checkDefaultGameInstall() {
    for (final Path path : GameVFS.getDefaultGameFileLocations()) {
      if (GameVFS.isValidGameDirectory(path.toFile())) {
        final Property<String> gameDirectory = settings.getString(Settings.CORE_GAME_DIRECTORY);
        gameDirectory.setValue(path.toAbsolutePath().toString());
        setSubText("Found game at default location: " + gameDirectory.getValue());
        return true;
      }
    }
    return false;
  }

  /**
   * Figures out where to place a new (or overwritten) database files.
   *
   * @return A {@link File} with a location.
   * @throws IOException Thrown if no location could be determined or the location is invalid.
   */
  private File getDatabaseLocationWrite() throws IOException {
    final String databaseLocation = settings.getString(Settings.CORE_DATABASE).getValue();
    if (databaseLocation.isEmpty()) {
      throw new IOException("An empty string was used as database location in the settings file!");
    }
    final File databaseFile = new File(databaseLocation);
    if (databaseFile.isDirectory()) {
      throw new IOException(
          "The database location ("
              + databaseLocation
              + ") is a directory! Expected non-existent or a plain file.");
    }
    return databaseFile;
  }

  private Optional<Database> getPreviouslyParsed() {
    final String databaseFile = settings.getString(Settings.CORE_DATABASE).getValue();
    Database database;
    try (InputStream is = new FileInputStream(databaseFile);
        BufferedInputStream bis = new BufferedInputStream(is)) {
      database = Database.readFromStream(bis);
    } catch (final Throwable e) {
      // If the parsing fails, either the database is corrupted or the internal format has changed
      // between versions. Just silently ignore it, we'll parse a new database from the data files
      // or use the bundled data.
      return Optional.empty();
    }

    final String version = database.getVersion();
    if (version == null || !version.equals(currentVersion)) {
      return Optional.empty();
    }
    return Optional.of(database);
  }

  private Database loadDatabase() {
    // This method is executed in a background task so that the splash can display while we're doing
    // work.
    // Unfortunately we also need to display dialogs to the FX application thread so there will be
    // some back and
    // forth here.
    splashScreen.setProgressText("Loading game data...");

    final Property<Boolean> useBundledData = settings.getBoolean(Settings.CORE_FORCE_BUNDLED_DATA);
    if (useBundledData.getValue()) {
      setSubText("Forced use of bundled data");
      return getBundled();
    }

    final Property<String> gameDirectory = settings.getString(Settings.CORE_GAME_DIRECTORY);
    boolean hasGameInstall = GameVFS.isValidGameDirectory(new File(gameDirectory.getValue()));
    if (!hasGameInstall) {
      hasGameInstall = checkDefaultGameInstall();
    }

    if (!hasGameInstall) {
      hasGameInstall = askUserForGameInstall();
    }

    if (!hasGameInstall) {
      useBundledData.setValue(Boolean.TRUE);
      return getBundled();
    }

    Optional<Database> dataBase = getPreviouslyParsed();
    try {
      if (dataBase.isEmpty()
          || dataReader.shouldUpdate(dataBase.get(), new File(gameDirectory.getValue()))) {
        dataBase = updateDatabase();
      }
    } catch (Throwable t) {
      errorReporter.error(
          "Unable to determine if game files need to be updated!",
          "Probably because MWO or some other application has opened the file, try closing any such applications.",
          t);
    }
    return dataBase.orElseGet(this::getBundled);
  }

  private void setSubText(String aText) {
    Platform.runLater(() -> splashScreen.subProgressTextProperty().set(aText));
  }

  private Boolean showConfirmGameDirDialog(Path aPath) {
    final LsmlAlert confirm = new LsmlAlert(null, AlertType.CONFIRMATION);
    confirm.setHeaderText("Is this your primary MWO installation?");
    confirm.setContentText(aPath.toString());
    final Optional<ButtonType> answer = confirm.showAndWait();
    return answer.isPresent() && answer.get() == ButtonType.OK;
  }

  private Optional<Database> updateDatabase() {
    try {
      final Property<String> gameDirectory = settings.getString(Settings.CORE_GAME_DIRECTORY);
      final Database parsedDatabase = dataReader.parseGameFiles(new File(gameDirectory.getValue()));
      writeDatabase(parsedDatabase);
      return Optional.of(parsedDatabase);
    } catch (final IOException e) {
      errorReporter.error(
          "Error writing database",
          "LSML has encountered an error while writing the new database to disk. Previous data will be used.",
          e);
    } catch (ParseErrorException e) {
      errorReporter.error(
          "Parse error",
          "This usually happens when PGI has changed the structure of the data files "
              + "in a patch. Please look for an updated version of LSML at www.li-soft.org."
              + " In the meanwhile LSML will continue to function with the data from the last"
              + " successfully parsed patch.",
          e);
    }
    return Optional.empty();
  }

  private void writeDatabase(Database aDatabase) throws IOException {
    final File databaseFile = getDatabaseLocationWrite();
    try (FileOutputStream fos = new FileOutputStream(databaseFile)) {
      aDatabase.writeToStream(fos);
    }
    settings.getString(Settings.CORE_DATABASE).setValue(databaseFile.getPath());
  }
}
