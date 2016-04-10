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
import org.lisoft.lsml.model.modifiers.Efficiencies;

public class EfficienciesMessage implements Message {
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EfficienciesMessage) {
            EfficienciesMessage other = (EfficienciesMessage) obj;
            return efficiencies == other.efficiencies && type == other.type;
        }
        return false;
    }

    public EfficienciesMessage(Efficiencies aEfficiencies, EfficienciesMessage.Type aType, boolean aAffectsHeat) {
        efficiencies = aEfficiencies;
        type = aType;
        affectsHeat = aAffectsHeat;
    }

    public static enum Type {
        Changed
    }

    private final Efficiencies efficiencies;
    public final EfficienciesMessage.Type type;
    private final boolean affectsHeat;

    @Override
    public boolean isForMe(Loadout aLoadout) {
        return aLoadout.getEfficiencies() == efficiencies;
    }

    @Override
    public boolean affectsHeatOrDamage() {
        return affectsHeat;
    }
}