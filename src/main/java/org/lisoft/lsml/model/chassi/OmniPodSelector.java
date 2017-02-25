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
package org.lisoft.lsml.model.chassi;

import static java.lang.Math.max;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.lisoft.lsml.math.graph.BackTrackingSolver;
import org.lisoft.lsml.math.graph.PartialCandidate;
import org.lisoft.lsml.model.datacache.OmniPodDB;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;

/**
 * Determines {@link OmniPod}s for a {@link LoadoutOmniMech} so that the given requirements on hard points is met.
 *
 * @author Emily Björk
 */
public class OmniPodSelector {
    private static class PartialSelection implements PartialCandidate<PartialSelection> {
        /**
         * The order in which different locations are tried.
         */
        private final static Location[] LOCATION_ORDER = new Location[] { Location.CenterTorso, Location.RightTorso,
                Location.LeftTorso, Location.RightArm, Location.LeftArm, Location.Head, Location.LeftLeg,
                Location.RightLeg };

        private final Map<Location, List<OmniPod>> allowedPods;
        private final int location;
        private final int pod;

        private final Map<Location, OmniPod> currentState;
        private final int remainingEnergy;
        private final int remainingMissile;
        private final int remainingBallistic;
        private final int remainingJumpJet;
        private final int remainingECM;

        /**
         * Creates a root partial selection to start searching for. Also defines the constraints.
         *
         * @param aAllowedPods
         *            The pods that are allowed to be selected (preferably (but not necessarily) pruned from pods that
         *            do not affect the constraints).
         * @param aWantedEnergy
         *            The number of wanted energy hard points.
         * @param aWantedMissile
         *            The number of wanted missile hard points.
         * @param aWantedBallistic
         *            The number of wanted ballistics hard points.
         * @param aWantedJumpJet
         *            The number of wanted jump jets.
         * @param aWantEcm
         *            Whether or not ECM is wanted.
         */
        public PartialSelection(Map<Location, List<OmniPod>> aAllowedPods, int aWantedEnergy, int aWantedMissile,
                int aWantedBallistic, int aWantedJumpJet, boolean aWantEcm) {
            location = 0; // CT
            pod = -1; // Not used on root
            allowedPods = aAllowedPods;

            currentState = new HashMap<>();
            remainingEnergy = aWantedEnergy;
            remainingMissile = aWantedMissile;
            remainingBallistic = aWantedBallistic;
            remainingJumpJet = aWantedJumpJet;
            remainingECM = aWantEcm ? 1 : 0;
        }

        private PartialSelection(PartialSelection aPrevious, int aLocation, int aPod) {
            location = aLocation;
            pod = aPod;
            allowedPods = aPrevious.allowedPods;

            currentState = new HashMap<>(aPrevious.currentState);
            final Location key = LOCATION_ORDER[location];
            if (aPrevious.location == location) {
                // Change pod
                final OmniPod oldPod = currentState.get(key);
                final OmniPod newPod = allowedPods.get(key).get(pod);
                remainingEnergy = aPrevious.remainingEnergy - newPod.getHardPointCount(HardPointType.ENERGY)
                        + oldPod.getHardPointCount(HardPointType.ENERGY);
                remainingMissile = aPrevious.remainingMissile - newPod.getHardPointCount(HardPointType.MISSILE)
                        + oldPod.getHardPointCount(HardPointType.MISSILE);
                remainingBallistic = aPrevious.remainingBallistic - newPod.getHardPointCount(HardPointType.BALLISTIC)
                        + oldPod.getHardPointCount(HardPointType.BALLISTIC);
                remainingJumpJet = aPrevious.remainingJumpJet - newPod.getJumpJetsMax() + oldPod.getJumpJetsMax();
                remainingECM = aPrevious.remainingECM - newPod.getHardPointCount(HardPointType.ECM)
                        + oldPod.getHardPointCount(HardPointType.ECM);
                currentState.put(key, newPod);
            }
            else {
                // Add new pod
                final OmniPod newPod = allowedPods.get(key).get(pod);
                remainingEnergy = aPrevious.remainingEnergy - newPod.getHardPointCount(HardPointType.ENERGY);
                remainingMissile = aPrevious.remainingMissile - newPod.getHardPointCount(HardPointType.MISSILE);
                remainingBallistic = aPrevious.remainingBallistic - newPod.getHardPointCount(HardPointType.BALLISTIC);
                remainingJumpJet = aPrevious.remainingJumpJet - newPod.getJumpJetsMax();
                remainingECM = aPrevious.remainingECM - newPod.getHardPointCount(HardPointType.ECM);
                currentState.put(key, newPod);
            }
        }

        @Override
        public boolean accept() {
            return remainingBallistic <= 0 && //
                    remainingMissile <= 0 && //
                    remainingEnergy <= 0 && //
                    remainingJumpJet <= 0 && //
                    remainingECM <= 0;
        }

        @Override
        public Optional<PartialSelection> first() {
            int newLocation = location;
            final int newPod = 0;
            List<OmniPod> allowed = null;
            do {
                newLocation = newLocation + 1;
                if (newLocation >= LOCATION_ORDER.length) {
                    return Optional.empty();
                }
                allowed = allowedPods.get(LOCATION_ORDER[newLocation]);
            } while (allowed.isEmpty());
            return Optional.of(new PartialSelection(this, newLocation, newPod));
        }

        @Override
        public Optional<PartialSelection> next() {
            final List<OmniPod> allowed = allowedPods.get(LOCATION_ORDER[location]);
            final int newPod = pod + 1;
            if (newPod < allowed.size()) {
                return Optional.of(new PartialSelection(this, location, newPod));
            }
            return Optional.empty();
        }

        @Override
        public boolean reject() {
            int maxPossibleEnergy = 0;
            int maxPossibleMissile = 0;
            int maxPossibleBallistic = 0;
            int maxPossibleJumpJet = 0;
            int maxPossibleEcm = 0;

            for (int remLoc = location + 1; remLoc < LOCATION_ORDER.length; ++remLoc) {
                final List<OmniPod> allowed = allowedPods.get(LOCATION_ORDER[remLoc]);
                int localMaxPossibleEnergy = 0;
                int localMaxPossibleMissile = 0;
                int localMaxPossibleBallistic = 0;
                int localMaxPossibleJumpJet = 0;
                int localMaxPossibleEcm = 0;

                for (final OmniPod omniPod : allowed) {
                    localMaxPossibleBallistic = max(localMaxPossibleBallistic,
                            omniPod.getHardPointCount(HardPointType.BALLISTIC));
                    localMaxPossibleEnergy = max(localMaxPossibleEnergy,
                            omniPod.getHardPointCount(HardPointType.ENERGY));
                    localMaxPossibleMissile = max(localMaxPossibleMissile,
                            omniPod.getHardPointCount(HardPointType.MISSILE));
                    localMaxPossibleEcm = max(localMaxPossibleEcm, omniPod.getHardPointCount(HardPointType.ECM));
                    localMaxPossibleJumpJet = max(localMaxPossibleJumpJet, omniPod.getJumpJetsMax());
                }
                maxPossibleEnergy += localMaxPossibleEnergy;
                maxPossibleMissile += localMaxPossibleMissile;
                maxPossibleBallistic += localMaxPossibleBallistic;
                maxPossibleJumpJet += localMaxPossibleJumpJet;
                maxPossibleEcm += localMaxPossibleEcm;
            }

            return maxPossibleEnergy < remainingEnergy || // Break it up for coverage
                    maxPossibleMissile < remainingMissile || //
                    maxPossibleBallistic < remainingBallistic || //
                    maxPossibleJumpJet < remainingJumpJet || //
                    maxPossibleEcm < remainingECM;
        }
    }

    private final BackTrackingSolver<PartialSelection> solver = new BackTrackingSolver<>();

    public Optional<Map<Location, OmniPod>> selectPods(ChassisOmniMech aChassis, int aWantedEnergy, int aWantedMissile,
            int aWantedBallistic, int aWantedJumpJet, boolean aWantEcm) {

        final Map<Location, List<OmniPod>> allowedPods = new HashMap<>();
        for (final Location location : Location.values()) {
            allowedPods.put(location, OmniPodDB.lookup(aChassis, location));
        }

        // Discount hard points in the CT
        final OmniPod ct = aChassis.getComponent(Location.CenterTorso).getFixedOmniPod();
        final int energy = aWantedEnergy - ct.getHardPointCount(HardPointType.ENERGY);
        final int missile = aWantedMissile - ct.getHardPointCount(HardPointType.MISSILE);
        final int ballistic = aWantedBallistic - ct.getHardPointCount(HardPointType.BALLISTIC);
        final int jumpJet = aWantedJumpJet - aChassis.getFixedJumpJets();
        final boolean ecm = ct.getHardPointCount(HardPointType.ECM) > 0 ? false : aWantEcm;

        final PartialSelection root = new PartialSelection(allowedPods, energy, missile, ballistic, jumpJet, ecm);
        final Optional<PartialSelection> ans = solver.solveOne(root);
        if (ans.isPresent()) {
            return Optional.of(ans.get().currentState);
        }
        return Optional.empty();
    }
}
