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
package org.lisoft.lsml.messages;

import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.modifiers.Efficiencies;

public class EfficienciesMessage implements Message {
    public enum Type {
        Changed
    }

    private final Efficiencies efficiencies;
    public final EfficienciesMessage.Type type;
    private final boolean affectsHeat;

    public EfficienciesMessage(Efficiencies aEfficiencies, EfficienciesMessage.Type aType, boolean aAffectsHeat) {
        efficiencies = aEfficiencies;
        type = aType;
        affectsHeat = aAffectsHeat;
    }

    @Override
    public boolean affectsHeatOrDamage() {
        return affectsHeat;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EfficienciesMessage) {
            final EfficienciesMessage other = (EfficienciesMessage) obj;
            return efficiencies == other.efficiencies && type == other.type;
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime * (Boolean.hashCode(affectsHeat) + prime * (efficiencies.hashCode() + prime * type.hashCode()));
    }

    @Override
    public boolean isForMe(Loadout aLoadout) {
        return aLoadout.getEfficiencies() == efficiencies;
    }
}