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
package org.lisoft.lsml.view.mechlab.dropshipframe;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.lisoft.lsml.command.CmdAddDropShipToGarage;
import org.lisoft.lsml.command.CmdDropShipSetLoadout;
import org.lisoft.lsml.command.CmdMoveLoadoutFromGarageToDropShip;
import org.lisoft.lsml.messages.ComponentMessage;
import org.lisoft.lsml.messages.DropShipMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageReception;
import org.lisoft.lsml.model.chassi.MovementProfile;
import org.lisoft.lsml.model.garage.DropShip;
import org.lisoft.lsml.model.garage.MechGarage;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.metrics.TopSpeed;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.view.LSML;
import org.lisoft.lsml.view.LoadoutTransferHandler;
import org.lisoft.lsml.view.ProgramInit;
import org.lisoft.lsml.view.action.AddDropShipToGarageAction;
import org.lisoft.lsml.view.action.RenameDropShipAction;
import org.lisoft.lsml.view.render.StyleManager;

/**
 * This class is a view for a {@link DropShip} model.
 * 
 * @author Emily Björk
 *
 */
public class DropShipFrame extends JInternalFrame implements MessageReceiver {

    static public class LoadoutDisplay extends JPanel implements MessageReceiver {
        private final static String NO_LOADOUT_PANEL   = "NLP";
        private final static String SHOW_LOADOUT_PANEL = "SLP";

        private final JPanel        noLoadoutPanel     = new JPanel();
        private final JPanel        showLoadoutPanel   = new JPanel();
        private final int           bayIndex;
        private final DropShip      dropShip;

        /**
         * @param aMessageReception
         *            Where to listen for messages to the drop ship.
         * @param aBayIndex
         *            Which bay of the {@link DropShip} to display.
         * @param aDropShip
         * 
         */
        public LoadoutDisplay(MessageReception aMessageReception, int aBayIndex, DropShip aDropShip) {
            aMessageReception.attach(this);
            setLayout(new CardLayout());
            setBorder(StyleManager.sectionBorder("'Mech"));
            setTransferHandler(new LoadoutTransferHandler());

            setAlignmentY(SwingConstants.TOP);

            bayIndex = aBayIndex;
            dropShip = aDropShip;

            makeNoLoadoutPanel();
            makeShowLoadoutPanel(dropShip.getMech(bayIndex));

            add(noLoadoutPanel, NO_LOADOUT_PANEL);
            add(showLoadoutPanel, SHOW_LOADOUT_PANEL);

            update();

        }

        public Command makeCopyCommand(MessageDelivery aMessageDelivery, LoadoutBase<?> aLoadout) {
            LoadoutBase<?> loadoutCopy = DefaultLoadoutFactory.instance.produceClone(aLoadout);
            return new CmdDropShipSetLoadout(aMessageDelivery, dropShip, bayIndex, loadoutCopy);
        }

        public Command makeMoveCommand(MessageDelivery aMessageDelivery, MechGarage aGarage, LoadoutBase<?> aLoadout) {
            return new CmdMoveLoadoutFromGarageToDropShip(aMessageDelivery, aGarage, dropShip, bayIndex, aLoadout);
        }

        public int getBayIndex() {
            return bayIndex;
        }

        public DropShip getDropShip() {
            return dropShip;
        }

        private void makeNoLoadoutPanel() {
            JLabel button = new JLabel("Drag from garage");
            noLoadoutPanel.add(button);
        }

        private void makeShowLoadoutPanel(final LoadoutBase<?> aLoadout) {
            showLoadoutPanel.setLayout(new GridBagLayout());

            JLabel protoLabel = new JLabel();
            int tenCharWidth = protoLabel.getFontMetrics(protoLabel.getFont()).stringWidth("----------");

            showLoadoutPanel.setPreferredSize(new Dimension(tenCharWidth * 4, tenCharWidth * 6));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.BASELINE_LEADING;
            gbc.ipadx = 10;

            gbc.gridy = 0;
            gbc.gridx = 0;
            showLoadoutPanel.add(new JButton(new AbstractAction("Open") {
                @Override
                public void actionPerformed(ActionEvent aE) {
                    ProgramInit.lsml().mechLabPane.openLoadout(aLoadout, true);
                }
            }), gbc);
            gbc.gridx++;
            showLoadoutPanel.add(new JButton(new AbstractAction("Remove") {
                @Override
                public void actionPerformed(ActionEvent aE) {
                    try {
                        ProgramInit.lsml().garageCmdStack.pushAndApply(
                                new CmdDropShipSetLoadout(ProgramInit.lsml().xBar, dropShip, bayIndex, null));
                    }
                    catch (Exception e) {
                        // There is no reason for this to fail, promote to unchecked.
                        throw new RuntimeException(e);
                    }
                }
            }), gbc);

            gbc.gridy++;
            gbc.gridx = 0;
            showLoadoutPanel.add(new JLabel("Name:"), gbc);
            gbc.gridx++;
            String name = aLoadout == null ? "-------------------" : aLoadout.getName();
            showLoadoutPanel.add(new JLabel(name), gbc);

            gbc.gridy++;
            gbc.gridx = 0;
            showLoadoutPanel.add(new JLabel("Chassis:"), gbc);
            gbc.gridx++;
            String chassisText = aLoadout == null ? "STORMCROW SCR-PRIME " : aLoadout.getChassis().getNameShort();
            showLoadoutPanel.add(new JLabel(chassisText), gbc);

            gbc.gridy++;
            gbc.gridx = 0;
            showLoadoutPanel.add(new JLabel("Mass:"), gbc);
            gbc.gridx++;
            int tons = aLoadout == null ? 0 : aLoadout.getChassis().getMassMax();
            showLoadoutPanel.add(new JLabel(tons + "t"), gbc);

            gbc.gridy++;
            gbc.gridx = 0;
            showLoadoutPanel.add(new JLabel("Engine:"), gbc);
            gbc.gridx++;
            Engine engine = aLoadout == null ? null : aLoadout.getEngine();
            MovementProfile mp = aLoadout == null ? null : aLoadout.getMovementProfile();
            Collection<Modifier> modifiers = aLoadout == null ? null : aLoadout.getModifiers();
            double topSpeed = engine == null ? 0 : TopSpeed.calculate(engine.getRating(), mp, tons, modifiers);
            DecimalFormat df = new DecimalFormat("###");
            String engineText = engine == null ? "000STD (999km/h)"
                    : engine.getRating() + engine.getType().toString() + " (" + df.format(topSpeed) + "km/h)";
            showLoadoutPanel.add(new JLabel(engineText), gbc);

            gbc.gridy++;
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            showLoadoutPanel.add(Box.createRigidArea(new Dimension(10, 10)), gbc);

            gbc.gridy++;
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            showLoadoutPanel.add(new JLabel("Weapons:"), gbc);

            if (aLoadout != null) {
                HashMap<Weapon, Integer> weapons = new HashMap<>();
                for (Weapon w : aLoadout.items(Weapon.class)) {
                    Integer i = weapons.get(w);
                    if (i == null) {
                        i = 0;
                    }
                    weapons.put(w, i + 1);
                }

                for (Entry<Weapon, Integer> e : weapons.entrySet()) {
                    gbc.gridy++;
                    showLoadoutPanel.add(new JLabel(e.getValue().toString() + "x " + e.getKey().getShortName()), gbc);
                }
            }
            else {
                for (int i = 0; i < 8; ++i) {
                    gbc.gridy++;
                    showLoadoutPanel.add(new JLabel("x"), gbc);
                }
            }

            gbc.gridy++;
            gbc.gridx = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            showLoadoutPanel.add(new JLabel(""), gbc);
        }

        private void update() {
            LoadoutBase<?> loadout = dropShip.getMech(bayIndex);
            CardLayout cl = (CardLayout) (getLayout());
            if (loadout == null) {
                cl.show(this, NO_LOADOUT_PANEL);
            }
            else {
                showLoadoutPanel.removeAll();
                makeShowLoadoutPanel(loadout);
                showLoadoutPanel.revalidate();
                showLoadoutPanel.repaint();
                cl.show(this, SHOW_LOADOUT_PANEL);
            }
        }

        @Override
        public void receive(Message aMsg) {
            if (aMsg instanceof DropShipMessage) {
                update();
            }
            else if (aMsg.isForMe(dropShip.getMech(bayIndex))) {
                if (aMsg instanceof ComponentMessage) {
                    ComponentMessage msg = (ComponentMessage) aMsg;
                    if (msg.isItemsChanged()) {
                        update();
                    }
                }
            }
        }
    }

    private final DropShip dropShip;
    private final JLabel   tonnageLabel = new JLabel("0t");

    public DropShipFrame(MessageReception aMessageReception, MessageDelivery aMessageDelivery, DropShip aDropShip) {
        super(aDropShip.getName(), false, // resizable
                true, // closable
                false, // maximizable
                true);// iconifiable

        dropShip = aDropShip;
        aMessageReception.attach(this);
        setJMenuBar(makeMenuBar(aMessageDelivery));

        // Set the window's location.
        // setLocation(xOffset * openFrameCount, yOffset * openFrameCount);
        // openFrameCount++;
        //
        // JPanel mainPanel = new JPanel(new BorderLayout());
        // mainPanel.add(tabbedPane, BorderLayout.WEST);
        // mainPanel.add(infoPanel, BorderLayout.EAST);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTH;

        gbc.gridy = 1;
        gbc.gridx = 0;
        mainPanel.add(new LoadoutDisplay(aMessageReception, 0, aDropShip), gbc);

        gbc.gridx++;
        mainPanel.add(new LoadoutDisplay(aMessageReception, 1, aDropShip), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        mainPanel.add(new LoadoutDisplay(aMessageReception, 2, aDropShip), gbc);

        gbc.gridx++;
        mainPanel.add(new LoadoutDisplay(aMessageReception, 3, aDropShip), gbc);

        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        mainPanel.add(makeStatsPanel(), gbc);

        setFrameIcon(null);
        setContentPane(mainPanel);
        update();
        pack();
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        addVetoableChangeListener(new VetoableChangeListener() {
            @Override
            public void vetoableChange(PropertyChangeEvent aE) throws PropertyVetoException {
                if (aE.getPropertyName().equals("closed") && aE.getNewValue().equals(true)) {
                    if (!isSaved()) {
                        int ans = JOptionPane.showConfirmDialog(ProgramInit.lsml(),
                                "Would you like to save " + dropShip.getName() + " to your garage?", "Save to garage?",
                                JOptionPane.YES_NO_CANCEL_OPTION);
                        if (ans == JOptionPane.YES_OPTION) {
                            LSML lsml = ProgramInit.lsml();
                            try {
                                lsml.garageCmdStack
                                        .pushAndApply(new CmdAddDropShipToGarage(lsml.getGarage(), dropShip));
                            }
                            catch (Exception e) {
                                // Should never happen
                                throw new RuntimeException(e);
                            }
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
    }

    private JMenuBar makeMenuBar(MessageDelivery aMessageDelivery) {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createMenuDropShip(aMessageDelivery));

        return menuBar;
    }

    private JMenu createMenuDropShip(MessageDelivery aMessageDelivery) {
        JMenu menu = new JMenu("Drop Ship");
        menu.add(new JMenuItem(new AddDropShipToGarageAction(dropShip)));
        menu.add(new JMenuItem(new RenameDropShipAction(dropShip, aMessageDelivery)));
        return menu;
    }

    public boolean isSaved() {
        return ProgramInit.lsml().getGarage().getDropShips().contains(dropShip);
    }

    /**
     * @return
     */
    private JPanel makeStatsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(StyleManager.sectionBorder("Drop Ship"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.ipadx = 10;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;

        gbc.gridy = 0;
        gbc.gridx = 0;
        panel.add(new JLabel("Faction:"), gbc);
        gbc.gridx++;
        gbc.weightx = 1.0;
        panel.add(new JLabel(dropShip.getFaction().getUiName()), gbc);
        gbc.weightx = 0.0;

        gbc.gridx++;
        panel.add(new JLabel("Min/Max Tonnage:"), gbc);
        gbc.gridx++;
        panel.add(new JLabel(dropShip.getMinTonnage() + " / " + dropShip.getMaxTonnage() + "t"), gbc);

        gbc.gridy++;
        gbc.gridx = 2;
        panel.add(new JLabel("Total Tonnage:"), gbc);
        gbc.gridx++;
        panel.add(tonnageLabel, gbc);

        return panel;
    }

    /**
     * @return The {@link DropShip} shown in this frame.
     */
    public DropShip getDropShip() {
        return dropShip;
    }

    private void update() {
        int tonnage = dropShip.getTonnage();
        tonnageLabel.setText(tonnage + "t");
        if (tonnage < dropShip.getMinTonnage() || tonnage > dropShip.getMaxTonnage()) {
            tonnageLabel.setForeground(Color.RED);
        }
        else {
            tonnageLabel.setForeground(new JLabel().getForeground());
        }

        setTitle(dropShip.getName());
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof DropShipMessage) {
            update();
        }
    }
}
