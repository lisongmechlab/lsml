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
package lisong_mechlab.model.chassi;

/**
 * This enumeration represents the weight class of a chassis: Light, Medium, Heavy or Assault. It provides a way to
 * determine weight class from tonnage.
 * 
 * @author Li Song
 */
public enum ChassisClass {
    LIGHT, MEDIUM, HEAVY, ASSAULT, COLOSSAL;

    private final static double TONNAGE_EPSILON = Math.ulp(100) * 5.0;

    /**
     * Determines the {@link ChassisClass} from a tonnage amount.
     * 
     * @param tons
     *            The tonnage to calculate from.
     * @return The {@link ChassisClass} matching the argument.
     */
    public static ChassisClass fromMaxTons(double tons) {
        if (tons < 40 - TONNAGE_EPSILON) {
            return ChassisClass.LIGHT;
        }
        else if (tons < 60 - TONNAGE_EPSILON) {
            return ChassisClass.MEDIUM;
        }
        else if (tons < 80 - TONNAGE_EPSILON) {
            return ChassisClass.HEAVY;
        }
        else {
            return ChassisClass.ASSAULT;
        }
    }
}
