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
package lisong_mechlab.mwo_data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lisong_mechlab.model.DataCache;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.quirks.ExternalHeatQuirk;
import lisong_mechlab.model.quirks.MovementQuirk;
import lisong_mechlab.model.quirks.MovementQuirk.Affects;
import lisong_mechlab.model.quirks.Quirk;
import lisong_mechlab.model.quirks.Quirks.QuirkBenefit;
import lisong_mechlab.model.quirks.UnimplementedQuirk;
import lisong_mechlab.model.quirks.WeaponCooldownQuirk;
import lisong_mechlab.model.quirks.WeaponDurationQuirk;
import lisong_mechlab.model.quirks.WeaponHeatQuirk;
import lisong_mechlab.model.quirks.WeaponRangeQuirk;
import lisong_mechlab.model.quirks.WeaponTypeCooldownQuirk;
import lisong_mechlab.model.quirks.WeaponTypeDurationQuirk;
import lisong_mechlab.model.quirks.WeaponTypeHeatQuirk;
import lisong_mechlab.model.quirks.WeaponTypeRangeQuirk;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class XMLQuirk {
    @XStreamAsAttribute
    public String name;
    @XStreamAsAttribute
    public double value;

    public Quirk toQuirk(DataCache aDataCache) {

        // Handle weapon specific quirks without enumerating all of them.
        if (name.startsWith("is")) {
            Pattern p = Pattern.compile("is(.*)_(.*)_(.*)");
            Matcher m = p.matcher(name);
            if (m.matches()) {
                String weapon = m.group(1);
                String attribute = m.group(2);
                String operation = m.group(3);

                Weapon w = (Weapon) DataCache.findItem(weapon, aDataCache.getItems());
                
                String uiName = w.getName() + " " + attribute.toUpperCase();
                
                if (operation.contains("mult")) {
                    if (attribute.equals("cooldown")) {
                        return new WeaponCooldownQuirk(uiName, value, w);
                    }
                    else if (attribute.equals("range")) {
                        return new WeaponRangeQuirk(uiName, value, w);
                    }
                    else if (attribute.equals("duration")) {
                        return new WeaponDurationQuirk(uiName, value, w);
                    }
                    else if (attribute.equals("heat")) {
                        return new WeaponHeatQuirk(uiName, value, w);
                    }
                    else if (attribute.equals("velocity")) {
                        return new UnimplementedQuirk(uiName, value, QuirkBenefit.POSITIVE_GOOD);
                    }
                }
            }
        }
        
        String uiName = Localization.key2string("qrk_" + name);

        switch (name) {
            case "missile_range_multiplier":
                return new WeaponTypeRangeQuirk(uiName, value, HardPointType.MISSILE);
            case "energy_range_multiplier":
                return new WeaponTypeRangeQuirk(uiName, value, HardPointType.ENERGY);
            case "ballistic_range_multiplier":
                return new WeaponTypeRangeQuirk(uiName, value, HardPointType.BALLISTIC);
            case "missile_heat_multiplier":
                return new WeaponTypeHeatQuirk(uiName, value, HardPointType.MISSILE);
            case "energy_heat_multiplier":
                return new WeaponTypeHeatQuirk(uiName, value, HardPointType.ENERGY);
            case "ballistic_heat_multiplier":
                return new WeaponTypeHeatQuirk(uiName, value, HardPointType.BALLISTIC);
            case "missile_cooldown_multiplier":
                return new WeaponTypeCooldownQuirk(uiName, value, HardPointType.MISSILE);
            case "energy_cooldown_multiplier":
                return new WeaponTypeCooldownQuirk(uiName, value, HardPointType.ENERGY);
            case "ballistic_cooldown_multiplier":
                return new WeaponTypeCooldownQuirk(uiName, value, HardPointType.BALLISTIC);
            case "laser_duration_multiplier":
                return new WeaponTypeDurationQuirk(uiName, value, HardPointType.ENERGY);

                // Movement quirks
            case "torsoangle_yaw_additive":
                return new MovementQuirk(uiName, value, Affects.TorsoAngleYawAdd);
            case "torsospeed_yaw_multiplier":
                return new MovementQuirk(uiName, value, Affects.TorsoSpeedYawMult);
            case "torsoangle_pitch_additive":
                return new MovementQuirk(uiName, value, Affects.TorsoAnglePitchAdd);
            case "torsospeed_pitch_multiplier":
                return new MovementQuirk(uiName, value, Affects.TorsoSpeedPitchMult);
            case "armangle_yaw_additive":
                return new MovementQuirk(uiName, value, Affects.ArmAngleYawAdd);
            case "armspeed_yaw_multiplier":
                return new MovementQuirk(uiName, value, Affects.ArmSpeedYawMult);
            case "armangle_pitch_additive":
                return new MovementQuirk(uiName, value, Affects.ArmAnglePitchAdd);
            case "armspeed_pitch_multiplier":
                return new MovementQuirk(uiName, value, Affects.ArmSpeedPitchMult);
            case "turnlerp_lowspeed_multiplier":
                return new MovementQuirk(uiName, value, Affects.TurnLerpLowSpeedMult);
            case "turnlerp_midspeed_multiplier":
                return new MovementQuirk(uiName, value, Affects.TurnLerpMidSpeedMult);
            case "turnlerp_highspeed_multiplier":
                return new MovementQuirk(uiName, value, Affects.TurnLerpHighSpeedMult);
            case "turnlerp_lowrate_multiplier":
                return new MovementQuirk(uiName, value, Affects.TurnLerpLowRateMult);
            case "turnlerp_midrate_multiplier":
                return new MovementQuirk(uiName, value, Affects.TurnLerpMidRateMult);
            case "turnlerp_highrate_multiplier":
                return new MovementQuirk(uiName, value, Affects.TurnLerpHighRateMult);


            case "externalheat_multiplier":
                return new ExternalHeatQuirk(uiName, value);
                
                // Unimplemented negative good quirks
            case "overheat_damage_multiplier":
                return new UnimplementedQuirk(uiName, value, QuirkBenefit.NEGATIVE_GOOD);

                // Unimplemented positive good quirks
            case "missile_velocity_multiplier":
            case "energy_velocity_multiplier":
            case "ballistic_velocity_multiplier":

            case "reversespeed_multiplier":
            case "decellerp_lowrate_multiplier":
            case "decellerp_midrate_multiplier":
            case "decellerp_highrate_multiplier":
            case "accellerp_lowrate_multiplier":
            case "accellerp_midrate_multiplier":
            case "accellerp_highrate_multiplier":
            case "internalresist_rl_additive":
            case "internalresist_ll_additive":
            case "internalresist_ra_additive":
            case "internalresist_la_additive":
            case "internalresist_rt_additive":
            case "internalresist_lt_additive":
            case "internalresist_ct_additive":
            case "internalresist_hd_additive":
            case "armorresist_rl_multiplier":
            case "armorresist_ll_multiplier":
            case "armorresist_ra_multiplier":
            case "armorresist_la_multiplier":
            case "armorresist_lt_multiplier":
            case "armorresist_rt_multiplier":
            case "armorresist_ct_multiplier":
            case "armorresist_hd_multiplier":
            case "armorresist_rl_additive":
            case "armorresist_ll_additive":
            case "armorresist_ra_additive":
            case "armorresist_la_additive":
            case "armorresist_lt_additive":
            case "armorresist_rt_additive":
            case "armorresist_ct_additive":
            case "armorresist_hd_additive":
                return new UnimplementedQuirk(uiName, value, QuirkBenefit.POSITIVE_GOOD);
            default:
                throw new IllegalArgumentException("Unknown quirk");
        }
    }
}