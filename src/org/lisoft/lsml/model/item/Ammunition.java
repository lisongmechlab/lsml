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
package org.lisoft.lsml.model.item;

import org.lisoft.lsml.model.chassi.HardPointType;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * A generic ammunition item.
 * 
 * @author Emily Björk
 */
public class Ammunition extends Item {
    @XStreamAsAttribute
    protected final int           rounds;
    @XStreamAsAttribute
    protected final double        internalDamage;
    @XStreamAsAttribute
    protected final HardPointType type;
    @XStreamAsAttribute
    protected final String        ammoType;

    public Ammunition(String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons,
            HardPointType aHardpointType, int aHP, Faction aFaction, int aRounds, String aAmmoType, HardPointType aWeaponType, double aInternalDamage) {
        super(aName, aDesc, aMwoName, aMwoId, aSlots, aTons, aHardpointType, aHP, aFaction, null, null);
        
        rounds = aRounds;
        ammoType = aAmmoType;
        type = aWeaponType;
        internalDamage = aInternalDamage;
    }

    public int getNumShots() {
        return rounds;
    }

    /**
     * @return The {@link HardPointType} that the weapon that uses this ammo is using. Useful for color coding and
     *         searching.
     */
    public HardPointType getWeaponHardpointType() {
        return type;
    }

    @Override
    public String getShortName() {
        String name = getName();
        name = name.replace("ULTRA ", "U");
        name = name.replace("MACHINE GUN", "MG");
        return name;
    }

    /**
     * @return The type name of this {@link Ammunition}. Used to match with {@link Weapon} ammo type.
     */
    public String getAmmoType() {
        return ammoType;
    }
}
