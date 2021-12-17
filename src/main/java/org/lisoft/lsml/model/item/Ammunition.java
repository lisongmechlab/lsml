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

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;

import java.util.Collection;

/**
 * A generic ammunition item.
 *
 * @author Li Song
 */
public class Ammunition extends Item {
    protected final Attribute rounds;
    @XStreamAsAttribute
    protected final double internalDamage;

    /**
     * This is set through reflection in parsing post process step.
     */
    @XStreamAsAttribute
    protected final HardPointType type = HardPointType.NONE;

    @XStreamAsAttribute
    protected final String ammoType;

    public Ammunition(String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons,
            HardPointType aHardPointType, double aHP, Faction aFaction, int aRounds, String aAmmoType,
            double aInternalDamage) {
        super(aName, aDesc, aMwoName, aMwoId, aSlots, aTons, aHardPointType, aHP, aFaction, null, null);

        rounds = new Attribute(aRounds, ModifierDescription.SEL_AMMOCAPACITY, specifierMap(aMwoName));
        ammoType = aAmmoType;
        internalDamage = aInternalDamage;
    }

    public String getQuirkSpecifier(){
        return rounds.getSpecifier();
    }

    static private String specifierMap(String aMwoName){
        String ans = aMwoName.toLowerCase();
        ans = ans.replaceAll("ammohalf", "");
        ans = ans.replaceAll("ammo", "");
        ans = ans.replaceAll("clan", "c");
        ans = ans.replaceAll("-xac","x");
        return ans;
    }

    /**
     * @return The type name of this {@link Ammunition}. Used to match with {@link Weapon} ammo type.
     */
    public String getAmmoId() {
        return ammoType;
    }

    public int getNumRounds(Collection<Modifier> aModifiers) {
        return (int)rounds.value(aModifiers);
    }

    /**
     * @return The {@link HardPointType} that the weapon that uses this ammo is using. Useful for color coding and
     *         searching.
     */
    public HardPointType getWeaponHardPointType() {
        return type;
    }
}
