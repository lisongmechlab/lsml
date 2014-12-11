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
package lisong_mechlab.model.loadout.component;

import lisong_mechlab.model.chassi.OmniPod;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.loadout.LoadoutOmniMech;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase.ComponentMessage.Type;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.util.OperationStack.CompositeOperation;
import lisong_mechlab.util.OperationStack.Operation;
import lisong_mechlab.util.message.MessageDelivery;
import lisong_mechlab.util.message.MessageXBar;

/**
 * This operation changes an {@link OmniPod} on a {@link ConfiguredComponentOmniMech}.
 * 
 * @author Li Song
 */
public class OpChangeOmniPod extends CompositeOperation {

    private final ConfiguredComponentOmniMech component;
    private final OmniPod                     newOmniPod;
    private final LoadoutOmniMech             loadout;
    private OmniPod                           oldOmniPod;

    /**
     * Creates a new {@link OmniPod} change {@link Operation}.
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
    public OpChangeOmniPod(MessageDelivery aMessageDelivery, LoadoutOmniMech aLoadout,
            ConfiguredComponentOmniMech aComponentOmniMech, OmniPod aOmniPod) {
        super("change omnipod on " + aComponentOmniMech.getInternalComponent().getLocation(), aMessageDelivery);
        if (aOmniPod == null)
            throw new IllegalArgumentException("Omnipod must not be null!");

        component = aComponentOmniMech;
        newOmniPod = aOmniPod;
        loadout = aLoadout;
    }

    @Override
    public void buildOperation() {
        oldOmniPod = component.getOmniPod();

        // Remove all items
        for (Item item : component.getItemsEquipped()) {
            addOp(new OpRemoveItem(messageBuffer, loadout, component, item));
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
                        addOp(new OpRemoveItem(messageBuffer, loadout, componentOmniMech, item));
                    }
                }
            }
        }

        addOp(new OperationStack.Operation() {
            @Override
            protected void undo() {
                loadout.setOmniPod(oldOmniPod);
                messageBuffer.post(new ConfiguredComponentBase.ComponentMessage(component, Type.OmniPodChanged));
                messageBuffer.post(new ConfiguredComponentBase.ComponentMessage(component, Type.ItemsChanged));
            }

            @Override
            public String describe() {
                return "internal omnipod change";
            }

            @Override
            protected void apply() {
                loadout.setOmniPod(newOmniPod);
                messageBuffer.post(new ConfiguredComponentBase.ComponentMessage(component, Type.OmniPodChanged));
                messageBuffer.post(new ConfiguredComponentBase.ComponentMessage(component, Type.ItemsChanged));
            }
        });
    }
}
