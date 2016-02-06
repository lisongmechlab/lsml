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

import org.lisoft.lsml.command.CmdAddItem;
import org.lisoft.lsml.command.CmdAutoAddItem;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view.ProgramInit;

/**
 * Adds a given item to the loadout.
 * 
 * @author Emily Björk
 */
public class AddItem extends AbstractAction {

    private final MessageDelivery     messageDelivery;
    private final Loadout<?>          loadout;
    private final ConfiguredComponent component;
    private final Item                item;
    private final CommandStack        cmdStack;

    /**
     * Adds a item to the given component.
     * 
     * @param aTitle
     *            The title for the action.
     * @param aCmdStack
     *            The {@link CommandStack} to use for applying commands.
     * @param aMessageDelivery
     *            Where to deliver change notifications.
     * @param aLoadout
     *            The loadout to modify.
     * @param aComponent
     *            The component to add to.
     * @param aItem
     *            The item to add.
     */
    public AddItem(String aTitle, CommandStack aCmdStack, MessageDelivery aMessageDelivery, Loadout<?> aLoadout,
            ConfiguredComponent aComponent, Item aItem) {
        super(aTitle);
        cmdStack = aCmdStack;
        messageDelivery = aMessageDelivery;
        loadout = aLoadout;
        component = aComponent;
        item = aItem;
    }

    /**
     * Adds a item to the given component using auto-place.
     * 
     * @param aTitle
     *            The title for the action.
     * @param aCmdStack
     *            The {@link CommandStack} to use for applying commands.
     * @param aMessageDelivery
     *            Where to deliver change notifications.
     * @param aLoadout
     *            The loadout to modify.
     * @param aItem
     *            The item to add.
     */
    public AddItem(String aTitle, CommandStack aCmdStack, MessageDelivery aMessageDelivery, Loadout<?> aLoadout,
            Item aItem) {
        super(aTitle);
        cmdStack = aCmdStack;
        messageDelivery = aMessageDelivery;
        loadout = aLoadout;
        component = null;
        item = aItem;
    }

    @Override
    public boolean isEnabled() {
        boolean canEquipGlobally = EquipResult.SUCCESS == loadout.canEquipDirectly(item);
        if (component != null)
            return canEquipGlobally && EquipResult.SUCCESS == component.canEquip(item);
        return canEquipGlobally;
    }

    @Override
    public void actionPerformed(ActionEvent aEvent) {

        try {
            if (component == null) {
                cmdStack.pushAndApply(new CmdAutoAddItem(loadout, messageDelivery, item));
            }
            else {
                cmdStack.pushAndApply(new CmdAddItem(messageDelivery, loadout, component, item));
            }
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(ProgramInit.lsml(), e.getMessage(), "Couldn't add item!",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
