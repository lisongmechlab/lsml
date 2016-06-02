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
package org.lisoft.lsml.command;

import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.chassi.ArmourSide;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;

/**
 * This {@link Command} will remove all items and armour on this component.
 *
 * @author Li Song
 */
@Deprecated
public class CmdStripComponent extends CompositeCommand {
    private final ConfiguredComponent component;
    private final Loadout loadout;
    private final boolean removeArmourToo;

    /**
     * @param aComponent
     *            The {@link ConfiguredComponent} to strip.
     * @param aMessageDelivery
     *            Where to announce changes from this operation.
     * @param aLoadout
     *            The {@link Loadout} to operate on.
     */
    public CmdStripComponent(MessageDelivery aMessageDelivery, Loadout aLoadout, ConfiguredComponent aComponent) {
        this(aMessageDelivery, aLoadout, aComponent, true);
    }

    /**
     * @param aComponent
     *            The {@link ConfiguredComponent} to strip.
     * @param aMessageDelivery
     *            Where to announce changes from this operation.
     * @param aLoadout
     *            The {@link Loadout} to operate on.
     * @param aRemoveArmourToo
     *            <code>true</code> if armour should be stripped in addition to the equipment.
     */
    public CmdStripComponent(MessageDelivery aMessageDelivery, Loadout aLoadout, ConfiguredComponent aComponent,
            boolean aRemoveArmourToo) {
        super("strip part", aMessageDelivery);

        component = aComponent;
        loadout = aLoadout;
        removeArmourToo = aRemoveArmourToo;
    }

    @Override
    public void buildCommand() throws EquipException {
        // Engine heat sinks are removed together with the engine.
        int hsSkipp = component.getEngineHeatSinks();
        for (final Item item : component.getItemsEquipped()) {
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
        if (removeArmourToo) {
            for (final ArmourSide side : ArmourSide.allSides(component.getInternalComponent())) {
                addOp(new CmdSetArmour(messageBuffer, loadout, component, side, 0, false));
            }
        }
    }
}
