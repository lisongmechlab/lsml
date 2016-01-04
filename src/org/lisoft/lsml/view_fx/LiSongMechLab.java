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
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.datacache.DataCache;
import org.lisoft.lsml.model.datacache.EnvironmentDB;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.datacache.StockLoadoutDB;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.datacache.gamedata.GameVFS;
import org.lisoft.lsml.model.export.LsmlProtocolIPC;
import org.lisoft.lsml.model.garage.MechGarage;
import org.lisoft.lsml.model.item.Equipment;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.OS;
import org.lisoft.lsml.util.SwingHelpers;
import org.lisoft.lsml.view.LSML;
import org.lisoft.lsml.view.preferences.CorePreferences;
import org.lisoft.lsml.view.preferences.PreferenceStore;
import org.lisoft.lsml.view_fx.UpdateChecker.ReleaseData;
import org.lisoft.lsml.view_fx.UpdateChecker.UpdateCallback;
import org.lisoft.lsml.view_fx.controls.FixedRowsListView;
import org.lisoft.lsml.view_fx.loadout.LoadoutWindow;
import org.lisoft.lsml.view_fx.loadout.component.ComponentPane;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.lsml.view_fx.util.FxmlHelpers;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.WString;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * This is the main application for the LSML JavaFX GUI.
 * 
 * @author Emily Björk
 */
public class LiSongMechLab extends Application {
    // public static final DataFormat ITEM_DATA_FORMAT = new DataFormat("item.custom");
    // public static final DataFormat MODULE_DATA_FORMAT = new DataFormat("module.custom");
    public static final long              MIN_SPLASH_TIME_MS = 20;

    private static ObservableList<String> active_style_sheets;

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

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());
        checkCliArguments(args);
        launch(args);
    }

    public static void openLoadout(LoadoutBase<?> aLoadout, MechGarage aGarage) throws IOException {
        Stage stage = new Stage();
        LoadoutWindow root = new LoadoutWindow(aLoadout, aGarage, stage);
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
            int port = Integer.parseInt(PreferenceStore.getString(PreferenceStore.IPC_PORT, "0"));
            if (port < 1024)
                port = LsmlProtocolIPC.DEFAULT_PORT;
            if (LsmlProtocolIPC.sendLoadout(args[0], port)) {
                System.exit(0);
            }
        }
    }

    @SuppressWarnings("unused")
    private static void checkForUpdates() {
        if (!CorePreferences.getCheckForUpdates())
            return;

        Date lastUpdate = CorePreferences.getLastUpdateCheck();
        Date now = new Date();
        final long msPerDay = 24 * 60 * 60 * 1000;
        long diffDays = (now.getTime() - lastUpdate.getTime()) / msPerDay;
        if (diffDays < 3) { // Will check every three days.
            return;
        }
        CorePreferences.setLastUpdateCheck(now);

        boolean acceptBeta = CorePreferences.getAcceptBeta();

        try {
            new UpdateChecker(new URL(UpdateChecker.GITHUB_RELEASES_ADDRESS), LSML.getVersion(), new UpdateCallback() {
                @Override
                public void run(final ReleaseData aReleaseData) {
                    if (aReleaseData != null) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                JLabel release = new JLabel(aReleaseData.name);
                                java.awt.Font f = release.getFont();
                                release.setFont(f.deriveFont(2.0f * f.getSize2D()));

                                JLabel downloadLink = new JLabel();
                                SwingHelpers.hypertextLink(downloadLink, aReleaseData.html_url, aReleaseData.html_url);

                                final JPanel message = new JPanel();
                                message.setLayout(new BoxLayout(message, BoxLayout.PAGE_AXIS));
                                message.add(new JLabel("A new update is available!"));
                                message.add(release);
                                message.add(new JLabel("Download from here:"));
                                message.add(downloadLink);

                                JCheckBox checkUpdates = new JCheckBox("Automatically check for udpates");
                                checkUpdates.setModel(CorePreferences.UPDATE_CHECK_FOR_UPDATES_MODEL);
                                message.add(Box.createVerticalStrut(15));
                                message.add(checkUpdates);

                                JCheckBox acceptBetaCheckbox = new JCheckBox("Accept beta releases");
                                acceptBetaCheckbox.setModel(CorePreferences.UPDATE_ACCEPT_BETA_MODEL);
                                message.add(acceptBetaCheckbox);

                                JOptionPane.showMessageDialog(null, message, "Update available!",
                                        JOptionPane.INFORMATION_MESSAGE);
                            }
                        });
                    }

                }
            }, acceptBeta);
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

        new Thread(() -> {
            try {
                setAppUserModelID();
                checkForUpdates();
                long startTimeMs = System.currentTimeMillis();
                loadGameFiles();
                Parent root = FXMLLoader.load(getClass().getResource("/org/lisoft/lsml/view_fx/GarageWindow.fxml"));
                active_style_sheets = root.getStylesheets();

                long endTimeMs = System.currentTimeMillis();
                long sleepTimeMs = Math.max(0, MIN_SPLASH_TIME_MS - (endTimeMs - startTimeMs));
                Thread.sleep(sleepTimeMs);

                Platform.runLater(() -> {
                    Stage mainStage = new Stage();
                    mainStage.setTitle("Li Song Mechlab");
                    try {
                        FxmlHelpers.createStage(mainStage, root);
                    }
                    catch (Exception e) {
                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                        System.exit(1);
                    }
                    SplashScreen.closeSplash();
                });
            }
            catch (Throwable t) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), t);
                System.exit(1);
            }
        }).start();
    }
}
