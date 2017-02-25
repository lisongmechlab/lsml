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
package org.lisoft.lsml.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.chassi.ArmourSide;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.item.EngineType;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.upgrades.ArmourUpgrade;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;

/**
 * This operation will distribute a number of points of armour (rounded down to the closest half ton) on a loadout,
 * respecting manually set values.
 *
 * @author Emily Björk
 */
public class CmdDistributeArmour extends CompositeCommand {
    private final Map<Location, Integer> armours = new HashMap<>(Location.values().length);
    private final Loadout loadout;
    private final int totalPointsOfArmour;
    private final double frontRearRatio;

    /**
     * @param aLoadout
     *            The {@link Loadout} to distribute armour on.
     * @param aPointsOfArmour
     *            The wanted amount of total armour.
     * @param aFrontRearRatio
     *            The ratio of front/back on armour.
     * @param aMessageDelivery
     *            The {@link MessageDelivery} to send messages on.
     */
    public CmdDistributeArmour(Loadout aLoadout, int aPointsOfArmour, double aFrontRearRatio,
            MessageDelivery aMessageDelivery) {
        super("distribute armour", aMessageDelivery);
        loadout = aLoadout;
        totalPointsOfArmour = aPointsOfArmour;
        frontRearRatio = aFrontRearRatio;
    }

    /**
     * @see org.lisoft.lsml.util.CommandStack.Command#canCoalescele(org.lisoft.lsml.util.CommandStack.Command)
     */
    @Override
    public boolean canCoalescele(Command aOperation) {
        if (this == aOperation) {
            return false;
        }
        if (aOperation == null) {
            return false;
        }
        if (!(aOperation instanceof CmdDistributeArmour)) {
            return false;
        }
        final CmdDistributeArmour operation = (CmdDistributeArmour) aOperation;
        return loadout == operation.loadout;
    }

    @Override
    protected void buildCommand() {
        final int armourLeft = calculateArmourToDistribute(loadout, totalPointsOfArmour);
        if (armourLeft > 0) {
            final Map<Location, Integer> prioMap = prioritize(loadout);
            distribute(loadout, armourLeft, prioMap);
        }
        applyArmours(loadout, frontRearRatio, messageBuffer);
    }

    private void applyArmours(Loadout aLoadout, double aFrontRearRatio, MessageDelivery aMessageDelivery) {
        for (final Location part : Location.values()) {
            final ConfiguredComponent component = aLoadout.getComponent(part);

            if (component.hasManualArmour()) {
                continue;
            }
            for (final ArmourSide side : ArmourSide.allSides(component.getInternalComponent())) {
                addOp(new CmdSetArmour(aMessageDelivery, loadout, component, side, 0, false));
            }
        }

        for (final Location part : Location.values()) {
            final ConfiguredComponent loadoutPart = aLoadout.getComponent(part);

            if (loadoutPart.hasManualArmour()) {
                continue;
            }

            final int armour = getArmour(loadoutPart);
            if (loadoutPart.getInternalComponent().getLocation().isTwoSided()) {
                // 1) front + back = max
                // 2) front / back = ratio
                // front = back * ratio
                // front = max - back
                // = > back * ratio = max - back
                final int back = (int) (armour / (aFrontRearRatio + 1));
                final int front = armour - back;

                addOp(new CmdSetArmour(aMessageDelivery, loadout, loadoutPart, ArmourSide.FRONT, front, false));
                addOp(new CmdSetArmour(aMessageDelivery, loadout, loadoutPart, ArmourSide.BACK, back, false));
            }
            else {
                addOp(new CmdSetArmour(aMessageDelivery, loadout, loadoutPart, ArmourSide.ONLY, armour, false));
            }
        }
    }

    private int calculateArmourToDistribute(Loadout aLoadout, int aPointsOfArmour) {
        final ArmourUpgrade armourUpgrade = aLoadout.getUpgrades().getArmour();
        final double unarmouredMass = aLoadout.getMassStructItems();
        final double requestedArmourMass = aPointsOfArmour / armourUpgrade.getArmourPerTon();
        final double expectedLoadoutMass = Math.floor((unarmouredMass + requestedArmourMass) * 2) / 2; // Round down to
                                                                                                       // closest half
                                                                                                       // ton
        final double expectedArmourMass = expectedLoadoutMass - unarmouredMass;
        int armourLeft = (int) (expectedArmourMass * armourUpgrade.getArmourPerTon());

        // We can't apply more armour than we can carry
        final int maxArmourTonnage = (int) ((aLoadout.getChassis().getMassMax() - unarmouredMass)
                * armourUpgrade.getArmourPerTon());
        armourLeft = Math.min(maxArmourTonnage, armourLeft);

        int maxArmourPoints = 0;

        // Discount armour that is manually fixed.
        for (final Location part : Location.values()) {
            final ConfiguredComponent loadoutPart = aLoadout.getComponent(part);
            if (loadoutPart.hasManualArmour()) {
                armourLeft -= loadoutPart.getArmourTotal();
            }
            else {
                maxArmourPoints += loadoutPart.getInternalComponent().getArmourMax();
            }
        }
        armourLeft = Math.min(maxArmourPoints, armourLeft);
        return armourLeft;
    }

    private void distribute(final Loadout aLoadout, int aArmourAmount, final Map<Location, Integer> aPriorities) {
        int prioSum = 0;
        for (final double prio : aPriorities.values()) {
            prioSum += prio;
        }

        final TreeMap<Location, Integer> byPriority = new TreeMap<>((aO1, aO2) -> {
            final int c = -aPriorities.get(aO1).compareTo(aPriorities.get(aO2));
            if (c == 0) {
                final int d = Integer.compare(aLoadout.getComponent(aO1).getInternalComponent().getArmourMax(),
                        aLoadout.getComponent(aO2).getInternalComponent().getArmourMax());
                if (d == 0) {
                    return aO1.compareTo(aO2);
                }
                return d;
            }
            return c;
        });
        byPriority.putAll(aPriorities);

        int armourLeft = aArmourAmount;
        for (final Entry<Location, Integer> entry : byPriority.entrySet()) {
            final Location part = entry.getKey();
            final int prio = entry.getValue();
            if (prio == 0) {
                continue;
            }

            final ConfiguredComponent loadoutPart = aLoadout.getComponent(part);
            final int armour = Math.min(loadoutPart.getInternalComponent().getArmourMax(), armourLeft * prio / prioSum);
            setArmour(loadoutPart, armour);
            armourLeft -= armour;
            prioSum -= prio;
        }

        final List<ConfiguredComponent> parts = new ArrayList<>(aLoadout.getComponents());
        while (armourLeft > 0 && !parts.isEmpty()) {
            final Iterator<ConfiguredComponent> it = parts.iterator();
            while (it.hasNext()) {
                final ConfiguredComponent part = it.next();
                if (part.hasManualArmour() || getArmour(part) == part.getInternalComponent().getArmourMax()) {
                    it.remove();
                }
            }

            int partsLeft = parts.size();
            for (final ConfiguredComponent loadoutPart : parts) {
                final int additionalArmour = Math.min(
                        loadoutPart.getInternalComponent().getArmourMax() - getArmour(loadoutPart),
                        armourLeft / partsLeft);
                setArmour(loadoutPart, getArmour(loadoutPart) + additionalArmour);
                armourLeft -= additionalArmour;
                partsLeft--;
            }
        }
    }

    private int getArmour(ConfiguredComponent aPart) {
        final Integer stored = armours.get(aPart.getInternalComponent().getLocation());
        if (stored != null) {
            return stored;
        }
        return 0;
    }

    private Map<Location, Integer> prioritize(Loadout aLoadout) {
        final Map<Location, Integer> ans = new HashMap<>(Location.values().length);

        for (final Location location : Location.values()) {
            final ConfiguredComponent loadoutPart = aLoadout.getComponent(location);
            if (loadoutPart.hasManualArmour()) {
                continue;
            }

            // Protect engine at all costs
            if (location == Location.CenterTorso) {
                ans.put(location, 2000);
            }
            else if (location == Location.LeftTorso || location == Location.RightTorso) {
                if (loadout.getEngine() != null && loadout.getEngine().getType() == EngineType.XL) {
                    ans.put(location, 1000);
                }
                else {
                    ans.put(location, 20);
                }
            }
            // Legs and head are high priority too
            else if (location == Location.LeftLeg || location == Location.RightLeg) {
                ans.put(location, 10);
            }
            else if (location == Location.Head) {
                ans.put(location, 7);
            }
            else if (loadoutPart.getItemMass() == 0.0 && !ans.containsKey(location)) {
                ans.put(location, 0);
            }
            else {
                if (location == Location.LeftArm) {
                    ans.put(Location.LeftArm, 10);
                    if (!aLoadout.getComponent(Location.LeftTorso).hasManualArmour()
                            && (!ans.containsKey(Location.LeftTorso) || ans.get(Location.LeftTorso) < 10)) {
                        ans.put(Location.LeftTorso, 10);
                    }
                }
                else if (location == Location.RightArm) {
                    ans.put(Location.RightArm, 10);
                    if (!aLoadout.getComponent(Location.RightTorso).hasManualArmour()
                            && (!ans.containsKey(Location.RightTorso) || ans.get(Location.RightTorso) < 10)) {
                        ans.put(Location.RightTorso, 10);
                    }
                }
                else {
                    ans.put(location, 10);
                }
            }
        }
        return ans;
    }

    private void setArmour(ConfiguredComponent aComponent, int aArmour) {
        armours.put(aComponent.getInternalComponent().getLocation(), aArmour);
    }
}
