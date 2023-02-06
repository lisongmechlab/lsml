/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.mwo_data.mwo_parser;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.lisoft.lsml.mwo_data.equipment.*;
import org.lisoft.lsml.mwo_data.equipment.WeaponRangeProfile.RangeNode;
import org.lisoft.lsml.mwo_data.equipment.WeaponRangeProfile.RangeNode.InterpolationType;
import org.lisoft.lsml.mwo_data.mechs.HardPointType;
import org.lisoft.lsml.mwo_data.modifiers.Attribute;
import org.lisoft.lsml.mwo_data.modifiers.ModifierDescription;

class WeaponXML extends ModuleBaseXML {

  private static class ArtemisTag {
    @XStreamAsAttribute int RestrictedTo;
  }

  private static class WeaponStatsTag {
    @XStreamAsAttribute double JammedTime;
    @XStreamAsAttribute double JammingChance;
    @XStreamAsAttribute double RampDownDelay;
    @XStreamAsAttribute int ShotsDuringCooldown;
    @XStreamAsAttribute String ammoType;
    @XStreamAsAttribute String artemisAmmoType;
    @XStreamAsAttribute double chargeTime;
    @XStreamAsAttribute double cooldown;
    @XStreamAsAttribute double damage;
    @XStreamAsAttribute double duration;
    @XStreamAsAttribute double heat;
    @XStreamAsAttribute int heatPenaltyID;
    @XStreamAsAttribute double heatpenalty;
    @XStreamAsAttribute double impulse;
    @XStreamAsAttribute int isOneShot;
    @XStreamAsAttribute double jamRampDownTime;
    @XStreamAsAttribute double jamRampUpTime;
    @XStreamAsAttribute int minheatpenaltylevel;
    /** The number of ammunition rounds expelled in one shot. */
    @XStreamAsAttribute int numFiring;
    /** The number of projectile in one round of ammo. Fired simultaneously (only LB type AC). */
    @XStreamAsAttribute int numPerShot;

    @XStreamAsAttribute String projectileclass;
    @XStreamAsAttribute double rampDownTime;
    @XStreamAsAttribute double rampUpTime;
    @XStreamAsAttribute double rof;
    @XStreamAsAttribute double speed;
    @XStreamAsAttribute double spread;
    @XStreamAsAttribute String type;
    @XStreamAsAttribute double volleydelay;
    @XStreamAsAttribute int volleysize;
    @XStreamAsAttribute int slots;
    @XStreamAsAttribute double tons;

    @XStreamAlias("Health")
    @XStreamAsAttribute
    public double health;
  }

  ArtemisTag Artemis;
  @XStreamAsAttribute String HardpointAliases;
  @XStreamAsAttribute int InheritFrom; // Special case handling of inherit from
  List<Range> Ranges;
  WeaponStatsTag WeaponStats;

  public Optional<Weapon> asWeapon(List<WeaponXML> aWeaponList, PartialDatabase aPartialDatabase)
      throws IOException {
    if (!isUsable()) {
      return Optional.empty();
    }

    final int baseType = inheritFrom(aWeaponList);
    final int slots = WeaponStats.slots;
    final int roundsPerShot = WeaponStats.numFiring;
    // The data files have an attribute for ammoPerShot, but it doesn't really agree with what
    // happens in game
    // luckily for us, roundsPerShot agrees 1:1 with the actual amount of ammo consumed for all
    // weapons as of
    // 2022-08-12, so we use that instead here.
    final int projectilesPerRound = WeaponStats.numPerShot > 0 ? WeaponStats.numPerShot : 1;
    final int volleySize = WeaponStats.volleysize > 0 ? WeaponStats.volleysize : roundsPerShot;
    final double damagePerProjectile = determineDamage();
    final double coolDownValue = determineCooldown();
    final double mass = WeaponStats.tons;
    final double hp = WeaponStats.health;
    final boolean isOneShot = WeaponStats.isOneShot != 0;
    // For weapons with coolDown=0, the heat is per second. Convert to per shot as LSML expects.
    final double heatPerShot = WeaponStats.heat * (WeaponStats.cooldown <= 0 ? coolDownValue : 1);
    final List<String> selectors = computeSelectors(getMwoKey());
    final Attribute spread = computeSpreadAttribute(selectors);
    final Attribute heat =
        new Attribute(heatPerShot, selectors, ModifierDescription.SPEC_WEAPON_HEAT);

    int ghostHeatGroupId;
    double ghostHeatMultiplier;
    final Attribute ghostHeatFreeAlpha;
    if (WeaponStats.minheatpenaltylevel != 0) {
      ghostHeatGroupId = WeaponStats.heatPenaltyID;
      ghostHeatMultiplier = WeaponStats.heatpenalty;
      ghostHeatFreeAlpha =
          new Attribute(
              WeaponStats.minheatpenaltylevel - 1,
              selectors,
              ModifierDescription.SPEC_WEAPON_MAX_FREE_ALPHA);
    } else {
      ghostHeatGroupId = -1;
      ghostHeatMultiplier = 0;
      ghostHeatFreeAlpha =
          new Attribute(-1, selectors, ModifierDescription.SPEC_WEAPON_MAX_FREE_ALPHA);
    }

    final List<RangeNode> rangeNodes =
        Ranges.stream()
            .map(
                r ->
                    new RangeNode(
                        new Attribute(r.start, selectors, ModifierDescription.SPEC_WEAPON_RANGE),
                        InterpolationType.fromMwo(r.interpolationToNextRange),
                        r.damageModifier,
                        r.exponent))
            .collect(Collectors.toList());

    final Attribute projectileSpeed =
        new Attribute(computeSpeed(), selectors, ModifierDescription.SPEC_WEAPON_PROJECTILE_SPEED);
    final Attribute coolDown =
        new Attribute(coolDownValue, selectors, ModifierDescription.SPEC_WEAPON_COOL_DOWN);
    final WeaponRangeProfile rangeProfile = new WeaponRangeProfile(spread, rangeNodes);

    switch (HardPointType.fromMwoType(WeaponStats.type)) {
      case AMS:
        return Optional.of(
            new AmmoWeapon(
                // Item Arguments
                getUiName(aPartialDatabase),
                getUiDescription(aPartialDatabase),
                getMwoKey(),
                getMwoId(),
                slots,
                mass,
                HardPointType.AMS,
                hp,
                getFaction(),
                // HeatSource Arguments
                heat,
                // Weapon Arguments
                coolDown,
                rangeProfile,
                roundsPerShot,
                volleySize,
                damagePerProjectile,
                projectilesPerRound,
                projectileSpeed,
                ghostHeatGroupId,
                ghostHeatMultiplier,
                ghostHeatFreeAlpha,
                WeaponStats.volleydelay,
                WeaponStats.impulse,
                // AmmoWeapon Arguments
                getAmmoType(),
                isOneShot,
                roundsPerShot));
      case BALLISTIC:
        final Attribute jamChanceAttrib =
            new Attribute(
                WeaponStats.JammingChance,
                selectors,
                ModifierDescription.SPEC_WEAPON_JAM_PROBABILITY);
        final Attribute jamTimeAttrib =
            new Attribute(
                WeaponStats.JammedTime, selectors, ModifierDescription.SPEC_WEAPON_JAM_DURATION);
        final Attribute jamRampDownTime =
            new Attribute(
                WeaponStats.jamRampDownTime,
                selectors,
                ModifierDescription.SPEC_WEAPON_JAM_RAMP_DOWN_TIME);

        return Optional.of(
            new BallisticWeapon(
                // Item Arguments
                getUiName(aPartialDatabase),
                getUiDescription(aPartialDatabase),
                getMwoKey(),
                getMwoId(),
                slots,
                mass,
                hp,
                getFaction(),
                // HeatSource Arguments
                heat,
                // Weapon Arguments
                coolDown,
                rangeProfile,
                roundsPerShot,
                damagePerProjectile,
                projectilesPerRound,
                projectileSpeed,
                ghostHeatGroupId,
                ghostHeatMultiplier,
                ghostHeatFreeAlpha,
                WeaponStats.volleydelay,
                WeaponStats.impulse,
                // AmmoWeapon Arguments
                getAmmoType(),
                isOneShot,
                roundsPerShot,
                // BallisticWeapon Arguments
                jamChanceAttrib,
                jamTimeAttrib,
                WeaponStats.ShotsDuringCooldown,
                WeaponStats.chargeTime,
                WeaponStats.rampUpTime,
                WeaponStats.rampDownTime,
                WeaponStats.RampDownDelay,
                WeaponStats.jamRampUpTime,
                jamRampDownTime));
      case ENERGY:
        final Attribute burnTime =
            new Attribute(
                WeaponStats.duration < 0 ? Double.POSITIVE_INFINITY : WeaponStats.duration,
                selectors,
                ModifierDescription.SPEC_WEAPON_DURATION);
        return Optional.of(
            new EnergyWeapon(
                // Item Arguments
                getUiName(aPartialDatabase),
                getUiDescription(aPartialDatabase),
                getMwoKey(),
                getMwoId(),
                slots,
                mass,
                hp,
                getFaction(),
                // HeatSource Arguments
                heat,
                // Weapon Arguments
                coolDown,
                rangeProfile,
                roundsPerShot,
                damagePerProjectile,
                projectilesPerRound,
                projectileSpeed,
                ghostHeatGroupId,
                ghostHeatMultiplier,
                ghostHeatFreeAlpha,
                WeaponStats.volleydelay,
                WeaponStats.impulse,
                // EnergyWeapon Arguments
                burnTime));
      case MISSILE:
        final int requiredGuidance;
        if (null != Artemis) {
          requiredGuidance = Artemis.RestrictedTo;
        } else {
          requiredGuidance = -1;
        }

        final int baseItemId = baseType == -1 ? requiredGuidance != -1 ? getMwoId() : -1 : baseType;
        return Optional.of(
            new MissileWeapon(
                // Item Arguments
                getUiName(aPartialDatabase),
                getUiDescription(aPartialDatabase),
                getMwoKey(),
                getMwoId(),
                slots,
                mass,
                hp,
                getFaction(),
                // HeatSource Arguments
                heat,
                // Weapon Arguments
                coolDown,
                rangeProfile,
                roundsPerShot,
                volleySize,
                damagePerProjectile,
                projectilesPerRound,
                projectileSpeed,
                ghostHeatGroupId,
                ghostHeatMultiplier,
                ghostHeatFreeAlpha,
                WeaponStats.volleydelay,
                WeaponStats.impulse,
                // AmmoWeapon Arguments
                getAmmoType(),
                isOneShot,
                roundsPerShot,
                // MissileWeapon Arguments
                requiredGuidance,
                baseItemId));
      case ECM: // Fall through, not a weapon
      case NONE: // Fall through, not a weapon
      default:
        throw new IOException(
            "Unknown value for type field in ItemStatsXML. Please update the program!");
    }
  }

  private boolean isUsable() {
    // Stupid dropshiplargepulselaser and testing machinegun screwing stuff up
    return getMwoId() != 1998 && getMwoId() != 1999;
  }

  private List<String> computeSelectors(final String mwoName) {
    final List<String> selectors =
        new ArrayList<>(Arrays.asList(HardpointAliases.toLowerCase().split(",")));
    selectors.add(QuirkModifiers.SPECIFIC_ITEM_PREFIX + mwoName.toLowerCase());
    return selectors;
  }

  private double computeSpeed() {
    return WeaponStats.speed == 0 ? Double.POSITIVE_INFINITY : WeaponStats.speed;
  }

  private Attribute computeSpreadAttribute(final List<String> selectors) {
    final Attribute spread;
    // For now, don't use the spread attribute on javelin type weapons #691.
    if (WeaponStats.spread > 0 && !"javelin".equalsIgnoreCase(WeaponStats.projectileclass)) {
      spread = new Attribute(WeaponStats.spread, selectors, ModifierDescription.SPEC_WEAPON_SPREAD);
    } else {
      spread = null;
    }
    return spread;
  }

  private double determineCooldown() {
    if (WeaponStats.cooldown <= 0.0) {
      // All weapons have a cooldown, some of them are zero. If it's zero there's usually a rate of
      // fire
      // attribute "rof". But when that's not present, damage and heat is per second.
      if (WeaponStats.rof > 0.0) {
        return 1.0 / WeaponStats.rof;
      } else {
        return 0.10;
      }
    }
    return WeaponStats.cooldown;
  }

  private double determineDamage() {
    if (WeaponStats.cooldown <= 0.0 && WeaponStats.rof <= 0.0) {
      // Flamers and TAG have damage per second, normalize this
      return WeaponStats.damage * determineCooldown();
    }
    return WeaponStats.damage;
  }

  private String getAmmoType() {
    final String regularAmmo = WeaponStats.ammoType;
    if (WeaponStats.artemisAmmoType == null) {
      return regularAmmo;
    }

    if (Artemis == null) {
      return regularAmmo;
    }

    if (Artemis.RestrictedTo == 3051) {
      return regularAmmo;
    }
    return WeaponStats.artemisAmmoType;
  }

  private int inheritFrom(List<WeaponXML> aWeaponList) throws IOException {
    int baseType = -1;
    if (InheritFrom > 0) {
      baseType = InheritFrom;
      for (final WeaponXML weapon : aWeaponList) {
        try {
          if (weapon.getMwoId() == InheritFrom) {
            WeaponStats = weapon.WeaponStats;
            Ranges = weapon.Ranges;
            super.inheritFrom(weapon);
            break;
          }
        } catch (final NumberFormatException e) {
          // Eat exceptions
        }
      }
      if (WeaponStats == null) {
        throw new IOException(
            "Unable to find referenced item in \"inherit statement from clause\" for: "
                + getMwoKey());
      }
    }
    return baseType;
  }
}
