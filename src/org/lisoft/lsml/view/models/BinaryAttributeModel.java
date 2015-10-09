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
package org.lisoft.lsml.view.models;

import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import org.lisoft.lsml.util.message.Message.Recipient;
import org.lisoft.lsml.util.message.MessageReception;
import org.lisoft.lsml.view.ProgramInit;

/**
 * This abstract base class allows toggle button models to be created as anonymous inner classes while still being
 * message recipients.
 * 
 * @author Li Song
 *
 */
public abstract class BinaryAttributeModel extends JToggleButton.ToggleButtonModel implements Recipient {

    public BinaryAttributeModel(MessageReception aMessageReception) {
        aMessageReception.attach(this);
    }

    abstract public void changeValue(boolean aEnabled) throws Exception;

    @Override
    public void setSelected(boolean aEnabled) {
        try {
            changeValue(aEnabled);
        }
        catch (final Exception e) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(ProgramInit.lsml(), e.getMessage());
                }
            });
        }
    }
}
