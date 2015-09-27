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

import java.util.Collection;

import org.lisoft.lsml.model.modifiers.Modifier;

/**
 * This {@link MovementProfile} provides an abstract base for a composite {@link MovementProfile} where the value of
 * each attribute is the result a function applied to a set of {@link MovementProfile}s.
 * 
 * @author Emily Björk
 */
public abstract class ModifiedProfileBase implements MovementProfile {

    /**
     * Uses reflection to calculate the sought for value.
     * 
     * @param aMethodName
     *            The name of the function to call to get the value.
     * @param aExtraModifiers
     *            Modifiers that shall be applied in addition to the base ones.
     * @return The calculated value.
     */
    protected abstract double calc(String aMethodName, Collection<Modifier> aExtraModifiers);

    @Override
    public double getMaxMovementSpeed(Collection<Modifier> aModifiers) {
        return calc("getMaxMovementSpeed", aModifiers);
    }

    @Override
    public double getReverseSpeedMultiplier(Collection<Modifier> aModifiers) {
        return calc("getReverseSpeedMultiplier", aModifiers);
    }

    @Override
    public double getTorsoYawMax(Collection<Modifier> aModifiers) {
        return calc("getTorsoYawMax", aModifiers);
    }

    @Override
    public double getTorsoYawSpeed(Collection<Modifier> aModifiers) {
        return calc("getTorsoYawSpeed", aModifiers);
    }

    @Override
    public double getTorsoPitchMax(Collection<Modifier> aModifiers) {
        return calc("getTorsoPitchMax", aModifiers);
    }

    @Override
    public double getTorsoPitchSpeed(Collection<Modifier> aModifiers) {
        return calc("getTorsoPitchSpeed", aModifiers);
    }

    @Override
    public double getArmYawMax(Collection<Modifier> aModifiers) {
        return calc("getArmYawMax", aModifiers);
    }

    @Override
    public double getArmYawSpeed(Collection<Modifier> aModifiers) {
        return calc("getArmYawSpeed", aModifiers);
    }

    @Override
    public double getArmPitchMax(Collection<Modifier> aModifiers) {
        return calc("getArmPitchMax", aModifiers);
    }

    @Override
    public double getArmPitchSpeed(Collection<Modifier> aModifiers) {
        return calc("getArmPitchSpeed", aModifiers);
    }

    @Override
    public double getTurnLerpLowSpeed(Collection<Modifier> aModifiers) {
        return calc("getTurnLerpLowSpeed", aModifiers);
    }

    @Override
    public double getTurnLerpMidSpeed(Collection<Modifier> aModifiers) {
        return calc("getTurnLerpMidSpeed", aModifiers);
    }

    @Override
    public double getTurnLerpHighSpeed(Collection<Modifier> aModifiers) {
        return calc("getTurnLerpHighSpeed", aModifiers);
    }

    @Override
    public double getTurnLerpLowRate(Collection<Modifier> aModifiers) {
        return calc("getTurnLerpLowRate", aModifiers);
    }

    @Override
    public double getTurnLerpMidRate(Collection<Modifier> aModifiers) {
        return calc("getTurnLerpMidRate", aModifiers);
    }

    @Override
    public double getTurnLerpHighRate(Collection<Modifier> aModifiers) {
        return calc("getTurnLerpHighRate", aModifiers);
    }

}
