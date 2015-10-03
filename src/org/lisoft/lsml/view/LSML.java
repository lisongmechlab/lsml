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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import org.lisoft.lsml.model.NotificationMessage;
import org.lisoft.lsml.model.garage.MechGarage;
import org.lisoft.lsml.parsing.export.Base64LoadoutCoder;
import org.lisoft.lsml.parsing.export.LsmlProtocolIPC;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.SwingHelpers;
import org.lisoft.lsml.util.message.Message;
import org.lisoft.lsml.util.message.MessageXBar;
import org.lisoft.lsml.view.action.RedoGarageAction;
import org.lisoft.lsml.view.action.UndoGarageAction;
import org.lisoft.lsml.view.graphs.PayloadSelectionPanel;
import org.lisoft.lsml.view.help.OnlineHelp;
import org.lisoft.lsml.view.mechlab.MechLabPane;
import org.lisoft.lsml.view.preferences.PreferenceStore;
import org.lisoft.lsml.view.preferences.Preferences;

/**
 * This is the main program instance. It contains some program globals and startup and shutdown procedures.
 * 
 * @author Emily Björk
 */
public class LSML extends JFrame implements Message.Recipient {
    public static final String      PROGRAM_FNAME          = "Li Song Mechlab ";
    private static final String     GARAGE_FILEDESCRIPTION = PROGRAM_FNAME + " Garage File (.xml)";
    private static final FileFilter GARAGE_FILE_FILTER     = new FileFilter() {
                                                               @Override
                                                               public String getDescription() {
                                                                   return GARAGE_FILEDESCRIPTION;
                                                               }

                                                               @Override
                                                               public boolean accept(File aArg0) {
                                                                   return aArg0.isDirectory()
                                                                           || (aArg0.isFile() && aArg0.getName()
                                                                                   .toLowerCase().endsWith(".xml"));
                                                               }
                                                           };
    private static final long       serialVersionUID       = -2463321343234141728L;
    private static final String     CMD_UNDO_GARAGE        = "undo garage action";
    private static final String     CMD_REDO_GARAGE        = "redo garage action";

    // Order of definition matters here !
    public final MessageXBar        xBar                   = new MessageXBar();
    public final Preferences        preferences            = new Preferences(xBar);
    public final CommandStack     garageOperationStack   = new CommandStack(256);

    public final Base64LoadoutCoder loadoutCoder           = new Base64LoadoutCoder();
    public final MechLabPane        mechLabPane            = new MechLabPane(xBar, preferences);
    public final JTabbedPane        tabbedPane             = new JTabbedPane();
    final Action                    undoGarageAction       = new UndoGarageAction(xBar);
    final Action                    redoGarageAction       = new RedoGarageAction(xBar);

    private LsmlProtocolIPC         lsmlProtocolIPC;
    private MechGarage              garage;

    public static String getVersion() {
        Class<?> clazz = LSML.class;
        String className = clazz.getSimpleName() + ".class";
        String classPath = clazz.getResource(className).toString();
        if (!classPath.startsWith("jar")) {
            // Class not from JAR
            return "(develop)";
        }
        String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
        try (InputStream stream = new URL(manifestPath).openStream()) {
            Manifest manifest = new Manifest(new URL(manifestPath).openStream());
            Attributes attr = manifest.getMainAttributes();
            String value = attr.getValue("Implementation-Version");
            return value;
        }
        catch (IOException e) {
            return "(develop)";
        }
    }

    public LSML() {
        super(PROGRAM_FNAME + getVersion());
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setIconImage(ProgramInit.programIcon);
        setSize((int) (screenSize.width * 0.9), (int) (screenSize.height * 0.9));
        setLocation(screenSize.width / 2 - getSize().width / 2, screenSize.height / 2 - getSize().height / 2);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setJMenuBar(new MenuBar(this));

        xBar.attach(this);

        JTabbedPane mechTab = new JTabbedPane();
        mechTab.add("By tonnage", new ChassiSelectionPane(preferences, xBar));
        mechTab.add("By payload", new PayloadSelectionPanel());

        tabbedPane.addTab("Mechlab", mechLabPane);
        tabbedPane.addTab("Mechs", mechTab);
        tabbedPane.addTab("Weapons", new WeaponsListView());

        setContentPane(tabbedPane);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (mechLabPane.close()) {
                    shutdown();
                }
            }
        });

        // Open the IPC socket first after everything else has succeeded.
        boolean keepTrying = true;
        int port = Integer.parseInt(PreferenceStore.getString(PreferenceStore.IPC_PORT, "0"));
        if (port < 1024)
            port = LsmlProtocolIPC.DEFAULT_PORT;
        while (keepTrying) {
            try {
                lsmlProtocolIPC = new LsmlProtocolIPC(port);
                keepTrying = false;
            }
            catch (IOException | IllegalArgumentException e) {
                lsmlProtocolIPC = null;

                int ans = JOptionPane.showOptionDialog(this, "LSML was unable to open local socket on port: " + port
                        + " for IPC."
                        + "\nYou can try again with a new (random) port or disable automatic links for this session. ",
                        "Unable to start IPC", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                        new String[] { "Try again (random)", "Try again (manual)", "Disable" }, "Try again");

                if (ans == 0) {
                    // Try again
                    port = (int) (Math.random() * 60000 + 1025);
                    PreferenceStore.setString(PreferenceStore.IPC_PORT, Integer.toString(port));
                }
                else if (ans == 1) {
                    // Try again (manual)
                    boolean hasNewPort = false;
                    while (!hasNewPort) {
                        try {
                            String newPortString = JOptionPane.showInputDialog("Enter a new port:");
                            if (newPortString != null) {
                                port = Integer.parseInt(newPortString);
                                PreferenceStore.setString(PreferenceStore.IPC_PORT, Integer.toString(port));
                            }
                            hasNewPort = true;
                        }
                        catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(this, "Not a number, or too big number. Please try again.");
                        }
                    }
                }
                else {
                    keepTrying = false;
                }
            }
        }
        setupKeybindings();

        openLastGarage();

        ToolTipManager.sharedInstance().setDismissDelay(60000);
    }

    private void setupKeybindings() {
        SwingHelpers.bindAction(getRootPane(), CMD_UNDO_GARAGE, undoGarageAction);
        SwingHelpers.bindAction(getRootPane(), CMD_REDO_GARAGE, redoGarageAction);
    }

    public MechGarage getGarage() {
        return garage;
    }

    public void openLastGarage() {
        assert (SwingUtilities.isEventDispatchThread());

        String garageFileName = PreferenceStore.getString(PreferenceStore.GARAGEFILE_KEY,
                PreferenceStore.GARAGEFILE_DEFAULT);
        File garageFile = new File(garageFileName);
        if (garageFile.exists()) {
            try {
                garage = MechGarage.open(garageFile, xBar);
            }
            catch (Exception e) {
                JOptionPane
                        .showMessageDialog(
                                this,
                                "An error ocurred while opening your last saved garage!\nTo prevent further damage to the garage file, the application will close.\nPlease check your garage file manually, it is located at: "
                                        + garageFile + "\nError: " + e.getMessage(), "Error loading mech garage!",
                                JOptionPane.ERROR_MESSAGE);
                shutdown();
            }
        }
        else {
            Object[] options = { "Create a new garage", "Open a saved garage", "Show Help", "Exit LSML" };

            boolean done = false;

            while (!done) {
                int ans = JOptionPane
                        .showOptionDialog(
                                this,
                                "Your last garage could not be found. This is normal if this is the first time you are using LSML or if you have moved your garage file manually.\n\n"
                                        + "All your mechs will be stored in a \"garage\" and the garage is saved to a \"garage file\".\n"
                                        + "The last opened garage is automatically opened when you start LSML and your currently open garage is always saved automatically on exit.\n"
                                        + "How would you like to proceed?", "No garage found",
                                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
                                options[2]);
                if (ans == 0) {
                    newGarage();
                    saveGarageAs();
                    if (garage.getFile() == null) {
                        JOptionPane.showMessageDialog(this,
                                "Please select a location to store your garage to avoid loosing any of your mechs.",
                                "Garage was not saved", JOptionPane.OK_OPTION);
                    }
                    done = garage.getFile() != null;
                }
                else if (ans == 1) {
                    openGarage();
                    if (garage == null) {
                        JOptionPane.showMessageDialog(this, "A garage must be opened or created.",
                                "No garage selected", JOptionPane.OK_OPTION);
                    }
                    done = garage != null;
                }
                if (ans == 2) {
                    OnlineHelp.openHelp("Garage");
                }
                else if (ans == 3) {
                    shutdown();
                    done = true;
                }
            }
        }
    }

    public void openGarage() {
        assert (SwingUtilities.isEventDispatchThread());

        File startingPosition = garage != null ? garage.getFile() : new File(System.getProperty("user.home"));
        JFileChooser chooser = new JFileChooser(startingPosition);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(GARAGE_FILE_FILTER);

        if (JFileChooser.APPROVE_OPTION != chooser.showOpenDialog(this)) {
            return;
        }
        try {
            garage = MechGarage.open(chooser.getSelectedFile(), xBar);
            PreferenceStore.setString(PreferenceStore.GARAGEFILE_KEY, chooser.getSelectedFile().getAbsolutePath());
        }
        catch (IOException e) {
            JOptionPane.showOptionDialog(this, "Error: " + e.getMessage(), "Couldn't open garage!",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, 0);
        }
    }

    public void newGarage() {
        assert (SwingUtilities.isEventDispatchThread());

        if (garage != null && !garage.getMechs().isEmpty()) {
            try {
                garage.save();
            }
            catch (IOException e) {
                int choice = JOptionPane.showOptionDialog(this, "Error: " + e.getMessage(),
                        "Couldn't save current garage!", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, new String[] { "Don't save", "Cancel", "Pick another file" }, 0);
                if (1 == choice) {
                    return;
                }
                else if (2 == choice) {
                    saveGarageAs();
                }
            }
        }

        garage = new MechGarage(xBar);
    }

    public void saveGarageAs() {
        assert (SwingUtilities.isEventDispatchThread());

        JFileChooser chooser = new JFileChooser(garage.getFile());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(GARAGE_FILE_FILTER);

        while (true) {
            if (JFileChooser.APPROVE_OPTION != chooser.showSaveDialog(this)) {
                break;
            }

            boolean overwrite = false;
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".xml")) {
                file = new File(file.getParentFile(), file.getName() + ".xml");
            }

            if (file.exists()) {
                int choice = JOptionPane.showOptionDialog(this, "How would you like to proceed?",
                        "File already exists!", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                        new String[] { "Overwrite", "Cancel", "Pick another file" }, 0);
                if (0 == choice) {
                    overwrite = true;
                }
                else if (1 == choice) {
                    break;
                }
                else {
                    continue;
                }
            }
            try {
                garage.saveas(file, overwrite);
                PreferenceStore.setString(PreferenceStore.GARAGEFILE_KEY, file.getAbsolutePath());
                break;
            }
            catch (IOException e) {
                int choice = JOptionPane.showOptionDialog(this, "Error was: " + e.getMessage(), "Couldn't save file!",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[] { "Cancel",
                                "Pick another file" }, 0);
                if (choice == 0)
                    break;
            }
        }
    }

    public void saveGarage() {
        assert (SwingUtilities.isEventDispatchThread());

        try {
            if (garage != null)
                garage.save();
        }
        catch (IOException e) {
            saveGarageAs();
        }
    }

    public void shutdown() {
        saveGarage();
        if (lsmlProtocolIPC != null)
            lsmlProtocolIPC.close();
        dispose();
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof NotificationMessage && aMsg.isForMe(null)) {
            NotificationMessage notice = (NotificationMessage) aMsg;
            String message = notice.severity.toString() + ": " + notice.message;
            JOptionPane.showMessageDialog(ProgramInit.lsml(), message);
            Toolkit.getDefaultToolkit().beep();
        }
    }
}
