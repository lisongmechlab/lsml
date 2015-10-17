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

import org.lisoft.lsml.command.CmdSetMaxArmor;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.view.mechlab.loadoutframe.LoadoutFrame;

/**
 * This action sets the armor to max on the given {@link LoadoutStandard}.
 * 
 * @author Emily Björk
 */
public class MaxArmorAction extends AbstractAction {
    private static final long    serialVersionUID = -5939335331941199195L;
    private final LoadoutBase<?> loadout;
    private final double         ratio;
    private final LoadoutFrame   loadoutFrame;
    private final MessageXBar    xBar;

    /**
     * Creates a new {@link MaxArmorAction}. If <code>aRatio</code> is less than or equal to 0, the user is prompted for
     * a ratio.
     * 
     * @param aTitle
     *            The title to give to the action.
     * @param aLoadout
     *            The {@link LoadoutStandard} to set armor for.
     * @param aRatio
     *            The ratio between back and front as: <code>front/back</code>
     * @param anXBar
     *            The {@link MessageXBar} to signal armor changes on.
     */
    public MaxArmorAction(String aTitle, LoadoutFrame aLoadout, double aRatio, MessageXBar anXBar) {
        super(aTitle);
        loadoutFrame = aLoadout;
        loadout = aLoadout.getLoadout();
        ratio = aRatio;
        xBar = anXBar;
    }

    @Override
    public void actionPerformed(ActionEvent aArg0) {
        try {
            if (ratio > 0) {
                loadoutFrame.getOpStack().pushAndApply(new CmdSetMaxArmor(loadout, xBar, ratio, true));
            }
            else {
                String input = (String) JOptionPane.showInputDialog(loadoutFrame,
                        "Please enter the ratio between front and back armor as front:back.Example 3:1",
                        "Maximizing armor...", JOptionPane.INFORMATION_MESSAGE, null, null, "3:1");
                if (input == null) {
                    return;
                }

                String[] s = input.split(":");
                if (s.length == 2) {
                    double front, back;
                    try {
                        front = Double.parseDouble(s[0]);
                        back = Double.parseDouble(s[1]);
                    }
                    catch (Exception e) {
                        JOptionPane.showMessageDialog(loadoutFrame, "Error parsing ratio! Loadout was not changed!");
                        return;
                    }
                    loadoutFrame.getOpStack().pushAndApply(new CmdSetMaxArmor(loadout, xBar, front / back, true));
                }
                else
                    JOptionPane.showMessageDialog(loadoutFrame, "Error parsing ratio! Loadout was not changed!");
            }
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(loadoutFrame, "Unable to set max armor! Error: " + e.getMessage());
        }
    }
}
