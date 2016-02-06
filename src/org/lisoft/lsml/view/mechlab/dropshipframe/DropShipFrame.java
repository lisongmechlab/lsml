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
package org.lisoft.lsml.view.mechlab.dropshipframe;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.lisoft.lsml.messages.DropShipMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageReception;
import org.lisoft.lsml.model.garage.DropShip;
import org.lisoft.lsml.view.LSML;
import org.lisoft.lsml.view.ProgramInit;
import org.lisoft.lsml.view.action.AddDropShipToGarageAction;
import org.lisoft.lsml.view.action.RenameDropShipAction;

/**
 * This class is a view for a {@link DropShip} model.
 * 
 * @author Li Song
 *
 */
public class DropShipFrame extends JInternalFrame implements MessageReceiver {

    private final class CloseVetoListener implements VetoableChangeListener {

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
                            lsml.garageCmdStack.pushAndApply(new CmdAddDropShipToGarage(lsml.getGarage(), dropShip));
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
    }

    private final DropShip         dropShip;
    private final JLabel           tonnageLabel = new JLabel("0t");
    private final MessageReception reception;

    public DropShipFrame(MessageReception aMessageReception, MessageDelivery aMessageDelivery, DropShip aDropShip) {
        super(aDropShip.getName(), false, // resizable
                true, // closable
                false, // maximizable
                true);// iconifiable
        reception = aMessageReception;
        dropShip = aDropShip;

        reception.attach(this);
        setJMenuBar(makeMenuBar(aMessageDelivery));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTH;

        gbc.gridy = 1;
        gbc.gridx = 0;
        mainPanel.add(new LoadoutDisplay(reception, 0, aDropShip), gbc);

        gbc.gridx++;
        mainPanel.add(new LoadoutDisplay(reception, 1, aDropShip), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        mainPanel.add(new LoadoutDisplay(reception, 2, aDropShip), gbc);

        gbc.gridx++;
        mainPanel.add(new LoadoutDisplay(reception, 3, aDropShip), gbc);

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

        addVetoableChangeListener(new CloseVetoListener());
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

    @Override
    public void dispose() {
        super.dispose();
        reception.detach(this);
    }
}
