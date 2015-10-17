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
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.garage.MechGarage;
import org.lisoft.lsml.view.ProgramInit;

/**
 * This action will undo a change to the garage.
 * 
 * @author Li Song
 */
public class UndoGarageAction extends AbstractAction implements MessageReceiver {
    private static final long   serialVersionUID = 665074705972425989L;
    private static final String SHORTCUT_STROKE  = "control shift Z";

    public UndoGarageAction(MessageXBar anXBar) {
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(SHORTCUT_STROKE));
        anXBar.attach(this);
        setEnabled(false); // Initially
    }

    @Override
    public Object getValue(String key) {
        if (key == Action.NAME) {
            if (isEnabled()) {
                return "Undo " + ProgramInit.lsml().garageCmdStack.nextUndo().describe();
            }
            return "Undo Garage";
        }
        return super.getValue(key);
    }

    @Override
    public void actionPerformed(ActionEvent aArg0) {
        ProgramInit.lsml().garageCmdStack.undo();
    }

    @Override
    public void receive(final Message aMsg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (aMsg instanceof MechGarage.GarageMessage) {
                    if (ProgramInit.lsml() == null || ProgramInit.lsml().garageCmdStack == null)
                        setEnabled(false);
                    else
                        setEnabled(null != ProgramInit.lsml().garageCmdStack.nextUndo());
                    firePropertyChange(NAME, "", getValue(NAME));
                }
            }
        });
    }
}
