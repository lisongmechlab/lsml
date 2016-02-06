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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

import org.lisoft.lsml.model.item.Weapon;

/**
 * This class abstracts weapon groups on mech loadouts.
 * 
 * @author Emily Björk
 */
public class WeaponGroups {
    public final static int MAX_GROUPS  = 6;
    public final static int MAX_WEAPONS = 16;

    private final BitSet    bs          = new BitSet(MAX_GROUPS * MAX_WEAPONS);

    /**
     * Creates a new {@link WeaponGroups}.
     */
    public WeaponGroups() {
    }

    /**
     * Creates a new {@link WeaponGroups} with the same settings as aThat but for another loadout.
     * 
     * @param aThat
     *            The {@link WeaponGroups} to copy.
     */
    public WeaponGroups(WeaponGroups aThat) {
        assign(aThat);
    }

    /**
     * Copy assigns a weapon group to this weapon group. Will not assign the loadout.
     * 
     * @param aThat
     *            The weapon group to assign.
     */
    public void assign(WeaponGroups aThat) {
        for (int i = 0; i < MAX_GROUPS; ++i) {
            for (int j = 0; j < MAX_WEAPONS; ++j) {
                setGroup(i, j, aThat.isInGroup(i, j));
            }
        }
    }

    /**
     * Gets the order that weapons are appearing for the groups.
     * 
     * @param aLoadout
     *            The loadout to get the weapon order for.
     * 
     * @return A {@link List} of {@link Weapon}s in an implementation defined, deterministic order.
     */
    public List<Weapon> getWeaponOrder(Loadout aLoadout) {
        List<Weapon> weapons = new ArrayList<>();
        for (Weapon w : aLoadout.items(Weapon.class)) {
            if (w.isOffensive()) {
                weapons.add(w);
            }
        }
        return weapons;
    }

    /**
     * Gets a collection of all the weapons in the specified group.
     * 
     * @param aGroup
     *            The group to get weapons for.
     * @param aLoadout
     *            The loadout to get the weapons for.
     * @return A {@link Collection} of {@link Weapon}s.
     */
    public Collection<Weapon> getWeapons(int aGroup, Loadout aLoadout) {
        List<Weapon> ans = new ArrayList<>();
        List<Weapon> weapons = getWeaponOrder(aLoadout);
        for (int i = 0; i < weapons.size(); ++i) {
            if (isInGroup(aGroup, i)) {
                ans.add(weapons.get(i));
            }
        }
        return ans;
    }

    /**
     * Checks if a weapon is in a given group.
     * 
     * @param aGroup
     *            The group to check.
     * @param aWeapon
     *            The weapon to check.
     * @return <code>true</code> if the given weapon is in the given group.
     */
    public boolean isInGroup(int aGroup, int aWeapon) {
        return bs.get(index(aGroup, aWeapon));
    }

    /**
     * Sets whether a weapon is included in a group or not.
     * 
     * @param aGroup
     *            The group to affect.
     * @param aWeapon
     *            The weapon to affect.
     * @param aInGroup
     *            If the weapon should be in the group or not.
     */
    public void setGroup(int aGroup, int aWeapon, boolean aInGroup) {
        bs.set(index(aGroup, aWeapon), aInGroup);
    }

    /**
     * @param aGroup
     *            The group to get the index for.
     * @param aWeapon
     *            The weapon to get the index for.
     * @return The index into the {@link BitSet} of the given group and weapon.
     */
    private int index(int aGroup, int aWeapon) {
        return aGroup + aWeapon * MAX_GROUPS;
    }
}
