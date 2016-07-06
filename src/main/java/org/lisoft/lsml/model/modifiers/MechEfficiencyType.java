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
package org.lisoft.lsml.model.modifiers;

/**
 * This enumeration lists the different mech efficiencies that are available in the game.
 *
 * @author Emily Björk
 */
public enum MechEfficiencyType {
    COOL_RUN, KINETIC_BURST, TWIST_X, HEAT_CONTAINMENT, HARD_BRAKE, TWIST_SPEED, ARM_REFLEX, ANCHORTURN, QUICKIGNITION, FAST_FIRE, PINPOINT, SPEED_TWEAK, MODULESLOT;

    public static MechEfficiencyType fromMwo(String aName) {
        switch (aName) {
            case "eMTBasic_CoolRun":
                return COOL_RUN;
            case "eMTBasic_KineticBurst":
                return KINETIC_BURST;
            case "eMTBasic_TwistX":
                return TWIST_X;
            case "eMTBasic_HeatContainment":
                return HEAT_CONTAINMENT;
            case "eMTBasic_HardBrake":
                return HARD_BRAKE;
            case "eMTBasic_TwistSpeed":
                return TWIST_SPEED;
            case "eMTBasic_ArmReflex":
                return ARM_REFLEX;
            case "eMTBasic_AnchorTurn":
                return ANCHORTURN;
            case "eMTElite_QuickIgnition":
                return QUICKIGNITION;
            case "eMTElite_FastFire":
                return FAST_FIRE;
            case "eMTElite_PinPoint":
                return PINPOINT;
            case "eMTElite_SpeedTweak":
                return SPEED_TWEAK;
            case "eMTMaster_ModuleSlot":
                return MODULESLOT;
            default:
                throw new IllegalArgumentException("Unknown mwo efficiency string: " + aName);
        }

    }

    public static MechEfficiencyType fromOldName(String aName) {
        switch (aName.toLowerCase()) {
            case "speedtweak":
                return SPEED_TWEAK;
            case "coolrun":
                return COOL_RUN;
            case "heatcontainment":
                return HEAT_CONTAINMENT;
            case "anchorturn":
                return ANCHORTURN;
            case "fastfire":
                return FAST_FIRE;
            case "twistx":
                return TWIST_X;
            case "twistspeed":
                return TWIST_SPEED;
            case "armreflex":
                return ARM_REFLEX;
            default:
                throw new IllegalArgumentException("Unknown old efficiency name: " + aName);
        }
    }

    /**
     * @return <code>true</code> if this {@link MechEfficiencyType} will affect the 'Mech heat.
     */
    public boolean affectsHeat() {
        return this == MechEfficiencyType.COOL_RUN || this == HEAT_CONTAINMENT || this == FAST_FIRE;
    }
}
