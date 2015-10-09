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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.item.Item;

/**
 * This class contains the result after trying to equip an {@link Item} on a {@link LoadoutBase}.
 * 
 * @author Li Song
 */
public class EquipResult extends Exception {
    public static enum Type {
        Success(0, "Success"), //
        TooHeavy(1, "Too heavy"), //
        NotEnoughSlots(2, "Not enough slots"), //
        NotEnoughSlotsForXLSide(10, "Not enough slots for XL side engine"), //
        NotSupported(1, "Not supported by chassis"), //
        IncompatibleUpgrades(100, "Current upgrades do not admit the item"), //
        NoComponentSupport(10, "No component can support the item"), //
        JumpJetCapacityReached(100, "Maximum number of jumpjets already installed"), //
        EngineAlreadyEquipped(100, "An engine is already equipped"), //
        NoFreeHardPoints(50, "No free hard points"), //
        ComponentAlreadyHasCase(20, "C.A.S.E. is already equipped"), //
        ModuleAlreadyEquipped(100, "That module is already equipped");

        private final int specificity;
        private final String message;

        Type(int aSpecificity, String aMessage) {
            specificity = aSpecificity;
            message = aMessage;
        }

        boolean isMoreSpecificThan(Type aType) {
            return specificity > aType.specificity;
        }

        @Override
        public String toString() {
            return message;
        }
    }

    static public final EquipResult                   SUCCESS;
    static private final Map<Type, List<EquipResult>> RESULTS;

    static {
        RESULTS = new HashMap<>();
        for (Type type : Type.values()) {
            List<EquipResult> list = new ArrayList<>();
            list.add(new EquipResult(type));
            for (Location location : Location.values()) {
                list.add(new EquipResult(location, type));
            }
            RESULTS.put(type, Collections.unmodifiableList(list));
        }
        SUCCESS = make(Type.Success);
    }

    private final Type     type;
    private final Location location;

    private EquipResult(Type aType) {
        this(null, aType);
    }

    private EquipResult(Location aLocation, Type aType) {
        location = aLocation;
        type = aType;
    }

    @Override
    public String getMessage() {
        return toString();
    }

    @Override
    public String toString() {
        if (location != null)
            return type.toString() + " on " + location.longName();
        return type.toString();
    }

    public boolean isMoreSpecificThan(EquipResult aResult) {
        return type.isMoreSpecificThan(aResult.type);
    }

    static public EquipResult make(Location aLocation, Type aType) {
        List<EquipResult> l = RESULTS.get(aType);
        for (EquipResult equipResult : l) {
            if (equipResult.location == aLocation) {
                return equipResult;
            }
        }
        throw new RuntimeException("Results map is missing values!");
    }

    static public EquipResult make(Type aType) {
        return make(null, aType);
    }

    /**
     * Checks if this result is a failure and throw if it is
     * 
     * @throws EquipResult
     *             Thrown if this is a failure result.
     */
    public void checkFailureAndThrow() throws EquipResult {
        if (this != SUCCESS)
            throw this;
    }
}
