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
package org.lisoft.lsml.model.datacache.gamedata.helpers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.datacache.ModifiersDB;
import org.lisoft.lsml.model.datacache.gamedata.Localization;
import org.lisoft.lsml.model.item.AmmoWeapon;
import org.lisoft.lsml.model.item.BallisticWeapon;
import org.lisoft.lsml.model.item.EnergyWeapon;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.MissileWeapon;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.modifiers.Attribute;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class ItemStatsWeapon extends ItemStats {

    public static class WeaponStatsTag extends ItemStatsModuleStats {
        @XStreamAsAttribute
        public double speed;
        @XStreamAsAttribute
        public double volleydelay;
        @XStreamAsAttribute
        public double duration;
        @XStreamAsAttribute
        public double maxRange;
        @XStreamAsAttribute
        public double longRange;
        @XStreamAsAttribute
        public double minRange;
        @XStreamAsAttribute
        public double nullRange;
        @XStreamAsAttribute
        public int    ammoPerShot;
        @XStreamAsAttribute
        public String ammoType;
        @XStreamAsAttribute
        public double cooldown;
        @XStreamAsAttribute
        public double heat;
        @XStreamAsAttribute
        public double impulse;
        @XStreamAsAttribute
        public double heatdamage;
        @XStreamAsAttribute
        public double damage;

        /** The number of ammunition rounds expelled in one shot. */
        @XStreamAsAttribute
        public int    numFiring;
        @XStreamAsAttribute
        public String projectileclass;
        @XStreamAsAttribute
        public String type;
        @XStreamAsAttribute
        public String artemisAmmoType;
        /** The number of projectile in one round of ammo. Fired simultaneously (only LB type AC). */
        @XStreamAsAttribute
        public int    numPerShot;
        @XStreamAsAttribute
        public int    minheatpenaltylevel;
        @XStreamAsAttribute
        public double heatpenalty;
        @XStreamAsAttribute
        public int    heatPenaltyID;
        @XStreamAsAttribute
        public double rof;
        @XStreamAsAttribute
        public double spread;
        @XStreamAsAttribute
        public double JammingChance;
        @XStreamAsAttribute
        public double JammedTime;
        @XStreamAsAttribute
        public int    ShotsDuringCooldown;
        @XStreamAsAttribute
        public double falloffexponent;
    }

    public static class ArtemisTag {
        @XStreamAsAttribute
        public int RestrictedTo;
    }

    @XStreamAsAttribute
    public int            InheritFrom;     // Special case handling of inherit from
    @XStreamAsAttribute
    public String         HardpointAliases;
    public WeaponStatsTag WeaponStats;
    public ArtemisTag     Artemis;

    private double determineCooldown() {
        if (WeaponStats.cooldown <= 0.0) {
            // Some weapons are troublesome in that they have zero cooldown in the data files.
            // These include: Machine Gun, Flamer, TAG
            if (WeaponStats.rof > 0.0) {
                return 1.0 / WeaponStats.rof;
            }
            else if (WeaponStats.type.toLowerCase().equals("energy")) {
                return 1.0;
            }
            else {
                return 0.10375; // Determined on testing grounds: 4000 mg rounds 6min 55s or 415s -> 415/4000 =
                // 0.10375
            }
        }
        return WeaponStats.cooldown;
    }

    public Weapon asWeapon(List<ItemStatsWeapon> aWeaponList) throws IOException {
        int baseType = -1;
        if (InheritFrom > 0) {
            baseType = InheritFrom;
            for (ItemStatsWeapon w : aWeaponList) {
                try {
                    if (Integer.parseInt(w.id) == InheritFrom) {
                        WeaponStats = w.WeaponStats;
                        if (Loc.descTag == null) {
                            Loc.descTag = w.Loc.descTag;
                        }
                        break;
                    }
                }
                catch (NumberFormatException e) {
                    continue;
                }
            }
            if (WeaponStats == null) {
                throw new IOException(
                        "Unable to find referenced item in \"inherit statement from clause\" for: " + name);
            }
        }

        double cooldownValue = determineCooldown();
        String uiName = Localization.key2string(Loc.nameTag);
        String uiDesc = Localization.key2string(Loc.descTag);
        String mwoName = name;
        int mwoId = Integer.parseInt(id);
        int slots = WeaponStats.slots;
        double mass = WeaponStats.tons;
        int hp = WeaponStats.health;
        Faction itemFaction = Faction.fromMwo(faction);

        double damagePerProjectile = WeaponStats.damage;

        double fallOffExponent = WeaponStats.falloffexponent != 0 ? WeaponStats.falloffexponent : 1.0;

        // There are three attributes that affect the projectile and ammo count.
        //
        int roundsPerShot = WeaponStats.numFiring;
        int projectilesPerRound = WeaponStats.numPerShot > 0 ? WeaponStats.numPerShot : 1;
        double projectileSpeed = WeaponStats.speed;

        int ghostHeatGroupId;
        double ghostHeatMultiplier;
        int ghostHeatFreeAlpha;
        if (WeaponStats.minheatpenaltylevel != 0) {
            ghostHeatGroupId = WeaponStats.heatPenaltyID;
            ghostHeatMultiplier = WeaponStats.heatpenalty;
            ghostHeatFreeAlpha = WeaponStats.minheatpenaltylevel - 1;
        }
        else {
            ghostHeatGroupId = -1;
            ghostHeatMultiplier = 0;
            ghostHeatFreeAlpha = -1;
        }
        
        final double spread;
        if (WeaponStats.spread > 0)
            spread = WeaponStats.spread;
        else
            spread = 0;

        List<String> selectors = Arrays.asList(HardpointAliases.toLowerCase().split(","));
        Attribute cooldown = new Attribute(cooldownValue, selectors, ModifiersDB.SEL_WEAPON_COOLDOWN);
        Attribute rangeZero = new Attribute(WeaponStats.nullRange, selectors, ModifiersDB.SEL_WEAPON_RANGE);
        Attribute rangeMin = new Attribute(WeaponStats.minRange, selectors, ModifiersDB.SEL_WEAPON_RANGE);
        Attribute rangeLong = new Attribute(WeaponStats.longRange, selectors, ModifiersDB.SEL_WEAPON_RANGE);
        Attribute rangeMax = new Attribute(WeaponStats.maxRange, selectors, ModifiersDB.SEL_WEAPON_RANGE);
        Attribute heat = new Attribute(WeaponStats.heat, selectors, ModifiersDB.SEL_WEAPON_HEAT);

        switch (HardPointType.fromMwoType(WeaponStats.type)) {
            case AMS:
                return new AmmoWeapon(
                        // Item Arguments
                        uiName, uiDesc, mwoName, mwoId, slots, mass, HardPointType.AMS, hp, itemFaction,
                        // HeatSource Arguments
                        heat,
                        // Weapon Arguments
                        cooldown, rangeZero, rangeMin, rangeLong, rangeMax, fallOffExponent, roundsPerShot,
                        damagePerProjectile, projectilesPerRound, projectileSpeed, ghostHeatGroupId,
                        ghostHeatMultiplier, ghostHeatFreeAlpha, WeaponStats.volleydelay, WeaponStats.impulse,
                        // AmmoWeapon Arguments
                        getAmmoType(), 0.0);
            case BALLISTIC:
                final double jammingChance;
                final int shotsDuringCooldown;
                final double jammingTime;
                if (WeaponStats.JammingChance >= 0) {
                    jammingChance = WeaponStats.JammingChance;
                    shotsDuringCooldown = WeaponStats.ShotsDuringCooldown;
                    jammingTime = WeaponStats.JammedTime;
                }
                else {
                    jammingChance = 0.0;
                    shotsDuringCooldown = 0;
                    jammingTime = 0.0;
                }

                Attribute jamChanceAttrib = new Attribute(jammingChance, selectors,
                        ModifiersDB.SEL_WEAPON_JAMMING_CHANCE);
                Attribute jamTimeAttrib = new Attribute(jammingTime, selectors, ModifiersDB.SEL_WEAPON_JAMMED_TIME);

                return new BallisticWeapon(
                        // Item Arguments
                        uiName, uiDesc, mwoName, mwoId, slots, mass, hp, itemFaction,
                        // HeatSource Arguments
                        heat,
                        // Weapon Arguments
                        cooldown, rangeZero, rangeMin, rangeLong, rangeMax, fallOffExponent, roundsPerShot,
                        damagePerProjectile, projectilesPerRound, projectileSpeed, ghostHeatGroupId,
                        ghostHeatMultiplier, ghostHeatFreeAlpha, WeaponStats.volleydelay, WeaponStats.impulse,
                        // AmmoWeapon Arguments
                        getAmmoType(), spread,
                        // BallisticWeapon Arguments
                        jamChanceAttrib, jamTimeAttrib, shotsDuringCooldown);
            case ENERGY:
                Attribute burntime = new Attribute(
                        (WeaponStats.duration < 0) ? Double.POSITIVE_INFINITY : WeaponStats.duration, selectors,
                        "duration");
                return new EnergyWeapon(
                        // Item Arguments
                        uiName, uiDesc, mwoName, mwoId, slots, mass, hp, itemFaction,
                        // HeatSource Arguments
                        heat,
                        // Weapon Arguments
                        cooldown, rangeZero, rangeMin, rangeLong, rangeMax, fallOffExponent, roundsPerShot,
                        damagePerProjectile, projectilesPerRound, projectileSpeed, ghostHeatGroupId,
                        ghostHeatMultiplier, ghostHeatFreeAlpha, WeaponStats.volleydelay, WeaponStats.impulse,
                        // EnergyWeapon Arguments
                        burntime);
            case MISSILE:
                final int requiredGuidance;
                if (null != Artemis)
                    requiredGuidance = Artemis.RestrictedTo;
                else
                    requiredGuidance = -1;

                int baseItemId = baseType == -1 ? (requiredGuidance != -1 ? mwoId : -1) : baseType;
                return new MissileWeapon(
                     // Item Arguments
                        uiName, uiDesc, mwoName, mwoId, slots, mass, hp, itemFaction,
                        // HeatSource Arguments
                        heat,
                        // Weapon Arguments
                        cooldown, rangeZero, rangeMin, rangeLong, rangeMax, fallOffExponent, roundsPerShot,
                        damagePerProjectile, projectilesPerRound, projectileSpeed, ghostHeatGroupId,
                        ghostHeatMultiplier, ghostHeatFreeAlpha, WeaponStats.volleydelay, WeaponStats.impulse,
                        // AmmoWeapon Arguments
                        getAmmoType(), spread,
                        // MissileWeapon Arguments
                        requiredGuidance, baseItemId);

            default:
                throw new IOException("Unknown value for type field in ItemStatsXML. Please update the program!");
        }
    }

    private String getAmmoType() {
        String regularAmmo = WeaponStats.ammoType;
        if (WeaponStats.artemisAmmoType == null)
            return regularAmmo;

        if (Artemis == null)
            return regularAmmo;

        if (Artemis.RestrictedTo == 3051) // No artemis
            return regularAmmo;
        return WeaponStats.artemisAmmoType;
    }
}
