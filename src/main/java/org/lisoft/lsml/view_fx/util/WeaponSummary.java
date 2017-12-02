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
package org.lisoft.lsml.view_fx.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.lisoft.lsml.model.item.AmmoWeapon;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.MissileWeapon;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.modifiers.Modifier;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * This class holds the summary of a weapon type for the weapon table.
 *
 * @author Li Song
 */
public class WeaponSummary {
    private final ObservableList<Weapon> weapons = FXCollections.observableArrayList();
    private final ObservableList<Ammunition> ammo = FXCollections.observableArrayList();

    private final Supplier<Collection<Modifier>> supplier;

    /**
     * How much ammo is consumed in one "firing" of the weapon.
     */
    private final IntegerBinding volleySize;

    /**
     * The display text to show for this weapon summary.
     */
    private final StringBinding name;

    /**
     * The total amount of ammo available for this weapon type.
     */
    private final DoubleBinding ammoRounds;

    /**
     * The amount of time that this weapon summary can be used in combat.
     */
    private final DoubleBinding battleTime;

    /**
     * The total amount of damage that can be done using the given ammo.
     */
    private final DoubleBinding totalDamage;

    private final String selectorName;

    /**
     * Creates a new weapon summary based on an Item.
     *
     * @param aItem
     *            The {@link Item} to base this {@link WeaponSummary} on initially.
     * @param aModifierSupplier
     *            A {@link Supplier} that provides modifiers to use for computing the values of the weapon.
     */
    public WeaponSummary(Supplier<Collection<Modifier>> aModifierSupplier, Item aItem) {
        supplier = aModifierSupplier;
        selectorName = getSelectorFor(aItem);
        consume(aItem);

        ammoRounds = new DoubleBinding() {
            {
                bind(weapons);
                bind(ammo);
            }

            @Override
            protected double computeValue() {
                if (!weapons.isEmpty()) {
                    if (!(weapons.iterator().next() instanceof AmmoWeapon)) {
                        return Double.POSITIVE_INFINITY;
                    }
                }
                return ammo.stream().collect(Collectors.summingInt(Ammunition::getNumRounds)) + weapons.stream()
                        .map(w -> (AmmoWeapon) w).collect(Collectors.summingInt(AmmoWeapon::getBuiltInRounds));
            }
        };

        volleySize = new IntegerBinding() {
            {
                bind(weapons);
            }

            @Override
            protected int computeValue() {
                if (!weapons.isEmpty()) {
                    if (weapons.iterator().next() instanceof AmmoWeapon) {
                        return weapons.stream().map(w -> (AmmoWeapon) w)
                                .collect(Collectors.summingInt(AmmoWeapon::getAmmoPerPerShot));
                    }
                    return weapons.size();
                }
                return 0;
            }
        };

        battleTime = new DoubleBinding() {
            {
                bind(weapons);
                bind(ammoRounds);
                bind(volleySize);
            }

            @Override
            protected double computeValue() {
                final Collection<Modifier> modifiers = supplier.get();
                final Optional<Weapon> weapon = weapons.stream()
                        .max(Comparator.comparingDouble(w -> w.getSecondsPerShot(modifiers)));
                if (weapon.isPresent()) {
                    return weapon.get().getSecondsPerShot(supplier.get()) * ammoRounds.get() / volleySize.get();
                }
                return 0;
            }
        };

        totalDamage = new DoubleBinding() {
            {
                bind(weapons);
                bind(ammoRounds);
                bind(battleTime); // Not on the time itself but the weapon.
            }

            @Override
            protected double computeValue() {
                if (weapons.isEmpty()) {
                    return 0.0;
                }
                final Weapon anyWeapon = weapons.iterator().next();
                if (anyWeapon.getDamagePerProjectile() == 0) {
                    return 0.0;
                }
                return ammoRounds.get() * anyWeapon.getDamagePerProjectile();
            }
        };

        name = new StringBinding() {
            {
                bind(weapons);
                bind(ammo);
            }

            @Override
            protected String computeValue() {
                if (weapons.isEmpty()) {
                    if (ammo.isEmpty()) {
                        return "N/A";
                    }
                    return ammo.sorted(Comparator.comparingDouble(Ammunition::getMass).reversed()).iterator().next()
                            .getShortName();
                }

                final Weapon weapon = weapons.iterator().next();

                if (shouldCountTubes(weapon)) { // Implies missile weapon -> ammo weapon
                    final AmmoWeapon aAmmoWeapon = (AmmoWeapon) weapon;
                    return aAmmoWeapon.getShortName().replaceFirst("\\d+", Integer.toString(volleySize.get()));
                }
                final int multiplicity = weapons.size();
                if (multiplicity > 1) {
                    return multiplicity + "x " + weapon.getShortName();
                }
                return weapon.getShortName();
            }
        };
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
        if (selectorName.equals(getSelectorFor(aItem))) {
            if (aItem instanceof Ammunition) {
                return ammo.add((Ammunition) aItem);
            }
            return weapons.add((Weapon) aItem);
        }
        return false;
    }

    /**
     * @return <code>true</code> if this {@link WeaponSummary} doesn't represent any active weapon or ammo and should be
     *         removed.
     */
    public boolean empty() {
        return weapons.isEmpty() && ammo.isEmpty();
    }

    /**
     * @return A {@link StringProperty} that represents the display name of this {@link WeaponSummary}. Based on the
     *         volley size and weapon/ammo type.
     */
    public StringBinding nameProperty() {
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
        if (selectorName.equals(getSelectorFor(aItem))) {
            if (aItem instanceof Ammunition) {
                return ammo.remove(aItem);
            }
            return weapons.remove(aItem);
        }
        return false;
    }

    /**
     * @return A {@link DoubleProperty} that represents how many rounds of ammo are available to this
     *         {@link WeaponSummary}. The property will have the value {@link Double#POSITIVE_INFINITY} for weapons that
     *         don't use ammo.
     */
    public DoubleBinding roundsProperty() {
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
    public IntegerBinding volleySizeProperty() {
        return volleySize;
    }

    private String getSelectorFor(Item aItem) {
        if (aItem instanceof Ammunition) {
            return ((Ammunition) aItem).getAmmoId();
        }
        if (aItem instanceof AmmoWeapon) {
            final AmmoWeapon ammoWeapon = (AmmoWeapon) aItem;
            if (ammoWeapon.hasBuiltInAmmo()) {
                return ammoWeapon.getName();
            }
            return ammoWeapon.getAmmoId();
        }
        return aItem.getName();
    }

    private boolean shouldCountTubes(Item aItem) {
        if (aItem instanceof MissileWeapon) {
            final MissileWeapon missileWeapon = (MissileWeapon) aItem;
            return missileWeapon.getAmmoPerPerShot() > 1;
        }
        return false;
    }
}
