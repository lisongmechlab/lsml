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

import java.util.Optional;

import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.Garage;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.loadout.Loadout;

/**
 * This class implements {@link org.lisoft.lsml.messages.Message}s for the {@link Garage} so that other components can
 * react to changes in the garage.
 *
 * @author Emily Björk
 * @param <T>
 *            The value type of the object the message affects.
 */
public class GarageMessage<T extends NamedObject> implements Message {
    /**
     * This is the parent directory of the value or directory the message affects. Is only <code>null</code> if the
     * message affects the root directory.
     */
    public final GarageDirectory<T> parentDir;

    /**
     * Optional, the {@link GarageDirectory} the message affects.
     */
    public final Optional<GarageDirectory<T>> directory;

    /**
     * Optional, the {@link NamedObject} the message affects.
     */
    public final Optional<T> value;

    public final GarageMessageType type;

    public GarageMessage(GarageMessageType aType, GarageDirectory<T> aParent, GarageDirectory<T> aValue) {
        this(aType, aParent, Optional.of(aValue), Optional.empty());
    }

    public GarageMessage(GarageMessageType aType, GarageDirectory<T> aParent, Optional<GarageDirectory<T>> aDirectory,
            Optional<T> aValue) {
        type = aType;
        parentDir = aParent;
        directory = aDirectory;
        value = aValue;
    }

    public GarageMessage(GarageMessageType aType, GarageDirectory<T> aParent, T aValue) {
        this(aType, aParent, Optional.empty(), Optional.of(aValue));
    }

    @Override
    public boolean affectsHeatOrDamage() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof GarageMessage)) {
            return false;
        }
        final GarageMessage<?> other = (GarageMessage<?>) obj;
        if (directory == null) {
            if (other.directory != null) {
                return false;
            }
        }
        else if (!directory.equals(other.directory)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        }
        else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((directory == null) ? 0 : directory.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean isForMe(Loadout aLoadout) {
        return value.isPresent() && aLoadout == value.get();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append(" [").append(type).append(", ").append(directory).append(", ")
                .append(value).append("]");
        return super.toString();
    }
}