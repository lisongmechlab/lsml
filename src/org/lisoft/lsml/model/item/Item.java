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

import java.util.List;

import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.lisoft.lsml.parsing.mwo_gamedata.Localization;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class Item implements Comparable<Item> {
    @XStreamAsAttribute
    private final String             locName;
    @XStreamAsAttribute
    private final String             locDesc;
    @XStreamAsAttribute
    private final String             mwoName;
    @XStreamAsAttribute
    private final int                mwoIdx;

    @XStreamAsAttribute
    private final int                slots;
    @XStreamAsAttribute
    private final double             tons;
    @XStreamAsAttribute
    private final HardPointType      hardpointType;
    @XStreamAsAttribute
    private final int                health;
    @XStreamAsAttribute
    private final Faction            faction;
    private final List<Location>     allowedLocations;
    private final List<ChassisClass> allowedChassisClasses;

    public Item(String aUiName, String aUiDesc, String aMwoName, int aMwoId, int aSlots, double aTons,
            HardPointType aHardpointType, int aHP, Faction aFaction, List<Location> aAllowedLocations,
            List<ChassisClass> aAllowedClasses) {
        locName = aUiName;
        locDesc = aUiDesc;
        mwoName = aMwoName;
        mwoIdx = aMwoId;
        slots = aSlots;
        tons = aTons;
        hardpointType = aHardpointType;
        health = aHP;
        faction = aFaction;
        allowedLocations = aAllowedLocations;
        allowedChassisClasses = aAllowedClasses;
    }

    // TODO: Add a maximum allowed attribute here

    public Item(String aNameTag, String aDesc, int aSlots, int aHealth, Faction aFaction) {
        this(Localization.key2string(aNameTag), Localization.key2string(aDesc), aNameTag, -1, aSlots, 0.0,
                HardPointType.NONE, aHealth, aFaction, null, null);
    }

    public String getKey() {
        return mwoName;
    }

    public boolean isCrittable() {
        return health > 0;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        return locName;
    }

    public int getNumCriticalSlots() {
        return slots;
    }

    public HardPointType getHardpointType() {
        return hardpointType;
    }

    public double getMass() {
        return tons;
    }

    public int getMwoId() {
        return mwoIdx;
    }

    public String getShortName() {
        return getName();
    }

    public String getDescription() {
        return locDesc;
    }

    /**
     * This method checks if this {@link Item} can be equipped in combination with the given {@link Upgrades}.
     * 
     * @param aUpgrades
     *            The {@link Upgrades} to check against.
     * @return <code>true</code> if this {@link Item} is compatible with the given upgrades.
     */
    @SuppressWarnings("unused")
    // Interface
    public boolean isCompatible(Upgrades aUpgrades) {
        return true;
    }

    /**
     * Defines the default sorting order of arbitrary items.
     * <p>
     * The sorting order is as follows:
     * <ol>
     * <li>Energy weapons</li>
     * <li>Ballistic weapons + ammo</li>
     * <li>Missile weapons + ammo</li>
     * <li>AMS + ammo</li>
     * <li>ECM</li>
     * <li>Other items except engines</li>
     * <li>Engines</li>
     * </ol>.
     */
    @Override
    public int compareTo(Item rhs) {
        // Engines last
        if (this instanceof Engine && !(rhs instanceof Engine))
            return 1;
        else if (!(this instanceof Engine) && rhs instanceof Engine)
            return -1;

        // Count ammunition types together with their parent weapon type.
        HardPointType lhsHp = this instanceof Ammunition ? ((Ammunition) this).getWeaponHardpointType() : this
                .getHardpointType();
        HardPointType rhsHp = rhs instanceof Ammunition ? ((Ammunition) rhs).getWeaponHardpointType() : rhs
                .getHardpointType();

        // Sort by hard point type (order they appear in the enumeration declaration)
        // This gives the main order of items as given in the java doc.
        int hp = lhsHp.compareTo(rhsHp);

        // Resolve ties
        if (hp == 0) {

            // Ammunition after weapons in same hard point.
            if (this instanceof Ammunition && !(rhs instanceof Ammunition))
                return 1;
            else if (!(this instanceof Ammunition) && rhs instanceof Ammunition)
                return -1;

            // Let weapon groups sort internally
            if (this instanceof EnergyWeapon && rhs instanceof EnergyWeapon) {
                return EnergyWeapon.DEFAULT_ORDERING.compare((EnergyWeapon) this, (EnergyWeapon) rhs);
            }
            else if (lhsHp == HardPointType.BALLISTIC) {
                return Weapon.DEFAULT_WEAPON_ORDERING.compare(this, rhs);
            }
            else if (lhsHp == HardPointType.MISSILE) {
                return Weapon.DEFAULT_WEAPON_ORDERING.compare(this, rhs);
            }

            // Sort by class name, this groups single/double heat sinks together
            int classCompare = this.getClass().getName().compareTo(rhs.getClass().getName());

            // Resolve ties
            if (classCompare == 0) {
                // Last resort: Lexicographical ordering
                return toString().compareTo(rhs.toString());
            }
            return classCompare;
        }
        return hp;
    }

    public int getHealth() {
        return health;
    }

    /**
     * @return The faction requirement of this {@link Item}.
     */
    public Faction getFaction() {
        return faction;
    }

    /**
     * @return A {@link List} of locations on which this item is allowed.
     */
    public List<Location> getAllowedComponents() {
        return allowedLocations;
    }

    /**
     * @return A {@link List} of allowed chassis classes.
     */
    public List<ChassisClass> getAllowedChassisClasses() {
        return allowedChassisClasses;
    }
}
