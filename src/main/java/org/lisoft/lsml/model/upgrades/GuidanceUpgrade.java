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
package org.lisoft.lsml.model.upgrades;

import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.MissileWeapon;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class models a guidance upgrade.
 *
 * @author Emily Björk
 */
public class GuidanceUpgrade extends Upgrade {
    @XStreamAsAttribute
    final private int slots;
    @XStreamAsAttribute
    final private double tons;
    @XStreamAsAttribute
    final private double spreadFactor;

    public GuidanceUpgrade(String aUiName, String aUiDesc, String aMwoName, int aMwoId, Faction aFaction, int aSlots,
            double aTons, double aSpreadFactor) {
        super(aUiName, aUiDesc, aMwoName, aMwoId, aFaction);
        slots = aSlots;
        tons = aTons;
        spreadFactor = aSpreadFactor;
    }

    /**
     * Calculates how many extra slots are needed for the given {@link ConfiguredComponent} for the given upgrade.
     *
     * @param aLoadoutPart
     *            The {@link ConfiguredComponent} to calculate for.
     * @return A number of slots needed.
     */
    public int getExtraSlots(ConfiguredComponent aLoadoutPart) {
        int ans = 0;
        for (final Item item : aLoadoutPart.getItemsFixed()) {
            if (item instanceof MissileWeapon) {
                final MissileWeapon weapon = (MissileWeapon) item;
                if (weapon.isArtemisCapable()) {
                    ans += slots;
                }
            }
        }
        for (final Item item : aLoadoutPart.getItemsEquipped()) {
            if (item instanceof MissileWeapon) {
                final MissileWeapon weapon = (MissileWeapon) item;
                if (weapon.isArtemisCapable()) {
                    ans += slots;
                }
            }
        }
        return ans;
    }

    /**
     * Calculates how many extra slots are needed in total for the given upgrade.
     *
     * @param aLoadout
     *            The loadout to calculate for.
     * @return A number of slots needed.
     */
    @Override
    public int getTotalSlots(Loadout aLoadout) {
        int ans = 0;
        for (final ConfiguredComponent part : aLoadout.getComponents()) {
            ans += getExtraSlots(part);
        }
        return ans;
    }

    /**
     * Calculates how many extra tons are needed for the given {@link ConfiguredComponent} for the given upgrade.
     *
     * @param aLoadoutPart
     *            The {@link ConfiguredComponent} to calculate for.
     * @return A number of tons needed.
     */
    public double getExtraTons(ConfiguredComponent aLoadoutPart) {
        double ans = 0;
        for (final Item item : aLoadoutPart.getItemsEquipped()) {
            if (item instanceof MissileWeapon) {
                final MissileWeapon weapon = (MissileWeapon) item;
                if (weapon.isArtemisCapable()) {
                    ans += tons;
                }
            }
        }
        for (final Item item : aLoadoutPart.getItemsFixed()) {
            if (item instanceof MissileWeapon) {
                final MissileWeapon weapon = (MissileWeapon) item;
                if (weapon.isArtemisCapable()) {
                    ans += tons;
                }
            }
        }
        return ans;
    }

    /**
     * Calculates how many extra tons are needed in total for the given upgrade.
     *
     * @param aLoadout
     *            The {@link LoadoutStandard} to calculate for.
     * @return A number of tons needed.
     */
    @Override
    public double getTotalTons(Loadout aLoadout) {
        double ans = 0;
        for (final ConfiguredComponent part : aLoadout.getComponents()) {
            ans += getExtraTons(part);
        }
        return ans;
    }

    public int getSlots() {
        return slots;
    }

    /**
     * @return The spread factor for this guidance upgrade.
     */
    public double getSpreadFactor() {
        return spreadFactor;
    }

    public double getTons() {
        return tons;
    }

    @Override
    public UpgradeType getType() {
        return UpgradeType.ARTEMIS;
    }

    /**
     * Upgrades a {@link Ammunition} to match this guidance type.
     *
     * @param aOldAmmo
     *            The {@link Ammunition} to upgrade.
     * @return An {@link Ammunition} object of the appropriate type for this guidance.
     */
    public Ammunition upgrade(Ammunition aOldAmmo) {
        if (aOldAmmo.getWeaponHardpointType() != HardPointType.MISSILE) {
            return aOldAmmo;
        }

        for (final MissileWeapon weapon : ItemDB.lookup(MissileWeapon.class)) {
            if (weapon.isCompatibleAmmo(aOldAmmo)) {
                final MissileWeapon representant = upgrade(weapon);

                for (final Ammunition ammunition : ItemDB.lookup(Ammunition.class)) {
                    if (representant.isCompatibleAmmo(ammunition) && ammunition.getMass() == aOldAmmo.getMass()) {
                        return ammunition;
                    }
                }
                break;
            }
        }

        throw new RuntimeException("Unable to find upgraded version of: " + aOldAmmo);
    }

    /**
     * Upgrades a {@link MissileWeapon} to match this guidance type.
     *
     * @param aOldWeapon
     *            The {@link MissileWeapon} to upgrade.
     * @return A {@link MissileWeapon} which is an appropriate variant for this guidance type.
     */
    public MissileWeapon upgrade(MissileWeapon aOldWeapon) {
        final int baseVariant = aOldWeapon.getBaseVariant();
        if (baseVariant <= 0) {
            return aOldWeapon;
        }

        for (final MissileWeapon weapon : ItemDB.lookup(MissileWeapon.class)) {
            if (weapon.getBaseVariant() == baseVariant && weapon.getRequiredUpgrade() == this) {
                return weapon;
            }
        }
        throw new RuntimeException("Unable to find upgraded version of: " + baseVariant);
    }
}
