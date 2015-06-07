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
package org.lisoft.lsml.parsing.mwo_gamedata.helpers;

import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.item.Ammunition;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class AmmoTypeStats {
    @XStreamAsAttribute
    public String type;
    @XStreamAsAttribute
    public int    numShots;
    @XStreamAsAttribute
    public double internalDamage;
    
    public Ammunition asAmmunition(ItemStatsModule aStats) {
        final String name = aStats.getUiName();
        final HardPointType aWeaponType;
        if (name.contains("LRM") || name.contains("SRM") || name.contains("NARC")) {
            aWeaponType = HardPointType.MISSILE;
        }
        else if (name.contains("AMS")) {
            aWeaponType = HardPointType.AMS;
        }
        else {
            aWeaponType = HardPointType.BALLISTIC;
        }
        
        return new Ammunition(name, aStats.getUiDesc(), aStats.getMwoKey(), aStats.getMwoId(), aStats.ModuleStats.slots,
                aStats.ModuleStats.tons, HardPointType.NONE, aStats.ModuleStats.health, aStats.getFaction(), numShots, type, aWeaponType, internalDamage);
    }
}
