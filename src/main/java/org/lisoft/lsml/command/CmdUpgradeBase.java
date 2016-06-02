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
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.model.upgrades.UpgradesMutable;
import org.lisoft.lsml.util.CommandStack.Command;

/**
 * An abstract package local class that facilitates implementing {@link Command}s that relate to {@link UpgradesMutable}
 * .
 *
 * @author Emily Björk
 */
public abstract class CmdUpgradeBase implements Command {
    protected final transient MessageDelivery messageDelivery;
    private final String description;

    protected CmdUpgradeBase(MessageDelivery aMessageDelivery, String aDescription) {
        description = aDescription;
        messageDelivery = aMessageDelivery;
    }

    @Override
    public String describe() {
        return description;
    }

    EquipResult verifyLoadoutInvariant(Loadout aLoadout) {
        if (aLoadout == null) {
            return EquipResult.SUCCESS;
        }
        if (aLoadout.getFreeMass() < 0) {
            return EquipResult.make(EquipResultType.TooHeavy);
        }
        if (aLoadout.getNumCriticalSlotsFree() < 0) {
            return EquipResult.make(EquipResultType.NotEnoughSlots);
        }
        for (final ConfiguredComponent component : aLoadout.getComponents()) {
            if (component.getSlotsFree() < 0) {
                return EquipResult.make(component.getInternalComponent().getLocation(), EquipResultType.NotEnoughSlots);
            }
        }
        return EquipResult.SUCCESS;
    }
}
