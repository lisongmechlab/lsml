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
package org.lisoft.lsml.model.loadout;

import org.lisoft.lsml.messages.MessageReception;
import org.lisoft.lsml.model.environment.Environment;
import org.lisoft.lsml.model.metrics.AlphaHeat;
import org.lisoft.lsml.model.metrics.AlphaStrike;
import org.lisoft.lsml.model.metrics.AlphaTimeToOverHeat;
import org.lisoft.lsml.model.metrics.ArmRotatePitchSpeed;
import org.lisoft.lsml.model.metrics.ArmRotateYawSpeed;
import org.lisoft.lsml.model.metrics.AsymptoticTimeToOverHeat;
import org.lisoft.lsml.model.metrics.BurstDamageOverTime;
import org.lisoft.lsml.model.metrics.CoolingRatio;
import org.lisoft.lsml.model.metrics.GhostHeat;
import org.lisoft.lsml.model.metrics.HeatCapacity;
import org.lisoft.lsml.model.metrics.HeatDissipation;
import org.lisoft.lsml.model.metrics.HeatGeneration;
import org.lisoft.lsml.model.metrics.HeatOverTime;
import org.lisoft.lsml.model.metrics.JumpDistance;
import org.lisoft.lsml.model.metrics.MaxDPS;
import org.lisoft.lsml.model.metrics.MaxSustainedDPS;
import org.lisoft.lsml.model.metrics.Metric;
import org.lisoft.lsml.model.metrics.ReverseSpeed;
import org.lisoft.lsml.model.metrics.TimeToCool;
import org.lisoft.lsml.model.metrics.TopSpeed;
import org.lisoft.lsml.model.metrics.TorsoTwistPitchSpeed;
import org.lisoft.lsml.model.metrics.TorsoTwistYawSpeed;
import org.lisoft.lsml.model.metrics.TurningSpeed;

/**
 * This is a convenience class that collects all useful metrics about a loadout in one easily accessible and re-usable
 * place.
 * 
 * @author Emily Björk
 */
public class LoadoutMetrics {
    // Mobility
    public final JumpDistance             jumpDistance;
    public final ReverseSpeed             reverseSpeed;
    public final TopSpeed                 topSpeed;
    public final TurningSpeed             turningSpeed;
    public final TorsoTwistYawSpeed       torsoYawSpeed;

    // Heat
    public final AlphaTimeToOverHeat      alphaTimeToOverHeat;
    public final AlphaHeat                alphaHeat;
    public final AsymptoticTimeToOverHeat asymptoticTimeToOverHeat;
    public final CoolingRatio             coolingRatio;
    public final GhostHeat                ghostHeat;
    public final HeatCapacity             heatCapacity;
    public final HeatDissipation          heatDissipation;
    public final HeatGeneration           heatGeneration;
    public final HeatOverTime             heatOverTime;
    public final TimeToCool               timeToCool;

    // Offense
    public final AlphaStrike              alphaStrike;
    public final BurstDamageOverTime      burstDamageOverTime;
    public final MaxDPS                   maxDPS;
    public final MaxSustainedDPS          sustainedDPS;

    // Per group (Heat)
    public final AlphaTimeToOverHeat      groupAlphaTimeToOverHeat[] = new AlphaTimeToOverHeat[WeaponGroups.MAX_GROUPS];
    public final CoolingRatio             groupCoolingRatio[]        = new CoolingRatio[WeaponGroups.MAX_GROUPS];
    public final GhostHeat                groupGhostHeat[]           = new GhostHeat[WeaponGroups.MAX_GROUPS];
    public final HeatGeneration           groupHeatGeneration[]      = new HeatGeneration[WeaponGroups.MAX_GROUPS];
    public final HeatOverTime             groupHeatOverTime[]        = new HeatOverTime[WeaponGroups.MAX_GROUPS];

    // Per group (Offense)
    public final AlphaStrike              groupAlphaStrike[]         = new AlphaStrike[WeaponGroups.MAX_GROUPS];
    public final AlphaHeat                groupAlphaHeat[]           = new AlphaHeat[WeaponGroups.MAX_GROUPS];
    public final BurstDamageOverTime      groupBurstDamage[]         = new BurstDamageOverTime[WeaponGroups.MAX_GROUPS];
    public final MaxDPS                   groupMaxDPS[]              = new MaxDPS[WeaponGroups.MAX_GROUPS];
    public final MaxSustainedDPS          groupsustainedDPS[]        = new MaxSustainedDPS[WeaponGroups.MAX_GROUPS];
    public final Metric                   torsoPitchSpeed;
    public final Metric                   armYawSpeed;
    public final Metric                   armPitchSpeed;

    // Defense
    // public final CriticalItemDamage criticalItemDamage;
    // public final CriticalStrikeProbability criticalStrikeProbability;
    // public final ItemEffectiveHP itemEffectiveHP;

    public LoadoutMetrics(LoadoutBase<?> aLoadout, Environment aEnvironment, MessageReception aReception) {
        // Mobility
        jumpDistance = new JumpDistance(aLoadout);
        reverseSpeed = new ReverseSpeed(aLoadout);
        topSpeed = new TopSpeed(aLoadout);
        turningSpeed = new TurningSpeed(aLoadout);
        torsoYawSpeed = new TorsoTwistYawSpeed(aLoadout);
        torsoPitchSpeed = new TorsoTwistPitchSpeed(aLoadout);
        armYawSpeed = new ArmRotateYawSpeed(aLoadout);
        armPitchSpeed = new ArmRotatePitchSpeed(aLoadout);

        // Heat
        ghostHeat = new GhostHeat(aLoadout);
        heatCapacity = new HeatCapacity(aLoadout);
        heatDissipation = new HeatDissipation(aLoadout, aEnvironment);
        heatGeneration = new HeatGeneration(aLoadout);
        heatOverTime = new HeatOverTime(aLoadout, aReception);
        alphaTimeToOverHeat = new AlphaTimeToOverHeat(heatCapacity, heatOverTime, heatDissipation);
        asymptoticTimeToOverHeat = new AsymptoticTimeToOverHeat(heatCapacity, heatDissipation, heatGeneration);
        coolingRatio = new CoolingRatio(heatDissipation, heatGeneration);
        timeToCool = new TimeToCool(heatCapacity, heatDissipation);
        alphaHeat = new AlphaHeat(aLoadout);

        // Offense
        alphaStrike = new AlphaStrike(aLoadout);
        burstDamageOverTime = new BurstDamageOverTime(aLoadout, aReception);
        maxDPS = new MaxDPS(aLoadout);
        sustainedDPS = new MaxSustainedDPS(aLoadout, heatDissipation);

        // Per group
        for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
            // Heat
            groupHeatGeneration[i] = new HeatGeneration(aLoadout, i);
            groupHeatOverTime[i] = new HeatOverTime(aLoadout, aReception, i);
            groupAlphaTimeToOverHeat[i] = new AlphaTimeToOverHeat(heatCapacity, groupHeatOverTime[i], heatDissipation);
            groupCoolingRatio[i] = new CoolingRatio(heatDissipation, groupHeatGeneration[i]);
            groupGhostHeat[i] = new GhostHeat(aLoadout, i);

            // Offense
            groupAlphaStrike[i] = new AlphaStrike(aLoadout, i);
            groupAlphaHeat[i] = new AlphaHeat(aLoadout, i);
            groupBurstDamage[i] = new BurstDamageOverTime(aLoadout, aReception, i);
            groupMaxDPS[i] = new MaxDPS(aLoadout, i);
            groupsustainedDPS[i] = new MaxSustainedDPS(aLoadout, heatDissipation, i);
        }

        // Defense
    }

    /**
     * Changes the range for which range based metrics are calculated.
     * 
     * @param aRange
     *            The new range in meters or -1 to select optimal range.
     */
    public void changeRange(double aRange) {
        alphaStrike.changeRange(aRange);
        maxDPS.changeRange(aRange);
        sustainedDPS.changeRange(aRange);
        burstDamageOverTime.changeRange(aRange);

        for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
            groupAlphaStrike[i].changeRange(aRange);
            groupMaxDPS[i].changeRange(aRange);
            groupsustainedDPS[i].changeRange(aRange);
            groupBurstDamage[i].changeRange(aRange);
        }
    }

    /**
     * Changes the time duration for which time base metrics are calculated.
     * 
     * @param aTime
     *            The new time in seconds.
     */
    public void changeTime(double aTime) {
        burstDamageOverTime.changeTime(aTime);
        for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
            groupBurstDamage[i].changeTime(aTime);
        }
    }

    /**
     * Changes the environment the metrics are calculated in.
     * 
     * @param aEnvironment
     *            The new environment.
     */
    public void changeEnvironment(Environment aEnvironment) {
        heatDissipation.changeEnvironment(aEnvironment);
    }
}
