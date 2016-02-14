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
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.swing.JOptionPane;

import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.datacache.DataCache;
import org.lisoft.lsml.model.datacache.EnvironmentDB;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.datacache.StockLoadoutDB;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.datacache.gamedata.GameVFS;
import org.lisoft.lsml.model.export.LsmlProtocolIPC;
import org.lisoft.lsml.model.garage.Garage;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.OS;
import org.lisoft.lsml.view.LSML;
import org.lisoft.lsml.view_fx.loadout.LoadoutWindow;
import org.lisoft.lsml.view_fx.util.FxmlHelpers;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.WString;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * This is the main application for the LSML JavaFX GUI.
 * 
 * @author Emily Björk
 */
public class LiSongMechLab extends Application {
    public static final long             MIN_SPLASH_TIME_MS = 20;
    private static final Settings        SETTINGS           = Settings.getSettings();

    @Deprecated // Devise a better solution
    public static ObservableList<String> active_style_sheets;

    public static final String           DEVELOP_VERSION    = "(develop)";

    public static String getVersion() {
        Class<?> clazz = LiSongMechLab.class;
        String className = clazz.getSimpleName() + ".class";
        String classPath = clazz.getResource(className).toString();
        if (!classPath.startsWith("jar")) {
            // Class not from JAR
            return DEVELOP_VERSION;
        }
        String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
        try (InputStream stream = new URL(manifestPath).openStream()) {
            Manifest manifest = new Manifest(new URL(manifestPath).openStream());
            Attributes attr = manifest.getMainAttributes();
            String value = attr.getValue("Implementation-Version");
            return value;
        }
        catch (IOException e) {
            return DEVELOP_VERSION;
        }
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());
        checkCliArguments(args);
        launch(args);
    }

    public static void openLoadout(MessageXBar aGlobalXBar, Loadout aLoadout, Garage aGarage) {
        Stage stage = new Stage();
        LoadoutWindow root = new LoadoutWindow(aGlobalXBar, aLoadout, aGarage, stage);
        FxmlHelpers.createStage(stage, root);
    }

    /**
     * @param aUrl
     */
    public static void openLoadout(String aUrl) {
        throw new UnsupportedOperationException("NYI");
    }

    public static boolean safeCommand(Node aOwner, CommandStack aStack, Command aCommand) {
        try {
            aStack.pushAndApply(aCommand);
        }
        catch (Exception e) {
            LiSongMechLab.showError(aOwner, e);
            return false;
        }
        return true;
    }

    public static void showError(Node aOwner, Exception aException) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR, aException.getMessage(), ButtonType.CLOSE);
            if (null != aOwner) {
                alert.initOwner(aOwner.getScene().getWindow());
            }
            alert.getDialogPane().getStylesheets().addAll(active_style_sheets);
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

        boolean acceptBeta = SETTINGS.getProperty(Settings.CORE_ACCEPT_BETA_UPDATES, Boolean.class).getValue()
                .booleanValue();

        try {
            UpdateChecker updateChecker = new UpdateChecker(new URL(UpdateChecker.GITHUB_RELEASES_ADDRESS),
                    getVersion(), (aReleaseData) -> {
                        if (aReleaseData != null) {
                            Platform.runLater(() -> {
                                Alert alert = new Alert(AlertType.INFORMATION);
                                alert.setTitle("Update available!");
                                alert.setHeaderText("A new version of LSML is available: " + aReleaseData.name);
                                alert.setContentText("The update can be downloaded from: " + aReleaseData.html_url);
                                alert.show();
                            });
                        }

                    } , acceptBeta);
        }
        catch (MalformedURLException e) {
            // MalformedURL is a programmer error, promote to unchecked, let default
            // exception handler report it.
            throw new RuntimeException(e);
        }

    }

    private static void loadGameFiles() throws IOException {
        GameVFS.checkGameFilesInstalled();

        PrintWriter writer = new PrintWriter(System.out);
        DataCache.getInstance(writer);
        writer.flush();

        switch (DataCache.getStatus()) {
            case Builtin:
                break;
            case ParseFailed:
                JOptionPane.showMessageDialog(null,
                        "Reading the game files failed. This is most likely due to changes in the last patch.\n\n"
                                + "LSML will still function with data from the last successfull parse.\n"
                                + "Please update LSML to the latest version to be sure you have the latest game data.",
                        "Game file parse failed", JOptionPane.INFORMATION_MESSAGE);
                break;
            default:
                break;

        }

        // Causes static initialization to be ran.
        ItemDB.lookup("C.A.S.E.");
        StockLoadoutDB.lookup(ChassisDB.lookup("JR7-D"));
        EnvironmentDB.lookupAll();
        UpgradeDB.lookup(3003);
    }

    private static void setAppUserModelID() {
        if (OS.isWindowsOrNewer(OS.WindowsVersion.Win7)) {
            try {
                // Setup AppUserModelID if windows 7 or later.
                Native.register("shell32");
                setCurrentProcessExplicitAppUserModelID(LSML.class.getName());
                Native.unregister();
            }
            catch (Throwable t) {
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

    @Override
    public void start(Stage aStage) throws Exception {
        // setUserAgentStylesheet("/org/lisoft/lsml/view_fx/BaseStyle.css");

        SplashScreen.showSplash(aStage);

        Task<MainWindow> startupTask = new Task<MainWindow>() {
            @Override
            protected MainWindow call() throws Exception {
                long startTimeMs = System.currentTimeMillis();
                setAppUserModelID();
                checkForUpdates();
                loadGameFiles();
                MainWindow root = new MainWindow();
                active_style_sheets = root.getStylesheets();

                long endTimeMs = System.currentTimeMillis();
                long sleepTimeMs = Math.max(0, MIN_SPLASH_TIME_MS - (endTimeMs - startTimeMs));
                Thread.sleep(sleepTimeMs);

                return root;
            }
        };

        startupTask.setOnSucceeded((aEvent) -> {
            try {
                final Stage mainStage = new Stage();
                mainStage.setTitle("Li Song Mechlab");
                final MainWindow root = startupTask.get();
                FxmlHelpers.createStage(mainStage, root);
                SplashScreen.closeSplash();
                root.prepareShow();
                aEvent.consume();
            }
            catch (Exception e) {
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
}
