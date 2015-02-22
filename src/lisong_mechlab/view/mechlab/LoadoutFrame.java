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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;

import lisong_mechlab.model.DynamicSlotDistributor;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.ModuleSlot;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutMessage;
import lisong_mechlab.model.loadout.OpStripArmor;
import lisong_mechlab.model.loadout.OpStripLoadout;
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
import lisong_mechlab.view.action.UndoLoadoutAction;
import lisong_mechlab.view.graphs.DpsGraph;
import lisong_mechlab.view.graphs.SustainedDpsGraph;
import lisong_mechlab.view.render.StyleManager;

public class LoadoutFrame extends JInternalFrame implements Message.Recipient {
    private static final String    CMD_UNDO_LOADOUT      = "undo loadout";
    private static final String    CMD_REDO_LOADOUT      = "redo loadout";
    private static final String    CMD_RENAME_LOADOUT    = "rename loadout";
    private static final String    CMD_SAVE_TO_GARAGE    = "add to garage";
    private static final long      serialVersionUID      = -9181002222136052106L;
    private static int             openFrameCount        = 0;
    private static final int       xOffset               = 30, yOffset = 30;
    private static final int       ARM_OFFSET            = 60;
    private static final int       TORSO_OFFSET          = 20;
    private static final int       HEAD_OFFSET           = 0;
    private final LoadoutBase<?>   loadout;
    private final MessageXBar      xbar;
    private final OperationStack   loadoutOperationStack = new OperationStack(128);
    private final Action           actionUndoLoadout;
    private final Action           actionRedoLoadout;
    private final Action           actionRename;
    private final Action           actionAddToGarage;
    private final LoadoutInfoPanel infoPanel;

    public LoadoutFrame(LoadoutBase<?> aLoadout, MessageXBar aXBar) {
        super(aLoadout.toString(), true, // resizable
                true, // closable
                false, // maximizable
                true);// iconifiable
        xbar = aXBar;
        xbar.attach(this);
        loadout = aLoadout;

        // Actions
        actionUndoLoadout = new UndoLoadoutAction(xbar, this);
        actionRedoLoadout = new RedoLoadoutAction(xbar, this);
        actionRename = new RenameLoadoutAction(this, xbar);
        actionAddToGarage = new AddToGarageAction(loadout);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createMenuLoadout());
        menuBar.add(createMenuArmor());
        menuBar.add(createMenuGraphs());
        menuBar.add(createMenuShare());
        setJMenuBar(menuBar);

        // Set the window's location.
        setLocation(xOffset * openFrameCount, yOffset * openFrameCount);
        openFrameCount++;
        infoPanel = new LoadoutInfoPanel(this, aXBar);

        JPanel root = new JPanel(new BorderLayout());
        JPanel mechview = createMechView(aLoadout, aXBar);
        root.add(mechview, BorderLayout.WEST);
        if (ProgramInit.lsml().preferences.uiPreferences.getCompactMode()) {
            JScrollPane scrollpane = new JScrollPane(infoPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            Dimension preferredSize = new Dimension();
            preferredSize.height = (int) (mechview.getPreferredSize().getHeight() + 1);
            preferredSize.width = (int) (infoPanel.getPreferredSize().getWidth()
                    + scrollpane.getVerticalScrollBar().getPreferredSize().getWidth() + 1);
            scrollpane.setPreferredSize(preferredSize);
            root.add(scrollpane, BorderLayout.EAST);
        }
        else {
            root.add(infoPanel, BorderLayout.EAST);
        }
        root.add(new StatusBar(this, aXBar), BorderLayout.SOUTH);

        setFrameIcon(null);
        setContentPane(root);

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

    private JPanel createComponentPadPanel(final int height, JComponent aChild) {
        final JPanel padPanel = new JPanel();
        padPanel.setLayout(new BoxLayout(padPanel, BoxLayout.LINE_AXIS));

        if (null != aChild) {
            JPanel content = new JPanel() {
                private static final long serialVersionUID = -7026792320508640323L;

                @Override
                public Dimension getMaximumSize() {
                    Dimension d = super.getMaximumSize();
                    d.height = height;
                    return d;
                }
            };
            content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
            content.add(aChild);
            content.add(Box.createVerticalGlue());
            padPanel.add(content);
        }
        padPanel.add(Box.createRigidArea(new Dimension(0, height)));
        return padPanel;
    }

    private JPanel createComponentPanel(JPanel aPadPanel, JPanel aContentPanel, JPanel aContentPanel2) {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(aPadPanel);
        panel.add(aContentPanel);
        if (null != aContentPanel2) {
            panel.add(aContentPanel2);
        }
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel createMechView(LoadoutBase<?> aLoadout, MessageXBar aXBar) {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

        Dimension padding = new Dimension(5, 0);

        panel.add(Box.createRigidArea(padding));

        DynamicSlotDistributor slotDistributor = new DynamicSlotDistributor(loadout);

        JCheckBox symmetricArmor;
        if (ProgramInit.lsml().preferences.uiPreferences.getCompactMode()) {
            symmetricArmor = new JCheckBox("Sym.armr", false);
        }
        else {
            symmetricArmor = new JCheckBox("Symmetric armor", false);
        }
        // symmetricArmor.setAlignmentX(LEFT_ALIGNMENT);

        // Right Arm
        {
            JPanel padPanel = createComponentPadPanel(ARM_OFFSET, symmetricArmor);
            final JPanel arm = new PartPanel(aLoadout, aLoadout.getComponent(Location.RightArm), aXBar, true,
                    slotDistributor, symmetricArmor, loadoutOperationStack);

            JPanel modulesPanel = new JPanel();
            modulesPanel.setLayout(new BoxLayout(modulesPanel, BoxLayout.PAGE_AXIS));
            for (ModuleSlot moduleSlot : ModuleSlot.values()) {
                JPanel modulePanel = new JPanel();
                modulePanel.setBorder(StyleManager.sectionBorder(moduleSlot.toString() + " Modules"));
                modulePanel.setLayout(new BoxLayout(modulePanel, BoxLayout.PAGE_AXIS));
                modulePanel.add(Box.createHorizontalGlue());
                // modulePanel.add(Box.createVerticalGlue());
                modulePanel.add(new PilotModuleList(xbar, loadoutOperationStack, loadout, moduleSlot));
                modulesPanel.add(modulePanel);
            }
            panel.add(createComponentPanel(padPanel, arm, modulesPanel));
        }

        if (!ProgramInit.lsml().preferences.uiPreferences.getCompactMode())
            panel.add(Box.createRigidArea(padding));

        // Right Torso + Leg
        {
            final JPanel torso = new PartPanel(aLoadout, aLoadout.getComponent(Location.RightTorso), aXBar, true,
                    slotDistributor, symmetricArmor, loadoutOperationStack);
            final JPanel leg = new PartPanel(aLoadout, aLoadout.getComponent(Location.RightLeg), aXBar, false,
                    slotDistributor, symmetricArmor, loadoutOperationStack);
            panel.add(createComponentPanel(createComponentPadPanel(TORSO_OFFSET, null), torso, leg));
        }

        if (!ProgramInit.lsml().preferences.uiPreferences.getCompactMode())
            panel.add(Box.createRigidArea(padding));

        // Center Torso + Head
        {
            final JPanel head = new PartPanel(aLoadout, aLoadout.getComponent(Location.Head), aXBar, true,
                    slotDistributor, symmetricArmor, loadoutOperationStack);
            final JPanel torso = new PartPanel(aLoadout, aLoadout.getComponent(Location.CenterTorso), aXBar, true,
                    slotDistributor, symmetricArmor, loadoutOperationStack);
            panel.add(createComponentPanel(createComponentPadPanel(HEAD_OFFSET, null), head, torso));
        }

        if (!ProgramInit.lsml().preferences.uiPreferences.getCompactMode())
            panel.add(Box.createRigidArea(padding));

        // Left Torso + Leg
        {
            final JPanel torso = new PartPanel(aLoadout, aLoadout.getComponent(Location.LeftTorso), aXBar, true,
                    slotDistributor, symmetricArmor, loadoutOperationStack);
            final JPanel leg = new PartPanel(aLoadout, aLoadout.getComponent(Location.LeftLeg), aXBar, false,
                    slotDistributor, symmetricArmor, loadoutOperationStack);
            panel.add(createComponentPanel(createComponentPadPanel(TORSO_OFFSET, null), torso, leg));
        }

        if (!ProgramInit.lsml().preferences.uiPreferences.getCompactMode())
            panel.add(Box.createRigidArea(padding));

        // Left Arm
        {
            final JPanel arm = new PartPanel(aLoadout, aLoadout.getComponent(Location.LeftArm), aXBar, true,
                    slotDistributor, symmetricArmor, loadoutOperationStack);

            final JLabel quirksummary = new JLabel("Quirk summary");
            quirksummary.addMouseListener(new MouseAdapter() {
                JWindow window = null;

                @Override
                public void mouseEntered(MouseEvent aE) {
                    window = new JWindow(ProgramInit.lsml());
                    JLabel text = new JLabel(loadout.getQuirkHtmlSummary());
                    JPanel textPanel = new JPanel();

                    textPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                    textPanel.add(text);
                    textPanel.setBackground(Color.WHITE);
                    window.add(textPanel);
                    window.pack();
                    window.setLocation(aE.getLocationOnScreen());
                    window.setVisible(true);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (window != null) {
                        window.dispose();
                        window = null;
                    }
                }
            });
            quirksummary.setForeground(Color.BLUE);
            Font font = quirksummary.getFont();
            Map<TextAttribute, Object> attributes = new HashMap<>();
            attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            quirksummary.setFont(font.deriveFont(attributes));

            panel.add(createComponentPanel(createComponentPadPanel(ARM_OFFSET, quirksummary), arm, null));
        }
        return panel;
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
        JMenu menu = new JMenu("Loadout");
        menu.add(new JMenuItem(actionAddToGarage));
        menu.add(new JMenuItem(actionUndoLoadout));
        menu.add(new JMenuItem(actionRedoLoadout));
        menu.add(new JMenuItem(actionRename));
        menu.add(new JMenuItem(new DeleteLoadoutAction(xbar, ProgramInit.lsml().getGarage(), this)));
        menu.add(new JMenuItem(new LoadStockAction(loadout, loadoutOperationStack, xbar, this)));
        menu.add(createMenuItem("Strip mech", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent aArg0) {
                loadoutOperationStack.pushAndApply(new OpStripLoadout(loadout, xbar));
            }
        }));

        menu.add(new JMenuItem(new CloneLoadoutAction("Clone", loadout, KeyStroke.getKeyStroke("C"))));
        return menu;
    }

    private JMenu createMenuArmor() {
        JMenu menu = new JMenu("Armor");

        menu.add(createMenuItem("Strip Armor", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent aArg0) {
                loadoutOperationStack.pushAndApply(new OpStripArmor(loadout, xbar));
            }
        }));

        {
            JMenu subMenu = new JMenu("Max Armor");
            menu.add(subMenu);
            subMenu.add(new JMenuItem(new MaxArmorAction("3:1", this, 3, xbar)));
            subMenu.add(new JMenuItem(new MaxArmorAction("5:1", this, 5, xbar)));
            subMenu.add(new JMenuItem(new MaxArmorAction("10:1", this, 10, xbar)));
            subMenu.add(new JMenuItem(new MaxArmorAction("Custom...", this, -1, xbar)));
        }
        return menu;
    }

    private JMenu createMenuGraphs() {
        JMenu menu = new JMenu("Graphs");

        menu.add(createMenuItem("Sustained DPS", new ActionListener() {
            @SuppressWarnings("unused")
            // Constructor has intended side effects.
            @Override
            public void actionPerformed(ActionEvent aArg0) {
                new SustainedDpsGraph(loadout, xbar, infoPanel.getMaxSustainedDPSMetric());
            }
        }));
        menu.add(createMenuItem("Max DPS", new ActionListener() {
            @SuppressWarnings("unused")
            // Constructor has intended side effects.
            @Override
            public void actionPerformed(ActionEvent aArg0) {
                new DpsGraph(loadout, xbar);
            }
        }));
        return menu;
    }

    @Override
    public void receive(Message aMsg) {
        if (!aMsg.isForMe(loadout))
            return;

        // if( aMsg instanceof MechGarage.Message ){
        // MechGarage.Message msg = (MechGarage.Message)aMsg;
        // if( msg.type == MechGarage.Message.Type.LoadoutRemoved ){
        // dispose(); // Closes frame
        // }
        // }
        // else
        if (aMsg instanceof LoadoutMessage) {
            LoadoutMessage msg = (LoadoutMessage) aMsg;
            if (msg.type == LoadoutMessage.Type.RENAME) {
                setTitle(loadout.toString());
            }
        }
    }
}
