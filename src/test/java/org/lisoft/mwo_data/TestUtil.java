/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2023  Li Song
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
package org.lisoft.mwo_data;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.lisoft.mwo_data.equipment.BallisticWeapon;
import org.lisoft.mwo_data.equipment.EnergyWeapon;
import org.lisoft.mwo_data.equipment.WeaponRangeProfile;
import org.lisoft.mwo_data.modifiers.Attribute;
import org.lisoft.mwo_data.modifiers.ModifierDescription;

public class TestUtil {

  public static BallisticWeapon makeAutoCannon(
      int aSlots,
      double aTons,
      double aHeat,
      double aCoolDown,
      double aLongRange,
      double aMaxRange,
      double aJamChance,
      double aJamTime,
      int aShotsDuringCoolDown,
      Collection<String> aSelectors) {
    Attribute heat = new Attribute(aHeat, aSelectors, ModifierDescription.SPEC_WEAPON_HEAT);
    Attribute coolDown =
        new Attribute(aCoolDown, aSelectors, ModifierDescription.SPEC_WEAPON_COOL_DOWN);
    Attribute projectileSpeed =
        new Attribute(
            Double.POSITIVE_INFINITY, aSelectors, ModifierDescription.SPEC_WEAPON_PROJECTILE_SPEED);
    Attribute freeAlpha =
        new Attribute(-1, aSelectors, ModifierDescription.SPEC_WEAPON_MAX_FREE_ALPHA);

    Attribute nearRange = new Attribute(0, aSelectors, ModifierDescription.SPEC_WEAPON_RANGE);
    Attribute longRange =
        new Attribute(aLongRange, aSelectors, ModifierDescription.SPEC_WEAPON_RANGE);
    Attribute maxRange =
        new Attribute(aMaxRange, aSelectors, ModifierDescription.SPEC_WEAPON_RANGE);

    Attribute jamChance =
        new Attribute(aJamChance, aSelectors, ModifierDescription.SPEC_WEAPON_JAM_PROBABILITY);
    Attribute jamTime =
        new Attribute(aJamTime, aSelectors, ModifierDescription.SPEC_WEAPON_JAM_DURATION);
    Attribute jamRampDownTime =
        new Attribute(0, aSelectors, ModifierDescription.SPEC_WEAPON_JAM_RAMP_DOWN_TIME);

    List<WeaponRangeProfile.RangeNode> nodes =
        Arrays.asList(
            new WeaponRangeProfile.RangeNode(
                nearRange, WeaponRangeProfile.RangeNode.InterpolationType.LINEAR, 1.0),
            new WeaponRangeProfile.RangeNode(
                longRange, WeaponRangeProfile.RangeNode.InterpolationType.LINEAR, 1.0),
            new WeaponRangeProfile.RangeNode(
                maxRange, WeaponRangeProfile.RangeNode.InterpolationType.LINEAR, 0));
    WeaponRangeProfile rangeProfile = new WeaponRangeProfile(null, nodes);

    return new BallisticWeapon(
        "",
        "",
        "",
        0,
        aSlots,
        aTons,
        10.0,
        Faction.INNERSPHERE,
        heat,
        coolDown,
        rangeProfile,
        1,
        1,
        1,
        projectileSpeed,
        -1,
        0,
        freeAlpha,
        0,
        0,
        "ammoType",
        false,
        1,
        jamChance,
        jamTime,
        aShotsDuringCoolDown,
        0,
        0,
        0,
        0,
        0,
        jamRampDownTime);
  }

  public static EnergyWeapon makeLaser(
      int aSlots,
      double aTons,
      double aHeat,
      double aCoolDown,
      double aBurnTime,
      double aLongRange,
      double aMaxRange,
      Collection<String> aSelectors) {
    Attribute heat = new Attribute(aHeat, aSelectors, ModifierDescription.SPEC_WEAPON_HEAT);
    Attribute coolDown =
        new Attribute(aCoolDown, aSelectors, ModifierDescription.SPEC_WEAPON_COOL_DOWN);
    Attribute projectileSpeed =
        new Attribute(
            Double.POSITIVE_INFINITY, aSelectors, ModifierDescription.SPEC_WEAPON_PROJECTILE_SPEED);
    Attribute freeAlpha =
        new Attribute(-1, aSelectors, ModifierDescription.SPEC_WEAPON_MAX_FREE_ALPHA);
    Attribute burnTime =
        new Attribute(aBurnTime, aSelectors, ModifierDescription.SPEC_WEAPON_DURATION);

    Attribute nearRange = new Attribute(0, aSelectors, ModifierDescription.SPEC_WEAPON_RANGE);
    Attribute longRange =
        new Attribute(aLongRange, aSelectors, ModifierDescription.SPEC_WEAPON_RANGE);
    Attribute maxRange =
        new Attribute(aMaxRange, aSelectors, ModifierDescription.SPEC_WEAPON_RANGE);

    List<WeaponRangeProfile.RangeNode> nodes =
        Arrays.asList(
            new WeaponRangeProfile.RangeNode(
                nearRange, WeaponRangeProfile.RangeNode.InterpolationType.LINEAR, 1.0),
            new WeaponRangeProfile.RangeNode(
                longRange, WeaponRangeProfile.RangeNode.InterpolationType.LINEAR, 1.0),
            new WeaponRangeProfile.RangeNode(
                maxRange, WeaponRangeProfile.RangeNode.InterpolationType.LINEAR, 0));
    WeaponRangeProfile rangeProfile = new WeaponRangeProfile(null, nodes);

    return new EnergyWeapon(
        "",
        "",
        "",
        0,
        aSlots,
        aTons,
        10.0,
        Faction.INNERSPHERE,
        heat,
        coolDown,
        rangeProfile,
        1,
        1,
        1,
        projectileSpeed,
        -1,
        0,
        freeAlpha,
        0,
        burnTime);
  }

  public static EnergyWeapon makePPC(
      int aSlots,
      double aTons,
      double aHeat,
      double aCoolDown,
      double aProjectileSpeed,
      double aLongRange,
      double aMaxRange,
      Collection<String> aSelectors) {
    Attribute heat = new Attribute(aHeat, aSelectors, ModifierDescription.SPEC_WEAPON_HEAT);
    Attribute coolDown =
        new Attribute(aCoolDown, aSelectors, ModifierDescription.SPEC_WEAPON_COOL_DOWN);
    Attribute projectileSpeed =
        new Attribute(
            aProjectileSpeed, aSelectors, ModifierDescription.SPEC_WEAPON_PROJECTILE_SPEED);
    Attribute freeAlpha =
        new Attribute(-1, aSelectors, ModifierDescription.SPEC_WEAPON_MAX_FREE_ALPHA);
    Attribute burnTime = new Attribute(0.0, aSelectors, ModifierDescription.SPEC_WEAPON_DURATION);

    Attribute nearRange = new Attribute(0, aSelectors, ModifierDescription.SPEC_WEAPON_RANGE);
    Attribute longRange =
        new Attribute(aLongRange, aSelectors, ModifierDescription.SPEC_WEAPON_RANGE);
    Attribute maxRange =
        new Attribute(aMaxRange, aSelectors, ModifierDescription.SPEC_WEAPON_RANGE);

    List<WeaponRangeProfile.RangeNode> nodes =
        Arrays.asList(
            new WeaponRangeProfile.RangeNode(
                nearRange, WeaponRangeProfile.RangeNode.InterpolationType.LINEAR, 1.0),
            new WeaponRangeProfile.RangeNode(
                longRange, WeaponRangeProfile.RangeNode.InterpolationType.LINEAR, 1.0),
            new WeaponRangeProfile.RangeNode(
                maxRange, WeaponRangeProfile.RangeNode.InterpolationType.LINEAR, 0));
    WeaponRangeProfile rangeProfile = new WeaponRangeProfile(null, nodes);

    return new EnergyWeapon(
        "",
        "",
        "",
        0,
        aSlots,
        aTons,
        10.0,
        Faction.INNERSPHERE,
        heat,
        coolDown,
        rangeProfile,
        1,
        1,
        1,
        projectileSpeed,
        -1,
        0,
        freeAlpha,
        0,
        burnTime);
  }
}
