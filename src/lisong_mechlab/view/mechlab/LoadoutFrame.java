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
package lisong_mechlab.view.mechlab;

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
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutMessage;
import lisong_mechlab.model.loadout.OpStripLoadout;
import lisong_mechlab.model.metrics.MaxSustainedDPS;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.util.SwingHelpers;
import lisong_mechlab.util.message.Message;
import lisong_mechlab.util.message.MessageXBar;
import lisong_mechlab.view.ProgramInit;
import lisong_mechlab.view.action.AddToGarageAction;
import lisong_mechlab.view.action.CloneLoadoutAction;
import lisong_mechlab.view.action.DeleteLoadoutAction;
import lisong_mechlab.view.action.ExportToLsmlAction;
import lisong_mechlab.view.action.ExportToSmurfyAction;
import lisong_mechlab.view.action.LoadStockAction;
import lisong_mechlab.view.action.MaxArmorAction;
import lisong_mechlab.view.action.RedoLoadoutAction;
import lisong_mechlab.view.action.RenameLoadoutAction;
import lisong_mechlab.view.action.ShowDamageGraphAction;
import lisong_mechlab.view.action.StripArmorAction;
import lisong_mechlab.view.action.UndoLoadoutAction;

public class LoadoutFrame extends JInternalFrame implements Message.Recipient {
    private static final String  CMD_UNDO_LOADOUT      = "undo loadout";
    private static final String  CMD_REDO_LOADOUT      = "redo loadout";
    private static final String  CMD_RENAME_LOADOUT    = "rename loadout";
    private static final String  CMD_SAVE_TO_GARAGE    = "add to garage";
    private static final long    serialVersionUID      = -9181002222136052106L;
    private static final int     xOffset               = 30, yOffset = 30;
    private static int           openFrameCount        = 0;
    private final LoadoutBase<?> loadout;
    private final MessageXBar    xBar;
    private final OperationStack loadoutOperationStack = new OperationStack(128);
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

        // Actions
        actionUndoLoadout = new UndoLoadoutAction(xBar, this);
        actionRedoLoadout = new RedoLoadoutAction(xBar, this);
        actionRename = new RenameLoadoutAction(this, xBar);
        actionAddToGarage = new AddToGarageAction(loadout);

        // Set the window's location.
        setLocation(xOffset * openFrameCount, yOffset * openFrameCount);
        openFrameCount++;

        LoadoutPage loadoutPage = new LoadoutPage(loadout, loadoutOperationStack, xBar);
        MobilityPage mobilityPage = new MobilityPage(loadout, xBar);
        WeaponLabPage weaponLabPage = new WeaponLabPage();

        setJMenuBar(createMenuBar(loadoutPage.getMaxSustainedDPS()));
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Loadout", loadoutPage);
        tabbedPane.addTab("Mobility", mobilityPage);
        tabbedPane.addTab("Weapon Lab.", weaponLabPage);

        setFrameIcon(null);
        setContentPane(tabbedPane);

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

    public OperationStack getOpStack() {
        return loadoutOperationStack;
    }

    private JMenuBar createMenuBar(MaxSustainedDPS aMaxSustainedDPS) {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createMenuLoadout());
        menuBar.add(createMenuArmor());
        menuBar.add(createMenuGraphs(aMaxSustainedDPS));
        menuBar.add(createMenuShare());
        return menuBar;
    }

    private JMenuItem createMenuItem(String text, ActionListener anActionListener) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(anActionListener);
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
                loadoutOperationStack.pushAndApply(new OpStripLoadout(loadout, xBar));
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

    private JMenu createMenuGraphs(MaxSustainedDPS aMaxDPSMetric) {
        JMenu menu = new JMenu("Graphs");
        menu.add(new JMenuItem(new ShowDamageGraphAction(loadout, xBar, aMaxDPSMetric)));
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
