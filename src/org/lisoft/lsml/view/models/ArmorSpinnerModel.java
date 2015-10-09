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
package org.lisoft.lsml.view.models;

import java.awt.Toolkit;

import javax.swing.JCheckBox;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.lisoft.lsml.command.CmdSetArmor;
import org.lisoft.lsml.command.CmdSetArmorSymmetric;
import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.component.ComponentMessage;
import org.lisoft.lsml.model.loadout.component.ComponentMessage.Type;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.message.Message;
import org.lisoft.lsml.util.message.MessageXBar;

public class ArmorSpinnerModel extends SpinnerNumberModel implements Message.Recipient {
    private static final long             serialVersionUID = 2130487332299251881L;
    private final ConfiguredComponentBase part;
    private final ArmorSide               side;
    private final JCheckBox               symmetric;
    private final CommandStack            cmdStack;
    private final MessageXBar             xBar;
    private final LoadoutBase<?>          loadout;

    public ArmorSpinnerModel(LoadoutBase<?> aLoadout, ConfiguredComponentBase aPart, ArmorSide anArmorSide,
            MessageXBar anXBar, JCheckBox aSymmetric, CommandStack anOperationStack) {
        part = aPart;
        loadout = aLoadout;
        side = anArmorSide;
        symmetric = aSymmetric;
        xBar = anXBar;
        xBar.attach(this);
        cmdStack = anOperationStack;
    }

    @Override
    public Object getNextValue() {
        if (part.getArmor(side) < part.getArmorMax(side)) {
            return Integer.valueOf(part.getArmor(side) + 1);
        }
        return Integer.valueOf(part.getArmor(side) + 1);
    }

    @Override
    public Object getPreviousValue() {
        if (part.getArmor(side) < 1)
            return null;
        return Integer.valueOf(part.getArmor(side) - 1);
    }

    @Override
    public Object getValue() {
        return Integer.valueOf(part.getArmor(side));
    }

    @Override
    public void setValue(Object arg0) {
        if (getValue().equals(arg0))
            return;

        try {
            final boolean setSymmetric = symmetric.isSelected()
                    && part.getInternalComponent().getLocation().oppositeSide() != null;
            final int armor = ((Integer) arg0).intValue();

            if (setSymmetric) {
                cmdStack.pushAndApply(new CmdSetArmorSymmetric(xBar, loadout, part, side, armor, true));
            }
            else {
                cmdStack.pushAndApply(new CmdSetArmor(xBar, loadout, part, side, armor, true));
            }
        }
        catch (Exception exception) {
            Toolkit.getDefaultToolkit().beep();
        }
        finally {
            fireStateChanged();
        }
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg.isForMe(loadout) && aMsg instanceof ComponentMessage) {
            ComponentMessage message = (ComponentMessage) aMsg;
            if (message.component != part)
                return;
            if (message.type == Type.ArmorChanged) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        fireStateChanged();
                    }
                });
            }
        }
    }

}
