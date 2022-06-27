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

import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.Garage;
import org.lisoft.lsml.model.garage.GaragePath;
import org.lisoft.lsml.model.loadout.Loadout;

/**
 * This class implements {@link org.lisoft.lsml.messages.Message}s for the {@link Garage} so that other components can
 * react to changes in the garage.
 *
 * @param <T> The value type of the object the message affects.
 * @author Li Song
 */
public class GarageMessage<T extends NamedObject> implements Message {
    public final GaragePath<T> path;
    public final GarageMessageType type;

    public GarageMessage(GarageMessageType aType, GaragePath<T> aParent) {
        type = aType;
        path = aParent;
    }

    @Override
    public boolean affectsHeatOrDamage() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GarageMessage) {
            final GarageMessage<?> that = (GarageMessage<?>) obj;
            return type == that.type && path.equals(that.path);
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime * (prime + path.hashCode()) + type.hashCode();
    }

    @Override
    public boolean isForMe(Loadout aLoadout) {
        return path.getValue().orElse(null) == aLoadout;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append(" [").append(type).append(", ");
        path.toPath(sb);
        sb.append("]");
        return sb.toString();
    }
}