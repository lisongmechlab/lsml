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
package org.lisoft.lsml.view_fx.properties;

import java.util.Collection;
import java.util.function.Predicate;

import org.lisoft.lsml.messages.EfficienciesMessage;
import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReception;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.messages.OmniPodMessage;
import org.lisoft.lsml.model.chassi.MovementProfile;
import org.lisoft.lsml.model.environment.Environment;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutMetrics;
import org.lisoft.lsml.model.loadout.LoadoutMetrics.GroupMetrics;
import org.lisoft.lsml.model.loadout.WeaponGroups;
import org.lisoft.lsml.model.modifiers.Modifier;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * This class adapts {@link LoadoutMetrics} for JavaFX.
 * 
 * @author Emily Björk
 */
public class LoadoutMetricsModelAdaptor {
    public static class GroupMetricsModelAdaptor {
        public final DoubleBinding alphaTimeToOverHeat;
        public final DoubleBinding alphaGhostHeat;
        public final DoubleBinding alphaDamage;
        public final DoubleBinding alphaHeat;
        public final DoubleBinding burstDamage;
        public final DoubleBinding maxDPS;
        public final DoubleBinding sustainedDPS;

        public final DoubleBinding alphaRange;
        public final DoubleBinding maxDPSRange;
        public final DoubleBinding sustainedDPSRange;
        public final DoubleBinding burstRange;

        public GroupMetricsModelAdaptor(MessageReception aRcv, GroupMetrics aMetrics,
                Predicate<Message> affectsHeatOrDamage) {

            alphaTimeToOverHeat = new LsmlDoubleBinding(aRcv, () -> aMetrics.alphaTimeToOverHeat.calculate(),
                    affectsHeatOrDamage);
            alphaGhostHeat = new LsmlDoubleBinding(aRcv, () -> aMetrics.ghostHeat.calculate(), affectsHeatOrDamage);
            alphaDamage = new LsmlDoubleBinding(aRcv, () -> aMetrics.alphaStrike.calculate(), affectsHeatOrDamage);
            alphaHeat = new LsmlDoubleBinding(aRcv, () -> aMetrics.alphaHeat.calculate(), affectsHeatOrDamage);
            burstDamage = new LsmlDoubleBinding(aRcv, () -> aMetrics.burstDamage.calculate(), affectsHeatOrDamage);
            maxDPS = new LsmlDoubleBinding(aRcv, () -> aMetrics.maxDPS.calculate(), affectsHeatOrDamage);
            sustainedDPS = new LsmlDoubleBinding(aRcv, () -> aMetrics.sustainedDPS.calculate(), affectsHeatOrDamage);

            // Offense
            alphaRange = new LsmlDoubleBinding(aRcv, () -> aMetrics.alphaStrike.getRange(), affectsHeatOrDamage);
            maxDPSRange = new LsmlDoubleBinding(aRcv, () -> aMetrics.maxDPS.getRange(), affectsHeatOrDamage);
            sustainedDPSRange = new LsmlDoubleBinding(aRcv, () -> aMetrics.sustainedDPS.getRange(),
                    affectsHeatOrDamage);
            burstRange = new LsmlDoubleBinding(aRcv, () -> aMetrics.burstDamage.getRange(), affectsHeatOrDamage);
        }
    }

    public final LoadoutMetrics metrics;
    private final MessageXBar xBar;

    // Mobility
    public final DoubleBinding topSpeed;
    public final DoubleBinding turnSpeed;
    public final DoubleBinding torsoPitchSpeed;
    public final DoubleBinding torsoYawSpeed;
    public final DoubleBinding armPitchSpeed;
    public final DoubleBinding armYawSpeed;
    public final DoubleBinding torsoPitch;
    public final DoubleBinding torsoYaw;
    public final DoubleBinding armPitch;
    public final DoubleBinding armYaw;
    public final IntegerBinding jumpJetCount;
    public final IntegerBinding jumpJetMax;

    // Heat
    public final IntegerBinding heatSinkCount;
    public final DoubleBinding heatCapacity;
    public final DoubleBinding heatDissipation;
    public final DoubleBinding coolingRatio;
    public final DoubleBinding timeToCool;

    // Offensive
    public final DoubleProperty burstTime = new SimpleDoubleProperty(5.0);
    public final ObjectProperty<Double> range = new SimpleObjectProperty<>(null);
    public final ObjectProperty<Environment> environment = new SimpleObjectProperty<>();

    public final GroupMetricsModelAdaptor alphaGroup;
    public final GroupMetricsModelAdaptor[] weaponGroups = new GroupMetricsModelAdaptor[WeaponGroups.MAX_WEAPONS];

    public LoadoutMetricsModelAdaptor(LoadoutMetrics aMetrics, Loadout aLoadout, MessageXBar aRcv) {
        metrics = aMetrics;
        xBar = aRcv;

        MovementProfile mp = aLoadout.getMovementProfile();
        Collection<Modifier> modifiers = aLoadout.getModifiers();

        // Update predicates
        Predicate<Message> itemsChanged = (aMsg) -> aMsg instanceof ItemMessage;
        Predicate<Message> effsChanged = (aMsg) -> aMsg instanceof EfficienciesMessage;
        Predicate<Message> omniPodChanged = (aMsg) -> aMsg instanceof OmniPodMessage;
        Predicate<Message> affectsHeatOrDamage = (aMsg) -> aMsg.affectsHeatOrDamage();
        Predicate<Message> engineOrEffsChanged = (aMsg) -> itemsChanged.test(aMsg) || effsChanged.test(aMsg);
        Predicate<Message> itemsOrPodsChanged = (aMsg) -> itemsChanged.test(aMsg) || omniPodChanged.test(aMsg);

        // Mobility
        topSpeed = new LsmlDoubleBinding(aRcv, () -> metrics.topSpeed.calculate(), engineOrEffsChanged);
        turnSpeed = new LsmlDoubleBinding(aRcv, () -> metrics.turningSpeed.calculate(), engineOrEffsChanged);
        torsoPitchSpeed = new LsmlDoubleBinding(aRcv, () -> metrics.torsoPitchSpeed.calculate(), engineOrEffsChanged);
        torsoYawSpeed = new LsmlDoubleBinding(aRcv, () -> metrics.torsoYawSpeed.calculate(), engineOrEffsChanged);
        armPitchSpeed = new LsmlDoubleBinding(aRcv, () -> metrics.armPitchSpeed.calculate(), engineOrEffsChanged);
        armYawSpeed = new LsmlDoubleBinding(aRcv, () -> metrics.armYawSpeed.calculate(), engineOrEffsChanged);
        jumpJetCount = new LsmlIntegerBinding(aRcv, () -> aLoadout.getJumpJetCount(), itemsOrPodsChanged);
        jumpJetMax = new LsmlIntegerBinding(aRcv, () -> aLoadout.getJumpJetsMax(), itemsOrPodsChanged);
        torsoPitch = new LsmlDoubleBinding(aRcv, () -> mp.getTorsoPitchMax(modifiers), engineOrEffsChanged);
        torsoYaw = new LsmlDoubleBinding(aRcv, () -> mp.getTorsoYawMax(modifiers), engineOrEffsChanged);
        armPitch = new LsmlDoubleBinding(aRcv, () -> mp.getArmPitchMax(modifiers), engineOrEffsChanged);
        armYaw = new LsmlDoubleBinding(aRcv, () -> mp.getArmYawMax(modifiers), engineOrEffsChanged);

        // Heat
        heatSinkCount = new LsmlIntegerBinding(aRcv, () -> aLoadout.getHeatsinksCount(), itemsOrPodsChanged);
        heatCapacity = new LsmlDoubleBinding(aRcv, () -> metrics.heatCapacity.calculate(), affectsHeatOrDamage);
        heatDissipation = new LsmlDoubleBinding(aRcv, () -> metrics.heatDissipation.calculate(), affectsHeatOrDamage);
        coolingRatio = new LsmlDoubleBinding(aRcv, () -> metrics.alphaGroup.coolingRatio.calculate(),
                affectsHeatOrDamage);
        timeToCool = new LsmlDoubleBinding(aRcv, () -> metrics.timeToCool.calculate(), affectsHeatOrDamage);

        alphaGroup = new GroupMetricsModelAdaptor(xBar, metrics.alphaGroup, affectsHeatOrDamage);

        for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
            weaponGroups[i] = new GroupMetricsModelAdaptor(xBar, metrics.weaponGroups[i], affectsHeatOrDamage);
        }

        burstTime.addListener((aObservable, aOld, aNew) -> {
            metrics.changeTime(aNew.doubleValue(), -1);
            for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
                metrics.changeTime(aNew.doubleValue(), i);
            }
            updateHeatAndDamageMetrics();
        });

        range.addListener((aObservable, aOld, aNew) -> {
            metrics.changeRange(aNew.doubleValue(), -1);
            for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
                metrics.changeRange(aNew.doubleValue(), i);
            }
            updateHeatAndDamageMetrics();
        });

        environment.addListener((aObservable, aOld, aNew) -> {
            metrics.changeEnvironment(aNew);
            updateHeatAndDamageMetrics();
        });
    }

    public void updateHeatAndDamageMetrics() {
        xBar.post(new Message() {
            @Override
            public boolean isForMe(Loadout aLoadout) {
                return true;
            }

            @Override
            public boolean affectsHeatOrDamage() {
                return true;
            }
        });
    }
}
