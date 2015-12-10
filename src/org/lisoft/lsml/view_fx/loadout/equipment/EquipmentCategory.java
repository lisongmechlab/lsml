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
package org.lisoft.lsml.view_fx.loadout.equipment;

import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.Item;

/**
 * Classification of equipment into categories.
 * 
 * @author Li Song
 *
 */
public enum EquipmentCategory {
    ENERGY, BALLISTIC, MISSILE, AMS, ECM, ENGINE, MISC;

    public static EquipmentCategory classify(Item aItem) {
        if (aItem instanceof Engine) {
            return EquipmentCategory.ENGINE;
        }

        final HardPointType hardPointType;
        if (aItem instanceof Ammunition)
            hardPointType = ((Ammunition) aItem).getWeaponHardpointType();
        else
            hardPointType = aItem.getHardpointType();

        switch (hardPointType) {
            case AMS:
                return AMS;
            case BALLISTIC:
                return BALLISTIC;
            case ECM:
                return ECM;
            case ENERGY:
                return ENERGY;
            case MISSILE:
                return MISSILE;
            default:
                return MISC;
        }
    }
}
