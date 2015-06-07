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
package org.lisoft.lsml.model.loadout;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

import org.lisoft.lsml.model.item.Weapon;

/**
 * This class abstracts weapon groups on mech loadouts.
 * 
 * @author Li Song
 */
public class WeaponGroups {
    /**
     * This enum represents different firing patterns for a weapon group. The firing pattern will affect sustained DPS
     * and heat values. For maximal DPS, alpha strike is assumed.
     * 
     * @author Li Song
     *
     */
    public static enum FiringMode {
        /**
         * Assumes that all the weapons in the group are fired in an optimal pattern. Useful for calculating total
         * sustained DPS for example.
         */
        Optimal,
        /**
         * Assumes that all weapons are fired as often as possible.
         */
        AlphaStrike,
        /**
         * Assumes that all weapons are fired 0.5s after each other. Weapons on cool-down when their turn arrives are
         * skipped past and the next available weapon fires.
         */
        ChainFire
    }

    public final static int                MAX_GROUPS  = 6;
    public final static int                MAX_WEAPONS = 16;

    private final BitSet                   bs          = new BitSet(MAX_GROUPS * MAX_WEAPONS);
    private final FiringMode[]             firingMode  = new FiringMode[MAX_GROUPS];
    private final transient LoadoutBase<?> loadout;

    /**
     * Creates a new {@link WeaponGroups}.
     * 
     * @param aLoadout
     *            The {@link LoadoutBase} that this {@link WeaponGroups} is for.
     */
    public WeaponGroups(LoadoutBase<?> aLoadout) {
        loadout = aLoadout;
        for (int i = 0; i < MAX_GROUPS; ++i) {
            setFiringMode(i, FiringMode.Optimal);
        }
    }

    /**
     * Creates a new {@link WeaponGroups} with the same settings as aThat but for another loadout.
     * 
     * @param aThat
     *            The {@link WeaponGroups} to copy.
     * @param aNewLoadout
     *            The {@link LoadoutBase} to use for this new {@link WeaponGroups}.
     */
    public WeaponGroups(WeaponGroups aThat, LoadoutBase<?> aNewLoadout) {
        loadout = aNewLoadout;
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
            setFiringMode(i, aThat.getFiringMode(i));

            for (int j = 0; j < MAX_WEAPONS; ++j) {
                setGroup(i, j, aThat.isInGroup(i, j));
            }
        }
    }

    /**
     * Gets the {@link FiringMode} for a given group.
     * 
     * @param aGroup
     *            The group to check the firing mode for.
     * @return The {@link FiringMode} mode for the given group.
     */
    public FiringMode getFiringMode(int aGroup) {
        return firingMode[aGroup];
    }

    /**
     * Gets the order that weapons are appearing for the groups.
     * 
     * @return A {@link List} of {@link Weapon}s in an implementation defined, deterministic order.
     */
    public List<Weapon> getWeaponOrder() {
        List<Weapon> weapons = new ArrayList<>();
        for (Weapon w : loadout.items(Weapon.class)) {
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
     * @return A {@link Collection} of {@link Weapon}s.
     */
    public Collection<Weapon> getWeapons(int aGroup) {
        List<Weapon> ans = new ArrayList<>();
        List<Weapon> weapons = getWeaponOrder();
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
     * Sets the firing mode for a group.
     * 
     * @param aGroup
     *            The group to affect.
     * @param aFiringMode
     *            The new {@link FiringMode}.
     */
    public void setFiringMode(int aGroup, FiringMode aFiringMode) {
        firingMode[aGroup] = aFiringMode;
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
