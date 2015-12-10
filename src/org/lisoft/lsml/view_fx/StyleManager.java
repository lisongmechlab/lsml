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
package org.lisoft.lsml.view_fx;

import java.util.HashMap;
import java.util.Map;

import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.JumpJet;

/**
 * This class helps setting consistent CSS classes to various UI elements.
 * 
 * @author Li Song
 *
 */
public class StyleManager {
    public static final String                      CSS_CLASS_EQUIPPED     = "Equipped";

    public static final String                      CSS_CLASS_ARM_STRUT    = "ArmStrut";
    public static final String                      CSS_CLASS_TORSO_STRUT  = "TorsoStrut";

    private static final String                     CSS_COLOUR_MISSILE     = "item-colour-missile";
    private static final String                     CSS_COLOUR_ENERGY      = "item-colour-energy";
    private static final String                     CSS_COLOUR_ECM         = "item-colour-ecm";
    private static final String                     CSS_COLOUR_BALLISTIC   = "item-colour-ballistic";
    private static final String                     CSS_COLOUR_AMS         = "item-colour-ams";
    private static final String                     CSS_COLOUR_MISC        = "item-colour-misc";
    private static final String                     CSS_COLOUR_AMMO_SUFFIX = "-ammo";
    private static final String                     CSS_COLOUR_INTERNAL    = "item-colour-internal";
    private static final String                     CSS_COLOUR_HEAT_SINK   = "item-colour-hs";
    private static final String                     CSS_COLOUR_ENGINE      = "item-colour-engine";
    private static final String                     CSS_COLOUR_JUMP_JET    = "item-colour-jj";
    private static final String                     CSS_COLOUR_DYNAMIC     = "item-colour-dynamic";
    private static final String                     CSS_COLOUR_DISABLED    = "item-colour-disabled";

    private static final Map<HardPointType, String> CSS_HP2COLOUR;

    static {
        CSS_HP2COLOUR = new HashMap<>();
        CSS_HP2COLOUR.put(HardPointType.AMS, CSS_COLOUR_AMS);
        CSS_HP2COLOUR.put(HardPointType.BALLISTIC, CSS_COLOUR_BALLISTIC);
        CSS_HP2COLOUR.put(HardPointType.ECM, CSS_COLOUR_ECM);
        CSS_HP2COLOUR.put(HardPointType.ENERGY, CSS_COLOUR_ENERGY);
        CSS_HP2COLOUR.put(HardPointType.MISSILE, CSS_COLOUR_MISSILE);
        CSS_HP2COLOUR.put(HardPointType.NONE, CSS_COLOUR_MISC);
    }

    public static String getCssColorFor(HardPointType aHardPointType) {
        return CSS_HP2COLOUR.get(aHardPointType);
    }

    public static String getCssColorFor(Item aItem) {
        if (aItem == null)
            return CSS_COLOUR_MISC;

        if (aItem instanceof Ammunition) {
            Ammunition ammunition = (Ammunition) aItem;
            return CSS_HP2COLOUR.get(ammunition.getWeaponHardpointType()) + CSS_COLOUR_AMMO_SUFFIX;
        }

        if (aItem instanceof JumpJet) {
            return CSS_COLOUR_JUMP_JET;
        }

        if (aItem instanceof Engine) {
            return CSS_COLOUR_ENGINE;
        }

        if (aItem instanceof HeatSink) {
            return CSS_COLOUR_HEAT_SINK;
        }

        if (aItem instanceof Internal) {
            return CSS_COLOUR_INTERNAL;
        }

        return getCssColorFor(aItem.getHardpointType());
    }

    public static String getCssColorForDisabled() {
        // TODO Auto-generated method stub
        return CSS_COLOUR_DISABLED;
    }
}
