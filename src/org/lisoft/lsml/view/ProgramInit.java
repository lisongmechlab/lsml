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
package org.lisoft.lsml.view;

import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.lisoft.lsml.model.DataCache;
import org.lisoft.lsml.model.chassi.ChassisDB;
import org.lisoft.lsml.model.environment.EnvironmentDB;
import org.lisoft.lsml.model.item.ItemDB;
import org.lisoft.lsml.model.loadout.StockLoadoutDB;
import org.lisoft.lsml.model.upgrades.UpgradeDB;
import org.lisoft.lsml.parsing.export.LsmlProtocolIPC;
import org.lisoft.lsml.parsing.mwo_gamedata.GameVFS;
import org.lisoft.lsml.util.OS;
import org.lisoft.lsml.util.SwingHelpers;
import org.lisoft.lsml.view.UpdateChecker.ReleaseData;
import org.lisoft.lsml.view.UpdateChecker.UpdateCallback;
import org.lisoft.lsml.view.preferences.CorePreferences;
import org.lisoft.lsml.view.preferences.PreferenceStore;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.WString;

/**
 * This class handles the initial program startup. Things that need to be done before the {@link LSML} instance is
 * created. And it does it while showing a nifty splash screen!
 * 
 * @author Li Song
 */
public class ProgramInit {
    private static final long MIN_SPLASH_TIME_MS = 20;
    private static LSML       instanceL;
    public static Image       programIcon        = Toolkit.getDefaultToolkit()
            .getImage(ProgramInit.class.getResource("/resources/icon.png"));

    public static void loadGameFiles() throws IOException {
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

    private static native NativeLong SetCurrentProcessExplicitAppUserModelID(WString appID);

    public static void setCurrentProcessExplicitAppUserModelID(final String appID) {
        if (SetCurrentProcessExplicitAppUserModelID(new WString(appID)).longValue() != 0)
            throw new RuntimeException("Unable to set current process explicit AppUserModelID to: " + appID);
    }

    public static void main(final String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());
        setAppUserModelID();
        checkCliArguments(args);
        setLookAndFeel();

        checkForUpdates();
        SplashScreen.showSplash();
        try {
            long startTimeMs = new Date().getTime();
            loadGameFiles();
            long endTimeMs = new Date().getTime();
            long sleepTimeMs = Math.max(0, MIN_SPLASH_TIME_MS - (endTimeMs - startTimeMs));
            Thread.sleep(sleepTimeMs);
        }
        catch (Exception e) {
            System.exit(1);
        }
        finally {
            SplashScreen.closeSplash();
        }

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                instanceL = new LSML();
                if (args.length > 0) {
                    // This has to be done after other events have been processed and the UI is constructed.
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            instanceL.mechLabPane.openLoadout(args[0]);
                        }
                    });
                }

            }
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

    private static void setLookAndFeel() {
        try {
            // Static global initialization. Stuff that needs to be done before anything else.
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("TabbedPane.opaque", true);
            JFrame.setDefaultLookAndFeelDecorated(true);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Unable to set default look and feel. Something is seriously wrong with your java install!\nError: "
                            + e);
        }
    }

    @SuppressWarnings("unused")
    private static void checkForUpdates() {
        if (!CorePreferences.getCheckForUpdates())
            return;
        
        Date lastUpdate = CorePreferences.getLastUpdateCheck();
        Date now = new Date();
        final long msPerDay = 24*60*60*1000; 
        long diffDays = (now.getTime() - lastUpdate.getTime()) / msPerDay;
        if(diffDays < 3){ // Will check every three days.
            return;
        }
        CorePreferences.setLastUpdateCheck(now);
        
        boolean acceptBeta = CorePreferences.getAcceptBeta();

        try {
            new UpdateChecker(new URL(UpdateChecker.GITHUB_RELEASES_ADDRESS), "1.6.5", new UpdateCallback() {
                @Override
                public void run(final ReleaseData aReleaseData) {
                    if (aReleaseData != null) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                JLabel release = new JLabel(aReleaseData.name);
                                Font f = release.getFont();
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

    public static LSML lsml() {
        return instanceL;
    }
}
