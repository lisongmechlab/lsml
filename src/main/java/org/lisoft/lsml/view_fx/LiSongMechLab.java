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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.datacache.DataCache;
import org.lisoft.lsml.model.datacache.EnvironmentDB;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.datacache.StockLoadoutDB;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.datacache.gamedata.GameVFS;
import org.lisoft.lsml.model.export.Base64LoadoutCoder;
import org.lisoft.lsml.model.export.LsmlProtocolIPC;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.OS;
import org.lisoft.lsml.view_fx.loadout.LoadoutWindow;
import org.lisoft.lsml.view_fx.util.FxmlHelpers;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.WString;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * This is the main application for the LSML JavaFX GUI.
 * 
 * FIXME: Dependency Inject stuff
 * 
 * @author Emily Björk
 */
public class LiSongMechLab extends Application {
    public static final String DEVELOP_VERSION = "(develop)";
    public static final long MIN_SPLASH_TIME_MS = 20;

    private static final Base64LoadoutCoder coder = new Base64LoadoutCoder(DefaultLoadoutErrorReporter.instance);
    private static final Settings SETTINGS = Settings.getSettings();

    public static String getVersion() {
        final Class<?> clazz = LiSongMechLab.class;
        final String className = clazz.getSimpleName() + ".class";
        final String classPath = clazz.getResource(className).toString();
        if (!classPath.startsWith("jar")) {
            // Class not from JAR
            return DEVELOP_VERSION;
        }
        final String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
        try (InputStream stream = new URL(manifestPath).openStream()) {
            final Manifest manifest = new Manifest(new URL(manifestPath).openStream());
            final Attributes attr = manifest.getMainAttributes();
            final String value = attr.getValue("Implementation-Version");
            return value;
        }
        catch (final IOException e) {
            return DEVELOP_VERSION;
        }
    }

    public static void main(final String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());
        checkCliArguments(args);
        launch(args);
    }

    public static void openLoadout(final MessageXBar aGlobalXBar, final Loadout aLoadout) {
        final Stage stage = new Stage();
        final LoadoutWindow root = new LoadoutWindow(aGlobalXBar, aLoadout, stage, coder);
        FxmlHelpers.createStage(stage, root);
    }

    public static void openLoadout(final MessageXBar aGlobalXBar, final String aUrl) {
        try {
            openLoadout(aGlobalXBar, coder.parse(aUrl));
        }
        catch (Exception exception) {
            showError(null, exception);
        }
    }

    public static boolean safeCommand(final Node aOwner, final CommandStack aStack, final Command aCommand) {
        try {
            aStack.pushAndApply(aCommand);
        }
        catch (final Exception e) {
            LiSongMechLab.showError(aOwner, e);
            return false;
        }
        return true;
    }

    public static void showError(final Node aOwner, final Exception aException) {
        javafx.application.Platform.runLater(() -> {
            final Alert alert = new Alert(AlertType.ERROR, aException.getMessage(), ButtonType.CLOSE);
            if (null != aOwner && aOwner.getScene() != null) {
                alert.initOwner(aOwner.getScene().getWindow());
            }
            alert.getDialogPane().getStylesheets().addAll(FxmlHelpers.getBaseStyleSheet());
            alert.showAndWait();
        });
    }

    private static void checkCliArguments(final String[] args) {
        // Started with an argument, it's likely a LSML:// protocol string, send it over the IPC and quit.
        if (args.length > 0) {
            int port = SETTINGS.getProperty(Settings.CORE_IPC_PORT, Integer.class).getValue().intValue();
            if (port < 1024)
                port = LsmlProtocolIPC.DEFAULT_PORT;
            if (LsmlProtocolIPC.sendLoadout(args[0], port)) {
                System.exit(0);
            }
        }
    }

    private static void checkForUpdates() {
        if (!SETTINGS.getProperty(Settings.CORE_CHECK_FOR_UPDATES, Boolean.class).getValue().booleanValue())
            return;

        final Property<Long> lastUpdate = SETTINGS.getProperty(Settings.CORE_LAST_UPDATE_CHECK, Long.class);
        final long lastUpdateMs = lastUpdate.getValue();
        final long nowMs = System.currentTimeMillis();
        final long msPerDay = 24 * 60 * 60 * 1000;
        final long diffDays = (nowMs - lastUpdateMs) / msPerDay;
        if (diffDays < 3) { // Will check every three days.
            return;
        }
        lastUpdate.setValue(nowMs);

        final boolean acceptBeta = SETTINGS.getProperty(Settings.CORE_ACCEPT_BETA_UPDATES, Boolean.class).getValue()
                .booleanValue();

        try {
            final UpdateChecker updateChecker = new UpdateChecker(new URL(UpdateChecker.GITHUB_RELEASES_ADDRESS),
                    getVersion(), (aReleaseData) -> {
                        if (aReleaseData != null) {
                            Platform.runLater(() -> {
                                final Alert alert = new Alert(AlertType.INFORMATION);
                                alert.setTitle("Update available!");
                                alert.setHeaderText("A new version of LSML is available: " + aReleaseData.name);
                                alert.setContentText("The update can be downloaded from: " + aReleaseData.html_url);
                                alert.show();
                            });
                        }

                    }, acceptBeta);
        }
        catch (final MalformedURLException e) {
            // MalformedURL is a programmer error, promote to unchecked, let default
            // exception handler report it.
            throw new RuntimeException(e);
        }
    }

    private static void loadGameFiles(final StringProperty aCurrentFileReport) throws IOException {
        // This method is executed in a background task so that the splash can display while we're doing
        // work. Unfortunately we also need to display dialogs to the FX application thread so there will
        // be some back and forth here.
        while (!GameVFS.isDataFilesAvailable()) {
            final ButtonType useBundled = new ButtonType("Use bundled data");
            final ButtonType browse = new ButtonType("Browse...");
            final ButtonType autoDetect = new ButtonType("Auto detect");
            final ButtonType exit = new ButtonType("Close LSML", ButtonData.CANCEL_CLOSE);

            runInAppThreadAndWait(() -> showDataSourceSelectionDialog(useBundled, browse, autoDetect, exit))
                    .ifPresent(aAction -> {
                        if (aAction == autoDetect) {
                            final Callback<Path, Boolean> confirmationCallback = (aPath) -> runInAppThreadAndWait(
                                    () -> showConfirmGameDirDialog(aPath));

                            if (!GameVFS.autoDetectGameInstall(aCurrentFileReport, confirmationCallback)) {
                                runInAppThreadAndWait(() -> {
                                    final Alert failed = new Alert(AlertType.ERROR,
                                            "Auto detection failed, no game install detected");
                                    failed.showAndWait();
                                    return null;
                                });
                            }
                        }
                        else if (aAction == exit) {
                            System.exit(0);
                        }
                        else {
                            throw new IllegalArgumentException("Unknown action: " + aAction);
                        }
                    });
        }

        final PrintWriter writer = new PrintWriter(System.out);
        DataCache.getInstance(writer);
        writer.flush();

        switch (DataCache.getStatus()) {
            case Builtin:
                break;
            case ParseFailed:
                runInAppThreadAndWait(() -> {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Couldn't read game files...");
                    alert.setHeaderText("LSML was unable to parse the game data files.");
                    alert.setContentText("This usually happens when PGI has changed the structure of the data files "
                            + "in a patch. Please look for an updated version of LSML at www.li-soft.org."
                            + " In the meanwhile LSML will continue to function with the data from the last"
                            + " successfully parsed patch.");
                    alert.showAndWait();
                    return null;
                });
                break;
            default:
                break;

        }

        // Causes static initialisation to be ran.
        ItemDB.lookup("C.A.S.E.");
        StockLoadoutDB.lookup(ChassisDB.lookup("JR7-D"));
        EnvironmentDB.lookupAll();
        UpgradeDB.lookup(3003);
    }

    private static <T> T runInAppThreadAndWait(Callable<T> aRunnable) {
        Task<T> task = new Task<T>() {
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

    private static void setAppUserModelID() {
        if (OS.isWindowsOrNewer(OS.WindowsVersion.Win7)) {
            try {
                // Setup AppUserModelID if windows 7 or later.
                Native.register("shell32");
                setCurrentProcessExplicitAppUserModelID(LiSongMechLab.class.getName());
                Native.unregister();
            }
            catch (final Throwable t) {
                System.out.println("Couldn't call into shell32.dll!");
                System.out.println(t.getMessage());
            }
        }
    }

    private static void setCurrentProcessExplicitAppUserModelID(final String appID) {
        if (SetCurrentProcessExplicitAppUserModelID(new WString(appID)).longValue() != 0)
            throw new RuntimeException("Unable to set current process explicit AppUserModelID to: " + appID);
    }

    private static native NativeLong SetCurrentProcessExplicitAppUserModelID(WString appID);

    private static Boolean showConfirmGameDirDialog(Path aPath) {
        final Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setHeaderText("Is this your primary MWO installation?");
        confirm.setContentText(aPath.toString());
        final Optional<ButtonType> answer = confirm.showAndWait();
        return (answer.isPresent() && answer.get() == ButtonType.OK);
    }

    /**
     * Explains to the user that LSML needs data files, one way or another and prompts the user to either browse for
     * game files, automatically detect the game installation, use bundled data or exit LSML.
     * 
     * This function may change the state of the application settings as a result of user input.
     * 
     * @param useBundled
     *            {@link ButtonType} to use for showing the user the option to use bundled data.
     * @param browse
     *            The {@link ButtonType} to use for showing the user the option to browse for the game install,.
     * @param autoDetect
     *            The {@link ButtonType} to use for showing the user the option of auto detecting the game install.
     * @param exit
     *            The {@link ButtonType} to use for showing the user the option of exiting LSML.
     * @return An {@link Optional} containing either <code>autoDetect</code> (which must be ran in a background thread)
     *         or <code>exit</code> if the user selected those actions otherwise the {@link Optional} is empty.
     */
    private static Optional<ButtonType> showDataSourceSelectionDialog(final ButtonType useBundled,
            final ButtonType browse, final ButtonType autoDetect, final ButtonType exit) {
        final Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Detecting game files...");
        alert.setHeaderText("LSML needs access to game files.");
        alert.setContentText(
                "Normally LSML will parse your game install to find the latest 'Mech and weapon stats automatically."
                        + " To do this LSML needs to know where your game install is, you can choose to browse for it or let"
                        + " LSML auto-detect it for you. If you don't have a game install you can use the bundled data "
                        + "(can be changed from settings page).");

        alert.getButtonTypes().setAll(autoDetect, browse, useBundled, exit);

        final Optional<ButtonType> action = alert.showAndWait();
        if (action.isPresent()) {
            final ButtonType aButton = action.get();

            if (aButton == useBundled) {
                SETTINGS.getProperty(Settings.CORE_FORCE_BUNDLED_DATA, Boolean.class).setValue(Boolean.TRUE);
            }
            else if (aButton == autoDetect) {
                return Optional.of(autoDetect);
            }
            else if (aButton == browse) {
                final DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Browse for MWO installation directory...");
                final File dir = chooser.showDialog(null);
                if (!GameVFS.isValidGameDirectory(dir)) {
                    final Alert error = new Alert(AlertType.ERROR);
                    error.setContentText("That directory is not a valid MWO installation.");
                    error.showAndWait();
                }
                else {
                    final Property<String> installDir = SETTINGS.getProperty(Settings.CORE_GAME_DIRECTORY,
                            String.class);
                    installDir.setValue(dir.getAbsolutePath().toString());
                }
            }
            else if (aButton == exit) {
                return Optional.of(exit);
            }
        }
        return Optional.empty();
    }

    private final GlobalGarage globalGarage = GlobalGarage.instance;

    private LsmlProtocolIPC ipc;

    @Override
    public void start(final Stage aStage) throws Exception {
        // setUserAgentStylesheet("/org/lisoft/lsml/view_fx/BaseStyle.css");

        SplashScreen.showSplash(aStage);

        final Task<Void> startupTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final long startTimeMs = System.currentTimeMillis();
                setAppUserModelID();
                checkForUpdates();
                loadGameFiles(SplashScreen.subTextProperty());
                final long endTimeMs = System.currentTimeMillis();
                final long sleepTimeMs = Math.max(0, MIN_SPLASH_TIME_MS - (endTimeMs - startTimeMs));
                Thread.sleep(sleepTimeMs);

                return null;
            }
        };

        startupTask.setOnSucceeded((aEvent) -> {
            try {
                final Stage mainStage = new Stage();
                mainStage.setTitle("Li Song Mechlab");
                final MainWindow root = new MainWindow(mainStage, coder);
                FxmlHelpers.createStage(mainStage, root);
                SplashScreen.closeSplash();
                int port = Settings.getSettings().getProperty(Settings.CORE_IPC_PORT, Integer.class).getValue();
                ipc = new LsmlProtocolIPC(port, aURL -> {
                    Platform.runLater(() -> {
                        openLoadout(root.getXBar(), aURL);
                    });
                    return null;
                });

                // After prepareShow, the garage exists.
                List<String> params = getParameters().getUnnamed();
                for (String param : params) {
                    openLoadout(root.getXBar(), param);
                }

                aEvent.consume();
            }
            catch (final Exception e) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        });

        startupTask.setOnFailed((aEvent) -> {
            SplashScreen.closeSplash();
            aEvent.consume();
            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                    startupTask.getException());
        });

        new Thread(startupTask).start();
    }

    @Override
    public void stop() throws Exception {
        try {
            globalGarage.saveGarage();
        }
        catch (IOException e) {
            showError(null, e);

            boolean successfull = false;
            while (!successfull) {
                try {
                    globalGarage.saveGarageAs(null);
                    successfull = true;
                }
                catch (IOException e1) {
                    showError(null, e1);
                }
            }
        }
        if (ipc != null) {
            ipc.close(DefaultLoadoutErrorReporter.instance);
        }
        super.stop();
    }

}
