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
import javax.swing.Action;
import javax.swing.JOptionPane;

import org.lisoft.lsml.command.CmdStripLoadout;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.view.mechlab.loadoutframe.LoadoutFrame;

/**
 * This action sets the armor to max on the given {@link LoadoutStandard}.
 * 
 * @author Emily Björk
 */
public class StripLoadout extends AbstractAction {
    private static final long    serialVersionUID = -5939335331941199195L;
    private final LoadoutBase<?> loadout;
    private final LoadoutFrame   loadoutFrame;
    private final MessageXBar    xBar;
    private final boolean        removeArmor;

    /**
     * Creates a new {@link StripLoadout}.
     * 
     * @param aLoadout
     *            The {@link LoadoutStandard} to set armor for.
     * @param aXBar
     *            The {@link MessageXBar} to signal armor changes on.
     * @param aRemoveArmor
     *            If <code>true</code> will remove armor too.
     */
    public StripLoadout(LoadoutFrame aLoadout, MessageXBar aXBar, boolean aRemoveArmor) {
        super(aRemoveArmor ? "Strip Everything" : "Strip Equipment");
        loadoutFrame = aLoadout;
        loadout = aLoadout.getLoadout();
        xBar = aXBar;
        removeArmor = aRemoveArmor;
    }

    @Override
    public void actionPerformed(ActionEvent aArg0) {
        try {
            loadoutFrame.getOpStack().pushAndApply(new CmdStripLoadout(loadout, xBar, removeArmor));
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null,  getValue(Action.NAME) +  " failed.\nError: " + e.getMessage());
        }
    }
}
