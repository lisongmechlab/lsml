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

import org.lisoft.lsml.model.garage.DropShip;
import org.lisoft.lsml.model.garage.MechGarage;
import org.lisoft.lsml.model.loadout.LoadoutBase;

/**
 * This class implements {@link org.lisoft.lsml.messages.Message}s for the {@link MechGarage} so that other
 * components can react to changes in the garage.
 * 
 * @author Emily Björk
 */
public class GarageMessage implements Message {
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((garage == null) ? 0 : garage.hashCode());
        result = prime * result + ((loadout == null) ? 0 : loadout.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GarageMessage) {
            GarageMessage that = (GarageMessage) obj;
            return this.garage == that.garage && this.type == that.type && this.loadout == that.loadout;
        }
        return false;
    }

    public enum Type {
        LoadoutAdded, LoadoutRemoved, NewGarage, Saved, DropShipRemoved, DropShipAdded
    }

    public final GarageMessage.Type            type;
    public final MechGarage      garage;
    private final LoadoutBase<?> loadout;
    public final DropShip        dropShip;

    public GarageMessage(GarageMessage.Type aType, MechGarage aGarage, LoadoutBase<?> aLoadout) {
        type = aType;
        garage = aGarage;
        loadout = aLoadout;
        dropShip = null;
    }

    public GarageMessage(GarageMessage.Type aType, MechGarage aGarage, DropShip aDropShip) {
        type = aType;
        garage = aGarage;
        loadout = null;
        dropShip = aDropShip;
    }

    public GarageMessage(GarageMessage.Type aType, MechGarage aGarage) {
        this(aType, aGarage, (DropShip) null);
    }

    @Override
    public boolean isForMe(LoadoutBase<?> aLoadout) {
        return aLoadout == loadout;
    }

    @Override
    public boolean affectsHeatOrDamage() {
        return false;
    }
}