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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.lisoft.lsml.model.datacache.AbstractDatabaseProvider;
import org.lisoft.lsml.model.datacache.DataCache;
import org.lisoft.lsml.model.datacache.MwoDataReader;
import org.lisoft.lsml.model.datacache.gamedata.GameVFS;
import org.lisoft.lsml.view_fx.controllers.SplashScreenController;
import org.lisoft.lsml.view_fx.controls.LsmlAlert;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.concurrent.Task;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;

/**
 * The purpose of this class is to in some way or another provide a usable {@link DataCache} object for the application.
 *
 * @author Emily Björk
 */
@Singleton
public class FXDatabaseProvider extends AbstractDatabaseProvider {

    private static <T> T runInAppThreadAndWait(Callable<T> aRunnable) {
        final Task<T> task = new Task<T>() {
            @Override
            protected T call() throws Exception {
                return aRunnable.call();
            }
        };
        Platform.runLater(task);
        try {
            return task.get();
        }
        catch (InterruptedException | ExecutionException e) {
            // Programmer error
            throw new RuntimeException(e);
        }
    }

    private final Settings settings;
    private final SplashScreenController splashScreen;
    private final ErrorReporter errorReporter;
    private final String currentVersion;

    private final MwoDataReader dataReader;
    private Optional<DataCache> activeCache = null;

    @Inject
    public FXDatabaseProvider(Settings aSettings, SplashScreenController aSplashScreen, ErrorReporter aErrorReporter,
            @Named("version") String aVersion, MwoDataReader aDataReader) {
        super(aVersion, aErrorReporter);
        settings = aSettings;
        splashScreen = aSplashScreen;
        errorReporter = aErrorReporter;
        currentVersion = aVersion;
        dataReader = aDataReader;
    }

    @Override
    public Optional<DataCache> getDatacache() {
        if (activeCache == null) {
            activeCache = loadCache();
        }
        return activeCache;
    }

    private boolean askUserForGameInstall() {
        boolean retry = true;
        final Property<String> gameDirectory = settings.getString(Settings.CORE_GAME_DIRECTORY);
        while (retry) {
            final ButtonType useBundled = new ButtonType("Use bundled data");
            final ButtonType browse = new ButtonType("Browse...");
            final ButtonType autoDetect = new ButtonType("Auto detect");

            final Optional<ButtonType> userAction = runInAppThreadAndWait(() -> {
                final LsmlAlert alert = new LsmlAlert(null, AlertType.CONFIRMATION);
                alert.setTitle("Detecting game files...");
                alert.setHeaderText("LSML needs access to game files.");
                alert.setContentText(
                        "Normally LSML will parse your game install to find the latest 'Mech and weapon stats automatically."
                                + " To do this LSML needs to know where your game install is, you can choose to browse for it or let"
                                + " LSML auto-detect it for you. If you don't have a game install you can use the bundled data "
                                + "(can be changed from settings page).");

                alert.getButtonTypes().setAll(autoDetect, browse, useBundled);
                return alert.showAndWait();
            });

            final ButtonType action = userAction.orElse(null);
            if (action == useBundled) {
                return false;
            }
            else if (action == browse) {
                final DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Browse for MWO installation directory...");
                final File dir = chooser.showDialog(null);
                retry = !GameVFS.isValidGameDirectory(dir);
                if (retry) {
                    runInAppThreadAndWait(() -> {
                        final LsmlAlert error = new LsmlAlert(null, AlertType.ERROR);
                        error.setContentText("That directory is not a valid MWO installation.");
                        error.showAndWait();
                        return null;
                    });
                }
                else {
                    gameDirectory.setValue(dir.getAbsolutePath().toString());
                }
            }
            else if (action == autoDetect) {
                retry = !GameVFS.autoDetectGameInstall(settings, splashScreen.subProgressTextProperty(),
                        (aPath) -> runInAppThreadAndWait(() -> showConfirmGameDirDialog(aPath)));
                if (retry) {
                    runInAppThreadAndWait(() -> {
                        final LsmlAlert failed = new LsmlAlert(splashScreen.getView(), AlertType.ERROR,
                                "Auto detection failed, no game install detected");
                        failed.showAndWait();
                        return null;
                    });
                }
            }
            else if (action == null) {
                return false;
            }
            else {
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

    private Optional<DataCache> getCached() {
        final String cacheFile = settings.getString(Settings.CORE_DATA_CACHE).getValue();

        DataCache dataCache;
        try {
            dataCache = (DataCache) DataCache.makeDataCacheXStream().fromXML(cacheFile);
        }
        catch (final Throwable e) {
            // If the parsing fails, either the cache is corrupted or the internal format has changed between versions.
            // Just silently ignore it, we'll parse a new cache from the data files or use the bundled data.
            return Optional.empty();
        }

        if (!dataCache.getVersion().equals(currentVersion)) {
            return Optional.empty();
        }
        return Optional.of(dataCache);
    }

    /**
     * Figures out where to place a new (or overwritten) cache data files.
     *
     * @return A {@link File} with a location.
     * @throws IOException
     *             Thrown if no location could be determined or the location is invalid.
     */
    private File getCacheLocationWrite() throws IOException {
        final String dataCacheLocation = settings.getString(Settings.CORE_DATA_CACHE).getValue();
        if (dataCacheLocation.isEmpty()) {
            throw new IOException("An empty string was used as data cache location in the settings file!");
        }
        final File dataCacheFile = new File(dataCacheLocation);
        if (dataCacheFile.isDirectory()) {
            throw new IOException("The data cache location (" + dataCacheLocation
                    + ") is a directory! Expected non-existent or a plain file.");
        }
        return dataCacheFile;
    }

    private Optional<DataCache> loadCache() {
        // This method is executed in a background task so that the splash can display while we're doing work.
        // Unfortunately we also need to display dialogs to the FX application thread so there will be some back and
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

        Optional<DataCache> cache = getCached();

        if (!cache.isPresent() || dataReader.shouldUpdate(cache.get(), new File(gameDirectory.getValue()))) {
            cache = updateCache(cache);
        }

        if (cache.isPresent()) {
            return cache;
        }
        return getBundled();
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

    private Optional<DataCache> updateCache(Optional<DataCache> cache) {
        final PrintWriter log = new PrintWriter(System.out);
        try {
            final Property<String> gameDirectory = settings.getString(Settings.CORE_GAME_DIRECTORY);
            final Optional<DataCache> parsedCache = dataReader.parseGameFiles(log, new File(gameDirectory.getValue()));
            if (parsedCache.isPresent()) {
                writeCache(parsedCache.get());
                return parsedCache;
            }
        }
        catch (final Exception e) {
            errorReporter.error("Error writing data cache",
                    "LSML has encountered an error while writing the new data cache to disk. Previous data will be used.",
                    e);
        }
        return cache;
    }

    private void writeCache(DataCache aDataCache) throws IOException {
        final File cacheFile = getCacheLocationWrite();
        final XStream stream = DataCache.makeDataCacheXStream();
        try (OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(cacheFile), "UTF-8");
                StringWriter sw = new StringWriter()) {
            // Write to memory first, this prevents touching the old file if the
            // marshaling fails
            sw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            stream.marshal(aDataCache, new PrettyPrintWriter(sw));
            // Write to file
            ow.append(sw.toString());
        }
        settings.getString(Settings.CORE_DATA_CACHE).setValue(cacheFile.getPath());
    }
}
