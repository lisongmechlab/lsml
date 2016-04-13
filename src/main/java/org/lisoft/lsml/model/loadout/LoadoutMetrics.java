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
 * @author Li Song
 */
public class LoadoutMetrics {

    public static class GroupMetrics {
        // Heat
        public final AlphaTimeToOverHeat alphaTimeToOverHeat;
        public final CoolingRatio coolingRatio;
        public final GhostHeat ghostHeat;
        public final HeatGeneration heatGeneration;
        public final HeatOverTime heatOverTime;

        // Offense
        public final AlphaStrike alphaStrike;
        public final AlphaHeat alphaHeat;
        public final BurstDamageOverTime burstDamage;
        public final MaxDPS maxDPS;
        public final MaxSustainedDPS sustainedDPS;

        public GroupMetrics(Loadout aLoadout, int aGroup, MessageReception aReception, HeatCapacity aHeatCapacity,
                HeatDissipation aHeatDissipation) {
            heatGeneration = new HeatGeneration(aLoadout, aGroup);
            heatOverTime = new HeatOverTime(aLoadout, aReception, aGroup);
            alphaTimeToOverHeat = new AlphaTimeToOverHeat(aHeatCapacity, heatOverTime, aHeatDissipation);
            coolingRatio = new CoolingRatio(aHeatDissipation, heatGeneration);
            ghostHeat = new GhostHeat(aLoadout, aGroup);

            // Offense
            alphaStrike = new AlphaStrike(aLoadout, aGroup);
            alphaHeat = new AlphaHeat(aLoadout, aGroup);
            burstDamage = new BurstDamageOverTime(aLoadout, aReception, aGroup);
            maxDPS = new MaxDPS(aLoadout, aGroup);
            sustainedDPS = new MaxSustainedDPS(aLoadout, aHeatDissipation, aGroup);
        }

        /**
         * @param aRange
         *            The new range, or -1 for optimal.
         */
        public void changeRange(double aRange) {
            alphaStrike.changeRange(aRange);
            maxDPS.changeRange(aRange);
            sustainedDPS.changeRange(aRange);
            burstDamage.changeRange(aRange);
        }

        /**
         * @param aTime
         *            The new time to use for time dependent metrics.
         */
        public void changeTime(double aTime) {
            burstDamage.changeTime(aTime);
        }
    }

    // Mobility
    public final JumpDistance jumpDistance;
    public final ReverseSpeed reverseSpeed;
    public final TopSpeed topSpeed;
    public final TurningSpeed turningSpeed;
    public final TorsoTwistYawSpeed torsoYawSpeed;
    public final TorsoTwistPitchSpeed torsoPitchSpeed;
    public final ArmRotateYawSpeed armYawSpeed;
    public final ArmRotatePitchSpeed armPitchSpeed;

    // Heat
    public final AsymptoticTimeToOverHeat asymptoticTimeToOverHeat;
    public final HeatCapacity heatCapacity;
    public final HeatDissipation heatDissipation;
    public final TimeToCool timeToCool;

    public final GroupMetrics alphaGroup;

    public final GroupMetrics[] weaponGroups = new GroupMetrics[WeaponGroups.MAX_GROUPS];

    // Defense
    // public final CriticalItemDamage criticalItemDamage;
    // public final CriticalStrikeProbability criticalStrikeProbability;
    // public final ItemEffectiveHP itemEffectiveHP;

    public LoadoutMetrics(Loadout aLoadout, Environment aEnvironment, MessageReception aReception) {
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
        heatCapacity = new HeatCapacity(aLoadout);
        heatDissipation = new HeatDissipation(aLoadout, aEnvironment);
        timeToCool = new TimeToCool(heatCapacity, heatDissipation);

        alphaGroup = new GroupMetrics(aLoadout, -1, aReception, heatCapacity, heatDissipation);

        asymptoticTimeToOverHeat = new AsymptoticTimeToOverHeat(heatCapacity, heatDissipation,
                alphaGroup.heatGeneration);

        // Per group
        for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
            weaponGroups[i] = new GroupMetrics(aLoadout, i, aReception, heatCapacity, heatDissipation);
        }

        // Defense
    }

    /**
     * Changes the range for which range based metrics are calculated.
     * 
     * @param aRange
     *            The new range in meters or -1 to select optimal range.
     * @param aGroup
     *            The group to change range for or -1 for the alpha group.
     */
    public void changeRange(double aRange, int aGroup) {
        if (aGroup < 0) {
            alphaGroup.changeRange(aRange);
        }
        else {
            weaponGroups[aGroup].changeRange(aRange);
        }
    }

    /**
     * Changes the time duration for which time base metrics are calculated.
     * 
     * @param aTime
     *            The new time in seconds.
     * @param aGroup
     *            The group to change range for or -1 for the alpha group.
     */
    public void changeTime(double aTime, int aGroup) {
        if (aGroup < 0) {
            alphaGroup.changeTime(aTime);
        }
        else {
            weaponGroups[aGroup].changeTime(aTime);
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
