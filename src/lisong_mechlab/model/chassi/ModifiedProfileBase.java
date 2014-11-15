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
package lisong_mechlab.model.chassi;

import java.util.Collection;

import lisong_mechlab.model.modifiers.Modifier;

/**
 * This {@link MovementProfile} provides an abstract base for a composite {@link MovementProfile} where the value of
 * each attribute is the result a function applied to a set of {@link MovementProfile}s.
 * 
 * @author Li Song
 */
public abstract class ModifiedProfileBase implements MovementProfile {

    /**
     * Uses reflection to calculate the sought for value.
     * 
     * @param aMethodName
     *            The name of the function to call to get the value.
     * @return The calculated value.
     */
    protected abstract double calc(String aMethodName);

    @Override
    public double getTorsoYawMax(Collection<Modifier> aModifiers) {
        return calc("getTorsoYawMax");
    }

    @Override
    public double getTorsoYawSpeed(Collection<Modifier> aModifiers) {
        return calc("getTorsoYawSpeed");
    }

    @Override
    public double getTorsoPitchMax(Collection<Modifier> aModifiers) {
        return calc("getTorsoPitchMax");
    }

    @Override
    public double getTorsoPitchSpeed(Collection<Modifier> aModifiers) {
        return calc("getTorsoPitchSpeed");
    }

    @Override
    public double getArmYawMax(Collection<Modifier> aModifiers) {
        return calc("getArmYawMax");
    }

    @Override
    public double getArmYawSpeed(Collection<Modifier> aModifiers) {
        return calc("getArmYawSpeed");
    }

    @Override
    public double getArmPitchMax(Collection<Modifier> aModifiers) {
        return calc("getArmPitchMax");
    }

    @Override
    public double getArmPitchSpeed(Collection<Modifier> aModifiers) {
        return calc("getArmPitchSpeed");
    }

    @Override
    public double getTurnLerpLowSpeed(Collection<Modifier> aModifiers) {
        return calc("getTurnLerpLowSpeed");
    }

    @Override
    public double getTurnLerpMidSpeed(Collection<Modifier> aModifiers) {
        return calc("getTurnLerpMidSpeed");
    }

    @Override
    public double getTurnLerpHighSpeed(Collection<Modifier> aModifiers) {
        return calc("getTurnLerpHighSpeed");
    }

    @Override
    public double getTurnLerpLowRate(Collection<Modifier> aModifiers) {
        return calc("getTurnLerpLowRate");
    }

    @Override
    public double getTurnLerpMidRate(Collection<Modifier> aModifiers) {
        return calc("getTurnLerpMidRate");
    }

    @Override
    public double getTurnLerpHighRate(Collection<Modifier> aModifiers) {
        return calc("getTurnLerpHighRate");
    }

}
