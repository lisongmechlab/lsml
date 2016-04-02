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

import java.util.Collection;
import java.util.function.Predicate;

import org.lisoft.lsml.messages.EfficienciesMessage;
import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.messages.OmniPodMessage;
import org.lisoft.lsml.model.chassi.MovementProfile;
import org.lisoft.lsml.model.environment.Environment;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutMetrics;
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
 * @author Li Song
 */
public class LoadoutMetricsModelAdaptor {
    public final LoadoutMetrics              metrics;
    private final MessageXBar                xBar;

    // Mobility
    public final DoubleBinding               topSpeed;
    public final DoubleBinding               turnSpeed;
    public final DoubleBinding               torsoPitchSpeed;
    public final DoubleBinding               torsoYawSpeed;
    public final DoubleBinding               armPitchSpeed;
    public final DoubleBinding               armYawSpeed;
    public final DoubleBinding               torsoPitch;
    public final DoubleBinding               torsoYaw;
    public final DoubleBinding               armPitch;
    public final DoubleBinding               armYaw;
    public final IntegerBinding              jumpJetCount;
    public final IntegerBinding              jumpJetMax;

    // Heat
    public final IntegerBinding              heatSinkCount;
    public final DoubleBinding               heatCapacity;
    public final DoubleBinding               heatDissipation;
    public final DoubleBinding               coolingRatio;
    public final DoubleBinding               timeToCool;

    // Offensive
    public final DoubleBinding               alphaDamage;
    public final DoubleBinding               alphaRange;
    public final DoubleBinding               alphaHeat;
    public final DoubleBinding               alphaGhostHeat;
    public final DoubleBinding               alphaTimeToOverheat;
    public final DoubleBinding               maxDPS;
    public final DoubleBinding               maxDPSRange;
    public final DoubleBinding               sustainedDPS;
    public final DoubleBinding               sustainedDPSRange;
    public final DoubleBinding               burstDamage;
    public final DoubleBinding               burstRange;
    public final DoubleProperty              burstTime                  = new SimpleDoubleProperty(5.0);
    public final ObjectProperty<Double>      range                      = new SimpleObjectProperty<>(null);
    public final ObjectProperty<Environment> environment                = new SimpleObjectProperty<>();

    // Per group
    public final DoubleBinding               groupAlphaTimeToOverHeat[] = new DoubleBinding[WeaponGroups.MAX_GROUPS];
    public final DoubleBinding               groupAlphaGhostHeat[]      = new DoubleBinding[WeaponGroups.MAX_GROUPS];
    public final DoubleBinding               groupAlphaDamage[]         = new DoubleBinding[WeaponGroups.MAX_GROUPS];
    public final DoubleBinding               groupAlphaHeat[]           = new DoubleBinding[WeaponGroups.MAX_GROUPS];
    public final DoubleBinding               groupBurstDamage[]         = new DoubleBinding[WeaponGroups.MAX_GROUPS];
    public final DoubleBinding               groupMaxDPS[]              = new DoubleBinding[WeaponGroups.MAX_GROUPS];
    public final DoubleBinding               groupSustainedDPS[]        = new DoubleBinding[WeaponGroups.MAX_GROUPS];

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
        coolingRatio = new LsmlDoubleBinding(aRcv, () -> metrics.coolingRatio.calculate(), affectsHeatOrDamage);
        timeToCool = new LsmlDoubleBinding(aRcv, () -> metrics.timeToCool.calculate(), affectsHeatOrDamage);

        // Offense
        alphaDamage = new LsmlDoubleBinding(aRcv, () -> metrics.alphaStrike.calculate(), affectsHeatOrDamage);
        alphaRange = new LsmlDoubleBinding(aRcv, () -> metrics.alphaStrike.getRange(), affectsHeatOrDamage);
        alphaHeat = new LsmlDoubleBinding(aRcv, () -> metrics.alphaHeat.calculate(), affectsHeatOrDamage);
        alphaGhostHeat = new LsmlDoubleBinding(aRcv, () -> metrics.ghostHeat.calculate(), affectsHeatOrDamage);
        alphaTimeToOverheat = new LsmlDoubleBinding(aRcv, () -> metrics.alphaTimeToOverHeat.calculate(),
                affectsHeatOrDamage);
        maxDPS = new LsmlDoubleBinding(aRcv, () -> metrics.maxDPS.calculate(), affectsHeatOrDamage);
        maxDPSRange = new LsmlDoubleBinding(aRcv, () -> metrics.maxDPS.getRange(), affectsHeatOrDamage);
        sustainedDPS = new LsmlDoubleBinding(aRcv, () -> metrics.sustainedDPS.calculate(), affectsHeatOrDamage);
        sustainedDPSRange = new LsmlDoubleBinding(aRcv, () -> metrics.sustainedDPS.getRange(), affectsHeatOrDamage);
        burstDamage = new LsmlDoubleBinding(aRcv, () -> metrics.burstDamageOverTime.calculate(), affectsHeatOrDamage);
        burstRange = new LsmlDoubleBinding(aRcv, () -> metrics.burstDamageOverTime.getRange(), affectsHeatOrDamage);

        for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
            final int grp = i;
            groupAlphaTimeToOverHeat[grp] = new LsmlDoubleBinding(aRcv,
                    () -> metrics.groupAlphaTimeToOverHeat[grp].calculate(), affectsHeatOrDamage);
            groupAlphaGhostHeat[grp] = new LsmlDoubleBinding(aRcv, () -> metrics.groupGhostHeat[grp].calculate(),
                    affectsHeatOrDamage);
            groupAlphaDamage[grp] = new LsmlDoubleBinding(aRcv, () -> metrics.groupAlphaStrike[grp].calculate(),
                    affectsHeatOrDamage);
            groupAlphaHeat[grp] = new LsmlDoubleBinding(aRcv, () -> metrics.groupAlphaHeat[grp].calculate(),
                    affectsHeatOrDamage);
            groupBurstDamage[grp] = new LsmlDoubleBinding(aRcv, () -> metrics.groupBurstDamage[grp].calculate(),
                    affectsHeatOrDamage);
            groupMaxDPS[grp] = new LsmlDoubleBinding(aRcv, () -> metrics.groupMaxDPS[grp].calculate(),
                    affectsHeatOrDamage);
            groupSustainedDPS[grp] = new LsmlDoubleBinding(aRcv, () -> metrics.groupsustainedDPS[grp].calculate(),
                    affectsHeatOrDamage);
        }

        burstTime.addListener((aObservable, aOld, aNew) -> {
            metrics.changeTime(aNew.doubleValue());
            updateHeatAndDamageMetrics();
        });

        range.addListener((aObservable, aOld, aNew) -> {
            metrics.changeRange(aNew.doubleValue());
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
