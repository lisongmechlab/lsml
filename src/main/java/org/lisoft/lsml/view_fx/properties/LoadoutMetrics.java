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
package org.lisoft.lsml.view_fx.properties;

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
import org.lisoft.lsml.model.loadout.WeaponGroups;
import org.lisoft.lsml.model.metrics.AlphaHeat;
import org.lisoft.lsml.model.metrics.AlphaHeatPercent;
import org.lisoft.lsml.model.metrics.AlphaStrike;
import org.lisoft.lsml.model.metrics.AlphaTimeToOverHeat;
import org.lisoft.lsml.model.metrics.ArmRotatePitchSpeed;
import org.lisoft.lsml.model.metrics.ArmRotateYawSpeed;
import org.lisoft.lsml.model.metrics.BurstDamageOverTime;
import org.lisoft.lsml.model.metrics.CoolingRatio;
import org.lisoft.lsml.model.metrics.GhostHeat;
import org.lisoft.lsml.model.metrics.HeatCapacity;
import org.lisoft.lsml.model.metrics.HeatDissipation;
import org.lisoft.lsml.model.metrics.HeatGeneration;
import org.lisoft.lsml.model.metrics.HeatOverTime;
import org.lisoft.lsml.model.metrics.MASCSpeed;
import org.lisoft.lsml.model.metrics.MaxDPS;
import org.lisoft.lsml.model.metrics.MaxSustainedDPS;
import org.lisoft.lsml.model.metrics.TimeToCool;
import org.lisoft.lsml.model.metrics.TopSpeed;
import org.lisoft.lsml.model.metrics.TorsoTwistPitchSpeed;
import org.lisoft.lsml.model.metrics.TorsoTwistYawSpeed;
import org.lisoft.lsml.model.metrics.TurningSpeed;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * This class wraps all the metrics that are calculated for a {@link Loadout} in a JavaFX friendly manner.
 *
 * @author Li Song
 */
public class LoadoutMetrics {
    public static class GroupMetrics {
        public final HeatGeneration heatGeneration;
        public final HeatOverTime heatOverTime;

        public final MetricExpression<CoolingRatio> coolingRatio;
        public final MetricExpression<AlphaTimeToOverHeat> alphaTtO;
        public final MetricExpression<GhostHeat> alphaGhostHeat;
        public final RangeMetricExpression<AlphaStrike> alphaDamage;
        public final MetricExpression<AlphaHeat> alphaHeat;
        public final MetricExpression<AlphaHeatPercent> alphaHeatPct;
        public final RangeMetricExpression<BurstDamageOverTime> burstDamage;
        public final RangeMetricExpression<MaxDPS> maxDPS;
        public final RangeMetricExpression<MaxSustainedDPS> sustainedDPS;

        public GroupMetrics(MessageReception aRcv, Loadout aLoadout, int aGroup, HeatCapacity aHeatCapacity,
                HeatDissipation aHeatDissipation, Predicate<Message> aFilter) {
            heatGeneration = new HeatGeneration(aLoadout, aGroup);
            heatOverTime = new HeatOverTime(aLoadout, aRcv, aGroup);

            alphaTtO = new MetricExpression<>(aRcv,
                    new AlphaTimeToOverHeat(aHeatCapacity, heatOverTime, aHeatDissipation), aFilter);
            alphaGhostHeat = new MetricExpression<>(aRcv, new GhostHeat(aLoadout, aGroup), aFilter);
            alphaDamage = new RangeMetricExpression<>(aRcv, new AlphaStrike(aLoadout, aGroup), aFilter);
            alphaHeat = new MetricExpression<>(aRcv, new AlphaHeat(aLoadout, aGroup), aFilter);
            alphaHeatPct = new MetricExpression<>(aRcv, new AlphaHeatPercent(alphaHeat.getMetric(),
                    alphaGhostHeat.getMetric(), aHeatDissipation, aHeatCapacity, aLoadout, aGroup), aFilter);
            burstDamage = new RangeMetricExpression<>(aRcv, new BurstDamageOverTime(aLoadout, aRcv, aGroup), aFilter);
            maxDPS = new RangeMetricExpression<>(aRcv, new MaxDPS(aLoadout, aGroup), aFilter);
            sustainedDPS = new RangeMetricExpression<>(aRcv, new MaxSustainedDPS(aLoadout, aHeatDissipation, aGroup),
                    aFilter);
            coolingRatio = new MetricExpression<>(aRcv, new CoolingRatio(aHeatDissipation, heatGeneration), aFilter);
        }

        /**
         * @param aRange
         *            The new range, or -1 for optimal.
         */
        public void changeRange(Double aRange) {
            alphaDamage.setRange(aRange);
            maxDPS.setRange(aRange);
            sustainedDPS.setRange(aRange);
            burstDamage.setRange(aRange);
        }

        /**
         * @param aTime
         *            The new time to use for time dependent metrics.
         */
        public void changeTime(double aTime) {
            burstDamage.getMetric().changeTime(aTime);
        }
    }

    private static final double DEFAULT_BURST_TIME = 5.0;

    private static final Double DEFAULT_RANGE = null;

    private final MessageXBar xBar;

    // Mobility
    public final MetricExpression<TopSpeed> topSpeed;
    public final MetricExpression<MASCSpeed> mascSpeed;
    public final MetricExpression<TurningSpeed> turnSpeed;
    public final MetricExpression<TorsoTwistPitchSpeed> torsoPitchSpeed;
    public final MetricExpression<TorsoTwistYawSpeed> torsoYawSpeed;
    public final MetricExpression<ArmRotatePitchSpeed> armPitchSpeed;
    public final MetricExpression<ArmRotateYawSpeed> armYawSpeed;

    public final DoubleBinding torsoPitch;
    public final DoubleBinding torsoYaw;
    public final DoubleBinding armPitch;
    public final DoubleBinding armYaw;
    public final IntegerBinding jumpJetCount;
    public final IntegerBinding jumpJetMax;

    // Heat
    public final IntegerBinding heatSinkCount;
    public final MetricExpression<HeatCapacity> heatCapacity;
    public final MetricExpression<HeatDissipation> heatDissipation;
    public final MetricExpression<TimeToCool> timeToCool;

    // Offensive
    public final DoubleProperty burstTime = new SimpleDoubleProperty();
    public final ObjectProperty<Double> range = new SimpleObjectProperty<>();
    public final ObjectProperty<Environment> environmentProperty = new SimpleObjectProperty<>();

    public final GroupMetrics alphaGroup;
    public final GroupMetrics[] weaponGroups = new GroupMetrics[WeaponGroups.MAX_WEAPONS];

    public LoadoutMetrics(Loadout aLoadout, Environment aEnvironment, MessageXBar aRcv) {
        xBar = aRcv;

        final MovementProfile mp = aLoadout.getMovementProfile();
        // Update predicates
        final Predicate<Message> itemsChanged = (aMsg) -> aMsg instanceof ItemMessage;
        final Predicate<Message> effsChanged = (aMsg) -> aMsg instanceof EfficienciesMessage;
        final Predicate<Message> omniPodChanged = (aMsg) -> aMsg instanceof OmniPodMessage;
        final Predicate<Message> affectsHeatOrDamage = (aMsg) -> aMsg.affectsHeatOrDamage();
        final Predicate<Message> engineOrEffsChanged = (aMsg) -> itemsChanged.test(aMsg) || effsChanged.test(aMsg);
        final Predicate<Message> itemsOrPodsChanged = (aMsg) -> itemsChanged.test(aMsg) || omniPodChanged.test(aMsg);

        // Mobility
        topSpeed = new MetricExpression<>(aRcv, new TopSpeed(aLoadout), engineOrEffsChanged);
        mascSpeed = new MetricExpression<>(aRcv, new MASCSpeed(aLoadout, topSpeed.getMetric()), engineOrEffsChanged);
        turnSpeed = new MetricExpression<>(aRcv, new TurningSpeed(aLoadout), engineOrEffsChanged);
        torsoPitchSpeed = new MetricExpression<>(aRcv, new TorsoTwistPitchSpeed(aLoadout), engineOrEffsChanged);
        torsoYawSpeed = new MetricExpression<>(aRcv, new TorsoTwistYawSpeed(aLoadout), engineOrEffsChanged);
        armPitchSpeed = new MetricExpression<>(aRcv, new ArmRotatePitchSpeed(aLoadout), engineOrEffsChanged);
        armYawSpeed = new MetricExpression<>(aRcv, new ArmRotateYawSpeed(aLoadout), engineOrEffsChanged);

        jumpJetCount = new LsmlIntegerBinding(aRcv, aLoadout::getJumpJetCount, itemsOrPodsChanged);
        jumpJetMax = new LsmlIntegerBinding(aRcv, aLoadout::getJumpJetsMax, itemsOrPodsChanged);
        torsoPitch = new LsmlDoubleBinding(aRcv, () -> mp.getTorsoPitchMax(aLoadout.getModifiers()),
                engineOrEffsChanged);
        torsoYaw = new LsmlDoubleBinding(aRcv, () -> mp.getTorsoYawMax(aLoadout.getModifiers()), engineOrEffsChanged);
        armPitch = new LsmlDoubleBinding(aRcv, () -> mp.getArmPitchMax(aLoadout.getModifiers()), engineOrEffsChanged);
        armYaw = new LsmlDoubleBinding(aRcv, () -> mp.getArmYawMax(aLoadout.getModifiers()), engineOrEffsChanged);

        // Heat
        heatSinkCount = new LsmlIntegerBinding(aRcv, () -> aLoadout.getHeatsinksCount(), itemsOrPodsChanged);
        heatCapacity = new MetricExpression<>(aRcv, new HeatCapacity(aLoadout, aEnvironment), affectsHeatOrDamage);
        heatDissipation = new MetricExpression<>(aRcv, new HeatDissipation(aLoadout, aEnvironment),
                affectsHeatOrDamage);
        timeToCool = new MetricExpression<>(aRcv, new TimeToCool(heatCapacity.getMetric(), heatDissipation.getMetric()),
                affectsHeatOrDamage);

        alphaGroup = new GroupMetrics(xBar, aLoadout, -1, heatCapacity.getMetric(), heatDissipation.getMetric(),
                affectsHeatOrDamage);

        for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
            weaponGroups[i] = new GroupMetrics(xBar, aLoadout, i, heatCapacity.getMetric(), heatDissipation.getMetric(),
                    affectsHeatOrDamage);
        }

        burstTime.addListener((aObservable, aOld, aNew) -> {
            alphaGroup.changeTime(aNew.doubleValue());
            for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
                weaponGroups[i].changeTime(aNew.doubleValue());
            }
            updateHeatAndDamageMetrics();
        });
        burstTime.set(DEFAULT_BURST_TIME);

        range.addListener((aObservable, aOld, aNew) -> {
            alphaGroup.changeRange(aNew);
            for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
                weaponGroups[i].changeRange(aNew);
            }
            updateHeatAndDamageMetrics();
        });

        range.set(DEFAULT_RANGE);

        environmentProperty.addListener((aObservable, aOld, aNew) -> {
            heatDissipation.getMetric().changeEnvironment(aNew);
            heatCapacity.getMetric().changeEnvironment(aNew);
            updateHeatAndDamageMetrics();
        });
    }

    public void updateHeatAndDamageMetrics() {
        xBar.post(new Message() {
            @Override
            public boolean affectsHeatOrDamage() {
                return true;
            }

            @Override
            public boolean isForMe(Loadout aLoadout) {
                return true;
            }
        });
    }
}
