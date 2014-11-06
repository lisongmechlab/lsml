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
package lisong_mechlab.model.item;

import lisong_mechlab.model.chassi.OmniPod;
import lisong_mechlab.model.pilot.PilotSkillTree;

/**
 * This interface provides a means to modify the characteristics of a set of weapons. Used by {@link OmniPod} quirks and
 * {@link WeaponModule}s.
 * 
 * @author Li Song
 */
public interface WeaponModifier {
    /**
     * @param aWeapon
     *            The {@link Weapon} to check if it should be affected.
     * @return <code>true</code> if this {@link WeaponModule} affects the given {@link Weapon}.
     */
    public boolean affectsWeapon(Weapon aWeapon);

    /**
     * Applies the effect of this modifier on the given attribute. Must only be called after
     * {@link #affectsWeapon(Weapon)} returns true for <code>aWeapon</code>.
     * 
     * @param aWeapon
     *            The weapon to apply the modifier to.
     * @param aRange
     *            The attribute to augment.
     * @param aPilotSkillTree
     *            The skills of the pilot, may affect the results.
     * @return The augmented value of the attribute (can be unchanged).
     */
    public double extraMaxRange(Weapon aWeapon, double aRange, PilotSkillTree aPilotSkillTree);

    /**
     * Applies the effect of this modifier on the given attribute. Must only be called after
     * {@link #affectsWeapon(Weapon)} returns true for <code>aWeapon</code>.
     * 
     * @param aWeapon
     *            The weapon to apply the modifier to.
     * @param aRange
     *            The attribute to augment.
     * @param aPilotSkillTree
     *            The skills of the pilot, may affect the results.
     * @return The augmented value of the attribute (can be unchanged).
     */
    public double extraLongRange(Weapon aWeapon, double aRange, PilotSkillTree aPilotSkillTree);

    /**
     * Applies the effect of this modifier on the given attribute. Must only be called after
     * {@link #affectsWeapon(Weapon)} returns true for <code>aWeapon</code>.
     * 
     * @param aWeapon
     *            The weapon to apply the modifier to.
     * @param aHeat
     *            The attribute to augment.
     * @param aPilotSkillTree
     *            The skills of the pilot, may affect the results.
     * @return The augmented value of the attribute (can be unchanged).
     */
    public double extraWeaponHeat(Weapon aWeapon, double aHeat, PilotSkillTree aPilotSkillTree);

    /**
     * Applies the effect of this modifier on the given attribute. Must only be called after
     * {@link #affectsWeapon(Weapon)} returns true for <code>aWeapon</code>.
     * 
     * @param aWeapon
     *            The weapon to apply the modifier to.
     * @param aCooldown
     *            The attribute to augment.
     * @param aPilotSkillTree
     *            The skills of the pilot, may affect the results.
     * @return The augmented value of the attribute (can be unchanged).
     */
    public double extraCooldown(Weapon aWeapon, double aCooldown, PilotSkillTree aPilotSkillTree);
    
    /**
     * Applies the effect of this modifier on the given attribute. Must only be called after
     * {@link #affectsWeapon(Weapon)} returns true for <code>aWeapon</code>.
     * 
     * @param aWeapon
     *            The weapon to apply the modifier to.
     * @param aDuration
     *            The attribute to augment.
     * @param aPilotSkillTree
     *            The skills of the pilot, may affect the results.
     * @return The augmented value of the attribute (can be unchanged).
     */
    public double extraDuration(Weapon aWeapon, double aDuration, PilotSkillTree aPilotSkillTree);
}
