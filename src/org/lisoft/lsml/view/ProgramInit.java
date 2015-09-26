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
package org.lisoft.lsml.view;

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.PrintWriter;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
import org.lisoft.lsml.view.preferences.PreferenceStore;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.WString;

/**
 * This class handles the initial program startup. Things that need to be done before the {@link LSML} instance is
 * created. And it does it while showing a nifty splash screen!
 * 
 * @author Emily Björk
 */
public class ProgramInit extends JFrame {
    private static final long  serialVersionUID   = -2877785947094537320L;
    private static final long  MIN_SPLASH_TIME_MS = 20;
    private static ProgramInit instance;
    private static LSML        instanceL;
    public static Image        programIcon;

    private String             progressSubText    = "";
    private String             progressText       = "";

    private class BackgroundImage extends JComponent {
        private static final long serialVersionUID = 2294812231919303690L;
        private Image             image;

        public BackgroundImage(Image anImage) {
            image = anImage;
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.drawImage(image, 0, 0, this);
            int penX = 20;
            int penY = 250;
            g.setColor(Color.WHITE);
            g.drawString(progressText, penX, penY);
            penY += 15;
            g.drawString(progressSubText, penX, penY);
        }
    }

    ProgramInit() {
        instance = this;
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                Image splash = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/splash.png"));
                setContentPane(new BackgroundImage(splash));
                programIcon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/icon.png"));
                setIconImage(programIcon);
                setResizable(false);
                setUndecorated(true);
                setTitle("loading...");
                setSize(350, 350);

                // This works for multi-screen configurations in linux as well.
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                DisplayMode mode = ge.getDefaultScreenDevice().getDisplayMode();

                setLocation(mode.getWidth() / 2 - getSize().width / 2, mode.getHeight() / 2 - getSize().height / 2);
                setVisible(true);
                getRootPane().setBorder(BorderFactory.createLineBorder(Color.BLACK));
                getRootPane().putClientProperty("Window.shadow", Boolean.TRUE);
            }
        });
    }

    public static void setProcessText(String aString) {
        if (null != instance) {
            instance.progressText = aString;
            instance.repaint();
        }
    }

    public static void setSubText(String aString) {
        if (null != instance) {
            instance.progressSubText = aString;
            instance.repaint();
        }
    }

    public boolean waitUntilDone() {
        long startTimeMs = new Date().getTime();

        try {
            GameVFS.checkGameFilesInstalled();

            PrintWriter writer = new PrintWriter(System.out);
            DataCache.getInstance(writer);
            writer.flush();

            switch (DataCache.getStatus()) {
                case Builtin:
                    break;
                case ParseFailed:
                    JOptionPane
                            .showMessageDialog(
                                    null,
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
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
        finally {
            long endTimeMs = new Date().getTime();
            long sleepTimeMs = Math.max(0, MIN_SPLASH_TIME_MS - (endTimeMs - startTimeMs));
            try {
                Thread.sleep(sleepTimeMs);
            }
            catch (Exception e) {
                // No-Op
            }
            dispose();
            instance = null;
        }
        return true;
    }

    private static native NativeLong SetCurrentProcessExplicitAppUserModelID(WString appID);

    public static void setCurrentProcessExplicitAppUserModelID(final String appID) {
        if (SetCurrentProcessExplicitAppUserModelID(new WString(appID)).longValue() != 0)
            throw new RuntimeException("Unable to set current process explicit AppUserModelID to: " + appID);
    }

    public static void main(final String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());

        if (OS.isWindowsOrNewer(OS.WindowsVersion.Win7)) {
            // Setup AppUserModelID if windows 7 or later.
            Native.register("shell32");
            setCurrentProcessExplicitAppUserModelID(LSML.class.getName());
            Native.unregister();
        }

        // Started with an argument, it's likely a LSML:// protocol string, send it over the IPC and quit.
        if (args.length > 0) {
            int port = Integer.parseInt(PreferenceStore.getString(PreferenceStore.IPC_PORT, "0"));
            if (port < 1024)
                port = LsmlProtocolIPC.DEFAULT_PORT;
            if (LsmlProtocolIPC.sendLoadout(args[0], port))
                return; // Message received we can close this program.
        }

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

        ProgramInit splash = new ProgramInit();
        if (!splash.waitUntilDone()) {
            System.exit(1);
        }

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
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
                catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Unable to start! Error: " + e);
                }
            }
        });
    }

    public static LSML lsml() {
        return instanceL;
    }
}
