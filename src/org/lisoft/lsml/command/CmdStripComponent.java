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

import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;

/**
 * This {@link Command} will remove all items and armor on this component.
 * 
 * @author Emily Björk
 */
public class CmdStripComponent extends CompositeCommand {
    private final ConfiguredComponentBase component;
    private final LoadoutBase<?>          loadout;
    private final boolean                 removeArmorToo;

    /**
     * @param aComponent
     *            The {@link ConfiguredComponentBase} to strip.
     * @param aMessageDelivery
     *            Where to announce changes from this operation.
     * @param aLoadout
     *            The {@link LoadoutBase} to operate on.
     */
    public CmdStripComponent(MessageDelivery aMessageDelivery, LoadoutBase<?> aLoadout,
            ConfiguredComponentBase aComponent) {
        this(aMessageDelivery, aLoadout, aComponent, true);
    }

    /**
     * @param aComponent
     *            The {@link ConfiguredComponentBase} to strip.
     * @param aMessageDelivery
     *            Where to announce changes from this operation.
     * @param aLoadout
     *            The {@link LoadoutBase} to operate on.
     * @param aRemoveArmorToo
     *            <code>true</code> if armor should be stripped in addition to the equipment.
     */
    public CmdStripComponent(MessageDelivery aMessageDelivery, LoadoutBase<?> aLoadout,
            ConfiguredComponentBase aComponent, boolean aRemoveArmorToo) {
        super("strip part", aMessageDelivery);

        component = aComponent;
        loadout = aLoadout;
        removeArmorToo = aRemoveArmorToo;
    }

    @Override
    public void buildCommand() throws EquipResult {
        // Engine heat sinks are removed together with the engine.
        int hsSkipp = component.getEngineHeatSinks();
        for (Item item : component.getItemsEquipped()) {
            if (!(item instanceof Internal)) {
                if (item instanceof HeatSink) {
                    if (hsSkipp > 0) {
                        hsSkipp--;
                        continue;
                    }
                }
                addOp(new CmdRemoveItem(messageBuffer, loadout, component, item));
            }
        }
        if (removeArmorToo) {
            for (ArmorSide side : ArmorSide.allSides(component.getInternalComponent())) {
                addOp(new CmdSetArmor(messageBuffer, loadout, component, side, 0, false));
            }
        }
    }
}
