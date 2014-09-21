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
package lisong_mechlab.model.chassi;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import lisong_mechlab.model.item.BallisticWeapon;
import lisong_mechlab.model.item.EnergyWeapon;
import lisong_mechlab.model.item.MissileWeapon;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.item.WeaponModifier;
import lisong_mechlab.model.pilot.PilotSkillTree;
import lisong_mechlab.view.LSML;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class represents quirks in the form of movement, health and weapon stats.
 * 
 * @author Emily Björk
 */
public class Quirks implements MovementModifier, WeaponModifier, HealthModifier, HeatModifier {
	private final static DecimalFormat	FORMAT	= new DecimalFormat("###.#");

	enum QuirkBenefit {
		POSITIVE_GOOD, NEGATIVE_GOOD, INDETERMINATE
	}

	private final static transient Map<String, QuirkBenefit>	KNOWN_QUIRKS;

	static {
		KNOWN_QUIRKS = new HashMap<>();

		KNOWN_QUIRKS.put("torso_angle_yaw_additive", QuirkBenefit.POSITIVE_GOOD);
		KNOWN_QUIRKS.put("torso_speed_yaw_multiplier", QuirkBenefit.POSITIVE_GOOD);
		KNOWN_QUIRKS.put("torso_angle_pitch_additive", QuirkBenefit.POSITIVE_GOOD);
		KNOWN_QUIRKS.put("torso_speed_pitch_multiplier", QuirkBenefit.POSITIVE_GOOD);
		KNOWN_QUIRKS.put("arm_angle_yaw_additive", QuirkBenefit.POSITIVE_GOOD);
		KNOWN_QUIRKS.put("arm_speed_yaw_multiplier", QuirkBenefit.POSITIVE_GOOD);
		KNOWN_QUIRKS.put("arm_angle_pitch_additive", QuirkBenefit.POSITIVE_GOOD);
		KNOWN_QUIRKS.put("arm_speed_pitch_multiplier", QuirkBenefit.POSITIVE_GOOD);
		KNOWN_QUIRKS.put("turn_lerp_low_speed_multiplier", QuirkBenefit.POSITIVE_GOOD);
		KNOWN_QUIRKS.put("turn_lerp_mid_speed_multiplier", QuirkBenefit.POSITIVE_GOOD);
		KNOWN_QUIRKS.put("turn_lerp_high_speed_multiplier", QuirkBenefit.POSITIVE_GOOD);
		KNOWN_QUIRKS.put("turn_lerp_low_rate_multiplier", QuirkBenefit.POSITIVE_GOOD);
		KNOWN_QUIRKS.put("turn_lerp_mid_rate_multiplier", QuirkBenefit.POSITIVE_GOOD);
		KNOWN_QUIRKS.put("turn_lerp_high_rate_multiplier", QuirkBenefit.POSITIVE_GOOD);
		KNOWN_QUIRKS.put("heat_loss_multiplier", QuirkBenefit.POSITIVE_GOOD);
		KNOWN_QUIRKS.put("external_heat_multiplier", QuirkBenefit.INDETERMINATE);

		for (HardPointType type : HardPointType.values()) {
			KNOWN_QUIRKS.put(type.toString().toLowerCase() + "_heat_multiplier", QuirkBenefit.NEGATIVE_GOOD);
			KNOWN_QUIRKS.put(type.toString().toLowerCase() + "_cooldown_multiplier", QuirkBenefit.NEGATIVE_GOOD);
		}

		for (Location location : Location.values()) {
			KNOWN_QUIRKS.put("internal_resist_" + location.shortName().toLowerCase() + "_multiplier",
					QuirkBenefit.POSITIVE_GOOD);
			KNOWN_QUIRKS.put("internal_resist_" + location.shortName().toLowerCase() + "_additive",
					QuirkBenefit.POSITIVE_GOOD);
			KNOWN_QUIRKS.put("armor_resist_" + location.shortName().toLowerCase() + "_multiplier",
					QuirkBenefit.POSITIVE_GOOD);
			KNOWN_QUIRKS.put("armor_resist_" + location.shortName().toLowerCase() + "_additive",
					QuirkBenefit.POSITIVE_GOOD);
		}

		// XXX: Known but ignored (present but not used for other than display to the user) quirks:
		KNOWN_QUIRKS.put("overheat_damage_multiplier", QuirkBenefit.NEGATIVE_GOOD);
		KNOWN_QUIRKS.put("reverse_speed_multiplier", QuirkBenefit.POSITIVE_GOOD);
		KNOWN_QUIRKS.put("decel_lerp_low_rate_multiplier", QuirkBenefit.POSITIVE_GOOD);
		KNOWN_QUIRKS.put("decel_lerp_mid_rate_multiplier", QuirkBenefit.POSITIVE_GOOD);
		KNOWN_QUIRKS.put("decel_lerp_high_rate_multiplier", QuirkBenefit.POSITIVE_GOOD);
		KNOWN_QUIRKS.put("accel_lerp_low_rate_multiplier", QuirkBenefit.POSITIVE_GOOD);
		KNOWN_QUIRKS.put("accel_lerp_mid_rate_multiplier", QuirkBenefit.POSITIVE_GOOD);
		KNOWN_QUIRKS.put("accel_lerp_high_rate_multiplier", QuirkBenefit.POSITIVE_GOOD);
	}

	public static class Quirk {
		@XStreamAsAttribute
		public final String	name;
		@XStreamAsAttribute
		public final String	key;
		@XStreamAsAttribute
		public final double	value;

		public Quirk(String aKey, String aName, double aValue) {
			key = aKey;
			name = aName;
			value = aValue;
		}
	}

	private final Map<String, Quirk>	quirks;

	/**
	 * Creates a mew {@link Quirks} object.
	 * 
	 * @param aQuirks
	 *            A {@link Map} of {@link Quirk}s that make up this {@link Quirks}.
	 */
	public Quirks(Map<String, Quirk> aQuirks) {
		if (LSML.getVersion().toLowerCase().contains("dev")) {
			for (String quirk : aQuirks.keySet()) {
				if (!KNOWN_QUIRKS.containsKey(quirk)) {
					System.out.println("Unknown quirk value: " + quirk);
				}
			}
		}
		quirks = aQuirks;
	}

	public String describeAsHtml() {
		if (quirks.isEmpty())
			return "";
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<body>");
		sb.append("<p>Quirks:</p>");
		describeAsHtmlWithoutHeaders(sb);
		sb.append("</body>");
		sb.append("</html>");
		return sb.toString();
	}

	/**
	 * Will describe the contents of this quirks object without emitting HTML head and body tags.
	 * 
	 * @param aOutput
	 *            The {@link StringBuilder} to put the output in.
	 */
	public void describeAsHtmlWithoutHeaders(StringBuilder aOutput) {
		if (quirks.isEmpty())
			return;

		for (Quirk quirk : quirks.values()) {
			final String color;
			switch (isPositiveGood(quirk)) {
				case INDETERMINATE:
					color = "black";
					break;
				case NEGATIVE_GOOD:
					color = (quirk.value < 0) ? "green" : "red";
					break;
				case POSITIVE_GOOD:
					color = (quirk.value > 0) ? "green" : "red";
					break;
				default:
					throw new IllegalArgumentException("Unknown quirkmode!");
			}

			aOutput.append("<p style=\"color:").append(color).append(";\">");
			aOutput.append(quirk.name).append(": ");

			if (quirk.value > 0) {
				aOutput.append("+");
			}
			if (quirk.key.contains("multip")) {
				aOutput.append(FORMAT.format(quirk.value * 100)).append("%");
			} else {
				aOutput.append(FORMAT.format(quirk.value));
			}
			aOutput.append("</p>");
		}
	}

	private QuirkBenefit isPositiveGood(Quirk aQuirk) {
		if (KNOWN_QUIRKS.containsKey(aQuirk.key)) {
			return KNOWN_QUIRKS.get(aQuirk.key);
		}
		return QuirkBenefit.POSITIVE_GOOD; // Most quirks are positive good, assume this if we don't know about the
											// quirk.
	}

	@Override
	public double extraTorsoYawMax(double aBase) {
		return add("torso_angle_yaw_additive");
	}

	@Override
	public double extraTorsoYawSpeed(double aBase) {
		return mult("torso_speed_yaw_multiplier", aBase);
	}

	@Override
	public double extraTorsoPitchMax(double aBase) {
		return add("torso_angle_pitch_additive");
	}

	@Override
	public double extraTorsoPitchSpeed(double aBase) {
		return mult("torso_speed_pitch_multiplier", aBase);
	}

	@Override
	public double extraArmYawMax(double aBase) {
		return add("arm_angle_yaw_additive");
	}

	@Override
	public double extraArmYawSpeed(double aBase) {
		return mult("arm_speed_yaw_multiplier", aBase);
	}

	@Override
	public double extraArmPitchMax(double aBase) {
		return add("arm_angle_pitch_additive");
	}

	@Override
	public double extraArmPitchSpeed(double aBase) {
		return mult("arm_speed_pitch_multiplier", aBase);
	}

	@Override
	public double extraTurnLerpLowSpeed(double aBase) {
		return mult("turn_lerp_low_speed_multiplier", aBase);
	}

	@Override
	public double extraTurnLerpMidSpeed(double aBase) {
		return mult("turn_lerp_mid_speed_multiplier", aBase);
	}

	@Override
	public double extraTurnLerpHighSpeed(double aBase) {
		return mult("turn_lerp_high_speed_multiplier", aBase);
	}

	@Override
	public double extraTurnLerpLowRate(double aBase) {
		return mult("turn_lerp_low_rate_multiplier", aBase);
	}

	@Override
	public double extraTurnLerpMidRate(double aBase) {
		return mult("turn_lerp_mid_rate_multiplier", aBase);
	}

	@Override
	public double extraTurnLerpHighRate(double aBase) {
		return mult("turn_lerp_high_rate_multiplier", aBase);
	}

	@Override
	public boolean affectsWeapon(Weapon aWeapon) {
		if (aWeapon instanceof MissileWeapon
				&& (quirks.containsKey("missile_cooldown_multiplier") || quirks.containsKey("missile_heat_multiplier")))
			return true;
		if (aWeapon instanceof EnergyWeapon
				&& (quirks.containsKey("energy_cooldown_multiplier") || quirks.containsKey("energy_heat_multiplier")))
			return true;
		if (aWeapon instanceof BallisticWeapon
				&& (quirks.containsKey("ballistic_cooldown_multiplier") || quirks
						.containsKey("ballistic_heat_multiplier")))
			return true;
		return false;
	}

	@Override
	public double extraMaxRange(Weapon aWeapon, double aRange, PilotSkillTree aPilotSkillTree) {
		return 0;
	}

	@Override
	public double extraLongRange(Weapon aWeapon, double aRange, PilotSkillTree aPilotSkillTree) {
		return 0;
	}

	@Override
	public double extraWeaponHeat(Weapon aWeapon, double aHeat, PilotSkillTree aPilotSkillTree) {
		return mult(aWeapon.getHardpointType().toString().toLowerCase() + "_heat_multiplier", aHeat);
	}

	@Override
	public double extraCooldown(Weapon aWeapon, double aCooldown, PilotSkillTree aPilotSkillTree) {
		return mult(aWeapon.getHardpointType().toString().toLowerCase() + "_cooldown_multiplier", aCooldown);
	}

	@Override
	public double extraInternalHP(Location aLocation, double aHP) {
		return mult("internal_resist_" + aLocation.shortName().toLowerCase() + "_multiplier", aHP)
				+ add("internal_resist_" + aLocation.shortName().toLowerCase() + "_additive");
	}

	@Override
	public double extraArmor(Location aLocation, double aHP) {
		return mult("armor_resist_" + aLocation.shortName().toLowerCase() + "_multiplier", aHP)
				+ +add("armor_resist_" + aLocation.shortName().toLowerCase() + "_additive");
	}

	private double mult(String aQuirk, double aValue) {
		Quirk quirk = quirks.get(aQuirk);
		if (null != quirk) {
			return aValue * quirk.value;
		}
		return 0;
	}

	private double add(String aQuirk) {
		Quirk quirk = quirks.get(aQuirk);
		if (null != quirk) {
			return quirk.value;
		}
		return 0;
	}

	@Override
	public double extraEnvironmentHeat(double aEnvironmentHeat) {
		return mult("external_heat_multiplier", aEnvironmentHeat);
	}

	@Override
	public double extraHeatDissipation(double aHeat) {
		return mult("heat_loss_multiplier", aHeat);
	}

	@Override
	public double extraHeatGeneration(double aHeat) {
		return 0; // XXX: NYI
	}

	@Override
	public double extraHeatCapacity(double aHeat) {
		return 0; // XXX: NYI
	}
}
