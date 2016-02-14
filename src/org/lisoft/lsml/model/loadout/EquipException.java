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
package org.lisoft.lsml.model.loadout;

/**
 * Thrown when an equipment related operation failed with an excepton.
 * 
 * @author Li Song
 */
public class EquipException extends Exception {
    private static final long serialVersionUID = -4061303636318772812L;
    private final EquipResult result;

    public EquipException(EquipResult aResult) {
        result = aResult;
    }

    public static void checkAndThrow(EquipResult aResult) throws EquipException {
        if (aResult != EquipResult.SUCCESS) {
            throw new EquipException(aResult);
        }
    }

    @Override
    public String getMessage() {
        return result.toString();
    }

}
