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
package org.lisoft.lsml.messages;

import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.upgrades.Upgrades;

public class UpgradesMessage implements Message {
    public final UpgradesMessage.ChangeMsg msg;
    private final Upgrades source;

    public enum ChangeMsg {
        GUIDANCE, STRUCTURE, ARMOUR, HEATSINKS
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UpgradesMessage) {
            UpgradesMessage other = (UpgradesMessage) obj;
            return msg == other.msg && source == other.source;
        }
        return false;
    }

    public UpgradesMessage(UpgradesMessage.ChangeMsg aChangeMsg, Upgrades anUpgrades) {
        msg = aChangeMsg;
        source = anUpgrades;
    }

    @Override
    public boolean isForMe(Loadout aLoadout) {
        return aLoadout.getUpgrades() == source;
    }

    @Override
    public boolean affectsHeatOrDamage() {
        if (msg == ChangeMsg.HEATSINKS)
            return true;
        return false; // Changes to the items that are a side effect of change to upgrades can affect but the item
                      // messages will trigger that already.
    }
}