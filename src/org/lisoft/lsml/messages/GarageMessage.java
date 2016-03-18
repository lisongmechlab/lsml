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

import java.util.Optional;

import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.Garage;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.loadout.Loadout;

/**
 * This class implements {@link org.lisoft.lsml.messages.Message}s for the {@link Garage} so that other components can
 * react to changes in the garage.
 * 
 * @author Li Song
 */
public class GarageMessage implements Message {
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((garageDir == null) ? 0 : garageDir.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GarageMessage) {
            GarageMessage that = (GarageMessage) obj;
            return this.garageDir == that.garageDir && this.type == that.type && this.value == that.value;
        }
        return false;
    }

    public final GarageMessageType                                type;
    public final Optional<GarageDirectory<? extends NamedObject>> garageDir;
    public final Optional<? extends NamedObject>                  value;

    public GarageMessage(GarageMessageType aType, GarageDirectory<? extends NamedObject> aGarageDirectory,
            NamedObject aValue) {
        this(aType, Optional.ofNullable(aGarageDirectory), Optional.ofNullable(aValue));
    }

    public GarageMessage(GarageMessageType aType, Optional<GarageDirectory<? extends NamedObject>> aGarageDirectory,
            Optional<? extends NamedObject> aValue) {
        type = aType;
        garageDir = aGarageDirectory;
        value = aValue;
    }

    public GarageMessage(GarageMessageType aType) {
        this(aType, Optional.empty(), Optional.empty());
    }

    @Override
    public boolean isForMe(Loadout aLoadout) {
        return value.isPresent() && aLoadout == value.get();
    }

    @Override
    public boolean affectsHeatOrDamage() {
        return false;
    }
}