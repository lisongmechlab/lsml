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
package org.lisoft.lsml.model.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageReception;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.item.BallisticWeapon;
import org.lisoft.lsml.model.item.EnergyWeapon;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.metrics.helpers.DoubleFireBurstSignal;
import org.lisoft.lsml.model.metrics.helpers.IntegratedImpulseTrain;
import org.lisoft.lsml.model.metrics.helpers.IntegratedPulseTrain;
import org.lisoft.lsml.model.metrics.helpers.IntegratedSignal;
import org.lisoft.lsml.model.modifiers.Modifier;

/**
 * This metric calculates how much damage a loadout can dish out in a given time interval ignoring heat.
 *
 * @author Li Song
 */
public class BurstDamageOverTime extends RangeTimeMetric implements MessageReceiver {
    private final List<IntegratedSignal> damageIntegrals = new ArrayList<>();
    private double cachedRange = -1;
    private final int weaponGroup;

    /**
     * Creates a new {@link BurstDamageOverTime} metric that calculates the maximal burst damage using all weapons on
     * the loadout.
     *
     * @param aLoadout
     *            The loadout to calculate for.
     * @param aReception
     *            The {@link MessageXBar} to listen for changes to 'aLoadout' on.
     */
    public BurstDamageOverTime(Loadout aLoadout, MessageReception aReception) {
        this(aLoadout, aReception, -1);
    }

    /**
     * Creates a new {@link BurstDamageOverTime} that only calculates the damage for the given weapon group.
     *
     * @param aLoadout
     *            The loadout to calculate for.
     * @param aReception
     *            The cross-bar to listen to changes on the loadout on.
     * @param aGroup
     *            The group to calculate for.
     */
    public BurstDamageOverTime(Loadout aLoadout, MessageReception aReception, int aGroup) {
        super(aLoadout);
        weaponGroup = aGroup;
        updateEvents(getUserRange());
        aReception.attach(this);
    }

    @Override
    public double calculate(double aRange) {
        return calculate(aRange, time);
    }

    @Override
    public double calculate(double aRange, double aTime) {
        if (aRange != cachedRange) {
            updateEvents(aRange);
        }
        double ans = 0;
        for (final IntegratedSignal event : damageIntegrals) {
            ans += event.integrateFromZeroTo(aTime);
        }
        return ans;
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg.isForMe(loadout) && aMsg.affectsHeatOrDamage()) {
            updateEvents(getUserRange());
        }
    }

    /**
     * Updates the contents in the damageIntegrals list.
     *
     * @param aRange
     *            The range to compute for, or < 0 for optimal range for respective weapons.
     */
    private void updateEvents(double aRange) {
        damageIntegrals.clear();
        final Collection<Modifier> modifiers = loadout.getAllModifiers();

        final Iterable<Weapon> weapons;
        if (weaponGroup < 0) {
            weapons = loadout.items(Weapon.class);
        }
        else {
            weapons = loadout.getWeaponGroups().getWeapons(weaponGroup, loadout);
        }

        for (final Weapon weapon : weapons) {
            if (!weapon.isOffensive()) {
                continue;
            }

            final double factor = aRange < 0 ? 1.0 : weapon.getRangeEffectiveness(aRange, modifiers);
            final double period = weapon.getExpectedFiringPeriod(modifiers);
            final double damage = factor * weapon.getDamagePerShot();

            if (weapon instanceof EnergyWeapon) {
                final EnergyWeapon energyWeapon = (EnergyWeapon) weapon;
                if (energyWeapon.getDuration(modifiers) > 0) {
                    damageIntegrals.add(new IntegratedPulseTrain(period, energyWeapon.getDuration(modifiers),
                            damage / energyWeapon.getDuration(modifiers)));
                    continue;
                }
            }
            else if (weapon instanceof BallisticWeapon) {
                final BallisticWeapon ballisticWeapon = (BallisticWeapon) weapon;
                if (ballisticWeapon.canDoubleFire()) {
                    final double range = aRange < 0.0 ? ballisticWeapon.getRangeOptimal(modifiers).first : aRange;
                    damageIntegrals.add(new DoubleFireBurstSignal(ballisticWeapon, modifiers, range));
                    continue;
                }
            }
            damageIntegrals.add(new IntegratedImpulseTrain(period, damage));

        }
        cachedRange = getUserRange();
    }
}
