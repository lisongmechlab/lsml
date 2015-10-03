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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.JList;
import javax.swing.JOptionPane;

import org.lisoft.lsml.command.CmdLoadStock;
import org.lisoft.lsml.model.chassi.ChassisBase;
import org.lisoft.lsml.model.chassi.ChassisDB;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.message.MessageXBar;
import org.lisoft.lsml.view.ProgramInit;

/**
 * This action loads a stock loadout. If the chassis has multiple variations the user is prompted for which stock
 * loadout to load.
 * 
 * @author Li Song
 */
public class LoadStockAction extends AbstractAction {
    private static final long    serialVersionUID = 4350731510583942480L;
    private final LoadoutBase<?> loadout;
    private final CommandStack stack;
    private final MessageXBar    xBar;
    private final Component      component;

    /**
     * Creates a new {@link LoadStockAction}.
     * 
     * @param aLoadout
     *            The {@link LoadoutStandard} to load stock for.
     * @param aStack
     *            The {@link CommandStack} stack that shall be used for undo information.
     * @param aXBar
     *            The {@link MessageXBar} that shall be used for signaling changes to the {@link LoadoutStandard}.
     * @param aComponent
     *            The {@link Component} on which any dialogs will be centered.
     */
    public LoadStockAction(LoadoutBase<?> aLoadout, CommandStack aStack, MessageXBar aXBar, Component aComponent) {
        super(getActionName(aLoadout.getChassis()));
        loadout = aLoadout;
        stack = aStack;
        xBar = aXBar;
        component = aComponent;
    }

    @Override
    public void actionPerformed(ActionEvent aArg0) {
        final Collection<? extends ChassisBase> variations = ChassisDB.lookupVariations(loadout.getChassis());

        try {
            if (variations.size() == 1) {
                stack.pushAndApply(new CmdLoadStock(loadout.getChassis(), loadout, xBar));
            }
            else {
                JList<ChassisBase> list = new JList<>(variations.toArray(new ChassisBase[variations.size()]));
                JOptionPane.showConfirmDialog(component, list, "Which stock loadout?", JOptionPane.OK_CANCEL_OPTION);
                if (list.getSelectedValue() != null) {
                    stack.pushAndApply(new CmdLoadStock(list.getSelectedValue(), loadout, xBar));
                }
            }
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(ProgramInit.lsml(), "Couldn't load stock loadout! Error: " + e.getMessage());
        }
    }

    private static String getActionName(ChassisBase aChassis) {
        if (ChassisDB.lookupVariations(aChassis).size() > 1) {
            return "Load stock...";
        }
        return "Load stock";
    }
}
