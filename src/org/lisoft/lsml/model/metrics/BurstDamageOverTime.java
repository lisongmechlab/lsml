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
package org.lisoft.lsml.model.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.lisoft.lsml.model.item.BallisticWeapon;
import org.lisoft.lsml.model.item.EnergyWeapon;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.metrics.helpers.DoubleFireBurstSignal;
import org.lisoft.lsml.model.metrics.helpers.IntegratedImpulseTrain;
import org.lisoft.lsml.model.metrics.helpers.IntegratedPulseTrain;
import org.lisoft.lsml.model.metrics.helpers.IntegratedSignal;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.util.message.Message;
import org.lisoft.lsml.util.message.MessageXBar;

/**
 * This metric calculates how much damage a loadout can dish out in a given time interval ignoring heat.
 * 
 * @author Emily Björk
 */
public class BurstDamageOverTime extends RangeTimeMetric implements Message.Recipient {
    private final List<IntegratedSignal> damageIntegrals = new ArrayList<>();
    private double                       cachedRange     = -1;
    private final int                    weaponGroup;

    /**
     * Creates a new {@link BurstDamageOverTime} metric that calculates the maximal burst damage using all weapons on
     * the loadout.
     * 
     * @param aLoadout
     *            The loadout to calculate for.
     * @param aXBar
     *            The {@link MessageXBar} to listen for changes to 'aLoadout' on.
     */
    public BurstDamageOverTime(LoadoutBase<?> aLoadout, MessageXBar aXBar) {
        this(aLoadout, aXBar, -1);
    }

    /**
     * Creates a new {@link BurstDamageOverTime} that only calculates the damage for the given weapon group.
     * 
     * @param aLoadout
     *            The loadout to calculate for.
     * @param aXBar
     *            The cross-bar to listen to changes on the loadout on.
     * @param aGroup
     *            The group to calculate for.
     */
    public BurstDamageOverTime(LoadoutBase<?> aLoadout, MessageXBar aXBar, int aGroup) {
        super(aLoadout);
        weaponGroup = aGroup;
        updateEvents(getRange());
        aXBar.attach(this);
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg.isForMe(loadout) && aMsg.affectsHeatOrDamage()) {
            updateEvents(getRange());
        }
    }

    private void updateEvents(double aRange) {
        damageIntegrals.clear();
        Collection<Modifier> modifiers = loadout.getModifiers();

        final Iterable<Weapon> weapons;
        if (weaponGroup < 0) {
            weapons = loadout.items(Weapon.class);
        }
        else {
            weapons = loadout.getWeaponGroups().getWeapons(weaponGroup, loadout);
        }

        for (Weapon weapon : weapons) {
            if (!weapon.isOffensive())
                continue;

            double factor = (aRange < 0) ? 1.0 : weapon.getRangeEffectivity(aRange, modifiers);
            double period = weapon.getSecondsPerShot(modifiers);
            double damage = factor * weapon.getDamagePerShot();

            if (weapon instanceof EnergyWeapon) {
                EnergyWeapon energyWeapon = (EnergyWeapon) weapon;
                if (energyWeapon.getDuration(modifiers) > 0) {
                    damageIntegrals.add(new IntegratedPulseTrain(period, energyWeapon.getDuration(modifiers), damage
                            / energyWeapon.getDuration(modifiers)));
                    continue;
                }
            }
            else if (weapon instanceof BallisticWeapon) {
                BallisticWeapon ballisticWeapon = (BallisticWeapon) weapon;
                if (ballisticWeapon.canDoubleFire()) {
                    damageIntegrals.add(new DoubleFireBurstSignal(ballisticWeapon, modifiers, aRange));
                    continue;
                }
            }
            damageIntegrals.add(new IntegratedImpulseTrain(period, damage));

        }
        cachedRange = getRange();
    }

    @Override
    public double calculate(double aRange, double aTime) {
        if (aRange != cachedRange) {
            updateEvents(aRange);
        }
        double ans = 0;
        for (IntegratedSignal event : damageIntegrals) {
            ans += event.integrateFromZeroTo(aTime);
        }
        return ans;
    }
}
