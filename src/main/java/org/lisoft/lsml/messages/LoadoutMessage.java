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

/**
 * This message carries information about change to a {@link Loadout} object.
 *
 * @author Li Song
 */
public class LoadoutMessage implements Message {
    // FIXME: Modules should have their own messages
    public enum Type {
        UPDATE, MODULES_CHANGED, WEAPON_GROUPS_CHANGED
    }

    private final Loadout loadout;
    public final Type type;

    public LoadoutMessage(Loadout aLoadout, Type aType) {
        loadout = aLoadout;
        type = aType;
    }

    @Override
    public boolean affectsHeatOrDamage() {
        return type == Type.UPDATE || type == Type.MODULES_CHANGED || type == Type.WEAPON_GROUPS_CHANGED;
    }

    /**
     * @return <code>true</code> if this message affects weapon ranges.
     */
    public boolean affectsRange() {
        return type == Type.MODULES_CHANGED;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LoadoutMessage other = (LoadoutMessage) obj;
        if (loadout == null) {
            if (other.loadout != null) {
                return false;
            }
        }
        else if (!loadout.equals(other.loadout)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (loadout == null ? 0 : loadout.hashCode());
        result = prime * result + (type == null ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean isForMe(Loadout aLoadout) {
        return loadout == null || loadout == aLoadout;
    }
}
