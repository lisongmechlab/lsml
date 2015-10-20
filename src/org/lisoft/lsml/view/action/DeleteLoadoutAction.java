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
package org.lisoft.lsml.view.action;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.lisoft.lsml.command.CmdRemoveLoadoutFromGarage;
import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.garage.MechGarage;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.view.ProgramInit;
import org.lisoft.lsml.view.mechlab.loadoutframe.LoadoutFrame;

public class DeleteLoadoutAction extends AbstractAction implements MessageReceiver {
    private static final long    serialVersionUID = -4813215864397617783L;
    private static final String  SHORTCUT_STROKE  = "control D";
    private final LoadoutBase<?> loadout;
    private final MechGarage     garage;
    private final LoadoutFrame   loadoutFrame;

    public DeleteLoadoutAction(MessageXBar anXBar, MechGarage aGarage, LoadoutFrame aLoadoutFrame) {
        this(anXBar, aGarage, aLoadoutFrame, aLoadoutFrame.getLoadout());
    }

    public DeleteLoadoutAction(MessageXBar anXBar, MechGarage aGarage, LoadoutBase<?> aLoadout) {
        this(anXBar, aGarage, null, aLoadout);
    }

    private DeleteLoadoutAction(MessageXBar anXBar, MechGarage aGarage, LoadoutFrame aLoadoutFrame,
            LoadoutBase<?> aLoadout) {
        super("Delete loadout");
        loadoutFrame = aLoadoutFrame;
        loadout = aLoadout;
        garage = aGarage;
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(SHORTCUT_STROKE));
        setEnabled(garage.getMechs().contains(loadout));
        anXBar.attach(this);
    }

    @Override
    public void actionPerformed(ActionEvent aE) {
        if (garage.getMechs().contains(loadout)) {
            Component source = loadoutFrame == null ? ProgramInit.lsml() : loadoutFrame;

            int result = JOptionPane.showConfirmDialog(source,
                    "Are you certain you want to delete the loadout: " + loadout.getName() + "?", "Confirm operation",
                    JOptionPane.YES_NO_OPTION);
            if (JOptionPane.YES_OPTION == result) {
                try {
                    ProgramInit.lsml().garageCmdStack.pushAndApply(new CmdRemoveLoadoutFromGarage(garage, loadout));
                }
                catch (Exception e) {
                    // TODO replace with generic report bug dialog.
                    JOptionPane.showMessageDialog(source,
                            "An error occured!\n"
                                    + "Please report an issue at https://github.com/EmilyBjoerk/lsml/issues and copy paste the following this message:\n"
                                    + e.getMessage() + "\nStack trace:\n" + e.getStackTrace());
                }
            }
        }
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof GarageMessage) {
            GarageMessage msg = (GarageMessage) aMsg;
            if (msg.isForMe(loadout)) {
                setEnabled(garage.getMechs().contains(loadout));
            }
        }
    }
}
