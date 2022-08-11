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
package org.lisoft.lsml.model.item;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.Modifier;

import java.util.Collection;

/**
 * Base class for weapons that consume ammunition.
 *
 * @author Li Song
 */
public class AmmoWeapon extends Weapon {
    /**
     * This field will be set through reflection in a post-processing pass.
     */
    @XStreamAsAttribute
    private final Ammunition ammoHalfType = null;
    /**
     * This field will be set through reflection in a post-processing pass.
     */
    @XStreamAsAttribute
    private final Ammunition ammoType = null;
    @XStreamAsAttribute
    private final String ammoTypeId;
    @XStreamAsAttribute
    private final int volleySize;
    @XStreamAsAttribute
    private final int ammoPerShot;
    @XStreamAsAttribute
    private final boolean oneShot;    
    @XStreamAsAttribute
    private final double firingDelay;
    
    public AmmoWeapon(
            // Item Arguments
            String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons,
            HardPointType aHardPointType, double aHP, Faction aFaction,
            // HeatSource Arguments
            Attribute aHeat,
            // Weapon Arguments
            Attribute aCoolDown, WeaponRangeProfile aRangeProfile, int aRoundsPerShot, int aVolleySize, double aDamagePerProjectile,
            int aProjectilesPerRound, Attribute aProjectileSpeed, int aGhostHeatGroupId, double aGhostHeatMultiplier,
            Attribute aGhostHeatMaxFreeAlpha, double aVolleyDelay, double aImpulse,
            // AmmoWeapon Arguments
            String aAmmoType, boolean aOneShot, int aAmmoPerShot) {
        super(aName, aDesc, aMwoName, aMwoId, aSlots, aTons, aHardPointType, aHP, aFaction, aHeat, aCoolDown,
              aRangeProfile, aRoundsPerShot, aDamagePerProjectile, aProjectilesPerRound, aProjectileSpeed,
              aGhostHeatGroupId, aGhostHeatMultiplier, aGhostHeatMaxFreeAlpha, aVolleyDelay, aImpulse);
        ammoTypeId = aAmmoType;
        volleySize = aVolleySize;  
        ammoPerShot = aAmmoPerShot;
        oneShot = aOneShot;
                
        double numVolleys = Math.ceil((double) aRoundsPerShot / (double) aVolleySize); 
        firingDelay = (numVolleys - 1) * aVolleyDelay;
    }

    public Ammunition getAmmoHalfType() {
        return ammoHalfType;
    }

    /**
     * @return The {@link String} name of the ammo type required for this weapon.
     */
    public String getAmmoId() {
        return ammoTypeId;
    }

    public Ammunition getAmmoType() {
        return ammoType;
    }

    public int getVolleySize() {
        return volleySize;
    }
    
    public int getAmmoPerShot() {
        return ammoPerShot;
    }
    
    public int getBuiltInRounds() {
        return hasBuiltInAmmo() ? super.getRoundsPerShot(): 0;
    }

    @Override
    public double getCoolDown(Collection<Modifier> aModifiers) {
        return isOneShot() ? Double.POSITIVE_INFINITY : super.getCoolDown(aModifiers);
    }

    public double getFiringDelay() {
        return firingDelay;
    }
    
    /**
    * {@inheritDoc}
    *
    * Also accounts for volley size of weapons that have such limitations.
    * 
    * @param aModifiers {@inheritDoc}
    * @return {@inheritDoc}
    **/

    @Override
    public double getRawFiringPeriod(Collection<Modifier> aModifiers) {
        double firingDelay = getFiringDelay();
        double cooldown = getCoolDown(aModifiers); 

        return isOneShot() ? Double.POSITIVE_INFINITY : firingDelay + cooldown;
   }

    /**
     * @return <code>true</code> if the weapon has builtin ammo.
     */
    public boolean hasBuiltInAmmo() {
        return null == ammoTypeId;
    }

    public boolean isCompatibleAmmo(Ammunition aAmmunition) {
        if (hasBuiltInAmmo()) {
            return false;
        }
        return ammoTypeId.equals(aAmmunition.getAmmoId());
    }

    public boolean isOneShot() {
        return oneShot;
    }
}
