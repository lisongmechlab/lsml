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
package lisong_mechlab.model.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lisong_mechlab.model.item.BallisticWeapon;
import lisong_mechlab.model.item.EnergyWeapon;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.metrics.helpers.DoubleFireBurstSignal;
import lisong_mechlab.model.metrics.helpers.IntegratedImpulseTrain;
import lisong_mechlab.model.metrics.helpers.IntegratedPulseTrain;
import lisong_mechlab.model.metrics.helpers.IntegratedSignal;
import lisong_mechlab.model.modifiers.Modifier;
import lisong_mechlab.util.message.Message;
import lisong_mechlab.util.message.MessageXBar;

/**
 * This metric calculates how much damage a loadout can dish out in a given time interval ignoring heat.
 * 
 * @author Li Song
 */
public class BurstDamageOverTime extends RangeTimeMetric implements Message.Recipient {
    private final List<IntegratedSignal> damageIntegrals = new ArrayList<>();
    private double                       cachedRange     = -1;

    /**
     * Creates a new calculator object
     * 
     * @param aLoadout
     *            The {@link LoadoutStandard} to calculate for.
     * @param aXBar
     *            The {@link MessageXBar} to listen for changes to 'aLoadout' on.
     */
    public BurstDamageOverTime(LoadoutBase<?> aLoadout, MessageXBar aXBar) {
        super(aLoadout);
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
        for (Weapon weapon : loadout.items(Weapon.class)) {
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
