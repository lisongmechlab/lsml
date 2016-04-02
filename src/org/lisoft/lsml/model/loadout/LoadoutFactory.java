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
package org.lisoft.lsml.model.loadout;

import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.view_fx.Settings;

/**
 * This interface can construct a loadout from a base chassis.
 * 
 * @author Emily Björk
 */
public interface LoadoutFactory {

    public Loadout produceEmpty(Chassis aChassis);

    public Loadout produceDefault(Chassis aChassis, Settings aSettings);

    public Loadout produceStock(Chassis aChassis) throws Exception;

    public Loadout produceClone(Loadout aLoadout);
}
