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

import java.util.*;

import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.item.Item;

/**
 * This class contains the result after trying to equip an {@link Item} on a {@link Loadout}.
 *
 * @author Li Song
 */
public class EquipResult {
    public enum EquipResultType {
        Success(0, "Success"), //
        TooHeavy(1, "Too heavy"), //
        NotEnoughSlots(2, "Not enough slots"), //
        NotEnoughSlotsForXLSide(10, "Not enough slots for XL side engine"), //
        NotSupported(1, "Not supported by chassis"), //
        IncompatibleUpgrades(100, "Current upgrades do not admit the item"), //
        NoComponentSupport(10, "No component can support the item"), //
        JumpJetCapacityReached(100, "Maximum number of jumpjets already installed"), //
        EngineAlreadyEquipped(100, "An engine is already equipped"), //
        TooManyOfThatType(60, "No more items of that type can be equipped"), //
        NoFreeHardPoints(50, "No free hard points"), //
        ComponentAlreadyHasCase(20, "C.A.S.E. is already equipped"), //
        EverythingAlreadyHasCase(20, "C.A.S.E. is already equipped in all possible locations"), //
        InternalsNotAllowed(100, "Internals cannot be modified"), //
        ExceededMaxArmour(90, "Exceeded max allowed armour"), //
        LargeBoreWeaponPresent(90, "Cannot toggle because a large bore weapon is present"), //
        LaaBeforeHa(90, "Hand actuator can only be enabled if Lower Arm Actuator is enabled"), //
        NotToggleable(90, "Item is not toggleable"), //
        NeedEcm(100, "ECM must be equipped before"), //
        CannotRemoveECM(100, "Cannot remove ECM when stealth armour is equipped");

        private final int specificity;
        private final String message;

        EquipResultType(int aSpecificity, String aMessage) {
            specificity = aSpecificity;
            message = aMessage;
        }

        @Override
        public String toString() {
            return message;
        }

        boolean isMoreSpecificThan(EquipResultType aType) {
            return specificity > aType.specificity;
        }
    }

    static public final EquipResult SUCCESS;
    static private final Map<EquipResultType, List<EquipResult>> RESULTS;

    static {
        RESULTS = new HashMap<>();
        for (final EquipResultType type : EquipResultType.values()) {
            final List<EquipResult> list = new ArrayList<>();
            list.add(new EquipResult(type));
            for (final Location location : Location.values()) {
                list.add(new EquipResult(location, type));
            }
            RESULTS.put(type, Collections.unmodifiableList(list));
        }
        SUCCESS = make(EquipResultType.Success);
    }

    static public EquipResult make(EquipResultType aType) {
        return make(null, aType);
    }

    static public EquipResult make(Location aLocation, EquipResultType aType) {
        final List<EquipResult> l = RESULTS.get(aType);
        for (final EquipResult equipResult : l) {
            if (equipResult.location == aLocation) {
                return equipResult;
            }
        }
        throw new RuntimeException("Results map is missing values!");
    }

    private final EquipResultType type;

    private final Location location;

    private EquipResult(EquipResultType aType) {
        this(null, aType);
    }

    private EquipResult(Location aLocation, EquipResultType aType) {
        location = aLocation;
        type = aType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EquipResult other = (EquipResult) obj;
        if (location != other.location) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

    public EquipResultType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (location == null ? 0 : location.hashCode());
        result = prime * result + (type == null ? 0 : type.hashCode());
        return result;
    }

    public boolean isMoreSpecificThan(EquipResult aResult) {
        return type.isMoreSpecificThan(aResult.type);
    }

    @Override
    public String toString() {
        if (location != null) {
            return type.toString() + " on " + location.longName();
        }
        return type.toString();
    }

}
