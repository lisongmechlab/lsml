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
package org.lisoft.lsml.command;

import org.lisoft.lsml.messages.ComponentMessage;
import org.lisoft.lsml.messages.ComponentMessage.Type;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.OmniPod;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.JumpJet;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentOmniMech;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;

/**
 * This operation changes an {@link OmniPod} on a {@link ConfiguredComponentOmniMech}.
 * 
 * @author Emily Björk
 */
public class CmdSetOmniPod extends CompositeCommand {

    private final ConfiguredComponentOmniMech component;
    private final OmniPod                     newOmniPod;
    private final LoadoutOmniMech             loadout;
    private OmniPod                           oldOmniPod;

    /**
     * Creates a new {@link OmniPod} change {@link Command}.
     * 
     * @param aMessageDelivery
     *            A {@link MessageXBar} to send messages on.
     * @param aLoadout
     *            The {@link LoadoutOmniMech} that the component is a part on.
     * @param aComponentOmniMech
     *            The component to change the {@link OmniPod} on.
     * @param aOmniPod
     *            The new {@link OmniPod} to change to.
     */
    public CmdSetOmniPod(MessageDelivery aMessageDelivery, LoadoutOmniMech aLoadout,
            ConfiguredComponentOmniMech aComponentOmniMech, OmniPod aOmniPod) {
        super("change omnipod on " + aComponentOmniMech.getInternalComponent().getLocation(), aMessageDelivery);
        if (aOmniPod == null)
            throw new IllegalArgumentException("Omnipod must not be null!");

        component = aComponentOmniMech;
        newOmniPod = aOmniPod;
        loadout = aLoadout;
    }

    @Override
    public void buildCommand() throws EquipResult {
        oldOmniPod = component.getOmniPod();

        // Remove all items
        for (Item item : component.getItemsEquipped()) {
            addOp(new CmdRemoveItem(messageBuffer, loadout, component, item));
        }

        // Make sure we respect global jump-jet limit
        int jjLeft = loadout.getJumpJetsMax() + (newOmniPod.getJumpJetsMax() - oldOmniPod.getJumpJetsMax());
        for (ConfiguredComponentOmniMech componentOmniMech : loadout.getComponents()) {
            for (Item item : componentOmniMech.getItemsEquipped()) {
                if (item instanceof JumpJet) {
                    if (jjLeft > 0) {
                        jjLeft--;
                    }
                    else {
                        addOp(new CmdRemoveItem(messageBuffer, loadout, componentOmniMech, item));
                    }
                }
            }
        }

        addOp(new CommandStack.Command() {
            @Override
            protected void undo() {
                loadout.setOmniPod(oldOmniPod);
                messageBuffer.post(new ComponentMessage(component, Type.OmniPodChanged));
                messageBuffer.post(new ComponentMessage(component, Type.ItemsChanged));
            }

            @Override
            public String describe() {
                return "internal omnipod change";
            }

            @Override
            protected void apply() {
                loadout.setOmniPod(newOmniPod);
                messageBuffer.post(new ComponentMessage(component, Type.OmniPodChanged));
                messageBuffer.post(new ComponentMessage(component, Type.ItemsChanged));
            }
        });
    }
}
