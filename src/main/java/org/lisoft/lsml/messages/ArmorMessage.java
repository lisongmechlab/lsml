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
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;

public class ArmorMessage implements Message {
    public enum Type {
        ARMOR_CHANGED, ARMOR_DISTRIBUTION_UPDATE_REQUEST
    }

    /**
     * True if this message was automatically in response to a change.
     */
    public final boolean             manualArmor;
    public final ConfiguredComponent component;
    public final Type                type;

    public ArmorMessage(ConfiguredComponent aComponent, Type aType) {
        this(aComponent, aType, false);
    }

    public ArmorMessage(ConfiguredComponent aComponent, Type aType, boolean aManualArmor) {
        component = aComponent;
        type = aType;
        manualArmor = aManualArmor;
    }

    @Override
    public boolean affectsHeatOrDamage() {
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (manualArmor ? 1231 : 1237);
        result = prime * result + ((component == null) ? 0 : component.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArmorMessage) {
            ArmorMessage other = (ArmorMessage) obj;
            return component == other.component && type == other.type && manualArmor == other.manualArmor;
        }
        return false;
    }

    @Override
    public boolean isForMe(Loadout aLoadout) {
        return aLoadout.getComponents().contains(component);
    }

    @Override
    public String toString() {
        return type.toString() + " for " + component.getInternalComponent().getLocation().toString();
    }
}