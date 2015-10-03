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
package org.lisoft.lsml.view.mechlab;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import javax.swing.Action;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;

import org.lisoft.lsml.command.CmdStripLoadout;
import org.lisoft.lsml.model.graphs.AlphaStrikeGraphModel;
import org.lisoft.lsml.model.graphs.MaxDpsGraphModel;
import org.lisoft.lsml.model.graphs.SustainedDpsGraphModel;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutMessage;
import org.lisoft.lsml.model.loadout.LoadoutMetrics;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.SwingHelpers;
import org.lisoft.lsml.util.message.Message;
import org.lisoft.lsml.util.message.MessageXBar;
import org.lisoft.lsml.view.ProgramInit;
import org.lisoft.lsml.view.action.AddToGarageAction;
import org.lisoft.lsml.view.action.CloneLoadoutAction;
import org.lisoft.lsml.view.action.DeleteLoadoutAction;
import org.lisoft.lsml.view.action.ExportToLsmlAction;
import org.lisoft.lsml.view.action.ExportToSmurfyAction;
import org.lisoft.lsml.view.action.LoadStockAction;
import org.lisoft.lsml.view.action.MaxArmorAction;
import org.lisoft.lsml.view.action.RedoLoadoutAction;
import org.lisoft.lsml.view.action.RenameLoadoutAction;
import org.lisoft.lsml.view.action.ShowDamageGraphAction;
import org.lisoft.lsml.view.action.StripArmorAction;
import org.lisoft.lsml.view.action.UndoLoadoutAction;

public class LoadoutFrame extends JInternalFrame implements Message.Recipient {
    private static final String  CMD_UNDO_LOADOUT      = "undo loadout";
    private static final String  CMD_REDO_LOADOUT      = "redo loadout";
    private static final String  CMD_RENAME_LOADOUT    = "rename loadout";
    private static final String  CMD_SAVE_TO_GARAGE    = "add to garage";
    private static final long    serialVersionUID      = -9181002222136052106L;
    private static final int     xOffset               = 30, yOffset = 30;
    private static int           openFrameCount        = 0;
    private final LoadoutBase<?> loadout;
    private final LoadoutMetrics metrics;
    private final MessageXBar    xBar;
    private final CommandStack loadoutOperationStack = new CommandStack(128);
    private final Action         actionUndoLoadout;
    private final Action         actionRedoLoadout;
    private final Action         actionRename;
    private final Action         actionAddToGarage;

    public LoadoutFrame(LoadoutBase<?> aLoadout, MessageXBar aXBar) {
        super(aLoadout.toString(), true, // resizable
                true, // closable
                false, // maximizable
                true);// iconifiable
        xBar = aXBar;
        xBar.attach(this);
        loadout = aLoadout;
        metrics = new LoadoutMetrics(loadout, null, xBar);

        // Actions
        actionUndoLoadout = new UndoLoadoutAction(xBar, this);
        actionRedoLoadout = new RedoLoadoutAction(xBar, this);
        actionRename = new RenameLoadoutAction(this, xBar);
        actionAddToGarage = new AddToGarageAction(loadout);

        // Set the window's location.
        setLocation(xOffset * openFrameCount, yOffset * openFrameCount);
        openFrameCount++;

        LoadoutPanel loadoutPage = new LoadoutPanel(loadout, loadoutOperationStack, xBar);

        LoadoutInfoPanel infoPanel = new LoadoutInfoPanel(loadout, metrics, loadoutOperationStack, aXBar);
        if (ProgramInit.lsml().preferences.uiPreferences.getCompactMode()) {
            JScrollPane scrollpane = new JScrollPane(infoPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            Dimension preferredSize = new Dimension();
            preferredSize.height = (int) (loadoutPage.getPreferredSize().getHeight() + 1);
            preferredSize.width = (int) (infoPanel.getPreferredSize().getWidth()
                    + scrollpane.getVerticalScrollBar().getPreferredSize().getWidth() + 1);
            scrollpane.setPreferredSize(preferredSize);
            add(scrollpane, BorderLayout.EAST);
        }

        setJMenuBar(createMenuBar());
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Loadout", loadoutPage);
        tabbedPane.addTab("Statistics", new MechStatisticsPanel(loadout, xBar, metrics, loadoutPage));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tabbedPane, BorderLayout.WEST);
        mainPanel.add(infoPanel, BorderLayout.EAST);

        setFrameIcon(null);
        setContentPane(mainPanel);

        pack();
        setVisible(true);

        addVetoableChangeListener(new VetoableChangeListener() {
            @Override
            public void vetoableChange(PropertyChangeEvent aE) throws PropertyVetoException {
                if (aE.getPropertyName().equals("closed") && aE.getNewValue().equals(true)) {
                    if (!isSaved()) {
                        int ans = JOptionPane.showConfirmDialog(ProgramInit.lsml(),
                                "Would you like to save " + loadout.getName() + " to your garage?", "Save to garage?",
                                JOptionPane.YES_NO_CANCEL_OPTION);
                        if (ans == JOptionPane.YES_OPTION) {
                            (new AddToGarageAction(loadout)).actionPerformed(null);
                        }
                        else if (ans == JOptionPane.NO_OPTION) {
                            // Discard loadout
                        }
                        else {
                            throw new PropertyVetoException("Save canceled!", aE);
                        }
                    }
                }
            }
        });
        setupKeybindings();
    }

    private void setupKeybindings() {
        SwingHelpers.bindAction(this, CMD_UNDO_LOADOUT, actionUndoLoadout);
        SwingHelpers.bindAction(this, CMD_REDO_LOADOUT, actionRedoLoadout);
        SwingHelpers.bindAction(this, CMD_RENAME_LOADOUT, actionRename);
        SwingHelpers.bindAction(this, CMD_SAVE_TO_GARAGE, actionAddToGarage);
    }

    public boolean isSaved() {
        return ProgramInit.lsml().getGarage().getMechs().contains(loadout);
    }

    public LoadoutBase<?> getLoadout() {
        return loadout;
    }

    public LoadoutMetrics getMetrics() {
        return metrics;
    }

    public CommandStack getOpStack() {
        return loadoutOperationStack;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createMenuLoadout());
        menuBar.add(createMenuArmor());
        menuBar.add(createMenuGraphs());
        menuBar.add(createMenuShare());
        return menuBar;
    }

    private JMenuItem createMenuItem(String aText, ActionListener aActionListener) {
        JMenuItem item = new JMenuItem(aText);
        item.addActionListener(aActionListener);
        return item;
    }

    private JMenu createMenuShare() {
        JMenu menu = new JMenu("Share!");
        menu.add(new JMenuItem(new ExportToLsmlAction(this)));
        menu.add(new JMenuItem(new ExportToSmurfyAction(this)));
        return menu;
    }

    private JMenu createMenuLoadout() {
        JMenu loadoutMenu = new JMenu("Loadout");
        loadoutMenu.add(new JMenuItem(actionAddToGarage));
        loadoutMenu.add(new JMenuItem(actionUndoLoadout));
        loadoutMenu.add(new JMenuItem(actionRedoLoadout));
        loadoutMenu.add(new JMenuItem(actionRename));
        loadoutMenu.add(new JMenuItem(new DeleteLoadoutAction(xBar, ProgramInit.lsml().getGarage(), this)));
        loadoutMenu.add(new JMenuItem(new LoadStockAction(loadout, loadoutOperationStack, xBar, this)));
        loadoutMenu.add(createMenuItem("Strip mech", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent aArg0) {
                loadoutOperationStack.pushAndApply(new CmdStripLoadout(loadout, xBar));
            }
        }));

        loadoutMenu.add(new JMenuItem(new CloneLoadoutAction("Clone", loadout, KeyStroke.getKeyStroke("C"))));
        return loadoutMenu;
    }

    private JMenu createMenuArmor() {
        JMenu armorMenu = new JMenu("Armor");
        armorMenu.add(new JMenuItem(new StripArmorAction(this, xBar)));

        JMenu maxArmorMenu = new JMenu("Max Armor");
        armorMenu.add(maxArmorMenu);
        maxArmorMenu.add(new JMenuItem(new MaxArmorAction("3:1", this, 3, xBar)));
        maxArmorMenu.add(new JMenuItem(new MaxArmorAction("5:1", this, 5, xBar)));
        maxArmorMenu.add(new JMenuItem(new MaxArmorAction("10:1", this, 10, xBar)));
        maxArmorMenu.add(new JMenuItem(new MaxArmorAction("Custom...", this, -1, xBar)));
        return armorMenu;
    }

    private JMenu createMenuGraphs() {
        JMenu menu = new JMenu("Graphs");
        menu.add(new JMenuItem(new ShowDamageGraphAction(loadout, xBar, new AlphaStrikeGraphModel(metrics, loadout))));
        menu.add(new JMenuItem(new ShowDamageGraphAction(loadout, xBar, new SustainedDpsGraphModel(metrics, loadout))));
        menu.add(new JMenuItem(new ShowDamageGraphAction(loadout, xBar, new MaxDpsGraphModel(loadout))));
        return menu;
    }

    @Override
    public void receive(Message aMsg) {
        if (!aMsg.isForMe(loadout))
            return;

        if (aMsg instanceof LoadoutMessage) {
            LoadoutMessage msg = (LoadoutMessage) aMsg;
            if (msg.type == LoadoutMessage.Type.RENAME) {
                setTitle(loadout.toString());
            }
        }
    }
}
