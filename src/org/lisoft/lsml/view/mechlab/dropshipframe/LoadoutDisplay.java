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

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.lisoft.lsml.command.CmdDropShipSetLoadout;
import org.lisoft.lsml.command.CmdMoveLoadoutFromGarageToDropShip;
import org.lisoft.lsml.messages.DropShipMessage;
import org.lisoft.lsml.messages.ItemMessage;
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
import org.lisoft.lsml.view.LoadoutTransferHandler;
import org.lisoft.lsml.view.ProgramInit;
import org.lisoft.lsml.view.render.StyleManager;

public class LoadoutDisplay extends JPanel implements MessageReceiver {
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
                    ProgramInit.lsml().garageCmdStack
                            .pushAndApply(new CmdDropShipSetLoadout(ProgramInit.lsml().xBar, dropShip, bayIndex, null));
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
        else {
            LoadoutBase<?> loadout = dropShip.getMech(bayIndex);
            if (loadout != null && aMsg.isForMe(loadout)) {
                if (aMsg instanceof ItemMessage) {
                    update();
                }
            }
        }
    }
}