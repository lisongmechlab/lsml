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
package lisong_mechlab.model.garage;

import lisong_mechlab.model.loadout.LoadoutBase;

/**
 * Thrown if the attempt to add a mech to the garage failed due to it already being in the garage.
 * 
 * @author Li Song
 */
public class AlreadyInGarageException extends Exception {
    /**
     * @param aLoadout
     *            The loadout that the error occurred for.
     */
    public AlreadyInGarageException(LoadoutBase<?> aLoadout) {
        super("The loadout \"" + aLoadout.getName() + "\" is already saved to the garage!");
    }

    private static final long serialVersionUID = 4597113571327421920L;
}
