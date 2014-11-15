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

import java.util.Comparator;

import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.modifiers.Attribute;
import lisong_mechlab.model.upgrades.GuidanceUpgrade;
import lisong_mechlab.model.upgrades.Upgrade;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.model.upgrades.Upgrades;

public class MissileWeapon extends AmmoWeapon {
    private final int requiredGuidanceType;
    private final int baseItemId;

    public MissileWeapon(String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons, int aHP,
            Faction aFaction, Attribute aHeat, Attribute aCooldown, Attribute aRangeZero, Attribute aRangeMin,
            Attribute aRangeLong, Attribute aRangeMax, double aFallOffExponent, int aRoundsPerShot,
            double aDamagePerProjectile, int aProjectilesPerRound, double aProjectileSpeed, int aGhostHeatGroupId,
            double aGhostHeatMultiplier, int aGhostHeatMaxFreeAlpha, String aAmmoType, int aRequiredGuidanceId,
            int aBaseItemId) {
        super(aName, aDesc, aMwoName, aMwoId, aSlots, aTons, HardPointType.MISSILE, aHP, aFaction, aHeat, aCooldown,
                aRangeZero, aRangeMin, aRangeLong, aRangeMax, aFallOffExponent, aRoundsPerShot, aDamagePerProjectile,
                aProjectilesPerRound, aProjectileSpeed, aGhostHeatGroupId, aGhostHeatMultiplier,
                aGhostHeatMaxFreeAlpha, aAmmoType);
        requiredGuidanceType = aRequiredGuidanceId;
        baseItemId = aBaseItemId;
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

    public final static Comparator<Item> DEFAULT_ORDERING = DEFAULT_WEAPON_ORDERING; // XXX: Should this really be here?

    /**
     * @return If this weapon requires a specific upgrade, this will return that upgrade, otherwise returns
     *         <code>null</code>.
     */
    public Upgrade getRequiredUpgrade() {
        return UpgradeDB.lookup(requiredGuidanceType);
    }
}
