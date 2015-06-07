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
package org.lisoft.lsml.parsing.export;

import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.MissileWeapon;
import org.lisoft.lsml.model.upgrades.GuidanceUpgrade;

/**
 * This class helps dealing with compatibility issues that arise along the way.
 * 
 * @author Li Song
 */
public class CompatibilityHelper {

    /**
     * February 4th patch introduced new weapon IDs for artemis enabled missile launchers. This function canonizes old
     * missile launchers to the new types if applicable.
     * 
     * @param anItem
     * @param aGuidanceType
     * @return A canonized item.
     */
    public static Item fixArtemis(final Item anItem, GuidanceUpgrade aGuidanceType) {
        Item ans = anItem;
        if (anItem instanceof MissileWeapon) {
            MissileWeapon weapon = (MissileWeapon) anItem;
            ans = aGuidanceType.upgrade(weapon);
        }
        else if (anItem instanceof Ammunition) {
            Ammunition ammunition = (Ammunition) anItem;
            ans = aGuidanceType.upgrade(ammunition);
        }
        return ans;
    }
}
