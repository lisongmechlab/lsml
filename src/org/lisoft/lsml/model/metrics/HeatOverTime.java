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
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.item.EnergyWeapon;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.HeatSource;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.metrics.helpers.IntegratedImpulseTrain;
import org.lisoft.lsml.model.metrics.helpers.IntegratedPulseTrain;
import org.lisoft.lsml.model.metrics.helpers.IntegratedSignal;
import org.lisoft.lsml.model.modifiers.Modifier;

/**
 * This class calculates the accurate heat generation over time for a {@link LoadoutStandard} assuming all guns fire as
 * often as possible with engine at max speed and without jump jets.
 * 
 * @author Li Song
 */
public class HeatOverTime implements VariableMetric, MessageReceiver {

    private final LoadoutBase<?>         loadout;
    private final List<IntegratedSignal> heatIntegrals = new ArrayList<>();
    private final int                    weaponGroup;

    /**
     * Creates a new {@link HeatOverTime} metric for the given loadout. It will calculate the heat assuming all guns are
     * a' blazing.
     * 
     * @param aLoadout
     *            The loadout to calculate the metric for.
     * @param aXBar
     *            The cross-bar to listen for changes on.
     */
    public HeatOverTime(LoadoutBase<?> aLoadout, MessageXBar aXBar) {
        this(aLoadout, aXBar, -1);
    }

    /**
     * Creates a new {@link HeatOverTime} metric for the given weapon group in the loadout.
     * 
     * @param aLoadout
     *            The loadout to calculate the metric for.
     * @param aXBar
     *            The cross-bar to listen for changes on.
     * @param aGroup
     *            The weapon group to calculate the metric for.
     */
    public HeatOverTime(LoadoutBase<?> aLoadout, MessageXBar aXBar, int aGroup) {
        loadout = aLoadout;
        weaponGroup = aGroup;
        updateEvents();
        aXBar.attach(this);
    }

    @Override
    public double calculate(double aTime) {
        double ans = 0;
        for (IntegratedSignal event : heatIntegrals) {
            ans += event.integrateFromZeroTo(aTime);
        }
        return ans;
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg.isForMe(loadout) && aMsg.affectsHeatOrDamage()) {
            updateEvents();
        }
    }

    private void updateEvents() {
        heatIntegrals.clear();
        Collection<Modifier> modifiers = loadout.getModifiers();

        final Collection<Weapon> weaponsInGroup;
        if (weaponGroup >= 0) {
            weaponsInGroup = loadout.getWeaponGroups().getWeapons(weaponGroup, loadout);
        }
        else {
            weaponsInGroup = null;
        }

        for (HeatSource item : loadout.items(HeatSource.class)) {
            if (item instanceof Weapon) {
                Weapon weapon = (Weapon) item;

                if (weaponsInGroup != null) {
                    if (weaponsInGroup.contains(weapon)) {
                        weaponsInGroup.remove(weapon);
                    }
                    else {
                        continue;
                    }
                }

                if (weapon instanceof EnergyWeapon) {
                    EnergyWeapon energyWeapon = (EnergyWeapon) weapon;
                    if (energyWeapon.getDuration(modifiers) > 0) {
                        heatIntegrals.add(new IntegratedPulseTrain(energyWeapon.getSecondsPerShot(modifiers),
                                energyWeapon.getDuration(modifiers),
                                energyWeapon.getHeat(modifiers) / energyWeapon.getDuration(modifiers)));
                        continue;
                    }
                }
                heatIntegrals.add(
                        new IntegratedImpulseTrain(weapon.getSecondsPerShot(modifiers), weapon.getHeat(modifiers)));
            }
            if (item instanceof Engine) {
                heatIntegrals.add(new IntegratedPulseTrain(10, 10, ((Engine) item).getHeat(modifiers)));
            }
        }
    }

    @Override
    public String getMetricName() {
        return "Heat";
    }

    @Override
    public String getArgumentName() {
        return "Time [s]";
    }

    @Override
    public List<Double> getArgumentValues() {
        List<Double> ans = new ArrayList<>();
        final double maxTime = 5 * 60;
        final double step = 0.5;
        for (double t = 0.0; t <= maxTime; t += step) {
            ans.add(t);
        }
        return ans;
    }
}
