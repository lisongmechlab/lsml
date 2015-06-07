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
package org.lisoft.lsml.model.upgrades;

import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.util.OperationStack.Operation;
import org.lisoft.lsml.util.message.MessageDelivery;

/**
 * An abstract package local class that facilitates implementing {@link Operation}s that relate to
 * {@link UpgradesMutable}.
 * 
 * @author Emily Björk
 */
public abstract class OpUpgradeBase extends Operation {
    protected final transient MessageDelivery messageDelivery;
    private final String                      description;

    protected OpUpgradeBase(MessageDelivery aMessageDelivery, String aDescription) {
        description = aDescription;
        messageDelivery = aMessageDelivery;
    }

    @Override
    public String describe() {
        return description;
    }

    protected void verifyLoadoutInvariant(LoadoutBase<?> aLoadout) {
        if (aLoadout == null)
            return;
        if (aLoadout.getFreeMass() < 0) {
            throw new IllegalArgumentException("Not enough tonnage!");
        }
        if (aLoadout.getNumCriticalSlotsFree() < 0) {
            throw new IllegalArgumentException("Not enough free slots!");
        }
        for (ConfiguredComponentBase loadoutPart : aLoadout.getComponents()) {
            if (loadoutPart.getSlotsFree() < 0) {
                throw new IllegalArgumentException("Not enough free slots!");
            }
        }
    }
}
