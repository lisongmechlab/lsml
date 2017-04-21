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
package org.lisoft.lsml.view_fx.util;

import org.lisoft.lsml.model.item.AmmoWeapon;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.Loadout;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * This class holds the summary of a weapon type for the weapon table.
 *
 * @author Emily Björk
 */
public class WeaponSummary {
    private class BattleTimeBinding extends DoubleBinding {
        private Weapon weapon;

        BattleTimeBinding(Weapon aWeapon) {
            weapon = aWeapon;
            bind(ammoRounds);
            bind(volleySize);
        }

        public Weapon getWeapon() {
            return weapon;
        }

        @Override
        protected double computeValue() {
            if (weapon != null) {
                return weapon.getSecondsPerShot(loadout.getModifiers()) * ammoRounds.get() / volleySize.get();
            }
            return 0;
        }

        /**
         * Updates the weapon used for calculating the battle time.
         *
         * Note that the battle time itself is automatically updated when the multiplicity or rounds changes.
         *
         * @param aWeapon
         *            The new weapon to potentially use for calculating battle time.
         */
        void offer(Weapon aWeapon) {
            if (volleySizeProperty().get() <= 0) {
                weapon = null;
                invalidate();
            }
            else {
                if (weapon == null
                        || battleTime.getWeapon().getSecondsPerShot(null) < aWeapon.getSecondsPerShot(null)) {
                    weapon = aWeapon;
                    invalidate();
                }
            }
        }
    }

    private final Loadout loadout;
    /**
     * How many of the given weapon are equipped, for missile weapons this is tube count.
     */
    private final IntegerProperty multiplicity = new SimpleIntegerProperty(0);

    /**
     * How much ammo is consumed in one "firing" of the weapon.
     */
    private final IntegerProperty volleySize = new SimpleIntegerProperty(0);

    /**
     * The display text to show for this weapon summary.
     */
    private final StringProperty name = new SimpleStringProperty();

    /**
     * The total amount of ammo available for this weapon type.
     */
    private final DoubleProperty ammoRounds = new SimpleDoubleProperty();

    /**
     * The amount of time that this weapon summary can be used in combat.
     */
    private final BattleTimeBinding battleTime;

    /**
     * The total amount of damage that can be done using the given ammo.
     */
    private final DoubleBinding totalDamage;

    private String selectorName;

    /**
     * Creates a new weapon summary based on an Item.
     *
     * @param aItem
     *            The {@link Item} to base this {@link WeaponSummary} on initially.
     * @param aLoadout
     *            A {@link Loadout} that this {@link WeaponSummary} is calculated for.
     */
    public WeaponSummary(Loadout aLoadout, Item aItem) {
        loadout = aLoadout;

        if (aItem instanceof Weapon) {
            final Weapon weapon = (Weapon) aItem;
            if (aItem instanceof AmmoWeapon) {
                selectorName = ((AmmoWeapon) weapon).getAmmoType();
                ammoRounds.set(0);
            }
            else {
                selectorName = aItem.getName();
                ammoRounds.setValue(Double.POSITIVE_INFINITY);
            }
            battleTime = new BattleTimeBinding(weapon);
        }
        else if (aItem instanceof Ammunition) {
            final Ammunition ammunition = (Ammunition) aItem;
            selectorName = ammunition.getAmmoType();
            ammoRounds.set(ammunition.getNumRounds());
            battleTime = new BattleTimeBinding(null);
        }
        else {
            throw new IllegalArgumentException("Item must be ammo or weapon!");
        }

        totalDamage = new DoubleBinding() {
            {
                bind(ammoRounds);
                bind(battleTime); // Not on the time itself but the weapon.
            }

            @Override
            protected double computeValue() {
                final Weapon weapon = battleTime.weapon;
                if (null != weapon) {
                    return ammoRounds.get() * weapon.getDamagePerShot() / weapon.getAmmoPerPerShot();
                }
                return 0.0;
            }
        };

        addItem(aItem);
    }

    /**
     * @return A {@link DoubleBinding} that calculates how long the weapon(s) represented by this {@link WeaponSummary}
     *         can be combat effective.
     */
    public DoubleBinding battleTimeProperty() {
        return battleTime;
    }

    /**
     * Tries to meld the given item into this weapon summary, updating this in the process.
     *
     * @param aItem
     *            The {@link Item} to attempt to meld.
     * @return <code>true</code> if the item was melded into this {@link WeaponSummary}. Returns <code>false</code>
     *         otherwise.
     */
    public boolean consume(Item aItem) {
        if (aItem instanceof Ammunition) {
            final Ammunition ammunition = (Ammunition) aItem;
            if (selectorName.equals(ammunition.getAmmoType())) {
                ammoRounds.set(ammoRounds.get() + ammunition.getNumRounds());
                return true;
            }
        }
        else {
            if (selectorName.equals(getSelectorFor(aItem))) {
                addItem(aItem);
                battleTime.offer((Weapon) aItem);
                return true;
            }
        }
        return false;
    }

    /**
     * @return <code>true</code> if this {@link WeaponSummary} doesn't represent any active weapon or ammo and should be
     *         removed.
     */
    public boolean empty() {
        return volleySizeProperty().get() <= 0 && (ammoRounds.get() < 1 || Double.isInfinite(ammoRounds.get()));
    }

    /**
     * @return A {@link StringProperty} that represents the display name of this {@link WeaponSummary}. Based on the
     *         volley size and weapon/ammo type.
     */
    public StringProperty nameProperty() {
        return name;
    }

    /**
     * Attempts to split (or demeld) the given item from this {@link WeaponSummary} and updates this in the process.
     *
     * @param aItem
     *            The item to attempt to split away.
     * @return <code>true</code> if the item was successfully split from this, <code>false</code> otherwise.
     */
    public boolean remove(Item aItem) {
        if (aItem instanceof Ammunition) {
            final Ammunition ammunition = (Ammunition) aItem;
            if (selectorName.equals(ammunition.getAmmoType())) {
                ammoRounds.set(ammoRounds.get() - ammunition.getNumRounds());
                return true;
            }
        }
        else {
            final Weapon weapon = (Weapon) aItem;
            if (selectorName.equals(getSelectorFor(weapon))) {
                volleySize.set(volleySizeProperty().get() - weapon.getAmmoPerPerShot());
                multiplicity.set(multiplicity.get() - getMultiplicityFor(weapon));
                battleTime.offer(weapon);
                updateName(weapon);
                return true;
            }
        }
        return false;
    }

    /**
     * @return A {@link DoubleProperty} that represents how many rounds of ammo are available to this
     *         {@link WeaponSummary}. The property will have the value {@link Double#POSITIVE_INFINITY} for weapons that
     *         don't use ammo.
     */
    public DoubleProperty roundsProperty() {
        return ammoRounds;
    }

    /**
     * @return A {@link DoubleBinding} that calculates how much total damage this {@link WeaponSummary} can do.
     */
    public DoubleBinding totalDamageProperty() {
        return totalDamage;
    }

    /**
     * @return A {@link IntegerProperty} that represents how many units of ammo, or rounds, are consumed on one alpha
     *         strike of this {@link WeaponSummary}. If the weapon doesn't use ammo, the volley size is the number of
     *         weapons that fire simultaneously. If this {@link WeaponSummary} doesn't have a weapon associated, i.e.
     *         only ammo without a matching weapon, then the volley size property will be <code>zero</code>.
     */
    public IntegerProperty volleySizeProperty() {
        return volleySize;
    }

    private void addItem(Item aItem) {
        if (aItem instanceof Weapon) {
            final Weapon weapon = (Weapon) aItem;
            volleySize.set(volleySize.get() + weapon.getAmmoPerPerShot());
            multiplicity.set(multiplicity.get() + getMultiplicityFor(weapon));
            updateName(aItem);
        }
        else {
            name.set(aItem.getShortName());
        }
    }

    private int getMultiplicityFor(Weapon aWeapon) {
        if (shouldCountTubes(aWeapon)) {
            return aWeapon.getAmmoPerPerShot();
        }
        return 1;
    }

    private String getSelectorFor(Item aItem) {
        return aItem instanceof AmmoWeapon ? ((AmmoWeapon) aItem).getAmmoType() : aItem.getName();
    }

    private boolean shouldCountTubes(Item aItem) {
        return aItem.getName().matches(".*[LS]RM \\d+.*");
    }

    private void updateName(Item aItem) {
        if (shouldCountTubes(aItem)) { // Implies missile weapon -> ammo weapon
            final AmmoWeapon aAmmoWeapon = (AmmoWeapon) aItem;
            name.set(aAmmoWeapon.getShortName().replaceFirst("\\d+", Integer.toString(multiplicity.get())));
        }
        else {
            if (multiplicity.get() > 1) {
                name.set(multiplicity.get() + "x " + aItem.getShortName());
            }
            else {
                name.set(aItem.getShortName());
            }
        }
    }

}
