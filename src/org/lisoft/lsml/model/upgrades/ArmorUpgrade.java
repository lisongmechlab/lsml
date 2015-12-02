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
package org.lisoft.lsml.model.upgrades;

import org.lisoft.lsml.model.item.Faction;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Represents an upgrade to a 'mechs armor.
 * 
 * @author Li Song
 */
public class ArmorUpgrade extends Upgrade {
    @XStreamAsAttribute
    private final int    slots;
    @XStreamAsAttribute
    private final double armorPerTon;

    public ArmorUpgrade(String aName, String aDescription, int aMwoId, Faction aFaction, int aExtraSlots,
            double aArmorPerTon) {
        super(aName, aDescription, aMwoId, aFaction);
        slots = aExtraSlots;
        armorPerTon = aArmorPerTon;
    }

    /**
     * @return The number of extra slots required by this upgrade.
     */
    public int getExtraSlots() {
        return slots;
    }

    /**
     * @return The number of points of armor per ton from this armor type.
     */
    public double getArmorPerTon() {
        return armorPerTon;
    }

    /**
     * Calculates the mass of the given amount of armor points.
     * 
     * @param aArmor
     *            The amount of armor.
     * @return The mass of the given armor amount.
     */
    public double getArmorMass(int aArmor) {
        return aArmor / armorPerTon;
    }

    @Override
    public UpgradeType getType() {
        return UpgradeType.ARMOR;
    }
}
