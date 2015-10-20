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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.lisoft.lsml.command.CmdChangeEngine;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view.ProgramInit;

/**
 * Attempts to change the engine of the given loadout.
 * 
 * @author Emily Björk
 */
public class ChangeEngine extends AbstractAction {
    private final LoadoutStandard loadout;
    private final CommandStack    cmdStack;
    private final Engine          engine;
    private final MessageDelivery messageDelivery;

    public ChangeEngine(MessageDelivery aMessageDelivery, String aTitle, CommandStack aCmdStack, Engine aEngine,
            LoadoutStandard aLoadout) {
        super(aTitle);
        messageDelivery = aMessageDelivery;
        cmdStack = aCmdStack;
        engine = aEngine;
        loadout = aLoadout;
    }

    @Override
    public void actionPerformed(ActionEvent aEvent) {
        try {
            cmdStack.pushAndApply(new CmdChangeEngine(messageDelivery, loadout,
                    ItemDB.getEngine(engine.getRating(), engine.getType(), engine.getFaction())));
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(ProgramInit.lsml(), e.getMessage(), "Couldn't change engine!",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

}
