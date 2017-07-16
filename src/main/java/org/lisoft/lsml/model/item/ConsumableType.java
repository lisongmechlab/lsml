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
package org.lisoft.lsml.model.item;

/**
 * All the different types of consumables present in MWO.
 *
 * @author Li Song
 */
public enum ConsumableType {
    STRATEGIC_STRIKE, COOLANT_FLUSH, UAV, UNKNOWN;

    /**
     * Determines the consumable type from the MWO equipType string.
     *
     * @param aEquipType
     *            The string from the data file's equipType field.
     * @return A {@link ConsumableType}.
     */
    public static ConsumableType fromMwo(String aEquipType) {
        switch (aEquipType.toLowerCase()) {
            case "strategicstrike":
                return STRATEGIC_STRIKE;
            case "uav":
                return UAV;
            case "coolantflush":
                return COOLANT_FLUSH;
            default:
                return UNKNOWN;
        }
    }

}
