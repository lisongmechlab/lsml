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
package org.lisoft.lsml.model.item;

import java.util.Collection;

import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.upgrades.GuidanceUpgrade;
import org.lisoft.lsml.model.upgrades.Upgrade;
import org.lisoft.lsml.model.upgrades.Upgrades;

public class MissileWeapon extends AmmoWeapon {
    private final int requiredGuidanceType;
    private final int baseItemId;

    public MissileWeapon(
            // Item Arguments
            String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons, int aHP,
            Faction aFaction,
            // HeatSource Arguments
            Attribute aHeat,
            // Weapon Arguments
            Attribute aCooldown, Attribute aRangeZero, Attribute aRangeMin, Attribute aRangeLong, Attribute aRangeMax,
            double aFallOffExponent, int aRoundsPerShot, double aDamagePerProjectile, int aProjectilesPerRound,
            double aProjectileSpeed, int aGhostHeatGroupId, double aGhostHeatMultiplier, int aGhostHeatMaxFreeAlpha,
            double aVolleyDelay, double aImpulse,
            // AmmoWeapon Arguments
            String aAmmoType, Attribute aSpread,
            // MissileWeapon Arguments
            int aRequiredGuidanceId, int aBaseItemId) {
        super(// Item Arguments
                aName, aDesc, aMwoName, aMwoId, aSlots, aTons, HardPointType.MISSILE, aHP, aFaction,
                // HeatSource Arguments
                aHeat,
                // Weapon Arguments
                aCooldown, aRangeZero, aRangeMin, aRangeLong, aRangeMax, aFallOffExponent, aRoundsPerShot,
                aDamagePerProjectile, aProjectilesPerRound, aProjectileSpeed, aGhostHeatGroupId, aGhostHeatMultiplier,
                aGhostHeatMaxFreeAlpha, aVolleyDelay, aImpulse,
                // AmmoWeapon Arguments
                aAmmoType, aSpread);
        requiredGuidanceType = aRequiredGuidanceId;
        baseItemId = aBaseItemId;
    }

    @Override
    public double getSecondsPerShot(Collection<Modifier> aModifiers) {
        if (getFaction() == Faction.INNERSPHERE || getAliases().contains("srm") || getAliases().contains("streaksrm")) {
            // Implicit assumption that:
            // 1) All missiles can launch simultaneously for IS LRM launchers.
            // 2) All missiles can launch simultaneously for IS + Clan (S)SRM launchers.
            return getCoolDown(aModifiers);
        }
        return super.getSecondsPerShot(aModifiers);
    }

    @Override
    public boolean isCompatible(Upgrades aUpgrades) {
        if (isArtemisCapable()) {
            return aUpgrades.getGuidance().getMwoId() == requiredGuidanceType;
        }
        return super.isCompatible(aUpgrades);
    }

    @Override
    public int getNumCriticalSlots() {
        if (isArtemisCapable()) {
            return super.getNumCriticalSlots() + ((GuidanceUpgrade) UpgradeDB.lookup(requiredGuidanceType)).getSlots();
        }
        return super.getNumCriticalSlots();
    }

    @Override
    public double getMass() {
        if (isArtemisCapable()) {
            return super.getMass() + ((GuidanceUpgrade) UpgradeDB.lookup(requiredGuidanceType)).getTons();
        }
        return super.getMass();
    }

    public boolean isArtemisCapable() {
        return requiredGuidanceType != -1;
    }

    public MissileWeapon getBaseVariant() {
        if (baseItemId <= 0) {
            return null;
        }
        return (MissileWeapon) ItemDB.lookup(baseItemId);
    }

    @Override
    public boolean hasSpread() {
        return super.hasSpread() && getAliases().contains("srm");
    }

    /**
     * @return If this weapon requires a specific upgrade, this will return that upgrade, otherwise returns
     *         <code>null</code>.
     */
    public Upgrade getRequiredUpgrade() {
        return UpgradeDB.lookup(requiredGuidanceType);
    }

    @Override
    public double getSpread(Collection<Modifier> aModifiers) {
        if (requiredGuidanceType == UpgradeDB.ARTEMIS_IV.getMwoId()) {
            return super.getSpread(aModifiers) * UpgradeDB.ARTEMIS_IV.getSpreadFactor();
        }
        return super.getSpread(aModifiers);
    }
}
