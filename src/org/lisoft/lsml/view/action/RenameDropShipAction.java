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
package org.lisoft.lsml.view.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.garage.DropShip;
import org.lisoft.lsml.view.ProgramInit;
import org.lisoft.lsml.view.mechlab.dropshipframe.DropShipFrame;

public class RenameDropShipAction extends AbstractAction {
    private static final String   SHORTCUT_STROKE  = "control R";
    private final DropShipFrame   frame;
    private final DropShip        dropShip;
    private final MessageDelivery messageDelivery;

    public RenameDropShipAction(DropShip aLoadout, MessageDelivery aMessageDelivery) {
        super("Rename drop ship...");
        dropShip = aLoadout;
        frame = null;
        messageDelivery = aMessageDelivery;
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(SHORTCUT_STROKE));
    }

    public RenameDropShipAction(DropShipFrame aLoadoutFrame, MessageDelivery aXBar) {
        super("Rename drop ship...");
        dropShip = aLoadoutFrame.getDropShip();
        frame = aLoadoutFrame;
        messageDelivery = aXBar;
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(SHORTCUT_STROKE));
    }

    @Override
    public void actionPerformed(ActionEvent aE) {
        String name = JOptionPane.showInputDialog(frame, "Give a name", dropShip.getName());
        if (name == null || name.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No name given!");
            return;
        }
        try {
            ProgramInit.lsml().garageCmdStack.pushAndApply(new CmdSetDropShipName(dropShip, messageDelivery, name));
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Change name failed.\nError: " + e.getMessage());
        }
    }
}
