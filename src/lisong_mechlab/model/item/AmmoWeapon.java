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
package lisong_mechlab.model.item;

import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.mwo_data.helpers.ItemStatsWeapon;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class AmmoWeapon extends Weapon {
    @XStreamAsAttribute
    private final String ammoTypeId;

    public AmmoWeapon(ItemStatsWeapon aStatsWeapon, HardPointType aHardpointType) {
        this(aStatsWeapon, aHardpointType, aStatsWeapon.WeaponStats.ammoType);
    }

    public AmmoWeapon(ItemStatsWeapon aStatsWeapon, HardPointType aHardpointType, String aAmmoType) {
        super(aStatsWeapon, aHardpointType);
        ammoTypeId = aAmmoType;
    }

    public boolean isCompatibleAmmo(Ammunition aAmmunition) {
        return ammoTypeId.equals(aAmmunition.getAmmoType());
    }

    @Override
    public String getShortName() {
        String name = getName();
        name = name.replace("ANTI-MISSILE SYSTEM", "AMS");
        return name;
    }

    /**
     * @return The {@link String} name of the ammo type required for this weapon.
     */
    public String getAmmoType() {
        return ammoTypeId;
    }
}
