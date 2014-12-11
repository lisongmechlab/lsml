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
package lisong_mechlab.model.loadout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.EngineType;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.loadout.component.OpSetArmor;
import lisong_mechlab.model.upgrades.ArmorUpgrade;
import lisong_mechlab.util.OperationStack.CompositeOperation;
import lisong_mechlab.util.OperationStack.Operation;
import lisong_mechlab.util.message.MessageDelivery;

/**
 * This operation will distribute a number of points of armor (rounded down to the closest half ton) on a loadout,
 * respecting manually set values.
 * 
 * @author Li Song
 */
public class OpDistributeArmor extends CompositeOperation {
    private final Map<Location, Integer> armors = new HashMap<>(Location.values().length);
    private final LoadoutBase<?>         loadout;
    private final int                    totalPointsOfArmor;
    private final double                 frontRearRatio;

    /**
     * @param aLoadout
     *            The {@link LoadoutBase} to distribute armor on.
     * @param aPointsOfArmor
     *            The wanted amount of total armor.
     * @param aFrontRearRatio
     *            The ratio of front/back on armor.
     * @param aMessageDelivery
     *            The {@link MessageDelivery} to send messages on.
     */
    public OpDistributeArmor(LoadoutBase<?> aLoadout, int aPointsOfArmor, double aFrontRearRatio,
            MessageDelivery aMessageDelivery) {
        super("distribute armor", aMessageDelivery);
        loadout = aLoadout;
        totalPointsOfArmor = aPointsOfArmor;
        frontRearRatio = aFrontRearRatio;
    }

    /**
     * @see lisong_mechlab.util.OperationStack.Operation#canCoalescele(lisong_mechlab.util.OperationStack.Operation)
     */
    @Override
    public boolean canCoalescele(Operation aOperation) {
        if (this == aOperation)
            return false;
        if (aOperation == null)
            return false;
        if (!(aOperation instanceof OpDistributeArmor))
            return false;
        OpDistributeArmor operation = (OpDistributeArmor) aOperation;
        return loadout == operation.loadout;
    }

    @Override
    protected void buildOperation() {
        int armorLeft = calculateArmorToDistribute(loadout, totalPointsOfArmor);
        if (armorLeft > 0) {
            Map<Location, Integer> prioMap = prioritize(loadout);
            distribute(loadout, armorLeft, prioMap);
        }
        applyArmors(loadout, frontRearRatio, messageBuffer);
    }

    private void distribute(final LoadoutBase<?> aLoadout, int aArmorAmount, final Map<Location, Integer> aPriorities) {
        int prioSum = 0;
        for (double prio : aPriorities.values()) {
            prioSum += prio;
        }

        TreeMap<Location, Integer> byPriority = new TreeMap<>(new Comparator<Location>() {
            @Override
            public int compare(Location aO1, Location aO2) {
                int c = -aPriorities.get(aO1).compareTo(aPriorities.get(aO2));
                if (c == 0) {
                    int d = Integer.compare(aLoadout.getComponent(aO1).getInternalComponent().getArmorMax(), aLoadout
                            .getComponent(aO2).getInternalComponent().getArmorMax());
                    if (d == 0)
                        return aO1.compareTo(aO2);
                    return d;
                }
                return c;
            }
        });
        byPriority.putAll(aPriorities);

        int armorLeft = aArmorAmount;
        for (Entry<Location, Integer> entry : byPriority.entrySet()) {
            Location part = entry.getKey();
            int prio = entry.getValue();
            if (prio == 0)
                continue;

            ConfiguredComponentBase loadoutPart = aLoadout.getComponent(part);
            int armor = Math.min(loadoutPart.getInternalComponent().getArmorMax(), armorLeft * prio / prioSum);
            setArmor(loadoutPart, armor);
            armorLeft -= armor;
            prioSum -= prio;
        }

        List<ConfiguredComponentBase> parts = new ArrayList<>(aLoadout.getComponents());
        while (armorLeft > 0 && !parts.isEmpty()) {
            Iterator<ConfiguredComponentBase> it = parts.iterator();
            while (it.hasNext()) {
                ConfiguredComponentBase part = it.next();
                if (!part.allowAutomaticArmor() || getArmor(part) == part.getInternalComponent().getArmorMax())
                    it.remove();
            }

            int partsLeft = parts.size();
            for (ConfiguredComponentBase loadoutPart : parts) {
                int additionalArmor = Math
                        .min(loadoutPart.getInternalComponent().getArmorMax() - getArmor(loadoutPart), armorLeft
                                / partsLeft);
                setArmor(loadoutPart, getArmor(loadoutPart) + additionalArmor);
                armorLeft -= additionalArmor;
                partsLeft--;
            }
        }
    }

    private int calculateArmorToDistribute(LoadoutBase<?> aLoadout, int aPointsOfArmor) {
        final ArmorUpgrade armorUpgrade = aLoadout.getUpgrades().getArmor();
        final double unarmoredMass = aLoadout.getMassStructItems();
        final double requestedArmorMass = aPointsOfArmor / armorUpgrade.getArmorPerTon();
        final double expectedLoadoutMass = Math.floor((unarmoredMass + requestedArmorMass) * 2) / 2; // Round down to
                                                                                                     // closest half
                                                                                                     // ton
        final double expectedArmorMass = expectedLoadoutMass - unarmoredMass;
        int armorLeft = (int) (expectedArmorMass * armorUpgrade.getArmorPerTon());

        // We can't apply more armor than we can carry
        int maxArmorTonnage = (int) ((aLoadout.getChassis().getMassMax() - unarmoredMass) * armorUpgrade
                .getArmorPerTon());
        armorLeft = Math.min(maxArmorTonnage, armorLeft);

        int maxArmorPoints = 0;

        // Discount armor that is manually fixed.
        for (Location part : Location.values()) {
            final ConfiguredComponentBase loadoutPart = aLoadout.getComponent(part);
            if (!loadoutPart.allowAutomaticArmor()) {
                armorLeft -= loadoutPart.getArmorTotal();
            }
            else {
                maxArmorPoints += loadoutPart.getInternalComponent().getArmorMax();
            }
        }
        armorLeft = Math.min(maxArmorPoints, armorLeft);
        return armorLeft;
    }

    private int getArmor(ConfiguredComponentBase aPart) {
        Integer stored = armors.get(aPart.getInternalComponent().getLocation());
        if (stored != null)
            return stored;
        return 0;
    }

    private void setArmor(ConfiguredComponentBase aPart, int armor) {
        armors.put(aPart.getInternalComponent().getLocation(), armor);
    }

    private void applyArmors(LoadoutBase<?> aLoadout, double aFrontRearRatio, MessageDelivery aMessageDelivery) {
        for (Location part : Location.values()) {
            final ConfiguredComponentBase loadoutPart = aLoadout.getComponent(part);

            if (!loadoutPart.allowAutomaticArmor())
                continue;
            if (loadoutPart.getInternalComponent().getLocation().isTwoSided()) {
                addOp(new OpSetArmor(aMessageDelivery, loadout, loadoutPart, ArmorSide.BACK, 0, false));
                addOp(new OpSetArmor(aMessageDelivery, loadout, loadoutPart, ArmorSide.FRONT, 0, false));
            }
            else {
                addOp(new OpSetArmor(aMessageDelivery, loadout, loadoutPart, ArmorSide.ONLY, 0, false));
            }
        }

        for (Location part : Location.values()) {
            final ConfiguredComponentBase loadoutPart = aLoadout.getComponent(part);

            if (!loadoutPart.allowAutomaticArmor())
                continue;

            int armor = getArmor(loadoutPart);
            if (loadoutPart.getInternalComponent().getLocation().isTwoSided()) {
                // 1) front + back = max
                // 2) front / back = ratio
                // front = back * ratio
                // front = max - back
                // = > back * ratio = max - back
                int back = (int) (armor / (aFrontRearRatio + 1));
                int front = armor - back;

                addOp(new OpSetArmor(aMessageDelivery, loadout, loadoutPart, ArmorSide.FRONT, front, false));
                addOp(new OpSetArmor(aMessageDelivery, loadout, loadoutPart, ArmorSide.BACK, back, false));
            }
            else {
                addOp(new OpSetArmor(aMessageDelivery, loadout, loadoutPart, ArmorSide.ONLY, armor, false));
            }
        }
    }

    private Map<Location, Integer> prioritize(LoadoutBase<?> aLoadout) {
        Map<Location, Integer> ans = new HashMap<>(Location.values().length);

        for (Location location : Location.values()) {
            ConfiguredComponentBase loadoutPart = aLoadout.getComponent(location);
            if (!loadoutPart.allowAutomaticArmor())
                continue;

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
                    if (aLoadout.getComponent(Location.LeftTorso).allowAutomaticArmor()
                            && (!ans.containsKey(Location.LeftTorso) || ans.get(Location.LeftTorso) < 10))
                        ans.put(Location.LeftTorso, 10);
                }
                else if (location == Location.RightArm) {
                    ans.put(Location.RightArm, 10);
                    if (aLoadout.getComponent(Location.RightTorso).allowAutomaticArmor()
                            && (!ans.containsKey(Location.RightTorso) || ans.get(Location.RightTorso) < 10))
                        ans.put(Location.RightTorso, 10);
                }
                else {
                    ans.put(location, 10);
                }
            }
        }
        return ans;
    }
}
