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

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.util.OperationStack.CompositeOperation;
import lisong_mechlab.util.OperationStack.Operation;
import lisong_mechlab.util.message.MessageDelivery;

/**
 * This {@link Operation} will remove all items and armor on this component.
 * 
 * @author Li Song
 */
public class OpStripComponent extends CompositeOperation {
    /**
     * @param aLoadoutPart
     *            The {@link ConfiguredComponentBase} to strip.
     * @param aMessageDelivery
     *            Where to announce changes from this operation.
     * @param aLoadout
     *            The {@link LoadoutBase} to operate on.
     */
    public OpStripComponent(MessageDelivery aMessageDelivery, LoadoutBase<?> aLoadout,
            ConfiguredComponentBase aLoadoutPart) {
        super("strip part", aMessageDelivery);
        // Engine heat sinks are removed together with the engine.
        int hsSkipp = aLoadoutPart.getEngineHeatsinks();
        for (Item item : aLoadoutPart.getItemsEquipped()) {
            if (!(item instanceof Internal)) {
                if (item instanceof HeatSink) {
                    if (hsSkipp > 0) {
                        hsSkipp--;
                        continue;
                    }
                }
                addOp(new OpRemoveItem(messageBuffer, aLoadout, aLoadoutPart, item));
            }
        }
        if (aLoadoutPart.getInternalComponent().getLocation().isTwoSided()) {
            addOp(new OpSetArmor(messageBuffer, aLoadout, aLoadoutPart, ArmorSide.FRONT, 0, false));
            addOp(new OpSetArmor(messageBuffer, aLoadout, aLoadoutPart, ArmorSide.BACK, 0, false));
        }
        else {
            addOp(new OpSetArmor(messageBuffer, aLoadout, aLoadoutPart, ArmorSide.ONLY, 0, false));
        }
    }

    @Override
    public void buildOperation() {
        // No-op The preparation is invariant of time and performed in constructor
    }
}
