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
package org.lisoft.lsml.view_fx.loadout;

import org.lisoft.lsml.messages.EfficienciesMessage;
import org.lisoft.lsml.messages.LoadoutMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageReception;
import org.lisoft.lsml.messages.OmniPodMessage;
import org.lisoft.lsml.model.item.AmmoWeapon;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.LoadoutBase;

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
public class WeaponSummary implements MessageReceiver {
    private class BattleTimeBinding extends DoubleBinding {
        private Weapon weapon;

        BattleTimeBinding(Weapon aWeapon) {
            weapon = aWeapon;
            bind(rounds);
            bind(volleySize);
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
                if (weapon == null || battleTime.getWeapon().getCoolDown(null) < aWeapon.getCoolDown(null)) {
                    weapon = aWeapon;
                    invalidate();
                }
            }
        }

        @Override
        protected double computeValue() {
            if (weapon != null)
                return weapon.getCoolDown(loadout.getModifiers()) * rounds.get() / volleySize.get();
            return 0;
        }

        public Weapon getWeapon() {
            return weapon;
        }
    }

    private final LoadoutBase<?>    loadout;
    private final IntegerProperty   volleySize = new SimpleIntegerProperty(0);
    private final StringProperty    name       = new SimpleStringProperty();
    private final DoubleProperty    rounds     = new SimpleDoubleProperty();
    private final BattleTimeBinding battleTime;
    private final DoubleBinding     totalDamage;
    private String                  selectorName;

    @Override
    public void receive(Message aMsg) {
        // Any of the below messages affects battle time
        // LoadoutMessage: Modules changed
        // EfficienciesMessage: Efficiencies can affect summary
        // OmniPodMessage: Quirks changed
        if (aMsg instanceof LoadoutMessage || aMsg instanceof EfficienciesMessage || aMsg instanceof OmniPodMessage) {
            battleTime.invalidate();
        }
    }

    /**
     * Creates a new weapon summary based on an Item.
     * 
     * @param aItem
     *            The {@link Item} to base this {@link WeaponSummary} on initially.
     * @param aReception
     *            The {@link MessageReception} to listen to changes that affect this on.
     * @param aLoadout
     *            A {@link LoadoutBase} that this {@link WeaponSummary} is calculated for.
     */
    public WeaponSummary(MessageReception aReception, LoadoutBase<?> aLoadout, Item aItem) {
        aReception.attach(this);
        loadout = aLoadout;

        if (aItem instanceof Weapon) {
            if (aItem instanceof AmmoWeapon) {
                selectorName = ((AmmoWeapon) aItem).getAmmoType();
                rounds.set(0);
            }
            else {
                selectorName = aItem.getName();
                rounds.setValue(Double.POSITIVE_INFINITY);
            }
            battleTime = new BattleTimeBinding((Weapon) aItem);
        }
        else if (aItem instanceof Ammunition) {
            Ammunition ammunition = (Ammunition) aItem;
            selectorName = ammunition.getAmmoType();
            rounds.set(ammunition.getNumRounds());
            battleTime = new BattleTimeBinding(null);
        }
        else {
            throw new IllegalArgumentException("Item must be ammo or weapon!");
        }

        totalDamage = new DoubleBinding() {
            {
                bind(rounds);
                bind(battleTime); // Not on the time itself but the weapon.
            }

            @Override
            protected double computeValue() {
                Weapon weapon = battleTime.weapon;
                if (null != weapon) {
                    return rounds.get() * weapon.getDamagePerShot() / weapon.getAmmoPerPerShot();
                }
                return 0.0;
            }
        };

        updateMultiplicityAndName(aItem);
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
            Ammunition ammunition = (Ammunition) aItem;
            if (selectorName.equals(ammunition.getAmmoType())) {
                rounds.set(rounds.get() + ammunition.getNumRounds());
                return true;
            }
        }
        else {
            if (selectorName.equals(getSelectorFor(aItem))) {
                updateMultiplicityAndName(aItem);
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
        return volleySizeProperty().get() <= 0 && (rounds.get() < 1 || Double.isInfinite(rounds.get()));
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
            Ammunition ammunition = (Ammunition) aItem;
            if (selectorName.equals(ammunition.getAmmoType())) {
                rounds.set(rounds.get() - ammunition.getNumRounds());
                return true;
            }
        }
        else {
            if (selectorName.equals(getSelectorFor(aItem))) {
                volleySizeProperty().set(volleySizeProperty().get() - 1);
                battleTime.offer((Weapon) aItem);
                return true;
            }
        }
        return false;
    }

    /**
     * @return A {@link DoubleBinding} that calculates how long the weapon(s) represented by this {@link WeaponSummary}
     *         can be combat effective.
     */
    public DoubleBinding battleTimeProperty() {
        return battleTime;
    }

    /**
     * @return A {@link DoubleBinding} that calculates how much total damage this {@link WeaponSummary} can do.
     */
    public DoubleBinding totalDamageProperty() {
        return totalDamage;
    }

    /**
     * @return A {@link DoubleProperty} that represents how many rounds of ammo are available to this
     *         {@link WeaponSummary}. The property will have the value {@link Double#POSITIVE_INFINITY} for weapons that
     *         don't use ammo.
     */
    public DoubleProperty roundsProperty() {
        return rounds;
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

    private String getSelectorFor(Item aItem) {
        return aItem instanceof AmmoWeapon ? ((AmmoWeapon) aItem).getAmmoType() : aItem.getName();
    }

    private void updateMultiplicityAndName(Item aItem) {
        if (aItem instanceof Weapon) {
            if (aItem.getName().matches(".*[LS]RM \\d+.*")) { // Implies missile weapon -> ammo weapon
                AmmoWeapon aAmmoWeapon = (AmmoWeapon) aItem;
                volleySize.set(volleySize.get() + aAmmoWeapon.getAmmoPerPerShot());
                name.set(aAmmoWeapon.getShortName().replaceFirst("\\d+", Integer.toString(volleySize.get())));
            }
            else {
                volleySize.set(volleySize.get() + 1);
                if (volleySize.get() > 1) {
                    name.set(volleySize.get() + "x " + aItem.getShortName());
                }
                else {
                    name.set(aItem.getShortName());
                }
            }
        }
        else {
            name.set(aItem.getShortName());
        }
    }

}
